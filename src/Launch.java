public class Launch {
    public static void main(String[] args) {
        Infrastructure.DbController.Main.startConnection();

        javax.swing.SwingUtilities.invokeLater(() -> {
            Presentation.TableViewerFrame frame = new Presentation.TableViewerFrame("products");
            frame.setVisible(true);
        });

        Runtime.getRuntime().addShutdownHook(new Thread(Infrastructure.DbController.Main::closeConnection));
    }
}
  /*frame.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosing(java.awt.event.WindowEvent e) {
                    Infrastructure.DbController.Main.closeConnection();
                }
            });*/