package Controller;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import Model.FileClientModel;
import View.FileClientView;
import View.LoginView;
import View.ShareDialog;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class FileClientController {

    private FileClientModel model;
    private FileClientView view;
    private TreePath path;
    private LoginView loginView;
    
    private String userName;
    private String passWord;
    private String ipServer;
    private int portServer;
    public FileClientController(FileClientView view, LoginView loginView) throws IOException {
//        model = new FileClientModel();
        this.loginView = loginView;
        this.view = view;
        initController();
        System.out.println("cb nhan cay");
       // getTree();
        loginView.setVisible(true);
        view.setVisible(false);
    }

    private void initController() {
    	loginView.getLoginButton().addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				userName = loginView.usernameField.getText();
				char[] passWordArray = loginView.passwordField.getPassword();
				passWord = new String(passWordArray);
				
				ipServer = loginView.getIpField();
				portServer = Integer.parseInt(loginView.getPortField());
				try {
					model = new FileClientModel();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				model.setServerAddress(ipServer);
				model.setServerPort(portServer);
				model.initSocket();
				System.out.println("TK" + userName);
				System.out.println("MK" + passWord);
				getTree(userName, passWord);
				model.initThreadNotif();
			}
		});
        view.getReloadButton().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                reload();
            }
        });

        view.getUploadButton().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                uploadFile();
            }
        });

        view.getDownloadButton().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                downloadFile();
            }
        });

        view.getFileTree().addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                updateSelectedPath(e);
            }
        });
        view.getNewFolderButton().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                newFolder();
            }
        });
        view.getNewProjectButton().addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				newProject();
				
			}
		});
        view.getDeleteButton().addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				deleteFileOrFolder();
				
			}
		});
        view.getShareButton().addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				handleShareAction();			
			}
		});
        view.getSyncButton().addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					syncData();
				} catch (IOException e1) {
					e1.printStackTrace();
				}				
			}
		});
        view.getDisconnectButton().addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					disconnect();
				} catch (IOException e1) {
					e1.printStackTrace();
				}			
			}
		});
    }
    private void getTree(String _userN, String _pass)
    {
        try {
        	System.out.println("Chuan bi vao authenticate ");
        	if(model.authenticate(_userN, _pass))
        	{
        		//model.initThreadNotif();
        		loginView.setVisible(false);
        		view.setVisible(true);
        		DefaultMutableTreeNode root = model.getFileTree();
                System.out.print("da nhan cay ban dau");
                DefaultTreeModel treeModel = new DefaultTreeModel(root);
                view.setTreeModel(treeModel);
                view.getLogArea().append("Files synchronized.\n");
        	}
        	else {
                JOptionPane.showMessageDialog(
                    loginView, 
                    "Sai tên tài khoản hoặc mật khẩu. Vui lòng thử lại!", 
                    "Đăng nhập thất bại", 
                    JOptionPane.ERROR_MESSAGE
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(
                loginView, 
                "Đã xảy ra lỗi khi thực hiện! Vui lòng kiểm tra lại.",
                "Lỗi",
                JOptionPane.ERROR_MESSAGE
            );}
    }
    
    private void reload() {
        try {
            System.out.println("Bat dau reload...");
            model.reloadTree();             
            System.out.println("reload cây hoàn tất.");
            DefaultMutableTreeNode root = model.getFileTree();
            
            System.out.println("Đã nhận được cây reload.");
            DefaultTreeModel treeModel = new DefaultTreeModel(root);
            view.setTreeModel(treeModel);
            
            System.out.println("reload TreeModel xong.");
            view.getLogArea().append("Files synchronized.\n");
            view.getPromtLabel().setText("Warning: " + model.getWarning());
        } catch (Exception e) {
            System.out.println("Lỗi xảy ra: " + e.getMessage());
            view.getLogArea().append("Error: dfdfdfd" + e.getMessage() + "\n");
        }
    }


    private void uploadFile() {
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showOpenDialog(view) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            String remoteFolder = treePathToString(path);
            try {
                model.uploadFile(file, remoteFolder);
                view.getLogArea().append("Uploaded file: " + file.getName() + "\n");
            } catch (Exception e) {
                view.getLogArea().append("Error: " + e.getMessage() + "\n");
            }
        }
        reload();
    }

    private void downloadFile() {
    	try {
    		String selectedPath = treePathToString(path);
    		String checkFolderorFile = model.headerDownloadFile(selectedPath);
	        view.getLogArea().append(selectedPath);
	        if (selectedPath != "") {
	            JFileChooser fileChooser = new JFileChooser();
	            fileChooser.setDialogTitle("Chọn đường dẫn để lưu file");
	            if(checkFolderorFile.equals("FILE")) {
	            	fileChooser.setSelectedFile(new File(selectedPath));
	            }
	            else if(checkFolderorFile.equals("FOLDER")){
	            	fileChooser.setSelectedFile(new File(selectedPath + ".zip"));
	            }
	            if (fileChooser.showSaveDialog(view) == JFileChooser.APPROVE_OPTION) {
	            	model.downloadConfirmation(true);
	                File file = fileChooser.getSelectedFile();
	                model.downloadFile(selectedPath, file);
                    view.getLogArea().append("Downloaded file: " + file.getAbsolutePath() + "\n");
	            }
	            else {
	            	model.downloadConfirmation(false);
	            }
	        }
    	}catch(Exception e) {
    		view.getLogArea().append("Error: " + e.getMessage() + "\n");
    	}
        
    }

    private void updateSelectedPath(TreeSelectionEvent e) {
        path = e.getPath();
        view.getLinkLabel().setText("Selected: " + treePathToString(path));
    }
    public String treePathToString(TreePath path) {
        if (path == null) return "";

        StringBuilder result = new StringBuilder();
        Object[] elements = path.getPath();

        for (int i = 0; i < elements.length; i++) {
            result.append(elements[i].toString());
            if (i < elements.length - 1) {
                result.append("/"); 
            }
        }
        return result.toString();
    }

    private void newFolder() {
    	String selectedPath = treePathToString(path); 
    	if (selectedPath == null || selectedPath.isEmpty()) {
    		view.getLogArea().append("No folder selected.\n");
    		return;
    	}

    	String folderName = JOptionPane.showInputDialog(
    			view,
    			"Enter the name for the new folder:",
    			"Create New Folder",
    			JOptionPane.PLAIN_MESSAGE
    			);

    	if (folderName == null || folderName.trim().isEmpty()) {
    		view.getLogArea().append("Folder creation cancelled.\n");
    		return;
    	}

    	try {
    		model.createFolder(selectedPath, folderName.trim()); 
    		view.getLogArea().append("Created new folder: " + folderName + " at " + selectedPath + "\n");
    		//syncFiles(); 
    	} catch (Exception e) {
    		view.getLogArea().append("Error creating folder " + folderName + ": " + e.getMessage() + "\n");
    	}
    	reload();
    }
    private void newProject() {
    	
    	String projName = JOptionPane.showInputDialog(
    			view,
    			"Enter the name for the new Project:",
    			"Create New Project",
    			JOptionPane.PLAIN_MESSAGE
    			);

    	if (projName == null || projName.trim().isEmpty()) {
    		view.getLogArea().append("Folder creation cancelled.\n");
    		return;
    	}

    	try {
    		model.createProject(projName);
    		view.getLogArea().append("Created new Project: " + projName + " at " + "\n");
    		//syncFiles(); 
    	} catch (Exception e) {
    		view.getLogArea().append("Error creating Project " + projName + ": " + e.getMessage() + "\n");
    	}
    	reload();
    }
    private void deleteFileOrFolder()
    {
    	String selectedPath = treePathToString(path); // Chuyển đường dẫn từ cây thành chuỗi
        if (selectedPath == null || selectedPath.isEmpty()) {
            view.getLogArea().append("No file or folder selected.\n");
            return;
        }

        // Xác nhận từ người dùng trước khi xóa
        int confirm = JOptionPane.showConfirmDialog(
            view,
            "Are you sure you want to delete: " + selectedPath + "?",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                model.delete(selectedPath); 
                view.getLogArea().append("Deleted: " + selectedPath + "\n");
            } catch (Exception e) {
                view.getLogArea().append("Error deleting " + selectedPath + ": " + e.getMessage() + "\n");
            }
        } else {
            view.getLogArea().append("Delete operation cancelled.\n");
        }
        reload();
    }
    private void disconnect() throws IOException
    {
    	model.disconnect();
    	//model.clientNotifThread.stop();
    }


    public void handleShareAction() {
        String repoName = getRepoName();
        if (repoName == null) {
            JOptionPane.showMessageDialog(view,
                "Please select a repository to manage sharing permissions.",
                "No Repository Selected",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
        	model.sendShareMessage(repoName);

            List<String> unsharedUsers = model.getUsersFromServer(); 
            List<String> sharedUsers = model.getUsersFromServer(); 
  
            // Khởi tạo ShareDialog
            ShareDialog shareDialog = new ShareDialog(view, unsharedUsers, sharedUsers);
            
            
            if(model.shareCheckOwner()) {
            	shareDialog.getShareButton().setEnabled(true);
            	shareDialog.getUnshareButton().setEnabled(true);
            }else {
            	shareDialog.getShareButton().setEnabled(false);
            	shareDialog.getUnshareButton().setEnabled(false);
			}
            
            // Lắng nghe sự kiện nút Share
            shareDialog.getShareButton().addActionListener(e -> {
                List<String> selectedUnsharedUsers = shareDialog.getUnsharedUserList().getSelectedValuesList();

                if (selectedUnsharedUsers.isEmpty()) {
                    JOptionPane.showMessageDialog(view, 
                        "Please select at least one user to share with.", 
                        "No User Selected", 
                        JOptionPane.WARNING_MESSAGE);
                } else {
                    try {
                        // Gửi yêu cầu chia sẻ tới server
                        model.shareRepoMessage();
                        model.sendListToServer(selectedUnsharedUsers);

                        JOptionPane.showMessageDialog(view,
                            "Shared successfully with: " + String.join(", ", selectedUnsharedUsers),
                            "Share Success",
                            JOptionPane.INFORMATION_MESSAGE);

                        // Cập nhật lại danh sách trong dialog
                     //   unsharedUsers.removeAll(selectedUnsharedUsers);
                       // sharedUsers.addAll(selectedUnsharedUsers);
                        shareDialog.updateLists(unsharedUsers, sharedUsers);

                    } catch (IOException ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(view,
                            "Failed to share repository. Please try again.",
                            "Share Error",
                            JOptionPane.ERROR_MESSAGE);
                    }
                }
            });

            // Lắng nghe sự kiện nút Unshare
            shareDialog.getUnshareButton().addActionListener(e -> {
                List<String> selectedSharedUsers = shareDialog.getSharedUserList().getSelectedValuesList();

                if (selectedSharedUsers.isEmpty()) {
                    JOptionPane.showMessageDialog(view, 
                        "Please select at least one user to unshare.", 
                        "No User Selected", 
                        JOptionPane.WARNING_MESSAGE);
                } else {
                    try {
                    	// Gửi yêu cầu chia sẻ tới server
                        model.unshareRepoMessage();
                        model.sendListToServer(selectedSharedUsers);

                        JOptionPane.showMessageDialog(view,
                            "Unshared successfully with: " + String.join(", ", selectedSharedUsers),
                            "Unshare Success",
                            JOptionPane.INFORMATION_MESSAGE);

                        // Cập nhật lại danh sách trong dialog
                        sharedUsers.removeAll(selectedSharedUsers);
                        unsharedUsers.addAll(selectedSharedUsers);
                        shareDialog.updateLists(unsharedUsers, sharedUsers);

                    } catch (IOException ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(view,
                            "Failed to unshare repository. Please try again.",
                            "Unshare Error",
                            JOptionPane.ERROR_MESSAGE);
                    }
                }
            });

            // Lắng nghe sự kiện nút Cancel
            shareDialog.getCancelButton().addActionListener(e -> {
            	try {
					model.sendCanelMessage();
					shareDialog.dispose();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
            	
            });
            
            shareDialog.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    try {
						model.sendCanelMessage();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
                    shareDialog.dispose();
                }
            });

            shareDialog.setVisible(true);

        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(view,
                "Failed to fetch user list. Please try again.",
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    private String getRepoName() {
        TreePath path = view.getFileTree().getSelectionPath();
        
        if (path == null) {
            return null; 
        }

        Object[] nodes = path.getPath(); 
        if (nodes.length < 2) {
            return null; 
        }

        String repoName = nodes[1].toString();
        return repoName;
    }
    
    public void syncData() throws IOException
    {
    	model.syncData();
    	reload();
    }
    
}