package infrastructure.persistence;

import infrastructure.config.DatabaseConfig;
import org.slf4j.Logger;

import java.io.File;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static infrastructure.config.DatabaseConfig.*;

public class DatabaseConnection {

    public static Connection con;
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(DatabaseConnection.class);

    public static void startConnection() {
        try {
            if (con != null && !con.isClosed())
                return;
            con = DriverManager.getConnection(getSecureDbUrl(), "program_user", getSecureDbPassword());
        } catch (SQLException e) {
            log.error("Database connection failed: {}", e.getMessage());
            System.exit(0);
        }
    }

    public boolean createDatabaseBackup(String targetDirectory) {
        String filePassword = System.getenv("STORE_DB_FILE_KEY");
        String backupPassword = System.getenv("STORE_DB_BACKUP_KEY");
        String combinedPassword = filePassword + " " + backupPassword;

        if (filePassword == null || backupPassword == null) {
            log.error("Backup keys missing from Windows Environment Variables!");
            return false;
        }

        String timeStamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy_HH-mm"));

        if (!targetDirectory.endsWith(File.separator) && !targetDirectory.endsWith("/"))
            targetDirectory += File.separator;

        String fullBackupPath = targetDirectory + "StoreBackup_" + timeStamp + ".zip";
        String backupSql = "BACKUP TO '" + fullBackupPath + "'";

        try (Connection conn = DriverManager.getConnection(getSecureDbUrl(), "backup_service", combinedPassword);
             Statement stmt = conn.createStatement()) {

            stmt.execute(backupSql);
            log.info("Database backup created successfully at: {} at time: {}", fullBackupPath, timeStamp);
            return true;

        } catch (Exception e) {
            log.error("Database backup failed at time: {}. {}", timeStamp, e.getMessage());
            return false;
        }
    }

    public static void closeConnection() {
        try {
            if (con != null) con.close();
        } catch (SQLException e) {
            log.error("Failed to close database connection: {}", e.getMessage());
        }
    }


}

