package View;


import javax.swing.*;

public class LoginView extends JFrame {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public JTextField usernameField;
    public JPasswordField passwordField;
    public JButton loginButton;
    public JLabel statusLabel;
    public JTextField ipField; 
    public JTextField portField;

    public LoginView() {
        setTitle("Login");
        setSize(300, 300); 
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(null);

        // Username field
        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setBounds(30, 30, 80, 25);
        add(usernameLabel);

        usernameField = new JTextField();
        usernameField.setBounds(120, 30, 140, 25);
        add(usernameField);

        // Password field
        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setBounds(30, 70, 80, 25);
        add(passwordLabel);

        passwordField = new JPasswordField();
        passwordField.setBounds(120, 70, 140, 25);
        add(passwordField);

        // IP field
        JLabel ipLabel = new JLabel("IP Server:");
        ipLabel.setBounds(30, 110, 80, 25);
        add(ipLabel);

        ipField = new JTextField();
        ipField.setBounds(120, 110, 140, 25);
        add(ipField);

        // Port field
        JLabel portLabel = new JLabel("Port:");
        portLabel.setBounds(30, 150, 80, 25);
        add(portLabel);

        portField = new JTextField();
        portField.setBounds(120, 150, 140, 25);
        add(portField);

        // Login button
        loginButton = new JButton("Login");
        loginButton.setBounds(100, 190, 100, 30);
        add(loginButton);
     
        // Status label
        statusLabel = new JLabel();
        statusLabel.setBounds(30, 220, 250, 25);
        add(statusLabel);
    }

    // Getter for loginButton
    public JButton getLoginButton() {
        return loginButton;
    }

    // Getter for IP field
    public String getIpField() {
        return ipField.getText();
    }

    // Getter for Port field
    public String getPortField() {
        return portField.getText();
    }
}