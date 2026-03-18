package presentation.dash;

import javax.swing.table.TableModel;

public final class TableViewUtils {
    private TableViewUtils() {
    }

    public static String normalizeColumnName(String value) {
        if (value == null) {
            return "";
        }
        return value.toLowerCase().replace("_", "").replace(" ", "").trim();
    }

    public static int findColumnIndex(TableModel model, String columnHint) {
        if (columnHint == null || columnHint.isBlank() || model == null) {
            return -1;
        }
        String normalizedHint = normalizeColumnName(columnHint);
        for (int i = 0; i < model.getColumnCount(); i++) {
            String name = model.getColumnName(i);
            if (name == null) {
                continue;
            }
            String normalizedName = normalizeColumnName(name);
            if (normalizedName.equals(normalizedHint) || normalizedName.contains(normalizedHint)) {
                return i;
            }
        }
        return -1;
    }

    public static String getCellValue(TableModel model, int row, String columnHint) {
        int index = findColumnIndex(model, columnHint);
        if (index < 0) {
            return "";
        }
        Object value = model.getValueAt(row, index);
        return value == null ? "" : value.toString();
    }

    public static Integer parseOptionalIntCell(TableModel model, int row, String columnHint) {
        String value = getCellValue(model, row, columnHint);
        if (value.isBlank()) {
            return null;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static Integer parseRequiredIntCell(TableModel model, int row, String columnHint) {
        String value = getCellValue(model, row, columnHint);
        if (value.isBlank()) {
            return null;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static int findBarcodeColumnIndex(TableModel model) {
        if (model == null) {
            return -1;
        }
        for (int col = 0; col < model.getColumnCount(); col++) {
            String name = model.getColumnName(col);
            if (name != null && name.toLowerCase().contains("barcode")) {
                return col;
            }
        }
        return -1;
    }

    public static String getBarcodeValue(TableModel model, int modelRow) {
        int columnIndex = findBarcodeColumnIndex(model);
        if (columnIndex < 0) {
            return null;
        }
        Object value = model.getValueAt(modelRow, columnIndex);
        return value == null ? null : value.toString();
    }
}
