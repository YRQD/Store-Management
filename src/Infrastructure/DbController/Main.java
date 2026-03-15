package Infrastructure.DbController;

import Infrastructure.Entities.OptionItem;
import static Infrastructure.DbController.Constant.*;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.DriverManager;
import java.sql.Statement;

import java.util.List;
import java.util.ArrayList;

public class Main {

    public static void startConnection() {
        try {
            con = DriverManager.getConnection(url, user, password);
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
        try (Statement stmt = con.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                for (int i = 0; i < columnsCount; i++) {
                    data[j][i] = rs.getString(i + 1);
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
        String sql = "SELECT " + idColumn + ", " + nameColumn + " FROM " + tableName + " ORDER BY " + nameColumn;
        try (Statement stmt = con.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next())
                options.add(new OptionItem(rs.getInt(1), rs.getString(2)));
            return options;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            throw new RuntimeException();
        }
    }

    public static String insertInto(Object object, String tableName) {
        Field[] fields = object.getClass().getFields();
        String sql = "INSERT INTO " + tableName + '(' + Helper.columnsPart(fields) + ")VALUES(" + Helper.questionMarksPart(fields) + ')';

        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            for (int i = 0; i < fields.length; i++) {
                Field field = fields[i];
                field.setAccessible(true);
                Object value = field.get(object);
                switch (value) {
                    case String a -> stmt.setString(i + 1, a);
                    case Integer b -> stmt.setInt(i + 1, b);
                    case Float c -> stmt.setFloat(i + 1, c);
                    case null, default -> stmt.setObject(i + 1, value);
                }
            }

            stmt.execute();

        } catch (SQLException e) {
            return "ERROR: INSERTING INTO ".concat(tableName).concat(e.getMessage());
        } catch (IllegalAccessException e) {
            return "ERROR: ACCESSING FIELDS OF THE OBJECT: ".concat(e.getMessage());
        }
        return ("SUCCESSFUL INSERT INTO ".concat(tableName));
    }

    public static void updateUserLogin(String username) {
        String sql = "UPDATE users SET lastlogin = CURRENT_TIMESTAMP WHERE username = ?";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error updating user login time: " + e.getMessage());
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
