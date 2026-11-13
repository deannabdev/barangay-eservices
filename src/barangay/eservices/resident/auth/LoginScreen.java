import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.Arrays;
import java.awt.image.*;
import java.io.IOException;
import java.security.GeneralSecurityException;

public class LoginScreen extends JFrame {

    // ---- Colors taken from the design ----
    private static final Color RED        = new Color(211, 47, 47);
    private static final Color RED_TOP    = new Color(214, 42, 42);
    private static final Color RED_BOTTOM = new Color(160, 28, 30);
    private static final Color GRAY_TEXT  = new Color(120, 120, 120);

    private static final int W = 351, H = 775;

    private static Font font(int style, float size) {
        String[] names = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        String family = Arrays.asList(names).contains("Lexend") ? "Lexend" : "SansSerif";
        return new Font(family, style, 12).deriveFont(size);
    }

    private static void smooth(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
    }

    // ---- Fields ----
    private PlaceholderField emailField;
    private PlaceholderPassword passwordField;
    private EyeButton eyeButton;

    public LoginScreen() {
        super("Barangay Central Signal Hub");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);

        BackgroundPanel root = new BackgroundPanel();
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
        title.setFont(font(Font.BOLD, 20f));
        title.setForeground(Color.WHITE);
        title.setBounds(0, 122, W, 56);
        root.add(title);

        // "Welcome back!" with red drop shadow
        ShadowLabel welcome = new ShadowLabel("Welcome back!");
        welcome.setFont(font(Font.BOLD, 28f));
        welcome.setBounds(18, 280, 320, 50);
        root.add(welcome);

        JLabel subtitle = new JLabel(
                "<html>Please log in to access your Barangay Central Signal<br>Hub account.</html>");
        subtitle.setFont(font(Font.PLAIN, 12f));
        subtitle.setForeground(Color.BLACK);
        subtitle.setBounds(24, 320, 310, 34);
        root.add(subtitle);

        // Email field
        emailField = new PlaceholderField("Email Address or Mobile Number");
        InputBox emailBox = new InputBox(InputBox.Icon.MAIL, emailField);
        emailBox.setBounds(36, 394, 275, 44);
        root.add(emailBox);

        // Password field + eye toggle
        passwordField = new PlaceholderPassword("Password");
        InputBox passBox = new InputBox(InputBox.Icon.LOCK, passwordField);
        passBox.setBounds(36, 466, 275, 44);
        eyeButton = new EyeButton();
        eyeButton.setBounds(275 - 38, 10, 26, 24);
        eyeButton.addActionListener(e -> togglePassword());
        passBox.add(eyeButton);
        root.add(passBox);

        // Forgot password
        JLabel forgot = new JLabel("Forgot password?", SwingConstants.RIGHT);
        forgot.setFont(font(Font.PLAIN, 11f));
        forgot.setForeground(GRAY_TEXT);
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
        RoundedButton login = new RoundedButton("Log In");
        login.setBounds(70, 557, 207, 34);
        login.addActionListener(e -> doLogin());
        root.add(login);

        // Register link
        JLabel register = new JLabel(
                "<html>Don't have an account? <b><font color='#D32F2F'>Register Here</font></b></html>",
                SwingConstants.CENTER);
        register.setFont(font(Font.PLAIN, 11f));
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
            JOptionPane.showMessageDialog(this, "Login successful.");
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

            BackgroundPanel root = new BackgroundPanel();
            root.setLayout(null);
            root.setPreferredSize(new Dimension(W, H));

            JLabel title = new JLabel(
                    "<html><div style='text-align:center'>Barangay<br>Central Signal Hub</div></html>",
                    SwingConstants.CENTER);
            title.setFont(font(Font.BOLD, 20f));
            title.setForeground(Color.WHITE);
            title.setBounds(0, 122, W, 56);
            root.add(title);

            ShadowLabel heading = new ShadowLabel("Forgot Password?");
            heading.setFont(font(Font.BOLD, 26f));
            heading.setBounds(18, 280, 320, 50);
            root.add(heading);

            JLabel subtitle = new JLabel(
                    "<html>Enter the email address or mobile number linked to your account and "
                    + "we'll send you a code to reset your password.</html>");
            subtitle.setFont(font(Font.PLAIN, 12f));
            subtitle.setBounds(24, 322, 300, 50);
            root.add(subtitle);

            emailField = new PlaceholderField("Email Address or Mobile Number");
            InputBox emailBox = new InputBox(InputBox.Icon.MAIL, emailField);
            emailBox.setBounds(36, 394, 275, 44);
            root.add(emailBox);

            RoundedButton send = new RoundedButton("Send Reset Code");
            send.setBounds(70, 478, 207, 34);
            send.addActionListener(e -> doSend());
            root.add(send);
            emailField.addActionListener(e -> doSend());

