package Infrastructure.DbController;

import java.sql.*;
import java.sql.SQLException;

public class Main {

    public static void startConnection() {
        try {
            Constants.con = DriverManager.getConnection(Constants.url, Constants.user, Constants.password);
            Constants.st = Constants.con.createStatement();
            System.out.println(Constants.url);
        } catch (SQLException e) {
            System.out.println(Constants.url);
            System.exit(0);
        }
    }

    public static int getRowsNumber(String tableName) {
        try {
            Constants.rsl = Constants.st.executeQuery("SELECT COUNT(*) FROM ".concat(tableName));
            Constants.rsl.next();
            return Constants.rsl.getInt(1);
        } catch (SQLException e) {
            return -1;
        }
    }

    public static int getColumnsNumber(String tableName) {
        try {
            Constants.rsl = Constants.st.executeQuery("SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = '".concat(tableName).concat("'"));
            Constants.rsl.next();
            return Constants.rsl.getInt(1);
        } catch (SQLException e) {
            return -1;
        }
    }

    public static Object[][] getAll(String tableName) {
        String sql = "SELECT * FROM ".concat(tableName);

        int rowsCount = Main.getRowsNumber(tableName);
        int columnsCount = Main.getColumnsNumber(tableName);

        Object[][] data = new Object[rowsCount][columnsCount];
        int j = 0;
        try {
            Constants.rsl = Constants.st.executeQuery(sql);
            while (Constants.rsl.next()) {
                for (int i = 0; i < columnsCount; i++) {
                    data[j][i] = Constants.rsl.getString(i + 1);
                }
                j++;
            }
        } catch (SQLException e) {
            System.out.println("Error in getAll: " + e.getMessage());
        }
        return data;
    }

    public static void closeConnection() {
        try {
            if (Constants.rsl != null) Constants.rsl.close();
            if (Constants.st != null) Constants.st.close();
            if (Constants.con != null) Constants.con.close();
        } catch (SQLException e) {
            System.out.println("Error closing database resources: " + e.getMessage());
        }
    }
}
