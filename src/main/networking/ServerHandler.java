package main.networking;

import javafx.collections.ObservableList;
import main.view.AppController;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by rkossowski on 18.11.2017.
 */
public class ServerHandler {
    private static Socket socket;
    private BufferedReader in;
    private PrintWriter out;


    /*
    Konstruktor klasy.
    Tworzy strumienie wejściowe i wyjściowe strumienia.
     */
    public ServerHandler(){
       try {
           in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
           out = new PrintWriter(socket.getOutputStream(), true);
       } catch (IOException e) {
           e.printStackTrace();
       }
    }


    /*
    Metoda statycnza - sprawdza, czy możemy utworzyć połaczenie z serwerem.
    Zapisuje gniazdo do pola statycznego, aby nie było konieczności tworzenia go ponownie.
     */
    public static boolean isServerOnline(String serverIpAddress, int serverPortNumber){
        try {
            socket = new Socket(serverIpAddress, serverPortNumber);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }


    /*
    Metoda służąca do autentykacji i autoryzacji użytkownika.
    (sprawdza, czy dany użytkownik istnieje na serwerze i czy dane hasło jest zgodne z hasłem na serwerze).
    true - użytkownik poprawnie podał dane
    false - użytkownik podał błedne dane
     */
    public boolean authenticateUser(String username, String password) {
        try {
            out.println(ClientMessage.LOG_IN.name());
            if (in.readLine().equals(ServerMessage.GET_USERNAME.name())) {
                out.println(username);
                if (in.readLine().equals(ServerMessage.GET_PASSWORD.name())) {
                    out.println(password);
                    if (in.readLine().equals(ServerMessage.LOGIN_SUCCESS.name())) {
                        return true;
                    } else if (in.readLine().equals(ServerMessage.LOGIN_FAILED.name())) {
                        return false;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        //dorobic jakas funkcjonalnosc, jakby sie cos pomylilo w komunikacji
        return false;
    }

    /*
    Metoda odpowiedzialna za zdalne dodawaine użytkownika do listy użytkowników serwisu.
    true - poprawnie stworzono użytkownika
    false - użytkownik już istnieje na liście użytkowników serwisu
     */
    public boolean registerUser(String username, String password) {
        try {
            out.println(ClientMessage.REGISTER.name());
            if (in.readLine().equals(ServerMessage.GET_USERNAME.name())) {
                out.println(username);
                if (in.readLine().equals(ServerMessage.GET_PASSWORD.name())) {
                    out.println(password);
                    if (in.readLine().equals(ServerMessage.USER_CREATED.name())) {
                        return true;
                    }
                    else if(in.readLine().equals(ServerMessage.USER_EXISTS.name())){
                        return false;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        //dorobic jakas funkcjonalnosc, jakby sie cos pomylilo w komunikacji
        return false;
    }


    /*
    Metoda odpowiedzialna za pobranie absolutnych ścieżek plików, znajdujących się na serwerze.
    Metoda zwraca listę obiektów typu File (analogiczna lista jest w klasie User).
     */
    public ArrayList<File> getBackupFilesListFromServer(){
        ArrayList<File> backupFiles= new ArrayList<>();

        try {
            out.println(ClientMessage.GET_BACKUP_FILES_LIST.name());
            if (in.readLine().equals(ServerMessage.SENDING_BACKUP_FILES_LIST.name())) {
                String nextMessageLine = in.readLine();
                while (nextMessageLine.equals(ServerMessage.SENDING_BACKUP_FILES_LIST_FINISHED.name()))
                {
                    backupFiles.add(new File(nextMessageLine));
                    nextMessageLine = in.readLine();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return backupFiles;
        }

        //dorobic jakas funkcjonalnosc, jakby sie cos pomylilo w komunikacji
        return backupFiles;
    }


    /*
    Metoda odpowiedzialna za pobranie wszystkich wersji danego pliku.
    Jako wersję pliku traktować będziemy datę modyfikacji zapisaną w Stringu.
     */
    public ArrayList<String> getAllFileVersionsFromServer(File file){
        ArrayList<String> versionsList= new ArrayList<>();

        try {
            out.println(ClientMessage.GET_ALL_FILE_VERSIONS.name());
            if (in.readLine().equals(ServerMessage.GET_FILE_PATH.name())) {
                out.println(file.getAbsolutePath());
                if (in.readLine().equals(ServerMessage.SENDING_FILE_VERSIONS.name())) {
                    String nextMessageLine = in.readLine();
                    while (nextMessageLine.equals(ServerMessage.SENDING_FILE_VERSIIONS_FINISHED.name())) {
                        versionsList.add(nextMessageLine);
                        nextMessageLine = in.readLine();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return versionsList;
        }

        //dorobic jakas funkcjonalnosc, jakby sie cos pomylilo w komunikacji
        return versionsList;
    }


    /*
    Metoda odpowiedzialna za zdalne usunięcie wszystkich wersji pliku znajdujących się na serwerze.
    Zwraca status operacji - true, jeżeli operacja powiodła się.
    */
    public boolean removeSelectedFile(File file){
        try {
            out.println(ClientMessage.REMOVE_FILE.name());
            if (in.readLine().equals(ServerMessage.GET_FILE_PATH.name())) {
                out.println(file.getAbsolutePath());
                if (in.readLine().equals(ServerMessage.FILE_REMOVED.name())) {
                    return true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        //dorobic jakas funkcjonalnosc, jakby sie cos pomylilo w komunikacji
        return false;
    }


    /*
    Metoda odpowiedzialna za usunięcie konkretnej wersji pliku znajdującego się na serwerze.
    Wersja oznaczona jest jako string zawierający datę modyfikacji.
     */
    public boolean removeSelectedFileVersion(String filePath, String fileVersion){
        try {
            out.println(ClientMessage.REMOVE_FILE_VERSION.name());
            if (in.readLine().equals(ServerMessage.GET_FILE_PATH.name())) {
                out.println(filePath);
                if (in.readLine().equals(ServerMessage.GET_FILE_VERSION.name())) {
                    out.println(fileVersion);
                    if(in.readLine().equals(ServerMessage.FILE_VERSION_REMOVED.name())){
                        return true;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        //dorobic jakas funkcjonalnosc, jakby sie cos pomylilo w komunikacji
        return false;
    }

    /*
    Metoda odpowiedzialna za usunięcie profilu użytkownika po stronie serwera.
    (usuwa możliwość logowania oraz wszelkie zapisane pliki z nim związane)
     */
    public boolean deleteUser(String username) {
        try {
            out.println(ClientMessage.DELETE_USER.name());
            if (in.readLine().equals(ServerMessage.GET_USERNAME.name())) {
                out.println(username);
                if (in.readLine().equals(ServerMessage.DELETE_USER_FINISHED.name())) {
                    return true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        //dorobic jakas funkcjonalnosc, jakby sie cos pomylilo w komunikacji
        return false;
    }

    /*
    Metoda odpowiedzialna za wylogowanie użytkownika - informuje
     */
    public boolean logoutUser(String username) {
        try {
            out.println(ClientMessage.LOG_OUT.name());
            if (in.readLine().equals(ServerMessage.LOG_OUT_FINISHED.name())) {
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }


    /*
    Metoda zamykajaca połaczenie - przed zamknięciem wysyłamy wiadomosć EXIT
     */
    public void closeConnection() {
        out.println(ClientMessage.EXIT.name());
        try {
            out.close();
            in.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Metoda tworzy listę jednoelementową plików oraz uruchamia metodę backupAllFiles
     * @param fileToArchive
     * @param appController
     */
    public void backupOnlySelectedFile(File fileToArchive, AppController appController) {
        ArrayList<File> filesToArchive = new ArrayList<>();
        filesToArchive.add(fileToArchive);

        this.backupAllFiles(filesToArchive, appController);
    }

    /**
     * Metoda odpowiedzialna za uruchomienie nowego wątku związanego z obsługą procesu archiwizacji danych.
     * @param filesToArchive
     * @param appController
     */
    public void backupAllFiles(List<File> filesToArchive, AppController appController) {
        BackupWorker backupWorker = new BackupWorker(
                socket, in, out,
                filesToArchive,
                appController );
        Thread backupThread = new Thread(backupWorker);
        backupThread.start();
    }

    /**
     * Metoda odpowiedzialna za uruchomienie nowego wątku związanego z obsługą procesu związanego z pobraniem
     * wybranego archiwum z serwera.
     * @param filePath
     * @param fileVersion
     */
    public void restoreSelectedFileVersion(
            String filePath, String fileVersion, File backupFile, AppController appController){

        RestoreWorker restoreWorker = new RestoreWorker(
                socket, in, out,
                backupFile, filePath, fileVersion,
                appController );
        Thread restoreThread = new Thread(restoreWorker);
        restoreThread.start();
    }
}
