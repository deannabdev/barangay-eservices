package barangay.eservices;
import javax.swing.*;
import javax.swing.plaf.basic.BasicComboBoxUI;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.*;
import java.util.Arrays;

final class RegistrationTheme {
    static final Color RED = new Color(211, 47, 47);
    static final Color RED_TOP = new Color(214, 42, 42);
    static final Color RED_BOTTOM = new Color(160, 28, 30);
    static final Color NAVY = new Color(27, 27, 47);
    static final Color HINT = new Color(125, 125, 135);
    static final Color SUCCESS_GREEN = new Color(36, 94, 56);

    private RegistrationTheme() {}

    static Font font(int style, float size) {
        String[] names = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        String family = Arrays.asList(names).contains("Lexend") ? "Lexend" : "SansSerif";
        return new Font(family, style, 12).deriveFont(size);
    }

    static PlaceholderField field(String hint) {
        return new PlaceholderField(hint, font(Font.PLAIN, 11f), NAVY, HINT, NAVY, font(Font.PLAIN, 10f));
    }
}

/** Red gradient header and rounded white sheet used by registration step one. */
class RegistrationBackgroundPanel extends JPanel {
    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0.create();
        Theme.smooth(g);
        g.setPaint(new GradientPaint(0, 0, RegistrationTheme.RED_TOP,
                0, 80, RegistrationTheme.RED_BOTTOM));
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setColor(Color.WHITE);
        g.fill(new RoundRectangle2D.Double(0, 62, getWidth(), getHeight(), 80, 80));
        g.dispose();
    }
}

/** Three numbered circles joined by lines for the registration flow. */
class RegistrationStepIndicator extends JComponent {
    private final Font font;
    private final Color color;
    private final boolean detailedLines;
    private int step = 1;

    RegistrationStepIndicator(Font font, Color color) {
        this(font, color, false);
    }

    RegistrationStepIndicator(Font font, Color color, boolean detailedLines) {
        this.font = font;
        this.color = color;
        this.detailedLines = detailedLines;
    }

    void setStep(int step) { this.step = step; repaint(); }

    @Override
    protected void paintComponent(Graphics g0) {
        Graphics2D g = (Graphics2D) g0.create();
        Theme.smooth(g);
        int center = getWidth() / 2;
        int margin = (int) (getWidth() * 0.14);
        int[] cx = detailedLines
                ? new int[]{center - 127, center, center + 127}
                : new int[]{margin, center, getWidth() - margin};
        int cy = getHeight() / 2, r = 14;

        for (int i = 0; i < 2; i++) {
            boolean done = detailedLines && step >= i + 2;
            g.setStroke(new BasicStroke(detailedLines ? (done ? 2.5f : 1.2f) : 1.5f));
            g.setColor(detailedLines && !done
                    ? new Color(color.getRed(), color.getGreen(), color.getBlue(), 140)
                    : color);
            g.drawLine(cx[i] + r, cy, cx[i + 1] - r, cy);
        }

        g.setFont(font);
        FontMetrics fm = g.getFontMetrics();
        for (int i = 0; i < 3; i++) {
            boolean active = i + 1 <= step;
            Ellipse2D circle = new Ellipse2D.Double(cx[i] - r, cy - r, 2 * r, 2 * r);
            g.setColor(active ? color : Color.WHITE);
            g.fill(circle);
            g.setColor(color);
            if (detailedLines) g.setStroke(new BasicStroke(1.5f));
            g.draw(circle);
            g.setColor(active ? Color.WHITE : color);
            String number = String.valueOf(i + 1);
            g.drawString(number, cx[i] - fm.stringWidth(number) / 2,
                    cy + (fm.getAscent() - fm.getDescent()) / 2);
        }
        g.dispose();
    }
}

/** Person icon and heading for the personal-information section. */
class RegistrationSectionTitle extends JComponent {
    enum Icon { PERSON, HOUSE, CONTACT, ID_CARD, LOCK }

    private final String text;
    private final Font font;
    private final Color color;
    private final Icon icon;

