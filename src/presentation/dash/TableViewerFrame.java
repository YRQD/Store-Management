package presentation.dash;

import infrastructure.printing.PrinterService;
import infrastructure.config.DatabaseConfig;
import infrastructure.persistence.DatabaseConnection;
import data.repository.SqlHelper;
import data.repository.StoreRepository;
import domain.Product;
import domain.OptionItem;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.List;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import static presentation.theme.UiTheme.*;
import static presentation.theme.UiLayout.*;

public class TableViewerFrame extends JFrame {
    private static final float UI_FONT_SIZE = 16f;
    private static final float TABLE_FONT_SIZE = 16f;
    private static final float HEADER_FONT_SIZE = 16f;
    private static final int MAX_WIDTH_SCAN_ROWS = 200;

    private static final String LOCATION_ALL = "All";
    private static final String LOCATION_SHOP = "Shop";
    private static final String LOCATION_STORAGE = "Storage";
    private static final String CATEGORY_ALL_LABEL = "All categories";
    private static final String PRODUCTS_TABLE = "PRODUCTS";

    private int hoveredRow = -1;
    private boolean categoriesLoaded = false;
    private boolean managerRole = false;

    private final JComboBox<String> tableNameCombo = new JComboBox<>(new String[] {"CATEGORIES", "PRODUCTS"});
    private final JComboBox<String> locationFilterCombo = new JComboBox<>(new String[] {LOCATION_ALL, LOCATION_SHOP, LOCATION_STORAGE});
    private final JComboBox<OptionItem> categoryFilterCombo = new JComboBox<>();
    private final JTextField searchField = new JTextField(16);
    private final JButton clearSearchButton = new JButton("Clear");
    private final JButton loadButton = new JButton("Load");
    private final JButton backupButton = new JButton("Backup");
    private final JButton printButton = new JButton("Print");
    private final JButton editButton = new JButton("Edit");
    private final JTable table = new JTable();
    private final JScrollPane tableScrollPane = new JScrollPane(table);
    private final JLabel statusLabel = new JLabel(" ");
    private final Timer searchDebounceTimer = new Timer(300, _ -> executeSearch());

    public TableViewerFrame(String defaultTable, String role) {
        super("Store Management");
        managerRole = isManager(role);
        searchDebounceTimer.setRepeats(false);
        initFrame();
        JTabbedPane tabs = buildTabs(defaultTable, role);
        configureLayout(tabs);
        bindActions();
    }

