package Model;

import java.util.List;

import Model.Tree.FileEntity;
import Model.Tree.FolderEntity;
import Model.Tree.RepositoryEntity;

public class UserCache {
    private List<RepositoryEntity> repos;
    private List<FolderEntity> folders;
    private List<FolderEntity> subFolders;
    private List<FileEntity> files;

    public UserCache(List<RepositoryEntity> repos, List<FolderEntity> folders, 
                     List<FolderEntity> subFolders, List<FileEntity> files) {
        this.repos = repos;
        this.folders = folders;
        this.subFolders = subFolders;
        this.files = files;
    }

    public List<RepositoryEntity> getRepos() {
        return repos;
    }
    public void setRepos(List<RepositoryEntity> _newRepos)
    {
    	this.repos = _newRepos;
    }
    public void addRepo(RepositoryEntity _newRepo)
    {
    	if(repos != null)
    		repos.add(_newRepo);
    	else
    		System.out.println("Danh sách Repo bị rỗng");
    }
    public void addListRepo(List<RepositoryEntity> _newListRepo)
    {
    	for(RepositoryEntity repo : _newListRepo)
    	{
    		repos.add(repo);
    	}
    }
    public void removeRepo(RepositoryEntity _delRepo)
    {
    	if (repos.isEmpty()) {
            System.out.println("Danh sách Repo rỗng.");
        } else if (!repos.contains(_delRepo)) {
            System.out.println("Repo không tồn tại trong danh sách.");
        } else {
            repos.remove(_delRepo);
            System.out.println("Repo đã bị xóa trong cache");
        }
    }
    
    
    
    public List<FolderEntity> getFolders() {
        return folders;
    }
    public void settFolders(List<FolderEntity> _newFolders) 
    {
        this.folders = _newFolders;
    }
    public void addFolder(FolderEntity _newFolder)
    {
    	if(folders != null)
    		folders.add(_newFolder);
    	else
    		System.out.println("Danh sách Folder bị rỗng");
    }
    public void addListFolder(List<FolderEntity> _newListFolder)
    {
    	for(FolderEntity folder : _newListFolder)
    	{
    		folders.add(folder);
    	}
    }
    public void removeListFolder(List<FolderEntity> _listFolder)
    {
    	for(FolderEntity folder : _listFolder)
    	{
    		folders.remove(folder);
    	}
    	System.out.println("Đã xóa Folder trong repo");
    }
    public void removeFolder(FolderEntity _delFolder)
    {
    	if (folders.isEmpty()) {
            System.out.println("Danh sách Folder rỗng.");
        } else if (!folders.contains(_delFolder)) {
            System.out.println("Folder không tồn tại trong danh sách.");
        } else {
        	System.out.println("Folder đã bị xóa trong cache");
            folders.remove(_delFolder);
        }
    }
    
    
    
    
    public List<FolderEntity> getSubFolders() {
        return subFolders;
    }
    public void setSubFolders(List<FolderEntity> _newSubFolders) {
        this.subFolders = _newSubFolders;
    }
    public void addSubFolder(FolderEntity _newSubFolder)
    {
    	if(subFolders != null)
    		subFolders.add(_newSubFolder);
    	else
    		System.out.println("Danh sách SubFolder bị rỗng");
    }
    public void addListSubFolder(List<FolderEntity> _newListSubFolder)
    {
    	for(FolderEntity subFolder : _newListSubFolder)
    	{
    		subFolders.add(subFolder);
    	}
    }
    public void removeListSubFolder(List<FolderEntity> _listSubFolder)
    {
    	for(FolderEntity subFolder : _listSubFolder)
    	{
    		subFolders.remove(subFolder);
    	}
    }
    public void removeSubFolder(FolderEntity _delSubFolder)
    {
    	if (subFolders.isEmpty()) {
            System.out.println("Danh sách subFolder rỗng.");
        } else if (!subFolders.contains(_delSubFolder)) {
            System.out.println("SubFolder không tồn tại trong danh sách.");
        } else {
        	System.out.println("SubFolder đã bị xóa trong cache");
            subFolders.remove(_delSubFolder);
        }
    }
    
    
    
    public List<FileEntity> getFiles() {
        return files;
    }
    public void setFiles(List<FileEntity> _newFiles) {
        this.files = _newFiles;
    }
    public void addFile(FileEntity _newfile) {
        if(files != null)
        	files.add(_newfile);   
        else
    		System.out.println("Danh sách File bị rỗng");
    }
    public void addListFile(List<FileEntity> _newListFile)
    {
    	for(FileEntity file : _newListFile)
    	{
    		files.add(file);
    	}
    }
    public void removeListFile(List<FileEntity> _listFile)
    {
    	if(_listFile == null)
    		return;
    	for(FileEntity file : _listFile)
    	{
    		files.remove(file);
    	}
    }
    public void removeFile(FileEntity _delFile) {
        if (files.isEmpty()) {
            System.out.println("Danh sách file rỗng.");
        } else if (!files.contains(_delFile)) {
            System.out.println("File không tồn tại trong danh sách.");
        } else {
        	System.out.println("File đã bị xóa trong cache");
            files.remove(_delFile);
        }
    }
}
