package main.view;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import main.config.ConfigDataManager;
import main.config.GlobalConfig;
import main.config.UserConfig;
import main.networking.BackupTask;
import main.networking.RestoreTask;
import main.user.User;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Created by kkossowski on 18.11.2017.
 */
public class AppController implements Initializable {

    //----------------------FXML Variables
    @FXML
    private AnchorPane appPane;
    @FXML
    private StackPane container;
    @FXML
    private Label lb_username;
    @FXML
    private Label lb_fileName;
    @FXML
    private Label lb_filePath;
    @FXML
    private Label lb_progressType;
    @FXML
    private Label lb_copying;
    @FXML
    private ProgressBar progressBar;
    @FXML
    private ProgressIndicator progressIndicator;
    @FXML
    private ProgressIndicator progress_versions;
    @FXML
    private Button btn_showVersionFile;

    @FXML
    private ListView<File> lv_filesToArchive;
    private ObservableList<File> filesToArchive = FXCollections.observableArrayList();
    @FXML
    private ListView<File> lv_filesOnServer;
    private ObservableList<File> filesOnServer = FXCollections.observableArrayList();
    @FXML
    private ListView<String> lv_fileVersions;
    private ObservableList<String> fileVersions = FXCollections.observableArrayList();

    @FXML
    private MenuItem mi_autocomplete;


    //-------------FXML Variables WaitingPane
    @FXML
    private VBox waitingPane;
    @FXML
    private Label lb_waitingPaneLabel;

    //----------------------Other Variables
    private User user;

    //--------------Mouse event
    private double xOffset;
    private double yOffset;

    //----------------------FXML Methods

