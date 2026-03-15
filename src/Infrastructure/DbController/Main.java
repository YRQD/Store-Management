package Infrastructure.DbController;

import Infrastructure.Entities.OptionItem;

import static Infrastructure.DbController.Constant.*;

import java.lang.reflect.Field;
import java.sql.*;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;

public class Main {

    public static void startConnection() {
        try {
            con = DriverManager.getConnection(url, user, password);
            st = con.createStatement();
            System.out.println("Connection to the database established successfully.");
        } catch (SQLException e) {
            System.out.println(url);
            System.exit(0);
        }
    }

    public static Object[][] getAll(String tableName, String condition) {
        String sql = "SELECT * FROM " + tableName + " WHERE " + condition;

        int rowsCount = Helper.getRowsNumber(tableName, condition);
        int columnsCount = Helper.getColumnsNumber(tableName);

        if (rowsCount < 0 || columnsCount < 0)
            return new Object[0][0];

        Object[][] data = new Object[rowsCount][columnsCount];
        int j = 0;
        try {
            rsl = st.executeQuery(sql);
            while (rsl.next()) {
                for (int i = 0; i < columnsCount; i++) {
                    data[j][i] = rsl.getString(i + 1);
                }
                j++;
            }
        } catch (SQLException e) {
            System.out.println("Error in getAll: " + e.getMessage());
        }
        return data;
    }

    public static List<OptionItem> getIdName(String tableName, String idColumn, String nameColumn) {
        List<OptionItem> options = new ArrayList<>();
        try {
            rsl = st.executeQuery("SELECT " + idColumn + ", " + nameColumn + " FROM " + tableName + " ORDER BY " + nameColumn);
            while (rsl.next())
                options.add(new OptionItem(rsl.getInt(1), rsl.getString(2)));
            return options;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            throw new RuntimeException();
        }
    }

    public static String insertInto(Object object, String tableName) {
        Field[] fields = object.getClass().getFields();
        String sql = "INSERT INTO " + tableName + '(' + Helper.columnsPart(fields) + ")VALUES(" + Helper.questionMarksPart(fields) + ')';

        try {
            pst = con.prepareStatement(sql);
            for (int i = 0; i < fields.length; i++) {
                Field field = fields[i];
                field.setAccessible(true);
                Object value = field.get(object);
                switch (value) {
                    case String a -> pst.setString(i + 1, a);
                    case Integer b -> pst.setInt(i + 1, b);
                    case Float c -> pst.setFloat(i + 1, c);
                    case null, default -> pst.setObject(i + 1, value);
                }
            }

            pst.execute();

        } catch (SQLException e) {
            return "ERROR: INSERTING INTO ".concat(tableName).concat(e.getMessage());
        } catch (IllegalAccessException e) {
            return "ERROR: ACCESSING FIELDS OF THE OBJECT: ".concat(e.getMessage());
        }
        return ("SUCCESSFUL INSERT INTO ".concat(tableName));
    }

    public static void updateUserLogin(String username) {
        try {
            pst = con.prepareStatement("UPDATE users SET lastlogin = CURRENT_TIMESTAMP WHERE username = ?");
            pst.setString(1, username);
            pst.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error updating user login time: " + e.getMessage());
        }
    }

        public static void closeConnection () {
            try {
                if (rsl != null) rsl.close();
                if (st != null) st.close();
                if (con != null) con.close();
                System.out.println("Database resources closed successfully.");
            } catch (SQLException e) {
                System.out.println("Error closing database resources: " + e.getMessage());
            }
        }
    }
