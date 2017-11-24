package main.config;

import java.io.*;
import java.util.ArrayList;

/**
 * Static class for static management of configuration files
 * Created by kkossowski on 18.11.2017.
 */
public class ConfigDataManager {

    public static boolean isAppDirExists(){
        return new File(Properties.appDataDir).exists();
    }
    public static boolean createAppDir(){
        return new File(Properties.appDataDir).mkdir();
    }

    public static boolean isGlobalConfigFileExists(){
        return new File(Properties.appDataDir + Properties.globalConfigFile).exists();
    }
    public static GlobalConfig readGlobalConfig()  {
        File globalConfigFile = new File(Properties.appDataDir + Properties.globalConfigFile);
        ObjectInputStream ois = null;
        GlobalConfig globalConfig = null;

        try {
            ois = new ObjectInputStream(new FileInputStream(globalConfigFile));
            globalConfig = (GlobalConfig)ois.readObject();
            ois.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return globalConfig;
    }
    public static void createGlobalConfig(GlobalConfig globalConfig){
        File globalConfigFile = new File(Properties.appDataDir + Properties.globalConfigFile);

        if(globalConfigFile.exists())
            globalConfigFile.delete();

        try {
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(globalConfigFile));
            oos.writeObject(globalConfig);
            oos.close();

        } catch (IOException e) {
            e.printStackTrace();

        }
    }

    public static boolean isUserConfigFileExists(String username){
        return new File(Properties.appDataDir + username + ".dat").exists();
    }
    public static UserConfig readUserConfig(String username){
        File userConfigFile = new File(Properties.appDataDir + username + ".dat");
        ObjectInputStream ois = null;
        UserConfig userConfig = null;

        try {
            ois = new ObjectInputStream(new FileInputStream(userConfigFile));
            userConfig = (UserConfig)ois.readObject();
            ois.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return userConfig;
    }
    public static void createUserConfig(UserConfig userConfig){
        File userConfigFile = new File(Properties.appDataDir + userConfig.getUsername() + ".dat");

        //change array in userConfig
        userConfig.setUserFilesToArchive(new ArrayList<File>(userConfig.getUserFilesToArchive()));

        if (userConfigFile.exists())
            userConfigFile.delete();

        try {
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(userConfigFile));
            oos.writeObject(userConfig);
            oos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void removeUserConfig(String username){
        if(isUserConfigFileExists(username))
            new File(Properties.appDataDir + username + ".dat").delete();
    }


}
