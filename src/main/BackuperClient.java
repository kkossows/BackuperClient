package main;
import javafx.application.Application;
import main.config.ConfigDataManager;
import main.config.GlobalConfig;
import main.view.ClientApplication;

public class BackuperClient {

    public static void main(String[] args) {
        //create configuration directory
        if (!ConfigDataManager.isAppDirExists())
            ConfigDataManager.createAppDir();

        Application.launch(ClientApplication.class, args);
    }
}
