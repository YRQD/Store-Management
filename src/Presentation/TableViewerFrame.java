package Presentation;

import Infrastructure.DbController.*;

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

import static Presentation.UiTheme.*;

public class TableViewerFrame extends JFrame {
    private static final float UI_FONT_SIZE = 16f;
    private static final float TABLE_FONT_SIZE = 16f;
    private static final float HEADER_FONT_SIZE = 16f;
    private static final int MAX_WIDTH_SCAN_ROWS = 200;

    private static final String LOCATION_ALL = "All";
    private static final String LOCATION_SHOP = "Shop";
    private static final String LOCATION_STORAGE = "Storage";
    private static final String CATEGORY_ALL_LABEL = "All categories";
    private static final String PRODUCTS_TABLE = "products";

    private int hoveredRow = -1;
    private boolean categoriesLoaded = false;
    private boolean managerRole = false;

    private final JComboBox<String> tableNameCombo = new JComboBox<>(new String[] {"categories", "products"});
    private final JComboBox<String> locationFilterCombo = new JComboBox<>(new String[] {LOCATION_ALL, LOCATION_SHOP, LOCATION_STORAGE});
    private final JComboBox<Infrastructure.Entities.OptionItem> categoryFilterCombo = new JComboBox<>();
    private final JTextField searchField = new JTextField(16);
    private final JButton clearSearchButton = new JButton("Clear");
    private final JButton loadButton = new JButton("Load");
    private final JButton printButton = new JButton("Print");
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
            tabs.addTab("Insert Data", new InsertionPanel());
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

        JPanel cardPanel = new JPanel(new BorderLayout(8, 8));
        cardPanel.setBackground(CARD_BG);
        cardPanel.setBorder(new CompoundBorder(
                new LineBorder(INPUT_BORDER),
                new EmptyBorder(12, 12, 12, 12)
        ));

        JPanel topPanel = buildTopPanel(defaultTable);
        JScrollPane scrollPane = configureTable();
        statusLabel.setBorder(new EmptyBorder(8, 0, 0, 0));
        statusLabel.setForeground(TEXT_MUTED);

        cardPanel.add(topPanel, BorderLayout.NORTH);
        cardPanel.add(scrollPane, BorderLayout.CENTER);
        cardPanel.add(statusLabel, BorderLayout.SOUTH);

        outerPanel.add(cardPanel, BorderLayout.CENTER);
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

        JLabel tableLabel = new JLabel("Table:");
        JLabel locationLabel = new JLabel("Location:");
        JLabel categoryLabel = new JLabel("Category:");
        JLabel searchLabel = new JLabel("Search:");
        Font uiFont = resolveFont(Font.BOLD, UI_FONT_SIZE, tableLabel.getFont());
        tableLabel.setFont(uiFont);
        tableLabel.setForeground(TEXT_TITLE);
        locationLabel.setFont(uiFont);
        locationLabel.setForeground(TEXT_TITLE);
        categoryLabel.setFont(uiFont);
        categoryLabel.setForeground(TEXT_TITLE);
        searchLabel.setFont(uiFont);
        searchLabel.setForeground(TEXT_TITLE);
        tableNameCombo.setFont(resolveFont(Font.PLAIN, UI_FONT_SIZE, tableNameCombo.getFont()));
        locationFilterCombo.setFont(resolveFont(Font.PLAIN, UI_FONT_SIZE, locationFilterCombo.getFont()));
        categoryFilterCombo.setFont(resolveFont(Font.PLAIN, UI_FONT_SIZE, categoryFilterCombo.getFont()));
        searchField.setFont(resolveFont(Font.PLAIN, UI_FONT_SIZE, searchField.getFont()));
        clearSearchButton.setFont(resolveFont(Font.PLAIN, UI_FONT_SIZE, clearSearchButton.getFont()));
        loadButton.setFont(resolveFont(Font.BOLD, UI_FONT_SIZE, loadButton.getFont()));
        printButton.setFont(resolveFont(Font.BOLD, UI_FONT_SIZE, printButton.getFont()));
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
        if (managerRole) {
            rightPanel.add(printButton);
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

        styleSecondaryButton(clearSearchButton);

        loadButton.setBackground(PRIMARY);
        loadButton.setForeground(Color.WHITE);
        loadButton.setFocusPainted(false);
        loadButton.setBorder(new CompoundBorder(
                new LineBorder(PRIMARY.darker()),
                new EmptyBorder(6, 14, 6, 14)
        ));
        loadButton.setOpaque(true);

        if (managerRole) {
            stylePrimaryButton(printButton, PRIMARY);
            printButton.setEnabled(false);
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
        if (table.getTableHeader() != null) {
            Font headerFont = resolveFont(Font.BOLD, HEADER_FONT_SIZE, table.getTableHeader().getFont());
            table.getTableHeader().setFont(headerFont);
            table.getTableHeader().setReorderingAllowed(false);
            table.getTableHeader().setDefaultRenderer(buildHeaderRenderer());
        }

        table.setDefaultRenderer(Object.class, new ZebraTableCellRenderer());
        table.setDefaultRenderer(Number.class, new ZebraTableCellRenderer());

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
            table.getSelectionModel().addListSelectionListener(_ -> updatePrintButtonState());
        }
        clearSearchButton.addActionListener(_ -> clearSearch());
    }

