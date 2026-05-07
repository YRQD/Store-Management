package presentation.dash;

import data.repository.SqlHelper;
import data.repository.StoreRepository;
import domain.Category;
import domain.OptionItem;
import domain.Product;
import domain.Supplier;

import java.util.List;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

import presentation.theme.UiTheme;
import presentation.util.Validators;

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
    private final JTextField productStorageQuantityField = new JTextField(10);
    private final JTextField productShopQuantityField = new JTextField(10);
    private final JTextField productBrandField = new JTextField(10);
    private final JTextField productReorderLevelField = new JTextField(10);

    private final JTextField supplierNameField = new JTextField(18);
    private final JTextField supplierPhoneField = new JTextField(18);
    private final JTextField supplierAddressField = new JTextField(18);
    private final JTextField supplierDescriptionField = new JTextField(18);

    private final String INSERT_TEXT = "Insert";
    private final String CLEAR_TEXT = "Clear";

    private final JButton insertCategoryButton = new JButton(INSERT_TEXT);
    private final JButton clearCategoryButton = new JButton(CLEAR_TEXT);
    private final JButton insertProductButton = new JButton(INSERT_TEXT);
    private final JButton clearProductButton = new JButton(CLEAR_TEXT);
    private final JButton insertSupplierButton = new JButton(INSERT_TEXT);
    private final JButton clearSupplierButton = new JButton(CLEAR_TEXT);
    private final Runnable onCategoryInserted;

    public InsertionPanel(Runnable onCategoryInserted) {
        super(new BorderLayout(12, 12));
        this.onCategoryInserted = onCategoryInserted;
        setBackground(APP_BG);
        buildLayout();
        styleButtons();
        styleInputs();
        bindActions();
        refreshComboData();
    }

    private void buildLayout() {
        JPanel contentPanel = new JPanel(new GridLayout(1, 3, 16, 0));
        contentPanel.setBackground(APP_BG);
        contentPanel.setBorder(new EmptyBorder(12, 12, 12, 12));
        contentPanel.add(wrapInCard(buildCategoryPanel()));
        contentPanel.add(wrapInCard(buildSupplierPanel()));
        contentPanel.add(wrapInCard(buildProductPanel()));

        statusLabel.setBorder(new EmptyBorder(8, 12, 8, 12));
        statusLabel.setFont(resolveFont(Font.PLAIN, statusLabel.getFont()));
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
        insertCategoryButton.setFont(resolveFont(Font.BOLD, insertCategoryButton.getFont()));
        clearCategoryButton.setFont(resolveFont(Font.PLAIN, clearCategoryButton.getFont()));
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        actionPanel.setBackground(CARD_BG);
        actionPanel.add(insertCategoryButton);
        actionPanel.add(clearCategoryButton);
        panel.add(actionPanel, gbc);

        applySectionTitleFont(panel, SECTION_TITLE_SIZE);
        return panel;
    }

    private JPanel buildSupplierPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(CARD_BG);
        panel.setBorder(BorderFactory.createTitledBorder("Suppliers"));

        GridBagConstraints gbc = baseConstraints();
        addLabeledField(panel, gbc, 0, "Name", supplierNameField, UI_FONT_SIZE);
        addLabeledField(panel, gbc, 1, "Phone", supplierPhoneField, UI_FONT_SIZE);
        addLabeledField(panel, gbc, 2, "Address", supplierAddressField, UI_FONT_SIZE);
        addLabeledField(panel, gbc, 3, "Description", supplierDescriptionField, UI_FONT_SIZE);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.LINE_START;
        insertSupplierButton.setFont(resolveFont(Font.BOLD, insertSupplierButton.getFont()));
        clearSupplierButton.setFont(resolveFont(Font.PLAIN, clearSupplierButton.getFont()));
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        actionPanel.setBackground(CARD_BG);
        actionPanel.add(insertSupplierButton);
        actionPanel.add(clearSupplierButton);
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
        addLabeledField(panel, gbc, row++, "Barcode", buildBarcodeField(), UI_FONT_SIZE);
        addLabeledField(panel, gbc, row++, "Name", productPartNameField, UI_FONT_SIZE);
        addLabeledField(panel, gbc, row++, "Cost", productCostPriceField, UI_FONT_SIZE);
        addLabeledField(panel, gbc, row++, "Sell", productSellingPriceField, UI_FONT_SIZE);
        addLabeledField(panel, gbc, row++, "Storage", productStorageQuantityField, UI_FONT_SIZE);
        addLabeledField(panel, gbc, row++, "Shop", productShopQuantityField, UI_FONT_SIZE);
        addLabeledField(panel, gbc, row++, "Brand", productBrandField, UI_FONT_SIZE);
        addLabeledField(panel, gbc, row++, "Reorder", productReorderLevelField, UI_FONT_SIZE);

        gbc.gridx = 0;
        gbc.gridy = row + 1;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.LINE_START;
        insertProductButton.setFont(resolveFont(Font.BOLD, insertProductButton.getFont()));
        clearProductButton.setFont(resolveFont(Font.PLAIN, clearProductButton.getFont()));
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
        stylePrimaryButton(insertSupplierButton, PRIMARY);
        styleSecondaryButton(clearSupplierButton);
        styleSuccessButton(insertProductButton, SUCCESS);
        styleSecondaryButton(clearProductButton);
        styleOutlineButton(generateBarcodeButton, PRIMARY);
    }

    private void styleInputs() {
        styleInput(categoryNameField);
        styleInput(categoryDescriptionField);
        styleInput(supplierNameField);
        styleInput(supplierPhoneField);
        styleInput(supplierAddressField);
        styleInput(supplierDescriptionField);
        styleInput(productCategoryCombo);
        styleInput(productSupplierCombo);
        styleInput(productBarcodeField);
        styleInput(productPartNameField);
        styleInput(productCostPriceField);
        styleInput(productSellingPriceField);
        styleInput(productStorageQuantityField);
        styleInput(productShopQuantityField);
        styleInput(productBrandField);
        styleInput(productReorderLevelField);
    }

    private void bindActions() {
        insertCategoryButton.addActionListener(_ -> insertCategory());
        clearCategoryButton.addActionListener(_ -> clearCategoryFields());
        insertSupplierButton.addActionListener(_ -> insertSupplier());
        clearSupplierButton.addActionListener(_ -> clearSupplierFields());
        insertProductButton.addActionListener(_ -> insertProduct());
        clearProductButton.addActionListener(_ -> clearProductFields());
        generateBarcodeButton.addActionListener(_ -> generateBarcode());
    }

    private void generateBarcode() {
        String barcode = infrastructure.printing.PrinterService.generateRandomCode();
        productBarcodeField.setText(barcode);
        productBarcodeField.setEditable(false);
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
        String result = StoreRepository.insertInto(category, "CATEGORIES");
        refreshComboData();
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

    private void insertSupplier() {
        String name = supplierNameField.getText().trim();
        if (name.isEmpty()) {
            statusLabel.setText("Supplier name is required.");
            return;
        }
        if (SqlHelper.existsInTable("suppliers", "suppliername", name)) {
            statusLabel.setText("Supplier already exists.");
            return;
        }
        String phoneNumber = supplierPhoneField.getText().trim();
        if (!phoneNumber.isEmpty() && !Validators.isValidPhoneNumber(phoneNumber)) {
            statusLabel.setText("Invalid phone number format.");
            return;
        }

        Supplier supplier = new Supplier(
                name,
                normalizeOptional(supplierPhoneField.getText()),
                normalizeOptional(supplierAddressField.getText()),
                normalizeOptional(supplierDescriptionField.getText())
        );

        String result = StoreRepository.insertInto(supplier, "SUPPLIERS");
        refreshComboData();
        clearSupplierFields();
        statusLabel.setText(result);
    }

    private void clearSupplierFields() {
        supplierNameField.setText("");
        supplierPhoneField.setText("");
        supplierAddressField.setText("");
        supplierDescriptionField.setText("");
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
        Float costPrice = requireFloat(productCostPriceField.getText(), "Cost", statusLabel::setText);
        if (costPrice == null) {
            return;
        }
        Float sellingPrice = requireFloat(productSellingPriceField.getText(), "Sell", statusLabel::setText);
        if (sellingPrice == null) {
            return;
        }
        Integer storageQty = requireInt(productStorageQuantityField.getText(), "Storage", statusLabel::setText);
        if (storageQty == null) {
            return;
        }
        Integer shopQty = requireInt(productShopQuantityField.getText(), "Shop", statusLabel::setText);
        if (shopQty == null) {
            return;
        }
        String brand = productBrandField.getText().trim();
        Integer reorderLevel = requireInt(productReorderLevelField.getText(), "Reorder", statusLabel::setText);
        if (reorderLevel == null) {
            return;
        }

        Product product = new Product(
                categoryItem.id(),
                supplierId,
                barcode,
                partName,
                costPrice,
                sellingPrice,
                storageQty,
                shopQty,
                brand,
                reorderLevel
        );

        String result = StoreRepository.insertInto(product, "PRODUCTS");
        clearProductFields();
        statusLabel.setText(result);
    }

    private void clearProductFields() {
        productCategoryCombo.setSelectedIndex(0);
        productSupplierCombo.setSelectedIndex(0);
        productBarcodeField.setText("");
        productBarcodeField.setEditable(true);
        productPartNameField.setText("");
        productCostPriceField.setText("");
        productSellingPriceField.setText("");
        productStorageQuantityField.setText("");
        productShopQuantityField.setText("");
        productBrandField.setText("");
        productReorderLevelField.setText("");
    }


    public void refreshComboData() {
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


    private Font resolveFont(int style, Font fallback) {
        return UiTheme.resolveFont(style, InsertionPanel.UI_FONT_SIZE, fallback);
    }

    private JComponent buildBarcodeField() {
        JPanel panel = new JPanel(new BorderLayout(6, 0));
        panel.setBackground(CARD_BG);
        panel.add(productBarcodeField, BorderLayout.CENTER);
        generateBarcodeButton.setFont(resolveFont(Font.PLAIN, generateBarcodeButton.getFont()));
        panel.add(generateBarcodeButton, BorderLayout.EAST);
        return panel;
    }

    private String normalizeOptional(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
