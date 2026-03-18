package presentation.dash;

import domain.OptionItem;
import presentation.theme.UiTheme;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

import static presentation.theme.UiTheme.*;
import static presentation.theme.UiLayout.*;
import static presentation.util.Validators.*;

public final class TableViewDialogs {
    private TableViewDialogs() {
    }

    public static PrintOptions showPrintOptionsDialog(Component parent, float fontSize) {
        JSpinner copiesSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 20, 1));
        JCheckBox evenLabelCheck = new JCheckBox("Label type is even");
        copiesSpinner.setFont(UiTheme.resolveFont(Font.PLAIN, fontSize, copiesSpinner.getFont()));
        evenLabelCheck.setFont(UiTheme.resolveFont(Font.PLAIN, fontSize, evenLabelCheck.getFont()));
        evenLabelCheck.setForeground(TEXT_BODY);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(CARD_BG);
        GridBagConstraints gbc = baseConstraints();

        JLabel copiesLabel = new JLabel("Copies:");
        copiesLabel.setFont(UiTheme.resolveFont(Font.PLAIN, fontSize, copiesLabel.getFont()));
        copiesLabel.setForeground(TEXT_BODY);

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(copiesLabel, gbc);
        gbc.gridx = 1;
        panel.add(copiesSpinner, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        panel.add(evenLabelCheck, gbc);

        int result = JOptionPane.showConfirmDialog(
                parent,
                panel,
                "Print Barcode",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );
        if (result != JOptionPane.OK_OPTION) {
            return null;
        }
        int copies = (int) copiesSpinner.getValue();
        boolean evenLabel = evenLabelCheck.isSelected();
        return new PrintOptions(copies, evenLabel);
    }

    public static ProductEditResult showEditProductDialog(Component parent,
                                                          DefaultTableModel model,
                                                          int modelRow,
                                                          String barcode,
                                                          int productId,
                                                          float fontSize,
                                                          JComboBox<OptionItem> categoryCombo,
                                                          JComboBox<OptionItem> supplierCombo) {
        JTextField productIdField = buildReadOnlyField(String.valueOf(productId), fontSize);
        JTextField barcodeField = buildReadOnlyField(barcode == null ? "" : barcode, fontSize);

        JTextField partNameField = new JTextField(TableViewUtils.getCellValue(model, modelRow, "partname"), 14);
        JTextField costPriceField = new JTextField(TableViewUtils.getCellValue(model, modelRow, "costprice"), 10);
        JTextField sellingPriceField = new JTextField(TableViewUtils.getCellValue(model, modelRow, "sellingprice"), 10);
        JTextField stockQtyField = new JTextField(TableViewUtils.getCellValue(model, modelRow, "stockquantity"), 10);
        JTextField brandField = new JTextField(TableViewUtils.getCellValue(model, modelRow, "brand"), 12);
        JTextField reorderLevelField = new JTextField(TableViewUtils.getCellValue(model, modelRow, "reorderlevel"), 10);
        JComboBox<String> locationCombo = new JComboBox<>(new String[] {"Storage", "Shop"});
        String locationValue = TableViewUtils.getCellValue(model, modelRow, "location");
        if (locationValue != null && !locationValue.isBlank()) {
            locationCombo.setSelectedItem(locationValue);
        }
        JCheckBox activeCheck = new JCheckBox("Active");
        String activeValue = TableViewUtils.getCellValue(model, modelRow, "isactive");
        activeCheck.setSelected(parseBoolean(activeValue));

        applyFont(fontSize,
                partNameField,
                costPriceField,
                sellingPriceField,
                stockQtyField,
                brandField,
                reorderLevelField,
                categoryCombo,
                supplierCombo,
                locationCombo,
                activeCheck
        );
        styleInput(categoryCombo);
        styleInput(supplierCombo);
        styleInput(partNameField);
        styleInput(costPriceField);
        styleInput(sellingPriceField);
        styleInput(stockQtyField);
        styleInput(brandField);
        styleInput(reorderLevelField);
        styleInput(locationCombo);
        styleInput(activeCheck);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(CARD_BG);
        GridBagConstraints gbc = baseConstraints();

        int row = 0;
        addLabeledField(panel, gbc, row++, "Product ID", productIdField, fontSize);
        addLabeledField(panel, gbc, row++, "Barcode/SKU", barcodeField, fontSize);
        addLabeledField(panel, gbc, row++, "Category", categoryCombo, fontSize);
        addLabeledField(panel, gbc, row++, "Supplier", supplierCombo, fontSize);
        addLabeledField(panel, gbc, row++, "Part Name", partNameField, fontSize);
        addLabeledField(panel, gbc, row++, "Cost Price", costPriceField, fontSize);
        addLabeledField(panel, gbc, row++, "Selling Price", sellingPriceField, fontSize);
        addLabeledField(panel, gbc, row++, "Stock Quantity", stockQtyField, fontSize);
        addLabeledField(panel, gbc, row++, "Brand", brandField, fontSize);
        addLabeledField(panel, gbc, row++, "Reorder Level", reorderLevelField, fontSize);
        addLabeledField(panel, gbc, row++, "Location", locationCombo, fontSize);
        addLabeledField(panel, gbc, row, "Active", activeCheck, fontSize);

        int result = JOptionPane.showConfirmDialog(
                parent,
                panel,
                "Edit Product",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );
        if (result != JOptionPane.OK_OPTION) {
            return null;
        }

        OptionItem categoryItem = (OptionItem) categoryCombo.getSelectedItem();
        if (categoryItem == null || categoryItem.id() == null) {
            showWarning(parent, "Category is required.");
            return null;
        }
        OptionItem supplierItem = (OptionItem) supplierCombo.getSelectedItem();
        Integer selectedSupplierId = supplierItem == null ? null : supplierItem.id();

        String partName = requireText(partNameField.getText(), "Part Name", message -> showWarning(parent, message));
        if (partName == null) {
            return null;
        }
        Float costPrice = requireFloat(costPriceField.getText(), "Cost Price", message -> showWarning(parent, message));
        if (costPrice == null) {
            return null;
        }
        Float sellingPrice = requireFloat(sellingPriceField.getText(), "Selling Price", message -> showWarning(parent, message));
        if (sellingPrice == null) {
            return null;
        }
        Integer stockQty = requireInt(stockQtyField.getText(), "Stock Quantity", message -> showWarning(parent, message));
        if (stockQty == null) {
            return null;
        }
        String brand = brandField.getText().trim();
        Integer reorderLevel = requireInt(reorderLevelField.getText(), "Reorder Level", message -> showWarning(parent, message));
        if (reorderLevel == null) {
            return null;
        }
        String location = (String) locationCombo.getSelectedItem();
        boolean isActive = activeCheck.isSelected();

        return new ProductEditResult(
                categoryItem.id(),
                selectedSupplierId,
                partName,
                costPrice,
                sellingPrice,
                stockQty,
                brand,
                reorderLevel,
                location,
                isActive
        );
    }

    public static void showWarning(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Notice", JOptionPane.WARNING_MESSAGE);
    }

    public static boolean parseBoolean(String value) {
        if (value == null) {
            return false;
        }
        String normalized = value.trim();
        return normalized.equalsIgnoreCase("true")
                || normalized.equalsIgnoreCase("t")
                || normalized.equals("1")
                || normalized.equalsIgnoreCase("yes");
    }

    private static JTextField buildReadOnlyField(String value, float fontSize) {
        JTextField field = new JTextField(value, 14);
        field.setEditable(false);
        field.setForeground(TEXT_MUTED);
        field.setBackground(SECONDARY_BG);
        field.setBorder(new LineBorder(INPUT_BORDER));
        field.setFont(UiTheme.resolveFont(Font.PLAIN, fontSize, field.getFont()));
        return field;
    }

    public record PrintOptions(int copies, boolean evenLabel) {
    }

    public record ProductEditResult(int categoryId,
                                    Integer supplierId,
                                    String partName,
                                    float costPrice,
                                    float sellingPrice,
                                    int stockQuantity,
                                    String brand,
                                    int reorderLevel,
                                    String location,
                                    boolean isActive) {
    }
}