    private void clearSearch() {
        searchField.setText("");
        if (PRODUCTS_TABLE.equalsIgnoreCase(resolveSelectedTable())) {
            loadTable();
        }
    }

    private void scheduleSearchRefresh() {
        if (!PRODUCTS_TABLE.equalsIgnoreCase(resolveSelectedTable())) {
            return;
        }
        searchDebounceTimer.restart();
    }

    private void executeSearch() {
        if (PRODUCTS_TABLE.equalsIgnoreCase(resolveSelectedTable())) {
            loadTable();
        }
    }

    private void updateFiltersState() {
        String tableName = resolveSelectedTable();
        boolean enabled = PRODUCTS_TABLE.equalsIgnoreCase(tableName);
        locationFilterCombo.setEnabled(enabled);
        categoryFilterCombo.setEnabled(enabled);
        searchField.setEnabled(enabled);
        clearSearchButton.setEnabled(enabled);
        if (!enabled) {
            locationFilterCombo.setSelectedItem(LOCATION_ALL);
            categoryFilterCombo.setSelectedIndex(0);
            searchField.setText("");
            searchDebounceTimer.stop();
        } else {
            ensureCategoriesLoaded();
        }
        updatePrintButtonState();
    }

    private void updatePrintButtonState() {
        if (!managerRole) {
            return;
        }
        boolean tableOk = PRODUCTS_TABLE.equalsIgnoreCase(resolveSelectedTable());
        boolean rowSelected = table.getSelectedRow() >= 0;
        printButton.setEnabled(tableOk && rowSelected);
    }

    private void ensureCategoriesLoaded() {
        if (categoriesLoaded) {
            return;
        }
        DefaultComboBoxModel<Infrastructure.Entities.OptionItem> model = new DefaultComboBoxModel<>();
        model.addElement(new Infrastructure.Entities.OptionItem(null, CATEGORY_ALL_LABEL));
        try {
            List<Infrastructure.Entities.OptionItem> options = Main.getIdName("categories", "categoryid", "categoryname");
            for (Infrastructure.Entities.OptionItem option : options) {
                model.addElement(option);
            }
            categoryFilterCombo.setModel(model);
            categoriesLoaded = true;
        } catch (RuntimeException e) {
            statusLabel.setText("Failed to load categories list.");
        }
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
        Object[][] data = Main.getAll(tableName, condition);
        String[] columns = Helper.getColumnsNames(tableName);

        if (columns == null || columns.length == 0) {
            statusLabel.setText("No columns found or table not accessible: " + tableName);
            table.setModel(new DefaultTableModel());
            updatePrintButtonState();
            return;
        }

        DefaultTableModel model = new DefaultTableModel(data, columns);
        table.setModel(model);
        if (table.getTableHeader() != null) {
            table.getTableHeader().setDefaultRenderer(buildHeaderRenderer());
        }
        adjustColumnWidths();
        applyAutoResizeMode();
        updatePrintButtonState();
        if (data.length == 0) {
            statusLabel.setText("No rows match the current filters for " + tableName + ".");
        } else {
            statusLabel.setText("Loaded " + data.length + " rows from " + tableName + ".");
        }
    }