    /**
     * Method responsible for initiating main application scene.
     * 1) set user name
     * 2) load all files that user backed up on last session and show them in Files to archive listView
     * 3) load files from server side and show them in Files on server listView
     * 4) set all observable lists to appropriate views
     * 5) turn on or off menuItem with autocomplete - decision based on user flag
     * 6) hide progress statistics
     *
     * @param location
     * @param resources
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if ((getClass().getResource("/fxml/Waiting.fxml")).toString().contains(location.getPath())) {
            //initialize is called when controller needs to load file
            //waiting scene has the same controller so it calls initialize method
            //we do not want to do this, that is why if statement was made

        } else{
            //set user name
            lb_username.setText(user.getUsername());

            //load all files that user backed up on last session
            filesToArchive.addAll(user.getUserFilesToArchive());
            //set list to view
            lv_filesToArchive.setItems(filesToArchive);
            lv_filesToArchive.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

            //load files from server side
            filesOnServer.addAll(user.getServerHandler().getBackupFilesListFromServer());
            //set list to view
            lv_filesOnServer.setItems(filesOnServer);
            lv_filesOnServer.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

            //set last list to view
            lv_fileVersions.setItems(fileVersions);
            lv_fileVersions.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

            //turn on or off menuItem with autocomplete based on user flag
            if (!user.isAutoCompleteOn())
                mi_autocomplete.setDisable(false);

            //hide progress statistics
            hideProgressStatistics();

            //hide progress
            progress_versions.setDisable(true);
            progress_versions.setVisible(false);
        }
    }


    public void makeDraggable(Scene scene, Stage stage) {
        scene.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                // record a delta distance for the drag and drop operation.
                xOffset = stage.getX() - mouseEvent.getSceneX();
                yOffset = stage.getY() - mouseEvent.getSceneY();
            }
        });
        scene.setOnMouseReleased(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                stage.setX(mouseEvent.getSceneX() + xOffset);
                stage.setY(mouseEvent.getSceneY() + yOffset);
            }
        });
    }

    private void showProgressStatistics() {
        lb_filePath.setVisible(true);
        lb_progressType.setVisible(true);
        lb_copying.setVisible(true);
        progressBar.setVisible(true);
        progressIndicator.setVisible(true);

        //clear
        progressBar.progressProperty().unbind();
        progressBar.setProgress(0);
        progressIndicator.progressProperty().unbind();
        progressIndicator.setProgress(0);
    }
    private void hideProgressStatistics() {
        lb_filePath.setVisible(false);
        lb_progressType.setVisible(false);
        lb_copying.setVisible(false);
        progressBar.setVisible(false);
        progressIndicator.setVisible(false);
    }

    @FXML
    public void btn_addFile_OnClick(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        //new window has to be closed to continue operation on main window
        //otherwise change stage showOpenDialog(...) to showOpenDialog(null)
        File file = fileChooser.showOpenDialog(((Node) event.getSource()).getScene().getWindow());
        if (file != null) {
            filesToArchive.add(file);
        }

        //userConfigFile is updated only with closing the app
    }
    @FXML
    public void btn_removeSelectedFile_OnClick() {
        int index = lv_filesToArchive.getSelectionModel().getSelectedIndex();
        if (index > -1) {
            filesToArchive.remove(index);
        } else {
            showWarningDialog(
                    "Error",
                    "No file selected. Please first select the file and press button again."
            );
        }
    }
    @FXML
    public void btn_removeAllFiles_OnClick() {
        filesToArchive.clear();
    }


    @FXML
    public void btn_backupAll_OnClick() {
        if (filesToArchive.size() == 0) {
            showWarningDialog(
                    "No files to archive.",
                    "Please, add file(s) and click the button again."
            );
        } else {
            //create new Task
            makeBackup(filesToArchive);
        }

    }
    @FXML
    public void btn_backupOnlySelected_OnClick() {
        //check if user selected file
        int index = lv_filesToArchive.getSelectionModel().getSelectedIndex();
        if (index > -1) {
            //create new Task
            makeBackup(
                    new ArrayList<File>(){{ filesToArchive.get(index); }}
            );
        } else {
            showWarningDialog(
                    "Error",
                    "No file selected. Please first select the file and press button again."
            );
        }
    }
    private void makeBackup(List<File> filesToArchive){
        //create new Task
        Task<Boolean> backupTask = new BackupTask(
                user.getServerHandler().getSocket(),
                user.getServerHandler().getIn(),
                user.getServerHandler().getOut(),
                filesToArchive,
                filesOnServer
        );
        //define what happen after task finished with no error
        backupTask.setOnSucceeded(e ->{
            if(backupTask.getValue()){
                showInformationDialog(
                        "All files sent.",
                        "Backup complete."
                );
            }
        });
        //bind variables
        progressBar.progressProperty().unbind();
        progressBar.progressProperty().bind(backupTask.progressProperty());
        progressIndicator.progressProperty().unbind();
        progressIndicator.progressProperty().bind(backupTask.progressProperty());
        lb_filePath.textProperty().unbind();
        lb_filePath.textProperty().bind(backupTask.messageProperty());

        //set statistics title
        lb_progressType.setText("Backup files:");

        //show statistics
        showProgressStatistics();

        //run task
        Thread backupThread = new Thread(backupTask);
        backupThread.setDaemon(true);
        backupThread.start();
    }

    @FXML
    public void btn_showVersionFile_OnClick() {
        //verify whether user select any file
        final int indexOfSelectedFile = lv_filesOnServer.getSelectionModel().getSelectedIndex();

        if(indexOfSelectedFile != -1){
            //disable showVersion button until one download finished
            btn_showVersionFile.setDisable(true);
            btn_showVersionFile.setVisible(false);

            //show progress ring
            progress_versions.setDisable(false);

            //set file name label
            lb_fileName.setText(filesOnServer.get(indexOfSelectedFile).getAbsolutePath());

            //declare tasks
            Task<ArrayList<String>> showVersionTask = new Task<ArrayList<String>>() {
                @Override
                protected ArrayList<String> call() throws Exception {
                    //get selected file indexOfSelectedFile and find it in arraylist to get require file
                    File selectedFile = filesOnServer.get(indexOfSelectedFile);

                    //return received list from server
                    return user.getServerHandler().getAllFileVersionsFromServer(selectedFile);
                }
            };
            //define what happen after task finished with no error
            showVersionTask.setOnSucceeded(e -> {
                //add hole list to observable list
                fileVersions.addAll(showVersionTask.getValue());


                //hide progress
                progress_versions.setDisable(true);

                //unlock buttons
                btn_showVersionFile.setDisable(false);
                btn_showVersionFile.setVisible(true);
            });
            //run task
            Thread showVersionThread = new Thread(showVersionTask);
            showVersionThread.setDaemon(true);
            showVersionThread.start();

        } else {
            showWarningDialog(
                    "Error",
                    "No file selected. Please first select the file and press button again."
            );
        }
    }
    @FXML
    public void btn_removeSelectedFileFromServer_OnClick() {
        //method remove all versions of selected file
        int indexOfSelectedFile = lv_filesOnServer.getSelectionModel().getSelectedIndex();

        if (indexOfSelectedFile > -1) {
            final boolean isConfirmed = showConfirmationDialog(
                    "Removing file means remove all file backup versions on server\n"
                            + "File: " + filesOnServer.get(indexOfSelectedFile),
                    "Continue?");


            //declare tasks
            Task<Boolean> removeFileTask = new Task<Boolean>() {
                @Override
                protected Boolean call() throws Exception {
                    if (isConfirmed) {
                        boolean fileRemoved = user.getServerHandler().removeSelectedFile(
                                filesOnServer.get(indexOfSelectedFile)
                        );
                        return fileRemoved;
                    } else {
                        return false;
                    }
                }
            };
            removeFileTask.setOnSucceeded(e -> {
                if (removeFileTask.getValue()) {
                    //remove selected file from list and view
                    if (lb_fileName.getText().equals(filesOnServer.get(indexOfSelectedFile).getAbsolutePath())) {
                        fileVersions.clear();
                        lb_fileName.setText("");
                    }

                    //remove selected file from list and view
                    filesOnServer.remove(indexOfSelectedFile);
                }
            });
            //run task
            Thread removeFileThread = new Thread(removeFileTask);
            removeFileThread.setDaemon(true);
            removeFileThread.start();
        } else {
            showWarningDialog(
                    "Error",
                    "No file selected. Please first select the file and press button again."
            );
        }
    }
    @FXML
    public void btn_restoreSelectedFileVersion_OnClick() {
        //WARNING! - restored file will not be added to files to archive!

        int indexOfSelectedVileVersion = lv_fileVersions.getSelectionModel().getSelectedIndex();

        if (indexOfSelectedVileVersion > -1) {
            //check if file is already on local file system
            //if it is, ask if user want to override it or create new file with
            String backupFilePath = lb_fileName.getText();
            String backupFileVersion = fileVersions.get(indexOfSelectedVileVersion);
            File backupFile = new File(backupFilePath);

            if (backupFile.exists()) {
                //file exist on local system file

                //verify version of file on local file system
                SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy_HH-mm-ss");
                String localFileVersion = sdf.format(backupFile.lastModified());

                if (backupFileVersion.equals(localFileVersion)) {
                    //same versions - show information and return from method

                    showInformationDialog(
                            "Same file versions",
                            "Selected version file already exists on local system file."
                    );
                    return;

                } else {
                    //different versions

                    //ask user
                    int questionAnswer = showYesNoDialog(
                            "Different file version on local file system.",
                            "Do you want to override file?\nYES-override\nNO-create new file with version in name"

                    );

                    //preparing empty File
                    switch (questionAnswer) {
                        case 1:
                            //user want to override existing file
                            backupFile.delete();
                        case 0:
                            //user want to create new file with version in name
                            //new file name example: oldName_version.txt -> test_11-19-2017_20-45-13.txt
                            String newBackupFileName;
                            String[] fileNameSplitter = (backupFile.getName()).split("\\.");
                            newBackupFileName = fileNameSplitter[0] + "_" + backupFileVersion + "." + fileNameSplitter[1];

                            String newBackupFilePath;
                            newBackupFilePath = backupFile.getParent() + "/" + newBackupFileName;

                            backupFile = new File(newBackupFilePath);
                            break;
                        case -1:
                            //user canceled operation - end method
                            return;
                    }
                    //create empty file
                    try {
                        backupFile.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                //file not exist on local file system

                //create sub-folders and empty file
                try {
                    backupFile.getParentFile().mkdirs();
                    backupFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            //create new Task
            Task<Boolean> restoringTask = new RestoreTask(
                    user.getServerHandler().getSocket(),
                    user.getServerHandler().getIn(),
                    user.getServerHandler().getOut(),
                    backupFile, backupFilePath, backupFileVersion
            );
            //bind variables
            progressBar.progressProperty().unbind();
            progressBar.progressProperty().bind(restoringTask.progressProperty());
            progressIndicator.progressProperty().unbind();
            progressIndicator.progressProperty().bind(restoringTask.progressProperty());
            lb_filePath.textProperty().unbind();
            lb_filePath.textProperty().bind(restoringTask.messageProperty());

            //set statistics title
            lb_progressType.setText("Restoring file:");

            //show statistics
            showProgressStatistics();

            //define what happen after task finished with no error
            restoringTask.setOnSucceeded(e -> {
                if (restoringTask.getValue()) {

                    showInformationDialog(
                            "Restoring file finished.",
                            ""
                    );
                }
            });
            //run task
            Thread restoringThread = new Thread(restoringTask);
            restoringThread.setDaemon(true);
            restoringThread.start();
        } else {
            showWarningDialog(
                    "Error",
                    "No file selected. Please first select the file and press button again."
            );
        }
    }
    @FXML
    public void btn_removeFileVersion_OnClick() {
        int indexOfSelectedFileVersion = lv_fileVersions.getSelectionModel().getSelectedIndex();

        if (indexOfSelectedFileVersion > -1) {
            boolean isConfirmed = showConfirmationDialog(
                    "Remove " + "\n"
                            + "File: " + lb_fileName.getText() + "\n"
                            + "Version: " + fileVersions.get(indexOfSelectedFileVersion),
                    "Continue?");

            //declare tasks
            Task<Boolean> removeFileVersionTask = new Task<Boolean>() {
                @Override
                protected Boolean call() throws Exception {
                    if (isConfirmed) {
                        boolean fileVersionRemoved = user.getServerHandler().removeSelectedFileVersion(
                                lb_fileName.getText(),
                                fileVersions.get(indexOfSelectedFileVersion)
                        );
                        return fileVersionRemoved;
                    } else {
                        return false;
                    }
                }
            };
            removeFileVersionTask.setOnSucceeded(e -> {
                if (removeFileVersionTask.getValue()) {
                    //if it was the last version, remove also file from filesOnServerList
                    if (fileVersions.size() == 1) {
                        int indexOfFileWithNoVersion = -1;
                        //find file
                        for (int i = 0; i < filesOnServer.size(); i++) {
                            if (filesOnServer.get(i).getAbsolutePath().equals(lb_fileName.getText())) {
                                indexOfFileWithNoVersion = i;
                                break;
                            }
                        }
                        //remove file from list
                        if (indexOfFileWithNoVersion != -1) {
                            filesOnServer.remove(indexOfFileWithNoVersion);
                        }
                        //remove file name label
                        lb_fileName.setText("");
                    }

                    //remove selected file version from list and view
                    fileVersions.remove(indexOfSelectedFileVersion);
                }
            });
            //run task
            Thread removeFileVersionThread = new Thread(removeFileVersionTask);
            removeFileVersionThread.setDaemon(true);
            removeFileVersionThread.start();

        }
        else {
            showWarningDialog(
                    "Error",
                    "No file selected. Please first select the file and press button again."
            );
        }
    }


    @FXML
    public void btn_minimize_OnClick(ActionEvent event){
        Stage primaryStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        primaryStage.setIconified(true);
    }
    @FXML
    public void btn_quit_OnClick(){
        //show waiting screen
        showWaitingScene();

        //declare tasks
        Task<Boolean> logoutTask = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                updateMessage("LOGOUT PROCEDURE. Please wait...");

                //logout user
                boolean isSucceeded = user.getServerHandler().logoutUser();

                if (isSucceeded) {
                    //show information
                    updateMessage("USER LOGOUT. Application will close soon...");

                    //close session connection
                    user.getServerHandler().closeConnection();

                    //update userConfigFile
                    UserConfig newUserConfig = new UserConfig();
                    newUserConfig.setUsername(user.getUsername());
                    newUserConfig.setUserFilesToArchive(filesToArchive);
                    ConfigDataManager.createUserConfig(newUserConfig);

                    //some visual effects
                    Thread.sleep(4000);
                }
                return isSucceeded;
            }
        };
        //define what happen after task finished with no error
        logoutTask.setOnSucceeded(e -> {
            if (logoutTask.getValue())
            {
                //close application
                Platform.exit();
            }
        });
        //bind label
        lb_waitingPaneLabel.textProperty().unbind();
        lb_waitingPaneLabel.textProperty().bind(logoutTask.messageProperty());

        //run task
        Thread logoutThread = new Thread(logoutTask);
        logoutThread.setDaemon(true);
        logoutThread.start();
    }


    /**
     * Method used to switch off automatic filling login form when user clicked remember me.
     * (delete all variables from globalConfig file and set only default ones)
     */
    @FXML
    public void mi_autocomplete_OnClick(){
        ConfigDataManager.createGlobalConfig(new GlobalConfig());
        showInformationDialog(
                "Disable autocomplete login formula done",
                "Operation succeeded"
        );
    }
    /**
     * Method responsible for the delete user process.
     * 1) delete all files in local storage system
     * 2) delete all files remotely on server storage system
     * 3) quit application
     */
    @FXML
    public void mi_deleteProfile_OnClick(){
        boolean isConfirmed = showConfirmationDialog(
                "Deleting user will remove all backup data on server.",
                "Continue?"
        );

        //deleting also logout on server side
        if(isConfirmed){
            //show waiting screen
            showWaitingScene();

            Task<Boolean> deleteProfile = new Task<Boolean>() {
                @Override
                protected Boolean call() throws Exception {
                    updateMessage("DELETING PROCESS - Please wait...");

                    boolean isSucceeded = user.getServerHandler().deleteUser();
                    if (isSucceeded){
                        //update view label
                        updateMessage("USER DELETED. Application will close soon.");

                        //close session connection
                        user.getServerHandler().closeConnection();

                        //delete local files
                        ConfigDataManager.removeUserConfig(lb_username.getText());
                        if(user.isAutoCompleteOn()){
                            ConfigDataManager.createGlobalConfig(new GlobalConfig());
                        }

                        //some visual effects
                        Thread.sleep(300);
                    }
                    return isSucceeded;
                }
            };
            deleteProfile.setOnSucceeded(event -> {
                if(deleteProfile.getValue()){
                    showInformationDialog(
                            "Your profile has been deleted.",
                            "Application will be closed."
                    );
                    //close application
                    Platform.exit();
                }
            });
            //bind label
            lb_waitingPaneLabel.textProperty().unbind();
            lb_waitingPaneLabel.textProperty().bind(deleteProfile.messageProperty());

            //run task in new thread
            Thread deleteThread = new Thread(deleteProfile);
            deleteThread.setDaemon(true);
            deleteThread.start();
        }
    }
    /**
     * Method display information dialog with application details.
     */
    @FXML
    public void mi_about_OnClick(){
        showInformationDialog(
                "BACKUPER - backup your file to remote server",
                "Version: 1.0\n More information in user manual."
        );
    }


