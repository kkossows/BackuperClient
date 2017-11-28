package main.networking.tasks;

import javafx.concurrent.Task;
import main.networking.ClientMessage;
import main.networking.ServerMessage;

import java.io.*;
import java.net.Socket;

/**
 * Class used to handle restoring file stuff.
 * Created by kkossowski on 19.11.2017.
 */
public class RestoreTask extends Task<Boolean> {
    private String serverIpAddress;
    private int serverPortNumber;
    private int authenticateCode;
    private File emptyFileOnLocalSystem;
    private String filePath;
    private String fileVersion;

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;


    public RestoreTask(
            String serverIpAddress, int serverPortNumber, int authenticateCode,
            File emptyFileOnLocalSystem, String filePath, String fileVersion) {

        this.serverIpAddress = serverIpAddress;
        this.serverPortNumber = serverPortNumber;
        this.authenticateCode = authenticateCode;
        this.emptyFileOnLocalSystem = emptyFileOnLocalSystem;
        this.filePath = filePath;
        this.fileVersion = fileVersion;
    }

    @Override
    protected Boolean call() throws Exception {
        //initialize communication
        socket = new Socket(serverIpAddress, serverPortNumber);

        //if no exception was created, create input and output streams
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);

        //authenticate stream with authenticationCode
        boolean authenticationStatus = false;
        out.println(ClientMessage.INIT_WITH_CODE.name());
        if (in.readLine().equals(ServerMessage.GET_CODE.name())) {
            out.println(authenticateCode);
            if (in.readLine().equals(ServerMessage.INIT_CORRECT.name())) {
                authenticationStatus = true;
            } else {
                //wrong authentication code
            }
        }

        if (authenticationStatus) {
            //start backup procedure
            return runRestoreProcedure();

        } else {
            //close connection
            closeConnection();
            return false;
        }
    }

    private boolean runRestoreProcedure() throws Exception {
        try {
            //update label
            this.updateMessage(filePath);

            boolean isSendingFile = false;
            long fileSize = 0;

            out.println(ClientMessage.RESTORE_FILE.name());
            if (in.readLine().equals(ServerMessage.GET_FILE_PATH.name())) {
                out.println(filePath);
                if (in.readLine().equals(ServerMessage.GET_FILE_VERSION.name())) {
                    out.println(fileVersion);
                    if (in.readLine().equals(ServerMessage.SENDING_FILE_SIZE.name())) {
                        fileSize = Long.parseLong(in.readLine());
                        if (in.readLine().equals(ServerMessage.SENDING_FILE.name())) {
                            isSendingFile = true;
                        }
                    }
                }
            }

            if (isSendingFile) {
                DataInputStream inputStream = new DataInputStream(socket.getInputStream());

                try (FileOutputStream outputStream = new FileOutputStream(emptyFileOnLocalSystem)) {
                    byte[] buffer = new byte[main.config.Properties.bufferSize];
                    int numberOfReadBytes = 0;
                    long bytesToRead = fileSize;
                    long bytesRead = 0;

                    while (bytesToRead > 0) {
                        if ((numberOfReadBytes = inputStream.read(buffer, 0, (int) Math.min(bytesToRead, buffer.length))) > 0) {

                            outputStream.write(buffer, 0, numberOfReadBytes);
                            bytesToRead -= numberOfReadBytes;
                            bytesRead += numberOfReadBytes;

                            //set statistics
                            updateProgress(bytesRead, fileSize);
                        }
                    }
                }//close file stream

                //verify whether server finished
                if (in.readLine().equals(ServerMessage.SENDING_FILE_FINISHED.name())) {
                    //restored file not show in files to archive list
                    //user only see informationDialog that restoring finished
                    return true;
                }
            }
            return false;
        } catch (Exception e){
            //server disconnected
            //delete new file
            emptyFileOnLocalSystem.delete();

            //rethrow the exception
            throw e;
        }
    }

    private void closeConnection(){
        try {
            out.close();
            in.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public File getEmptyFile(){
        return emptyFileOnLocalSystem;
    }
}
