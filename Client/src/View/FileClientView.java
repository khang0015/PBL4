package View;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.tree.DefaultTreeModel;

public class FileClientView extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private JPanel contentPane;
	
	private JTextArea logArea;
    private JTree fileTree;
    private JButton reloadButton, uploadButton, downloadButton, newFolderButton, disconnectButton, deleteButton, newProjectButton, shareButton, syncButton;
    private JLabel linkLabel, promtLabel;


	public FileClientView() {
		setTitle("File Sync Client");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(900, 500);
        setLocationRelativeTo(null);

        // Main Layout
        setLayout(new BorderLayout());

        // Toolbar
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);

        reloadButton = createButton("Reload", "reload.png");
        newFolderButton = createButton("New Folder", "folder.png");
        newProjectButton = createButton("New Project", "project.png");
        uploadButton = createButton("Upload", "upload.png");
        downloadButton = createButton("Download", "download.png");
        disconnectButton = createButton("Disconnect", "disconnect.png");
        deleteButton = createButton("Delete", "delete.png");
        shareButton = createButton("Share", "share.png");
        syncButton = createButton("Sync", "reload.png");

        toolbar.add(reloadButton);
        toolbar.add(newFolderButton);
        toolbar.add(newProjectButton);
        toolbar.add(uploadButton);
        toolbar.add(downloadButton);
        toolbar.add(disconnectButton);
        toolbar.add(deleteButton);
        toolbar.add(shareButton);
        toolbar.add(syncButton);

        add(toolbar, BorderLayout.NORTH);

        // File Tree and Log Area
        JSplitPane splitPane = new JSplitPane();
        splitPane.setDividerLocation(400);

        // File Tree
        fileTree = new JTree();
        JScrollPane fileScrollPane = new JScrollPane(fileTree);
        splitPane.setLeftComponent(fileScrollPane);

        // Log Area
        logArea = new JTextArea();
        logArea.setEditable(false);
        JScrollPane logScrollPane = new JScrollPane(logArea);
        splitPane.setRightComponent(logScrollPane);

        add(splitPane, BorderLayout.CENTER);

        // Footer
//        JPanel footerPanel = new JPanel(new BorderLayout());
//        footerPanel.setPreferredSize(new Dimension(getWidth(), 40)); // Đặt chiều cao là 40px (tùy chỉnh)
//        linkLabel = new JLabel("Selected: ");
//        promtLabel = new JLabel("Warning: ");
//        linkLabel.setBorder(new EmptyBorder(5, 10, 5, 10)); // Thêm padding nếu cần
//        footerPanel.add(linkLabel, BorderLayout.WEST);
//        footerPanel.add(promtLabel, BorderLayout.WEST);
//        add(footerPanel, BorderLayout.SOUTH);
     // Footer
        JPanel footerPanel = new JPanel(new GridLayout(2, 1)); // 2 hàng, 1 cột

        // Warning Label
        promtLabel = new JLabel("Warning: ");
        promtLabel.setBorder(new EmptyBorder(5, 10, 5, 10)); // Thêm padding nếu cần
        footerPanel.add(promtLabel);

        // Selected Label
        linkLabel = new JLabel("Selected: ");
        linkLabel.setBorder(new EmptyBorder(5, 10, 5, 10)); // Thêm padding nếu cần
        footerPanel.add(linkLabel);

        footerPanel.setPreferredSize(new Dimension(getWidth(), 60)); // Điều chỉnh chiều cao phù hợp
        add(footerPanel, BorderLayout.SOUTH);




        // Exit Confirmation
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                int confirm = JOptionPane.showConfirmDialog(
                        FileClientView.this,
                        "Do you want to disconnect and exit?",
                        "Exit Confirmation",
                        JOptionPane.YES_NO_OPTION
                );
                if (confirm == JOptionPane.YES_OPTION) {
                	disconnectButton.doClick();
                    System.exit(0);
                }
            }
        });

     //   setVisible(true);
	}
	
	private JButton createButton(String text, String iconName) {
    	String iconPath = "icon/" + iconName;
    	ImageIcon originalIcon = new ImageIcon(iconPath);
        Image scaledImage = originalIcon.getImage().getScaledInstance(24, 24, Image.SCALE_SMOOTH); // Resize icon 24x24
        ImageIcon scaledIcon = new ImageIcon(scaledImage);

        JButton button = new JButton(text, scaledIcon);
        button.setFocusable(false);
        button.setPreferredSize(new Dimension(140, 40)); // Kích thước nút
        button.setHorizontalTextPosition(SwingConstants.RIGHT); // Văn bản nằm bên phải icon
        button.setVerticalTextPosition(SwingConstants.CENTER);   // Văn bản và icon trên cùng một hàng
        button.setIconTextGap(10); // Khoảng cách giữa icon và văn bản
        return button;
    }
    
    public void setTreeModel(DefaultTreeModel model) {
        fileTree.setModel(model);
    }

    // Getters
    public JTree getFileTree() {
        return fileTree;
    }

    public JTextArea getLogArea() {
        return logArea;
    }

    public JButton getReloadButton() {
        return reloadButton;
    }

    public JButton getUploadButton() {
        return uploadButton;
    }

    public JButton getDownloadButton() {
        return downloadButton;
    }

    public JButton getNewFolderButton() {
        return newFolderButton;
    }

    public JButton getNewProjectButton() {
        return newProjectButton;
    }

    public JButton getDisconnectButton() {
        return disconnectButton;
    }

    public JButton getDeleteButton() {
        return deleteButton;
    }

    public JButton getShareButton() {
        return shareButton;
    }
    public JButton getSyncButton() {
    	return syncButton;
    }
    public JLabel getLinkLabel() {
        return linkLabel;
    }
    public JLabel getPromtLabel() {
    	return promtLabel;
    }
}