            JLabel back = new JLabel(
                    "<html>Remembered your password? <b><font color='#D32F2F'>Log In</font></b></html>",
                    SwingConstants.CENTER);
            back.setFont(font(Font.PLAIN, 11f));
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

        EyeButton eye = new EyeButton();
        eye.setPreferredSize(new Dimension(28, 24));
        eye.addActionListener(e -> {
            eye.setShown(!eye.isShown());
            field.setEchoChar(eye.isShown() ? (char) 0 : '\u2022');
        });

        input.add(field, BorderLayout.CENTER);
        input.add(eye, BorderLayout.EAST);
        return input;
    }

    // =====================================================================
    //  Custom components
    // =====================================================================

    /** Red gradient header, logo, and white rounded sheet. */
    private static class BackgroundPanel extends JPanel {
        @Override protected void paintComponent(Graphics g0) {
            super.paintComponent(g0);
            Graphics2D g = (Graphics2D) g0.create();
            smooth(g);
            int w = getWidth(), h = getHeight();

            // Red gradient
            g.setPaint(new GradientPaint(0, 0, RED_TOP, 0, 230, RED_BOTTOM));
            g.fillRect(0, 0, w, h);

            // White rounded sheet
            g.setColor(Color.WHITE);
            g.fill(new RoundRectangle2D.Double(0, 215, w, h, 90, 90));

            // Logo tile (shadow + tile)
            int size = 52, x = (w - size) / 2, y = 68;
            g.setColor(new Color(0, 0, 0, 60));
            g.fill(new RoundRectangle2D.Double(x, y + 5, size, size, 16, 16));
            g.setColor(new Color(222, 45, 48));
            g.fill(new RoundRectangle2D.Double(x, y, size, size, 16, 16));
            g.setColor(new Color(120, 15, 18));
            g.setStroke(new BasicStroke(1.2f));
            g.draw(new RoundRectangle2D.Double(x, y, size, size, 16, 16));

            // Shield
            double cx = x + size / 2.0, top = y + 12, bottom = y + size - 11;
            double left = cx - 11, right = cx + 11, hh = bottom - top;
            Path2D shield = new Path2D.Double();
            shield.moveTo(cx, top);
            shield.lineTo(right, top + hh * 0.2);
            shield.lineTo(right, top + hh * 0.5);
            shield.quadTo(right, top + hh * 0.85, cx, bottom);
            shield.quadTo(left, top + hh * 0.85, left, top + hh * 0.5);
            shield.lineTo(left, top + hh * 0.2);
            shield.closePath();
            g.setColor(Color.WHITE);
            g.setStroke(new BasicStroke(2.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.draw(shield);
            g.dispose();
        }
    }

    /** Figma drop shadow: X 0, Y 4, blur 4, #000000 25%. */
    private static class ShadowLabel extends JLabel {
        ShadowLabel(String text) { super(text); }

        @Override protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            smooth(g);
            g.setFont(getFont());
            int y = g.getFontMetrics().getAscent();

            // shadow text, drawn offscreen at (8, y + 4)
            BufferedImage img = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics2D ig = img.createGraphics();
            smooth(ig);
            ig.setFont(getFont());
            ig.setColor(new Color(0, 0, 0, 64));
            ig.drawString(getText(), 8, y + 4);
            ig.dispose();

            // 11x11 Gaussian blur (sigma 2 = Figma blur 4)
            float[] k = new float[121];
            float sum = 0;
            for (int i = 0; i < 121; i++)
                sum += k[i] = (float) Math.exp(-(Math.pow(i % 11 - 5, 2) + Math.pow(i / 11 - 5, 2)) / 8);
            for (int i = 0; i < 121; i++) k[i] /= sum;
            g.drawImage(new ConvolveOp(new Kernel(11, 11, k)).filter(img, null), 0, 0, null);

            // real text on top
            g.setColor(RED);
            g.drawString(getText(), 8, y);
            g.dispose();
        }
    }

    /** Rounded white box with soft shadow, left icon and a child text field. */
    private static class InputBox extends JPanel {
        enum Icon { MAIL, LOCK }
        private final Icon icon;

        InputBox(Icon icon, JTextField field) {
            this.icon = icon;
            setLayout(null);
            setOpaque(false);
            field.setBounds(34, 4, 275 - 34 - 44, 34);
            add(field);
        }

        @Override protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            smooth(g);
            int w = getWidth(), h = getHeight() - 5;

            // soft shadow
            for (int i = 4; i >= 1; i--) {
                g.setColor(new Color(0, 0, 0, 14));
                g.fill(new RoundRectangle2D.Double(1 - i * 0.3, 2 + i, w - 2 + i * 0.6, h, 18, 18));
            }
            // body
            g.setColor(Color.WHITE);
            g.fill(new RoundRectangle2D.Double(0, 0, w - 1, h, 18, 18));
            g.setColor(Color.BLACK);
            g.setStroke(new BasicStroke(1f));
            g.draw(new RoundRectangle2D.Double(0.5, 0.5, w - 2, h - 1, 18, 18));

            int cy = h / 2;
            if (icon == Icon.MAIL) {
                g.setColor(Color.BLACK);
                g.fillOval(10, cy - 8, 16, 16);
                g.setColor(Color.WHITE);
                g.setStroke(new BasicStroke(1.2f));
                g.drawRect(14, cy - 3, 8, 6);
                g.drawLine(14, cy - 3, 18, cy + 1);
                g.drawLine(22, cy - 3, 18, cy + 1);
            } else {
                g.setColor(Color.BLACK);
                g.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.draw(new Arc2D.Double(13, cy - 9, 9, 10, 0, 180, Arc2D.OPEN));
                g.fill(new RoundRectangle2D.Double(11, cy - 3, 13, 10, 3, 3));
            }
            g.dispose();
        }
    }

    /** Text field with placeholder text and no border. */
    private static class PlaceholderField extends JTextField {
        private final String placeholder;
        PlaceholderField(String placeholder) {
            this.placeholder = placeholder;
            setOpaque(false);
            setBorder(null);
            setFont(font(Font.PLAIN, 12f));
            setForeground(Color.BLACK);
            setCaretColor(Color.BLACK);
        }
        @Override protected void paintComponent(Graphics g0) {
            super.paintComponent(g0);
            if (getText().isEmpty()) {
                Graphics2D g = (Graphics2D) g0.create();
                smooth(g);
                g.setFont(getFont());
                g.setColor(new Color(60, 60, 60));
                FontMetrics fm = g.getFontMetrics();
                g.drawString(placeholder, 0, (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
                g.dispose();
            }
        }
    }

    /** Password field with placeholder text and no border. */
    private static class PlaceholderPassword extends JPasswordField {
        private final String placeholder;
        PlaceholderPassword(String placeholder) {
            this.placeholder = placeholder;
            setOpaque(false);
            setBorder(null);
            setFont(font(Font.PLAIN, 12f));
            setForeground(Color.BLACK);
            setCaretColor(Color.BLACK);
            setEchoChar('\u2022');
        }
        @Override protected void paintComponent(Graphics g0) {
            super.paintComponent(g0);
            if (getPassword().length == 0) {
                Graphics2D g = (Graphics2D) g0.create();
                smooth(g);
                g.setFont(getFont());
                g.setColor(new Color(60, 60, 60));
                FontMetrics fm = g.getFontMetrics();
                g.drawString(placeholder, 0, (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
                g.dispose();
            }
        }
    }

    /** Eye / eye-slash toggle button. */
    private static class EyeButton extends JButton {
        private boolean shown = false;
        EyeButton() {
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
        boolean isShown() { return shown; }
        void setShown(boolean s) { shown = s; repaint(); }

        @Override protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            smooth(g);
            int w = getWidth(), h = getHeight();
            double cx = w / 2.0, cy = h / 2.0;

            // eye outline
            Path2D eye = new Path2D.Double();
            eye.moveTo(cx - 10, cy);
            eye.quadTo(cx, cy - 11, cx + 10, cy);
            eye.quadTo(cx, cy + 11, cx - 10, cy);
            eye.closePath();
            g.setColor(Color.BLACK);
            g.fill(eye);
            g.setColor(Color.WHITE);
            g.fill(new Ellipse2D.Double(cx - 4, cy - 4, 8, 8));
            g.setColor(Color.BLACK);
            g.fill(new Ellipse2D.Double(cx - 2.5, cy - 2.5, 5, 5));

            if (!shown) { // slashed = password hidden
                g.setStroke(new BasicStroke(5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.setColor(Color.WHITE);
                g.draw(new Line2D.Double(cx - 9, cy + 9, cx + 9, cy - 9));
                g.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.setColor(Color.BLACK);
                g.draw(new Line2D.Double(cx - 9, cy + 9, cx + 9, cy - 9));
            }
            g.dispose();
        }
    }

    /** Rounded red button. */
    private static class RoundedButton extends JButton {
        private boolean hover, pressed;
        RoundedButton(String text) {
            super(text);
            setFont(font(Font.BOLD, 13f));
            setForeground(Color.WHITE);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
                @Override public void mouseExited(MouseEvent e)  { hover = false; repaint(); }
                @Override public void mousePressed(MouseEvent e) { pressed = true; repaint(); }
                @Override public void mouseReleased(MouseEvent e){ pressed = false; repaint(); }
            });
        }
        @Override protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            smooth(g);
            Color c = pressed ? RED.darker() : hover ? new Color(228, 52, 56) : RED;
            g.setColor(c);
            g.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 14, 14));
            g.setColor(Color.WHITE);
            g.setFont(getFont());
            FontMetrics fm = g.getFontMetrics();
            int x = (getWidth() - fm.stringWidth(getText())) / 2;
            int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
            g.drawString(getText(), x, y);
            g.dispose();
        }
    }

}