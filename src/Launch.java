public class Launch {
    public static void main(String[] args) {
        Infrastructure.DbController.Main.startConnection();

        javax.swing.SwingUtilities.invokeLater(() -> {
            Presentation.LoginFrame login = new Presentation.LoginFrame("products");
            login.setVisible(true);
        });

        Runtime.getRuntime().addShutdownHook(new Thread(Infrastructure.DbController.Main::closeConnection));
    }
}