    private String buildCondition(String tableName) {
        if (!PRODUCTS_TABLE.equalsIgnoreCase(tableName)) {
            return "TRUE";
        }

        String locationCondition = "TRUE";
        Object locationSelected = locationFilterCombo.getSelectedItem();
        String location = locationSelected == null ? LOCATION_ALL : locationSelected.toString();
        if (LOCATION_SHOP.equalsIgnoreCase(location)) {
            locationCondition = "location = 'Shop'";
        } else if (LOCATION_STORAGE.equalsIgnoreCase(location)) {
            locationCondition = "location = 'Storage'";
        }

        String categoryCondition = "TRUE";
        Object categorySelected = categoryFilterCombo.getSelectedItem();
        if (categorySelected instanceof Infrastructure.Entities.OptionItem option && option.id() != null) {
            categoryCondition = "categoryid = " + option.id();
        }

        String searchCondition = "TRUE";
        String rawSearch = searchField.getText().trim();
        if (!rawSearch.isEmpty()) {
            String escaped = rawSearch.replace("'", "''");
            searchCondition = "(barcode_sku ILIKE '%" + escaped + "%' OR partname ILIKE '%" + escaped + "%')";
        }

        return locationCondition + " AND " + categoryCondition + " AND " + searchCondition;
    }

    private void printSelectedProduct() {
        if (!PRODUCTS_TABLE.equalsIgnoreCase(resolveSelectedTable())) {
            showWarning("Please load the products table to print a barcode.");
            return;
        }
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            showWarning("Please select a product row first.");
            return;
        }
        String barcode = resolveBarcodeFromRow(viewRow);
        if (barcode == null || barcode.isBlank()) {
            showWarning("Selected row does not contain a barcode.");
            return;
        }
        PrintOptions options = showPrintOptionsDialog();
        if (options == null) {
            return;
        }
        String message = XPrinter.printCode_39(barcode, options.copies(), options.evenLabel());
        statusLabel.setText(message);
    }

    private void showWarning(String message) {
        statusLabel.setText(message);
        JOptionPane.showMessageDialog(this, message, "Notice", JOptionPane.WARNING_MESSAGE);
    }

    private String resolveBarcodeFromRow(int viewRow) {
        int modelRow = table.convertRowIndexToModel(viewRow);
        int columnIndex = findBarcodeColumnIndex();
        if (columnIndex < 0) {
            return null;
        }
        Object value = table.getModel().getValueAt(modelRow, columnIndex);
        return value == null ? null : value.toString();
    }

    private int findBarcodeColumnIndex() {
        for (int col = 0; col < table.getColumnCount(); col++) {
            String name = table.getColumnName(col);
            if (name != null && name.toLowerCase().contains("barcode")) {
                return col;
            }
        }
        return -1;
    }

    private PrintOptions showPrintOptionsDialog() {
        JSpinner copiesSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 20, 1));
        JCheckBox evenLabelCheck = new JCheckBox("Label type is even");
        copiesSpinner.setFont(resolveFont(Font.PLAIN, UI_FONT_SIZE, copiesSpinner.getFont()));
        evenLabelCheck.setFont(resolveFont(Font.PLAIN, UI_FONT_SIZE, evenLabelCheck.getFont()));
        evenLabelCheck.setForeground(TEXT_BODY);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(CARD_BG);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        JLabel copiesLabel = new JLabel("Copies:");
        copiesLabel.setFont(resolveFont(Font.PLAIN, UI_FONT_SIZE, copiesLabel.getFont()));
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
                this,
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

    private record PrintOptions(int copies, boolean evenLabel) {
    }

    private Font resolveFont(int style, float size, Font fallback) {
        return UiTheme.resolveFont(style, size, fallback);
    }
}
