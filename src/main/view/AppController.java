package main.view;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import main.config.ConfigDataManager;
import main.config.UserConfig;
import main.user.User;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by rkossowski on 18.11.2017.
 */
public class AppController implements Initializable{

    //----------------------FXML Variables
    @FXML
    private Label lb_username;

    @FXML
    private ListView<File> lv_filesToArchive;
    private ObservableList<File> filesToArchive = FXCollections.observableArrayList();
    @FXML
    private ListView<File> lv_filesOnServer;
    private ObservableList<File> filesOnServer = FXCollections.observableArrayList();
    @FXML
    private ListView<String> lv_fileVersions;
    private ObservableList<String> fileVersions = FXCollections.observableArrayList();



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

        //load files from server side
        filesOnServer.addAll(user.getServerHandler().getBackupFilesListFromServer());
        //set list to view
        lv_filesOnServer.setItems(filesOnServer);

        //set last list to view
        lv_fileVersions.setItems(fileVersions);
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

    }
    @FXML
    public void btn_removeFileFromServer_OnClick(ActionEvent event){
        //methode remove all versions of selected file
    }
    @FXML
    public void btn_restoreSelectedFileVersion_OnClick(ActionEvent event){

    }
    @FXML
    public void btn_removeFileVersion_OnClick(ActionEvent event){

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



    //-----------------------Other Methodes
    /*
    Metoda, dzięki której jesteśmy w stanie przekazać obecnego użytkownika do aplikacji
     */
    public void setUser(User user){
        this.user = user;
    }
}
