package presentation.dash;

import data.repository.SqlHelper;
import data.repository.StoreRepository;
import domain.Category;
import domain.OptionItem;
import domain.Product;

import java.util.List;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

import presentation.theme.UiTheme;
import static presentation.theme.UiTheme.*;
import static presentation.theme.UiLayout.*;
import static presentation.util.Validators.*;

public class InsertionPanel extends JPanel {
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
    private final JComboBox<String> productLocationCombo = new JComboBox<>(new String[] {"Storage", "Shop"});

    private final JButton insertCategoryButton = new JButton("Insert Category");
    private final JButton clearCategoryButton = new JButton("Clear Category");
    private final JButton insertProductButton = new JButton("Insert Product");
    private final JButton clearProductButton = new JButton("Clear Product");
    private final Runnable onCategoryInserted;

    public InsertionPanel(Runnable onCategoryInserted) {
        super(new BorderLayout(12, 12));
        this.onCategoryInserted = onCategoryInserted;
        setBackground(APP_BG);
        buildLayout();
        styleButtons();
        styleInputs();
        bindActions();
        loadComboData();
    }

    private void buildLayout() {
        JPanel contentPanel = new JPanel(new GridLayout(1, 2, 16, 0));
        contentPanel.setBackground(APP_BG);
        contentPanel.setBorder(new EmptyBorder(12, 12, 12, 12));
        contentPanel.add(wrapInCard(buildCategoryPanel()));
        contentPanel.add(wrapInCard(buildProductPanel()));

        statusLabel.setBorder(new EmptyBorder(8, 12, 8, 12));
        statusLabel.setFont(resolveFont(Font.PLAIN, UI_FONT_SIZE, statusLabel.getFont()));
        statusLabel.setForeground(TEXT_MUTED);

        add(contentPanel, BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);
    }

    private JPanel buildCategoryPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(CARD_BG);
        panel.setBorder(BorderFactory.createTitledBorder("Categories"));

        GridBagConstraints gbc = baseConstraints();
        addLabeledField(panel, gbc, 0, "Name", categoryNameField, UI_FONT_SIZE);
        addLabeledField(panel, gbc, 1, "Description", categoryDescriptionField, UI_FONT_SIZE);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.LINE_START;
        insertCategoryButton.setFont(resolveFont(Font.BOLD, UI_FONT_SIZE, insertCategoryButton.getFont()));
        clearCategoryButton.setFont(resolveFont(Font.PLAIN, UI_FONT_SIZE, clearCategoryButton.getFont()));
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        actionPanel.setBackground(CARD_BG);
        actionPanel.add(insertCategoryButton);
        actionPanel.add(clearCategoryButton);
        panel.add(actionPanel, gbc);

