package infrastructure.persistence;

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

    public static void startConnection() {
        try {
            if (con != null && !con.isClosed()) {
                System.out.println("Database connection is already established.");
                return;
            }
            con = DriverManager.getConnection(getSecureDbUrl(), "program_user", getSecureDbPassword());
            System.out.println("Connection to the database established successfully.");
        } catch (SQLException e) {
            System.out.println("Error connecting to the database: " + e.getMessage());
            System.exit(0);
        }
    }

    public boolean createDatabaseBackup(String targetDirectory) {
        String filePassword = System.getenv("STORE_DB_FILE_KEY");
        String backupPassword = System.getenv("STORE_DB_BACKUP_KEY");
        String combinedPassword = filePassword + " " + backupPassword;

        if (filePassword == null || backupPassword == null) {
            System.err.println("CRITICAL ERROR: Backup keys missing from Windows Environment Variables!");
            return false;
        }

        String timeStamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy_HH-mm"));

        if (!targetDirectory.endsWith(File.separator) && !targetDirectory.endsWith("/"))
            targetDirectory += File.separator;

        String fullBackupPath = targetDirectory + "StoreBackup_" + timeStamp + ".zip";

        String backupSql = "BACKUP TO '" + fullBackupPath + "'";

        try (Connection conn = DriverManager.getConnection(getSecureDbUrl(), "backup_service", combinedPassword);
             Statement stmt = conn.createStatement()) {

            System.out.println("Starting database backup...");
            stmt.execute(backupSql);
            System.out.println("Success! Backup saved to: " + fullBackupPath);
            return true;

        } catch (Exception e) {
            System.err.println("CRITICAL ERROR: Database backup failed!" + e.getMessage());
            return false;
        }
    }

    public static void closeConnection() {
        try {
            if (con != null) con.close();
            System.out.println("Database resources closed successfully.");
        } catch (SQLException e) {
            System.out.println("Error closing database resources: " + e.getMessage());
        }
    }


}

