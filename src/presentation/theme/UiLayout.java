package presentation.theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;

public final class UiLayout {
    private UiLayout() {
    }

    public static JPanel wrapInCard(JComponent content) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(UiTheme.CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UiTheme.INPUT_BORDER),
                new EmptyBorder(12, 12, 12, 12)
        ));
        card.add(content, BorderLayout.CENTER);
        return card;
    }

    public static GridBagConstraints baseConstraints() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        return gbc;
    }

    public static JLabel buildTitleLabel(String text, float fontSize) {
        return buildLabel(text, Font.BOLD, fontSize, UiTheme.TEXT_TITLE);
    }

    public static JLabel buildBodyLabel(String text, float fontSize) {
        return buildLabel(text, Font.PLAIN, fontSize, UiTheme.TEXT_BODY);
    }

    public static void addLabeledField(JPanel panel,
                                       GridBagConstraints gbc,
                                       int row,
                                       String labelText,
                                       JComponent field,
                                       float fontSize) {
        addLabeledField(panel, gbc, row, labelText, field, fontSize, UiTheme.TEXT_BODY);
    }

    public static void addLabeledField(JPanel panel,
                                       GridBagConstraints gbc,
                                       int row,
                                       String labelText,
                                       JComponent field,
                                       float fontSize,
                                       Color labelColor) {
        JLabel label = buildLabel(labelText + ":", Font.PLAIN, fontSize, labelColor);
        field.setFont(UiTheme.resolveFont(Font.PLAIN, fontSize, field.getFont()));

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.4;
        panel.add(label, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.6;
        panel.add(field, gbc);
    }

    public static void applySectionTitleFont(JPanel panel, float size) {
        if (panel.getBorder() instanceof TitledBorder titledBorder) {
            Font sectionFont = UiTheme.resolveFont(Font.BOLD, size, panel.getFont());
            titledBorder.setTitleFont(sectionFont);
            titledBorder.setTitleColor(UiTheme.TEXT_TITLE);
        }
    }

    public static void applyFont(float size, JComponent... components) {
        if (components == null) {
            return;
        }
        for (JComponent component : components) {
            if (component == null) {
                continue;
            }
            component.setFont(UiTheme.resolveFont(Font.PLAIN, size, component.getFont()));
        }
    }

    private static JLabel buildLabel(String text, int style, float size, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(UiTheme.resolveFont(style, size, label.getFont()));
        label.setForeground(color);
        return label;
    }
}

