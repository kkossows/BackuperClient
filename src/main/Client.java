package main;
import javafx.application.Application;
import main.config.ConfigDataManager;
import main.view.ClientApplication;

public class Client {

    public static void main(String[] args) {
        if (!ConfigDataManager.isAppDirExists())
            ConfigDataManager.createAppDir();

        Application.launch(ClientApplication.class, args);
    }
}
