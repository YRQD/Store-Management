package Infrastructure.DbController;

import static Infrastructure.DbController.Constant.*;

import java.lang.reflect.Field;
import java.sql.*;

public class Helper {

    public static int getRowsNumber(String tableName, String condition) {
        String sql = "SELECT COUNT(*) FROM " + tableName + " WHERE " + condition;
        try (Statement stmt = con.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            rs.next();
            return rs.getInt(1);
        } catch (SQLException e) {
            return -1;
        }
    }

    public static int getColumnsNumber(String tableName) {
        String sql = "SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = '" + tableName + "'";
        try (Statement stmt = con.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            rs.next();
            return rs.getInt(1);
        } catch (SQLException e) {
            return -1;
        }
    }

    public static String[] getColumnsNames(String tableName) {
        String[] columnsNames = new String[getColumnsNumber(tableName)];
        String sql = "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = '" + tableName + "' ORDER BY ORDINAL_POSITION;";
        try (Statement stmt = con.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            for (int i = 0; rs.next(); i++)
                columnsNames[i] = rs.getString(1).toUpperCase();
            return columnsNames;
        } catch (SQLException e) {
            System.out.println("Error in getColumnsNames: " + e.getMessage());
            return null;
        }
    }

    public static StringBuilder columnsPart(Field[] fields) {
        StringBuilder columnsPart = new StringBuilder();
        for (Field field : fields)
            columnsPart.append(field.getName()).append(",");
        int l = columnsPart.length();
        columnsPart.delete(l - 1, l);
        return columnsPart;
    }

    public static StringBuilder questionMarksPart(Field[] fields) {
        StringBuilder questionMarksPart = new StringBuilder();
        questionMarksPart.append("?,".repeat(fields.length));
        int l = questionMarksPart.length();
        questionMarksPart.delete(l - 1, l);
        return questionMarksPart;
    }

    public static boolean barcodeExists(String barcode) {
        String sql = "SELECT 1 FROM products WHERE barcode_sku = ? LIMIT 1";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, barcode);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.out.println("Error in barcodeExists: " + e.getMessage());
            return false;
        }
    }

    public static boolean existsInTable(String tableName, String columnName, String value) {
        String sql = "SELECT 1 FROM " + tableName + " WHERE LOWER(" + columnName + ") = LOWER(?) LIMIT 1";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, value);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.out.println("Error in existsInTable: " + e.getMessage());
            return false;
        }
    }

    public static boolean userExists(String username, String password) {
        String sql = "SELECT passwordhash FROM users WHERE username = ?";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String hashedPasswordFromDB = rs.getString(1);
                    return PasswordManager.verifyPassword(password, hashedPasswordFromDB);
                }
                return false;
            }
        } catch (SQLException e) {
            return false;
        }
    }

    public static String getUserPermission(String username) {
        String sql = "SELECT role FROM users WHERE username = ?";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString(1);
                }
                return "No permission found for user: " + username;
            }
        } catch (SQLException e) {
            return "Error in getUserPermission: " + e.getMessage();
        }
    }

    public static String getPrimaryKeyName(String tableName) {

        try {
            DatabaseMetaData metaData = con.getMetaData();
            ResultSet rs = metaData.getPrimaryKeys(null, null, tableName.toLowerCase());

            if (rs.next())
                return rs.getString("COLUMN_NAME");
        } catch (Exception e) {
            System.out.println("Error in getPrimaryKeyName: " + e.getMessage());
        }
        return "1";
    }
}
