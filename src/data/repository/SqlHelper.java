package data.repository;

import infrastructure.security.PasswordManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.sql.*;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;


import static infrastructure.persistence.DatabaseConnection.con;

public class SqlHelper {

    private static final Logger log = LoggerFactory.getLogger(SqlHelper.class);

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
        String sql = "SELECT 1 FROM products WHERE barcode = ? LIMIT 1";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, barcode);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            log.error("Error in barcodeExists: {}", e.getMessage());
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
            log.error("Error in existsInTable: {}", e.getMessage());
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
                log.warn("User: {} attempted to login with Password: {}", username, password);
                return false;
            }
        } catch (SQLException e) {
            log.error("Error in userExists: {}", e.getMessage());
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
            log.error("Error in getUserPermission: {}", e.getMessage());
            return "Error in getUserPermission: " + e.getMessage();
        }
    }

    public static String getPrimaryKeyName(String tableName) {
        if (tableName == null || tableName.isBlank()) {
            return "1";
        }

        String upperName = tableName.toUpperCase();
        switch (upperName) {
            case "CATEGORIES" -> {
                return "CATEGORYID";
            }
            case "SUPPLIERS" -> {
                return "SUPPLIERID";
            }
            case "PRODUCTS" -> {
                return "PRODUCTID";
            }
            case "USERS" -> {
                return "USERID";
            }
        }

        try (ResultSet rs = con.getMetaData().getPrimaryKeys(null, null, upperName)) {
            if (rs.next()) {
                return rs.getString("COLUMN_NAME");
            }
        } catch (Exception e) {
            log.error("Failed to getPrimaryKeyName: {}", e.getMessage());
        }
        return "1";
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
            log.error("Filed to getColumnTypes: {}", e.getMessage());
        }
        return types;
    }
}
