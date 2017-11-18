package main.config;

/**
 * Klasa zawiera zbiór danych używanych w aplikacji.
 * (łatwe do ewentualnej zmiany)
 * Created by rkossowski on 18.11.2017.
 */
public class Properties {

    public static String appName			= "backuper-client";
    public static String appDataDir 		= System.getenv("APPDATA") + "\\" + appName + "\\";
    public static String globalConfigFile	= "globalConfig.dat";

}

