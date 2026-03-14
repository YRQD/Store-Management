package Infrastructure.DbController;

import java.lang.reflect.Field;
import java.sql.*;
import java.sql.SQLException;

public class Main {

    public static void startConnection() {
        try {
            Constant.con = DriverManager.getConnection(Constant.url, Constant.user, Constant.password);
            Constant.st = Constant.con.createStatement();
            System.out.println(Constant.url);
        } catch (SQLException e) {
            System.out.println(Constant.url);
            System.exit(0);
        }
    }

    public static Object[][] getAll(String tableName) {
        String sql = "SELECT * FROM ".concat(tableName);

        int rowsCount = Helper.getRowsNumber(tableName);
        int columnsCount = Helper.getColumnsNumber(tableName);

        if (rowsCount < 0 || columnsCount < 0)
            return new Object[0][0];

        Object[][] data = new Object[rowsCount][columnsCount];
        int j = 0;
        try {
            Constant.rsl = Constant.st.executeQuery(sql);
            while (Constant.rsl.next()) {
                for (int i = 0; i < columnsCount; i++) {
                    data[j][i] = Constant.rsl.getString(i + 1);
                }
                j++;
            }
        } catch (SQLException e) {
            System.out.println("Error in getAll: " + e.getMessage());
        }
        return data;
    }

    public static String insertInto(Object object, String tableName) {
        Field[] fields = object.getClass().getFields();
        String sql = "INSERT INTO " + tableName + '(' + Helper.columnsPart(fields) + ")VALUES(" + Helper.questionMarksPart(fields) + ')';

        try {
            Constant.pst = Constant.con.prepareStatement(sql);
            for (int i = 0; i < fields.length; i++) {
                Field field = fields[i];
                field.setAccessible(true);
                Object value = field.get(object);
                switch (value) {
                    case String a -> Constant.pst.setString(i + 1, a);
                    case Integer b -> Constant.pst.setInt(i + 1, b);
                    case Float c -> Constant.pst.setFloat(i + 1, c);
                    case null, default -> Constant.pst.setObject(i + 1, value);
                }
            }

            Constant.pst.execute();

        } catch (SQLException e) {
            return "ERROR: INSERTING INTO ".concat(tableName).concat(e.getMessage());
        } catch (IllegalAccessException e) {
            return "ERROR: ACCESSING FIELDS OF THE OBJECT: ".concat(e.getMessage());
        }
        return ("SUCCESSFUL INSERT INTO ".concat(tableName));
    }

    public static void closeConnection() {
        try {
            if (Constant.rsl != null) Constant.rsl.close();
            if (Constant.st != null) Constant.st.close();
            if (Constant.con != null) Constant.con.close();
            System.out.println("Database resources closed successfully.");
        } catch (SQLException e) {
            System.out.println("Error closing database resources: " + e.getMessage());
        }
    }
}
