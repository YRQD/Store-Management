package data.repository;

import domain.OptionItem;
import domain.Product;
import domain.TableResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static infrastructure.persistence.DatabaseConnection.con;

import java.lang.reflect.Field;
import java.sql.*;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

public class StoreRepository {

    private static final Logger log = LoggerFactory.getLogger(StoreRepository.class);

    public static TableResult getAllWithColumns(String tableName, String condition) {
        String primaryKeyColumn = SqlHelper.getPrimaryKeyName(tableName);
        String sql = "SELECT * FROM " + tableName + " WHERE " + condition + " ORDER BY " + primaryKeyColumn;

        List<Object[]> rows = new ArrayList<>();
        String[] columns;

        try (Statement stmt = con.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            ResultSetMetaData metaData = rs.getMetaData();
            int columnsCount = metaData.getColumnCount();

            columns = new String[columnsCount];
            for (int i = 0; i < columnsCount; i++) {
                columns[i] = metaData.getColumnName(i + 1);
            }

            while (rs.next()) {
                Object[] row = new Object[columnsCount];
                for (int i = 0; i < columnsCount; i++) {
                    row[i] = rs.getObject(i + 1);
                }
                rows.add(row);
            }
        } catch (SQLException e) {
            log.error("Failed to getAllWithColumns: {}", e.getMessage());
            return new TableResult(new Object[0][0], new String[0]);
        }

        Object[][] data = new Object[rows.size()][columns.length];
        return new TableResult(rows.toArray(data), columns);
    }

    public static TableResult getProductsBrief(String tableName, String userCondition) {
        String primaryKeyColumn = SqlHelper.getPrimaryKeyName(tableName);
        String sql = "SELECT BARCODE, NAME, SELL, SHOP, BRAND FROM PRODUCTS WHERE ISACTIVE = TRUE AND SHOP > 0 AND (" + userCondition + ") ORDER BY " + primaryKeyColumn;

        List<Object[]> rows = new ArrayList<>();
        String[] columns;

        try (Statement stmt = con.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            ResultSetMetaData metaData = rs.getMetaData();
            int columnsCount = metaData.getColumnCount();

            columns = new String[columnsCount];
            for (int i = 0; i < columnsCount; i++) {
                columns[i] = metaData.getColumnName(i + 1);
            }

            while (rs.next()) {
                Object[] row = new Object[columnsCount];
                for (int i = 0; i < columnsCount; i++) {
                    row[i] = rs.getObject(i + 1);
                }
                rows.add(row);
            }
        } catch (SQLException e) {
            log.error("Failed to get products br: {}", e.getMessage());
            return new TableResult(new Object[0][0], new String[0]);
        }

        Object[][] data = new Object[rows.size()][columns.length];
        return new TableResult(rows.toArray(data), columns);
    }

