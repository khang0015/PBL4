package Controller;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import javax.swing.JOptionPane;

public class ClientNotificationThread extends Thread{
	private DataInputStream dis;
	private DataOutputStream dos;
	private boolean running;
	private int yourUserID;
	private Socket socket;
	private String warrning = "";
	public ClientNotificationThread(Socket socket, int _yourUserId) {
		this.yourUserID = _yourUserId;
		try {		
			this.socket = socket;
			this.dis = new DataInputStream(socket.getInputStream());
			this.dos = new DataOutputStream(socket.getOutputStream());
		} catch (IOException e) {
			System.out.println("22: ClientNotificationThread");
			try {
				this.socket.close();
			}catch(IOException e1) {
				System.out.println("26: ClientNotificationThread");
				e.printStackTrace();
			}
			
		}
	}

	public void runningThread()
	{
		running = true;
	}
	public void stopThreadClientNotif()
	{
		running = false;
	}
	public void setWarningSync(String _state)
	{
		this.warrning = _state;
	}
	public String getWarning() {
		return warrning;
	}
	public void run() {
		while(running) {
			try {
				String command = dis.readUTF();
				if(command.equals("SHARE")) {
			        Object[] options = {"Chấp nhận", "Để sau", "Không"};			        
			        int result = JOptionPane.showOptionDialog(
			            null, 
			            "Đã có chia sẻ dữ liệu từ người dùng khác đến bạn, vui lòng đồng bộ!", 
			            "Thông báo", 
			            JOptionPane.YES_NO_CANCEL_OPTION, 
			            JOptionPane.INFORMATION_MESSAGE, 
			            null, 
			            options,
			            options[0] 
			        );
			        
			        if (result == JOptionPane.YES_OPTION) {
			            System.out.println("Người dùng chọn Accept");
			            dos.writeUTF("ACCEPTSHARE");
			            dos.writeInt(yourUserID);
			        }else if (result == JOptionPane.NO_OPTION) {
			            System.out.println("Người dùng chọn Để sau");
			            setWarningSync("Cần đồng bộ");
			            dos.writeUTF("WAIT");
			        }else if(result == JOptionPane.CANCEL_OPTION) {
			        	System.out.println("Người dùng chọn Không");
			            dos.writeUTF("CANCEL");
			            dos.writeInt(yourUserID);
			        }
			    }
				else if(command.equals("UNSHARE"))
				{
					Object[] options = {"Xác nhận"};			        
			        int result = JOptionPane.showOptionDialog(
			            null, 
			            "Đã có hủy chia sẻ dữ liệu từ người dùng khác đến bạn, vui lòng đồng bộ!", 
			            "Thông báo", 
			            JOptionPane.YES_NO_OPTION, 
			            JOptionPane.INFORMATION_MESSAGE, 
			            null, 
			            options,
			            options[0] 
			        );
			        
			        if (result == JOptionPane.YES_OPTION) {
			            System.out.println("Người dùng chọn Accept");
			            dos.writeUTF("ACCEPTUNSHARE");
			        }	
				}
				else if(command.equals("CHANGE"))
				{
					Object[] options = {"OK"};			        
			        int result = JOptionPane.showOptionDialog(
			            null, 
			            "Đã có thay đổi dữ liệu trong Folder được chia sẻ, vui lòng đồng bộ!", 
			            "Thông báo", 
			            JOptionPane.YES_NO_OPTION, 
			            JOptionPane.INFORMATION_MESSAGE, 
			            null, 
			            options,
			            options[0] 
			        );
			        
			        if (result == JOptionPane.YES_OPTION) {
			            System.out.println("Người dùng chọn OK");
			            dos.writeUTF("OK");
			        }	
			        setWarningSync("Cần đồng bộ");
				}
				else if(command.equals("CLOSE"))
				{
					running = false;
					dos.writeUTF("CLOSE");
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
	}

}
