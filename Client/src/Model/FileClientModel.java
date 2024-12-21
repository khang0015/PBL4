package Model;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

import Controller.ClientNotificationThread;

public class FileClientModel {

    private static String SERVER_ADDRESS;// = "localhost";
    private static int SERVER_PORT;// = 8490;
    private static int SERVER_PORT_NOTIF = 8491;
    private Socket socket;
    private Socket socketClientNotif;
    private DataOutputStream output;
    private DataInputStream input;
    private ObjectInputStream objectInput;
    private int yourUserID;
    private ClientNotificationThread clientNotifThread;
    public FileClientModel() throws IOException {
    	
    }
    public void initSocket()
    {
    	try {
            System.out.println("Bắt đầu hàm dựng FileClientModel");
            this.socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            
            this.socketClientNotif = new Socket(SERVER_ADDRESS, SERVER_PORT_NOTIF);
            
            System.out.println("Tạo xong socket");
            this.output = new DataOutputStream(socket.getOutputStream());
            System.out.println("Tao xong dataoutputstream");
            this.input = new DataInputStream(socket.getInputStream());
            System.out.println("Tao xong dis");
            this.objectInput = new ObjectInputStream(socket.getInputStream());
            System.out.println("Kết thúc hàm dựng FileClientModel");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Lỗi trong hàm dựng FileClientModel");
        }
    }
    public void initThreadNotif()
    {
    	clientNotifThread = new ClientNotificationThread(socketClientNotif, yourUserID);
    	clientNotifThread.runningThread();
    	clientNotifThread.start();
    }
    public void setServerAddress(String serverAddress) {
        FileClientModel.SERVER_ADDRESS = serverAddress;
    }
    public void setServerPort(int serverPort) {
        FileClientModel.SERVER_PORT = serverPort;
    }
    
    public DefaultMutableTreeNode getFileTree() throws IOException, ClassNotFoundException {
        return (DefaultMutableTreeNode) objectInput.readObject();
    }
    
    public boolean authenticate(String username, String password) throws IOException {
        output.writeUTF(username);
        output.writeUTF(password);
        String response = input.readUTF();
        System.out.println("Da nhan response: " + response);
        
        if("Success".equals(response))
        {
        	int yourUserID = input.readInt();
        	setYourUserID(yourUserID);
        	System.out.println("Your UserID: " + yourUserID);
        	return true;
        }
        else
        	return false;
    }
    private void setYourUserID(int id)
    {
    	yourUserID = id;
    }
    public int getYourUserID()
    {
    	return yourUserID;
    }
    public void downloadFile(String remotePath, File localPath) throws IOException {
        	long fileSize = input.readLong();
	        if (fileSize == -1) throw new FileNotFoundException("File not found on server.");
	
	        try (FileOutputStream fileOutput = new FileOutputStream(localPath)) {
	            byte[] buffer = new byte[4096];
	            int bytesRead;
	            long totalBytesRead = 0;
	
	            while (totalBytesRead < fileSize && (bytesRead = input.read(buffer)) != -1) {
	                fileOutput.write(buffer, 0, bytesRead);
	                totalBytesRead += bytesRead;
	            }
	        }      
    }
    public String headerDownloadFile(String remotePath) throws IOException {
    	output.writeUTF("DOWNLOAD");
        output.writeUTF(remotePath);
        return input.readUTF();
    }
    
    public void downloadConfirmation(boolean check) throws IOException {
    	if(check) {
    		output.writeUTF("OK");
    	}else {
    		output.writeUTF("cancel");
    	}
    }
    
    
    
    public void uploadFile(File file, String remoteFolder) throws IOException {
        output.writeUTF("UPLOAD");
        output.writeUTF(remoteFolder);
        output.writeUTF(file.getName());
        output.writeLong(file.length());

        try (FileInputStream fileInput = new FileInputStream(file)) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = fileInput.read(buffer)) != -1) {
                output.write(buffer, 0, bytesRead);
            }
        }
    }
    public void createFolder(String parentPath, String folderName) throws IOException {
    	output.writeUTF("CREATEFOLDER");
        output.writeUTF(parentPath);
        output.writeUTF(folderName);
        
    }
    public void createProject(String _projName) throws IOException
    {
    	output.writeUTF("CREATEPROJECT");
    	output.writeUTF(_projName);
    }
    public void reloadTree() throws IOException
    {
    	output.writeUTF("RELOAD");
    }
    public void delete(String remotePath) throws IOException
    {
    	output.writeUTF("DELETE");
    	output.writeUTF(remotePath);
    }
    public void disconnect() throws IOException
    {
    	output.writeUTF("DISCONNECT");
    }
    
    
    
    
    public void sendShareMessage(String repoName) throws IOException {
    	output.writeUTF("SHARE");
    	output.writeUTF(repoName);   	
    }
    

      
    public List<String> getUsersFromServer() throws IOException {
        int userCount = input.readInt(); 
        List<String> Users = new ArrayList<>();
        for (int i = 0; i < userCount; i++) {
            String userName = input.readUTF(); 
            Users.add(userName);
        }
        return Users;
    }
 
    public void sendListToServer(List<String> list) throws IOException {
    	int count = list.size();
    	output.writeInt(count);
    	for(int i=0; i < count; i++) {
    		output.writeUTF(list.get(i));
    	}
    }
    
    public boolean shareCheckOwner() throws IOException {
    	String string = input.readUTF();
    	System.out.println(string);
    	return string.equals("Success");
    }
  
    public void shareRepoMessage() throws IOException {
        output.writeUTF("SHARE_REPO");   
    }

    public void unshareRepoMessage() throws IOException {
        output.writeUTF("UNSHARE_REPO");
    }
    
    public void sendCanelMessage() throws IOException {
    	output.writeUTF("CANCEL");
    }
    public void syncData() throws IOException
    {
    	output.writeUTF("SYNC");
    	clientNotifThread.setWarningSync("");
    }
    public String getWarning() {
    	return clientNotifThread.getWarning();
    }
    
}