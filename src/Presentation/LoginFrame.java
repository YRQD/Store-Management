package Presentation;

import Infrastructure.DbController.Helper;
import Infrastructure.DbController.Main;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

import static Presentation.UiTheme.*;

public class LoginFrame extends JFrame {
    private static final float UI_FONT_SIZE = 16f;

    private final JTextField usernameField = new JTextField(16);
    private final JPasswordField passwordField = new JPasswordField(16);
    private final JButton loginButton = new JButton("Login");
    private final JLabel statusLabel = new JLabel(" ");

    private final String defaultTable;

    public LoginFrame(String defaultTable) {
        super("Store Management - Login");
        this.defaultTable = defaultTable;
        initFrame();
        buildLayout();
        styleControls();
        bindActions();
    }

    private void initFrame() {
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(520, 360);
        setLocationRelativeTo(null);
        getContentPane().setBackground(APP_BG);
    }

    private void buildLayout() {
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(CARD_BG);
        card.setBorder(new CompoundBorder(
                new LineBorder(INPUT_BORDER),
                new EmptyBorder(16, 16, 16, 16)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        JLabel titleLabel = new JLabel("Login");
        titleLabel.setFont(resolveFont(Font.BOLD, UI_FONT_SIZE + 2, titleLabel.getFont()));
        titleLabel.setForeground(TEXT_TITLE);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        card.add(titleLabel, gbc);

        gbc.gridwidth = 1;
        gbc.gridy = 1;
        gbc.gridx = 0;
        card.add(buildFieldLabel("Username"), gbc);
        gbc.gridx = 1;
        card.add(usernameField, gbc);

        gbc.gridy = 2;
        gbc.gridx = 0;
        card.add(buildFieldLabel("Password"), gbc);
        gbc.gridx = 1;
        card.add(passwordField, gbc);

        gbc.gridy = 3;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionPanel.setBackground(CARD_BG);
        actionPanel.add(loginButton);
        card.add(actionPanel, gbc);

        gbc.gridy = 4;
        statusLabel.setFont(resolveFont(Font.PLAIN, UI_FONT_SIZE - 1, statusLabel.getFont()));
        statusLabel.setForeground(TEXT_MUTED);
        card.add(statusLabel, gbc);

        JPanel outer = new JPanel(new GridBagLayout());
        outer.setBackground(APP_BG);
        outer.add(card);
        add(outer, BorderLayout.CENTER);
    }

    private JLabel buildFieldLabel(String text) {
        JLabel label = new JLabel(text + ":");
        label.setFont(resolveFont(Font.PLAIN, UI_FONT_SIZE, label.getFont()));
        label.setForeground(TEXT_BODY);
        return label;
    }

    private void styleControls() {
        usernameField.setFont(resolveFont(Font.PLAIN, UI_FONT_SIZE, usernameField.getFont()));
        passwordField.setFont(resolveFont(Font.PLAIN, UI_FONT_SIZE, passwordField.getFont()));
        styleInput(usernameField);
        styleInput(passwordField);

        loginButton.setFont(resolveFont(Font.BOLD, UI_FONT_SIZE, loginButton.getFont()));
        stylePrimaryButton(loginButton, PRIMARY);
    }

    private void bindActions() {
        loginButton.addActionListener(_ -> attemptLogin());
        passwordField.addActionListener(_ -> attemptLogin());
    }

    private void attemptLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();
        if (username.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Username and password are required.");
            return;
        }

        if (!Helper.userExists(username, password)) {
            statusLabel.setText("Invalid username or password.");
            return;
        }

        String role = Helper.getUserPermission(username);
        if (!isValidRole(role)) {
            statusLabel.setText("Access denied: role not allowed.");
            return;
        }

        Main.updateUserLogin(username);
        SwingUtilities.invokeLater(() -> {
            TableViewerFrame frame = new TableViewerFrame(defaultTable, role);
            frame.setVisible(true);
        });
        dispose();
    }

    private boolean isValidRole(String role) {
        return role != null && (role.equalsIgnoreCase("Manager") || role.equalsIgnoreCase("Admin"));
    }
}
