package app;

import infrastructure.persistence.DatabaseConnection;

public class Launch {
    public static void main(String[] args) {
        DatabaseConnection.startConnection();
        javax.swing.SwingUtilities.invokeLater(() -> {
            presentation.login.LoginFrame login = new presentation.login.LoginFrame("PRODUCTS");
            login.setVisible(true);
        });

        Runtime.getRuntime().addShutdownHook(new Thread(DatabaseConnection::closeConnection));
    }
}