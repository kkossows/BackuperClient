package main.view;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
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
/**
 * Created by kkossowski on 18.11.2017.
 */
public class LoginController implements Initializable{

    //-------------FXML Variables LoginPane
    @FXML
    private AnchorPane loginPane;
    @FXML
    private StackPane container ;
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
    private VBox waitingPane;
    @FXML
    private Label lb_waitingPaneLabel;


    //-------------Other variables
    private boolean isServerOnline = false;

    //-------------FXML Methods
    /**
     * Method responsible for initiating login scene.
     *  - if globalConfig file not exists, create it with default values
     *  - if globalConfig file exists and user was remembered, complete all fields
     *  - if globalConfig file exists and user was not remembered, complete only fields associated with server
     * @param location
     * @param resources
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
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
    /**
     * Method responsible for the login process.
     * Steps:
     *  1) check whether server is online
     *  2) show waiting scene
     *  3) create new task loginTask
     *  4) in new task call authenticateUser method from ServerHandler class
     *  5) if authentication was correct:
     *      5.1) update waiting scene label
     *      5.2) create logged in user object
     *      5.3) add user to main application controller
     *      5.4) update configuration files
     *      5.5) verify whether user is saved in globalConfig file
     *  5) show information dialog with registration result
     * @param event
     */
    @FXML
    void btn_login_onClick(ActionEvent event) {
        //new version with Task (no gui frozen)
        isServerOnline = checkIfServerIsOnline();

        if(isServerOnline){
            //declare tasks
            Task<Boolean> loginTask = new Task<Boolean>() {
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


                        //create logged in user
                        //user needs only username and serverHandler object with connected socket
                        User currentUser = new User(tx_username.getText(), serverHandler);

                        //add user to main application controller
                        AppController appController = new AppController();
                        appController.setUser(currentUser);

                        //update configuration files
                        if (cbox_remember.isSelected()) {
                            GlobalConfig newGlobalConfig = new GlobalConfig();

                            newGlobalConfig.setDefaultServerIpAddress(Properties.defaultServerIpAddress);
                            newGlobalConfig.setDefaultServerPortNumber(Properties.defaultServerPortNumber);

                            newGlobalConfig.setSavedServerIpAddress(tx_serverIp.getText());
                            newGlobalConfig.setSavedServerPortNumber(Integer.getInteger(tx_serverPort.getText()));
                            newGlobalConfig.setSavedUsername(tx_username.getText());
                            newGlobalConfig.setSavedPassword(tx_password.getText());

                            ConfigDataManager.createGlobalConfig(newGlobalConfig);

                            //update user flag
                            currentUser.setAutoCompleteOn(true);
                        }

                        //verify whether user is saved in globalConfig file - if true, set user flag
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
            //define what happen after task finished with no error
            loginTask.setOnSucceeded(e -> {
                if(loginTask.getValue()) {
                    //switch scenes
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

            //show waiting screen
            showWaitingScene();

            //run task
            Thread loginThread = new Thread(loginTask);
            loginThread.setDaemon(true);
            loginThread.start();
        }
    }
    /**
     * Method responsible for registering new user.
     * Steps:
     *  1) check whether server is online
     *  2) show waiting scene
     *  3) create new task registerTask
     *  4) in new task call registerUser method from ServerHandler class
     *  5) show information dialog with registration result
     */
    @FXML
    void btn_register_onClick() {
        isServerOnline = checkIfServerIsOnline();
        //new version with Task (no gui frozen)
        if(isServerOnline){
            Task<Boolean> registerTask = new Task<Boolean >() {
                @Override
                protected Boolean call() throws Exception {
                    ServerHandler serverHandler = new ServerHandler();
                    boolean registrationSucceeded;
                    registrationSucceeded = serverHandler.registerUser(
                            tx_username.getText().trim(), tx_username.getText().trim());

                    return registrationSucceeded;
                }
            };
            //define what happen after task finished with no error
            registerTask.setOnSucceeded(e -> {
                if(registerTask.getValue()){
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

            //show waiting screen
            showWaitingScene();

            //run task
            Thread registerThread = new Thread(registerTask);
            registerThread.setDaemon(true);
            registerThread.start();
        }
    }
    @FXML
    void btn_quit_onClick() {
        //close application
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


    //-------------Other Methods
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
