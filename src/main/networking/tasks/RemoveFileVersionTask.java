package main.networking.tasks;

import javafx.concurrent.Task;
import main.user.User;

import java.io.File;

/**
 * Created by rkossowski on 25.11.2017.
 */
public class RemoveFileVersionTask extends Task<Boolean> {
    private boolean isConfirmed;
    private User user;
    private String filePath;
    private String fileVersion;

    public RemoveFileVersionTask(boolean isConfirmed, User user, String filePath, String fileVersion){
        this.isConfirmed = isConfirmed;
        this.user = user;
        this.filePath = filePath;
        this.fileVersion = fileVersion;
    }


    @Override
    protected Boolean call() throws Exception {
        if (isConfirmed)
            return user.getServerHandler().removeSelectedFileVersion(filePath, fileVersion);
        else
            return false;
    }
}