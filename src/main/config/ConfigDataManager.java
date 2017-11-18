package main.config;

import java.io.File;

/**
 * Klasa służąca do statycznego zarzadzania danymi konfiguracyjnymi
 * Created by rkossowski on 18.11.2017.
 */
public class ConfigDataManager {


    public static boolean isAppDirExists(){
        return new File(Properties.appDataDir).exists();
    }
    public static boolean createAppDir(){
        return new File(Properties.appDataDir).mkdir();
    }
    public static boolean isGlobalConfigFileExists(){
        return new File(Properties.globalConfigFile).exists();
    }




    public static boolean createGlobalConfigFile(){
        return false;
    }

    public static GlobalConfig readGlobalConfig(){
        return null;
    }

    public static boolean createGlobalConfig(){
        return false;
    }

    public static UserConfig readUserConfig(){
        return null;
    }

    public static boolean createUserConfig(){
        return false;
    }


}
