package main.view;

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
import main.config.UserConfig;
import main.user.User;

import java.io.File;
import java.net.URL;
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
    /*
    Metoda inicjalizuje wszystkie pola:
    1) wpisuje zalogowane użytkownika w miejsce username
    2) wczytuje wszystkie pliki do archiwizacji używane przez użytkownika
    3) wczytuje wszystkie pliki zarchiwizowane na serwerze
    4) przypisuje wszystkie obserwowane listy do ich reprezentacji w widoku
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
        //otherwise chande stage showOpenDialog(...) to showOpenDialog(null)
        File file = fileChooser.showOpenDialog((Stage) ((Node) event.getSource()).getScene().getWindow());
        if (file != null) {
            filesToArchive.add(file);
        }

        //only with closing the app, userConfigFile is updated
    }
    @FXML
    public void btn_removeFile_OnClick(ActionEvent event) {
        int index = lv_filesToArchive.getSelectionModel().getSelectedIndex();
        if (index > -1) {
            filesToArchive.remove(index);
        }
    }
    @FXML
    public void btn_removeAllFiles_OnClick(ActionEvent event){
        filesToArchive.clear();
    }


    @FXML
    public void btn_backupOnlySelected_OnClick(ActionEvent event){

    }
    @FXML
    public void btn_backupAll_OnClick(ActionEvent event){

    }


    @FXML
    public void btn_showVersionFile_OnClick(ActionEvent event){
        //get selected file index and find it in arraylist to get require file
        File selectedFile = filesOnServer.get(lv_filesOnServer.getSelectionModel().getSelectedIndex());

        //get list from server
        ArrayList<String> receivedList = user.getServerHandler().getAllFileVersionsFromServer(selectedFile);

        //add hole list to observable list
        fileVersions.addAll(receivedList);

        //set file name label
        lb_fileName.setText(selectedFile.getAbsolutePath());
    }
    @FXML
    public void btn_removeFileFromServer_OnClick(ActionEvent event){
        //methode remove all versions of selected file
        int indexOfSelectedFile = lv_filesOnServer.getSelectionModel().getSelectedIndex();

        boolean isConfirmed = showConfirmationDialog(
                "Removing file means remove all file backup versions on server\n"
                        + "File: " + filesOnServer.get(indexOfSelectedFile),
                "Continue?");

        if(isConfirmed){
            boolean fileRemoved = user.getServerHandler().removeSelectedFile(
                    filesOnServer.get(indexOfSelectedFile)
            );

            if(fileRemoved){
                //remove selected file from list and view
                filesOnServer.remove(indexOfSelectedFile);
            }
        }

        //when user clicked cancel, nothing happend
    }
    @FXML
    public void btn_restoreSelectedFileVersion_OnClick(ActionEvent event){

    }
    @FXML
    public void btn_removeFileVersion_OnClick(ActionEvent event){
        int indexOfSelectedFileVersion = lv_fileVersions.getSelectionModel().getSelectedIndex();

        boolean isConfirmed = showConfirmationDialog(
                "Remove " + "\n"
                        + "File: " + lb_fileName.getText() + "\n"
                        + "Version: " + fileVersions.get(indexOfSelectedFileVersion) ,
                "Continue?");

        if (isConfirmed){
            boolean fileVersionRemoved = user.getServerHandler().removeSelectedFileVersion(
                    lb_fileName.getText(),
                    fileVersions.get(indexOfSelectedFileVersion)
            );

            if(fileVersionRemoved){
                //remove selected file version from list and view
                fileVersions.remove(indexOfSelectedFileVersion);
            }
        }
    }


    @FXML
    public void btn_quit_OnClick(ActionEvent event){
        //update userConfigFile
        UserConfig newUserConfig = new UserConfig();
        newUserConfig.setUsername(user.getUsername());
        newUserConfig.setUserFilesToArchive(filesToArchive);
        ConfigDataManager.createUserConfig(newUserConfig);

        //close main stage
        Stage primaryStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        primaryStage.close();
    }
    @FXML
    public void btn_minimize_OnClick(ActionEvent event){
        Stage primaryStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        primaryStage.setIconified(true);
    }


    @FXML
    public void mi_autocomplete_OnClick(ActionEvent event){

    }

    //-----------------------Other Methodes
    /*
    Metoda, dzięki której jesteśmy w stanie przekazać obecnego użytkownika do aplikacji
     */
    public void setUser(User user){
        this.user = user;
    }


    private void showInformationDialog(String title, String content){
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
}
