package main.networking.tasks;

import javafx.concurrent.Task;
import main.user.User;

import java.io.File;

/**
 * Created by kkossowski on 25.11.2017.
 */
public class RemoveFileTask extends Task<Boolean> {
    private boolean isConfirmed;
    private User user;
    private File file;

    public RemoveFileTask(boolean isConfirmed, User user, File file){
        this.isConfirmed = isConfirmed;
        this.user = user;
        this.file = file;
    }


    @Override
    protected Boolean call() throws Exception {
        if (isConfirmed)
            return user.getServerHandler().removeSelectedFile(file);
        else
            return false;
    }
}