        applySectionTitleFont(panel, SECTION_TITLE_SIZE);
        return panel;
    }

    private JPanel buildProductPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(CARD_BG);
        panel.setBorder(BorderFactory.createTitledBorder("Products"));

        GridBagConstraints gbc = baseConstraints();
        int row = 0;
        addLabeledField(panel, gbc, row++, "Category", productCategoryCombo, UI_FONT_SIZE);
        addLabeledField(panel, gbc, row++, "Supplier", productSupplierCombo, UI_FONT_SIZE);
        addLabeledField(panel, gbc, row++, "Barcode/SKU", buildBarcodeField(), UI_FONT_SIZE);
        addLabeledField(panel, gbc, row++, "Part Name", productPartNameField, UI_FONT_SIZE);
        addLabeledField(panel, gbc, row++, "Cost Price", productCostPriceField, UI_FONT_SIZE);
        addLabeledField(panel, gbc, row++, "Selling Price", productSellingPriceField, UI_FONT_SIZE);
        addLabeledField(panel, gbc, row++, "Stock Quantity", productStockQuantityField, UI_FONT_SIZE);
        addLabeledField(panel, gbc, row++, "Brand", productBrandField, UI_FONT_SIZE);
        addLabeledField(panel, gbc, row++, "Reorder Level", productReorderLevelField, UI_FONT_SIZE);
        addLabeledField(panel, gbc, row, "Location", productLocationCombo, UI_FONT_SIZE);

        gbc.gridx = 0;
        gbc.gridy = row + 1;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.LINE_START;
        insertProductButton.setFont(resolveFont(Font.BOLD, UI_FONT_SIZE, insertProductButton.getFont()));
        clearProductButton.setFont(resolveFont(Font.PLAIN, UI_FONT_SIZE, clearProductButton.getFont()));
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        actionPanel.setBackground(CARD_BG);
        actionPanel.add(insertProductButton);
        actionPanel.add(clearProductButton);
        panel.add(actionPanel, gbc);

        applySectionTitleFont(panel, SECTION_TITLE_SIZE);
        return panel;
    }

    private void styleButtons() {
        stylePrimaryButton(insertCategoryButton, PRIMARY);
        styleSecondaryButton(clearCategoryButton);
        styleSuccessButton(insertProductButton, SUCCESS);
        styleSecondaryButton(clearProductButton);
        styleOutlineButton(generateBarcodeButton, PRIMARY);
    }

    private void styleInputs() {
        styleInput(categoryNameField);
        styleInput(categoryDescriptionField);
        styleInput(productCategoryCombo);
        styleInput(productSupplierCombo);
        styleInput(productBarcodeField);
        styleInput(productPartNameField);
        styleInput(productCostPriceField);
        styleInput(productSellingPriceField);
        styleInput(productStockQuantityField);
        styleInput(productBrandField);
        styleInput(productReorderLevelField);
        styleInput(productLocationCombo);
    }

    private void bindActions() {
        insertCategoryButton.addActionListener(_ -> insertCategory());
        clearCategoryButton.addActionListener(_ -> clearCategoryFields());
        insertProductButton.addActionListener(_ -> insertProduct());
        clearProductButton.addActionListener(_ -> clearProductFields());
        generateBarcodeButton.addActionListener(_ -> generateBarcode());
    }

    private void generateBarcode() {
        String barcode = infrastructure.printing.PrinterService.generateRandomCode();
        productBarcodeField.setText(barcode);
    }

    private void insertCategory() {
        String name = categoryNameField.getText().trim();
        String description = categoryDescriptionField.getText().trim();
        if (name.isEmpty()) {
            statusLabel.setText("Category name is required.");
            return;
        }
        if (SqlHelper.existsInTable("categories", "categoryname", name)) {
            statusLabel.setText("Category already exists.");
            return;
        }

        Category category = new Category(name, description);
        String result = StoreRepository.insertInto(category, "categories");
        loadComboData();
        clearCategoryFields();
        statusLabel.setText(result);
        if (onCategoryInserted != null) {
            onCategoryInserted.run();
        }
    }

    private void clearCategoryFields() {
        categoryNameField.setText("");
        categoryDescriptionField.setText("");
        statusLabel.setText(" ");
    }

    private void insertProduct() {
        OptionItem categoryItem = (OptionItem) productCategoryCombo.getSelectedItem();
        if (categoryItem == null || categoryItem.id() == null) {
            statusLabel.setText("Category is required.");
            return;
        }
        Integer supplierId = null;
        OptionItem supplierItem = (OptionItem) productSupplierCombo.getSelectedItem();
        if (supplierItem != null) {
            supplierId = supplierItem.id();
        }
        String barcode = productBarcodeField.getText().trim();
        if (barcode.isEmpty()) {
            statusLabel.setText("Barcode is required.");
            return;
        }
        if (SqlHelper.barcodeExists(barcode)) {
            statusLabel.setText("Barcode already exists. Please generate a new one.");
            return;
        }
        String partName = productPartNameField.getText().trim();
        Float costPrice = requireFloat(productCostPriceField.getText(), "Cost Price", statusLabel::setText);
        if (costPrice == null) {
            return;
        }
        Float sellingPrice = requireFloat(productSellingPriceField.getText(), "Selling Price", statusLabel::setText);
        if (sellingPrice == null) {
            return;
        }
        Integer stockQty = requireInt(productStockQuantityField.getText(), "Stock Quantity", statusLabel::setText);
        if (stockQty == null) {
            return;
        }
        String brand = productBrandField.getText().trim();
        Integer reorderLevel = requireInt(productReorderLevelField.getText(), "Reorder Level", statusLabel::setText);
        if (reorderLevel == null) {
            return;
        }
        String location = (String) productLocationCombo.getSelectedItem();

        Product product = new Product(
                categoryItem.id(),
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

        String result = StoreRepository.insertInto(product, "PRODUCTS");
        productBarcodeField.setText("");
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
        productLocationCombo.setSelectedIndex(0);
        statusLabel.setText(" ");
    }


    private void loadComboData() {
        productCategoryCombo.setModel(buildOptions("categories", "categoryid", "categoryname", "Select category..."));
        productSupplierCombo.setModel(buildOptions("suppliers", "supplierid", "suppliername", "None"));
    }

    private DefaultComboBoxModel<OptionItem> buildOptions(String table, String idColumn, String nameColumn, String emptyLabel) {
        DefaultComboBoxModel<OptionItem> model = new DefaultComboBoxModel<>();
        List<OptionItem> options = StoreRepository.getIdName(table, idColumn, nameColumn);

        model.addElement(new OptionItem(null, emptyLabel));
        try {
            for (OptionItem option : options)
                model.addElement(option);
        } catch (RuntimeException e) {
            statusLabel.setText("Failed to load " + table + " list: " + e.getMessage());
        }
        return model;
    }


    private Font resolveFont(int style, float size, Font fallback) {
        return UiTheme.resolveFont(style, size, fallback);
    }

    private JComponent buildBarcodeField() {
        JPanel panel = new JPanel(new BorderLayout(6, 0));
        panel.setBackground(CARD_BG);
        panel.add(productBarcodeField, BorderLayout.CENTER);
        generateBarcodeButton.setFont(resolveFont(Font.PLAIN, UI_FONT_SIZE, generateBarcodeButton.getFont()));
        panel.add(generateBarcodeButton, BorderLayout.EAST);
        return panel;
    }
}
