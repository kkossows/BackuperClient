package main.user;

import main.config.ConfigDataManager;
import main.config.UserConfig;
import main.networking.ServerHandler;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by kkossowski on 18.11.2017.
 */
public class User {
    private String username;
    private ServerHandler serverHandler;
    List<File> userFilesToArchive;
    //variable needs to verify, if user variables are saved in globalConfig file to autocomplete
    boolean autoCompleteOn;

    public User(String username, ServerHandler serverHandler){
        this.username = username;
        this.serverHandler = serverHandler;
        this.userFilesToArchive = loadUserFilesToArchiveFromConfigFile();
        this.autoCompleteOn = false;
    }

    /**
     * Method responsible for loading files list from config file.
     * - if file not exist, it create it with empty list
     * @return list of files to archive
     */
    private List<File> loadUserFilesToArchiveFromConfigFile(){
        UserConfig userConfig;
        if(ConfigDataManager.isUserConfigFileExists(this.username)){
            userConfig = ConfigDataManager.readUserConfig(this.username);
        }
        else {
            userConfig = new UserConfig();
            userConfig.setUsername(this.username);
            userConfig.setUserFilesToArchive(new ArrayList<>());
            ConfigDataManager.createUserConfig(userConfig);
        }
        return userConfig.getUserFilesToArchive();
    }

    public String getUsername() {
        return username;
    }
    public ServerHandler getServerHandler() {
        return serverHandler;
    }
    public List<File> getUserFilesToArchive() {
        return userFilesToArchive;
    }
    public boolean isAutoCompleteOn() {
        return autoCompleteOn;
    }

    public void setAutoCompleteOn(boolean autoCompleteOn) {
        this.autoCompleteOn = autoCompleteOn;
    }
}
