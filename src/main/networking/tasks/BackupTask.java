package main.networking.tasks;

import javafx.application.Platform;
import javafx.concurrent.Task;
import main.networking.ClientMessage;
import main.networking.ServerMessage;
import main.view.AppController;

import java.io.*;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Class to handle backup files stuff.
 * Created by kkossowski on 19.11.2017.
 */
public class BackupTask extends Task<Boolean> {
    private String serverIpAddress;
    private int serverPortNumber;
    private int authenticateCode;
    private List<File> filesToArchive;
    private AppController appController;

    private BufferedReader in;
    private PrintWriter out;
    private Socket socket;

    public BackupTask(
            String serverIpAddress, int serverPortNumber, int authenticateCode,
            List<File> filesToArchive, AppController appController) {

        this.serverIpAddress = serverIpAddress;
        this.serverPortNumber = serverPortNumber;
        this.authenticateCode = authenticateCode;
        this.filesToArchive = filesToArchive;
        this.appController = appController;
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
        if (in.readLine().equals(ServerMessage.GET_CODE.name())){
            out.println(authenticateCode);
            if (in.readLine().equals(ServerMessage.INIT_CORRECT.name())) {
                authenticationStatus = true;
            }
            else {
                //wrong authentication code
            }
        }

        if (authenticationStatus)
        {
            //start backup procedure
            return runBackupProcedure();

        } else {
          //close connection
            closeConnection();
            return false;
        }
    }

    private boolean runBackupProcedure() throws IOException {
        //statistics variables
        int stats_filesToArchive = filesToArchive.size();
        int stats_sendingFileNumber = 0;

        //open proper output stream
        DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
        //if we close outputStream, socket will be closed to

        for ( File file : filesToArchive) {
            //update statistics variables
            stats_sendingFileNumber += 1;
            this.updateMessage(
                    "[" + stats_sendingFileNumber + "/" +  stats_filesToArchive + "]"
                            + " ->" + file.getAbsolutePath()
            );

            //check if file exist on server
            boolean sendFile = false;
            long fileSize = file.length();

            try {
                out.println(ClientMessage.BACKUP_FILE.name());
                if (in.readLine().equals(ServerMessage.GET_FILE_PATH.name())) {
                    out.println(file.getAbsolutePath());
                    if (in.readLine().equals(ServerMessage.GET_FILE_VERSION.name())) {
                        out.println(getVersionOfFile(file));
                        if (in.readLine().equals(ServerMessage.GET_FILE_SIZE.name())) {
                            out.println(fileSize);
                            String message = in.readLine();
                            if (message.equals(ServerMessage.GET_FILE_CONTENT.name())) {
                                sendFile = true;
                            }
                            else if (message.equals(ServerMessage.FILE_VERSION_EXISTS.name())){
                                sendFile = false;
                            }
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }


            if (sendFile) {
                //send file
                try {
                    //open new input stream
                    try (FileInputStream inputStream = new FileInputStream(file)) {

                        byte[] buffer = new byte[main.config.Properties.bufferSize];
                        int numberOfReadBytes = 0;
                        long bytesToSend = fileSize;
                        long bytesSent = 0;

                        //-1 if there is no more data because the end of the file has been reached
                        while (bytesToSend > 0
                                && (numberOfReadBytes = inputStream.read(buffer, 0, (int) Math.min(buffer.length, bytesToSend))) != -1) {

                            outputStream.write(buffer,0,numberOfReadBytes);
                            bytesToSend -= numberOfReadBytes;
                            bytesSent += numberOfReadBytes;
                            //update statistics
                            this.updateProgress(bytesSent, fileSize);
                        }
                    }//close streams
                } catch (IOException e) {
                    e.printStackTrace();
                }

                //update server list in Javafx thread
                Platform.runLater(()-> {
                            appController.addFileToFilesOnServer(file);
                        }
                );

                //inform server side, that the transmission is finished
                out.println(ClientMessage.BACKUP_FILE_FINISHED.name());
            } else {
                //file exist on server
                Platform.runLater(() -> {
                    appController.showInformationDialog(
                            "Same file versions:" + file.getAbsolutePath(),
                            "Selected version file already exists on local system file."
                    );
                });
                updateProgress(fileSize,fileSize);
            }
        }
        closeConnection();
        return true;
    }

    /**
     * Method responsible for getting proper format of file last modify.
     * @param file
     * @return string format MM-dd-yyyy_HH-mm-ss
     */
    private String getVersionOfFile(File file){
        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy_HH-mm-ss");
        return sdf.format(file.lastModified());
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
}
