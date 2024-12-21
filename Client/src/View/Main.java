package View;

import java.io.IOException;
import Controller.FileClientController;


public class Main {
    public static void main(String[] args) throws IOException {
    	LoginView loginView = new LoginView();
    	FileClientView view = new FileClientView();
    	new FileClientController(view, loginView);
    }
}