    private void initFrame() {
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(8, 8));
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLocationRelativeTo(null);
        getContentPane().setBackground(APP_BG);
    }

    private JTabbedPane buildTabs(String defaultTable, String role) {
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(resolveFont(Font.BOLD, UI_FONT_SIZE, tabs.getFont()));
        tabs.addTab("Load Data", buildLoadPanel(defaultTable));
        if (isManager(role)) {
            tabs.addTab("Insert Data", new InsertionPanel(this::refreshCategoryFilter));
        }
        styleTabs(tabs);
        return tabs;
    }

    private boolean isManager(String role) {
        return role != null && role.trim().equalsIgnoreCase("Manager");
    }

    private void styleTabs(JTabbedPane tabs) {
        tabs.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        tabs.setBackground(APP_BG);
        for (int i = 0; i < tabs.getTabCount(); i++) {
            if (tabs.getTabComponentAt(i) == null) {
                tabs.setTabComponentAt(i, buildTabHeader(tabs.getTitleAt(i)));
            }
        }
        updateTabStyles(tabs);
        tabs.addChangeListener(_ -> updateTabStyles(tabs));
    }

    private void updateTabStyles(JTabbedPane tabs) {
        int selectedIndex = tabs.getSelectedIndex();
        for (int i = 0; i < tabs.getTabCount(); i++) {
            Component component = tabs.getTabComponentAt(i);
            if (!(component instanceof JPanel panel)) {
                continue;
            }
            JLabel label = (JLabel) panel.getClientProperty("tabLabel");
            boolean selected = i == selectedIndex;
            Color background = selected ? CARD_BG : TAB_INACTIVE_BG;
            Color text = selected ? TEXT_TITLE : TEXT_MUTED;
            Color border = selected ? PRIMARY : TAB_INACTIVE_BG;

            panel.setBackground(background);
            panel.setBorder(new CompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 3, 0, border),
                    new EmptyBorder(6, 12, 6, 12)
            ));
            if (label != null) {
                label.setForeground(text);
                label.setFont(resolveFont(Font.BOLD, UI_FONT_SIZE, label.getFont()));
                label.setBackground(background);
            }
            tabs.setBackgroundAt(i, background);
        }
    }

    private JComponent buildTabHeader(String title) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(true);
        JLabel label = new JLabel(title);
        label.setOpaque(true);
        label.setFont(resolveFont(Font.BOLD, UI_FONT_SIZE, label.getFont()));
        panel.add(label, BorderLayout.CENTER);
        panel.putClientProperty("tabLabel", label);
        return panel;
    }

    private JPanel buildLoadPanel(String defaultTable) {
        JPanel outerPanel = new JPanel(new BorderLayout(8, 8));
        outerPanel.setBackground(APP_BG);

        JPanel cardContent = new JPanel(new BorderLayout(8, 8));
        cardContent.setBackground(CARD_BG);

        JPanel topPanel = buildTopPanel(defaultTable);
        JScrollPane scrollPane = configureTable();
        statusLabel.setBorder(new EmptyBorder(8, 0, 0, 0));
        statusLabel.setForeground(TEXT_MUTED);

        cardContent.add(topPanel, BorderLayout.NORTH);
        cardContent.add(scrollPane, BorderLayout.CENTER);
        cardContent.add(statusLabel, BorderLayout.SOUTH);

        outerPanel.add(wrapInCard(cardContent), BorderLayout.CENTER);
        return outerPanel;
    }

    private void configureLayout(JTabbedPane tabs) {
        add(tabs, BorderLayout.CENTER);
    }


    private JPanel buildTopPanel(String defaultTable) {
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(CARD_BG);

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        leftPanel.setBackground(CARD_BG);

        JLabel tableLabel = buildTitleLabel("Table:", UI_FONT_SIZE);
        JLabel locationLabel = buildTitleLabel("Location:", UI_FONT_SIZE);
        JLabel categoryLabel = buildTitleLabel("Category:", UI_FONT_SIZE);
        JLabel searchLabel = buildTitleLabel("Search:", UI_FONT_SIZE);

        applyFont(UI_FONT_SIZE,
                tableNameCombo,
                locationFilterCombo,
                categoryFilterCombo,
                searchField,
                clearSearchButton
        );
        statusLabel.setFont(resolveFont(Font.PLAIN, UI_FONT_SIZE, statusLabel.getFont()));
        styleControls();

        leftPanel.add(tableLabel);
        if (defaultTable != null && !defaultTable.isBlank()) {
            tableNameCombo.setSelectedItem(defaultTable);
        }
        leftPanel.add(tableNameCombo);
        leftPanel.add(locationLabel);
        leftPanel.add(locationFilterCombo);
        leftPanel.add(categoryLabel);
        leftPanel.add(categoryFilterCombo);
        leftPanel.add(searchLabel);
        leftPanel.add(searchField);
        leftPanel.add(clearSearchButton);
        leftPanel.add(loadButton);

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightPanel.setBackground(CARD_BG);
        rightPanel.add(backupButton);
        if (managerRole) {
            rightPanel.add(printButton);
            rightPanel.add(editButton);
        }

        topPanel.add(leftPanel, BorderLayout.WEST);
        topPanel.add(rightPanel, BorderLayout.EAST);
        updateFiltersState();
        updatePrintButtonState();
        return topPanel;
    }

    private void styleControls() {
        styleInput(tableNameCombo);
        styleInput(locationFilterCombo);
        styleInput(categoryFilterCombo);
        styleInput(searchField);
        tableNameCombo.setPreferredSize(new Dimension(200, 34));
        locationFilterCombo.setPreferredSize(new Dimension(160, 34));
        categoryFilterCombo.setPreferredSize(new Dimension(200, 34));
        searchField.setPreferredSize(new Dimension(200, 34));
        clearSearchButton.setPreferredSize(new Dimension(90, 34));

        loadButton.setFont(resolveFont(Font.BOLD, UI_FONT_SIZE, loadButton.getFont()));
        printButton.setFont(resolveFont(Font.BOLD, UI_FONT_SIZE, printButton.getFont()));
        backupButton.setFont(resolveFont(Font.BOLD, UI_FONT_SIZE, backupButton.getFont()));
        styleSecondaryButton(clearSearchButton);
        stylePrimaryButton(loadButton, PRIMARY);
        stylePrimaryButton(backupButton, new Color(0x3B82F6));

        if (managerRole) {
            stylePrimaryButton(printButton, PRIMARY);
            stylePrimaryButton(editButton, new Color(0xF59E0B));
            printButton.setEnabled(false);
            editButton.setEnabled(false);
        }
    }

    private JScrollPane configureTable() {
        Font tableFont = resolveFont(Font.PLAIN, TABLE_FONT_SIZE, table.getFont());
        table.setFont(tableFont);
        table.setRowHeight(Math.max(30, table.getRowHeight() + 10));
        table.setFillsViewportHeight(true);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setShowGrid(true);
        table.setGridColor(new Color(0xE2E8F0));
        table.setSelectionBackground(SELECTION_BG);
        table.setSelectionForeground(TEXT_BODY);
        table.setForeground(TEXT_BODY);
        table.setBackground(ROW_ODD);
        applyHeaderStyle();

        table.setDefaultRenderer(Object.class, new ZebraTableCellRenderer());

        table.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override
            public void mouseMoved(java.awt.event.MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                if (row != hoveredRow) {
                    hoveredRow = row;
                    table.repaint();
                }
            }
        });
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                if (hoveredRow != -1) {
                    hoveredRow = -1;
                    table.repaint();
                }
            }
        });

        tableScrollPane.getViewport().setBackground(CARD_BG);
        tableScrollPane.setBorder(new LineBorder(INPUT_BORDER));
        tableScrollPane.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                applyAutoResizeMode();
            }
        });

        return tableScrollPane;
    }

    private void applyTableModel(Object[][] data, String[] columns, String tableName) {
        DefaultTableModel model = new DefaultTableModel(data, columns);
        table.setModel(model);
        applyHeaderStyle();
        adjustColumnWidths();
        applyAutoResizeMode();
        updatePrintButtonState();
        updateRowCountStatus(data.length, tableName);
    }

    private void updateRowCountStatus(int rowCount, String tableName) {
        if (rowCount == 0) {
            statusLabel.setText("No rows match the current filters for " + tableName + ".");
        } else {
            statusLabel.setText("Loaded " + rowCount + " rows from " + tableName + ".");
        }
    }

    private void applyHeaderStyle() {
        if (table.getTableHeader() == null) {
            return;
        }
        Font headerFont = resolveFont(Font.BOLD, HEADER_FONT_SIZE, table.getTableHeader().getFont());
        table.getTableHeader().setFont(headerFont);
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setDefaultRenderer(buildHeaderRenderer());
    }

    private DefaultTableCellRenderer buildHeaderRenderer() {
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
        renderer.setHorizontalAlignment(SwingConstants.CENTER);
        renderer.setBackground(TABLE_HEADER_BG);
        renderer.setForeground(TABLE_HEADER_TEXT);
        renderer.setBorder(new LineBorder(INPUT_BORDER));
        return renderer;
    }

    private class ZebraTableCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setHorizontalAlignment(SwingConstants.CENTER);

            if (isSelected) {
                setBackground(SELECTION_BG);
            } else if (row == hoveredRow) {
                setBackground(ROW_HOVER);
            } else if (row % 2 == 0) {
                setBackground(ROW_ODD);
            } else {
                setBackground(ROW_EVEN);
            }
            setForeground(TEXT_BODY);
            return this;
        }
    }

    private void bindActions() {
        loadButton.addActionListener(_ -> loadTable());
        backupButton.addActionListener(_ -> showBackupDialog());
        tableNameCombo.addActionListener(_ -> updateFiltersState());
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                scheduleSearchRefresh();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                scheduleSearchRefresh();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                scheduleSearchRefresh();
            }
        });
        if (managerRole) {
            printButton.addActionListener(_ -> printSelectedProduct());
            editButton.addActionListener(_ -> editSelectedProduct());
            table.getSelectionModel().addListSelectionListener(_ -> updatePrintButtonState());
        }
        clearSearchButton.addActionListener(_ -> clearSearch());
    }

    private void clearSearch() {
        searchField.setText("");
        if (isProductsTableSelected()) {
            loadTable();
        }
    }

    private void scheduleSearchRefresh() {
        if (!isProductsTableSelected()) {
            return;
        }
        searchDebounceTimer.restart();
    }

    private void executeSearch() {
        if (isProductsTableSelected()) {
            loadTable();
        }
    }

    private void updateFiltersState() {
        boolean enabled = isProductsTableSelected();
        setFiltersEnabled(enabled);
        if (!enabled) {
            resetFilters();
            searchDebounceTimer.stop();
        } else {
            ensureCategoriesLoaded();
        }
        updatePrintButtonState();
    }

    private void ensureCategoriesLoaded() {
        if (categoriesLoaded) {
            return;
        }
        reloadCategoryFilter();
    }

    private void refreshCategoryFilter() {
        categoriesLoaded = false;
        reloadCategoryFilter();
    }

    private void reloadCategoryFilter() {
        Integer selectedId = null;
        Object selected = categoryFilterCombo.getSelectedItem();
        if (selected instanceof OptionItem option) {
            selectedId = option.id();
        }

        DefaultComboBoxModel<OptionItem> model = new DefaultComboBoxModel<>();
        model.addElement(new OptionItem(null, CATEGORY_ALL_LABEL));
        try {
            List<OptionItem> options = StoreRepository.getIdName("categories", "categoryid", "categoryname");
            for (OptionItem option : options) {
                model.addElement(option);
            }
            categoryFilterCombo.setModel(model);
            selectOptionById(categoryFilterCombo, selectedId);
            categoriesLoaded = true;
        } catch (RuntimeException e) {
            statusLabel.setText("Failed to load categories list.");
        }
    }

    private void updatePrintButtonState() {
        if (!managerRole) {
            return;
        }
        boolean tableOk = isProductsTableSelected();
        boolean rowSelected = table.getSelectedRow() >= 0;
        printButton.setEnabled(tableOk && rowSelected);
        editButton.setEnabled(tableOk && rowSelected);
    }

    private boolean isProductsTableSelected() {
        return PRODUCTS_TABLE.equalsIgnoreCase(resolveSelectedTable());
    }

    private void setFiltersEnabled(boolean enabled) {
        locationFilterCombo.setEnabled(enabled);
        categoryFilterCombo.setEnabled(enabled);
        searchField.setEnabled(enabled);
        clearSearchButton.setEnabled(enabled);
    }

    private void resetFilters() {
        locationFilterCombo.setSelectedItem(LOCATION_ALL);
        categoryFilterCombo.setSelectedIndex(0);
        searchField.setText("");
    }

    private String resolveSelectedTable() {
        Object selected = tableNameCombo.getSelectedItem();
        return selected == null ? "" : selected.toString().trim();
    }

    private void loadTable() {
        String tableName = resolveSelectedTable();
        if (tableName.isEmpty()) {
            statusLabel.setText("Please select a table name.");
            return;
        }

        String condition = buildCondition(tableName);
        Object[][] data = StoreRepository.getAll(tableName, condition);
        String[] columns = SqlHelper.getColumnsNames(tableName);

        if (columns == null || columns.length == 0) {
            statusLabel.setText("No columns found or table not accessible: " + tableName);
            table.setModel(new DefaultTableModel());
            updatePrintButtonState();
            return;
        }

        applyTableModel(data, columns, tableName);
    }

    private String buildCondition(String tableName) {
        if (!PRODUCTS_TABLE.equalsIgnoreCase(tableName)) {
            return "TRUE";
        }

        OptionItem categorySelection = null;
        Object selectedCategory = categoryFilterCombo.getSelectedItem();
        if (selectedCategory instanceof OptionItem option) {
            categorySelection = option;
        }

        return TableFilterBuilder.buildProductsCondition(
                LOCATION_ALL,
                LOCATION_SHOP,
                LOCATION_STORAGE,
                locationFilterCombo.getSelectedItem(),
                categorySelection,
                searchField.getText()
        );
    }

    private void adjustColumnWidths() {
        int rowCount = table.getRowCount();
        int columnCount = table.getColumnCount();
        if (columnCount == 0) {
            return;
        }

        int scanRows = Math.min(rowCount, MAX_WIDTH_SCAN_ROWS);
        for (int col = 0; col < columnCount; col++) {
            int preferredWidth = preferredWidthForColumn(col, scanRows);
            TableColumn column = table.getColumnModel().getColumn(col);
            column.setPreferredWidth(preferredWidth);
        }
    }

    private int preferredWidthForColumn(int columnIndex, int scanRows) {
        int maxWidth = headerPreferredWidth(columnIndex);

        for (int row = 0; row < scanRows; row++) {
            Object value = table.getValueAt(row, columnIndex);
            if (value == null) {
                continue;
            }
            int cellWidth = cellPreferredWidth(row, columnIndex, value);
            maxWidth = Math.max(maxWidth, cellWidth);
        }

        return maxWidth + 16;
    }

    private void applyAutoResizeMode() {
        int viewportWidth = tableScrollPane.getViewport().getWidth();
        if (viewportWidth <= 0) {
            return;
        }

        int totalPreferredWidth = 0;
        for (int col = 0; col < table.getColumnCount(); col++) {
            totalPreferredWidth += table.getColumnModel().getColumn(col).getPreferredWidth();
        }

        if (totalPreferredWidth < viewportWidth) {
            table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        } else {
            table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        }
    }

    private int headerPreferredWidth(int column) {
        TableColumn tableColumn = table.getColumnModel().getColumn(column);
        TableCellRenderer headerRenderer = tableColumn.getHeaderRenderer();
        if (headerRenderer == null) {
            headerRenderer = table.getTableHeader().getDefaultRenderer();
        }

        Component headerComp = headerRenderer.getTableCellRendererComponent(
                table,
                tableColumn.getHeaderValue(),
                false,
                false,
                -1,
                column
        );

        return headerComp.getPreferredSize().width;
    }

    private int cellPreferredWidth(int row, int column, Object value) {
        TableCellRenderer renderer = table.getCellRenderer(row, column);
        Component component = renderer.getTableCellRendererComponent(
                table,
                value,
                false,
                false,
                row,
                column
        );
        return component.getPreferredSize().width;
    }

    private void printSelectedProduct() {
        if (!ensureProductsTableSelected("Please load the products table to print a barcode.")) {
            return;
        }
        int viewRow = ensureRowSelected("Please select a product row first.");
        if (viewRow < 0) {
            return;
        }
        int modelRow = table.convertRowIndexToModel(viewRow);
        String barcode = TableViewUtils.getBarcodeValue(table.getModel(), modelRow);
        if (barcode == null || barcode.isBlank()) {
            showWarning("Selected row does not contain a barcode.");
            return;
        }
        TableViewDialogs.PrintOptions options = TableViewDialogs.showPrintOptionsDialog(this, UI_FONT_SIZE);
        if (options == null) {
            return;
        }
        String message = PrinterService.printCode_39(barcode, options.copies(), options.evenLabel());
        statusLabel.setText(message);
    }

    private void showWarning(String message) {
        statusLabel.setText(message);
        TableViewDialogs.showWarning(this, message);
    }

    private boolean ensureProductsTableSelected(String message) {
        if (isProductsTableSelected()) {
            return true;
        }
        showWarning(message);
        return false;
    }

    private int ensureRowSelected(String message) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow >= 0) {
            return selectedRow;
        }
        showWarning(message);
        return -1;
    }

    private void editSelectedProduct() {
        if (!ensureProductsTableSelected("Please load the products table to edit a product.")) {
            return;
        }
        int viewRow = ensureRowSelected("Please select a product row first.");
        if (viewRow < 0) {
            return;
        }
        int modelRow = table.convertRowIndexToModel(viewRow);
        DefaultTableModel model = (DefaultTableModel) table.getModel();

        Integer productId = TableViewUtils.parseRequiredIntCell(model, modelRow, "productid");
        if (productId == null) {
            showWarning("productid column is required to update.");
            return;
        }
        String barcode = TableViewUtils.getCellValue(model, modelRow, "barcode_sku");

        JComboBox<OptionItem> categoryCombo = buildOptionsCombo(
                "categories", "categoryid", "categoryname", "Select category...");
        JComboBox<OptionItem> supplierCombo = buildOptionsCombo(
                "suppliers", "supplierid", "suppliername", "None");

        Integer categoryId = TableViewUtils.parseOptionalIntCell(model, modelRow, "categoryid");
        selectOptionById(categoryCombo, categoryId);
        Integer supplierId = TableViewUtils.parseOptionalIntCell(model, modelRow, "supplierid");
        selectOptionById(supplierCombo, supplierId);

        TableViewDialogs.ProductEditResult result = TableViewDialogs.showEditProductDialog(
                this,
                model,
                modelRow,
                barcode,
                productId,
                UI_FONT_SIZE,
                categoryCombo,
                supplierCombo
        );
        if (result == null) {
            return;
        }

        Product product = new Product(
                result.categoryId(),
                result.supplierId(),
                barcode == null ? "" : barcode,
                result.partName(),
                result.costPrice(),
                result.sellingPrice(),
                result.stockQuantity(),
                result.brand(),
                result.reorderLevel(),
                result.location()
        );
        String updateResult = StoreRepository.updateProduct(product, productId, result.isActive());
        statusLabel.setText(updateResult);
        loadTable();
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

    private JComboBox<OptionItem> buildOptionsCombo(String table, String idColumn, String nameColumn, String emptyLabel) {
        JComboBox<OptionItem> combo = new JComboBox<>();
        combo.setModel(buildOptions(table, idColumn, nameColumn, emptyLabel));
        return combo;
    }

    private void selectOptionById(JComboBox<OptionItem> combo, Integer id) {
        if (id == null) {
            combo.setSelectedIndex(0);
            return;
        }
        ComboBoxModel<OptionItem> model = combo.getModel();
        for (int i = 0; i < model.getSize(); i++) {
            OptionItem option = model.getElementAt(i);
            if (option != null && id.equals(option.id())) {
                combo.setSelectedIndex(i);
                return;
            }
        }
        combo.setSelectedIndex(0);
    }

    private void showBackupDialog() {
        List<String> partitions = DatabaseConfig.getExistingPartitions();
        if (partitions == null || partitions.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No partitions found!", "Backup Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JComboBox<String> partitionCombo = new JComboBox<>(partitions.toArray(new String[0]));
        int result = JOptionPane.showConfirmDialog(this, partitionCombo, "Select Backup Partition", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String selectedPartition = (String) partitionCombo.getSelectedItem();
            if (selectedPartition != null) {
                boolean success = new DatabaseConnection().createDatabaseBackup(selectedPartition);
                if (success) {
                    JOptionPane.showMessageDialog(this, "Backup created successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Backup failed! Check logs.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
}
