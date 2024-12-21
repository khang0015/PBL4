package Model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import Model.Tree.FileEntity;
import Model.Tree.FolderEntity;
import Model.Tree.RepositoryEntity;

public class FileSyncCache {
	private static final HashMap<Integer, List<RepositoryEntity>> dataCanSyncHash = new HashMap<Integer, List<RepositoryEntity>>();
	private static final HashMap<Integer, List<FolderEntity>> foldersOfRepo = new HashMap<Integer, List<FolderEntity>>();
	private static final HashMap<Integer, List<FolderEntity>>subFoldersOfRepo = new HashMap<Integer, List<FolderEntity>>();
	private static final HashMap<Integer, List<FileEntity>> filesOfRepo = new HashMap<Integer, List<FileEntity>>();
	
	private static volatile FileSyncCache instance; // Double-Checked Locking

    public static FileSyncCache getInstance() {
        if (instance == null) {
            synchronized (CacheManager.class) {
                if (instance == null) { 
                    instance = new FileSyncCache();
                }
            }
        }
        return instance;
    }
    public void addDataToSyncCache(Integer _userId, RepositoryEntity _repo, List<FolderEntity> _folders, 
    		List<FolderEntity> _subFolders, List<FileEntity> _files) 
    {
    	List<RepositoryEntity> repoList = dataCanSyncHash.getOrDefault(_userId, new ArrayList<>());
    	for (List<RepositoryEntity> allRepo : dataCanSyncHash.values()) 
    	{
			if (allRepo.contains(_repo)) {
				repoList.add(_repo);
				dataCanSyncHash.put(_userId, repoList);
				return;
			}
    	}
    	
		if (!repoList.contains(_repo)) {
			repoList.add(_repo);  
			
			dataCanSyncHash.put(_userId, repoList);		
			foldersOfRepo.put(_repo.getRepoId(), _folders);  
			subFoldersOfRepo.put(_repo.getRepoId(), _subFolders); 
			filesOfRepo.put(_repo.getRepoId(), _files);  
		}
	}

    public RepositoryEntity getLastRepository(Integer _userId) {
        List<RepositoryEntity> currentRepo = dataCanSyncHash.get(_userId);       
        if (currentRepo != null && !currentRepo.isEmpty()) {
            return currentRepo.get(currentRepo.size() - 1);
        }
        return null;  
    }
    public List<FolderEntity> getFolders(Integer repoId) {
        return foldersOfRepo.getOrDefault(repoId, new ArrayList<>());
    }

    public List<FolderEntity> getSubFolders(Integer repoId) {
        return subFoldersOfRepo.getOrDefault(repoId, new ArrayList<>());
    }

    public List<FileEntity> getFiles(Integer repoId) {
        return filesOfRepo.getOrDefault(repoId, new ArrayList<>());
    }
    public List<FileEntity> getFilesByState(Integer repoId, String state, int userId)
    {
    	 List<FileEntity> fileList = filesOfRepo.getOrDefault(repoId, new ArrayList<>());
    	 List<FileEntity> result = new ArrayList<FileEntity>();
    	 for (FileEntity file : fileList) {
    	        if (file.getState() != null && file.getState().equals(state) && (file.getIdUserDoer() != userId)) {
    	             if (!file.getUserSync().contains(userId)) {
    	            	 result.add(file);
    	            	 file.getUserSync().add(userId); 
    	             }
    	        }
    	    }
         return result;         
    }
    public List<FolderEntity> getFoldersByState(Integer repoId, String state, int userId)
    {
    	 List<FolderEntity> foldersList = foldersOfRepo.getOrDefault(repoId, new ArrayList<>());
    	 List<FolderEntity> result = new ArrayList<FolderEntity>();
         for(FolderEntity folder : foldersList)
         {
        	 if(folder.getState() != null && folder.getState().equals(state) && (folder.getIdUserDoer() != userId))
        	 {
        		 if(!folder.getUserSync().contains(userId))
        		 result.add(folder);
        		 folder.getUserSync().add(userId);
        	 }
         }
         return result;         
    }
    public List<FolderEntity> getSubFoldersByState(Integer repoId, String state, int userId)
    {
    	 List<FolderEntity> subFoldersList = subFoldersOfRepo.getOrDefault(repoId, new ArrayList<>());
    	 List<FolderEntity> result = new ArrayList<FolderEntity>();
         for(FolderEntity folder : subFoldersList)
         {
        	 if(folder.getState() != null && folder.getState().equals(state) && (folder.getIdUserDoer() != userId))
        	 {
        		 if(!folder.getUserSync().contains(userId))
        		 result.add(folder);
        		 folder.getUserSync().add(userId);
        	 }
         }
         return result;         
    }
    public List<RepositoryEntity> getRepositoriesByOwnerId(Integer userIdOwner) {
        List<RepositoryEntity> result = new ArrayList<>();
        
        for (List<RepositoryEntity> repoList : dataCanSyncHash.values()) {
            for (RepositoryEntity repo : repoList) {
                if (repo.getOwnerId() == userIdOwner) {
                    result.add(repo);
                }
            }
        }
        
        return result; 
    }


