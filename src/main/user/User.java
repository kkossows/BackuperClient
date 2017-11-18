package main.user;

import main.networking.ServerHandler;

/**
 * Created by rkossowski on 18.11.2017.
 */
public class User {
    private String username;
    private ServerHandler serverHandler;

    public User(String username, ServerHandler serverHandler){
        this.username = username;
        this.serverHandler = serverHandler;
    }


}
