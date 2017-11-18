package main.config;

import java.io.Serializable;

/**
 * Klasa opisująca pliki konfiguracyjne dotyczące poszczególnych użytkowników.
 * Created by rkossowski on 18.11.2017.
 */
public class UserConfig implements Serializable {
    String username;
    String password;
    String serverIp;
    int serverPortNumber;

    public String getServerIp() {
        return serverIp;
    }

    public void setServerIp(String serverIp) {
        this.serverIp = serverIp;
    }

    public int getServerPortNumber() {
        return serverPortNumber;
    }

    public void setServerPortNumber(int serverPortNumber) {
        this.serverPortNumber = serverPortNumber;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