    // Thêm
//    public void addRepository(Integer _userId, RepositoryEntity _repo) {
//        List<RepositoryEntity> repoList = dataCanSyncHash.getOrDefault(_userId, new ArrayList<>());
//        repoList.add(_repo);
//        dataCanSyncHash.put(_userId, repoList);
//    }


    public void addFolder(Integer repoId, FolderEntity folder) {
        List<FolderEntity> folderList = foldersOfRepo.getOrDefault(repoId, new ArrayList<>());
        folderList.add(folder);
        foldersOfRepo.put(repoId, folderList);
    }

    public void addSubFolder(Integer repoId, FolderEntity subFolder) {
        List<FolderEntity> subFolderList = subFoldersOfRepo.getOrDefault(repoId, new ArrayList<>());
        subFolderList.add(subFolder);
        subFoldersOfRepo.put(repoId, subFolderList);
    }

    public void addFile(Integer repoId, FileEntity file) {
        List<FileEntity> fileList = filesOfRepo.getOrDefault(repoId, new ArrayList<>());
        fileList.add(file);     
        filesOfRepo.put(repoId, fileList);
    }
    
    
    
    public void clearCache(Integer _userId) {
        dataCanSyncHash.remove(_userId);
    }

    public boolean containsUser(Integer _userId) {
        return dataCanSyncHash.containsKey(_userId);
    }

    public List<RepositoryEntity> getAllRepositoriesByID(Integer _userId) {
    	return dataCanSyncHash.getOrDefault(_userId, new ArrayList<>());
    }
    public RepositoryEntity removeLastRepository(Integer _userId) {
        List<RepositoryEntity> currentRepo = dataCanSyncHash.get(_userId);
        if (currentRepo != null && !currentRepo.isEmpty()) {
            return currentRepo.remove(currentRepo.size() - 1); 
        }
        return null;
    }

    public boolean removeRepository(Integer _userId, RepositoryEntity _repo) {
        List<RepositoryEntity> currentRepo = dataCanSyncHash.get(_userId);
		if (currentRepo != null && currentRepo.contains(_repo)) {
			currentRepo.remove(_repo);
			if (currentRepo.isEmpty()) {
				dataCanSyncHash.remove(_userId);
			} else {
				dataCanSyncHash.put(_userId, currentRepo);
			}
			return true; 			
		}
        return false; 
    }
    public void removeRepoData(Integer _repoId)
    {
    	foldersOfRepo.remove(_repoId);
    	subFoldersOfRepo.remove(_repoId);
    	filesOfRepo.remove(_repoId);
    }
    public void removeRepoDataByOwnerId(Integer _repoId, Integer _ownerId) {
//        List<FolderEntity> folders = foldersOfRepo.get(_repoId);
//        if (folders != null) {
//            folders.removeIf(folder -> !folder.getOwnerId() == _ownerId);
//        }

//        List<FolderEntity> subFolders = subFoldersOfRepo.get(_repoId);
//        if (subFolders != null) {
//            subFolders.removeIf(subFolder -> !subFolder.getOwnerId() == _ownerId);
//        }
//
        List<FileEntity> files = filesOfRepo.get(_repoId);
        if (files != null) {
            files.removeIf(file -> !(file.getIdUserDoer() == _ownerId));
        }
    }

