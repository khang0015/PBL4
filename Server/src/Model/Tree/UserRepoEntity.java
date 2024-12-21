package Model.Tree;

public class UserRepoEntity {
    private int userID;
    private String userName;
    private String userPassword;
    private int repoID;
    private String repoName;

    // Constructor
    public UserRepoEntity(int userID, String userName, String userPassword, int repoID, String repoName) {
        this.userID = userID;
        this.userName = userName;
        this.userPassword = userPassword;
        this.repoID = repoID;
        this.repoName = repoName;
    }

    // Getter and Setter methods
    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserPassword() {
        return userPassword;
    }

    public void setUserPassword(String userPassword) {
        this.userPassword = userPassword;
    }

    public int getRepoID() {
        return repoID;
    }

    public void setRepoID(int repoID) {
        this.repoID = repoID;
    }
    public String getRepoName() {
    	return repoName;
    }
    public void setRepoName(String repoName) {
    	this.repoName = repoName;
    }
}
