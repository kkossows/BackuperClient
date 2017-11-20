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
    private File emptyFileOnLocalSystem;
    private String filePath;
    private String fileVersion;
    private AppController appController;


    public RestoreWorker(
            Socket socket, BufferedReader inputStream, PrintWriter outputStream,
            File emptyFileOnLocalSystem, String filePath, String fileVersion,
            AppController appController) {

        this.socket = socket;
        this.in = inputStream;
        this.out = outputStream;
        this.emptyFileOnLocalSystem = emptyFileOnLocalSystem;
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

        if (isSendingFile) {
            try {
                DataInputStream inputStream = new DataInputStream(socket.getInputStream());
                FileOutputStream outputStream = new FileOutputStream(emptyFileOnLocalSystem);

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
                    //restored file not show in files to archive list
                    //user only see informationDialog that restoring finised
                    appController.showInformationDialog(
                            "Restoring file finished.",
                            "File saved in " + emptyFileOnLocalSystem.getAbsolutePath()
                    );
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
