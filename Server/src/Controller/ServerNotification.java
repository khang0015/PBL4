package Controller;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import Model.CacheManager;
import Model.DatabaseManager;
import Model.FileSyncCache;

public class ServerNotification extends Thread{
	private Socket socketNotification;
	private DataInputStream disNotification;
    private DataOutputStream dosNotification;
    private int USERID;
    private int userIdhasChoice;
    private boolean running;
    public ServerNotification(Socket socketNotification, int _userId){
    	this.USERID = _userId;
    	this.socketNotification = socketNotification;
    	try {
    		this.disNotification = new DataInputStream(socketNotification.getInputStream());
            this.dosNotification = new DataOutputStream(socketNotification.getOutputStream());
    	}catch (Exception e) {
            System.err.println("Lỗi khi tạo luồng dữ liệu cho client: " + e.getMessage());
            try {
                socketNotification.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
    public void runningThread()
	{
		running = true;
	}
    public void stopThreadServerNotif() throws IOException
    {
    	dosNotification.writeUTF("CLOSE");
    }
    @Override
    public void run() {
    	while(running) {
			try {
				String command = disNotification.readUTF();
				if(command.equals("ACCEPTSHARE"))
				{	
					int userIdAccept = disNotification.readInt();
					System.out.println("ID Client ACCEPT: " + userIdAccept);   	
					try {
						//DatabaseManager.getInstance().addShareRepoToDatabase(FileSyncCache.getInstance().getLastRepository(userIdAccept), DatabaseManager.getInstance().getUserNameByID(userIdAccept));					
						CacheManager.getInstance().syncDataSharedForCache(userIdAccept);
					} catch (Exception e) {
						System.out.println("Lỗi khi đồng bộ dữ liệu đến cache");
						e.printStackTrace();
					}
				}
				else if(command.equals("CANCEL"))
				{
					int userIdAccept = disNotification.readInt();
					FileSyncCache.getInstance().removeLastRepository(userIdAccept);
				}
				else if(command.equals("WAIT")){}
				else if(command.equals("OK")){}
				else if(command.equals("ACCEPTUNSHARE")){}
				else if(command.equals("CLOSE"))
				{
					running = false;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
    	}
    }
    public void SendNotif(String action) {
        try {
            String message = null;

            switch (action) {
                case "SHARE":
                    message = "SHARE";
                    break;
                case "UNSHARE":
                    message = "UNSHARE";
                    break;
                case "ADDFILE":
                case "ADDFOLDER":
                case "DELETEFILE":
                case "DELETEFOLDER":
                case "DELETEREPO":
                    message = "CHANGE";
                    break;
                default:
                    break;
            }

            if (message != null) {
                dosNotification.writeUTF(message);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}