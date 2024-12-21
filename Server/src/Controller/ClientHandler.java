package Controller;

import Model.CacheManager;
import Model.DatabaseManager;
import Model.NotificationManager;
import Model.Tree.RepositoryEntity;
import javax.swing.tree.DefaultMutableTreeNode;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ClientHandler extends Thread {
    private Socket socket;
    private Socket serverSocketNotification;
    private DataInputStream dis;
    private DataOutputStream dos;
    private ObjectOutputStream objectOutput;
    private Connection connection;
    private ServerNotification serverNotif;
    private int USERID;

    public ClientHandler(Socket socket, Socket serverSocketNotification) throws SQLException {
        this.socket = socket;
        this.serverSocketNotification = serverSocketNotification;
        this.connection = DatabaseManager.getInstance().getConnection(); //this.dbManager.getConnection();
        try {
        	this.objectOutput = new ObjectOutputStream(socket.getOutputStream());
            this.dis = new DataInputStream(socket.getInputStream());
            this.dos = new DataOutputStream(socket.getOutputStream());
            
            
        } catch (Exception e) {
            System.err.println("Lỗi khi tạo luồng dữ liệu cho client: " + e.getMessage());
            try {
                socket.close();
            } catch (IOException ex) {
                ex.printStackTrace();
                System.out.println("193");
            }
        }

    }

    @Override
    public void run() {
    	try {
    		boolean running = true;
    		while(running)
    		{
    			if(checkUserLogin())
                {
                	 checkCacheAvailable();
                     
                     boolean sessionActive = true;
                     while (sessionActive) {
                         String command = dis.readUTF();
                         System.out.println(command);
                         switch (command) {
                             case "RELOAD": 
                             	reloadTree();                   	 
                                 break;
                             case "UPLOAD":
                                 receiveFile(dis, connection);
                                 break;
                             case "DOWNLOAD":
                                 String fileName = dis.readUTF();          
                                 downloadFolderOrFile(handleDownloadLink(fileName));
                                 break;
                             case "CREATEFOLDER":
                             	createFolder(dis, connection);
                             	break; 
                             case "CREATEPROJECT":
                             	createRepository();
                             	break;
                             case "DELETE":
                             	deleteFile();
                             	break;
                             case "SHARE":
                             	shareHandle(dis,dos);
                             	break;
                             case "SYNC":
                            	 syncData();
                            	 break;
                             case "DISCONNECT":
                             	serverNotif.stopThreadServerNotif();
                             	sessionActive = false;
                             	running = false;
                             	break;
                             default:
                                 break;
                         }
                     }
                }
    		}           
        	         
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("94");
        } finally {
        	System.out.println("96");
            closeConnection();
        }
    }

	private void closeConnection() {
        try {
            if (socket != null) socket.close();
            if (dis != null) dis.close();
            if (dos != null) dos.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("261");
        }
    }
	
	private void checkCacheAvailable() throws IOException
	{
		 DefaultMutableTreeNode root = new DefaultMutableTreeNode("Repository");

         if (CacheManager.getInstance().isUserTreeCached(USERID)) {  
         	System.out.println("Tạo cây cho Client từ Cache");
         	CacheManager.getInstance().buildTreeForUser(root, USERID);
         } else {
         	CacheManager.getInstance().initCache(USERID);
         	CacheManager.getInstance().buildTreeForUser(root, USERID);
         };
         
         this.sendFileTree(root);
         System.out.println("Đã gửi Tree cho Client");
	}
    private boolean checkUserLogin() throws IOException, SQLException
    {
    	String username = dis.readUTF();
        String password = dis.readUTF();
        int userID = DatabaseManager.getInstance().authenticateUser(username, password);//dbManager.authenticateUser(username, password);
        if (userID == -1) {
            dos.writeUTF("AUTH_FAILED");
            dos.flush();
           // closeConnection();
            return false;
        } else {
        	this.USERID = userID;
        	
        	System.out.println("210");
            dos.writeUTF("Success");
            dos.writeInt(USERID);
            serverNotif = new ServerNotification(serverSocketNotification, userID);
            NotificationManager.getInstance().addUsersSocketHash(USERID, serverNotif);
            serverNotif.runningThread();
            serverNotif.start();
            return true;
            }
    }
	private void sendFileTree(DefaultMutableTreeNode root) throws IOException {
    	 System.out.println("Preparing to send file tree to client...");
    	    try {
    	        objectOutput.writeObject(root);
    	        objectOutput.flush();
    	        System.out.println("File tree sent to client successfully.");
    	    } catch (IOException e) {
    	        System.err.println("Failed to send file tree: " + e.getMessage());
    	        e.printStackTrace();
    	    }
    }
    private void reloadTree() throws IOException
    {
    	DefaultMutableTreeNode rootNew = new DefaultMutableTreeNode("Repository");
    	CacheManager.getInstance().buildTreeFromCache(rootNew, CacheManager.getInstance().getUserTree(USERID));
    	this.sendFileTree(rootNew);      
    }
    private void receiveFile(DataInputStream input, Connection connection) {
        try {
        	String addressFolder = input.readUTF();
            String fileName = input.readUTF();
            long fileSize = input.readLong();
            System.out.println(addressFolder);
            	
            String storageDirectory = "D:\\Java\\PBL4\\path" + handleUploadLinkString(addressFolder);// Thay đổi đường dẫn này thành đường dẫn bạn muốn lưu file
            System.out.println("Se luu file vao : "+ storageDirectory);
            File directory = new File(storageDirectory);
            

            if (!directory.exists()) {
                directory.mkdirs(); 
            }
            
            File fileToSave = new File(directory, fileName);
	        try (FileOutputStream fileOutput = new FileOutputStream(fileToSave)) {
	             byte[] buffer = new byte[4096]; // Đệm
	             int bytesRead;
	             long totalBytesRead = 0;
	
	             while ((bytesRead = input.read(buffer, 0, Math.min(buffer.length, (int) (fileSize - totalBytesRead)))) != -1) {
	                 fileOutput.write(buffer, 0, bytesRead);
	                 totalBytesRead += bytesRead;
	                 if (totalBytesRead >= fileSize) {
	                     break;
	                 }
	             }
	         }
	         System.out.println(addressFolder);
	            
	         DatabaseManager.getInstance().addFileToDatabase(addressFolder, fileName, USERID);
      
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
    }
    
    private String handleUploadLinkString (String path) {
        if (path.startsWith("Repository")) {
            return path.substring("Repository".length());
        }
        return path;
    }
    
    private String handleDownloadLink(String path) {
        if (path.startsWith("Repository/")) {
            return path.substring("Repository/".length());
        }
        return path;
    }
    
    private void downloadFolderOrFile(String fileName) {
        String storageDirectory = "D:\\Java\\PBL4\\path"; 
        File file = new File(storageDirectory, fileName); 
        if(file.isFile()) {
        	try{
        		dos.writeUTF("FILE");
        		if(dis.readUTF().equals("OK")) {
        			downloadFile(file);
        		}
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else {
        	try {
        		dos.writeUTF("FOLDER");
        		if(dis.readUTF().equals("OK")) {
        			downloadFolder(file);
        		}
			} catch (IOException e) {
				e.printStackTrace();
			}
        }
        
    }
    private void downloadFile(File file) throws IOException {
    	if (file.exists() && !file.isDirectory()) {
            
            dos.writeLong(file.length());

            try (FileInputStream fileInput = new FileInputStream(file)) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = fileInput.read(buffer)) != -1) {
                    dos.write(buffer, 0, bytesRead);
                }
            }

            System.out.println("Đã gửi file: " + file.getAbsolutePath());
            try {
			} catch (Exception e) {
				e.printStackTrace();
			}
        } else {
        	dos.writeLong(-1);
            System.out.println("File không tồn tại: " + file.getAbsolutePath());
        }
    }
    private void downloadFolder(File folder) throws IOException {
    	File zipFile = new File("D:\\Java\\PBL4\\path" + "/testzip.zip");
    	compressFolder(folder, zipFile);
    	downloadFile(zipFile);
    	deleteRecursively(zipFile);
    }
    
    private void compressFolder(File folder, File zipFile) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(zipFile);
             ZipOutputStream zos = new ZipOutputStream(fos)) {
            zipFolder(folder, folder.getName(), zos);
            
        }
    }

    private void zipFolder(File folder, String parentFolder, ZipOutputStream zos) throws IOException {
        File[] files = folder.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (file.isDirectory()) {
                zipFolder(file, parentFolder + "/" + file.getName(), zos);
            } else {
                try (FileInputStream fis = new FileInputStream(file)) {
                    ZipEntry zipEntry = new ZipEntry(parentFolder + "/" + file.getName());
                    zos.putNextEntry(zipEntry);

                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = fis.read(buffer)) > 0) {
                        zos.write(buffer, 0, bytesRead);
                    }
                    zos.closeEntry();
                }
            }
        }
    }
    private void createFolder(DataInputStream input, Connection connection) {
        try {
            String parentPath = input.readUTF(); 
            String folderName = input.readUTF(); 

            String storageDirectory = "D:\\Java\\PBL4\\path" + handleUploadLinkString(parentPath); // Đường dẫn đầy đủ trên server
            File parentDirectory = new File(storageDirectory);

            if (!parentDirectory.exists()) {
                System.out.println("Parent directory does not exist: " + storageDirectory);
                return;
            }

            File newFolder = new File(parentDirectory, folderName);
            if (newFolder.mkdir()) {
                System.out.println("Created new folder: " + newFolder.getAbsolutePath());

                DatabaseManager.getInstance().addFolderToDatabase(parentPath, folderName, USERID);
            } else {
                System.out.println("Failed to create folder: " + newFolder.getAbsolutePath());
            }
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
    }
    
    private void createRepository() {
    	try {
            String repoName = dis.readUTF(); 
            
            String storageDirectory = "D:\\Java\\PBL4\\path"; 
            File parentDirectory = new File(storageDirectory);
            
            File newRepo = new File(parentDirectory, repoName);
            if (newRepo.mkdir()) {
                System.out.println("Created new folder Repo: " + newRepo.getAbsolutePath());
 
                DatabaseManager.getInstance().addRepositoryToDatabase(storageDirectory, repoName, USERID);
            } else {
                System.out.println("Failed to create folder Repo: " + newRepo.getAbsolutePath());
            }
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
   	}
    
    private void deleteFile() throws Exception {
        try {
        	String address = dis.readUTF();   	

            String storageDirectory = "D:\\Java\\PBL4\\path" + handleUploadLinkString(address);
            File directory = new File(storageDirectory);
            System.out.println(directory.getAbsolutePath());
            System.out.println(directory.isDirectory());
            try {
                if (directory.isDirectory()) {

                	DatabaseManager.getInstance().deleteFolderOrRepoFromDatabase(address, USERID);
                    System.out.println("Deleted folder info from database: " + address);
                }
                else {                 
                    DatabaseManager.getInstance().deleteFileFromDatabase(address, USERID);
                    System.out.println("Deleted file info from database: " + address);
                }
            } catch (SQLException e) {
                System.err.println("Failed to delete info from database: " + e.getMessage());
                return;
            }
            
            deleteRecursively(directory);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private boolean deleteRecursively(File file) {
        if (file.isDirectory()) {
            for (File child : file.listFiles()) {
                if (!deleteRecursively(child)) {
                    return false;
                }
            }
        }
        return file.delete();
    }
    
    
    
    
    
    
    private void shareHandle(DataInputStream dis,DataOutputStream dos) throws Exception {
    	try {
    		String repoName = dis.readUTF();
    		List<String> unsharedUsers = DatabaseManager.getInstance().getUnsharedListFromDataBase(USERID, repoName);
    		sendListToClient(unsharedUsers, dos);
    		System.out.println("Da gui danh sach user da duoc chia se");
    		List<String> sharedUsers = DatabaseManager.getInstance().getSharedListFromDataBase(USERID,repoName);
    		sendListToClient(sharedUsers, dos);
    		System.out.println("Da  gui danh sach user chua duoc chia se");
    		
    		RepositoryEntity repo = DatabaseManager.getInstance().getRepoEntity(repoName);
    		boolean checkOwner = DatabaseManager.getInstance().isRepoOwner(repo.getRepoId(), USERID);
    		if(checkOwner) {
    			dos.writeUTF("Success");
    		}else {
				dos.writeUTF("Fail");
				return;
			}
    		
    		boolean sharing = true;
    		while(sharing) {
    			String command = dis.readUTF();
    			switch (command) {
    			case "SHARE_REPO":
    				handleShareRepo(dis, repo);
    				break;
    			case "UNSHARE_REPO":
    				handleUnshareRepo(dis, repo);
    				break;
    			case "CANCEL":
    				sharing = false;
    				break;
    			default:
    				System.out.println("Quit sharehandle");
    				sharing = false;
    				break;
    			}
    		}
		} catch (IOException e) {
			e.printStackTrace();
		}
    	
    }
    
    private void sendListToClient(List<String> listUser,DataOutputStream dos) throws IOException {
    	int count = listUser.size();
    	dos.writeInt(count);
    	for(int i=0; i<count;i++) {
    		dos.writeUTF(listUser.get(i));
    	}
    }
    
    public List<String> getUsersFromClient(DataInputStream dis) throws IOException 
    { 
        int userCount = dis.readInt(); 
        List<String> Users = new ArrayList<>();
        for (int i = 0; i < userCount; i++) {
            String userName = dis.readUTF(); 
            Users.add(userName);
        }
        return Users;
    }
    
    private void handleShareRepo(DataInputStream _dis, RepositoryEntity _repo) throws Exception {
        
    	List<String> userToShare = getUsersFromClient(_dis);
    	
    	for (int i = 0; i < userToShare.size();i++) {
    		if (!DatabaseManager.getInstance().isUserShared(_repo.getRepoId(), userToShare.get(i))) {
    			DatabaseManager.getInstance().shareRepoWithUser(_repo, userToShare.get(i), USERID);
            } else {
                System.out.println("Co loi khi chia se");
                return;
            }
    	}
    }

    private void handleUnshareRepo(DataInputStream _dis, RepositoryEntity _repo) throws Exception {
    	List<String> userToUnShare = getUsersFromClient(_dis);
    	
    	for (int i = 0; i < userToUnShare.size();i++) {
    		if (DatabaseManager.getInstance().isUserShared(_repo.getRepoId(), userToUnShare.get(i))) {
    			DatabaseManager.getInstance().unshareRepoWithUser(_repo, userToUnShare.get(i));
            } else {
                System.out.println("Co loi khi huy chia se");
                return;
            }
    	}
    }
    
	private void syncData() throws SQLException, Exception {
		CacheManager.getInstance().syncAllDataDelay(USERID);
	}
}
