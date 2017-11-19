package main.networking;

import main.view.AppController;

import java.io.*;
import java.net.Socket;

/**
 * Class used to handle restoring file stuff.
 * Created by rkossowski on 19.11.2017.
 */
public class RestoreWorker implements Runnable {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private String filePath;
    private String fileVersion;
    private AppController appController;


    public RestoreWorker(
            Socket socket, BufferedReader inputStream, PrintWriter outputStream,
            String filePath, String fileVersion, AppController appController) {

        this.socket = socket;
        this.in = inputStream;
        this.out = outputStream;
        this.filePath = filePath;
        this.fileVersion = fileVersion;
        this.appController = appController;
    }


    @Override
    public void run() {
        boolean isSendingFile = false;
        long fileSize = 0;

        try {
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
        } catch (IOException e) {
            e.printStackTrace();
        }

        //create file in local file system (and sub-directories if necessary)
        File backupFile = new File(filePath);
        backupFile.getParentFile().mkdirs();
        try {
            backupFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }


        if (isSendingFile) {
            boolean isFileReceivedCorrectly = false;

            try {
                DataInputStream inputStream = new DataInputStream(socket.getInputStream());
                FileOutputStream outputStream = new FileOutputStream(backupFile);

                byte[] buffer = new byte[main.config.Properties.bufferSize];
                int numberOfReadBytes = 0;
                long bytesToRead = fileSize;
                long bytesRead = 0;

                while (bytesToRead > 0) {
                    if ((numberOfReadBytes = inputStream.read(buffer, 0, (int) Math.min(bytesToRead, buffer.length))) > 0) {

                        outputStream.write(buffer);
                        bytesToRead -= numberOfReadBytes;
                        bytesRead += numberOfReadBytes;
                        appController.setStatisticLabelWithPercentRestoring((int) (bytesRead / fileSize));
                    }
                }

                //verify whether server finished
                if (in.readLine().equals(ServerMessage.SENDING_FILE_FINISHED.name())) {
                    isFileReceivedCorrectly = true;
                }

                if (isFileReceivedCorrectly){
                    //update lists
                    appController.addFileToArchiveList(backupFile);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
