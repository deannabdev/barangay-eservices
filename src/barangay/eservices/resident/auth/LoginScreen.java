package barangay.eservices;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Arrays;
import java.io.IOException;
import java.security.GeneralSecurityException;

public class LoginScreen extends JFrame {

    private static final int W = 351, H = 775;

    // ---- Fields ----
    private PlaceholderField emailField;
    private PlaceholderPasswordField passwordField;
    private PasswordVisibilityButton eyeButton;

    public LoginScreen() {
        super("Barangay Central Signal Hub");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);

        LoginBackgroundPanel root = new LoginBackgroundPanel();
        root.setLayout(null);
        root.setPreferredSize(new Dimension(W, H));
        buildUI(root);

        setContentPane(root);
        pack();
        setLocationRelativeTo(null);
    }

    private void buildUI(JPanel root) {
        // Title (white, centered, under the logo)
        JLabel title = new JLabel(
                "<html><div style='text-align:center'>Barangay<br>Central Signal Hub</div></html>",
                SwingConstants.CENTER);
        title.setFont(LoginTheme.font(Font.BOLD, 20f));
        title.setForeground(Color.WHITE);
        title.setBounds(0, 122, W, 56);
        root.add(title);

        JLabel welcome = new JLabel("Welcome back!");
        welcome.setFont(LoginTheme.font(Font.BOLD, 28f));
        welcome.setForeground(LoginTheme.RED);
        welcome.setBounds(24, 280, 320, 50);
        root.add(welcome);

        JLabel subtitle = new JLabel(
                "<html>Please log in to access your Barangay Central Signal<br>Hub account.</html>");
        subtitle.setFont(LoginTheme.font(Font.PLAIN, 12f));
        subtitle.setForeground(Color.BLACK);
        subtitle.setBounds(24, 320, 310, 34);
        root.add(subtitle);

        // Email field
        emailField = new PlaceholderField("Email Address or Mobile Number",
                LoginTheme.font(Font.PLAIN, 12f), Color.BLACK, new Color(60, 60, 60), Color.BLACK);
        LoginInputBox emailBox = new LoginInputBox(LoginInputBox.Icon.MAIL, emailField);
        emailBox.setBounds(36, 394, 275, 44);
        root.add(emailBox);

        // Password field + eye toggle
        passwordField = new PlaceholderPasswordField("Password", LoginTheme.font(Font.PLAIN, 12f));
        LoginInputBox passBox = new LoginInputBox(LoginInputBox.Icon.LOCK, passwordField);
        passBox.setBounds(36, 466, 275, 44);
        eyeButton = new PasswordVisibilityButton();
        eyeButton.setBounds(275 - 38, 10, 26, 24);
        eyeButton.addActionListener(e -> togglePassword());
        passBox.add(eyeButton);
        root.add(passBox);

        // Forgot password
        JLabel forgot = new JLabel("Forgot password?", SwingConstants.RIGHT);
        forgot.setFont(LoginTheme.font(Font.PLAIN, 11f));
        forgot.setForeground(LoginTheme.GRAY_TEXT);
        forgot.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        forgot.setBounds(161, 513, 150, 16);
        forgot.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                new ForgotPasswordScreen().setVisible(true);
                dispose();
            }
        });
        root.add(forgot);

        // Log In button
        LoginButton login = new LoginButton("Log In", LoginTheme.font(Font.BOLD, 13f));
        login.setBounds(70, 557, 207, 34);
        login.addActionListener(e -> doLogin());
        root.add(login);

        // Register link
        JLabel register = new JLabel(
                "<html>Don't have an account? <b><font color='#D32F2F'>Register Here</font></b></html>",
                SwingConstants.CENTER);
        register.setFont(LoginTheme.font(Font.PLAIN, 11f));
        register.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        register.setBounds(0, 710, W, 18);
        register.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                new RegisterScreen1().setVisible(true);
                dispose();
            }
        });
        root.add(register);

        // Enter key logs in
        getRootPane().setDefaultButton(null);
        ActionListener enter = e -> doLogin();
        emailField.addActionListener(enter);
        passwordField.addActionListener(enter);
    }

    private void togglePassword() {
        boolean show = !eyeButton.isShown();
        eyeButton.setShown(show);
        passwordField.setEchoChar(show ? (char) 0 : '\u2022');
    }

    private void doLogin() {
        String user = emailField.getText().trim();
        char[] pass = passwordField.getPassword();
        if (user.isEmpty() || pass.length == 0) {
            JOptionPane.showMessageDialog(this, "Please enter your email/mobile number and password.",
                    "Missing information", JOptionPane.WARNING_MESSAGE);
            Arrays.fill(pass, '\0');
            return;
        }
        try {
            if (!AccountsStore.authenticate(user, pass)) {
                JOptionPane.showMessageDialog(this, "The email/mobile number or password is incorrect.",
                        "Login failed", JOptionPane.WARNING_MESSAGE);
                return;
            }
            dispose();
            new HomeWindow().setVisible(true);
        } catch (IOException | GeneralSecurityException ex) {
            JOptionPane.showMessageDialog(this, "Could not read the local account store:\n" + ex.getMessage(),
                    "Login error", JOptionPane.ERROR_MESSAGE);
        } finally {
            Arrays.fill(pass, '\0');
        }
    }

    /** Forgot Password screen (reuses the login screen's components). */
    static class ForgotPasswordScreen extends JFrame {
        private PlaceholderField emailField;

        ForgotPasswordScreen() {
            super("Barangay Central Signal Hub");
            setDefaultCloseOperation(EXIT_ON_CLOSE);
            setResizable(false);

            LoginBackgroundPanel root = new LoginBackgroundPanel();
            root.setLayout(null);
            root.setPreferredSize(new Dimension(W, H));

            JLabel title = new JLabel(
                    "<html><div style='text-align:center'>Barangay<br>Central Signal Hub</div></html>",
                    SwingConstants.CENTER);
            title.setFont(LoginTheme.font(Font.BOLD, 20f));
            title.setForeground(Color.WHITE);
            title.setBounds(0, 122, W, 56);
            root.add(title);

            ShadowLabel heading = new ShadowLabel("Forgot Password?", LoginTheme.RED);
            heading.setFont(LoginTheme.font(Font.BOLD, 26f));
            heading.setBounds(18, 280, 320, 50);
            root.add(heading);

            JLabel subtitle = new JLabel(
                    "<html>Enter the email address or mobile number linked to your account and "
                    + "we'll send you a code to reset your password.</html>");
            subtitle.setFont(LoginTheme.font(Font.PLAIN, 12f));
            subtitle.setBounds(24, 322, 300, 50);
            root.add(subtitle);

            emailField = new PlaceholderField("Email Address or Mobile Number",
                    LoginTheme.font(Font.PLAIN, 12f), Color.BLACK, new Color(60, 60, 60), Color.BLACK);
            LoginInputBox emailBox = new LoginInputBox(LoginInputBox.Icon.MAIL, emailField);
            emailBox.setBounds(36, 394, 275, 44);
            root.add(emailBox);

            LoginButton send = new LoginButton("Send Reset Code", LoginTheme.font(Font.BOLD, 13f));
            send.setBounds(70, 478, 207, 34);
            send.addActionListener(e -> doSend());
            root.add(send);
            emailField.addActionListener(e -> doSend());

            JLabel back = new JLabel(
                    "<html>Remembered your password? <b><font color='#D32F2F'>Log In</font></b></html>",
                    SwingConstants.CENTER);
            back.setFont(LoginTheme.font(Font.PLAIN, 11f));
            back.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            back.setBounds(0, 710, W, 18);
            back.addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) {
                    new LoginScreen().setVisible(true);
                    dispose();
                }
            });
            root.add(back);

            setContentPane(root);
            pack();
            setLocationRelativeTo(null);
        }

        private void doSend() {
            String user = emailField.getText().trim();
            if (user.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Please enter your email address or mobile number.",
                        "Missing information", JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                String code = AccountsStore.createResetCode(user);
                if (code == null) {
                    JOptionPane.showMessageDialog(this,
                            "If an account exists for that email/mobile number, a reset code would be sent. "
                            + "No matching local account was found.",
                            "Check your messages", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }

                JTextField codeField = new JTextField(10);
                JPasswordField newPassword = new JPasswordField(16);
                JPasswordField confirmPassword = new JPasswordField(16);
                JPanel form = new JPanel(new GridLayout(0, 1, 0, 6));
                form.add(new JLabel("Reset code: " + code));
                form.add(new JLabel("Enter reset code"));
                form.add(codeField);
                form.add(new JLabel("New password (at least 8 characters)"));
                form.add(passwordInput(newPassword));
                form.add(new JLabel("Confirm new password"));
                form.add(passwordInput(confirmPassword));

                int choice = JOptionPane.showConfirmDialog(this, form, "Reset password",
                        JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
                if (choice != JOptionPane.OK_OPTION) return;

                char[] newPasswordValue = newPassword.getPassword();
                char[] confirmPasswordValue = confirmPassword.getPassword();
                try {
                    if (newPasswordValue.length < 8) {
                        JOptionPane.showMessageDialog(this, "The new password must be at least 8 characters.",
                                "Invalid password", JOptionPane.WARNING_MESSAGE);
                    } else if (!Arrays.equals(newPasswordValue, confirmPasswordValue)) {
                        JOptionPane.showMessageDialog(this, "The new passwords do not match.",
                                "Invalid password", JOptionPane.WARNING_MESSAGE);
                    } else if (!AccountsStore.resetPassword(user, codeField.getText().trim(), newPasswordValue)) {
                        JOptionPane.showMessageDialog(this,
                                "The reset code is invalid or expired. Request a new code and try again.",
                                "Reset failed", JOptionPane.WARNING_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(this, "Your password has been reset.",
                                "Password reset", JOptionPane.INFORMATION_MESSAGE);
                        new LoginScreen().setVisible(true);
                        dispose();
                    }
                } finally {
                    Arrays.fill(newPasswordValue, '\0');
                    Arrays.fill(confirmPasswordValue, '\0');
                }
            } catch (IOException | GeneralSecurityException ex) {
                JOptionPane.showMessageDialog(this,
                        "Could not access the local account store:\n" + ex.getMessage(),
                        "Password reset error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private static JPanel passwordInput(JPasswordField field) {
        JPanel input = new JPanel(new BorderLayout(4, 0));
        input.setOpaque(false);
        field.setEchoChar('\u2022');

        PasswordVisibilityButton eye = new PasswordVisibilityButton();
        eye.setPreferredSize(new Dimension(28, 24));
        eye.addActionListener(e -> {
            eye.setShown(!eye.isShown());
            field.setEchoChar(eye.isShown() ? (char) 0 : '\u2022');
        });

        input.add(field, BorderLayout.CENTER);
        input.add(eye, BorderLayout.EAST);
        return input;
    }

}