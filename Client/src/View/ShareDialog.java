package View;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.List;
import javax.swing.*;

public class ShareDialog extends JDialog {

    private JList<String> unsharedUserList; // Danh sách user chưa được chia sẻ
    private JList<String> sharedUserList;   // Danh sách user đã được chia sẻ
    private JButton shareButton, unshareButton, cancelButton;

    public ShareDialog(JFrame parent, List<String> unsharedUsers, List<String> sharedUsers) {
        super(parent, "Manage Share Permissions", true);
        setSize(500, 400);
        setLocationRelativeTo(parent);

        // Khởi tạo layout
        setLayout(new BorderLayout(10, 10));

        // Panel chứa danh sách
        JPanel listsPanel = new JPanel(new GridLayout(1, 2, 10, 10));

        // Danh sách chưa được chia sẻ
        DefaultListModel<String> unsharedModel = new DefaultListModel<>();
        if (unsharedUsers != null) {
            for (String user : unsharedUsers) {
                unsharedModel.addElement(user);
            }
        }
        unsharedUserList = new JList<>(unsharedModel);
        unsharedUserList.setBorder(BorderFactory.createTitledBorder("Unshared Users"));
        unsharedUserList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        listsPanel.add(new JScrollPane(unsharedUserList));

        // Danh sách đã được chia sẻ
        DefaultListModel<String> sharedModel = new DefaultListModel<>();
        if (sharedUsers != null) {
            for (String user : sharedUsers) {
                sharedModel.addElement(user);
            }
        }
        sharedUserList = new JList<>(sharedModel);
        sharedUserList.setBorder(BorderFactory.createTitledBorder("Shared Users"));
        sharedUserList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        listsPanel.add(new JScrollPane(sharedUserList));

        add(listsPanel, BorderLayout.CENTER);

        // Panel chứa các nút
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        shareButton = new JButton("Share");
        unshareButton = new JButton("Unshare");
        cancelButton = new JButton("Cancel");
        buttonPanel.add(shareButton);
        buttonPanel.add(unshareButton);
        buttonPanel.add(cancelButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    public void updateLists(List<String> unsharedUsers, List<String> sharedUsers) {
        // Cập nhật danh sách người dùng chưa chia sẻ
        DefaultListModel<String> unsharedModel = (DefaultListModel<String>) unsharedUserList.getModel();
        unsharedModel.clear(); // Xóa danh sách cũ
        for (String user : unsharedUsers) {
            unsharedModel.addElement(user); // Thêm người dùng mới vào danh sách
        }

        // Cập nhật danh sách người dùng đã chia sẻ
        DefaultListModel<String> sharedModel = (DefaultListModel<String>) sharedUserList.getModel();
        sharedModel.clear(); // Xóa danh sách cũ
        for (String user : sharedUsers) {
            sharedModel.addElement(user); // Thêm người dùng mới vào danh sách
        }
    }
    
    // Trả về danh sách người dùng chưa được chia sẻ được chọn
    public JList<String> getUnsharedUserList() {
        return unsharedUserList;
    }

    // Trả về danh sách người dùng đã được chia sẻ được chọn
    public JList<String> getSharedUserList() {
        return sharedUserList;
    }

    public JButton getShareButton() {
        return shareButton;
    }

    public JButton getUnshareButton() {
        return unshareButton;
    }

    public JButton getCancelButton() {
        return cancelButton;
    }
}
