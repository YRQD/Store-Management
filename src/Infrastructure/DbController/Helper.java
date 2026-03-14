package Infrastructure.DbController;

import java.lang.reflect.Field;
import java.sql.SQLException;

public class Helper {

    public static int getRowsNumber(String tableName) {
        try {
            Constant.rsl = Constant.st.executeQuery("SELECT COUNT(*) FROM ".concat(tableName));
            Constant.rsl.next();
            return Constant.rsl.getInt(1);
        } catch (SQLException e) {
            return -1;
        }
    }

    public static int getColumnsNumber(String tableName) {
        try {
            Constant.rsl = Constant.st.executeQuery("SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = '".concat(tableName).concat("'"));
            Constant.rsl.next();
            return Constant.rsl.getInt(1);
        } catch (SQLException e) {
            return -1;
        }
    }

    public static String[] getColumnsNames(String tableName) {
        String[] columnsNames = new String[getColumnsNumber(tableName)];
        try {
            Constant.rsl = Constant.st.executeQuery("SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = '".concat(tableName).concat("' ORDER BY ORDINAL_POSITION;"));
            for (int i = 0; Constant.rsl.next(); i++)
                columnsNames[i] = Constant.rsl.getString(1).toUpperCase();
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
            Constant.pst = Constant.con.prepareStatement("SELECT 1 FROM products WHERE barcode_sku = ? LIMIT 1");
            Constant.pst.setString(1, barcode);
            Constant.rsl = Constant.pst.executeQuery();
            return Constant.rsl.next();
        } catch (SQLException e) {
            System.out.println("Error in barcodeExists: " + e.getMessage());
            return false;
        }
    }
}
