package main.view;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

/**
 * Created by rkossowski on 18.11.2017.
 */
public class ClientApplication extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        /*
        Application always starts from login and register scene.
         */
        Parent rootPane = FXMLLoader.load(getClass().getResource("/fxml/Login.fxml"));
        Scene loginScene = new Scene(rootPane);
        primaryStage.initStyle(StageStyle.UNDECORATED);
        primaryStage.setScene(loginScene);
        primaryStage.show();

    }
}
