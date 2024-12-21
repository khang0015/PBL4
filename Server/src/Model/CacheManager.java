package Model;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.swing.tree.DefaultMutableTreeNode;
import Model.Tree.FileEntity;
import Model.Tree.FolderEntity;
import Model.Tree.RepositoryEntity;

public class CacheManager {
	private static final HashMap<Integer, UserCache> userCacheHash = new HashMap<Integer, UserCache>();
	
	private static List<RepositoryEntity> baseRepos;
    private static List<FolderEntity> baseFolders;
    private static List<FolderEntity> baseSubFolders;
    private static List<FileEntity> baseFiles;
    
	
	private List<RepositoryEntity> currentRepos;
    private List<FolderEntity> currentFolders;
    private List<FolderEntity> currentSubFolders;
    private List<FileEntity> currentFiles;
    
    private boolean syncTreeServer = false;
    
    private static volatile CacheManager instance; // Double-Checked Locking

    public static CacheManager getInstance() {
        if (instance == null) {
            synchronized (CacheManager.class) {
                if (instance == null) { 
                    instance = new CacheManager();
                }
            }
        }
        return instance;
    }
    public boolean isSyncTreeServer()
    {
    	return syncTreeServer;
    }
    public void setSyncServer(boolean _state)
    {
    	syncTreeServer = _state;
    }
    
    public CacheManager() {
        baseRepos = new ArrayList<RepositoryEntity>();
        baseFolders = new ArrayList<FolderEntity>();
        baseSubFolders = new ArrayList<FolderEntity>();
        baseFiles = new ArrayList<FileEntity>();
    }

