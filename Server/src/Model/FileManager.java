package Model;

import javax.swing.tree.DefaultMutableTreeNode;
import java.io.*;

public class FileManager {
    private String storageDirectory;

    public FileManager() {
    }

    public FileManager(String storageDirectory) {
        this.storageDirectory = storageDirectory;
    }

    public void saveFile(String filePath, String fileName, byte[] fileData) throws IOException {
        File file = new File(storageDirectory, filePath + File.separator + fileName);
        file.getParentFile().mkdirs();
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(fileData);
        }
    }

    public byte[] readFile(String filePath) throws IOException {
        File file = new File(storageDirectory, filePath);
        try (FileInputStream fis = new FileInputStream(file)) {
            return fis.readAllBytes();
        }
    }

    public void WriteFile(File _file, DataOutputStream _dos) {
		try (FileInputStream fileInput = new FileInputStream(_file)) {
             byte[] buffer = new byte[4096];
             int bytesRead;
             while ((bytesRead = fileInput.read(buffer)) != -1) {
                 _dos.write(buffer, 0, bytesRead);
             }
             System.out.println("Đã gửi file: " + _file.getAbsolutePath());
         }catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
		}
		
	}
    
    public void ReadFile(File _fileToSave, long _fileSize, DataInputStream dis) {
        try {
            try (FileOutputStream fileOutput = new FileOutputStream(_fileToSave)) {
                byte[] buffer = new byte[4096]; // Đệm
                int bytesRead;
                long totalBytesRead = 0;
                
                while ((bytesRead = dis.read(buffer, 0, Math.min(buffer.length, (int) (_fileSize - totalBytesRead)))) != -1) {
                    System.out.println(bytesRead);
                	fileOutput.write(buffer, 0, bytesRead);
                    totalBytesRead += bytesRead;
                    if (totalBytesRead >= _fileSize) {
                        break;
                    }
                }
                System.out.print(" -52 filemanager");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public DefaultMutableTreeNode buildTreeFromFiles() {
        // Xây dựng cây thư mục từ hệ thống file
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Repository");
        // Lập trình logic tại đây
        return root;
    }
}
