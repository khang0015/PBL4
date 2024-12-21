package Model.Tree;

import java.util.ArrayList;
import java.util.List;

public class FolderEntity {
    private int folderId;
    private String folderName;
    private int repoID;
    private Integer parentFolderId;
    private String state = "NORMAL";
    private int idUserDoer = -1;
    private List<Integer> userSync = new ArrayList<Integer>();
    
    public FolderEntity(int _folderId, String _folderName, int _repoId, Integer _parentFolderId) {
        this.folderId = _folderId;
        this.folderName = _folderName;
        this.repoID = _repoId;
        this.parentFolderId = _parentFolderId;
    }
    
//    public FolderEntity(int _folderId, String _folderName, Integer _parentFolderId) {
//        this.folderId = _folderId;
//        this.folderName = _folderName;
//        this.parentFolderId = _parentFolderId;
//    }
    
    public FolderEntity(String _folderName, int _repoID, Integer _parentFolderId)
    {
        this.folderName = _folderName;
        this.repoID = _repoID;
        this.parentFolderId = _parentFolderId;
    }
    public int getFolderId() {
        return folderId;
    }
    
    public String getFolderName() {
        return folderName;
    }

    public int getRepoId()
    {
    	return repoID;
    }
    public Integer getParentFolderId() {
        return parentFolderId;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true; // Nếu cùng tham chiếu, chắc chắn bằng nhau
        if (obj == null || getClass() != obj.getClass()) return false; // Null hoặc khác kiểu không bằng nhau

        FolderEntity that = (FolderEntity) obj;

        // So sánh từng thuộc tính
        return folderId == that.folderId &&
               repoID == that.repoID &&
               (parentFolderId == null ? that.parentFolderId == null : parentFolderId.equals(that.parentFolderId)) &&
               folderName.equals(that.folderName);
    }


    // Override hashCode để phù hợp với equals
    @Override
    public int hashCode() {
        int result = Integer.hashCode(folderId); // Hash từ folderId
        result = 31 * result + Integer.hashCode(repoID); // Kết hợp với repoID
        result = 31 * result + (parentFolderId == null ? 0 : parentFolderId.hashCode()); // Xử lý parentFolderId null
        result = 31 * result + folderName.hashCode(); // Kết hợp với folderName
        return result;
    }
    @Override
    public String toString() {
        return folderName != null ? folderName : "Unnamed Folder";
    }

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public int getIdUserDoer() {
		return idUserDoer;
	}
	
	public void setIdUserDoer(int idUserDoer) {
		this.idUserDoer = idUserDoer;
	}   
	public List<Integer> getUserSync() {
		return userSync;
	}
	public void addUserSync(Integer userId) {
	    if (!userSync.contains(userId)) {
	        userSync.add(userId); 
	    }
	}



}
