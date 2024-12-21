package Controller;

import Model.CacheManager;
import Model.DatabaseManager;
import Model.FileManager;
import Model.NotificationManager;
import View.FileServerView;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.sql.SQLException;

public class SyncController {
    private FileServerView view;

    private int port = 8490;
    private int portNotification = 8491;

    public SyncController(FileServerView view) throws SQLException {
        this.view = view;
        new DatabaseManager();
        new FileManager();
        new CacheManager();
        new NotificationManager();
    }

    public void startServer() {
        try (ServerSocket serverSocket = new ServerSocket(port);
        		ServerSocket serverSocketNotification = new ServerSocket(portNotification)) {
            view.log("Server listening on port " + port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                Socket clientSocketNotification = serverSocketNotification.accept();
                view.log("Client connected: " + clientSocket.getInetAddress());
                new ClientHandler(clientSocket, clientSocketNotification).start();              
            }
        } catch (Exception e) {
            e.printStackTrace();
            view.log("Error starting server: " + e.getMessage());
        }
    }
    public void updateIpAddress() {
        try {
            InetAddress inetAddress = InetAddress.getLocalHost();
            String ip = inetAddress.getHostAddress();
            view.setIpAddress(ip);
            view.setPort(String.valueOf(port));
        } catch (UnknownHostException e) {
            e.printStackTrace();
            view.setIpAddress("Không thể lấy IP");
        }
    }
}
