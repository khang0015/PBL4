package Model.Tree;

import java.util.ArrayList;
import java.util.List;

public class FileEntity {
    private int fileId;
    private String fileName;
    private int folderId;
    private String state = "NORMAL";
    private int idUserDoer = -1;
    private List<Integer> userSync = new ArrayList<Integer>();
    
    public FileEntity(String _fileName, int _folderId) {
        this.fileName = _fileName;
        this.folderId = _folderId;
    }
//    public FileEntity(int _fileId, String _fileName, int _folderId)
//    {
//    	this.fileId = _fileId;
//    	this.fileName = _fileName;
//    	this.folderId = _folderId;
//    }
    public int getFileId() {
        return fileId;
    }

    public String getFileName() {
        return fileName;
    }
    public int getFolderId()
    {
    	return folderId;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true; // Cùng tham chiếu
        if (obj == null || getClass() != obj.getClass()) return false; // null hoặc khác kiểu

        FileEntity that = (FileEntity) obj;

        // So sánh dựa trên fileName và folderId
        return folderId == that.folderId && fileName.equals(that.fileName);
    }

    // Override hashCode để phù hợp với equals
    @Override
    public int hashCode() {
        int result = Integer.hashCode(folderId); // Tính hash từ folderId
        result = 31 * result + fileName.hashCode(); // Kết hợp hash của fileName
        return result;
    }
    @Override
    public String toString() {
        return fileName != null ? fileName : "Unnamed File";
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
