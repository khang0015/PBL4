package Model.Tree;

import java.util.List;

public class RepositoryEntity {
    private int repoId;
    private String repoName;
    private String repoPath;
    private int ownerId;
    public RepositoryEntity(int _repoId, String _repoName) {
        this.repoId = _repoId;
        this.repoName = _repoName;
    }
    public RepositoryEntity(String _repoName, String _repoPath)
    {
    	this.repoName = _repoName;
    	this.repoPath = _repoPath;
    }
    public int getRepoId() {
        return repoId;
    }
    public String getRepoName() {
        return repoName;
    }
    public String getPath()
    {
    	return repoPath;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true; // Kiểm tra tham chiếu cùng một đối tượng
        if (obj == null || getClass() != obj.getClass()) return false; // Kiểm tra null và loại đối tượng

        RepositoryEntity other = (RepositoryEntity) obj; // Ép kiểu về RepositoryEntity

        // So sánh repoId và repoName
        return repoId == other.repoId &&
               (repoName != null ? repoName.equals(other.repoName) : other.repoName == null);
    }

    @Override
    public int hashCode() {
        int result = Integer.hashCode(repoId); // Băm repoId
        result = 31 * result + (repoName != null ? repoName.hashCode() : 0); // Băm repoName
        return result;
    }
    @Override
    public String toString() {
        return repoName != null ? repoName : "Unnamed Repository";
    }
	public int getOwnerId() {
		return ownerId;
	}
	public void setOwnerId(int ownerId) {
		this.ownerId = ownerId;
	}

}
