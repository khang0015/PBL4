package Controller;

import javax.swing.tree.DefaultMutableTreeNode;
import Model.CacheManager;
import View.FileServerView;

public class TreeServerController extends Thread {
    private static TreeServerController instance;

    private DefaultMutableTreeNode root;
    public static boolean running = true;
    private FileServerView view;

    public static synchronized TreeServerController getInstance(FileServerView view) {
        if (instance == null) {
            try {
                instance = new TreeServerController(view);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return instance;
    }

    public TreeServerController(FileServerView view) throws Exception {
        this.view = view;
        
        root = new DefaultMutableTreeNode("Repositories");
        
        CacheManager.getInstance().initCache(null);
        CacheManager.getInstance().buildTreeFromDatabase(root);
        System.out.println("Tree structure built successfully on server.");
        view.updateTree(root);
    }

    public void stopThread() {
        running = false;
    }


    @Override
    public void run() {
        while (running) {
            try {
            	if(CacheManager.getInstance().isSyncTreeServer())
            	{
            		try {
            			System.out.println("Tree updating!!!");                   	
                    	refreshTree();
            		}catch (Exception e) {
        				e.printStackTrace();
        			}
            	}
            } catch (Exception e) {
                e.printStackTrace();
                break;
            }
        }
    }


    public void refreshTree() throws Exception {
    	view.resetTree();
    	root = new DefaultMutableTreeNode("Repositories");
    	CacheManager.getInstance().buildTreeFromDatabase(root);
    	view.updateTree(root);
    	CacheManager.getInstance().setSyncServer(false);
    	//stopThread();
    }
}

