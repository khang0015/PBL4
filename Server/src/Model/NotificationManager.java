package Model;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Vector;
import Controller.ServerNotification;
import Model.Tree.RepositoryEntity;

public class NotificationManager {
	
	private static HashMap<Integer, ServerNotification> usersSocketHash;
	private static NotificationManager instance;
	public static NotificationManager getInstance()
	{
		if(instance == null)
			instance = new NotificationManager();
		return instance;
	}
	public NotificationManager() {
		usersSocketHash = new HashMap<Integer, ServerNotification>();
	}

	public HashMap<Integer, ServerNotification> getUsersSocketHash(){
		return usersSocketHash;
	}
	public void addUsersSocketHash(Integer userId, ServerNotification sn) {
		usersSocketHash.put(userId, sn);
	}
	public void SendShareNotification(int _userId, String action) {
		try {
			System.out.println("34 NotificationManager " + _userId + "-" + usersSocketHash.get(_userId));				
			ServerNotification currentSocket = usersSocketHash.get(_userId);
			if(currentSocket == null)
				return;
			else
				currentSocket.SendNotif(action);			
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
	public void SendChangeNotification(RepositoryEntity repo, int _userId, String action) {
		try {
			Vector<Integer> userAllId = DatabaseManager.getInstance().getUsersInRepo(repo);
			Vector<Integer> userIdsAvai = new Vector<Integer>();
			
			System.out.println("30 NotificationManager " + userAllId.size());
			for(Integer us : userAllId)
			{
				if(usersSocketHash.get(us) == null)
					continue;
				if(us != _userId)
				{
					userIdsAvai.add(us);
				}
			}
			for (Integer userId : userIdsAvai) {
				System.out.println("34 NotificationManager " + userId + "-" + usersSocketHash.get(userId));				
				ServerNotification currentSocket = usersSocketHash.get(userId);
				currentSocket.SendNotif(action);									
	    	}		
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
}
