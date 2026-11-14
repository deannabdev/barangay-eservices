package barangay.eservices;
import javax.swing.*;
import javax.swing.plaf.basic.BasicComboBoxUI;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.Arrays;
import java.util.function.IntConsumer;

final class ProfileTheme {
    static final Color RED = new Color(211, 47, 47);
    static final Color RED_TOP = new Color(214, 42, 42);
    static final Color RED_BOTTOM = new Color(160, 28, 30);
    static final Color MAROON = new Color(150, 28, 31);
    static final Color NAVY = new Color(27, 27, 47);
    static final Color GRAY = new Color(125, 125, 135);
    static final Color HINT = new Color(125, 125, 135);
    static final Color LOCKED = new Color(110, 110, 120);

    private ProfileTheme() {}

    static Font font(int style, float size) {
        String[] names = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        String family = Arrays.asList(names).contains("Lexend") ? "Lexend" : "SansSerif";
        return new Font(family, style, 12).deriveFont(size);
    }

    static void smooth(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
    }

    static void drawCard(Graphics2D g, int w, int h, boolean hover) {
        for (int i = 3; i >= 1; i--) {
            g.setColor(new Color(0, 0, 0, 18));
            g.fill(new RoundRectangle2D.Double(0.5, i, w - 1, h, 14, 14));
        }
        g.setColor(hover ? new Color(250, 246, 246) : Color.WHITE);
        g.fill(new RoundRectangle2D.Double(0, 0, w, h, 14, 14));
        g.setColor(new Color(228, 228, 232));
        g.setStroke(new BasicStroke(1f));
        g.draw(new RoundRectangle2D.Double(0.5, 0.5, w - 1, h - 1, 14, 14));
    }

    static JComboBox<String> combo(String... items) {
        JComboBox<String> c = new JComboBox<>(items);
        c.setUI(new BasicComboBoxUI() {
            @Override protected JButton createArrowButton() {
                JButton b = new JButton() {
                    @Override protected void paintComponent(Graphics g0) {
                        Graphics2D g = (Graphics2D) g0.create();
                        smooth(g);
                        int cx = getWidth() / 2, cy = getHeight() / 2;
                        g.setColor(NAVY);
                        g.fill(new Polygon(new int[]{cx - 4, cx + 4, cx},
                                new int[]{cy - 2, cy - 2, cy + 3}, 3));
                        g.dispose();
                    }
                };
                b.setBorder(null);
                b.setContentAreaFilled(false);
                b.setFocusable(false);
                return b;
            }

            @Override public void paintCurrentValueBackground(Graphics g, Rectangle r, boolean focus) {}

            @Override public void paintCurrentValue(Graphics g, Rectangle r, boolean focus) {
                super.paintCurrentValue(g, r, false);
            }
        });
        c.setRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean selected, boolean focus) {
                JLabel l = (JLabel) super.getListCellRendererComponent(list, value, index, selected, focus);
                l.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 0));
                return l;
            }
        });
        c.setSelectedIndex(-1);
        c.setOpaque(false);
        c.setBorder(null);
        c.setFont(font(Font.PLAIN, 11f));
        c.setForeground(NAVY);
        c.setBackground(Color.WHITE);
        return c;
    }
}

/** White background with a thin separator under the header. */
class ProfileInfoBackgroundPanel extends JPanel {
    @Override protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0.create();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, getWidth(), getHeight());
        for (int i = 0; i < 4; i++) {
            g.setColor(new Color(0, 0, 0, 10 - i * 2));
            g.fillRect(0, 91 + i, getWidth(), 1);
        }
        g.setColor(new Color(228, 228, 232));
        g.drawLine(0, 90, getWidth(), 90);
        g.dispose();
    }
}

