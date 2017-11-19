package main.networking;

import main.view.AppController;

import java.io.*;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Properties;

import static oracle.jrockit.jfr.events.Bits.intValue;

/**
 * Class to handle backup files stuff.
 * Created by rkossowski on 19.11.2017.
 */
public class BackupWorker implements Runnable {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private List<File> filesToArchive;
    private AppController appController;


    public BackupWorker(
            Socket socket, BufferedReader inputStream, PrintWriter outputStream,
            List<File> filesToArchive, AppController appController) {

        this.socket = socket;
        this.in = inputStream;
        this.out = outputStream;
        this.filesToArchive = filesToArchive;
        this.appController = appController;
    }

    @Override
    public void run() {
        //statistics variables
        int stats_filesToArchive = filesToArchive.size();
        int stats_sendingFileNumber = 0;


        for ( File file : filesToArchive) {
            //update statistics variables
            stats_sendingFileNumber += 1;
            appController.setStatisticLabelWithNumber(stats_sendingFileNumber, stats_filesToArchive);

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
                            if (in.readLine().equals(ServerMessage.GET_FILE_CONTENT.name())) {
                                sendFile = true;
                            }
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }


            //send file
            if (sendFile) {
                try {
                    DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
                    FileInputStream inputStream = new FileInputStream(file);

                    byte[] buffer = new byte[main.config.Properties.bufferSize];
                    int numberOfReadBytes = 0;
                    long bytesToSend = fileSize;
                    long bytesSent = 0;

                    //-1 if there is no more data because the end of the file has been reached
                    while (bytesToSend > 0
                            && (numberOfReadBytes = inputStream.read(buffer, 0, (int) Math.min(buffer.length, bytesToSend))) != -1){

                        outputStream.write(buffer);
                        bytesToSend -= numberOfReadBytes;
                        bytesSent += numberOfReadBytes;
                        appController.setStatisticLabelWithPercent((int)(bytesSent/fileSize));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            //update server list
            appController.addFileOnServerList(file);

            //inform server side, that the transmisiion is finished
            out.println(ClientMessage.BACKUP_FILE_FINISHED.name());
        }
    }

    /**
     * Metoda pomocnicza - zwraca stringa utworzonego na podstawie daty modyfikacji pliku
     * @param file
     * @return
     */
    private String getVersionOfFile(File file){
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        return sdf.format(file.lastModified());
    }

}
