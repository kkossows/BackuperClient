package main.view;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import main.config.ConfigDataManager;
import main.config.GlobalConfig;
import main.config.UserConfig;
import main.user.User;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Created by rkossowski on 18.11.2017.
 */
public class AppController implements Initializable{

    //----------------------FXML Variables
    @FXML
    private Label lb_username;
    @FXML
    private Label lb_fileName;
    @FXML
    private Label lb_statistics_number;
    @FXML
    private Label lb_statistics_percent;
    @FXML
    private Label lb_statistics_percentRestoring;

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


    //----------------------Other Variables
    private User user;



    //----------------------FXML Methodes
    /**
     * Metoda inicjalizuje wszystkie pola:
     * 1) wpisuje zalogowane użytkownika w miejsce username
     * 2) wczytuje wszystkie pliki do archiwizacji używane przez użytkownika
     * 3) wczytuje wszystkie pliki zarchiwizowane na serwerze
     * 4) przypisuje wszystkie obserwowane listy do ich reprezentacji w widoku
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        lb_username.setText(user.getUsername());

        //load all files that user archivate on last session
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
        if(!user.isAutoCompleteOn())
            mi_autocomplete.setDisable(false);
    }

    @FXML
    public void btn_addFile_OnClick(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        //new window has to be closed to continue operation on main window
        //otherwise change stage showOpenDialog(...) to showOpenDialog(null)
        File file = fileChooser.showOpenDialog((Stage) ((Node) event.getSource()).getScene().getWindow());
        if (file != null) {
            filesToArchive.add(file);
        }

        //only with closing the app, userConfigFile is updated
    }
    @FXML
    public void btn_removeSelectedFile_OnClick(ActionEvent event) {
        int index = lv_filesToArchive.getSelectionModel().getSelectedIndex();
        if (index > -1) {
            filesToArchive.remove(index);
        }
        else {
            showWarningDialog(
                    "Error",
                    "No file selected. Please first select the file and press button again."
            ) ;
        }
    }
    @FXML
    public void btn_removeAllFiles_OnClick(ActionEvent event){
        filesToArchive.clear();
    }


    @FXML
    public void btn_backupAll_OnClick(ActionEvent event){
        if (filesToArchive.size() == 0){
            showWarningDialog(
                    "No files to archive.",
                    "Please, add file(s) and click the button again."
            );
        }
        else {
            user.getServerHandler().backupAllFiles(
                    filesToArchive,
                    this
            );
        }

    }
    @FXML
    public void btn_backupOnlySelected_OnClick(ActionEvent event) {
        //check if user selected file
        int index = lv_filesToArchive.getSelectionModel().getSelectedIndex();
        if (index > -1) {
            user.getServerHandler().backupOnlySelectedFile(
                    filesToArchive.get(index),
                    this
            );
        }
        else {
            showWarningDialog(
                    "Error",
                    "No file selected. Please first select the file and press button again."
            ) ;
        }
    }


    @FXML
    public void btn_showVersionFile_OnClick(ActionEvent event){
        //verify whether user select any file
        int indexOfSelectedFile =  lv_filesOnServer.getSelectionModel().getSelectedIndex();

        if(indexOfSelectedFile > -1) {
            //get selected file indexOfSelectedFile and find it in arraylist to get require file
            File selectedFile = filesOnServer.get(indexOfSelectedFile);

            //get list from server
            ArrayList<String> receivedList = user.getServerHandler().getAllFileVersionsFromServer(selectedFile);

            //add hole list to observable list
            fileVersions.addAll(receivedList);

            //set file name label
            lb_fileName.setText(selectedFile.getAbsolutePath());
        }
        else {
            showWarningDialog(
                    "Error",
                    "No file selected. Please first select the file and press button again."
            ) ;
        }
    }
    @FXML
    public void btn_removeSelectedFileFromServer_OnClick(ActionEvent event){
        //method remove all versions of selected file
        int indexOfSelectedFile = lv_filesOnServer.getSelectionModel().getSelectedIndex();

        if (indexOfSelectedFile > -1) {
            boolean isConfirmed = showConfirmationDialog(
                    "Removing file means remove all file backup versions on server\n"
                            + "File: " + filesOnServer.get(indexOfSelectedFile),
                    "Continue?");

            if (isConfirmed) {
                boolean fileRemoved = user.getServerHandler().removeSelectedFile(
                        filesOnServer.get(indexOfSelectedFile)
                );

                //remove all version from view if file was proceeded before
                if (lb_fileName.getText().equals(filesOnServer.get(indexOfSelectedFile).getAbsolutePath())){
                    fileVersions.clear();
                    lb_fileName.setText("");
                }

                //remove selected file from list and view
                if (fileRemoved) {
                    filesOnServer.remove(indexOfSelectedFile);
                }
            }
        }
        else {
            showWarningDialog(
                    "Error",
                    "No file selected. Please first select the file and press button again."
            ) ;
        }
    }
    @FXML
    public void btn_restoreSelectedFileVersion_OnClick(ActionEvent event){
        int indexOfSelectedVileVersion = lv_fileVersions.getSelectionModel().getSelectedIndex();

        if (indexOfSelectedVileVersion > -1){
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

                            backupFile = new File (newBackupFilePath);
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
            }
            else {
                //file not exist on local file system

                //create sub-folders and empty file
                try {
                    backupFile.getParentFile().mkdirs();
                    backupFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            //run serverHandler method
            user.getServerHandler().restoreSelectedFileVersion(
                    backupFilePath,
                    backupFileVersion,
                    backupFile,
                    this
            );
        }
        else {
            showWarningDialog(
                    "Error",
                    "No file selected. Please first select the file and press button again."
            ) ;
        }
    }
    @FXML
    public void btn_removeFileVersion_OnClick(ActionEvent event){
        int indexOfSelectedFileVersion = lv_fileVersions.getSelectionModel().getSelectedIndex();

        if (indexOfSelectedFileVersion > -1) {
            boolean isConfirmed = showConfirmationDialog(
                    "Remove " + "\n"
                            + "File: " + lb_fileName.getText() + "\n"
                            + "Version: " + fileVersions.get(indexOfSelectedFileVersion),
                    "Continue?");

            if (isConfirmed) {
                boolean fileVersionRemoved = user.getServerHandler().removeSelectedFileVersion(
                        lb_fileName.getText(),
                        fileVersions.get(indexOfSelectedFileVersion)
                );

                //if it was the last version, remove also file from filesOnServerList
                if (fileVersions.size() == 1){
                    int indexOfFileWithNoVersion = -1;
                    //find file
                    for (int i = 0; i < filesOnServer.size(); i++){
                        if(filesOnServer.get(i).getAbsolutePath().equals(lb_fileName.getText())){
                            indexOfFileWithNoVersion = i;
                            break;
                        }
                    }
                    //remove file from list
                    if (indexOfFileWithNoVersion != -1){
                        filesOnServer.remove(indexOfFileWithNoVersion);
                    }
                    //remove file name label
                    lb_fileName.setText("");
                }

                //remove selected file version from list and view
                if (fileVersionRemoved) {
                    fileVersions.remove(indexOfSelectedFileVersion);
                }
            }
        }
        else {
            showWarningDialog(
                    "Error",
                    "No file selected. Please first select the file and press button again."
            ) ;
        }
    }



    @FXML
    public void btn_minimize_OnClick(ActionEvent event){
        Stage primaryStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        primaryStage.setIconified(true);
    }
    @FXML
    public void btn_quit_OnClick(ActionEvent event){
        //logout user
        boolean isSucceeded = user.getServerHandler().logoutUser(lb_username.getText());

        if (isSucceeded){
            //close session connection
            user.getServerHandler().closeConnection();

            //update userConfigFile
            UserConfig newUserConfig = new UserConfig();
            newUserConfig.setUsername(user.getUsername());
            newUserConfig.setUserFilesToArchive(filesToArchive);
            ConfigDataManager.createUserConfig(newUserConfig);

            //close application
            Platform.exit();
        }
    }




    /**
    * Metoda pozwala wyłączyć autouzupełnianie formularza logującego.
    * (usuwa z pliku globalconfig dane użytkownika)
     */
    @FXML
    public void mi_autocomplete_OnClick(ActionEvent event){
        ConfigDataManager.createGlobalConfig(new GlobalConfig());
        showInformationDialog("Disable autocomplete login formula done", "Operation succeeded");
    }
    /**
    Metoda pozwala usunąć użytkownika z serwera.
    - usuwa pliki w pamięci lokalnej
    - usuwa pliki w pamięci zdalnej
     */
    @FXML
    public void mi_deleteProfile_OnClick(ActionEvent event){
        boolean isConfirmed = showConfirmationDialog(
                "Deleting user will remove all backup data on server.",
                "Continue?"
        );

        if(isConfirmed){
            boolean isSucceeded = user.getServerHandler().deleteUser(lb_username.getText());
            if (isSucceeded){
                //close session connection
                user.getServerHandler().closeConnection();

                //delete local files
                ConfigDataManager.removeUserConfig(lb_username.getText());
                if(user.isAutoCompleteOn()){
                    ConfigDataManager.createGlobalConfig(new GlobalConfig());
                }

                showInformationDialog(
                        "Your profile has been deleted.",
                        "Application will be closed."
                );
                //close application
                Platform.exit();
            }
        }
    }
    /**
     * Metoda wyświetlająca podstawowe dane na temat aplikacji.
     */
    @FXML
    public void mi_about_OnClick(ActionEvent event){
        showInformationDialog(
                "BACKUPER - backup your file to remote server",
                "Version: 1.0\n More information in user manual."
        );
    }


    //-----------------------Other Methodes
    /**
     * Metoda, dzięki której jesteśmy w stanie przekazać obecnego użytkownika do aplikacji
     */
    public void setUser(User user){
        this.user = user;
    }


    //-----------------------Dialog Methodes
    public void showInformationDialog(String title, String content){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(content);

        alert.showAndWait();
    }
    private void showWarningDialog(String title, String content){
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
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

    //-----------------------Public Methodes
    public void setStatisticLabelWithNumber(int currentNumber, int finalNumber){
        lb_statistics_number.setText(currentNumber + "/" + finalNumber);
    }
    public void setStatisticLabelWithPercent(int percentValue){
        lb_statistics_percent.setText(percentValue + "%");
    }
    public void setStatisticLabelWithPercentRestoring(int percentValue){
        lb_statistics_percentRestoring.setText(percentValue + "%");
    }

    public void addFileOnServerList(File newFile){
        filesOnServer.add(newFile);
    }
}
