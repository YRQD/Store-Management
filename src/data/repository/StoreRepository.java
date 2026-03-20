package data.repository;

import domain.OptionItem;
import domain.Product;
import static infrastructure.persistence.DatabaseConnection.con;

import java.lang.reflect.Field;
import java.sql.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

public class StoreRepository {

    public static Object[][] getAll(String tableName, String condition) {
        String primaryKeyColumn = SqlHelper.getPrimaryKeyName(tableName);
        String sql = "SELECT * FROM " + tableName + " WHERE " + condition + " ORDER BY " + primaryKeyColumn;

        int rowsCount = SqlHelper.getRowsNumber(tableName, condition);
        int columnsCount = SqlHelper.getColumnsNumber(tableName);

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
        String sql = "INSERT INTO " + tableName + '(' + SqlHelper.columnsPart(fields) + ")VALUES(" + SqlHelper.questionMarksPart(fields) + ')';

        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            for (int i = 0; i < fields.length; i++) {
                Field field = fields[i];
                field.setAccessible(true);
                Object value = field.get(object);
                SqlHelper.bindValue(stmt, i + 1, value);
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
        String sql = "UPDATE users SET LASTLOGIN = CURRENT_TIMESTAMP WHERE USERNAME = ?";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error updating user login time: " + e.getMessage());
        }
    }

    public static String updateProduct(Product product, int productId, boolean isActive) {
        String sql = "UPDATE products SET categoryid = ?, supplierid = ?, partname = ?, costprice = ?, sellingprice = ?, " +
                "stockquantity = ?, brand = ?, reorderlevel = ?, location = ?, isactive = ? WHERE productid = ?";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, product.categoryid);
            if (product.supplierid == null) {
                stmt.setNull(2, Types.INTEGER);
            } else {
                stmt.setInt(2, product.supplierid);
            }
            stmt.setString(3, product.partname);
            stmt.setFloat(4, product.costprice);
            stmt.setFloat(5, product.sellingprice);
            stmt.setInt(6, product.stockquantity);
            stmt.setString(7, product.brand);
            stmt.setInt(8, product.reorderlevel);
            stmt.setString(9, product.location);
            stmt.setBoolean(10, isActive);
            stmt.setInt(11, productId);

            int rows = stmt.executeUpdate();
            if (rows > 0) {
                return "SUCCESSFUL UPDATE FOR productid=" + productId;
            }
            return "No rows updated for productid=" + productId;
        } catch (SQLException e) {
            return "ERROR: UPDATING PRODUCT " + e.getMessage();
        }
    }

    public static boolean update(String tableName, Map<String, Object> updateData, Object pkValue) {
        if (updateData == null || updateData.isEmpty() || pkValue == null)
            return false;

        String pkColumn = SqlHelper.getPrimaryKeyName(tableName);
        if (pkColumn.isBlank() || "1".equals(pkColumn))
            return false;

        Map<String, Object> updates = new LinkedHashMap<>(updateData);
        updates.remove(pkColumn);
        if (updates.isEmpty())
            return false;

        List<String> columns = new ArrayList<>(updates.keySet());
        String sql = SqlHelper.prepareUpdateSql(tableName, new LinkedHashSet<>(columns), pkColumn);

        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            int index = 1;
            for (Object value : updates.values())
                SqlHelper.bindValue(stmt, index++, value);
            SqlHelper.bindValue(stmt, index, pkValue);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating table: " + tableName);
            return false;
        }
    }
}
