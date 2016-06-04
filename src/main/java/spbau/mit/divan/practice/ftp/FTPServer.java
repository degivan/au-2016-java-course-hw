package spbau.mit.divan.practice.ftp;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

/**
 * Created by Degtjarenko Ivan on 02.06.2016.
 */
public class FTPServer {
    public static final int SERVER_DEFAULT_PORT = 8080;
    public static final int LIST = 1;
    public static final int GET = 2;
    private static final int NOT_EXISTS = 0;
    private ServerSocket serverSocket;
    private int port;
    private Thread workThread;

    public FTPServer(int portNumber) {
        port = portNumber;
    }

    public void startUp() throws IOException {
        serverSocket = new ServerSocket(port);
        workThread = new Thread(() -> {
            while (!Thread.interrupted()) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    processClient(clientSocket);
                } catch (IOException e) {
                    Logger.getAnonymousLogger().info("Connection closed");
                }
            }
        });
        workThread.start();
    }

    private static void list(DataOutputStream output, String path) throws IOException {
        Path filePath = Paths.get(path);
        if (Files.notExists(filePath)) {
            output.writeInt(NOT_EXISTS);
            output.flush();
            return;
        }

        File dir = new File(path);
        File[] files = dir.listFiles();

        if (files != null) {
            output.writeInt(files.length);
            for (File file : files) {
                output.writeUTF(file.getPath());
                output.writeBoolean(file.isDirectory());
            }
            output.flush();
        }
    }

    private static void get(DataOutputStream output, String path) throws IOException {
        Path filePath = Paths.get(path);
        if (Files.notExists(filePath)) {
            output.writeInt(NOT_EXISTS);
            output.flush();
            return;
        }

        int a = (int) Files.size(filePath);
        output.writeInt(a);
        output.write(Files.readAllBytes(filePath));
        output.flush();
    }

    private void processClient(Socket clientSocket) throws IOException {
        try (
                DataInputStream input = new DataInputStream(clientSocket.getInputStream());
                DataOutputStream output = new DataOutputStream(clientSocket.getOutputStream())
        ) {
            int requestType = input.readInt();
            String path = input.readUTF();

            if (requestType == LIST) {
                list(output, path);
            } else if (requestType == GET) {
                get(output, path);
            }
        } catch (IOException e) {
            Logger.getAnonymousLogger().info(e.getMessage());
            clientSocket.close();
        }
    }

    public synchronized void tearDown() throws InterruptedException, IOException {
        workThread.interrupt();
        serverSocket.close();
        workThread.join();
    }
}
