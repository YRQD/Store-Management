package presentation.theme;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

public final class UiTheme {
    public static final String UI_FONT_FAMILY = "Segoe UI";

    public static final Color APP_BG = new Color(0xF8FAFC);
    public static final Color CARD_BG = new Color(0xFFFFFF);
    public static final Color TEXT_TITLE = new Color(0x1E293B);
    public static final Color TEXT_BODY = new Color(0x334155);
    public static final Color TEXT_MUTED = new Color(0x475569);
    public static final Color PRIMARY = new Color(0x2563EB);
    public static final Color SECONDARY_BG = new Color(0xF1F5F9);
    public static final Color INPUT_BORDER = new Color(0xCBD5E1);
    public static final Color SUCCESS = new Color(0x10B981);
    public static final Color DANGER = new Color(0xEF4444);
    public static final Color TABLE_HEADER_BG = new Color(0xF1F5F9);
    public static final Color TABLE_HEADER_TEXT = new Color(0x0F172A);
    public static final Color ROW_ODD = new Color(0xFFFFFF);
    public static final Color ROW_EVEN = new Color(0xF8FAFC);
    public static final Color ROW_HOVER = new Color(0xEFF6FF);
    public static final Color SELECTION_BG = new Color(0xDBEAFE);
    public static final Color TAB_INACTIVE_BG = new Color(0xE2E8F0);

    private UiTheme() {
    }

    public static Font resolveFont(int style, float size, Font fallback) {
        Font candidate = new Font(UI_FONT_FAMILY, style, Math.round(size));
        if (candidate.getFamily().equalsIgnoreCase(UI_FONT_FAMILY)) {
            return candidate.deriveFont(style, size);
        }
        return fallback.deriveFont(style, size);
    }

    public static CompoundBorder buildInputBorder(Color color, int thickness) {
        return new CompoundBorder(
                new LineBorder(color, thickness),
                new EmptyBorder(4, 8, 4, 8)
        );
    }

    public static void styleInput(JComponent field) {
        field.setBackground(CARD_BG);
        field.setForeground(TEXT_BODY);
        field.setBorder(buildInputBorder(INPUT_BORDER, 1));
        field.setOpaque(true);
        field.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                field.setBorder(buildInputBorder(PRIMARY, 2));
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                field.setBorder(buildInputBorder(INPUT_BORDER, 1));
            }
        });
    }

    public static void stylePrimaryButton(JButton button, Color background) {
        button.setBackground(background);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(background.darker()),
                new EmptyBorder(6, 14, 6, 14)
        ));
        button.setOpaque(true);
    }

    public static void styleSuccessButton(JButton button, Color background) {
        stylePrimaryButton(button, background);
    }

    public static void styleSecondaryButton(JButton button) {
        button.setBackground(SECONDARY_BG);
        button.setForeground(TEXT_TITLE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(INPUT_BORDER),
                new EmptyBorder(6, 14, 6, 14)
        ));
        button.setOpaque(true);
    }

    public static void styleOutlineButton(JButton button, Color border) {
        button.setBackground(CARD_BG);
        button.setForeground(border.darker());
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(border),
                new EmptyBorder(6, 10, 6, 10)
        ));
        button.setOpaque(true);
    }
}

