package main.networking;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
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
            out.println(ClientMessage.LOG_IN);
            if (in.readLine() == ServerMessage.SEND_USERNAME.name()) {
                out.println(username);
                if (in.readLine() == ServerMessage.SEND_PASSWORD.name()) {
                    out.println(password);
                    if (in.readLine() == ServerMessage.LOGIN_SUCCESS.name()) {
                        return true;
                    } else if (in.readLine() == ServerMessage.LOGIN_FAILED.name()) {
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
            out.println(ClientMessage.REGISTER);
            if (in.readLine() == ServerMessage.SEND_USERNAME.name()) {
                out.println(username);
                if (in.readLine() == ServerMessage.SEND_PASSWORD.name()) {
                    out.println(password);
                    if (in.readLine() == ServerMessage.USER_CREATED.name()) {
                        return true;
                    }
                    else if(in.readLine() == ServerMessage.USER_EXISTS.name()){
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
}
