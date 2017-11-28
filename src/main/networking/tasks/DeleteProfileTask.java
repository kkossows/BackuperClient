package main.networking.tasks;

import javafx.concurrent.Task;
import main.config.ConfigDataManager;
import main.config.GlobalConfig;
import main.user.User;

/**
 * Created by kkossowski on 28.11.2017.
 */
public class DeleteProfileTask extends Task<Boolean> {
    private User user;

    public DeleteProfileTask(User user){
        this.user = user;
    }


    @Override
    protected Boolean call() throws Exception {
        updateMessage("DELETING PROCESS - Please wait...");

        //run delete process
        boolean isSucceeded = user.getServerHandler().deleteUser();
        if (isSucceeded){
            //update view label
            updateMessage("USER DELETED. Application will close soon.");

            //close session connection
            user.getServerHandler().closeConnection();

            //delete local files
            ConfigDataManager.removeUserConfig(user.getUsername());
            if(user.isAutoCompleteOn()){
                ConfigDataManager.createGlobalConfig(new GlobalConfig());
            }

            //some visual effects
            Thread.sleep(300);
        }
        return isSucceeded;
    }
}