    private void showWaitingScene(){
        if (waitingPane == null){
            //load waiting controller
            try {
                FXMLLoader waitingLoader = new FXMLLoader(
                        getClass().getResource("/fxml/Waiting.fxml")
                );
                waitingLoader.setController(this);
                waitingPane = (VBox) waitingLoader.load();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //set style
        waitingPane.setStyle("-fx-background-color: rgba(0, 0, 0, 0.5);");
        waitingPane.setMaxSize(800.0, 400.0);
        //add waitingPane to view
        container.getChildren().add(waitingPane);
        //disable login pane
        appPane.setDisable(true);
    }
    private void deleteWaitingScene(){
        //remove waiting pane
        container.getChildren().remove(waitingPane);
        //enable login pane
        appPane.setDisable(false);
    }

    //-----------------------Other Methods
    /**
     * Metoda, dzięki której jesteśmy w stanie przekazać obecnego użytkownika do aplikacji
     */
    public void setUser(User user){
        this.user = user;
    }


    //-----------------------Dialog Methods
    public void showInformationDialog(String header, String content){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("INFORMATION");
        alert.setHeaderText(header);
        alert.setContentText(content);

        alert.showAndWait();
    }
    private void showWarningDialog(String header, String content){
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("WARNING");
        alert.setHeaderText(header);
        alert.setContentText(content);

        alert.showAndWait();
    }
    private boolean showConfirmationDialog(String headerText, String contentText){
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation Dialog");
        alert.setHeaderText(headerText);
        alert.setContentText(contentText);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK){
            return true;
        } else {
            return false;
        }
    }
    private int showYesNoDialog(String headerText, String contentText) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("YES/NO Dialog");
        alert.setHeaderText(headerText);
        alert.setContentText(contentText);

        ButtonType buttonYES = new ButtonType("YES");
        ButtonType buttonNO = new ButtonType("NO");
        ButtonType buttonCANCEL = new ButtonType("CANCEL");
        alert.getButtonTypes().setAll(buttonYES, buttonNO);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == buttonYES){
            return 1;
        } else if (result.get() == buttonNO) {
            return 0;
        } else if (result.get() == buttonCANCEL) {
            return -1;
        } else {
            return -2;
        }
    }
}