/** Maroon back arrow. */
class ProfileBackButton extends JComponent {
    ProfileBackButton(Runnable onClick) {
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { onClick.run(); }
        });
    }

    @Override protected void paintComponent(Graphics g0) {
        Graphics2D g = (Graphics2D) g0.create();
        ProfileTheme.smooth(g);
        double cx = getWidth() / 2.0, cy = getHeight() / 2.0;
        Path2D p = new Path2D.Double();
        p.moveTo(cx + 4, cy - 8);
        p.lineTo(cx - 4, cy);
        p.lineTo(cx + 4, cy + 8);
        g.setColor(ProfileTheme.MAROON);
        g.setStroke(new BasicStroke(2.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(p);
        g.dispose();
    }
}

/** Rounded form box with a navy border and optional lock or calendar icon. */
class ProfileFieldBox extends JPanel {
    private final JComponent child;
    private final int padL, padR;
    private final boolean calendar;
    private boolean locked;

    ProfileFieldBox(JComponent child, int padL, int padR, boolean calendar) {
        this.child = child;
        this.padL = padL;
        this.padR = padR;
        this.calendar = calendar;
        setLayout(null);
        setOpaque(false);
        add(child);
    }

    void setLocked(boolean locked) {
        this.locked = locked;
        child.setForeground(locked ? ProfileTheme.LOCKED : ProfileTheme.NAVY);
        repaint();
    }

    @Override public void doLayout() {
        child.setBounds(padL, 1, getWidth() - 4 - padL - padR, getHeight() - 6);
    }

    @Override protected void paintComponent(Graphics g0) {
        Graphics2D g = (Graphics2D) g0.create();
        ProfileTheme.smooth(g);
        int w = getWidth() - 4, h = getHeight() - 4;

        for (int i = 3; i >= 1; i--) {
            g.setColor(new Color(20, 20, 50, locked ? 14 : 28));
            g.fill(new RoundRectangle2D.Double(i, i, w, h, 8, 8));
        }
        g.setColor(locked ? new Color(241, 241, 244) : Color.WHITE);
        g.fill(new RoundRectangle2D.Double(0, 0, w, h, 8, 8));
        g.setColor(locked ? new Color(170, 170, 180) : ProfileTheme.NAVY);
        g.setStroke(new BasicStroke(1.2f));
        g.draw(new RoundRectangle2D.Double(0.6, 0.6, w - 1.2, h - 1.2, 8, 8));

        Color icon = locked ? new Color(150, 150, 160) : ProfileTheme.NAVY;
        if (calendar) {
            int x = w - 24, y = (h - 14) / 2 + 1;
            g.setColor(icon);
            g.fillRoundRect(x, y, 14, 13, 3, 3);
            g.fillRect(x + 3, y - 2, 2, 3);
            g.fillRect(x + 9, y - 2, 2, 3);
            g.setColor(locked ? new Color(241, 241, 244) : Color.WHITE);
            g.fillRect(x + 2, y + 5, 10, 6);
        } else if (locked) {
            int x = w - 22, y = (h - 12) / 2;
            g.setColor(icon);
            g.setStroke(new BasicStroke(1.4f));
            g.draw(new Arc2D.Double(x + 1.5, y, 6, 7, 0, 180, Arc2D.OPEN));
            g.fill(new RoundRectangle2D.Double(x, y + 4.5, 9, 7.5, 2, 2));
        }
        g.dispose();
    }
}

/** Borderless text field with placeholder text. */
class ProfilePlaceholderField extends JTextField {
    private final String placeholder;

    ProfilePlaceholderField(String placeholder) {
        this.placeholder = placeholder;
        setOpaque(false);
        setBorder(null);
        setFont(ProfileTheme.font(Font.PLAIN, 11f));
        setForeground(ProfileTheme.NAVY);
        setCaretColor(ProfileTheme.NAVY);
    }

    @Override protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        if (getText().isEmpty()) {
            Graphics2D g = (Graphics2D) g0.create();
            ProfileTheme.smooth(g);
            g.setFont(getFont().deriveFont(10f));
            g.setColor(ProfileTheme.HINT);
            FontMetrics fm = g.getFontMetrics();
            g.drawString(placeholder, 0, (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
            g.dispose();
        }
    }
}

/** Small rounded button for saving or cancelling profile edits. */
class ProfileActionButton extends JButton {
    private final boolean outlined;
    private boolean hover, pressed;

    ProfileActionButton(String text, boolean outlined) {
        super(text);
        this.outlined = outlined;
        setFont(ProfileTheme.font(Font.BOLD, 9f));
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

    @Override public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        setCursor(Cursor.getPredefinedCursor(enabled ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
        repaint();
    }

    @Override protected void paintComponent(Graphics g0) {
        Graphics2D g = (Graphics2D) g0.create();
        ProfileTheme.smooth(g);
        int w = getWidth(), h = getHeight();

        if (outlined) {
            g.setColor(pressed ? new Color(250, 225, 225) : hover ? new Color(253, 240, 240) : Color.WHITE);
            g.fill(new RoundRectangle2D.Double(0, 0, w, h, 8, 8));
            g.setColor(ProfileTheme.RED);
            g.setStroke(new BasicStroke(1.2f));
            g.draw(new RoundRectangle2D.Double(0.6, 0.6, w - 1.2, h - 1.2, 8, 8));
            g.setColor(ProfileTheme.RED);
        } else {
            Color fill = !isEnabled() ? new Color(205, 205, 211)
                    : pressed ? ProfileTheme.RED.darker()
                    : hover ? new Color(228, 52, 56) : ProfileTheme.RED;
            g.setColor(fill);
            g.fill(new RoundRectangle2D.Double(0, 0, w, h, 8, 8));
            g.setColor(Color.WHITE);
        }
        g.setFont(getFont());
        FontMetrics fm = g.getFontMetrics();
        g.drawString(getText(), (w - fm.stringWidth(getText())) / 2,
                (h - fm.getHeight()) / 2 + fm.getAscent());
        g.dispose();
    }
}

/** White background with the dark-red curved header. */
class ProfileScreenBackgroundPanel extends JPanel {
    @Override protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0.create();
        ProfileTheme.smooth(g);
        int w = getWidth();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, w, getHeight());

        Path2D header = new Path2D.Double();
        header.moveTo(0, 0);
        header.lineTo(w, 0);
        header.lineTo(w, 62);
        header.quadTo(w / 2.0, 164, 0, 62);
        header.closePath();
        g.setPaint(new GradientPaint(0, 0, ProfileTheme.RED_TOP, 0, 110, ProfileTheme.RED_BOTTOM));
        g.fill(header);
        g.dispose();
    }
}

/** Plain card used as the profile card background. */
class ProfileCardBox extends JComponent {
    @Override protected void paintComponent(Graphics g0) {
        Graphics2D g = (Graphics2D) g0.create();
        ProfileTheme.smooth(g);
        ProfileTheme.drawCard(g, getWidth(), getHeight() - 4, false);
        g.dispose();
    }
}

/** Maroon ring with a person outline and a small edit badge. */
class ProfileAvatarView extends JComponent {
    private static final double BADGE_X = 107, BADGE_Y = 112;

    ProfileAvatarView(Runnable onEdit) {
        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (Point2D.distance(e.getX(), e.getY(), BADGE_X, BADGE_Y) <= 11) onEdit.run();
            }
        });
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override public void mouseMoved(MouseEvent e) {
                boolean onBadge = Point2D.distance(e.getX(), e.getY(), BADGE_X, BADGE_Y) <= 11;
                setCursor(Cursor.getPredefinedCursor(onBadge ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
            }
        });
    }

    @Override protected void paintComponent(Graphics g0) {
        Graphics2D g = (Graphics2D) g0.create();
        ProfileTheme.smooth(g);
        g.setColor(ProfileTheme.MAROON);
        g.setStroke(new BasicStroke(4f));
        g.draw(new Ellipse2D.Double(5, 5, 130, 130));

        g.setClip(new Ellipse2D.Double(7, 7, 126, 126));
        g.setStroke(new BasicStroke(2f));
        g.draw(new Ellipse2D.Double(50, 24, 40, 40));
        g.draw(new Arc2D.Double(36, 68, 68, 64, 0, 180, Arc2D.OPEN));
        g.setClip(null);

        g.setColor(new Color(0, 0, 0, 40));
        g.fill(new Ellipse2D.Double(BADGE_X - 10, BADGE_Y - 9, 22, 22));
        g.setColor(Color.WHITE);
        g.fill(new Ellipse2D.Double(BADGE_X - 10, BADGE_Y - 10, 20, 20));
        g.setColor(ProfileTheme.NAVY);
        g.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(new Line2D.Double(BADGE_X - 3, BADGE_Y + 1.5, BADGE_X + 3, BADGE_Y - 4.5));
        g.setStroke(new BasicStroke(1.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(new Line2D.Double(BADGE_X - 5, BADGE_Y + 5, BADGE_X + 4, BADGE_Y + 5));
        g.dispose();
    }
}

/** Clickable card row: icon, text and a chevron. */
class ProfileMenuRow extends JComponent {
    enum Icon { NEWS, EXIT }

    private final String text;
    private final Icon icon;
    private final int cardHeight;
    private boolean hover;

    ProfileMenuRow(String text, Icon icon, int cardHeight, Runnable onClick) {
        this.text = text;
        this.icon = icon;
        this.cardHeight = cardHeight;
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
            @Override public void mouseExited(MouseEvent e) { hover = false; repaint(); }
            @Override public void mouseClicked(MouseEvent e) { onClick.run(); }
        });
    }

    @Override protected void paintComponent(Graphics g0) {
        Graphics2D g = (Graphics2D) g0.create();
        ProfileTheme.smooth(g);
        int w = getWidth(), h = cardHeight;
        ProfileTheme.drawCard(g, w, h, hover);

        double cy = h / 2.0;
        g.setColor(ProfileTheme.NAVY);
        g.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        if (icon == Icon.NEWS) {
            double x = 14, y = cy - 8;
            g.draw(new RoundRectangle2D.Double(x, y, 17, 15, 3, 3));
            g.fill(new Rectangle2D.Double(x + 3, y + 3.5, 5, 4.5));
            g.draw(new Line2D.Double(x + 10, y + 4.5, x + 14, y + 4.5));
            g.draw(new Line2D.Double(x + 10, y + 7.5, x + 14, y + 7.5));
            g.draw(new Line2D.Double(x + 3, y + 11, x + 14, y + 11));
        } else {
            double x = 14, y = cy - 8;
            Path2D door = new Path2D.Double();
            door.moveTo(x + 9, y);
            door.lineTo(x + 16, y);
            door.lineTo(x + 16, y + 16);
            door.lineTo(x + 9, y + 16);
            g.draw(door);
            g.draw(new Line2D.Double(x, y + 8, x + 11, y + 8));
            g.draw(new Line2D.Double(x + 7, y + 4, x + 11, y + 8));
            g.draw(new Line2D.Double(x + 7, y + 12, x + 11, y + 8));
        }

        g.setFont(ProfileTheme.font(Font.PLAIN, 11.5f));
        FontMetrics fm = g.getFontMetrics();
        g.drawString(text, 42, (int) (cy - fm.getHeight() / 2.0 + fm.getAscent()));
        double cx = w - 22;
        g.setColor(ProfileTheme.MAROON);
        g.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        Path2D chevron = new Path2D.Double();
        chevron.moveTo(cx - 2.5, cy - 4.5);
        chevron.lineTo(cx + 2.5, cy);
        chevron.lineTo(cx - 2.5, cy + 4.5);
        g.draw(chevron);
        g.dispose();
    }
}

