package Controller;

import View.FileServerView;

public class Main {
	public static void main(String[] args) throws Exception {
		FileServerView view = new FileServerView();
		SyncController controller = new SyncController(view);
		controller.updateIpAddress();
		TreeServerController treeController = new TreeServerController(view);
		treeController.start();
		controller.startServer();
	}
}