    private void initCacheRepo(Integer userId)
    {
    	try {
			if(userId == null)
				baseRepos = DatabaseManager.getInstance().fetchRepositories();
			else
				currentRepos = DatabaseManager.getInstance().fetchRepositories(userId);
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    
    private void initCacheFolder(List<RepositoryEntity> repos, Integer userId)
    {
    	try {
    		for (RepositoryEntity repo : repos) {
    			List<FolderEntity> repoFolders = DatabaseManager.getInstance().fetchFolders(repo.getRepoId());
    			if (repoFolders != null && !repoFolders.isEmpty()) {
    				if(userId == null)
    					baseFolders.addAll(repoFolders);
    				else
    					currentFolders.addAll(repoFolders);
    			}
    		}    		
    	}catch (Exception e) {
    		e.printStackTrace();
		}
    }
    
    private void initCacheSubFolder(List<FolderEntity> folderEntities, Integer usedId) {
        try {
            List<FolderEntity> newSubFolders = new ArrayList<>(); 

            for (FolderEntity folder : folderEntities) {
                List<FolderEntity> subFolders = DatabaseManager.getInstance().fetchSubFolders(folder.getFolderId());
                if (subFolders != null && !subFolders.isEmpty()) {
                    newSubFolders.addAll(subFolders); 
                }
            }

            if (!newSubFolders.isEmpty()) {
            	if(usedId == null)            	
            		baseSubFolders.addAll(newSubFolders);
            	else
            		currentSubFolders.addAll(newSubFolders); 
                
            	initCacheSubFolder(newSubFolders, usedId);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    
    private void initCacheFile(List<FolderEntity> subFolders, List<FolderEntity> folders, Integer usedId) {
        try {
            for (FolderEntity subFolder : subFolders) {
                List<FileEntity> newFiles = DatabaseManager.getInstance().fetchFiles(subFolder.getFolderId());
                if (newFiles != null && !newFiles.isEmpty()) {
                	if(usedId == null)
                		baseFiles.addAll(newFiles);
                	else
                		currentFiles.addAll(newFiles);
                }
            }
            for (FolderEntity folder : folders) {
                List<FileEntity> newFiles = DatabaseManager.getInstance().fetchFiles(folder.getFolderId());
                if (newFiles != null && !newFiles.isEmpty()) {
                    if(usedId == null)
                    	baseFiles.addAll(newFiles);
                    else
                    	currentFiles.addAll(newFiles);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void initCache(Integer userId) {
        try {
            if(userId != null)
            {
            	 currentRepos = new ArrayList<>();
                 currentFolders = new ArrayList<>();
                 currentSubFolders = new ArrayList<>();
                 currentFiles = new ArrayList<>();
                 
            	initCacheRepo(userId);          	
            	initCacheFolder(currentRepos, userId);            	
            	initCacheSubFolder(currentFolders, userId);           	
            	initCacheFile(currentSubFolders, currentFolders, userId);
         
            	saveUserTree(userId, currentRepos, currentFolders, currentSubFolders, currentFiles);
            	
            	UserCache u = getUserTree(userId);
            	System.out.println("User " + userId + "được tạo cache:");
            	System.out.println("Repositories: " + u.getRepos());
            	System.out.println("Folders: " +u.getFolders());
            	System.out.println("SubFolders: " + u.getSubFolders());
            	System.out.println("Files: " + u.getFiles());
       	
            }
            else {
            	//Server 
            	initCacheRepo(userId);          	
            	initCacheFolder(baseRepos, userId);            	
            	initCacheSubFolder(baseFolders, userId);           	
            	initCacheFile(baseSubFolders, baseFolders, userId);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void buildTreeFromDatabase(DefaultMutableTreeNode root) throws Exception {
	    try {
	        for (RepositoryEntity repo : baseRepos) {
	            DefaultMutableTreeNode repoNode = new DefaultMutableTreeNode(repo);
	            root.add(repoNode);

	            buildFolderTree(repoNode, repo.getRepoId(), baseFolders, baseSubFolders, baseFiles);	           
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}
    
    
    public void buildTreeForUser(DefaultMutableTreeNode root, int userID)
    {
    	try {
    		UserCache usedCache = getUserTree(userID);
    		for(RepositoryEntity repo : usedCache.getRepos())
    		{
    			DefaultMutableTreeNode repoNode = new DefaultMutableTreeNode(repo.getRepoName());
				root.add(repoNode);
				buildFolderTree(repoNode, repo.getRepoId(), usedCache.getFolders(), usedCache.getSubFolders(), usedCache.getFiles());
				
    		}
    	}catch (Exception e) {
			e.printStackTrace();
		}
    	
    }
    public void buildTreeFromCache(DefaultMutableTreeNode root, UserCache userCache)
    {
    	try {
    		for(RepositoryEntity repo : userCache.getRepos())
    		{
    			DefaultMutableTreeNode repoNode = new DefaultMutableTreeNode(repo.getRepoName());
				root.add(repoNode);
				buildFolderTree(repoNode, repo.getRepoId(), userCache.getFolders(), userCache.getSubFolders(), userCache.getFiles());
				
    		}
    	}catch (Exception e) {
			e.printStackTrace();
		}
    }
   
    private void buildFolderTree(DefaultMutableTreeNode parentNode, int repoId, List<FolderEntity> folders, List<FolderEntity> subFolders, List<FileEntity> files) throws Exception {
	    for (FolderEntity folder : folders) {
	    	if(folder.getRepoId() == repoId)
	    	{
	    		DefaultMutableTreeNode folderNode = new DefaultMutableTreeNode(folder.getFolderName());
	    		parentNode.add(folderNode);
	    		
	    		buildSubFolderTree(folderNode, folder.getFolderId(), subFolders, files);
	    		buildFileTree(folderNode, folder.getFolderId(), files);    		
	    	}
	    }
	}
    
    private void buildSubFolderTree(DefaultMutableTreeNode parentNode, int parentFolderID, List<FolderEntity> subFolders, List<FileEntity> files) throws Exception {
        for (FolderEntity subFolder : subFolders) {
            if (subFolder.getParentFolderId() == parentFolderID) {

                DefaultMutableTreeNode subFolderNode = new DefaultMutableTreeNode(subFolder.getFolderName());
                parentNode.add(subFolderNode);

                buildSubFolderTree(subFolderNode, subFolder.getFolderId(), subFolders, files);
                buildFileTree(subFolderNode, subFolder.getFolderId(), files);
            }
        }
    }

    private void buildFileTree(DefaultMutableTreeNode parentNode, int folderId, List<FileEntity> files) throws Exception {
        for (FileEntity file : files) {
            if (file.getFolderId() == folderId) { 
                DefaultMutableTreeNode fileNode = new DefaultMutableTreeNode(file.getFileName());
                parentNode.add(fileNode);
            }
        }
    }

	public void saveUserTree(int userId, List<RepositoryEntity> repos, 
            List<FolderEntity> folders, 
            List<FolderEntity> subFolders, 
            List<FileEntity> files) {
		UserCache cache = new UserCache(repos, folders, subFolders, files);
	    userCacheHash.put(userId, cache);
	    System.out.println("Đã lưu cache cho userID: " + userId + " với dữ liệu: " + cache);
	    System.out.println("FILE khi lưu: " + files);
    }

    public UserCache getUserTree(int userId) {
    	return userCacheHash.get(userId);
    }

    public boolean isUserTreeCached(int userId) {
    	return userCacheHash.containsKey(userId);
    }
    
//    public void removeCacheForUser(int userId) {
//        resetCurrentEntity();
//        removeUserTree(userId);
//    }
    public void removeUserTree(int userId) {
    	userCacheHash.remove(userId);
    }
 
    
    private void optionFileChange(Object newObject, int usedId, String action)
    {
    	FileEntity newFile = (FileEntity) newObject;   
		UserCache curCache = getUserTree(usedId); 

        switch (action) {
			case "ADDFILE": 
				System.out.println("File được thêm: " + newFile.getFileName() + " " + newFile.getFolderId());
				System.out.println("Tất cả File trước khi thêm: " + curCache.getFiles());
				curCache.addFile(newFile);
				System.out.println("Tất cả File sau khi thêm: " + curCache.getFiles());

				System.out.println("FILE SERVER TRC" + baseFiles);
				baseFiles.add(newFile);
				setSyncServer(true);
				System.out.println("FILE SERVER sau" + baseFiles);				
				break;
			case "DELETEFILE":		
				System.out.println("File bị xóa: " + newFile.getFileName()+ newFile.getFolderId());
				System.out.println("Tất cả File trước khi xóa: " + curCache.getFiles());
				curCache.removeFile(newFile);
				System.out.println("Tất cả File sau khi xóa: " + curCache.getFiles());				
				
				baseFiles.remove(newFile);
				setSyncServer(true);
				break;
			default:
				throw new IllegalArgumentException("Unexpected value: " + action);
		}	
  
        saveUserTree(usedId, curCache.getRepos(), curCache.getFolders(), curCache.getSubFolders(), curCache.getFiles());                        
        System.out.println("Files sau khi thay đổi trong cache: " + getUserTree(usedId).getFiles());
    }
    
    private void optionFolderChange(Object newObject, int usedId, String action)
    {
    	FolderEntity newFolder = (FolderEntity) newObject;
		UserCache curCache = getUserTree(usedId);

		System.out.println("Cha FOLDER" + newFolder.getParentFolderId());
		switch (action) {
			case "ADDFOLDER": 
				if(newFolder.getParentFolderId() == null)
	    		{
					System.out.println("Folder được thêm: " + newFolder.getFolderId() + " " + newFolder.getFolderName() + newFolder.getRepoId() + " " + newFolder.getParentFolderId());
					System.out.println("Tất cả Folder trước khi thêm: " + curCache.getFolders());
					curCache.addFolder(newFolder);
	    			System.out.println("Tất cả Folder sau khi thêm: " + curCache.getFolders());
	    		
	    			baseFolders.add(newFolder);
	    			setSyncServer(true);
	    		}
	    		else
	    		{    
	    			System.out.println("Subfolder được thêm: " + newFolder.getFolderId() + " " + newFolder.getFolderName() + " Repo: " + newFolder.getRepoId() + " Parent: " + newFolder.getParentFolderId());
					System.out.println("Tất cả Subfolder trước khi thêm: " + curCache.getSubFolders());
	    			curCache.addSubFolder(newFolder);
	    			System.out.println("Tất cả Subfolder sau khi thêm: " + curCache.getSubFolders());
	    			
	    			baseSubFolders.add(newFolder);
	    			setSyncServer(true);
	    		}
				break;
			case "DELETEFOLDER":	
				if(newFolder.getParentFolderId() == null)
	    		{
					System.out.println("Folder bị xóa: " + newFolder.getFolderId() + " " + newFolder.getFolderName() + " Repo: " + newFolder.getRepoId() + " Parent: " + newFolder.getParentFolderId());
					System.out.println("Tất cả Folder trước khi xóa: " + curCache.getFolders());
					curCache.removeFolder(newFolder);
					System.out.println("Tất cả Folder sau khi xóa: " + curCache.getFolders());

					baseFolders.remove(newFolder);
					setSyncServer(true);
	    		}
	    		else
	    		{    
	    			System.out.println("Subfolder bị xóa: " + newFolder.getFolderId() + " " + newFolder.getFolderName() + " Repo: " + newFolder.getRepoId() + " Parent: " + newFolder.getParentFolderId());
					System.out.println("Tất cả Subfolder trước khi xóa: " + curCache.getSubFolders());
	    			curCache.removeSubFolder(newFolder);
	    			System.out.println("Tất cả Subfolder sau khi xóa: " + curCache.getSubFolders());

	    			baseSubFolders.remove(newFolder);
	    			setSyncServer(true);
	    		}
				break;
			default:
				throw new IllegalArgumentException("Unexpected value: " + action);
		}
		
		saveUserTree(usedId, curCache.getRepos(), curCache.getFolders(), curCache.getSubFolders(), curCache.getFiles());
		
		System.out.println("Folder sau khi thay đổi trong cache: " + getUserTree(usedId).getFolders());
		System.out.println("SubFolder sau khi thay đổi trong cache: " + getUserTree(usedId).getSubFolders());
		System.out.println("File sau khi thay đổi trong cache: " + getUserTree(usedId).getFiles());
    }
    
    private void optionRepoChange(Object newObject, int usedId, String action)
    {
    	RepositoryEntity newRepo = (RepositoryEntity) newObject;   
		UserCache curCache = getUserTree(usedId); 
		
        switch (action) {
			case "ADDREPO": 
				System.out.println("Repo được thêm: " + newRepo.getRepoName() + " " + newRepo.getRepoId());
				System.out.println("Tất cả Repo trước khi thêm: " + curCache.getRepos());
				curCache.addRepo(newRepo);
				System.out.println("Tất cả Repo sau khi thêm: " + curCache.getRepos());

				baseRepos.add(newRepo);
				setSyncServer(true);
				break;
			case "DELETEREPO":		
				System.out.println("Repo bị xóa: " + newRepo.getRepoName() + " " + newRepo.getRepoId());
				System.out.println("Tất cả Repo trước khi xóa: " + curCache.getRepos());
				curCache.removeRepo(newRepo);
				System.out.println("Tất cả Repo sau khi xóa: " + curCache.getRepos());				
 
				baseRepos.remove(newRepo);
				setSyncServer(true);
				break;
			default:
				throw new IllegalArgumentException("Unexpected value: " + action);
		}
        
        saveUserTree(usedId, curCache.getRepos(), curCache.getFolders(), curCache.getSubFolders(), curCache.getFiles());                          
        System.out.println("Repo sau khi thay đổi trong cache: " + getUserTree(usedId).getRepos());
    }
    
    public void changeCache(Object newObject, int usedId, String action)
    {
    	if(newObject instanceof FileEntity)
    	{
    		optionFileChange(newObject, usedId, action);
    	}
    	else if(newObject instanceof FolderEntity)
    	{
    		optionFolderChange(newObject, usedId, action);
    	}
    	else if(newObject instanceof RepositoryEntity)
    	{
    		optionRepoChange(newObject, usedId, action);
    	}
    }
    // share
    
    public void syncDataSharedForCache(int _userIdAccept) throws SQLException, Exception
    {	
    	UserCache userCache = getUserTree(_userIdAccept);
    	String myUserName = DatabaseManager.getInstance().getUserNameByID(_userIdAccept);
    	if(userCache == null)	
    		return;
    	RepositoryEntity repoShared = FileSyncCache.getInstance().getLastRepository(_userIdAccept);
    	DatabaseManager.getInstance().addShareRepoToDatabase(repoShared, myUserName);
    	userCache.addRepo(repoShared);
//    	userCache.addListFolder(FileSyncCache.getInstance().getFolders(repoShared.getRepoId()));
//    	userCache.addListSubFolder(FileSyncCache.getInstance().getSubFolders(repoShared.getRepoId()));
//    	userCache.addListFile(FileSyncCache.getInstance().getFiles(repoShared.getRepoId()));  
		userCache.addListFolder(FileSyncCache.getInstance().getFoldersByState(repoShared.getRepoId(), "NORMAL", _userIdAccept));
		userCache.addListSubFolder(FileSyncCache.getInstance().getSubFoldersByState(repoShared.getRepoId(), "NORMAL", _userIdAccept));			
		userCache.addListFile(FileSyncCache.getInstance().getFilesByState(repoShared.getRepoId(), "NORMAL", _userIdAccept));
    	
    	saveUserTree(_userIdAccept, userCache.getRepos(), userCache.getFolders(), userCache.getSubFolders(), userCache.getFiles());
    //	FileSyncCache.getInstance().removeRepoData(repoShared.getRepoId());
    	//FileSyncCache.getInstance().removeRepository(_userIdAccept, repoShared);
    	
    }
    public void syncAllDataDelay(int _userId) throws SQLException, Exception
    {
    	UserCache userCache = getUserTree(_userId);
    	String myUserName = DatabaseManager.getInstance().getUserNameByID(_userId);
    	List<RepositoryEntity> listRepoDataOwner = FileSyncCache.getInstance().getRepositoriesByOwnerId(_userId);
    	List<RepositoryEntity> listRepoData = FileSyncCache.getInstance().getAllRepositoriesByID(_userId);    		
    	if(listRepoDataOwner.isEmpty())
    	{
    		syncForCache(listRepoData, myUserName, userCache, _userId);
    	}
    	else
    	{
    		syncForCache(listRepoDataOwner, myUserName, userCache, _userId);
    		syncForCache(listRepoData, myUserName, userCache, _userId);
    	}
    	saveUserTree(_userId, userCache.getRepos(), userCache.getFolders(), userCache.getSubFolders(), userCache.getFiles());   	
    }
    private void syncForCache(List<RepositoryEntity> listRepo, String _userName, UserCache userCache, int _userId) throws SQLException
    {
    	for(RepositoryEntity repo : listRepo)
    	{	
    		if(!userCache.getRepos().contains(repo))
    		{
    			userCache.addRepo(repo);            
    			DatabaseManager.getInstance().addShareRepoToDatabase(repo, _userName);
    			
//    			userCache.addListFolder(FileSyncCache.getInstance().getFolders(repo.getRepoId()));
//    			userCache.addListSubFolder(FileSyncCache.getInstance().getSubFolders(repo.getRepoId()));
//    			userCache.addListFile(FileSyncCache.getInstance().getFiles(repo.getRepoId()));
    			userCache.addListFolder(FileSyncCache.getInstance().getFoldersByState(repo.getRepoId(), "NORMAL", _userId));
    			userCache.addListSubFolder(FileSyncCache.getInstance().getSubFoldersByState(repo.getRepoId(), "NORMAL", _userId));			
    			userCache.addListFile(FileSyncCache.getInstance().getFilesByState(repo.getRepoId(), "NORMAL", _userId));
    			
    			
    			userCache.removeListFolder(FileSyncCache.getInstance().getFoldersByState(repo.getRepoId(), "DELETE", _userId));
    			userCache.addListFolder(FileSyncCache.getInstance().getFoldersByState(repo.getRepoId(), "ADD", _userId));
    			
    			userCache.removeListSubFolder(FileSyncCache.getInstance().getSubFoldersByState(repo.getRepoId(), "DELETE", _userId));
    			userCache.addListSubFolder(FileSyncCache.getInstance().getSubFoldersByState(repo.getRepoId(), "ADD", _userId));
    			
    			userCache.removeListFile(FileSyncCache.getInstance().getFilesByState(repo.getRepoId(), "DELETE", _userId));
    			userCache.addListFile(FileSyncCache.getInstance().getFilesByState(repo.getRepoId(), "ADD", _userId));
    			
    		}
    		else {
    			userCache.removeListFolder(FileSyncCache.getInstance().getFoldersByState(repo.getRepoId(), "DELETE", _userId));
    			userCache.addListFolder(FileSyncCache.getInstance().getFoldersByState(repo.getRepoId(), "ADD", _userId));
    			
    			userCache.removeListSubFolder(FileSyncCache.getInstance().getSubFoldersByState(repo.getRepoId(), "DELETE", _userId));
    			userCache.addListSubFolder(FileSyncCache.getInstance().getSubFoldersByState(repo.getRepoId(), "ADD", _userId));
    			
    			userCache.removeListFile(FileSyncCache.getInstance().getFilesByState(repo.getRepoId(), "DELETE", _userId));
    			userCache.addListFile(FileSyncCache.getInstance().getFilesByState(repo.getRepoId(), "ADD", _userId));
    		}
			//FileSyncCache.getInstance().removeRepoData(repo.getRepoId());
    	}
    }
    
    public void modifyCache(Integer userId, RepositoryEntity repo, boolean isSharing) throws SQLException, Exception {

    	UserCache userCache = getUserTree(userId);
    	if(userCache == null)	
    		return;
    	if (isSharing) 
        {       	
            userCache.addRepo(repo);
            userCache.addListFolder(getListForderShare(repo.getRepoId()));
            userCache.addListSubFolder(getListSubFolderShare(getListForderShare(repo.getRepoId())));
            userCache.addListFile(getListFileShare(getListForderShare(repo.getRepoId()), getListSubFolderShare(getListForderShare(repo.getRepoId()))));           
        } 
        else 
        {
            userCache.removeRepo(repo);
            userCache.removeListFolder(getListForderShare(repo.getRepoId()));
            userCache.removeListSubFolder(getListSubFolderShare(getListForderShare(repo.getRepoId())));
            userCache.removeListFile(getListFileShare(getListForderShare(repo.getRepoId()), getListSubFolderShare(getListForderShare(repo.getRepoId()))));
        }
    	saveUserTree(userId, userCache.getRepos(), userCache.getFolders(), userCache.getSubFolders(), userCache.getFiles());

    }

    public void shareCache(Integer sharedUserId, RepositoryEntity newRepo) throws SQLException, Exception {
        modifyCache(sharedUserId, newRepo, true);
    }

    public void unshareCache(Integer unshareUserId, RepositoryEntity repo) throws SQLException, Exception {
        modifyCache(unshareUserId, repo, false);
    }
    
    
    public List<FolderEntity> getListForderShare(int _sharedSRepoId) throws SQLException, Exception
    {
    	return DatabaseManager.getInstance().fetchFolders(_sharedSRepoId);   	
    }
    
    public List<FolderEntity> getListSubFolderShare(List<FolderEntity> _sharedFolders) throws SQLException, Exception {
        if (_sharedFolders == null || _sharedFolders.isEmpty()) {
            return new ArrayList<>(); 
        }

        List<FolderEntity> sharedSubFolders = new ArrayList<FolderEntity>(); 

        for (FolderEntity folder : _sharedFolders) {
            List<FolderEntity> subFolders = DatabaseManager.getInstance().fetchSubFolders(folder.getFolderId());
            if (subFolders != null && !subFolders.isEmpty()) {
                sharedSubFolders.addAll(subFolders); 
            }
        }

        if (sharedSubFolders.isEmpty()) {
            return sharedSubFolders; 
        }

        List<FolderEntity> deeperSubFolders = getListSubFolderShare(sharedSubFolders);
        sharedSubFolders.addAll(deeperSubFolders);

        return sharedSubFolders;
    }

    public List<FileEntity> getListFileShare(List<FolderEntity> _sharedFolder, List<FolderEntity> _sharedSubFolder) throws SQLException, Exception
    {
    	List<FileEntity> sharedListFile = new ArrayList<FileEntity>();
    	for (FolderEntity subFolder : _sharedSubFolder) {
            List<FileEntity> newFiles = DatabaseManager.getInstance().fetchFiles(subFolder.getFolderId());
            if (newFiles != null && !newFiles.isEmpty()) {
            	sharedListFile.addAll(newFiles);
            }
        }
        for (FolderEntity folder : _sharedFolder) {
            List<FileEntity> newFiles = DatabaseManager.getInstance().fetchFiles(folder.getFolderId());
            if (newFiles != null && !newFiles.isEmpty()) {
            	sharedListFile.addAll(newFiles);
            }
        }
        return sharedListFile;
    }
    //end share
}
