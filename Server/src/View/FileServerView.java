package View;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import Controller.SyncController;
import Controller.TreeServerController;

public class FileServerView extends JFrame {

	private static final long serialVersionUID = 1L;
	private JTree fileTree;
    private JTextArea logArea;
    private JTextField ipField;
    private JTextField portField;

    public FileServerView() throws Exception {
        setTitle("File Server");
        setSize(700, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(null);

        // Tree View
        fileTree = new JTree(new DefaultMutableTreeNode("Repository"));
        JScrollPane treeScrollPane = new JScrollPane(fileTree);
        treeScrollPane.setBounds(10, 50, 350, 300);
        add(treeScrollPane);

        // Log View
        logArea = new JTextArea();
        logArea.setEditable(false);
        JScrollPane logScrollPane = new JScrollPane(logArea);
        logScrollPane.setBounds(370, 50, 300, 300);
        add(logScrollPane);

        // Server Info
        ipField = new JTextField();
        ipField.setEditable(false);
        portField = new JTextField();
        portField.setEditable(false);

        add(new JLabel("IP:")).setBounds(10, 10, 30, 20);
        add(ipField).setBounds(40, 10, 100, 20);
        add(new JLabel("Port:")).setBounds(150, 10, 40, 20);
        add(portField).setBounds(190, 10, 50, 20);
        
        setVisible(true);
//        SyncController controller = new SyncController(this);
//		TreeServerController treeController = new TreeServerController(this);
//		treeController.start();
//		controller.startServer();
    }
    public void setIpAddress(String ip) {
        ipField.setText(ip);
    }
    public void setPort(String port)
    {
    	portField.setText(port);
    }
    public void resetTree() {
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("Repository");
        fileTree.setModel(new DefaultTreeModel(rootNode));
    }


    public void updateTree(DefaultMutableTreeNode rootNode) {
        fileTree.setModel(new DefaultTreeModel(rootNode));
    }

    public void log(String message) {
        logArea.append(message + "\n");
    }
}