    RegistrationSectionTitle(String text, Font font, Color color) {
        this(text, font, color, Icon.PERSON);
    }

    RegistrationSectionTitle(String text, Font font, Color color, Icon icon) {
        this.text = text;
        this.font = font;
        this.color = color;
        this.icon = icon;
    }

    @Override
    protected void paintComponent(Graphics g0) {
        Graphics2D g = (Graphics2D) g0.create();
        Theme.smooth(g);
        g.setColor(color);
        switch (icon) {
            case PERSON:
                g.fill(new Ellipse2D.Double(5, 1, 9, 9));
                g.fill(new Arc2D.Double(1, 11, 18, 18, 0, 180, Arc2D.CHORD));
                break;
            case HOUSE:
                g.fillPolygon(new int[]{0, 10, 20}, new int[]{10, 1, 10}, 3);
                g.fillRect(3, 9, 14, 10);
                g.fillRect(14, 2, 3, 5);
                g.setColor(Color.WHITE);
                g.fillRect(8, 13, 4, 6);
                g.setColor(color);
                break;
            case CONTACT:
                g.fill(new RoundRectangle2D.Double(1, 1, 16, 20, 4, 4));
                g.fillRect(17, 5, 3, 3);
                g.fillRect(17, 10, 3, 3);
                g.fillRect(17, 15, 3, 3);
                g.setColor(Color.WHITE);
                g.fill(new Ellipse2D.Double(5.5, 5, 6, 6));
                g.fill(new Arc2D.Double(3.5, 12, 10, 10, 0, 180, Arc2D.CHORD));
                g.setColor(color);
                break;
            case ID_CARD:
                g.fill(new RoundRectangle2D.Double(0, 3, 20, 15, 4, 4));
                g.setColor(Color.WHITE);
                g.fill(new Ellipse2D.Double(3.5, 6, 5, 5));
                g.fill(new Arc2D.Double(2.5, 11.5, 7, 7, 0, 180, Arc2D.CHORD));
                g.fillRect(12, 7, 5, 2);
                g.fillRect(12, 11, 5, 2);
                g.setColor(color);
                break;
            case LOCK:
                g.setStroke(new BasicStroke(2.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.draw(new Arc2D.Double(5, 1.5, 10, 14, 0, 180, Arc2D.OPEN));
                g.fill(new RoundRectangle2D.Double(2, 9, 16, 12, 4, 4));
                break;
            default:
                throw new IllegalStateException("Unsupported registration section icon: " + icon);
        }
        g.setFont(font);
        FontMetrics fm = g.getFontMetrics();
        g.drawString(text, 30, (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
        g.dispose();
    }
}

/** Rounded form field with a navy border, shadow, and optional calendar icon. */
class RegistrationFieldBox extends JPanel {
    enum Deco { NONE, CALENDAR, LOCK }

    private final JComponent child;
    private final int padL;
    private final int padR;
    private final Deco deco;
    private final Color color;

    RegistrationFieldBox(JComponent child, int padL, int padR, boolean calendar, Color color) {
        this(child, padL, padR, calendar ? Deco.CALENDAR : Deco.NONE, color);
    }

    RegistrationFieldBox(JComponent child) {
        this(child, 10, 16, Deco.NONE, RegistrationTheme.NAVY);
    }

    RegistrationFieldBox(JComponent child, int padL, int padR, Deco deco, Color color) {
        this.child = child;
        this.padL = padL;
        this.padR = padR;
        this.deco = deco;
        this.color = color;
        setLayout(null);
        setOpaque(false);
        add(child);
    }

    @Override
    public void doLayout() {
        child.setBounds(padL, 1, getWidth() - 4 - padL - padR, getHeight() - 6);
    }

    @Override
    protected void paintComponent(Graphics g0) {
        Graphics2D g = (Graphics2D) g0.create();
        Theme.smooth(g);
        int w = getWidth() - 4, h = getHeight() - 4;

        for (int i = 3; i >= 1; i--) {
            g.setColor(new Color(20, 20, 50, 28));
            g.fill(new RoundRectangle2D.Double(i, i, w, h, 8, 8));
        }
        g.setColor(Color.WHITE);
        g.fill(new RoundRectangle2D.Double(0, 0, w, h, 8, 8));
        g.setColor(color);
        g.setStroke(new BasicStroke(1.2f));
        g.draw(new RoundRectangle2D.Double(0.6, 0.6, w - 1.2, h - 1.2, 8, 8));

        if (deco == Deco.CALENDAR) {
            int x = w - 24, y = (h - 14) / 2 + 1;
            g.setColor(color);
            g.fillRoundRect(x, y, 14, 13, 3, 3);
            g.fillRect(x + 3, y - 2, 2, 3);
            g.fillRect(x + 9, y - 2, 2, 3);
            g.setColor(Color.WHITE);
            g.fillRect(x + 2, y + 5, 10, 6);
        } else if (deco == Deco.LOCK) {
            int x = 11, y = (h - 13) / 2 + 1;
            g.setColor(new Color(70, 70, 85));
            g.setStroke(new BasicStroke(1.6f));
            g.draw(new Arc2D.Double(x + 1.5, y, 7, 9, 0, 180, Arc2D.OPEN));
            g.fill(new RoundRectangle2D.Double(x, y + 5, 10, 8, 2, 2));
        }
        g.dispose();
    }
}

/** Borderless registration combo box with a custom arrow and renderer. */
class RegistrationComboBox extends JComboBox<String> {
    RegistrationComboBox(Font font, Color color, String... items) {
        super(items);
        setUI(new BasicComboBoxUI() {
            @Override
            protected JButton createArrowButton() {
                JButton button = new JButton() {
                    @Override
                    protected void paintComponent(Graphics g0) {
                        Graphics2D g = (Graphics2D) g0.create();
                        Theme.smooth(g);
                        int cx = getWidth() / 2, cy = getHeight() / 2;
                        g.setColor(color);
                        g.fill(new Polygon(new int[]{cx - 4, cx + 4, cx},
                                new int[]{cy - 2, cy - 2, cy + 3}, 3));
                        g.dispose();
                    }
                };
                button.setBorder(null);
                button.setContentAreaFilled(false);
                button.setFocusable(false);
                return button;
            }

            @Override
            public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean focus) {}

            @Override
            public void paintCurrentValue(Graphics g, Rectangle bounds, boolean focus) {
                super.paintCurrentValue(g, bounds, false);
            }
        });
        setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean selected, boolean focus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(
                        list, value, index, selected, focus);
                label.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 0));
                return label;
            }
        });
        setSelectedIndex(-1);
        setOpaque(false);
        setBorder(null);
        setFont(font);
        setForeground(color);
        setBackground(Color.WHITE);
    }
}

