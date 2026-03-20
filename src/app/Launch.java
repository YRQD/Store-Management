package app;
import infrastructure.persistence.DatabaseConnection;

public class Launch {
    public static void main(String[] args) {
        System.setProperty("MY_APP_PATH", "A:\\Store_Manager_App"); // TODO: Remove this line before deployment.
        DatabaseConnection.startConnection();
        javax.swing.SwingUtilities.invokeLater(() -> {
            presentation.login.LoginFrame login = new presentation.login.LoginFrame("PRODUCTS");
            login.setVisible(true);
        });
        Runtime.getRuntime().addShutdownHook(new Thread(DatabaseConnection::closeConnection));
    }
}