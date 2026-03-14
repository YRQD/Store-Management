package Presentation;

import Infrastructure.DbController.Constant;
import Infrastructure.DbController.Main;
import Infrastructure.Entities.Category;
import Infrastructure.Entities.Product;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.SQLException;

public class InsertionPanel extends JPanel {
    private static final String UI_FONT_FAMILY = "Times New Roman";
    private static final float UI_FONT_SIZE = 16f;
    private static final float SECTION_TITLE_SIZE = 18f;

    private final JLabel statusLabel = new JLabel(" ");

    private final JTextField categoryNameField = new JTextField(18);
    private final JTextField categoryDescriptionField = new JTextField(18);

    private final JComboBox<OptionItem> productCategoryCombo = new JComboBox<>();
    private final JComboBox<OptionItem> productSupplierCombo = new JComboBox<>();
    private final JTextField productBarcodeField = new JTextField(14);
    private final JButton generateBarcodeButton = new JButton("Generate");
    private final JTextField productPartNameField = new JTextField(14);
    private final JTextField productCostPriceField = new JTextField(10);
    private final JTextField productSellingPriceField = new JTextField(10);
    private final JTextField productStockQuantityField = new JTextField(10);
    private final JTextField productBrandField = new JTextField(12);
    private final JTextField productReorderLevelField = new JTextField(10);
    private final JTextField productLocationField = new JTextField(12);

    private final JButton insertCategoryButton = new JButton("Insert Category");
    private final JButton clearCategoryButton = new JButton("Clear Category");
    private final JButton insertProductButton = new JButton("Insert Product");
    private final JButton clearProductButton = new JButton("Clear Product");

    public InsertionPanel() {
        super(new BorderLayout(12, 12));
        buildLayout();
        styleButtons();
        bindActions();
        loadComboData();
    }

    private void buildLayout() {
        JPanel contentPanel = new JPanel(new GridLayout(1, 2, 16, 0));
        contentPanel.setBorder(new EmptyBorder(12, 12, 12, 12));
        contentPanel.add(buildCategoryPanel());
        contentPanel.add(buildProductPanel());

        statusLabel.setBorder(new EmptyBorder(8, 12, 8, 12));
        statusLabel.setFont(resolveFont(Font.PLAIN, UI_FONT_SIZE, statusLabel.getFont()));

        add(contentPanel, BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);
    }

    private JPanel buildCategoryPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Categories"));

        GridBagConstraints gbc = baseConstraints();
        addField(panel, gbc, 0, "Name", categoryNameField);
        addField(panel, gbc, 1, "Description", categoryDescriptionField);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.LINE_START;
        insertCategoryButton.setFont(resolveFont(Font.BOLD, UI_FONT_SIZE, insertCategoryButton.getFont()));
        clearCategoryButton.setFont(resolveFont(Font.PLAIN, UI_FONT_SIZE, clearCategoryButton.getFont()));
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        actionPanel.add(insertCategoryButton);
        actionPanel.add(clearCategoryButton);
        panel.add(actionPanel, gbc);

