package app;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.util.StatusPrinter;
import org.slf4j.LoggerFactory;
import infrastructure.persistence.DatabaseConnection;

public class Launch {
    public static void main(String[] args) {
        DatabaseConnection.startConnection();
        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        StatusPrinter.print(lc);
        javax.swing.SwingUtilities.invokeLater(() -> {
            presentation.login.LoginFrame login = new presentation.login.LoginFrame("PRODUCTS");
            login.setVisible(true);
        });
        Runtime.getRuntime().addShutdownHook(new Thread(DatabaseConnection::closeConnection));
    }
}