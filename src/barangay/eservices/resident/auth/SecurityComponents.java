package barangay.eservices;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.Arrays;

final class SecurityTheme {
    static final Color RED = new Color(211, 47, 47);
    static final Color MAROON = new Color(150, 28, 31);
    static final Color NAVY = new Color(27, 27, 47);
    static final Color HINT = new Color(125, 125, 135);

    private SecurityTheme() {}

    static Font font(int style, float size) {
        String[] names = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        String family = Arrays.asList(names).contains("Lexend") ? "Lexend" : "SansSerif";
        return new Font(family, style, 12).deriveFont(size);
    }

    static void smooth(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    }
}

/** White background with the gray band that separates the two sections. */
class SecurityBackgroundPanel extends JPanel {
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

/** Maroon back arrow. */
class SecurityBackButton extends JComponent {
    SecurityBackButton(Runnable onClick) {
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { onClick.run(); }
        });
    }

    @Override protected void paintComponent(Graphics g0) {
        Graphics2D g = (Graphics2D) g0.create();
        SecurityTheme.smooth(g);
        double cx = getWidth() / 2.0, cy = getHeight() / 2.0;
        Path2D p = new Path2D.Double();
        p.moveTo(cx + 4, cy - 8);
        p.lineTo(cx - 4, cy);
        p.lineTo(cx + 4, cy + 8);
        g.setColor(SecurityTheme.MAROON);
        g.setStroke(new BasicStroke(2.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(p);
        g.dispose();
    }
}

/** Rounded white box with a navy border, soft shadow and a child component inside. */
class SecurityFieldBox extends JPanel {
    private final JComponent child;
    private final int padL, padR;

    SecurityFieldBox(JComponent child, int padL, int padR) {
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
        SecurityTheme.smooth(g);
        int w = getWidth() - 4, h = getHeight() - 4;

        for (int i = 3; i >= 1; i--) {
            g.setColor(new Color(20, 20, 50, 28));
            g.fill(new RoundRectangle2D.Double(i, i, w, h, 10, 10));
        }
        g.setColor(Color.WHITE);
        g.fill(new RoundRectangle2D.Double(0, 0, w, h, 10, 10));
        g.setColor(SecurityTheme.NAVY);
        g.setStroke(new BasicStroke(1.2f));
        g.draw(new RoundRectangle2D.Double(0.6, 0.6, w - 1.2, h - 1.2, 10, 10));
        g.dispose();
    }
}

/** Borderless password field with placeholder text. */
class SecurityPasswordField extends JPasswordField {
    private final String placeholder;

    SecurityPasswordField(String placeholder) {
        this.placeholder = placeholder;
        setOpaque(false);
        setBorder(null);
        setFont(SecurityTheme.font(Font.PLAIN, 11f));
        setForeground(SecurityTheme.NAVY);
        setCaretColor(SecurityTheme.NAVY);
        setEchoChar('\u2022');
    }

    @Override protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        if (getPassword().length == 0) {
            Graphics2D g = (Graphics2D) g0.create();
            SecurityTheme.smooth(g);
            g.setFont(getFont().deriveFont(10f));
            g.setColor(SecurityTheme.HINT);
            FontMetrics fm = g.getFontMetrics();
            g.drawString(placeholder, 0, (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
            g.dispose();
        }
    }
}

/** Eye / eye-slash toggle button (slashed = password hidden). */
class SecurityEyeButton extends JButton {
    private boolean shown;

    SecurityEyeButton() {
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    boolean isShown() { return shown; }
    void setShown(boolean shown) { this.shown = shown; repaint(); }

    @Override protected void paintComponent(Graphics g0) {
        Graphics2D g = (Graphics2D) g0.create();
        SecurityTheme.smooth(g);
        double cx = getWidth() / 2.0, cy = getHeight() / 2.0;

        Path2D eye = new Path2D.Double();
        eye.moveTo(cx - 9, cy);
        eye.quadTo(cx, cy - 9, cx + 9, cy);
        eye.quadTo(cx, cy + 9, cx - 9, cy);
        eye.closePath();
        g.setColor(SecurityTheme.NAVY);
        g.fill(eye);
        g.setColor(Color.WHITE);
        g.fill(new Ellipse2D.Double(cx - 3.5, cy - 3.5, 7, 7));
        g.setColor(SecurityTheme.NAVY);
        g.fill(new Ellipse2D.Double(cx - 2, cy - 2, 4, 4));

        if (!shown) {
            g.setStroke(new BasicStroke(4.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.setColor(Color.WHITE);
            g.draw(new Line2D.Double(cx - 8, cy + 8, cx + 8, cy - 8));
            g.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.setColor(SecurityTheme.NAVY);
            g.draw(new Line2D.Double(cx - 8, cy + 8, cx + 8, cy - 8));
        }
        g.dispose();
    }
}

/** Rounded button with a soft shadow: solid red, or white with a red outline. */
class SecurityButton extends JButton {
    private final boolean outlined;
    private boolean hover, pressed;

    SecurityButton(String text, boolean outlined) {
        super(text);
        this.outlined = outlined;
        setFont(SecurityTheme.font(Font.BOLD, 13f));
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
            @Override public void mouseExited(MouseEvent e) { hover = false; repaint(); }
            @Override public void mousePressed(MouseEvent e) { pressed = true; repaint(); }
            @Override public void mouseReleased(MouseEvent e) { pressed = false; repaint(); }
        });
    }

    @Override protected void paintComponent(Graphics g0) {
        Graphics2D g = (Graphics2D) g0.create();
        SecurityTheme.smooth(g);
        int w = getWidth(), h = getHeight() - 4;

        for (int i = 3; i >= 1; i--) {
            g.setColor(new Color(0, 0, 0, 20));
            g.fill(new RoundRectangle2D.Double(0.5, i, w - 1, h, 12, 12));
        }

        if (outlined) {
            g.setColor(pressed ? new Color(250, 225, 225) : hover ? new Color(253, 240, 240) : Color.WHITE);
            g.fill(new RoundRectangle2D.Double(0, 0, w, h, 12, 12));
            g.setColor(SecurityTheme.RED);
            g.setStroke(new BasicStroke(1.4f));
            g.draw(new RoundRectangle2D.Double(0.7, 0.7, w - 1.4, h - 1.4, 12, 12));
            g.setColor(SecurityTheme.RED);
        } else {
            g.setColor(pressed ? SecurityTheme.RED.darker()
                    : hover ? new Color(228, 52, 56) : SecurityTheme.RED);
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