/** Rounded red registration button with hover and pressed states. */
class RegistrationButton extends JButton {
    private final Color color;
    private final boolean outlined;
    private boolean hover;
    private boolean pressed;

    RegistrationButton(String text, Font font, Color color) {
        this(text, font, color, false);
    }

    RegistrationButton(String text, Font font, Color color, boolean outlined) {
        super(text);
        this.color = color;
        this.outlined = outlined;
        setFont(font);
        setForeground(Color.WHITE);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e)  { hover = true; repaint(); }
            @Override public void mouseExited(MouseEvent e)   { hover = false; repaint(); }
            @Override public void mousePressed(MouseEvent e)  { pressed = true; repaint(); }
            @Override public void mouseReleased(MouseEvent e) { pressed = false; repaint(); }
        });
    }

    @Override
    protected void paintComponent(Graphics g0) {
        Graphics2D g = (Graphics2D) g0.create();
        Theme.smooth(g);
        if (outlined) {
            g.setColor(pressed ? new Color(250, 225, 225)
                    : hover ? new Color(253, 240, 240) : Color.WHITE);
            g.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 14, 14));
            g.setColor(color);
            g.setStroke(new BasicStroke(1.2f));
            g.draw(new RoundRectangle2D.Double(0.6, 0.6, getWidth() - 1.2,
                    getHeight() - 1.2, 14, 14));
        } else {
            Color fill = pressed ? color.darker() : hover ? new Color(228, 52, 56) : color;
            g.setColor(fill);
            g.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 14, 14));
            g.setColor(Color.WHITE);
        }
        g.setFont(getFont());
        FontMetrics fm = g.getFontMetrics();
        g.drawString(getText(), (getWidth() - fm.stringWidth(getText())) / 2,
                (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
        g.dispose();
    }
}

