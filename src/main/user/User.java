package main.user;

import main.config.ConfigDataManager;
import main.config.UserConfig;
import main.networking.ServerHandler;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by rkossowski on 18.11.2017.
 */
public class User {
    private String username;
    private ServerHandler serverHandler;
    List<File> userFilesToArchive;


    public User(String username, ServerHandler serverHandler){
        this.username = username;
        this.serverHandler = serverHandler;
        this.userFilesToArchive = loadUserFilesToArchiveFromConfigFile();
    }

    //Methode load file list from config file
    //If file not exist, it create it with empty list
    private List<File> loadUserFilesToArchiveFromConfigFile(){
        UserConfig userConfig;
        if(ConfigDataManager.isUserConfigFileExists(this.username)){
            userConfig = ConfigDataManager.readUserConfig(this.username);
        }
        else {
            userConfig = new UserConfig();
            userConfig.setUsername(this.username);
            userConfig.setUserFilesToArchive(new ArrayList<File>());
            ConfigDataManager.createUserConfig(userConfig);
        }
        return userConfig.getUserFilesToArchive();
    }




    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public ServerHandler getServerHandler() {
        return serverHandler;
    }

    public void setServerHandler(ServerHandler serverHandler) {
        this.serverHandler = serverHandler;
    }

    public List<File> getUserFilesToArchive() {
        return userFilesToArchive;
    }

    public void setUserFilesToArchive(List<File> userFilesToArchive) {
        this.userFilesToArchive = userFilesToArchive;
    }

}
