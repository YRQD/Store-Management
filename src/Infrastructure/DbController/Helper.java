package Infrastructure.DbController;

import static Infrastructure.DbController.Constant.*;

import java.lang.reflect.Field;
import java.sql.SQLException;

public class Helper {

    public static int getRowsNumber(String tableName, String condition) {
        try {
            rsl = st.executeQuery("SELECT COUNT(*) FROM " + tableName + " WHERE " + condition);
            rsl.next();
            return rsl.getInt(1);
        } catch (SQLException e) {
            return -1;
        }
    }

    public static int getColumnsNumber(String tableName) {
        try {
            rsl = st.executeQuery("SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = '".concat(tableName).concat("'"));
            rsl.next();
            return rsl.getInt(1);
        } catch (SQLException e) {
            return -1;
        }
    }

    public static String[] getColumnsNames(String tableName) {
        String[] columnsNames = new String[getColumnsNumber(tableName)];
        try {
            rsl = st.executeQuery("SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = '".concat(tableName).concat("' ORDER BY ORDINAL_POSITION;"));
            for (int i = 0; rsl.next(); i++)
                columnsNames[i] = rsl.getString(1).toUpperCase();
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
        try {
            pst = con.prepareStatement("SELECT 1 FROM products WHERE barcode_sku = ? LIMIT 1");
            pst.setString(1, barcode);
            rsl = pst.executeQuery();
            return rsl.next();
        } catch (SQLException e) {
            System.out.println("Error in barcodeExists: " + e.getMessage());
            return false;
        }
    }

    public static boolean userExists(String username, String password) {
        try {
            pst = con.prepareStatement("SELECT passwordhash FROM users WHERE username = ?");
            pst.setString(1, username);
            rsl = pst.executeQuery();
            if (rsl.next()) {
                String hashedPasswordFromDB = rsl.getString(1);
                return PasswordManager.verifyPassword(password, hashedPasswordFromDB);
            }
            return false;
        } catch (SQLException e) {
            return false;
        }
    }

    public static String getUserPermission(String username) {
            try {
            pst = con.prepareStatement("SELECT role FROM users WHERE username = ?");
            pst.setString(1, username);
            rsl = pst.executeQuery();
            if (rsl.next()) {
                String userPermission = rsl.getString(1);
                return userPermission;
            }
            return "No permission found for user: " + username;
        } catch (SQLException e) {
            return "Error in getUserPermission: " + e.getMessage();
        }
    }
}
