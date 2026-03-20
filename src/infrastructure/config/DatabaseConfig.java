package infrastructure.config;

import org.slf4j.Logger;

import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class DatabaseConfig {

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(DatabaseConfig.class);

    public static String getSecureDbUrl() {
        return "jdbc:h2:file:" + getAppFolderPath() + File.separator + "db\\db;CIPHER=AES;FILE_LOCK=FS";
    }

    public static String getAppFolderPath() {
        String appPartition = getAppPartition();
        String appFolderName = "Store_Manager_App";
        return appPartition + File.separator + appFolderName;
    }

    public static String getSecureDbPassword() {
        String filePassword = System.getenv("STORE_DB_FILE_KEY");
        String userPassword = System.getenv("STORE_DB_USER_KEY");

        if (filePassword == null || userPassword == null) {
            log.error("Database passwords not found in Windows Environment Variables!");
            throw new RuntimeException("CRITICAL ERROR: Database passwords not found in Windows Environment Variables!");
        }
        return filePassword + " " + userPassword;
    }

    public static String getAppPartition() {
        try {
            File appLocation = new File(DatabaseConfig.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            String path = appLocation.getParent();
            path = path.substring(0, path.indexOf('\\') + 1);
            return path;
        } catch (URISyntaxException e) {
            log.error("Failed to determine application folder: {}", e.getMessage());
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