package main.networking;

import javax.swing.*;
import java.io.*;
import java.lang.reflect.Array;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

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
            if (in.readLine().equals(ServerMessage.SEND_USERNAME.name())) {
                out.println(username);
                if (in.readLine().equals(ServerMessage.SEND_PASSWORD.name())) {
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
            if (in.readLine().equals(ServerMessage.SEND_USERNAME.name())) {
                out.println(username);
                if (in.readLine().equals(ServerMessage.SEND_PASSWORD.name())) {
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
}
