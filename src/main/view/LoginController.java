package main.view;

import javafx.application.Platform;
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

    //--------------Mouse event
    private double xOffset;
    private double yOffset;


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
        if ((getClass().getResource("/fxml/Waiting.fxml")).toString().contains(location.getPath())) {
            //initialize is called when controller needs to load file
            //waiting scene has the same controller so it calls initialize method
            //we do not want to do this, that is why if statement was made

        } else {
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
    }

    public void makeDraggable(Scene scene, Stage stage){
        scene.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override public void handle(MouseEvent mouseEvent) {
                // record a delta distance for the drag and drop operation.
                xOffset = stage.getX() - mouseEvent.getSceneX();
                yOffset = stage.getY() - mouseEvent.getSceneY();
            }
        });
        scene.setOnMouseReleased(new EventHandler<MouseEvent>() {
            @Override public void handle(MouseEvent mouseEvent) {
                stage.setX(mouseEvent.getSceneX() + xOffset);
                stage.setY(mouseEvent.getSceneY() + yOffset);
            }
        });
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
        if (tx_serverIp.getText().trim().isEmpty() || tx_serverPort.getText().trim().isEmpty()
                || tx_username.getText().trim().isEmpty() || tx_username.getText().trim().isEmpty()) {

            showWarningDialog(
                    "MISSING VALUES",
                    "Please, enter all values first."
            );
        }
        else {
            //show waiting screen
            showWaitingScene();

            //declare tasks
            Task<User> loginTask = new Task<User>() {
                @Override
                protected User call() throws Exception {
                    updateMessage("LOGIN PROCESS - Please wait...");
                    User currentUser = null;

                    //create serverHandler and check if server is online
                    ServerHandler serverHandler = new ServerHandler();
                    isServerOnline = serverHandler.isServerOnline(
                            tx_serverIp.getText(),
                            Integer.parseInt(tx_serverPort.getText())
                    );
                    if (isServerOnline) {
                        //make authentication process
                        boolean authenticationSuccess = serverHandler.authenticateUser(
                                tx_username.getText().trim(),
                                tx_password.getText().trim()
                        );

                        if (authenticationSuccess) {
                            //update message
                            updateMessage("LOGIN SUCCEEDED - Loading application view. Please wait...");

                            //create logged in user
                            //user needs only username and serverHandler object with connected socket
                            currentUser = new User(tx_username.getText(), serverHandler);

                            //update configuration files
                            if (cbox_remember.isSelected()) {
                                GlobalConfig newGlobalConfig = new GlobalConfig();

                                newGlobalConfig.setDefaultServerIpAddress(Properties.defaultServerIpAddress);
                                newGlobalConfig.setDefaultServerPortNumber(Properties.defaultServerPortNumber);

                                newGlobalConfig.setSavedServerIpAddress(tx_serverIp.getText());
                                newGlobalConfig.setSavedServerPortNumber(Integer.parseInt(tx_serverPort.getText().trim()));
                                newGlobalConfig.setSavedUsername(tx_username.getText());
                                newGlobalConfig.setSavedPassword(tx_password.getText());

                                ConfigDataManager.createGlobalConfig(newGlobalConfig);

                                //update user flag
                                currentUser.setAutoCompleteOn(true);
                            }
                            else {
                                //verify whether user is saved in globalConfig file - if true, set user flag
                                GlobalConfig globalConfig = ConfigDataManager.readGlobalConfig();
                                if (globalConfig.getSavedServerIpAddress().equals(tx_serverIp.getText())
                                        && globalConfig.getSavedServerPortNumber() == Integer.parseInt(tx_serverPort.getText().trim())
                                        && globalConfig.getSavedUsername().equals(tx_username.getText())
                                        && globalConfig.getSavedPassword().equals(tx_password.getText())) {

                                    //update user flag
                                    currentUser.setAutoCompleteOn(true);
                                }
                                else {
                                    //update user flag
                                    currentUser.setAutoCompleteOn(false);
                                }
                            }

                            //switching stages is make in loginTask.OnSucceeded method
                            //some visual effects
                            try {
                                Thread.sleep(2000);
                            } catch (InterruptedException e1) {
                                e1.printStackTrace();
                            }
                        } //authentication process failed - currentUser is set to null
                    } //server is not online, exception will be thrown so onFailed method will inform about that
                    //in both versions, serverHandler will be deleted so socket will be closed - no need to close it manually
                    //(when authentication correct, serverHandler will be passed through currentUser object to AppController
                    // so it will not be deleted)
                    return currentUser;
                }
            };
            //define what happen after task finished with no error
            loginTask.setOnSucceeded(e -> {
                if (loginTask.getValue() != null) {
                    //switch scenes
                    StackPane appPane = null;
                    AppController appController = new AppController();
                    try {
                        FXMLLoader loader = new FXMLLoader();
                        loader.setLocation(getClass().getResource("/fxml/App.fxml"));

                        //add user to main application controller
                        appController.setUser(loginTask.getValue());

                        //set controller
                        loader.setController(appController);

                        //load FXMLLoader means do initialize method
                        appPane = loader.load();
                    } catch (IOException err) {
                        err.printStackTrace();
                    }

                    Scene appScene = new Scene(appPane);
                    Stage primaryStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                    appController.makeDraggable(appScene, primaryStage);
                    primaryStage.setScene(appScene);
                } else {
                    showWarningDialog(
                            "AUTHENTICATION ERROR",
                            "User not registered or password incorrect"
                    );
                    hideWaitingScene();
                }
            });
            loginTask.setOnFailed(e -> {
                if (loginTask.getException() instanceof NumberFormatException) {
                    showWarningDialog(
                            "WRONG PORT VALUE",
                            "Please, enter only numbers as port value."
                    );
                }
                else {
                    showInformationDialog(
                            "SERVER OFFLINE",
                            "Please, enter correct server variables"
                    );
                }
                hideWaitingScene();
            });

            //bind label to massageProperty to change text from task
            lb_waitingPaneLabel.textProperty().unbind();
            lb_waitingPaneLabel.textProperty().bind(loginTask.messageProperty());

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
        if (tx_serverIp.getText().trim().isEmpty() || tx_serverPort.getText().trim().isEmpty()
                || tx_username.getText().trim().isEmpty() || tx_username.getText().trim().isEmpty()) {

            showWarningDialog(
                    "MISSING VALUES",
                    "Please, enter all values first."
            );
        }
        else {
            //show waiting screen
            showWaitingScene();

            Task<Boolean> registerTask = new Task<Boolean>() {
                @Override
                protected Boolean call() throws Exception {
                    updateMessage("REGISTRATION PROCESS - Please wait...");

                    //create serverHandler and check if server is online
                    ServerHandler serverHandler = new ServerHandler();
                    isServerOnline = serverHandler.isServerOnline(
                            tx_serverIp.getText(),
                            Integer.parseInt(tx_serverPort.getText())
                    );
                    boolean registrationSucceeded = false;
                    if (isServerOnline) {
                        registrationSucceeded = serverHandler.registerUser(
                                tx_username.getText().trim(),
                                tx_password.getText().trim());
                    }
                    //always close connection after registration process
                    serverHandler.closeConnection();
                    return registrationSucceeded;
                }
            };
            //define what happen after task finished with no error
            registerTask.setOnSucceeded(e -> {
                if (registerTask.getValue()) {
                    showInformationDialog(
                            "REGISTRATION SUCCEEDED",
                            "User added correctly. Please login."
                    );
                    hideWaitingScene();
                } else {
                    showWarningDialog(
                            "REGISTRATION ERROR",
                            "User already exists on server side."
                    );
                    hideWaitingScene();
                }
            });
            //define what happen after task finished with error(s)
            registerTask.setOnFailed(e -> {
                if (registerTask.getException() instanceof NumberFormatException) {
                    showWarningDialog(
                            "WRONG PORT VALUE",
                            "Please, enter only numbers as port value."
                    );
                }
                else {
                    showInformationDialog(
                            "SERVER OFFLINE",
                            "Please, enter correct server variables"
                    );
                }
                hideWaitingScene();
            });

            //bind label to massageProperty to change text from task
            lb_waitingPaneLabel.textProperty().unbind();
            lb_waitingPaneLabel.textProperty().bind(registerTask.messageProperty());

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
    private void hideWaitingScene(){
        //remove waiting pane
        container.getChildren().remove(waitingPane);
        //enable login pane
        loginPane.setDisable(false);
    }

    //-------------Other Methods
    private void showInformationDialog(String header, String content){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("INFORMATION!");
        alert.setHeaderText(header);
        alert.setContentText(content);

        alert.showAndWait();
    }
    private void showWarningDialog(String header, String content){
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("WARNING!");
        alert.setHeaderText(header);
        alert.setContentText(content);

        alert.showAndWait();
    }
}
