package main.networking;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
/**
 * Created by kkossowski on 18.11.2017.
 */
public class ServerHandler {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private int serverListeningPortNumber; //socket has port number associated with connection, not listening port
    private int authenticateCode;


    /**
     * Constructor
     */
    public ServerHandler() {
        this.socket = null;
        this.in = null;
        this.out = null;
        this.serverListeningPortNumber = -1;
        authenticateCode = -1;
    }

    /**
     * Static method responsible for making connection with server.
     * If connection is active, server is online.
     * If connection is inactive, server is offline.
     * @param serverIpAddress
     * @param serverPortNumber
     * @return Status of connection (true mean connection established)
     */
    public boolean isServerOnline(String serverIpAddress, int serverPortNumber) throws IOException {
        //if socket will not be created, it throws IOException
        socket = new Socket(serverIpAddress, serverPortNumber);

        //create streams
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);

        //send init message
        out.println(ClientMessage.INIT.name());
        if (in.readLine().equals(ServerMessage.INIT_CORRECT.name())) {
            //save serverListeningPortNumber
            serverListeningPortNumber = serverPortNumber;
            return true;
        }
        else {
            closeConnection();
            return false;
        }
    }

    /**
     * Method responsible for the authentication process.
     * @param username
     * @param password
     * @return  Operations result (true - user exist on server and password is correct).
     */
    public boolean authenticateUser(String username, String password) throws IOException {
        out.println(ClientMessage.LOG_IN.name());
        if (in.readLine().equals(ServerMessage.GET_USERNAME.name())) {
            out.println(username);
            if (in.readLine().equals(ServerMessage.GET_PASSWORD.name())) {
                out.println(password);
                String message = in.readLine();
                if (message.equals(ServerMessage.LOGIN_SUCCESS.name())) {
                    //receive authentication code which will be used in backup/restore process
                    authenticateCode = Integer.parseInt(in.readLine());
                    return true;
                } else if (message.equals(ServerMessage.LOGIN_FAILED.name())) {
                    return false;
                }
            }
        }
        return false;
    }

    /**
     * Method responsible for the registration process.
     * @param username
     * @param password
     * @return Operations result (true - user registered, false - user already exists on the server).
     */
    public boolean registerUser(String username, String password) throws IOException {
        out.println(ClientMessage.REGISTER.name());
        if (in.readLine().equals(ServerMessage.GET_USERNAME.name())) {
            out.println(username);
            if (in.readLine().equals(ServerMessage.GET_PASSWORD.name())) {
                out.println(password);
                String message = in.readLine();
                if (message.equals(ServerMessage.USER_CREATED.name())) {
                    return true;
                } else if (message.equals(ServerMessage.USER_EXISTS.name())) {
                    return false;
                }
            }
        }
        return false;
    }

    /**
     * Method responsible for getting backup files list from server
     * (server send absolute path of files)
     * @return backup file list received from server
     */
    public ArrayList<File> getBackupFilesListFromServer(){
        ArrayList<File> backupFiles= new ArrayList<>();

        try {
            out.println(ClientMessage.GET_BACKUP_FILES_LIST.name());
            if (in.readLine().equals(ServerMessage.SENDING_BACKUP_FILES_LIST.name())) {
                String nextMessageLine = in.readLine();
                while (!nextMessageLine.equals(ServerMessage.SENDING_BACKUP_FILES_LIST_FINISHED.name()))
                {
                    backupFiles.add(new File(nextMessageLine));
                    nextMessageLine = in.readLine();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return backupFiles;
        }
        return backupFiles;
    }

    /**
     * Method responsible for getting all file versions stored on server side.
     * @param file
     * @return list of strings, each file version is represented by string (last modification date in proper format)
     */
    public ArrayList<String> getAllFileVersionsFromServer(File file) throws IOException {
        ArrayList<String> versionsList = new ArrayList<>();
        out.println(ClientMessage.GET_ALL_FILE_VERSIONS.name());
        if (in.readLine().equals(ServerMessage.GET_FILE_PATH.name())) {
            out.println(file.getAbsolutePath());
            if (in.readLine().equals(ServerMessage.SENDING_FILE_VERSIONS.name())) {
                String nextMessageLine = in.readLine();
                while (!nextMessageLine.equals(ServerMessage.SENDING_FILE_VERSIONS_FINISHED.name())) {
                    versionsList.add(nextMessageLine);
                    nextMessageLine = in.readLine();
                }
            }
        }

        return versionsList;
    }

    /**
     * Method responsible for removing file with all file versions on server side.
     * @param file
     * @return operation result (true - success)
     */
    public boolean removeSelectedFile(File file) throws IOException {
        out.println(ClientMessage.REMOVE_FILE.name());
        if (in.readLine().equals(ServerMessage.GET_FILE_PATH.name())) {
            out.println(file.getAbsolutePath());
            if (in.readLine().equals(ServerMessage.FILE_REMOVED.name())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Method responsible for removing one file version from server side.
     * @param filePath
     * @param fileVersion
     * @return operation result (true - success)
     */
    public boolean removeSelectedFileVersion(String filePath, String fileVersion) throws IOException {
        out.println(ClientMessage.REMOVE_FILE_VERSION.name());
        if (in.readLine().equals(ServerMessage.GET_FILE_PATH.name())) {
            out.println(filePath);
            if (in.readLine().equals(ServerMessage.GET_FILE_VERSION.name())) {
                out.println(fileVersion);
                if (in.readLine().equals(ServerMessage.FILE_VERSION_REMOVED.name())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Method responsible for deleting user on server side.
     * @return operation result
     */
    public boolean deleteUser() throws IOException {
        //rest of functionality handled byAppController
        out.println(ClientMessage.DELETE_USER.name());
        if (in.readLine().equals(ServerMessage.DELETE_USER_FINISHED.name())) {
            return true;
        }
        return false;
    }

    /**
     * Method responsible for the logout procedure.
     * @return operation result
     */
    public boolean logoutUser() {
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

    /**
     * Method responsible for the close connection process.
     * - close input and output streams
     * - close socket associated with active connection
     */
    public void closeConnection() {
        try {
            out.close();
            in.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Socket getSocket() {
        return socket;
    }

    public BufferedReader getIn() {
        return in;
    }

    public PrintWriter getOut() {
        return out;
    }

    public int getAuthenticateCode(){
        return authenticateCode;
    }

    public int getServerListeningPortNumber(){
        return serverListeningPortNumber;
    }
}
