import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.util.Arrays;

public class RegisterScreen2 extends JFrame {

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
    private final PlaceholderField houseNumber  = new PlaceholderField("e.g. Block 5 Lot 12");
    private final PlaceholderField street     = new PlaceholderField("e.g. Mabini St.");
    private final PlaceholderField barangay = new PlaceholderField("");
    private final PlaceholderField city   = new PlaceholderField("");
    private final PlaceholderField mobileNumber  = new PlaceholderField("e.g. 09XX XXX XXXX");
    private final PlaceholderField emailAddress  = new PlaceholderField("e.g. yourmail@example.com");
    private final RegistrationData data;

    public RegisterScreen2() {
        this(new RegistrationData());
    }

    RegisterScreen2(RegistrationData data) {
        super("Resident Registration");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
        this.data = data;

        houseNumber.setText(data.houseNumber);
        street.setText(data.street);
        barangay.setText(data.barangay);
        city.setText(data.city);
        mobileNumber.setText(data.mobile);
        emailAddress.setText(data.email);

        // Barangay and city are pre-filled in the design. Remove setEditable(false) to let users type.
        barangay.setEditable(false);
        city.setEditable(false);
        
        BackgroundPanel root = new BackgroundPanel();
        root.setLayout(null);
        root.setPreferredSize(new Dimension(W, H));
        buildUI(root);

        setContentPane(root);
        pack();
        setLocationRelativeTo(null);
    }

    private void buildUI(JPanel root) {
        int fieldX = (W - 271) / 2;
        int sectionX = (W - 270) / 2;

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
        steps.setStep(2);
        steps.setBounds(0, 140, W, 44);
        root.add(steps);

        // Address Section
        SectionTitle address = new SectionTitle("Address", SectionTitle.Icon.HOUSE);
        address.setBounds(sectionX, 190, 270, 22);
        root.add(address);

        addField(root, "House/Lot No.", houseNumber, fieldX, 230, 124, 26);
        addField(root, "Street", street, fieldX + 143, 230, 124, 26);
        addField(root, "Barangay", barangay, fieldX, 284, 267, 26);
        addField(root, "City", city, fieldX, 338, 267, 26);

        // Contact Section
        SectionTitle contact = new SectionTitle("Contact", SectionTitle.Icon.CONTACT);
        contact.setBounds(sectionX, 391, 270, 22);
        root.add(contact);

        addField(root, "Mobile Number", mobileNumber, fieldX, 431, 267, 26);
        addField(root, "Email Address", emailAddress, fieldX, 483, 267, 26);

        //Back button
        RoundedButton back = new RoundedButton("Back", true);
        back.setBounds(sectionX, 558, 127, 30);
        back.addActionListener(e -> {
            updateData();
            new RegisterScreen1(data).setVisible(true);
            dispose();
        });
        root.add(back);

        // Next button
        RoundedButton next = new RoundedButton("Next", false);
        next.setBounds(sectionX + 140, 558, 127, 30);
        next.addActionListener(e -> onNext());
        root.add(next);
    }

    /** Adds a small label above a rounded input box. x, y, w, h describe the visible box. */
    private void addField(JPanel root, String label, JComponent comp,
                          int x, int y, int w, int h) {
        JLabel l = new JLabel(label);
        l.setFont(font(Font.PLAIN, 10f));
        l.setForeground(NAVY);
        l.setBounds(x, y - 17, w, 14);
        root.add(l);

        FieldBox box = new FieldBox(comp);
        box.setBounds(x, y, w + 4, h + 4);   
        root.add(box);
    }

    private void onNext() {
        StringBuilder missing = new StringBuilder();
        if (houseNumber.getText().trim().isEmpty()) missing.append("\n- House/Lot No.");
        if (street.getText().trim().isEmpty())  missing.append("\n- Street");
        if (barangay.getText().trim().isEmpty()) missing.append("\n- Barangay");
        if (city.getText().trim().isEmpty())  missing.append("\n- City");
        if (!mobileNumber.getText().replaceAll("\\s", "").matches("(09|\\+639)\\d{9}"))
            missing.append("\n- Mobile number (e.g. 09XX XXX XXXX)");
        if (!emailAddress.getText().trim().matches("[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+"))
            missing.append("\n- Valid email address");

        if (missing.length() > 0) {
            JOptionPane.showMessageDialog(this, "Please complete:" + missing,
                    "Missing information", JOptionPane.WARNING_MESSAGE);
            return;
        }
        updateData();
        new RegisterScreen3(data).setVisible(true);
        dispose();
    }

