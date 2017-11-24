package main.networking;

import javafx.collections.ObservableList;
import javafx.concurrent.Task;

import java.io.*;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Class to handle backup files stuff.
 * Created by kkossowski on 19.11.2017.
 */
public class BackupTask extends Task<Boolean> {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private List<File> filesToArchive;
    private ObservableList<File> filesOnServer;


    public BackupTask(
            Socket socket, BufferedReader inputStream, PrintWriter outputStream,
            List<File> filesToArchive, ObservableList<File> filesOnServer) {

        this.socket = socket;
        this.in = inputStream;
        this.out = outputStream;
        this.filesToArchive = filesToArchive;
        this.filesOnServer = filesOnServer;
    }

    @Override
    protected Boolean call() throws Exception {
        //statistics variables
        int stats_filesToArchive = filesToArchive.size();
        int stats_sendingFileNumber = 0;


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


            //send file
            try {
                try (DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
                     FileInputStream inputStream = new FileInputStream(file)) {

                    byte[] buffer = new byte[main.config.Properties.bufferSize];
                    int numberOfReadBytes = 0;
                    long bytesToSend = fileSize;
                    long bytesSent = 0;

                    //-1 if there is no more data because the end of the file has been reached
                    while (bytesToSend > 0
                            && (numberOfReadBytes = inputStream.read(buffer, 0, (int) Math.min(buffer.length, bytesToSend))) != -1) {

                        outputStream.write(buffer);
                        bytesToSend -= numberOfReadBytes;
                        bytesSent += numberOfReadBytes;
                        //update statistics
                        updateProgress(bytesSent, fileSize);
                    }
                }//close streams
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (sendFile) {
                //update server list
                filesOnServer.add(file);

                //inform server side, that the transmission is finished
                out.println(ClientMessage.BACKUP_FILE_FINISHED.name());
            }
        }
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

}
