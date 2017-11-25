package main.config;

import java.io.Serializable;

/**
 * Class for the main application configuration files managing.
 * Created by kkossowski on 18.11.2017.
 */
public class GlobalConfig implements Serializable {

    private String defaultServerIpAddress;
    private int defaultServerPortNumber;

    private String savedServerIpAddress;
    private int savedServerPortNumber;
    private String savedUsername;
    private String savedPassword;


    //set default values
    public GlobalConfig(){
        this.defaultServerIpAddress = Properties.defaultServerIpAddress;
        this.defaultServerPortNumber = Properties.defaultServerPortNumber;

        this.savedServerIpAddress = "";
        this.savedServerPortNumber = 0;
        this.savedUsername = "";
        this.savedPassword = "";

    }

    //If globalConfig has saved values, it means that sb checked remember checkbox
    public boolean isUserRemembered(){
        if (savedServerIpAddress != ""
                && savedServerPortNumber != 0
                && savedUsername != ""
                && savedPassword != "")
            return true;
        else
            return false;
    }

    public String getDefaultServerIpAddress() {
        return defaultServerIpAddress;
    }

    public void setDefaultServerIpAddress(String defaultServerIpAddress) {
        this.defaultServerIpAddress = defaultServerIpAddress;
    }

    public int getDefaultServerPortNumber() {
        return defaultServerPortNumber;
    }

    public void setDefaultServerPortNumber(int defaultServerPortNumber) {
        this.defaultServerPortNumber = defaultServerPortNumber;
    }

    public String getSavedServerIpAddress() {
        return savedServerIpAddress;
    }

    public void setSavedServerIpAddress(String savedServerIpAddress) {
        this.savedServerIpAddress = savedServerIpAddress;
    }

    public int getSavedServerPortNumber() {
        return savedServerPortNumber;
    }

    public void setSavedServerPortNumber(int savedServerPortNumber) {
        this.savedServerPortNumber = savedServerPortNumber;
    }

    public String getSavedUsername() {
        return savedUsername;
    }

    public void setSavedUsername(String savedUsername) {
        this.savedUsername = savedUsername;
    }

    public String getSavedPassword() {
        return savedPassword;
    }

    public void setSavedPassword(String savedPassword) {
        this.savedPassword = savedPassword;
    }
}