        applySectionFont(panel);
        return panel;
    }

    private JPanel buildProductPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Products"));

        GridBagConstraints gbc = baseConstraints();
        int row = 0;
        addField(panel, gbc, row++, "Category", productCategoryCombo);
        addField(panel, gbc, row++, "Supplier", productSupplierCombo);
        addField(panel, gbc, row++, "Barcode/SKU", buildBarcodeField());
        addField(panel, gbc, row++, "Part Name", productPartNameField);
        addField(panel, gbc, row++, "Cost Price", productCostPriceField);
        addField(panel, gbc, row++, "Selling Price", productSellingPriceField);
        addField(panel, gbc, row++, "Stock Quantity", productStockQuantityField);
        addField(panel, gbc, row++, "Brand", productBrandField);
        addField(panel, gbc, row++, "Reorder Level", productReorderLevelField);
        addField(panel, gbc, row, "Location", productLocationField);

        gbc.gridx = 0;
        gbc.gridy = row + 1;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.LINE_START;
        insertProductButton.setFont(resolveFont(Font.BOLD, UI_FONT_SIZE, insertProductButton.getFont()));
        clearProductButton.setFont(resolveFont(Font.PLAIN, UI_FONT_SIZE, clearProductButton.getFont()));
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        actionPanel.add(insertProductButton);
        actionPanel.add(clearProductButton);
        panel.add(actionPanel, gbc);

        applySectionFont(panel);
        return panel;
    }

    private void applySectionFont(JPanel panel) {
        Font sectionFont = resolveFont(Font.BOLD, SECTION_TITLE_SIZE, panel.getFont());
        if (panel.getBorder() instanceof javax.swing.border.TitledBorder titledBorder) {
            titledBorder.setTitleFont(sectionFont);
        }
    }

    private void styleButtons() {
        stylePrimaryButton(insertCategoryButton, new Color(34, 122, 255), new Color(26, 94, 196));
        styleSecondaryButton(clearCategoryButton);
        stylePrimaryButton(insertProductButton, new Color(42, 167, 88), new Color(33, 130, 69));
        styleSecondaryButton(clearProductButton);
        styleOutlineButton(generateBarcodeButton, new Color(112, 88, 208));
    }

    private void stylePrimaryButton(JButton button, Color background, Color border) {
        button.setBackground(background);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(border),
                new EmptyBorder(6, 14, 6, 14)
        ));
        button.setOpaque(true);
    }

    private void styleSecondaryButton(JButton button) {
        button.setBackground(new Color(240, 240, 240));
        button.setForeground(new Color(40, 40, 40));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                new EmptyBorder(6, 14, 6, 14)
        ));
        button.setOpaque(true);
    }

    private void styleOutlineButton(JButton button, Color border) {
        button.setBackground(Color.WHITE);
        button.setForeground(border.darker());
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(border),
                new EmptyBorder(6, 10, 6, 10)
        ));
        button.setOpaque(true);
    }

    private void bindActions() {
        insertCategoryButton.addActionListener(_ -> insertCategory());
        clearCategoryButton.addActionListener(_ -> clearCategoryFields());
        insertProductButton.addActionListener(_ -> insertProduct());
        clearProductButton.addActionListener(_ -> clearProductFields());
        generateBarcodeButton.addActionListener(_ -> statusLabel.setText("Barcode generation is not implemented yet."));
    }

    private void insertCategory() {
        String name = categoryNameField.getText().trim();
        String description = categoryDescriptionField.getText().trim();
        if (name.isEmpty()) {
            statusLabel.setText("Category name is required.");
            return;
        }

        Category category = new Category(name, description);
        String result = Main.insertInto(category, "categories");
        statusLabel.setText(result);
    }

    private void clearCategoryFields() {
        categoryNameField.setText("");
        categoryDescriptionField.setText("");
        statusLabel.setText(" ");
    }

    private void insertProduct() {
        OptionItem categoryItem = (OptionItem) productCategoryCombo.getSelectedItem();
        if (categoryItem == null || categoryItem.id == null) {
            statusLabel.setText("Category is required.");
            return;
        }
        Integer supplierId = null;
        OptionItem supplierItem = (OptionItem) productSupplierCombo.getSelectedItem();
        if (supplierItem != null) {
            supplierId = supplierItem.id;
        }
        String barcode = productBarcodeField.getText().trim();
        if (barcode.isEmpty()) {
            statusLabel.setText("Barcode is required.");
            return;
        }
        String partName = productPartNameField.getText().trim();
        Float costPrice = parseRequiredFloat(productCostPriceField, "Cost Price");
        if (costPrice == null) {
            return;
        }
        Float sellingPrice = parseRequiredFloat(productSellingPriceField, "Selling Price");
        if (sellingPrice == null) {
            return;
        }
        Integer stockQty = parseRequiredInt(productStockQuantityField, "Stock Quantity");
        if (stockQty == null) {
            return;
        }
        String brand = productBrandField.getText().trim();
        Integer reorderLevel = parseRequiredInt(productReorderLevelField, "Reorder Level");
        if (reorderLevel == null) {
            return;
        }
        String location = productLocationField.getText().trim();

        Product product = new Product(
                categoryItem.id,
                supplierId,
                barcode,
                partName,
                costPrice,
                sellingPrice,
                stockQty,
                brand,
                reorderLevel,
                location
        );

        String result = Main.insertInto(product, "products");
        statusLabel.setText(result);
    }

    private void clearProductFields() {
        productCategoryCombo.setSelectedIndex(0);
        productSupplierCombo.setSelectedIndex(0);
        productBarcodeField.setText("");
        productPartNameField.setText("");
        productCostPriceField.setText("");
        productSellingPriceField.setText("");
        productStockQuantityField.setText("");
        productBrandField.setText("");
        productReorderLevelField.setText("");
        productLocationField.setText("");
        statusLabel.setText(" ");
    }

    private GridBagConstraints baseConstraints() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        return gbc;
    }

    private void addField(JPanel panel, GridBagConstraints gbc, int row, String labelText, JTextField field) {
        addField(panel, gbc, row, labelText, (JComponent) field);
    }

    private void addField(JPanel panel, GridBagConstraints gbc, int row, String labelText, JComponent field) {
        JLabel label = new JLabel(labelText + ":");
        label.setFont(resolveFont(Font.PLAIN, UI_FONT_SIZE, label.getFont()));
        field.setFont(resolveFont(Font.PLAIN, UI_FONT_SIZE, field.getFont()));

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.4;
        panel.add(label, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.6;
        panel.add(field, gbc);
    }

    private void loadComboData() {
        productCategoryCombo.setModel(buildOptions("categories", "categoryid", "categoryname", "Select category..."));
        productSupplierCombo.setModel(buildOptions("suppliers", "supplierid", "suppliername", "None"));
    }

    private DefaultComboBoxModel<OptionItem> buildOptions(String table, String idColumn, String nameColumn, String emptyLabel) {
        DefaultComboBoxModel<OptionItem> model = new DefaultComboBoxModel<>();
        model.addElement(new OptionItem(null, emptyLabel));
        try {
            Constant.rsl = Constant.st.executeQuery(
                    "SELECT " + idColumn + ", " + nameColumn + " FROM " + table + " ORDER BY " + nameColumn
            );
            while (Constant.rsl.next()) {
                Integer id = Constant.rsl.getInt(1);
                String name = Constant.rsl.getString(2);
                model.addElement(new OptionItem(id, name));
            }
        } catch (SQLException e) {
            statusLabel.setText("Failed to load " + table + " list: " + e.getMessage());
        }
        return model;
    }

    private record OptionItem(Integer id, String label) {

        @Override
            public String toString() {
                return label;
            }
        }

    private Integer parseRequiredInt(JTextField field, String label) {
        String value = field.getText().trim();
        if (value.isEmpty()) {
            statusLabel.setText(label + " is required.");
            return null;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            statusLabel.setText(label + " must be a number.");
            return null;
        }
    }

    private Float parseRequiredFloat(JTextField field, String label) {
        String value = field.getText().trim();
        if (value.isEmpty()) {
            statusLabel.setText(label + " is required.");
            return null;
        }
        try {
            return Float.parseFloat(value);
        } catch (NumberFormatException e) {
            statusLabel.setText(label + " must be a number.");
            return null;
        }
    }

    private Font resolveFont(int style, float size, Font fallback) {
        Font candidate = new Font(InsertionPanel.UI_FONT_FAMILY, style, Math.round(size));
        if (candidate.getFamily().equalsIgnoreCase(InsertionPanel.UI_FONT_FAMILY)) {
            return candidate.deriveFont(style, size);
        }
        return fallback.deriveFont(style, size);
    }

    private JComponent buildBarcodeField() {
        JPanel panel = new JPanel(new BorderLayout(6, 0));
        panel.add(productBarcodeField, BorderLayout.CENTER);
        generateBarcodeButton.setFont(resolveFont(Font.PLAIN, UI_FONT_SIZE, generateBarcodeButton.getFont()));
        panel.add(generateBarcodeButton, BorderLayout.EAST);
        return panel;
    }
}