/** One active request with its reference number and status. */
class ProfileRequestCard extends JComponent {
    private final String title, ref, status;

    ProfileRequestCard(String title, String ref, String status) {
        this.title = title;
        this.ref = ref;
        this.status = status;
    }

    @Override protected void paintComponent(Graphics g0) {
        Graphics2D g = (Graphics2D) g0.create();
        ProfileTheme.smooth(g);
        int w = getWidth(), h = getHeight() - 4;
        ProfileTheme.drawCard(g, w, h, false);
        double cy = h / 2.0;

        g.setColor(ProfileTheme.NAVY);
        g.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(new RoundRectangle2D.Double(19, cy - 10, 11, 14, 3, 3));
        g.setColor(Color.WHITE);
        g.fill(new RoundRectangle2D.Double(15, cy - 6, 12, 16, 3, 3));
        g.setColor(ProfileTheme.NAVY);
        g.draw(new RoundRectangle2D.Double(15, cy - 6, 12, 16, 3, 3));
        g.draw(new Line2D.Double(18, cy + 1, 24, cy + 1));
        g.draw(new Line2D.Double(18, cy + 5, 24, cy + 5));

        g.setColor(ProfileTheme.NAVY);
        g.setFont(ProfileTheme.font(Font.BOLD, 10f));
        FontMetrics fm = g.getFontMetrics();
        g.drawString(title, 44, (int) (cy - 3));
        g.setColor(ProfileTheme.GRAY);
        g.setFont(ProfileTheme.font(Font.PLAIN, 8.5f));
        g.drawString(ref, 44, (int) (cy + 12));

        Color bg, fg;
        switch (status.toUpperCase()) {
            case "APPROVED": bg = new Color(214, 240, 222); fg = new Color(30, 110, 55); break;
            case "REJECTED": bg = new Color(250, 218, 218); fg = new Color(180, 30, 30); break;
            default: bg = new Color(255, 236, 179); fg = new Color(230, 140, 0); break;
        }
        g.setFont(ProfileTheme.font(Font.BOLD, 8f));
        fm = g.getFontMetrics();
        int pw = fm.stringWidth(status) + 18, ph = 19;
        int px = w - pw - 12, py = (int) (cy - ph / 2.0);
        g.setColor(bg);
        g.fill(new RoundRectangle2D.Double(px, py, pw, ph, 8, 8));
        g.setColor(fg);
        g.drawString(status, px + (pw - fm.stringWidth(status)) / 2,
                py + (ph - fm.getHeight()) / 2 + fm.getAscent());
        g.dispose();
    }
}