    public static List<OptionItem> getIdName(String tableName, String idColumn, String nameColumn) {
        List<OptionItem> options = new ArrayList<>();
        String sql = "SELECT " + idColumn + ", " + nameColumn + " FROM " + tableName + " ORDER BY " + nameColumn;
        try (Statement stmt = con.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next())
                options.add(new OptionItem(rs.getInt(1), rs.getString(2)));
            return options;
        } catch (SQLException e) {
            log.error("Failed to getIdName: {}", e.getMessage());
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
            log.error("Failed to insertInto: {}", e.getMessage());
            return "ERROR: INSERTING INTO ".concat(tableName);
        } catch (IllegalAccessException e) {
            log.error("Failed to access fields in insertInto: {}", e.getMessage());
            return "ERROR: ACCESSING FIELDS OF THE OBJECT: ";
        }
        return ("SUCCESSFUL INSERT INTO ".concat(tableName));
    }

    public static void updateUserLogin(String username) {
        String sql = "UPDATE users SET LASTLOGIN = CURRENT_TIMESTAMP WHERE USERNAME = ?";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.executeUpdate();
            log.info("INFO: {} last login time: {}", username, new Timestamp(System.currentTimeMillis()));
        } catch (SQLException e) {
            log.error("ERROR: Failed to update user login time for {}: {}", username, e.getMessage());
        }
    }

    public static String updateProduct(Product product, int productId, boolean isActive) {
        String sql = "UPDATE products SET categoryid = ?, supplierid = ?, name = ?, cost = ?, sell = ?, " +
                "storage = ?, shop = ?, brand = ?, reorder = ?, isactive = ? WHERE productid = ?";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, product.categoryid);
            if (product.supplierid == null) {
                stmt.setNull(2, Types.INTEGER);
            } else {
                stmt.setInt(2, product.supplierid);
            }
            stmt.setString(3, product.name);
            stmt.setFloat(4, product.cost);
            stmt.setFloat(5, product.sell);
            stmt.setInt(6, product.storage);
            stmt.setInt(7, product.shop);
            stmt.setString(8, product.brand);
            stmt.setInt(9, product.reorder);
            stmt.setBoolean(10, isActive);
            stmt.setInt(11, productId);

            int rows = stmt.executeUpdate();
            if (rows > 0) {
                log.info("Product with ID {} updated successfully.", productId);
                return "SUCCESSFUL UPDATE FOR productid=" + productId;
            }
            log.warn("No rows updated for productid={}", productId);
            return "WARN: No rows updated for productid=" + productId;
        } catch (SQLException e) {
            log.error("Failed to update product with ID {}: {}", productId, e.getMessage());
            return "ERROR: UPDATING PRODUCT " + e.getMessage();
        }
    }

    public static String update(String tableName, Map<String, Object> updateData, Object pkValue) {
        if (updateData == null || updateData.isEmpty() || pkValue == null) {
            log.error("Invalid update parameters: updateData is null or empty, or pkValue is null.");
            return "ERROR: No data provided for update or primary key value is null.";
        }

        String pkColumn = SqlHelper.getPrimaryKeyName(tableName);
        if (pkColumn.isBlank() || "1".equals(pkColumn)) {
            log.error("Unable to determine primary key for table {}. Received pkColumn: '{}'", tableName, pkColumn);
            return "ERROR: Unable to determine primary key for table " + tableName;
        }

        List<String> columns = new ArrayList<>(updateData.keySet());
        String sql = SqlHelper.prepareUpdateSql(tableName, new LinkedHashSet<>(columns), pkColumn);
        updateData.remove(pkColumn);

        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            int index = 1;
            for (Object value : updateData.values())
                SqlHelper.bindValue(stmt, index++, value);
            SqlHelper.bindValue(stmt, index, pkValue);

            if (stmt.executeUpdate() > 0) {
                return "SUCCESSFUL UPDATE FOR " + tableName + " WITH " + pkColumn + "=" + pkValue;
            } else {
                log.warn("No rows updated for table {} with primary key value {}", tableName, pkValue);
                return "NO ROW UPDATED FOR " + tableName + " WITH " + pkColumn + "=" + pkValue;
            }

        } catch (SQLException e) {
            System.err.println("Error updating table: " + tableName);
            log.error("Failed to update table {}: {}", tableName, e.getMessage());
            return "ERROR: UPDATING TABLE " + tableName + " WITH " + pkColumn + "=" + pkValue;
        }
    }

    public static boolean applyPercentageMarkup(double percentage, Integer categoryId, boolean updateCost, boolean updateSelling) {
        if (!updateCost && !updateSelling) {
            log.warn("Apply markup called with no target columns selected.");
            return false;
        }

        if (percentage <= -100.0) {
            log.error("SECURITY BLOCK: Attempted to discount products by {}%, which would result in negative prices!", percentage);
            return false;
        }

        double multiplier = 1.0 + (percentage / 100.0);
        StringBuilder sql = new StringBuilder("UPDATE PRODUCTS SET ");
        List<String> setClauses = new ArrayList<>();
        if (updateCost) {
            setClauses.add("cost = cost * ?");
        }
        if (updateSelling) {
            setClauses.add("sell = sell * ?");
        }
        sql.append(String.join(", ", setClauses));

        if (categoryId != null) {
            sql.append(" WHERE categoryid = ?");
        }

        try (PreparedStatement stmt = con.prepareStatement(sql.toString())) {
            int paramIndex = 1;

            if (updateCost) {
                stmt.setDouble(paramIndex++, multiplier);
            }
            if (updateSelling) {
                stmt.setDouble(paramIndex++, multiplier);
            }

            if (categoryId != null) {
                stmt.setInt(paramIndex, categoryId);
            }

            int rowsAffected = stmt.executeUpdate();
            if (categoryId == null) {
                log.info("Mass Price Update: Adjusted prices by {}%. ({} items updated)", percentage, rowsAffected);
            } else {
                log.info("Category Price Update: Adjusted category {} prices by {}%. ({} items updated)", categoryId, percentage, rowsAffected);
            }
            return true;

        } catch (SQLException e) {
            log.error("Failed to apply {}% markup to category {}: {}", percentage, categoryId, e.getMessage());
            return false;
        }
    }

    public static List<String> getLowStockProducts() {
        List<String> alerts = new ArrayList<>();
        String sql = "SELECT NAME, STORAGE, REORDER FROM PRODUCTS WHERE STORAGE <= REORDER AND ISACTIVE = TRUE";

        try (Statement stmt = con.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String name = rs.getString("NAME");
                int storage = rs.getInt("STORAGE");
                int reorder = rs.getInt("REORDER");
                alerts.add(String.format("%s (Storage: %d, Reorder at: %d)", name, storage, reorder));
            }
        } catch (SQLException e) {
            log.error("Failed to check low stock products: {}", e.getMessage());
        }
        return alerts;
    }
}
