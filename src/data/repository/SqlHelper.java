package data.repository;

import infrastructure.security.PasswordManager;

import java.lang.reflect.Field;
import java.sql.*;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import static infrastructure.persistence.DatabaseConnection.con;

public class SqlHelper {

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
                columnsNames[i] = rs.getString(1);
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
        if (tableName == null || tableName.isBlank()) {
            return "1";
        }
        try {
            DatabaseMetaData metaData = con.getMetaData();
            String pk = readPrimaryKey(metaData, tableName);
            if (pk != null) {
                return pk;
            }
            pk = readPrimaryKey(metaData, tableName.toLowerCase());
            if (pk != null) {
                return pk;
            }
            pk = readPrimaryKey(metaData, tableName.toUpperCase());
            if (pk != null) {
                return pk;
            }
        } catch (Exception e) {
            System.out.println("Error in getPrimaryKeyName: " + e.getMessage());
        }

        String fallback = readPrimaryKeyFromInformationSchema(tableName);
        if (fallback != null) {
            return fallback;
        }
        return "1";
    }

    private static String readPrimaryKey(DatabaseMetaData metaData, String tableName) throws SQLException {
        try (ResultSet rs = metaData.getPrimaryKeys(null, null, tableName)) {
            if (rs.next()) {
                return rs.getString("COLUMN_NAME");
            }
        }
        return null;
    }

    private static String readPrimaryKeyFromInformationSchema(String tableName) {
        String sql = "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE " +
                "WHERE TABLE_NAME = ? AND CONSTRAINT_NAME = 'PRIMARY'";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, tableName);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString(1);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error in getPrimaryKeyName fallback: " + e.getMessage());
        }
        return null;
    }

    public static void bindValue(PreparedStatement stmt, int index, Object value) throws SQLException {
        switch (value) {
            case String a -> stmt.setString(index, a);
            case Integer b -> stmt.setInt(index, b);
            case Float c -> stmt.setFloat(index, c);
            case null, default -> stmt.setObject(index, value);
        }
    }

    public static String prepareUpdateSql(String tableName, Set<String> columns, String pkColumn) {
        List<String> setClauses = new ArrayList<>();
        for (String columnName : columns)
            setClauses.add(columnName + " = ?");
        String setString = String.join(", ", setClauses);
        return "UPDATE " + tableName + " SET " + setString + " WHERE " + pkColumn + " = ?";
    }

    public static Map<String, Integer> getColumnTypes(String tableName) {
        Map<String, Integer> types = new HashMap<>();
        try {
            DatabaseMetaData metaData = con.getMetaData();
            try (ResultSet rs = metaData.getColumns(null, null, tableName, null)) {
                while (rs.next()) {
                    types.put(rs.getString("COLUMN_NAME"), rs.getInt("DATA_TYPE"));
                }
            }
        } catch (SQLException e) {
            System.out.println("Error in getColumnTypes: " + e.getMessage());
        }
        return types;
    }
}
