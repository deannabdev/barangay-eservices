import javax.swing.*;
import javax.swing.plaf.basic.BasicComboBoxUI;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.util.Arrays;

public class RegisterScreen1 extends JFrame {

    // ---- Colors ----
    private static final Color RED        = new Color(211, 47, 47);
    private static final Color RED_TOP    = new Color(214, 42, 42);
    private static final Color RED_BOTTOM = new Color(160, 28, 30);
    private static final Color NAVY       = new Color(27, 27, 47);
    private static final Color HINT       = new Color(125, 125, 135);

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

    // ---- Form fields ----
    private final PlaceholderField firstName  = new PlaceholderField("Enter first name");
    private final PlaceholderField suffix     = new PlaceholderField("Enter suffix (optional)");
    private final PlaceholderField middleName = new PlaceholderField("Enter middle name (optional)");
    private final PlaceholderField lastName   = new PlaceholderField("Enter last name");
    private final PlaceholderField birthDate  = new PlaceholderField("MM/DD/YYYY");
    private final JComboBox<String> gender      = combo("Male", "Female", "Prefer not to say");
    private final JComboBox<String> civilStatus = combo("Single", "Married", "Widowed", "Separated", "Divorced");
    private final RegistrationData data;

    public RegisterScreen1() {
        this(new RegistrationData());
    }

    RegisterScreen1(RegistrationData data) {
        super("Resident Registration");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
        this.data = data;
        firstName.setText(data.firstName);
        suffix.setText(data.suffix);
        middleName.setText(data.middleName);
        lastName.setText(data.lastName);
        birthDate.setText(data.birthDate);
        if (!data.gender.isEmpty()) gender.setSelectedItem(data.gender);
        if (!data.civilStatus.isEmpty()) civilStatus.setSelectedItem(data.civilStatus);

        BackgroundPanel root = new BackgroundPanel();
        root.setLayout(null);
        root.setPreferredSize(new Dimension(W, H));
        buildUI(root);

        setContentPane(root);
        pack();
        setLocationRelativeTo(null);
    }