/** Bottom navigation bar: Home, E-Services, Reports, Guide, Profile. */
class ProfileNavBar extends JComponent {
    private static final String[] LABELS = {"Home", "E-Services", "Reports", "Guide", "Profile"};
    private final int active;

    ProfileNavBar(int active, IntConsumer onSelect) {
        this.active = active;
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                onSelect.accept(Math.min(4, e.getX() * 5 / getWidth()));
            }
        });
    }

    @Override protected void paintComponent(Graphics g0) {
        Graphics2D g = (Graphics2D) g0.create();
        ProfileTheme.smooth(g);
        int w = getWidth();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, w, getHeight());
        g.setColor(new Color(225, 225, 230));
        g.drawLine(0, 0, w, 0);

        for (int i = 0; i < 5; i++) {
            double cx = w * (i + 0.5) / 5;
            g.setColor(i == active ? ProfileTheme.RED : ProfileTheme.NAVY);
            g.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            drawIcon(g, i, cx, 25);
            g.setFont(ProfileTheme.font(i == active ? Font.BOLD : Font.PLAIN, 9f));
            FontMetrics fm = g.getFontMetrics();
            g.drawString(LABELS[i], (int) (cx - fm.stringWidth(LABELS[i]) / 2.0), 49);
        }
        g.dispose();
    }

    private void drawIcon(Graphics2D g, int index, double cx, double cy) {
        Path2D p = new Path2D.Double();
        switch (index) {
            case 0:
                p.moveTo(cx - 10, cy - 1); p.lineTo(cx, cy - 10); p.lineTo(cx + 10, cy - 1);
                g.draw(p);
                p = new Path2D.Double();
                p.moveTo(cx - 7.5, cy - 3.5); p.lineTo(cx - 7.5, cy + 9);
                p.lineTo(cx + 7.5, cy + 9); p.lineTo(cx + 7.5, cy - 3.5);
                g.draw(p);
                g.draw(new Rectangle2D.Double(cx - 2.5, cy + 3, 5, 6));
                break;
            case 1:
                p.moveTo(cx - 7, cy - 10); p.lineTo(cx + 2, cy - 10); p.lineTo(cx + 8, cy - 4);
                p.lineTo(cx + 8, cy + 10); p.lineTo(cx - 7, cy + 10); p.closePath();
                g.draw(p);
                p = new Path2D.Double();
                p.moveTo(cx + 2, cy - 10); p.lineTo(cx + 2, cy - 4); p.lineTo(cx + 8, cy - 4);
                g.draw(p);
                g.draw(new Line2D.Double(cx - 3, cy + 1, cx + 4, cy + 1));
                g.draw(new Line2D.Double(cx - 3, cy + 5, cx + 4, cy + 5));
                break;
            case 2:
                p.moveTo(cx, cy - 10); p.lineTo(cx + 10.5, cy + 8); p.lineTo(cx - 10.5, cy + 8); p.closePath();
                g.draw(p);
                g.draw(new Line2D.Double(cx, cy - 3, cx, cy + 2));
                g.fill(new Ellipse2D.Double(cx - 1, cy + 3.5, 2, 2));
                break;
            case 3:
                p.moveTo(cx, cy - 6);
                p.quadTo(cx - 5, cy - 9, cx - 10, cy - 7);
                p.lineTo(cx - 10, cy + 7);
                p.quadTo(cx - 5, cy + 5, cx, cy + 9);
                p.quadTo(cx + 5, cy + 5, cx + 10, cy + 7);
                p.lineTo(cx + 10, cy - 7);
                p.quadTo(cx + 5, cy - 9, cx, cy - 6);
                g.draw(p);
                g.draw(new Line2D.Double(cx, cy - 6, cx, cy + 9));
                break;
            default:
                g.draw(new Ellipse2D.Double(cx - 10.5, cy - 10.5, 21, 21));
                g.draw(new Ellipse2D.Double(cx - 3.5, cy - 7, 7, 7));
                g.draw(new Arc2D.Double(cx - 6.5, cy + 1.5, 13, 12, 0, 180, Arc2D.OPEN));
        }
    }
}