class RegistrationPasswordField extends JPasswordField {
    private final String hint;

    RegistrationPasswordField(String hint) {
        this.hint = hint;
        setOpaque(false);
        setBorder(null);
        setFont(RegistrationTheme.font(Font.PLAIN, 11f));
        setForeground(RegistrationTheme.NAVY);
        setCaretColor(RegistrationTheme.NAVY);
        setEchoChar('\u2022');
    }

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        if (getPassword().length == 0) {
            Graphics2D g = (Graphics2D) g0.create();
            Theme.smooth(g);
            g.setFont(getFont().deriveFont(10f));
            g.setColor(RegistrationTheme.HINT);
            FontMetrics fm = g.getFontMetrics();
            g.drawString(hint, 0, (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
            g.dispose();
        }
    }
}

class RegistrationEyeButton extends JButton {
    private boolean shown;

    RegistrationEyeButton() {
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
        eye.moveTo(cx - 9, cy);
        eye.quadTo(cx, cy - 9, cx + 9, cy);
        eye.quadTo(cx, cy + 9, cx - 9, cy);
        eye.closePath();
        g.setColor(RegistrationTheme.NAVY);
        g.fill(eye);
        g.setColor(Color.WHITE);
        g.fill(new Ellipse2D.Double(cx - 3.5, cy - 3.5, 7, 7));
        g.setColor(RegistrationTheme.NAVY);
        g.fill(new Ellipse2D.Double(cx - 2, cy - 2, 4, 4));
        if (!shown) {
            g.setStroke(new BasicStroke(4.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.setColor(Color.WHITE);
            g.draw(new Line2D.Double(cx - 8, cy + 8, cx + 8, cy - 8));
            g.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.setColor(RegistrationTheme.NAVY);
            g.draw(new Line2D.Double(cx - 8, cy + 8, cx + 8, cy - 8));
        }
        g.dispose();
    }
}

class RegistrationUploadBox extends JPanel {
    private String caption = "Upload a government-issued ID";
    private final RegistrationButton button = new RegistrationButton(
            "Tap to upload ID photo", RegistrationTheme.font(Font.BOLD, 10f), RegistrationTheme.RED);

    RegistrationUploadBox(Runnable onClick) {
        setLayout(null);
        setOpaque(false);
        button.addActionListener(e -> onClick.run());
        add(button);
    }

    void setUploaded(String fileName) {
        caption = fileName.length() > 34 ? fileName.substring(0, 31) + "..." : fileName;
        button.setText("Change ID photo");
        repaint();
    }

    @Override
    public void doLayout() {
        int w = getWidth() - 4;
        button.setBounds((w - 134) / 2, 62, 134, 22);
    }

    @Override
    protected void paintComponent(Graphics g0) {
        Graphics2D g = (Graphics2D) g0.create();
        Theme.smooth(g);
        int w = getWidth() - 4, h = getHeight() - 4;
        for (int i = 3; i >= 1; i--) {
            g.setColor(new Color(20, 20, 50, 28));
            g.fill(new RoundRectangle2D.Double(i, i, w, h, 8, 8));
        }
        g.setColor(Color.WHITE);
        g.fill(new RoundRectangle2D.Double(0, 0, w, h, 8, 8));
        g.setColor(RegistrationTheme.NAVY);
        g.setStroke(new BasicStroke(1.2f));
        g.draw(new RoundRectangle2D.Double(0.6, 0.6, w - 1.2, h - 1.2, 8, 8));

        double cx = w / 2.0, cy = 24;
        Area cloud = new Area(new Ellipse2D.Double(cx - 13, cy - 2, 14, 14));
        cloud.add(new Area(new Ellipse2D.Double(cx - 6, cy - 10, 16, 16)));
        cloud.add(new Area(new Ellipse2D.Double(cx + 3, cy - 4, 12, 12)));
        cloud.add(new Area(new RoundRectangle2D.Double(cx - 13, cy + 3, 28, 9, 9, 9)));
        g.setColor(RegistrationTheme.NAVY);
        g.fill(cloud);
        g.setColor(Color.WHITE);
        g.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(new Line2D.Double(cx, cy + 7, cx, cy - 3));
        g.draw(new Line2D.Double(cx - 4, cy + 0.5, cx, cy - 3.5));
        g.draw(new Line2D.Double(cx + 4, cy + 0.5, cx, cy - 3.5));
        g.setColor(RegistrationTheme.NAVY);
        g.setFont(RegistrationTheme.font(Font.PLAIN, 10f));
        FontMetrics fm = g.getFontMetrics();
        g.drawString(caption, (int) (cx - fm.stringWidth(caption) / 2.0), 49);
        g.dispose();
    }
}

class RegistrationSuccessCheck extends JComponent {
    @Override
    protected void paintComponent(Graphics g0) {
        Graphics2D g = (Graphics2D) g0.create();
        Theme.smooth(g);
        int w = getWidth(), h = getHeight();
        g.setColor(RegistrationTheme.SUCCESS_GREEN);
        g.fill(new Ellipse2D.Double(0, 0, w, h));
        double cx = w / 2.0, cy = h / 2.0;
        Path2D check = new Path2D.Double();
        check.moveTo(cx - 11, cy + 1);
        check.lineTo(cx - 4, cy + 8);
        check.lineTo(cx + 11, cy - 8);
        g.setColor(Color.WHITE);
        g.setStroke(new BasicStroke(5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(check);
        g.dispose();
    }
}

class RegistrationSuccessLabel extends JLabel {
    private final Color color;

    RegistrationSuccessLabel(String text, Color color) {
        super(text, SwingConstants.CENTER);
        this.color = color;
    }

    @Override
    protected void paintComponent(Graphics g0) {
        Graphics2D g = (Graphics2D) g0.create();
        Theme.smooth(g);
        g.setFont(getFont());
        FontMetrics fm = g.getFontMetrics();
        int x = (getWidth() - fm.stringWidth(getText())) / 2;
        int y = fm.getAscent();
        java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(
                getWidth(), getHeight(), java.awt.image.BufferedImage.TYPE_INT_ARGB);
        Graphics2D ig = img.createGraphics();
        Theme.smooth(ig);
        ig.setFont(getFont());
        ig.setColor(new Color(0, 0, 0, 64));
        ig.drawString(getText(), x, y + 4);
        ig.dispose();
        float[] kernel = new float[121];
        float sum = 0;
        for (int i = 0; i < 121; i++)
            sum += kernel[i] = (float) Math.exp(-(Math.pow(i % 11 - 5, 2)
                    + Math.pow(i / 11 - 5, 2)) / 8);
        for (int i = 0; i < 121; i++) kernel[i] /= sum;
        g.drawImage(new java.awt.image.ConvolveOp(new java.awt.image.Kernel(11, 11, kernel))
                .filter(img, null), 0, 0, null);
        g.setColor(color);
        g.drawString(getText(), x, y);
        g.dispose();
    }
}

class RegistrationPendingBadge extends JComponent {
    private final String text;

    RegistrationPendingBadge(String text) { this.text = text; }

    @Override
    protected void paintComponent(Graphics g0) {
        Graphics2D g = (Graphics2D) g0.create();
        Theme.smooth(g);
        g.setColor(new Color(255, 236, 179));
        g.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 8, 8));
        g.setColor(new Color(230, 140, 0));
        g.setFont(RegistrationTheme.font(Font.BOLD, 8f));
        FontMetrics fm = g.getFontMetrics();
        g.drawString(text, (getWidth() - fm.stringWidth(text)) / 2,
                (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
        g.dispose();
    }
}
