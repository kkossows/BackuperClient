package main.view;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import main.config.ConfigDataManager;
import main.config.GlobalConfig;
import main.networking.ServerHandler;
import main.user.User;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class LoginController implements Initializable{

    //-------------FXML Variables
    @FXML
    private Button btn_login;
    @FXML
    private Button btn_register;
    @FXML
    private Button btn_quit;
    @FXML
    private TextField tx_serverIp;
    @FXML
    private TextField tx_serverPort;
    @FXML
    private TextField tx_username;
    @FXML
    private PasswordField tx_password;
    @FXML
    private CheckBox cbox_remember;


    //-------------Other variables
    private boolean isServerOnline = false;

    //-------------FXML Methodes
    /*
    Metoda sprawdzająca, czy mozna wczytac dane z plików konfiguracyjnych, jezeli istnieją.
    1) jeżeli plik istnieje to wyciągnij z niego dane na temat serwera
    2) jeżeli w pliku znajduje się nazwa użytkownika to otwórz plik i wpisz dane użytkownika z pliku konfig.
    3) jeżeli nie istnieje, to nic nie rób - przy pierwszym logowaniu stworzy się plik
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (ConfigDataManager.isGlobalConfigFileExists()){
            GlobalConfig globalConfig = ConfigDataManager.readGlobalConfig();

            tx_serverIp.setText(globalConfig.getServerIpAddress());
            tx_serverPort.setText(Integer.toString(globalConfig.getServerPortNumber()));

            if(globalConfig.isSavedUserLoginSet()){

            }
        }
    }

    /*
    Metoda składająca się z następujących elmentów
     1) sprawdź, czy serwer jest online
     2) stworz dedykowanego ServerHandlera i spróbuj się zalogować
     3) stwórz obiekt klienta
     4) zmień scene na aplikacje
     5) przekaź klienta do kontrolera aplikacji
     */
    @FXML
    void btn_login_onClick(ActionEvent event) {
        isServerOnline = checkIfServerIsOnline();

        if(isServerOnline){
            ServerHandler serverHandler = new ServerHandler();
            boolean authenticationSuccess;
            authenticationSuccess = serverHandler.authenticateUser(
                    tx_username.getText().trim(), tx_username.getText().trim());

            if(authenticationSuccess){
                User currentUser = new User();
                ///DODODODODOODODODO

            }
            else {
                showWarningDialog("AUTHENTICATION ERROR", "User not registered or password incorrect");
            }

        }
        else {
            //po prostu kończę metodę, wszystkie alerty obłużone w metodzie checkIfSererIsOnline()
            return;
        }

    }
    /*
    Metoda umożliwiająca zdalne utworzenie nowego użytkownika w serwisie.
     1) sprawdź, czy serwer jest online
     2) stworz dedykowanego ServerHandlera i spróbuj zarejestrować użytkownika
     3) wyświetl komunikat o sukcesie lub błędzie (bład wystąpi wtedy, gdy dany użytkownik jest już zarejestrowany)
     */
    @FXML
    void btn_register_onClick(ActionEvent event) throws IOException {
        isServerOnline = checkIfServerIsOnline();

        if(isServerOnline){
            ServerHandler serverHandler = new ServerHandler();
            boolean registrationSucceeded;
            registrationSucceeded = serverHandler.registerUser(
                    tx_username.getText().trim(), tx_username.getText().trim());

            if(registrationSucceeded){
                showInformationDialog("REGISTRATION SUCCEEDED", "User added correctly. Please login.");
                return;
            }
            else {
                showWarningDialog("REGISTRATION ERROR", "User already exists in service.");
                return;
            }
        }
        else {
            //po prostu kończę metodę, wszystkie alerty obłużone w metodzie checkIfSererIsOnline()
            return;
        }
    }
    @FXML
    void btn_quit_onClick(ActionEvent event) {
        Stage primaryStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        primaryStage.close();
    }


    //-------------Other Methodes
    boolean checkIfServerIsOnline(){
        // trim usuwa białe znaki
        if (tx_serverIp.getText().trim().isEmpty() || tx_serverPort.getText().trim().isEmpty()
                || tx_username.getText().trim().isEmpty() || tx_username.getText().trim().isEmpty()){

            showWarningDialog("MISSING VALUES", "Please, enter all values first.");
            return false;
        }
        else {
            //trzeba sprawdzić, czy użytkownik wpisał same liczby w miejscu port
            try {
                isServerOnline = ServerHandler.isServerOnline(tx_serverIp.getText(), Integer.parseInt(tx_serverPort.getText()));
            }
            catch (NumberFormatException e) {
                showWarningDialog("WRONG PORT VALUE", "Please, enter only numbers as port value.");
                return false;
            }

            if (!isServerOnline){
                showInformationDialog("SERVER OFFLINE", "Please, enter correct server variables");
                return false;
            }
            else {
                //serwer jest dostępny
                return true;
            }
        }
    }

    void showInformationDialog(String title, String content){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(content);

        alert.showAndWait();
    }

    void showWarningDialog(String title, String content){
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setContentText(content);

        alert.showAndWait();
    }






    /*
    //przykład na zmianę scenografi
        AnchorPane appPane = FXMLLoader.load(getClass().getResource("/fxml/App.fxml"));
        Scene appScene = new Scene(appPane);
        Stage primaryStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        primaryStage.setScene(appScene);
     */
}
