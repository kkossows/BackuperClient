package main.config;

import java.io.Serializable;

/**
 * Klasa dotycząca głównego pliku konfiguracyjnego aplikacji.
 * Created by rkossowski on 18.11.2017.
 */
public class GlobalConfig implements Serializable {

    private String serverIpAddress;
    private int serverPortNumber;
    private String savedUserLogin;

    public GlobalConfig(){

    }


    public String getServerIpAddress() {
        return serverIpAddress;
    }

    public void setServerIpAddress(String serverIpAddress) {
        this.serverIpAddress = serverIpAddress;
    }

    public int getServerPortNumber() {
        return serverPortNumber;
    }

    public void setServerPortNumber(int serverPortNumber) {
        this.serverPortNumber = serverPortNumber;
    }

    public String getSavedUserLogin() {
        return savedUserLogin;
    }

    public void setSavedUserLogin(String savedUserLogin) {
        this.savedUserLogin = savedUserLogin;
    }

    public boolean isSavedUserLoginSet()
    {
        if(savedUserLogin == null || savedUserLogin == "")
            return false;
        else
            return true;
    }
}
