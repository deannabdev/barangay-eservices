import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.Arrays;

public class Security extends JFrame {

    // ---- Colors ----
    private static final Color RED    = new Color(211, 47, 47);
    private static final Color MAROON = new Color(150, 28, 31);
    private static final Color NAVY   = new Color(27, 27, 47);
    private static final Color HINT   = new Color(125, 125, 135);

    private static final int W = 351, H = 775;
    private static final int X0 = 29, FW = 291;   // left edge and width of the content column

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

    // ---- Form fields ----
    private final PlaceholderPassword currentPassword = new PlaceholderPassword("Current password");
    private final PlaceholderPassword newPassword     = new PlaceholderPassword("New password");
    private final PlaceholderPassword retypePassword  = new PlaceholderPassword("Re-type new password");

    public Security() {
        super("Security & Log In");
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
        // Header: back arrow + title
        BackButton back = new BackButton(this::goBack);
        back.setBounds(18, 50, 32, 32);
        root.add(back);

        JLabel title = new JLabel("SECURITY & LOG IN", SwingConstants.CENTER);
        title.setFont(font(Font.BOLD, 20f));
        title.setForeground(Color.BLACK);
        title.setBounds(0, 48, W, 36);
        root.add(title);

        // ---- Change password ----
        JLabel changeTitle = new JLabel("Change password");
        changeTitle.setFont(font(Font.BOLD, 13f));
        changeTitle.setForeground(Color.BLACK);
        changeTitle.setBounds(X0, 101, FW, 18);
        root.add(changeTitle);

        JLabel changeHint = new JLabel("Your password must be at least 8 characters.");
        changeHint.setFont(font(Font.PLAIN, 9f));
        changeHint.setForeground(Color.BLACK);
        changeHint.setBounds(X0, 117, FW, 13);
        root.add(changeHint);

        addPasswordField(root, currentPassword, 139);
        addPasswordField(root, newPassword, 185);
        addPasswordField(root, retypePassword, 231);

        JLabel forgot = new JLabel("Forgot your password?", SwingConstants.RIGHT);
        forgot.setFont(font(Font.PLAIN, 9.5f));
        forgot.setForeground(new Color(110, 110, 120));
        forgot.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        forgot.setBounds(X0, 270, FW, 14);
        forgot.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                // TODO: open your "forgot password" flow.
                JOptionPane.showMessageDialog(Security.this, "Forgot password - coming soon.");
            }
        });
        root.add(forgot);

        RoundedButton change = new RoundedButton("Change password", false);
        change.setBounds(X0, 297, FW, 35);
        change.addActionListener(e -> onChangePassword());
        root.add(change);

        // ---- Delete account ----
        JLabel deleteTitle = new JLabel("Delete account");
        deleteTitle.setFont(font(Font.BOLD, 13f));
        deleteTitle.setForeground(Color.BLACK);
        deleteTitle.setBounds(X0, 354, FW, 18);
        root.add(deleteTitle);

        JLabel deleteText = new JLabel(
                "<html><div style='width:" + FW + "px'><b>Deleting your account is permanent.</b> "
                + "When you delete your account, your main profile and everything else you've added "
                + "will be permanently deleted. You won't be able to retrieve any information from "
                + "this account.</div></html>");
        deleteText.setFont(font(Font.PLAIN, 9.5f));
        deleteText.setForeground(Color.BLACK);
        deleteText.setVerticalAlignment(SwingConstants.TOP);
        deleteText.setBounds(X0, 376, FW, 74);
        root.add(deleteText);

        RoundedButton delete = new RoundedButton("Delete Account", true);
        delete.setBounds(X0, 462, FW, 35);
        delete.addActionListener(e -> onDeleteAccount());
        root.add(delete);
    }

    /** Password input (30px tall) with a show/hide eye on the right. */
    private void addPasswordField(JPanel root, PlaceholderPassword pf, int y) {
        FieldBox box = new FieldBox(pf, 10, 36);
        box.setBounds(X0, y, FW + 4, 34);

        EyeButton eye = new EyeButton();
        eye.setBounds(FW - 32, 4, 24, 22);
        eye.addActionListener(e -> {
            eye.setShown(!eye.isShown());
            pf.setEchoChar(eye.isShown() ? (char) 0 : '\u2022');
        });
        box.add(eye);
        root.add(box);
    }

    // ---- Actions ----

    private void goBack() {
        new ProfileScreen().setVisible(true);
        dispose();
    }

    private void onChangePassword() {
        String current = new String(currentPassword.getPassword());
        String next    = new String(newPassword.getPassword());
        String retype  = new String(retypePassword.getPassword());

        String problem = null;
        if (current.isEmpty())              problem = "Please enter your current password.";
        else if (next.length() < 8)         problem = "Your new password must be at least 8 characters.";
        else if (next.equals(current))      problem = "Your new password must be different from the current one.";
        else if (!next.equals(retype))      problem = "The new passwords do not match.";

        if (problem != null) {
            JOptionPane.showMessageDialog(this, problem, "Change password", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // TODO: check the current password and save the new one (database / API) here.
        JOptionPane.showMessageDialog(this, "Your password has been changed.");
        goBack();
    }

    private void onDeleteAccount() {
        Object[] options = {"Delete", "Cancel"};
        int answer = JOptionPane.showOptionDialog(this,
                "Your account and everything you've added will be permanently deleted.\n"
                + "This cannot be undone.",
                "Delete account?", JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
                null, options, options[1]);
        if (answer == 0) {
            // TODO: delete the account (database / API) here.
            JOptionPane.showMessageDialog(this, "Your account has been deleted.");
            new LoginScreen().setVisible(true);
            dispose();
        }
    }

    // =====================================================================
    //  Custom components
    // =====================================================================

    /** White background with the gray band that separates the two sections. */
    private static class BackgroundPanel extends JPanel {
        @Override protected void paintComponent(Graphics g0) {
            super.paintComponent(g0);
            Graphics2D g = (Graphics2D) g0.create();
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, getWidth(), getHeight());
            g.setColor(new Color(238, 238, 241));
            g.fillRect(0, 337, getWidth(), 10);
            g.dispose();
        }
    }

    /** Maroon "<" arrow. */
    private static class BackButton extends JComponent {
        BackButton(Runnable onClick) {
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) { onClick.run(); }
            });
        }

        @Override protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            smooth(g);
            double cx = getWidth() / 2.0, cy = getHeight() / 2.0;
            Path2D p = new Path2D.Double();
            p.moveTo(cx + 4, cy - 8);
            p.lineTo(cx - 4, cy);
            p.lineTo(cx + 4, cy + 8);
            g.setColor(MAROON);
            g.setStroke(new BasicStroke(2.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.draw(p);
            g.dispose();
        }
    }

    /** Rounded white box with a navy border, soft shadow and a child component inside. */
    private static class FieldBox extends JPanel {
        private final JComponent child;
        private final int padL, padR;

        FieldBox(JComponent child, int padL, int padR) {
            this.child = child;
            this.padL = padL;
            this.padR = padR;
            setLayout(null);
            setOpaque(false);
            add(child);
        }

        @Override public void doLayout() {
            child.setBounds(padL, 1, getWidth() - 4 - padL - padR, getHeight() - 6);
        }

        @Override protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            smooth(g);
            int w = getWidth() - 4, h = getHeight() - 4;

            for (int i = 3; i >= 1; i--) {                 // shadow to the right / bottom
                g.setColor(new Color(20, 20, 50, 28));
                g.fill(new RoundRectangle2D.Double(i, i, w, h, 10, 10));
            }
            g.setColor(Color.WHITE);
            g.fill(new RoundRectangle2D.Double(0, 0, w, h, 10, 10));
            g.setColor(NAVY);
            g.setStroke(new BasicStroke(1.2f));
            g.draw(new RoundRectangle2D.Double(0.6, 0.6, w - 1.2, h - 1.2, 10, 10));
            g.dispose();
        }
    }

    /** Borderless password field with placeholder text. */
    private static class PlaceholderPassword extends JPasswordField {
        private final String placeholder;

        PlaceholderPassword(String placeholder) {
            this.placeholder = placeholder;
            setOpaque(false);
            setBorder(null);
            setFont(font(Font.PLAIN, 11f));
            setForeground(NAVY);
            setCaretColor(NAVY);
            setEchoChar('\u2022');
        }

        @Override protected void paintComponent(Graphics g0) {
            super.paintComponent(g0);
            if (getPassword().length == 0) {
                Graphics2D g = (Graphics2D) g0.create();
                smooth(g);
                g.setFont(getFont().deriveFont(10f));
                g.setColor(HINT);
                FontMetrics fm = g.getFontMetrics();
                g.drawString(placeholder, 0, (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
                g.dispose();
            }
        }
    }

    /** Eye / eye-slash toggle button (slashed = password hidden). */
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
            double cx = getWidth() / 2.0, cy = getHeight() / 2.0;

            Path2D eye = new Path2D.Double();
            eye.moveTo(cx - 9, cy);
            eye.quadTo(cx, cy - 9, cx + 9, cy);
            eye.quadTo(cx, cy + 9, cx - 9, cy);
            eye.closePath();
            g.setColor(NAVY);
            g.fill(eye);
            g.setColor(Color.WHITE);
            g.fill(new Ellipse2D.Double(cx - 3.5, cy - 3.5, 7, 7));
            g.setColor(NAVY);
            g.fill(new Ellipse2D.Double(cx - 2, cy - 2, 4, 4));

            if (!shown) {
                g.setStroke(new BasicStroke(4.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.setColor(Color.WHITE);
                g.draw(new Line2D.Double(cx - 8, cy + 8, cx + 8, cy - 8));
                g.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.setColor(NAVY);
                g.draw(new Line2D.Double(cx - 8, cy + 8, cx + 8, cy - 8));
            }
            g.dispose();
        }
    }

    /** Rounded button with a soft shadow: solid red, or white with a red outline (Delete Account). */
    private static class RoundedButton extends JButton {
        private final boolean outlined;
        private boolean hover, pressed;

        RoundedButton(String text, boolean outlined) {
            super(text);
            this.outlined = outlined;
            setFont(font(Font.BOLD, 13f));
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e)  { hover = true;  repaint(); }
                @Override public void mouseExited(MouseEvent e)   { hover = false; repaint(); }
                @Override public void mousePressed(MouseEvent e)  { pressed = true;  repaint(); }
                @Override public void mouseReleased(MouseEvent e) { pressed = false; repaint(); }
            });
        }

        @Override protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            smooth(g);
            int w = getWidth(), h = getHeight() - 4;

            for (int i = 3; i >= 1; i--) {                 // soft shadow underneath
                g.setColor(new Color(0, 0, 0, 20));
                g.fill(new RoundRectangle2D.Double(0.5, i, w - 1, h, 12, 12));
            }

            if (outlined) {
                g.setColor(pressed ? new Color(250, 225, 225) : hover ? new Color(253, 240, 240) : Color.WHITE);
                g.fill(new RoundRectangle2D.Double(0, 0, w, h, 12, 12));
                g.setColor(RED);
                g.setStroke(new BasicStroke(1.4f));
                g.draw(new RoundRectangle2D.Double(0.7, 0.7, w - 1.4, h - 1.4, 12, 12));
                g.setColor(RED);
            } else {
                g.setColor(pressed ? RED.darker() : hover ? new Color(228, 52, 56) : RED);
                g.fill(new RoundRectangle2D.Double(0, 0, w, h, 12, 12));
                g.setColor(Color.WHITE);
            }
            g.setFont(getFont());
            FontMetrics fm = g.getFontMetrics();
            g.drawString(getText(), (w - fm.stringWidth(getText())) / 2,
                    (h - fm.getHeight()) / 2 + fm.getAscent());
            g.dispose();
        }
    }

}