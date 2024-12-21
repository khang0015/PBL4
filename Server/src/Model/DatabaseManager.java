package Model;

import Model.Tree.FileEntity;
import Model.Tree.FolderEntity;
import Model.Tree.RepositoryEntity;

import java.awt.Desktop.Action;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class DatabaseManager {
	 private static Connection connection;
	 private static final String DB_URL = "jdbc:mysql://localhost:3306/file_sync_demo";
	 private static final String USER = "root"; 
	 private static final String PASSWORD = "";

	 public enum ActionType {
		 	ADDFILE,
		    ADDFOLDER,
		    ADDREPO,
		    DELETEFILE,
		    DELETEFOLDER,
		    DELETEREPO,
		    SHARE,
		    UNSHARE,
		}
	 private static volatile DatabaseManager instance;
	 public static DatabaseManager getInstance() throws SQLException {
	        if (instance == null) {
	            synchronized (DatabaseManager.class) {
	                if (instance == null) { 
	                    instance = new DatabaseManager();
	                }
	            }
	        }
	        return instance;
	    }
	 

	 public DatabaseManager() throws SQLException {
	     connection = DriverManager.getConnection(DB_URL,USER,PASSWORD);
	 }

	 public Connection getConnection() {
	     return connection;
	 }

	 	public int authenticateUser(String username, String password) throws SQLException {
	 		String query = "SELECT UserID FROM Users WHERE UserName = ? AND UserPassword = ?";
	 		try (PreparedStatement stmt = connection.prepareStatement(query)) {
	 			stmt.setString(1, username);
	 			stmt.setString(2, password);
	 			ResultSet rs = stmt.executeQuery();
	 			return rs.next() ? rs.getInt("UserID") : -1;
	 		}
	 	}	
		
		// fetch
		public List<RepositoryEntity> fetchRepositories() throws Exception {
			String query = "SELECT RepoID, RepoName FROM Repositories";
			List<RepositoryEntity> repositories = new ArrayList<>();
			try (PreparedStatement stmt = connection.prepareStatement(query);
					ResultSet rs = stmt.executeQuery()) {
				while (rs.next()) {
					repositories.add(new RepositoryEntity(rs.getInt("RepoID"), rs.getString("RepoName")));
				}
			}
			return repositories;				
		}
		
		public List<RepositoryEntity> fetchRepositories(int usedId) throws Exception {
			String query = "SELECT ura.RepoID, r.RepoName " +
		               "FROM Users u " +
		               "JOIN UserRepoAccess ura ON u.UserID = ura.UserID " +
		               "JOIN Repositories r ON ura.RepoID = r.RepoID " +
		               "WHERE u.UserID = ?";

			List<RepositoryEntity> repositories = new ArrayList<>();
			try (PreparedStatement stmt = connection.prepareStatement(query)) {
				stmt.setInt(1, usedId);
				ResultSet rs = stmt.executeQuery();

				while (rs.next()) {
					int repoID = rs.getInt("RepoID");
					String repoName = rs.getString("RepoName");

					repositories.add(new RepositoryEntity(repoID, repoName));

					System.out.println("Number of unique users: " + repositories.size());
				}
				
	        // In ra thông tin từng user (check)
				for (RepositoryEntity repoEntity : repositories) {
					System.out.println("Repo ID: " + repoEntity.getRepoId());
					System.out.println("Repo Name: " + repoEntity.getRepoName());
				}
				return repositories;
			} catch (SQLException e) {
				e.printStackTrace();
				throw new SQLException("Error retrieving user repository data.");
			}				
		}
		
				
		public List<FolderEntity> fetchFolders(int repoId) throws Exception {
			String query = "SELECT * FROM Folders WHERE RepoID = ? AND ParentFolderID IS NULL";
			List<FolderEntity> folders = new ArrayList<>();
			try (PreparedStatement stmt = connection.prepareStatement(query)) {
				stmt.setInt(1, repoId);
				try (ResultSet rs = stmt.executeQuery()) {
					while (rs.next()) {
						folders.add(new FolderEntity(rs.getInt("FolderID"), rs.getString("FolderName"), rs.getInt("RepoID"), rs.getObject("ParentFolderID", Integer.class)));
					}
				}
			}
			return folders;
		}
		
		public List<FolderEntity> fetchSubFolders(Integer parentFolderId) throws Exception {
			String query = "SELECT * FROM Folders WHERE ParentFolderID = ?";
			List<FolderEntity> folders = new ArrayList<>();
			try (PreparedStatement stmt = connection.prepareStatement(query)) {
				stmt.setObject(1, parentFolderId);////////////
				try (ResultSet rs = stmt.executeQuery()) {
					while (rs.next()) {
						folders.add(new FolderEntity(rs.getInt("FolderID"), rs.getString("FolderName"), rs.getInt("RepoID"), rs.getObject("ParentFolderID", Integer.class)));
					}
				}
			}
			return folders;
		}
		
		public List<FileEntity> fetchFiles(int folderId) throws Exception {
			String query = "SELECT FileName, FolderID FROM Files WHERE FolderID = ?";
			List<FileEntity> files = new ArrayList<>();
			try (PreparedStatement stmt = connection.prepareStatement(query)) {
				stmt.setInt(1, folderId);////////////////
				try (ResultSet rs = stmt.executeQuery()) {
					while (rs.next()) {
						files.add(new FileEntity(rs.getString("FileName"), rs.getInt("FolderID")));
					}
				}
			}
		return files;
		}
		
		public RepositoryEntity getRepoEntity(String repoName) throws SQLException {
    		String selectRepo = "SELECT RepoID, RepoName FROM Repositories WHERE RepoName = ?";
    		try (PreparedStatement stmt = connection.prepareStatement(selectRepo)) {
    			stmt.setString(1, repoName);
    			ResultSet rs = stmt.executeQuery();
    			if (rs.next()) {
    				int repoID = rs.getInt("RepoID");
    				String repoNameFromDB = rs.getString("RepoName");
    				RepositoryEntity repoEntity = new RepositoryEntity(repoID, repoNameFromDB);
                	return repoEntity;
    			}
    		}
    		throw new SQLException("Repository not found");
    	}
	
		//end fetch
		
		// Add File to database
    	public void addFileToDatabase(String _path, String _fileName, int _userId) throws SQLException {
    		String[] parts = _path.split("/");
    		String repoName = parts[1];
    		String fileName = _fileName;

    		RepositoryEntity repoEntity = getRepoEntity(repoName);
    		
    		int repoID = repoEntity.getRepoId();
    		
    		Integer parentFolderID = null;

    		for (int i = 2; i < parts.length; i++) {
    			parentFolderID = getFolderEntity(parts[i], repoID, parentFolderID).getFolderId();
    		}
        
    		FileEntity fileEntity = new FileEntity(fileName, parentFolderID);
    		
    		CacheManager.getInstance().changeCache(fileEntity, _userId, ActionType.ADDFILE.name());
    		
    		addFile(fileEntity);
    		
    		fileEntity.setState("ADD");
    		fileEntity.setIdUserDoer(_userId);
    		FileSyncCache.getInstance().updateEntityForAllUsers(repoEntity, fileEntity);
    		NotificationManager.getInstance().SendChangeNotification(repoEntity, _userId, ActionType.ADDFILE.name());  	
    	}

    	
    	private void addFile(FileEntity fileEntity) throws SQLException {
			String insertFile = "INSERT INTO Files (FileName, FolderID) VALUES (?, ?)";
			try (PreparedStatement stmt = connection.prepareStatement(insertFile)) {
				stmt.setString(1, fileEntity.getFileName());
				stmt.setObject(2, fileEntity.getFolderId()); 
				stmt.executeUpdate();
			}
		}

    	private FolderEntity getFolderEntity(String folderName, int repoID, Integer parentFolderID) throws SQLException {
    		String selectFolder = "SELECT * FROM Folders WHERE FolderName = ? AND RepoID = ? AND ParentFolderID " +
                              	(parentFolderID == null ? "IS NULL" : "= ?");
    		try (PreparedStatement stmt = connection.prepareStatement(selectFolder)) {
    			stmt.setString(1, folderName);
    			stmt.setInt(2, repoID);
    			if (parentFolderID != null) {
    				stmt.setInt(3, parentFolderID);
    			}
    			ResultSet rs = stmt.executeQuery();
    			if (rs.next()) {
    				int folderID = rs.getInt("FolderID");
    				String folderNameFromDB = rs.getString("FolderName");
    				int repoIDFromDB = rs.getInt("RepoID");
    				Integer parentFolderIDFromDB = rs.getObject("ParentFolderID", Integer.class);
    				FolderEntity folderEntity = new FolderEntity(folderID, folderNameFromDB, repoIDFromDB, parentFolderIDFromDB);
    				return folderEntity;
    			}
    		}
    		throw new SQLException("Folder not found");
    	}
    	
    	// end Add File to database
    	
    	// add folder 
    	public void addFolderToDatabase(String _path, String _folderName, int _userId) throws SQLException {
        	String[] parts = _path.split("/");
    		String repoName = parts[1];
    		String folderName = _folderName;

    		RepositoryEntity repoEntity = getRepoEntity(repoName);
    		int repoID = repoEntity.getRepoId();

    		Integer parentFolderID = null;

    		for (int i = 2; i < parts.length; i++) {
    			parentFolderID = getFolderEntity(parts[i], repoID, parentFolderID).getFolderId();
    		}
    		
    		FolderEntity folder = new FolderEntity(folderName, repoID, parentFolderID);
    		addFolder(folder);
    		   		
    		
    		FolderEntity folderEntity = getFolderEntity(folderName, repoID, parentFolderID);
    		
    		CacheManager.getInstance().changeCache(folderEntity, _userId, ActionType.ADDFOLDER.name());
    		
    		folderEntity.setState("ADD");
    		folderEntity.setIdUserDoer(_userId);
    		FileSyncCache.getInstance().updateEntityForAllUsers(repoEntity, folderEntity);
    		NotificationManager.getInstance().SendChangeNotification(repoEntity, _userId, ActionType.ADDFOLDER.name());  	
        }
        
        private void addFolder(FolderEntity folder) throws SQLException {
            String query = "INSERT INTO folders (FolderName, RepoID, ParentFolderID) VALUES (?, ?, ?)";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, folder.getFolderName());
                stmt.setInt(2, folder.getRepoId());
                stmt.setObject(3, folder.getParentFolderId());
                stmt.executeUpdate();
            }
        }
        
        private FolderEntity getFolderEntity(Integer FolderID) throws SQLException {
    		String selectFolder = "SELECT * FROM Folders WHERE FolderID = ?";
    		try (PreparedStatement stmt = connection.prepareStatement(selectFolder)) {
    			stmt.setObject(1, FolderID);
    			ResultSet rs = stmt.executeQuery();
    			if (rs.next()) {
    				FolderEntity folderEntity = new FolderEntity(rs.getInt("FolderID"), rs.getString("FolderName"), rs.getInt("RepoID"), rs.getObject("ParentFolderID", Integer.class));
    				return folderEntity;
    			}
    		}
    		throw new SQLException("Folder not found");
    	}
        //end add folder
        
        
        
        // add repo
        public void addRepositoryToDatabase(String _path,String _repoName, int _userId) throws SQLException {
    		String repoName = _repoName;
    		RepositoryEntity repository = new RepositoryEntity(repoName, _path);
    		addRepo(repository, _userId);
    		
    		RepositoryEntity repo = getRepoEntity(repoName);
    		CacheManager.getInstance().changeCache(repo, _userId, ActionType.ADDREPO.name());
    		
        }
        private void addRepo(RepositoryEntity repository, int _userId) throws SQLException {
            String query = "INSERT INTO repositories (RepoName, RepoPath, OwnerID) VALUES (?, ?, ?)";
            try (PreparedStatement stmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, repository.getRepoName());
                stmt.setString(2, repository.getPath());
                stmt.setInt(3, _userId);
                int rowsInserted = stmt.executeUpdate();

                if (rowsInserted > 0) {
                    ResultSet generatedKeys = stmt.getGeneratedKeys();
                    if (generatedKeys.next()) {
                        int repoId = generatedKeys.getInt(1);

                        String userRepoAccessQuery = "INSERT INTO userrepoaccess (UserID, RepoID) VALUES (?, ?)";
                        try (PreparedStatement userRepoAccessStmt = connection.prepareStatement(userRepoAccessQuery)) {
                            userRepoAccessStmt.setInt(1, _userId);
                            userRepoAccessStmt.setInt(2, repoId);
                            userRepoAccessStmt.executeUpdate();
                        }
                    } else {
                        System.err.println("Không lấy được RepoID sau khi thêm repository");
                    }
                }
            }
        }
        //end add repo
        
        // delete folder, repo
        public void deleteFolderOrRepoFromDatabase(String _path, int _userId) throws Exception {
    	    String[] parts = _path.split("/");
    	    String repoName = parts[1];
 
    	    RepositoryEntity repoEntity = getRepoEntity(repoName);
    	    int repoID = repoEntity.getRepoId();

    	    Integer parentFolderID = null;
    	    
    	    if(parts.length > 2) {
    		    for (int i = 2; i < parts.length ; i++) {
    		        parentFolderID = getFolderEntity(parts[i], repoID, parentFolderID).getFolderId();
    		    }
    	
    		    FolderEntity folderEntity = getFolderEntity(parentFolderID);
    		    
    		    folderEntity.setState("DELETE");
    		    folderEntity.setIdUserDoer(_userId);
    		    FileSyncCache.getInstance().updateEntityForAllUsers(repoEntity, folderEntity);
    		    
    		    CacheManager.getInstance().changeCache(folderEntity, _userId, ActionType.DELETEFOLDER.name());    		    
    		    deleteFolderAndContents(folderEntity, _userId);
    	    }
    	    else {
    	    	CacheManager.getInstance().changeCache(repoEntity, _userId, ActionType.DELETEREPO.name());
    	    	deleteRepositoryAndContents(repoEntity, _userId);
    	    }
    	    
    	    NotificationManager.getInstance().SendChangeNotification(repoEntity, _userId, ActionType.DELETEREPO.name());
    	}
        
        private void deleteFolderAndContents(FolderEntity folder, int _userId) throws Exception {

    	    List<FileEntity> files = fetchFiles(folder.getFolderId());
    	    for (FileEntity file : files) {    	
    	    	file.setState("DELETE");
    	    	file.setIdUserDoer(_userId);
    	    	CacheManager.getInstance().changeCache(file, _userId, ActionType.DELETEFILE.name());    	        
    	    	deleteFile(file);
    	    }

    	    List<FolderEntity> subFolders = fetchSubFolders(folder.getFolderId());
    	    for (FolderEntity subFolder : subFolders) {  
    	    	subFolder.setState("DELETE");
    	    	subFolder.setIdUserDoer(_userId);
    	    	deleteFolderAndContents(subFolder, _userId);
    	    }
    	    
    	    CacheManager.getInstance().changeCache(folder, _userId, ActionType.DELETEFOLDER.name());
    	    deleteFolder(folder);
    	    
    	}
        
        private void deleteRepositoryAndContents(RepositoryEntity repoEntity, int _userId) throws Exception {
    	    List<FolderEntity> folders = fetchFolders(repoEntity.getRepoId());
    	    for (FolderEntity folder : folders) {
    	        deleteFolderAndContents(folder, _userId);
    	    }
    	    System.out.println("319: databasemanager");
    	    
    	    deleteRepository(repoEntity);
    	}
    	
    	
        
    	private void deleteRepository(RepositoryEntity repo) throws SQLException {
    	    String query1 = "DELETE FROM userrepoaccess WHERE RepoID = ?;";
    	    String query2 = "DELETE FROM Repositories WHERE RepoID = ?;";

    	    try {
    	        // Bắt đầu giao dịch
    	        connection.setAutoCommit(false);

    	        // Thực hiện câu lệnh đầu tiên
    	        try (PreparedStatement stmt1 = connection.prepareStatement(query1)) {
    	            stmt1.setInt(1, repo.getRepoId());
    	            stmt1.executeUpdate();
    	        }

    	        // Thực hiện câu lệnh thứ hai
    	        try (PreparedStatement stmt2 = connection.prepareStatement(query2)) {
    	            stmt2.setInt(1, repo.getRepoId());
    	            stmt2.executeUpdate();
    	        }

    	        // Xác nhận giao dịch
    	        connection.commit();
    	    } catch (SQLException e) {
    	        // Hủy giao dịch nếu có lỗi
    	        connection.rollback();
    	        throw new SQLException("Failed to delete repository with ID: " + repo.getRepoId(), e);
    	    } finally {
    	        // Đặt lại chế độ tự động commit
    	        connection.setAutoCommit(true);
    	    }
    	}			

	
       
        private boolean deleteFolder(FolderEntity folder) throws SQLException {
        	System.out.println("VAO DELETE FOLDER");
            String query = "DELETE FROM Folders WHERE FolderID = ? AND " +
                           "(ParentFolderID = ? OR (ParentFolderID IS NULL AND ? IS NULL))";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setInt(1, folder.getFolderId());
                
                if (folder.getParentFolderId() != null) {
                    stmt.setInt(2, folder.getParentFolderId());
                    stmt.setObject(3, folder.getParentFolderId()); 
                } else {
                    stmt.setNull(2, java.sql.Types.INTEGER);
                    stmt.setNull(3, java.sql.Types.INTEGER);
                }
                return stmt.executeUpdate() > 0;
            }
        }
        
        public void deleteFileFromDatabase(String _path, int _userId) throws Exception {
        	String[] parts = _path.split("/");
    		String repoName = parts[1];
    		String fileName = parts[parts.length - 1];

    		RepositoryEntity repoEntity = getRepoEntity(repoName);
    		int repoID = repoEntity.getRepoId();

    		Integer parentFolderID = null;

    		for (int i = 2; i < parts.length - 1; i++) {
    			parentFolderID = getFolderEntity(parts[i], repoID, parentFolderID).getFolderId();
    		}
    		
    		System.out.println("349 database mânger: " + parentFolderID);
    		FileEntity fileEntity = new FileEntity(fileName, parentFolderID);
    		
    		CacheManager.getInstance().changeCache(fileEntity, _userId, ActionType.DELETEFILE.name());
    		
    		deleteFile(fileEntity);
    		
    		fileEntity.setState("DELETE");
    		fileEntity.setIdUserDoer(_userId);
    		FileSyncCache.getInstance().updateEntityForAllUsers(repoEntity, fileEntity);
    		NotificationManager.getInstance().SendChangeNotification(repoEntity, _userId, ActionType.DELETEFILE.name());

        }
        private void deleteFile(FileEntity file) throws SQLException {
            String query = "DELETE FROM Files WHERE FileName = ? AND FolderID = ?";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, file.getFileName());
                stmt.setInt(2, file.getFolderId());
                stmt.executeUpdate();
            }
        }
        
        
        
        
        
        
        // Share
      //share
        public List<String> getSharedListFromDataBase(int userId, String repoName) throws SQLException {
            String query = """
                SELECT u.UserName 
                FROM UserRepoAccess ura
                JOIN Users u ON ura.UserID = u.UserID
                WHERE ura.RepoID = (SELECT RepoID FROM Repositories WHERE RepoName = ? AND OwnerID = ?)
                AND u.UserID != ?;
            """;
            List<String> sharedUsers = new ArrayList<>();
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, repoName);
                stmt.setInt(2, userId); // Kiểm tra repo thuộc quyền của userId
                stmt.setInt(3, userId); // Bỏ qua chủ sở hữu
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    sharedUsers.add(rs.getString("UserName"));
                }
            }
            return sharedUsers;
        }
        
        public List<String> getUnsharedListFromDataBase(int userId, String repoName) throws SQLException {
            String query = """
                SELECT u.UserName 
                FROM Users u
                WHERE u.UserID != ? -- Bỏ qua chủ sở hữu
                AND u.UserID NOT IN (
                    SELECT ura.UserID 
                    FROM UserRepoAccess ura
                    WHERE ura.RepoID = (SELECT RepoID FROM Repositories WHERE RepoName = ? AND OwnerID = ?)
                );
            """;
            List<String> unsharedUsers = new ArrayList<>();
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setInt(1, userId); // Bỏ qua chủ sở hữu
                stmt.setString(2, repoName);
                stmt.setInt(3, userId); // Kiểm tra repo thuộc quyền của userId
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    unsharedUsers.add(rs.getString("UserName"));
                }
            }
            return unsharedUsers;
        }


        public boolean isRepoOwner(int repoId, int userId) throws SQLException {
            String query = "SELECT COUNT(*) FROM Repositories WHERE RepoID = ? AND OwnerID = ?";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setInt(1, repoId);
                stmt.setInt(2, userId);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
            return false;
        }
        
        public boolean isUserShared(int repoId, String userName) throws SQLException {
            String query = """
                SELECT COUNT(*)FROM UserRepoAccess ura
            JOIN Users u ON ura.UserID = u.UserID
            WHERE ura.RepoID = ? AND u.UserName = ?
        """;
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, repoId);
            stmt.setString(2, userName);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        }
        return false;
    }
        public void addShareRepoToDatabase(RepositoryEntity repo, String userName) throws SQLException
        {
        	String query = """
	            INSERT INTO UserRepoAccess (RepoID, UserID) 
	            SELECT ?, UserID FROM Users WHERE UserName = ? 
	        """; //repo id duoc them + userid bi them
        	try (PreparedStatement stmt = connection.prepareStatement(query)) {
        		stmt.setInt(1, repo.getRepoId());
        		stmt.setString(2, userName);
        		stmt.executeUpdate();
        	}
        	
        }
        public void shareRepoWithUser(RepositoryEntity repo, String userName, int _userId) throws Exception 
        {
	        repo.setOwnerId(_userId);
        	int sharedUserId = getUserID(userName);
	        FileSyncCache.getInstance().addDataToSyncCache(sharedUserId, repo, 
	        		CacheManager.getInstance().getListForderShare(repo.getRepoId()), 
	        		CacheManager.getInstance().getListSubFolderShare(CacheManager.getInstance().getListForderShare(repo.getRepoId())), 
	        		CacheManager.getInstance().getListFileShare(CacheManager.getInstance().getListForderShare(repo.getRepoId()), CacheManager.getInstance().getListSubFolderShare(CacheManager.getInstance().getListForderShare(repo.getRepoId()))));

	        NotificationManager.getInstance().SendShareNotification(sharedUserId, ActionType.SHARE.name());

        }
        private Integer getUserID(String _userName) throws SQLException
        {
        	String query = "SELECT UserID FROM Users WHERE UserName = ?";
        	try (PreparedStatement stmt = connection.prepareStatement(query)) {
	            stmt.setString(1, _userName);
	            ResultSet rs = stmt.executeQuery();
	            if (rs.next()) {
                    return rs.getInt(1);
                }
        	}
        	return null;
        }
        public String getUserNameByID(int _userId) throws SQLException
        {
        	String query = "SELECT UserName FROM Users WHERE UserID = ?";
        	try (PreparedStatement stmt = connection.prepareStatement(query)) {
	            stmt.setInt(1, _userId);
	            ResultSet rs = stmt.executeQuery();
	            if (rs.next()) {
                    return rs.getString(1);
                }
        	}
        	return null;
        }
        public void unshareRepoWithUser(RepositoryEntity repo, String userName) throws Exception {
	        String query = """
	            DELETE FROM UserRepoAccess 
	            WHERE RepoID = ? AND UserID = (SELECT UserID FROM Users WHERE UserName = ?)
	        """;
	        try (PreparedStatement stmt = connection.prepareStatement(query)) {
	            stmt.setInt(1, repo.getRepoId());
	            stmt.setString(2, userName);
	            stmt.executeUpdate();
	        }
	        
	        int unshareUserId = getUserID(userName);
	        CacheManager.getInstance().unshareCache(unshareUserId, repo);
	        FileSyncCache.getInstance().removeRepository(unshareUserId, repo);
	        NotificationManager.getInstance().SendShareNotification(unshareUserId, ActionType.UNSHARE.name());
	        
        }             


        public Vector<Integer> getUsersInRepo(RepositoryEntity repo) throws SQLException {
            Vector<Integer> users = new Vector<>();
           
            String sql = "SELECT UserID FROM userrepoaccess WHERE RepoID = ?";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setInt(1, repo.getRepoId());
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        users.add(rs.getInt("UserID"));
                    }
                }
                
            }
            return users;
        }

}