    public void removeAllRepositoriesByID(Integer _userId) {
        if (_userId != null && containsUser(_userId)) {
            dataCanSyncHash.remove(_userId); 
        }
    }

    public boolean containsRepository(Integer _userId, RepositoryEntity _repo) {
        if (_userId == null || _repo == null) {
            return false; 
        }
        List<RepositoryEntity> repoList = dataCanSyncHash.get(_userId);
        return repoList != null && repoList.contains(_repo);
    }

    public void updateEntityForAllUsers(RepositoryEntity repo, Object entity) {
        for (List<RepositoryEntity> repoList : dataCanSyncHash.values()) {
            for (int i = 0; i < repoList.size(); i++) {
                RepositoryEntity currentRepo = repoList.get(i);

                if (currentRepo.equals(repo)) {
                    if (entity instanceof FileEntity) {
                        FileEntity file = (FileEntity) entity;
                        if (containsFile(currentRepo.getRepoId(), file) && "DELETE".equals(file.getState())) {
                            removeFile(currentRepo.getRepoId(), file);
                        } else if (!containsFile(currentRepo.getRepoId(), file)) {
                            addFile(currentRepo.getRepoId(), file);
                        }
                    } else if (entity instanceof FolderEntity) {
                        FolderEntity folder = (FolderEntity) entity;
                        if (containsFolder(currentRepo.getRepoId(), folder) && "DELETE".equals(folder.getState())) {
                            removeFolder(currentRepo.getRepoId(), folder);
                        } 
                        else if(containsSubFolder(currentRepo.getRepoId(), folder) && "DELETE".equals(folder.getState()))
                        {
                        	removeSubFolder(currentRepo.getRepoId(), folder);
                        }
                        else if (!containsFolder(currentRepo.getRepoId(), folder) && !containsSubFolder(currentRepo.getRepoId(), folder)){
                        	if(folder.getParentFolderId() == null)
                        		addFolder(currentRepo.getRepoId(), folder);
                        	else
                        		addSubFolder(currentRepo.getRepoId(), folder);
                        }
                    }
                }
            }
        }
    }
    
    // Xóa
    public void removeFolder(Integer repoId, FolderEntity folder) {
        List<FolderEntity> folderList = foldersOfRepo.get(repoId);
        if (folderList != null) {
            folderList.remove(folder);
            if (folderList.isEmpty()) {
                foldersOfRepo.remove(repoId);
            } else {
                foldersOfRepo.put(repoId, folderList);
            }
        }
    }
    
    public void removeSubFolder(Integer repoId, FolderEntity subFolder) {
        List<FolderEntity> subFolderList = subFoldersOfRepo.get(repoId);
        if (subFolderList != null) {
            subFolderList.remove(subFolder);
            if (subFolderList.isEmpty()) {
                subFoldersOfRepo.remove(repoId);
            } else {
                subFoldersOfRepo.put(repoId, subFolderList);
            }
        }
    }
    public void removeFile(Integer repoId, FileEntity file) {
        List<FileEntity> fileList = filesOfRepo.get(repoId);
        if (fileList != null) {
            fileList.remove(file);
            if (fileList.isEmpty()) {
                filesOfRepo.remove(repoId);
            } else {
                filesOfRepo.put(repoId, fileList);
            }
        }
    }

    // kiểm tra tồn tại
    public boolean containsFolder(Integer repoId, FolderEntity folder) {
        List<FolderEntity> folderList = foldersOfRepo.get(repoId);
        return folderList != null && folderList.contains(folder);
    }

    public boolean containsSubFolder(Integer repoId, FolderEntity subFolder) {
        List<FolderEntity> subFolderList = subFoldersOfRepo.get(repoId);
        return subFolderList != null && subFolderList.contains(subFolder);
    }

    public boolean containsFile(Integer repoId, FileEntity file) {
        List<FileEntity> fileList = filesOfRepo.get(repoId);
        return fileList != null && fileList.contains(file);
    }

}
