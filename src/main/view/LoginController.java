package main.view;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import main.config.ConfigDataManager;
import main.config.GlobalConfig;
import main.config.Properties;
import main.networking.ServerHandler;
import main.user.User;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class LoginController implements Initializable{

    //-------------FXML Variables LoginPane
    @FXML
    private AnchorPane loginPane;
    @FXML
    private StackPane container ;
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

    //-------------FXML Variables WaitingPane
    @FXML
    private Label lb_waitingPaneLabel;
    @FXML
    private VBox waitingPane;


    //-------------Other variables
    private boolean isServerOnline = false;

    //-------------FXML Methodes
    /*
    Metoda sprawdzająca, czy mozna wczytac dane z plików konfiguracyjnych, jezeli istnieją.
    - jeżeli plik nie istnieje, to tworzymy go z domyslnymi wartościami;
    - jeżeli plik zawiera wszystkie zmienne zapisanego użytkownika, to wpisz je automatycznie
    - jeżeli plik nie zawiera wszystkich zmiennych, wpisz wartości domyślne z pliku konfiguracyjnego dotyczące serwer
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        //if global config file not exist, create it with default variables
        if (!ConfigDataManager.isGlobalConfigFileExists()) {
            ConfigDataManager.createGlobalConfig(new GlobalConfig());
        }

        GlobalConfig globalConfig = ConfigDataManager.readGlobalConfig();
        if (globalConfig.isUserRemembered()) {
            tx_serverIp.setText(globalConfig.getSavedServerIpAddress());
            tx_serverPort.setText(Integer.toString(globalConfig.getSavedServerPortNumber()));
            tx_username.setText(globalConfig.getSavedUsername());
            tx_password.setText(globalConfig.getSavedPassword());
            cbox_remember.setSelected(true);
        } else {
            tx_serverIp.setText(globalConfig.getDefaultServerIpAddress());
            tx_serverPort.setText(Integer.toString(globalConfig.getDefaultServerPortNumber()));
        }
    }


    /*
    Metoda składająca się z następujących elmentów
     1) sprawdź, czy serwer jest online
     2) stworz dedykowanego ServerHandlera i spróbuj się zalogować
     3) jeżeli użytkownik zaznaczył checkbox, zapisz dane do pliku globalconfig
     4) stwórz obiekt klienta
     5) zmień scene na aplikacje
     6) przekaź klienta do kontrolera aplikacji
     */
    @FXML
    void btn_login_onClick(ActionEvent event) {
        //new version with Task (no gui frozen)
        isServerOnline = checkIfServerIsOnline();

        if(isServerOnline){
            showWaitingScene();
            //declare tasks
            Task<Boolean> loginTask = new Task<Boolean >() {
                @Override
                protected Boolean call() throws Exception {
                    ServerHandler serverHandler = new ServerHandler();
                    boolean authenticationSuccess;
                    authenticationSuccess = serverHandler.authenticateUser(
                            tx_username.getText().trim(),
                            tx_password.getText().trim()
                    );

                    if(authenticationSuccess) {
                        //update message
                        updateMessage("LOGIN SUCCEEDED - Loading application view. Please wait...");


                        //stwórz bierzącego użytkownika
                        User currentUser = new User(tx_username.getText(), serverHandler);
                        AppController appController = new AppController();
                        appController.setUser(currentUser);

                        //aktualizacja plików konfiguracyjnych
                        if (cbox_remember.isSelected()) {
                            GlobalConfig newGlobalConfig = new GlobalConfig();

                            newGlobalConfig.setDefaultServerIpAddress(Properties.defaultServerIpAddress);
                            newGlobalConfig.setDefaultServerPortNumber(Properties.defaultServerPortNumber);

                            newGlobalConfig.setSavedServerIpAddress(tx_serverIp.getText());
                            newGlobalConfig.setSavedServerPortNumber(Integer.getInteger(tx_serverPort.getText()));
                            newGlobalConfig.setSavedUsername(tx_username.getText());
                            newGlobalConfig.setSavedPassword(tx_password.getText());

                            ConfigDataManager.createGlobalConfig(newGlobalConfig);

                            //zaktualizuj flage u uzytkownika
                            currentUser.setAutoCompleteOn(true);
                        }

                        //sprawdz, czy użytkownik nie jest zapisany w pliku globalnym
                        //jezeli jest, ustaw flagę na true
                        GlobalConfig globalConfig = ConfigDataManager.readGlobalConfig();
                        if (globalConfig.getSavedServerIpAddress().equals(tx_serverIp.getText())
                                && globalConfig.getSavedServerPortNumber() == Integer.getInteger(tx_serverPort.getText())
                                && globalConfig.getSavedUsername().equals(tx_username.getText())
                                && globalConfig.getSavedPassword().equals(tx_password.getText())) {
                            currentUser.setAutoCompleteOn(true);
                        }

                        //switching stages is make in loginTask.OnSucceeded method
                    }
                    return authenticationSuccess;
                }
            };
            //what happen after task finished with no error
            loginTask.setOnSucceeded(e -> {

                System.out.println("okolasdad");

                if(loginTask.getValue()) {
                    //przełącz sceny z logowania na główną scene aplikacji
                    //(zrobione inaczej aby dodać już utworzony kontroler)
                    StackPane appPane = null;
                    try {
                        FXMLLoader loader = new FXMLLoader();
                        loader.setController(this);

                        //load FXMLLoader mean do initialize method
                        appPane = loader.load(getClass().getResource("/fxml/App.fxml"));
                    } catch (IOException err) {
                        err.printStackTrace();
                    }

                    Scene appScene = new Scene(appPane);
                    Stage primaryStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                    primaryStage.setScene(appScene);
                }
                else {
                    showWarningDialog(
                            "AUTHENTICATION ERROR",
                            "User not registered or password incorrect"
                    );
                    deleteWaitingScene();
                    return;
                }
            });
            //set prompt text
            lb_waitingPaneLabel.setText("LOGIN PROCESS - Please wait...");
            //bind label to massageProperty to change text from task
            lb_waitingPaneLabel.textProperty().bind(loginTask.messageProperty());
            //run task
            Thread loginThread = new Thread(loginTask);
            loginThread.setDaemon(true);
            loginThread.start();
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
        //new version with Task (no gui frozen)
        if(isServerOnline){
            showWaitingScene();

            Task<Boolean> registerTast = new Task<Boolean >() {
                @Override
                protected Boolean call() throws Exception {
                    ServerHandler serverHandler = new ServerHandler();
                    boolean registrationSucceeded;
                    registrationSucceeded = serverHandler.registerUser(
                            tx_username.getText().trim(), tx_username.getText().trim());

                    return registrationSucceeded;
                }
            };
            //what happen after task finished with no error
            registerTast.setOnSucceeded(e -> {
                if(registerTast.getValue()){
                    showInformationDialog(
                            "REGISTRATION SUCCEEDED",
                            "User added correctly. Please login."
                    );
                    deleteWaitingScene();
                    return;
                }
                else {
                    showWarningDialog(
                            "REGISTRATION ERROR",
                            "User already exists on server side."
                    );
                    deleteWaitingScene();
                    return;
                }
            });
            //set prompt text
            lb_waitingPaneLabel.setText("REGISTRATION PROCESS - Please wait...");
            //run task
            Thread registerThread = new Thread(registerTast);
            registerThread.setDaemon(true);
            registerThread.start();
        }
    }
    @FXML
    void btn_quit_onClick(ActionEvent event) {
        Platform.exit();
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
        //add waitingPane to view
        container.getChildren().add(waitingPane);
        //disable login pane
        loginPane.setDisable(true);
    }
    private void deleteWaitingScene(){
        //remove waiting pane
        container.getChildren().remove(waitingPane);
        //enable login pane
        loginPane.setDisable(false);
    }


    //-------------Other Methodes
    private boolean checkIfServerIsOnline(){
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

}
