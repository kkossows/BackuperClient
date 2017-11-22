package main.config;

import java.io.File;
import java.io.Serializable;
import java.util.List;

/**
 * Class for the user configuration files managing.
 * Created by kkossowski on 18.11.2017.
 */
public class UserConfig implements Serializable {
    String username;
    List<File> userFilesToArchive;


    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public List<File> getUserFilesToArchive() {
        return userFilesToArchive;
    }

    public void setUserFilesToArchive(List<File> userFilesToArchive) {
        this.userFilesToArchive = userFilesToArchive;
    }
}
