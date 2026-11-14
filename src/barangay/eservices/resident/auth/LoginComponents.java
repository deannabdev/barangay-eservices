package barangay.eservices;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.util.Arrays;

final class LoginTheme {
    static final Color RED = new Color(211, 47, 47);
    static final Color RED_TOP = new Color(214, 42, 42);
    static final Color RED_BOTTOM = new Color(160, 28, 30);
    static final Color GRAY_TEXT = new Color(120, 120, 120);

    private LoginTheme() {}

    static Font font(int style, float size) {
        String[] names = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        String family = Arrays.asList(names).contains("Lexend") ? "Lexend" : "SansSerif";
        return new Font(family, style, 12).deriveFont(size);
    }
}

/** Red gradient header and rounded white sheet used by the login screens. */
class LoginBackgroundPanel extends JPanel {
    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0.create();
        Theme.smooth(g);
        int w = getWidth(), h = getHeight();

        g.setPaint(new GradientPaint(0, 0, LoginTheme.RED_TOP, 0, 230, LoginTheme.RED_BOTTOM));
        g.fillRect(0, 0, w, h);

        g.setColor(Color.WHITE);
        g.fill(new RoundRectangle2D.Double(0, 215, w, h, 90, 90));

        int size = 52, x = (w - size) / 2, y = 68;
        g.setColor(new Color(0, 0, 0, 60));
        g.fill(new RoundRectangle2D.Double(x, y + 5, size, size, 16, 16));
        g.setColor(new Color(222, 45, 48));
        g.fill(new RoundRectangle2D.Double(x, y, size, size, 16, 16));
        g.setColor(new Color(120, 15, 18));
        g.setStroke(new BasicStroke(1.2f));
        g.draw(new RoundRectangle2D.Double(x, y, size, size, 16, 16));

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

/** Rounded white login input with a mail or lock icon. */
class LoginInputBox extends JPanel {
    enum Icon { MAIL, LOCK }

    private final Icon icon;

    LoginInputBox(Icon icon, JTextField field) {
        this.icon = icon;
        setLayout(null);
        setOpaque(false);
        field.setBounds(34, 4, 275 - 34 - 44, 34);
        add(field);
    }

    @Override
    protected void paintComponent(Graphics g0) {
        Graphics2D g = (Graphics2D) g0.create();
        Theme.smooth(g);
        int w = getWidth(), h = getHeight() - 5;

        for (int i = 4; i >= 1; i--) {
            g.setColor(new Color(0, 0, 0, 14));
            g.fill(new RoundRectangle2D.Double(1 - i * 0.3, 2 + i, w - 2 + i * 0.6, h, 18, 18));
        }
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

/** Password input with a placeholder, styled for the login screen. */
class PlaceholderPasswordField extends JPasswordField {
    private final String hint;

    PlaceholderPasswordField(String hint, Font font) {
        this.hint = hint;
        setOpaque(false);
        setBorder(null);
        setFont(font);
        setForeground(Color.BLACK);
        setCaretColor(Color.BLACK);
        setEchoChar('\u2022');
    }

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        if (getPassword().length == 0) {
            Graphics2D g = (Graphics2D) g0.create();
            Theme.smooth(g);
            g.setFont(getFont());
            g.setColor(new Color(60, 60, 60));
            FontMetrics fm = g.getFontMetrics();
            g.drawString(hint, 0, (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
            g.dispose();
        }
    }
}

/** Eye / eye-slash button for toggling password visibility. */
class PasswordVisibilityButton extends JButton {
    private boolean shown;

    PasswordVisibilityButton() {
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    boolean isShown() { return shown; }
    void setShown(boolean shown) { this.shown = shown; repaint(); }

    @Override
    protected void paintComponent(Graphics g0) {
        Graphics2D g = (Graphics2D) g0.create();
        Theme.smooth(g);
        double cx = getWidth() / 2.0, cy = getHeight() / 2.0;

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

        if (!shown) {
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

/** Rounded red button used on the login screens. */
class LoginButton extends JButton {
    private boolean hover;
    private boolean pressed;

    LoginButton(String text, Font font) {
        super(text);
        setFont(font);
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

    @Override
    protected void paintComponent(Graphics g0) {
        Graphics2D g = (Graphics2D) g0.create();
        Theme.smooth(g);
        Color c = pressed ? LoginTheme.RED.darker()
                : hover ? new Color(228, 52, 56) : LoginTheme.RED;
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
