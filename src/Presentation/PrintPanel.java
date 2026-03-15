package Presentation;

import Infrastructure.DbController.XPrinter;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

import static Presentation.UiTheme.*;

public class PrintPanel extends JPanel {
    private static final float UI_FONT_SIZE = 16f;

    private final JTextField barcodeField = new JTextField(18);
    private final JSpinner copiesSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 20, 1));
    private final JCheckBox evenLabelCheck = new JCheckBox("Even Label");
    private final JButton printButton = new JButton("Print Barcode");
    private final JLabel statusLabel = new JLabel(" ");

    public PrintPanel() {
        super(new BorderLayout(12, 12));
        setBackground(APP_BG);
        buildLayout();
        styleControls();
        bindActions();
    }

    private void buildLayout() {
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(INPUT_BORDER),
                new EmptyBorder(12, 12, 12, 12)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        JLabel titleLabel = new JLabel("Print Barcode");
        titleLabel.setFont(resolveFont(Font.BOLD, UI_FONT_SIZE + 2, titleLabel.getFont()));
        titleLabel.setForeground(TEXT_TITLE);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        card.add(titleLabel, gbc);

        gbc.gridwidth = 1;
        gbc.gridy = 1;
        gbc.gridx = 0;
        JLabel barcodeLabel = new JLabel("Barcode/SKU:");
        barcodeLabel.setFont(resolveFont(Font.PLAIN, UI_FONT_SIZE, barcodeLabel.getFont()));
        barcodeLabel.setForeground(TEXT_BODY);
        card.add(barcodeLabel, gbc);

        gbc.gridx = 1;
        barcodeField.setFont(resolveFont(Font.PLAIN, UI_FONT_SIZE, barcodeField.getFont()));
        card.add(barcodeField, gbc);

        gbc.gridy = 2;
        gbc.gridx = 0;
        JLabel copiesLabel = new JLabel("Copies:");
        copiesLabel.setFont(resolveFont(Font.PLAIN, UI_FONT_SIZE, copiesLabel.getFont()));
        copiesLabel.setForeground(TEXT_BODY);
        card.add(copiesLabel, gbc);

        gbc.gridx = 1;
        copiesSpinner.setFont(resolveFont(Font.PLAIN, UI_FONT_SIZE, copiesSpinner.getFont()));
        card.add(copiesSpinner, gbc);

        gbc.gridy = 3;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        evenLabelCheck.setFont(resolveFont(Font.PLAIN, UI_FONT_SIZE, evenLabelCheck.getFont()));
        evenLabelCheck.setForeground(TEXT_BODY);
        evenLabelCheck.setBackground(CARD_BG);
        card.add(evenLabelCheck, gbc);

        gbc.gridy = 4;
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionPanel.setBackground(CARD_BG);
        actionPanel.add(printButton);
        card.add(actionPanel, gbc);

        gbc.gridy = 5;
        statusLabel.setFont(resolveFont(Font.PLAIN, UI_FONT_SIZE - 1, statusLabel.getFont()));
        statusLabel.setForeground(TEXT_MUTED);
        card.add(statusLabel, gbc);

        JPanel outer = new JPanel(new GridBagLayout());
        outer.setBackground(APP_BG);
        outer.add(card);
        add(outer, BorderLayout.CENTER);
    }

    private void styleControls() {
        styleInput(barcodeField);
        styleInput(copiesSpinner);
        JComponent editor = copiesSpinner.getEditor();
        if (editor instanceof JSpinner.DefaultEditor defaultEditor) {
            defaultEditor.getTextField().setFont(resolveFont(Font.PLAIN, UI_FONT_SIZE, defaultEditor.getTextField().getFont()));
        }
        printButton.setFont(resolveFont(Font.BOLD, UI_FONT_SIZE, printButton.getFont()));
        stylePrimaryButton(printButton, PRIMARY);
    }

    private void bindActions() {
        printButton.addActionListener(_ -> attemptPrint());
        barcodeField.addActionListener(_ -> attemptPrint());
    }

    private void attemptPrint() {
        String barcode = barcodeField.getText().trim();
        if (barcode.isEmpty()) {
            statusLabel.setText("Barcode is required.");
            return;
        }
        int copies = (int) copiesSpinner.getValue();
        boolean isEvenLabel = evenLabelCheck.isSelected();
        String message = XPrinter.printCode_39(barcode, copies, isEvenLabel);
        statusLabel.setText(message);
    }
}
