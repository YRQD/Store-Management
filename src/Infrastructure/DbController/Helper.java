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
        for (Field field : fields)
            questionMarksPart.append("?,");
        int l = questionMarksPart.length();
        questionMarksPart.delete(l - 1, l);
        return questionMarksPart;
    }
}
