package infrastructure.config;

import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class DatabaseConfig {

    public static String getSecureDbUrl() {
        String appFolder = getAppFolder();

        String dbFolderPath = appFolder + File.separator + ".store_manager_app";

        return "jdbc:h2:file:" + dbFolderPath + File.separator + "db;CIPHER=AES;FILE_LOCK=FS";
    }

    public static String getSecureDbPassword() {
        String filePassword = System.getenv("STORE_DB_FILE_KEY");
        String userPassword = System.getenv("STORE_DB_USER_KEY");

        if (filePassword == null || userPassword == null)
            throw new RuntimeException("CRITICAL ERROR: Database passwords not found in Windows Environment Variables!");

        return filePassword + " " + userPassword;
    }

    public static String getAppFolder() {
        try {
            File appLocation = new File(DatabaseConfig.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            String path = appLocation.getParent();
                path = path.substring(0, path.indexOf('\\') + 1);
            return path;

        } catch (URISyntaxException e) {
            System.out.println("Error determining application folder: " + e.getMessage());
            return null;
        }
    }

    public static List<String> getExistingPartitions() {
        List<String> cleanDrives = new ArrayList<>();
        File[] drives = File.listRoots();

        if (drives != null)
            for (File drive : drives)
                cleanDrives.add(drive.getAbsolutePath());
        return cleanDrives;
    }
}