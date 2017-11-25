package main.networking.tasks;

import javafx.concurrent.Task;
import main.user.User;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by kkossowski on 25.11.2017.
 */
public class ShowVersionsTask extends Task<ArrayList<String>> {
    private File file;
    private User user;

    public ShowVersionsTask(File file, User user){
        this.file = file;
        this.user = user;
    }

    @Override
    protected ArrayList<String> call() throws Exception {
        //return received list from server
        return user.getServerHandler().getAllFileVersionsFromServer(file);
    }
}
