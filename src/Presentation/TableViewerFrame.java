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

public class TableViewerFrame extends JFrame {
    private static final String UI_FONT_FAMILY = "Times New Roman";
    private static final float UI_FONT_SIZE = 16f;
    private static final float TABLE_FONT_SIZE = 17f;
    private static final float HEADER_FONT_SIZE = 17f;
    private static final int MAX_WIDTH_SCAN_ROWS = 200;

    private final JComboBox<String> tableNameCombo = new JComboBox<>(new String[] {"categories", "products"});
    private final JButton loadButton = new JButton("Load");
    private final JTable table = new JTable();
    private final JScrollPane tableScrollPane = new JScrollPane(table);
    private final JLabel statusLabel = new JLabel(" ");

    public TableViewerFrame(String defaultTable) {
        super("Store Management");
        initFrame();
        JTabbedPane tabs = buildTabs(defaultTable);
        configureLayout(tabs);
        bindActions();
    }

    private void initFrame() {
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(8, 8));
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLocationRelativeTo(null);
    }

    private JTabbedPane buildTabs(String defaultTable) {
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(resolveFont(Font.BOLD, UI_FONT_SIZE, tabs.getFont()));
        tabs.addTab("Load Data", buildLoadPanel(defaultTable));
        tabs.addTab("Insert Data", new InsertionPanel());
        styleTabs(tabs);
        return tabs;
    }

    private void styleTabs(JTabbedPane tabs) {
        Color loadColor = new Color(34, 122, 255);
        Color insertColor = new Color(42, 167, 88);
        Color tabBg = new Color(245, 245, 245);

        tabs.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        tabs.setBackground(Color.WHITE);

        if (tabs.getTabCount() >= 1) {
            styleTab(tabs, 0, loadColor, tabBg, "Load Data");
        }
        if (tabs.getTabCount() >= 2) {
            styleTab(tabs, 1, insertColor, tabBg, "Insert Data");
        }
    }

    private void styleTab(JTabbedPane tabs, int index, Color accent, Color background, String title) {
        tabs.setBackgroundAt(index, background);
        tabs.setForegroundAt(index, accent.darker());
        tabs.setTabComponentAt(index, buildTabHeader(title, accent, background));
    }

    private JComponent buildTabHeader(String title, Color accent, Color background) {
        JLabel label = new JLabel(title);
        label.setFont(resolveFont(Font.BOLD, UI_FONT_SIZE, label.getFont()));
        label.setForeground(accent.darker());
        label.setOpaque(true);
        label.setBackground(background);
        label.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(accent),
                new EmptyBorder(4, 10, 4, 10)
        ));
        return label;
    }

    private JPanel buildLoadPanel(String defaultTable) {
        JPanel loadPanel = new JPanel(new BorderLayout(8, 8));
        JPanel topPanel = buildTopPanel(defaultTable);
        JScrollPane scrollPane = configureTable();
        loadPanel.add(topPanel, BorderLayout.NORTH);
        loadPanel.add(scrollPane, BorderLayout.CENTER);
        loadPanel.add(statusLabel, BorderLayout.SOUTH);
        return loadPanel;
    }

    private void configureLayout(JTabbedPane tabs) {
        add(tabs, BorderLayout.CENTER);
    }


    private JPanel buildTopPanel(String defaultTable) {
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel tableLabel = new JLabel("Table:");
        Font uiFont = resolveFont(Font.BOLD, UI_FONT_SIZE, tableLabel.getFont());
        tableLabel.setFont(uiFont);
        tableNameCombo.setFont(resolveFont(Font.PLAIN, UI_FONT_SIZE, tableNameCombo.getFont()));
        loadButton.setFont(resolveFont(Font.BOLD, UI_FONT_SIZE, loadButton.getFont()));
        statusLabel.setFont(resolveFont(Font.PLAIN, UI_FONT_SIZE, statusLabel.getFont()));
        styleControls();

        topPanel.add(tableLabel);
        if (defaultTable != null && !defaultTable.isBlank()) {
            tableNameCombo.setSelectedItem(defaultTable);
        }
        topPanel.add(tableNameCombo);
        topPanel.add(loadButton);
        return topPanel;
    }

    private void styleControls() {
        tableNameCombo.setBackground(Color.WHITE);
        tableNameCombo.setBorder(new CompoundBorder(
                new LineBorder(new Color(180, 180, 180)),
                new EmptyBorder(4, 8, 4, 8)
        ));
        tableNameCombo.setPreferredSize(new Dimension(200, 34));

        loadButton.setBackground(new Color(34, 122, 255));
        loadButton.setForeground(Color.WHITE);
        loadButton.setFocusPainted(false);
        loadButton.setBorder(new CompoundBorder(
                new LineBorder(new Color(26, 94, 196)),
                new EmptyBorder(6, 14, 6, 14)
        ));
    }

    private JScrollPane configureTable() {
        Font tableFont = resolveFont(Font.PLAIN, TABLE_FONT_SIZE, table.getFont());
        table.setFont(tableFont);
        table.setRowHeight(Math.max(28, table.getRowHeight() + 8));
        table.setFillsViewportHeight(true);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setShowGrid(true);
        table.setGridColor(new Color(220, 220, 220));
        table.setSelectionBackground(new Color(208, 227, 255));
        table.setSelectionForeground(Color.BLACK);
        if (table.getTableHeader() != null) {
            Font headerFont = resolveFont(Font.BOLD, HEADER_FONT_SIZE, table.getTableHeader().getFont());
            table.getTableHeader().setFont(headerFont);
            table.getTableHeader().setReorderingAllowed(false);
        }

        DefaultTableCellRenderer centeredRenderer = new DefaultTableCellRenderer();
        centeredRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        table.setDefaultRenderer(Object.class, centeredRenderer);
        table.setDefaultRenderer(Number.class, centeredRenderer);

        tableScrollPane.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                applyAutoResizeMode();
            }
        });

        return tableScrollPane;
    }

    private void bindActions() {
        loadButton.addActionListener(_ -> loadTable());
    }

    private void loadTable() {
        Object selected = tableNameCombo.getSelectedItem();
        String tableName = selected == null ? "" : selected.toString().trim();
        if (tableName.isEmpty()) {
            statusLabel.setText("Please select a table name.");
            return;
        }

        Object[][] data = Main.getAll(tableName);
        String[] columns = Helper.getColumnsNames(tableName);

        if (columns == null || columns.length == 0) {
            statusLabel.setText("No columns found or table not accessible: " + tableName);
            table.setModel(new DefaultTableModel());
            return;
        }

        DefaultTableModel model = new DefaultTableModel(data, columns);
        table.setModel(model);
        adjustColumnWidths();
        applyAutoResizeMode();
        statusLabel.setText("Loaded " + data.length + " rows from " + tableName + ".");
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

    private Font resolveFont(int style, float size, Font fallback) {
        Font candidate = new Font(TableViewerFrame.UI_FONT_FAMILY, style, Math.round(size));
        if (candidate.getFamily().equalsIgnoreCase(TableViewerFrame.UI_FONT_FAMILY)) {
            return candidate.deriveFont(style, size);
        }
        return fallback.deriveFont(style, size);
    }
}