    private void buildUI(JPanel root) {
        int fieldX = (W - 267) / 2;

        // Title + subtitle
        ShadowLabel title = new ShadowLabel("Resident Registration");
        title.setFont(font(Font.BOLD, 22f));
        title.setBounds(fieldX - 8, 88, 267, 44);
        root.add(title);

        JLabel subtitle = new JLabel("Create your account to access barangay e-services");
        subtitle.setFont(font(Font.PLAIN, 12f));
        subtitle.setForeground(NAVY);
        subtitle.setBounds(fieldX, 120, 267, 18);
        root.add(subtitle);

        // Step indicator (1 - 2 - 3)
        StepIndicator steps = new StepIndicator();
        steps.setStep(1);
        steps.setBounds(0, 140, W, 44);
        root.add(steps);

        // Section title
        SectionTitle section = new SectionTitle("Personal Information");
        section.setBounds((W - 270) / 2, 194, 270, 22);
        root.add(section);

        // Row 1: first name + suffix
        addField(root, "First Name", firstName,  fieldX,       238, 124, 26, 10, 6, false);
        addField(root, "Suffix",     suffix,     fieldX + 143, 238, 124, 26, 10, 6, false);
        // Middle / last name
        addField(root, "Middle Name", middleName, fieldX, 292, 267, 26, 10, 6, false);
        addField(root, "Last Name",   lastName,   fieldX, 346, 267, 26, 10, 6, false);
        // Date of birth (calendar icon on the right)
        addField(root, "Date of Birth", birthDate, fieldX, 400, 267, 26, 10, 30, true);
        // Gender + civil status
        addField(root, "Gender",       gender,      fieldX,       452, 124, 24, 3, 3, false);
        addField(root, "Civil Status", civilStatus, fieldX + 143, 452, 124, 24, 3, 3, false);

        // Next button
        RoundedButton next = new RoundedButton("Next");
        next.setBounds(70, 557, 207, 34);
        next.addActionListener(e -> onNext());
        root.add(next);

        // Login link
        JLabel login = new JLabel(
                "<html>Already have an account? <b><font color='#D32F2F'>Login here</font></b></html>",
                SwingConstants.CENTER);
        login.setFont(font(Font.PLAIN, 11f));
        login.setForeground(NAVY);
        login.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        login.setBounds(0, 710, W, 18);
        login.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                new LoginScreen().setVisible(true);
                dispose();
            }
        });
        root.add(login);
    }

    /** Adds a small label above a rounded input box. x, y, w, h describe the visible box. */
    private void addField(JPanel root, String label, JComponent comp,
                          int x, int y, int w, int h, int padL, int padR, boolean calendar) {
        JLabel l = new JLabel(label);
        l.setFont(font(Font.PLAIN, 10f));
        l.setForeground(NAVY);
        l.setBounds(x, y - 17, w, 14);
        root.add(l);

        FieldBox box = new FieldBox(comp, padL, padR, calendar);
        box.setBounds(x, y, w + 4, h + 4);   // +4 leaves room for the shadow
        root.add(box);
    }

    private void onNext() {
        StringBuilder missing = new StringBuilder();
        if (firstName.getText().trim().isEmpty()) missing.append("\n- First name");
        if (lastName.getText().trim().isEmpty())  missing.append("\n- Last name");
        if (!birthDate.getText().trim().matches("(0[1-9]|1[0-2])/(0[1-9]|[12]\\d|3[01])/\\d{4}"))
            missing.append("\n- Date of birth (MM/DD/YYYY)");
        if (gender.getSelectedIndex() < 0)      missing.append("\n- Gender");
        if (civilStatus.getSelectedIndex() < 0) missing.append("\n- Civil status");

        if (missing.length() > 0) {
            JOptionPane.showMessageDialog(this, "Please complete:" + missing,
                    "Missing information", JOptionPane.WARNING_MESSAGE);
            return;
        }
        data.firstName = firstName.getText().trim();
        data.suffix = suffix.getText().trim();
        data.middleName = middleName.getText().trim();
        data.lastName = lastName.getText().trim();
        data.birthDate = birthDate.getText().trim();
        data.gender = String.valueOf(gender.getSelectedItem());
        data.civilStatus = String.valueOf(civilStatus.getSelectedItem());
        new RegisterScreen2(data).setVisible(true);
        dispose();
    }

    // =====================================================================
    //  Custom components
    // =====================================================================

    /** Red gradient header with a white rounded sheet. */
    private static class BackgroundPanel extends JPanel {
        @Override protected void paintComponent(Graphics g0) {
            super.paintComponent(g0);
            Graphics2D g = (Graphics2D) g0.create();
            smooth(g);
            g.setPaint(new GradientPaint(0, 0, RED_TOP, 0, 80, RED_BOTTOM));
            g.fillRect(0, 0, getWidth(), getHeight());
            g.setColor(Color.WHITE);
            g.fill(new RoundRectangle2D.Double(0, 62, getWidth(), getHeight(), 80, 80));
            g.dispose();
        }
    }

    /** Title with Figma drop shadow: X 0, Y 4, blur 4, #000000 25%. */
    private static class ShadowLabel extends JLabel {
        ShadowLabel(String text) { super(text); }

        @Override protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            smooth(g);
            g.setFont(getFont());
            int y = g.getFontMetrics().getAscent();

            BufferedImage img = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics2D ig = img.createGraphics();
            smooth(ig);
            ig.setFont(getFont());
            ig.setColor(new Color(0, 0, 0, 64));
            ig.drawString(getText(), 8, y + 4);
            ig.dispose();

            float[] k = new float[121];
            float sum = 0;
            for (int i = 0; i < 121; i++)
                sum += k[i] = (float) Math.exp(-(Math.pow(i % 11 - 5, 2) + Math.pow(i / 11 - 5, 2)) / 8);
            for (int i = 0; i < 121; i++) k[i] /= sum;
            g.drawImage(new ConvolveOp(new Kernel(11, 11, k)).filter(img, null), 0, 0, null);

            g.setColor(RED);
            g.drawString(getText(), 8, y);
            g.dispose();
        }
    }

    /** Three numbered circles joined by lines. Circles up to the current step are filled. */
    private static class StepIndicator extends JComponent {
        private int step = 1;

        void setStep(int s) { step = s; repaint(); }

        @Override protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            smooth(g);
            int margin = (int) (getWidth() * 0.14);
            int[] cx = {margin, getWidth() / 2, getWidth() - margin};
            int cy = getHeight() / 2, r = 14;

            g.setStroke(new BasicStroke(1.5f));
            g.setColor(RED);
            for (int i = 0; i < 2; i++) g.drawLine(cx[i] + r, cy, cx[i + 1] - r, cy);

            g.setFont(font(Font.BOLD, 12f));
            FontMetrics fm = g.getFontMetrics();
            for (int i = 0; i < 3; i++) {
                boolean active = i + 1 <= step;
                Ellipse2D c = new Ellipse2D.Double(cx[i] - r, cy - r, 2 * r, 2 * r);
                g.setColor(active ? RED : Color.WHITE);
                g.fill(c);
                g.setColor(RED);
                g.draw(c);
                g.setColor(active ? Color.WHITE : RED);
                String n = String.valueOf(i + 1);
                g.drawString(n, cx[i] - fm.stringWidth(n) / 2, cy + (fm.getAscent() - fm.getDescent()) / 2);
            }
            g.dispose();
        }
    }

    /** Person icon + bold section heading. */
    private static class SectionTitle extends JComponent {
        private final String text;
        SectionTitle(String text) { this.text = text; }

        @Override protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            smooth(g);
            g.setColor(NAVY);
            g.fill(new Ellipse2D.Double(5, 1, 9, 9));                         // head
            g.fill(new Arc2D.Double(1, 11, 18, 18, 0, 180, Arc2D.CHORD));      // shoulders
            g.setFont(font(Font.BOLD, 12f));
            FontMetrics fm = g.getFontMetrics();
            g.drawString(text, 30, (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
            g.dispose();
        }
    }

    /** Rounded white box with a navy border, soft shadow and a child component inside. */
    private static class FieldBox extends JPanel {
        private final JComponent child;
        private final int padL, padR;
        private final boolean calendar;

        FieldBox(JComponent child, int padL, int padR, boolean calendar) {
            this.child = child;
            this.padL = padL;
            this.padR = padR;
            this.calendar = calendar;
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
                g.fill(new RoundRectangle2D.Double(i, i, w, h, 8, 8));
            }
            g.setColor(Color.WHITE);
            g.fill(new RoundRectangle2D.Double(0, 0, w, h, 8, 8));
            g.setColor(NAVY);
            g.setStroke(new BasicStroke(1.2f));
            g.draw(new RoundRectangle2D.Double(0.6, 0.6, w - 1.2, h - 1.2, 8, 8));

            if (calendar) {                                // calendar icon (date of birth)
                int x = w - 24, y = (h - 14) / 2 + 1;
                g.setColor(NAVY);
                g.fillRoundRect(x, y, 14, 13, 3, 3);
                g.fillRect(x + 3, y - 2, 2, 3);
                g.fillRect(x + 9, y - 2, 2, 3);
                g.setColor(Color.WHITE);
                g.fillRect(x + 2, y + 5, 10, 6);
            }
            g.dispose();
        }
    }

    /** Borderless text field with placeholder text. */
    private static class PlaceholderField extends JTextField {
        private final String placeholder;

        PlaceholderField(String placeholder) {
            this.placeholder = placeholder;
            setOpaque(false);
            setBorder(null);
            setFont(font(Font.PLAIN, 11f));
            setForeground(NAVY);
            setCaretColor(NAVY);
        }

        @Override protected void paintComponent(Graphics g0) {
            super.paintComponent(g0);
            if (getText().isEmpty()) {
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

    /** Combo box with no selection by default, a painted arrow and no border. */
    private static JComboBox<String> combo(String... items) {
        JComboBox<String> c = new JComboBox<>(items);
        c.setUI(new BasicComboBoxUI() {
            @Override protected JButton createArrowButton() {
                JButton b = new JButton() {
                    @Override protected void paintComponent(Graphics g0) {
                        Graphics2D g = (Graphics2D) g0.create();
                        smooth(g);
                        int cx = getWidth() / 2, cy = getHeight() / 2;
                        g.setColor(NAVY);
                        g.fill(new Polygon(new int[]{cx - 4, cx + 4, cx}, new int[]{cy - 2, cy - 2, cy + 3}, 3));
                        g.dispose();
                    }
                };
                b.setBorder(null);
                b.setContentAreaFilled(false);
                b.setFocusable(false);
                return b;
            }
            @Override public void paintCurrentValueBackground(Graphics g, Rectangle r, boolean focus) { }
            @Override public void paintCurrentValue(Graphics g, Rectangle r, boolean focus) {
                super.paintCurrentValue(g, r, false);   // no blue focus highlight
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
                @Override public void mouseEntered(MouseEvent e)  { hover = true;  repaint(); }
                @Override public void mouseExited(MouseEvent e)   { hover = false; repaint(); }
                @Override public void mousePressed(MouseEvent e)  { pressed = true;  repaint(); }
                @Override public void mouseReleased(MouseEvent e) { pressed = false; repaint(); }
            });
        }

        @Override protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            smooth(g);
            g.setColor(pressed ? RED.darker() : hover ? new Color(228, 52, 56) : RED);
            g.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 14, 14));
            g.setColor(Color.WHITE);
            g.setFont(getFont());
            FontMetrics fm = g.getFontMetrics();
            g.drawString(getText(), (getWidth() - fm.stringWidth(getText())) / 2,
                    (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
            g.dispose();
        }
    }

}