    private void updateData() {
        data.houseNumber = houseNumber.getText().trim();
        data.street = street.getText().trim();
        data.barangay = barangay.getText().trim();
        data.city = city.getText().trim();
        data.mobile = mobileNumber.getText().trim();
        data.email = emailAddress.getText().trim();
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
        private static final int[] CX = {W / 2 - 127, W / 2, W / 2 + 127};
        private int step = 1;

        void setStep(int s) { step = s; repaint(); }

        @Override protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            smooth(g);
            int cy = getHeight() / 2, r = 14;

            for (int i = 0; i < 2; i++) {
                boolean done = step >= i + 2;
                g.setStroke(new BasicStroke(done ? 2.5f : 1.2f));
                g.setColor(done ? RED : new Color(RED.getRed(), RED.getGreen(), RED.getBlue(), 140));
                g.drawLine(CX[i] + r, cy, CX[i + 1] - r, cy);
            }

            g.setFont(font(Font.BOLD, 12f));
            FontMetrics fm = g.getFontMetrics();
            for (int i = 0; i < 3; i++) {
                boolean active = i + 1 <= step;
                Ellipse2D c = new Ellipse2D.Double(CX[i] - r, cy - r, 2 * r, 2 * r);
                g.setColor(active ? RED : Color.WHITE);
                g.fill(c);
                g.setColor(RED);
                g.setStroke(new BasicStroke(1.5f));
                g.draw(c);
                g.setColor(active ? Color.WHITE : RED);
                String n = String.valueOf(i + 1);
                g.drawString(n, CX[i] - fm.stringWidth(n) / 2, cy + (fm.getAscent() - fm.getDescent()) / 2);
            }
            g.dispose();
        }
    }

    /** Icon (house or contact book) + bold section heading. */
    private static class SectionTitle extends JComponent {
        enum Icon { HOUSE, CONTACT }
        private final String text;
        private final Icon icon;
 
        SectionTitle(String text, Icon icon) { this.text = text; this.icon = icon; }
 
        @Override protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            smooth(g);
            g.setColor(NAVY);
            if (icon == Icon.HOUSE) {
                g.fillPolygon(new int[]{0, 10, 20}, new int[]{10, 1, 10}, 3);   // roof
                g.fillRect(3, 9, 14, 10);                                        // walls
                g.fillRect(14, 2, 3, 5);                                         // chimney
                g.setColor(Color.WHITE);
                g.fillRect(8, 13, 4, 6);                                         // door
            } else {
                g.fill(new RoundRectangle2D.Double(1, 1, 16, 20, 4, 4));         // book
                g.fillRect(17, 5, 3, 3);                                         // side tabs
                g.fillRect(17, 10, 3, 3);
                g.fillRect(17, 15, 3, 3);
                g.setColor(Color.WHITE);
                g.fill(new Ellipse2D.Double(5.5, 5, 6, 6));                      // head
                g.fill(new Arc2D.Double(3.5, 12, 10, 10, 0, 180, Arc2D.CHORD)); // shoulders
            }
            g.setColor(NAVY);
            g.setFont(font(Font.BOLD, 12f));
            FontMetrics fm = g.getFontMetrics();
            g.drawString(text, 30, (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
            g.dispose();
        }
    }

    /** Rounded white box with a navy border, soft shadow and a child component inside. */
    private static class FieldBox extends JPanel {
        private final JComponent child;

        FieldBox(JComponent child) {
            this.child = child;
            setLayout(null);
            setOpaque(false);
            add(child);
        }

        @Override public void doLayout() {
            child.setBounds(10, 1, getWidth() - 4 - 16, getHeight() - 6);
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

    /** Rounded button: solid red, or white with a red outline (Back). */
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
            int w = getWidth(), h = getHeight();
 
            if (outlined) {
                g.setColor(pressed ? new Color(250, 225, 225) : hover ? new Color(253, 240, 240) : Color.WHITE);
                g.fill(new RoundRectangle2D.Double(0, 0, w, h, 14, 14));
                g.setColor(RED);
                g.setStroke(new BasicStroke(1.2f));
                g.draw(new RoundRectangle2D.Double(0.6, 0.6, w - 1.2, h - 1.2, 14, 14));
                g.setColor(RED);
            } else {
                g.setColor(pressed ? RED.darker() : hover ? new Color(228, 52, 56) : RED);
                g.fill(new RoundRectangle2D.Double(0, 0, w, h, 14, 14));
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