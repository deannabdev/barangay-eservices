import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.plaf.basic.BasicComboBoxUI;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;

public class RegisterScreen3 extends JFrame {

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
    private final JComboBox<String> idType = combo(
            "Philippine National ID (PhilSys)", "Driver's License", "Passport", "UMID",
            "Voter's ID", "Postal ID", "PRC ID", "Senior Citizen ID");
    private final PlaceholderPassword password = new PlaceholderPassword("Enter password");
    private final PlaceholderPassword confirm  = new PlaceholderPassword("Re-enter password");
    private final RegistrationData data;
    private UploadBox uploadBox;
    private File idPhoto;

    public RegisterScreen3() {
        this(new RegistrationData());
    }

    RegisterScreen3(RegistrationData data) {
        super("Resident Registration");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
        this.data = data;
        
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
        steps.setStep(3);
        steps.setBounds(0, 140, W, 44);
        root.add(steps);

        // ID Verification Section
        SectionTitle idSection = new SectionTitle("ID Verification", SectionTitle.Icon.ID_CARD);
        idSection.setBounds(fieldX + 1, 194, 270, 22);
        root.add(idSection);

        addLabel(root, "ID Type", fieldX, 238);
        FieldBox typeBox = new FieldBox(idType, 3, 3, FieldBox.Deco.NONE);
        typeBox.setBounds(fieldX, 238, 271, 30);
        root.add(typeBox);

        uploadBox = new UploadBox(this::chooseIdPhoto);
        uploadBox.setBounds(fieldX, 278, 271, 96);
        root.add(uploadBox);

        // Security Section
        SectionTitle security = new SectionTitle("Security", SectionTitle.Icon.LOCK);
        security.setBounds(fieldX + 1, 400, 270, 22);
        root.add(security);

        addLabel(root, "Password", fieldX, 440);
        FieldBox passBox = passwordBox(password);
        passBox.setBounds(fieldX, 440, 271, 30);
        root.add(passBox);

        JLabel hint = new JLabel("Must contain at least 8 characters");
        hint.setFont(font(Font.PLAIN, 9f));
        hint.setForeground(NAVY);
        hint.setBounds(fieldX + 2, 472, 270, 12);
        root.add(hint);

        addLabel(root, "Confirm Password", fieldX, 514);
        FieldBox confirmBox = passwordBox(confirm);
        confirmBox.setBounds(fieldX, 514, 271, 30);
        root.add(confirmBox);

        //Back button
        RoundedButton back = new RoundedButton("Back", true, 13f);
        back.setBounds(sectionX, 572, 127, 33);
        back.addActionListener(e -> {
            new RegisterScreen2(data).setVisible(true);
            dispose();
        });
        root.add(back);

        // Register button
        RoundedButton register = new RoundedButton("Register", false, 13f);
        register.setBounds(sectionX + 140, 572, 127, 33);
        register.addActionListener(e -> onRegister());
        root.add(register);
    }

    /** Small label placed above an input whose visible box starts at (x, y). */
    private void addLabel (JPanel root, String text, int x, int y) {
        JLabel l = new JLabel(text);
        l.setFont(font(Font.PLAIN, 10f));
        l.setForeground(NAVY);
        l.setBounds(x, y - 17, 267, 14);
        root.add(l);
    }

    /** Password input: lock icon on the left, show/hide eye on the right. */
    private FieldBox passwordBox(PlaceholderPassword pf) {
        FieldBox box = new FieldBox(pf, 10, 36, FieldBox.Deco.NONE);
        EyeButton eye = new EyeButton();
        eye.setBounds(267 - 32, 2, 24, 22);
        eye.addActionListener(e -> {
            eye.setShown(!eye.isShown());
            pf.setEchoChar(eye.isShown() ? (char) 0 : '\u2022');
        });
        box.add(eye);
        return box;
    }

    private void chooseIdPhoto() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Select a government-issued ID photo");
        fc.setFileFilter(new FileNameExtensionFilter("Images (jpg, jpeg, png)", "jpg", "jpeg", "png"));
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            idPhoto = fc.getSelectedFile();
            uploadBox.setUploaded(idPhoto.getName());
        }
    }

    private void onRegister() {
        StringBuilder missing = new StringBuilder();
        if (idType.getSelectedIndex() < 0) missing.append("\n- ID type");
        if (idPhoto == null)               missing.append("\n- ID photo");
 
        String pw = new String(password.getPassword());
        if (pw.length() < 8)
            missing.append("\n- Password (at least 8 characters)");
        else if (!pw.equals(new String(confirm.getPassword())))
            missing.append("\n- Passwords do not match");
 
        if (missing.length() > 0) {
            JOptionPane.showMessageDialog(this, "Please complete:" + missing,
                    "Missing information", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (data.email.isEmpty() || data.mobile.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please complete registration through the previous steps so your contact details are available.",
                    "Registration incomplete", JOptionPane.WARNING_MESSAGE);
            return;
        }

        char[] passwordValue = password.getPassword();
        try {
            if (!AccountsStore.register(data, String.valueOf(idType.getSelectedItem()), idPhoto, passwordValue)) {
                JOptionPane.showMessageDialog(this,
                        "An account already exists for that email address or mobile number.",
                        "Account already exists", JOptionPane.WARNING_MESSAGE);
                return;
            }
            new RegistrationSuccess().setVisible(true);
            dispose();
        } catch (IOException | GeneralSecurityException ex) {
            JOptionPane.showMessageDialog(this,
                    "Could not save your local account:\n" + ex.getMessage(),
                    "Registration error", JOptionPane.ERROR_MESSAGE);
        } finally {
            Arrays.fill(passwordValue, '\0');
        }
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

    /** Icon (ID card or lock) + bold section heading. */
    private static class SectionTitle extends JComponent {
        enum Icon { ID_CARD, LOCK }
        private final String text;
        private final Icon icon;
 
        SectionTitle(String text, Icon icon) { this.text = text; this.icon = icon; }
 
        @Override protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            smooth(g);
            g.setColor(NAVY);
            if (icon == Icon.ID_CARD) {
                g.fill(new RoundRectangle2D.Double(0, 3, 20, 15, 4, 4));          // card
                g.setColor(Color.WHITE);
                g.fill(new Ellipse2D.Double(3.5, 6, 5, 5));                        // head
                g.fill(new Arc2D.Double(2.5, 11.5, 7, 7, 0, 180, Arc2D.CHORD));    // shoulders
                g.fillRect(12, 7, 5, 2);                                           // text lines
                g.fillRect(12, 11, 5, 2);
            } else {
                g.setStroke(new BasicStroke(2.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.draw(new Arc2D.Double(5, 1.5, 10, 14, 0, 180, Arc2D.OPEN));      // shackle
                g.fill(new RoundRectangle2D.Double(2, 9, 16, 12, 4, 4));           // body
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
        enum Deco { NONE, LOCK }
        private final JComponent child;
        private final int padL, padR;
        private final Deco deco;

        FieldBox(JComponent child, int padL, int padR, Deco deco) {
            this.child = child;
            this.padL = padL;
            this.padR = padR;
            this.deco = deco;
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

            if (deco == Deco.LOCK) {                       // small lock on the left
                int x = 11, y = (h - 13) / 2 + 1;
                g.setColor(new Color(70, 70, 85));
                g.setStroke(new BasicStroke(1.6f));
                g.draw(new Arc2D.Double(x + 1.5, y, 7, 9, 0, 180, Arc2D.OPEN));
                g.fill(new RoundRectangle2D.Double(x, y + 5, 10, 8, 2, 2));
            }
            g.dispose();
        }
    }

    /** Dashed-look upload card: cloud icon, caption and a "Tap to upload" button. */
    private static class UploadBox extends JPanel {
        private String caption = "Upload a government-issued ID";
        private final RoundedButton button = new RoundedButton("Tap to upload ID photo", false, 10f);
 
        UploadBox(Runnable onClick) {
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
 
        @Override public void doLayout() {
            int w = getWidth() - 4;
            button.setBounds((w - 134) / 2, 62, 134, 22);
        }
 
        @Override protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            smooth(g);
            int w = getWidth() - 4, h = getHeight() - 4;
 
            for (int i = 3; i >= 1; i--) {
                g.setColor(new Color(20, 20, 50, 28));
                g.fill(new RoundRectangle2D.Double(i, i, w, h, 8, 8));
            }
            g.setColor(Color.WHITE);
            g.fill(new RoundRectangle2D.Double(0, 0, w, h, 8, 8));
            g.setColor(NAVY);
            g.setStroke(new BasicStroke(1.2f));
            g.draw(new RoundRectangle2D.Double(0.6, 0.6, w - 1.2, h - 1.2, 8, 8));
 
            // cloud + up arrow
            double cx = w / 2.0, cy = 24;
            Area cloud = new Area(new Ellipse2D.Double(cx - 13, cy - 2, 14, 14));
            cloud.add(new Area(new Ellipse2D.Double(cx - 6, cy - 10, 16, 16)));
            cloud.add(new Area(new Ellipse2D.Double(cx + 3, cy - 4, 12, 12)));
            cloud.add(new Area(new RoundRectangle2D.Double(cx - 13, cy + 3, 28, 9, 9, 9)));
            g.setColor(NAVY);
            g.fill(cloud);
            g.setColor(Color.WHITE);
            g.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.draw(new Line2D.Double(cx, cy + 7, cx, cy - 3));
            g.draw(new Line2D.Double(cx - 4, cy + 0.5, cx, cy - 3.5));
            g.draw(new Line2D.Double(cx + 4, cy + 0.5, cx, cy - 3.5));
 
            // caption
            g.setColor(NAVY);
            g.setFont(font(Font.PLAIN, 10f));
            FontMetrics fm = g.getFontMetrics();
            g.drawString(caption, (int) (cx - fm.stringWidth(caption) / 2.0), 49);
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

    /** Rounded button: solid red, or white with a red outline (Back). */
    private static class RoundedButton extends JButton {
        private final boolean outlined;
        private boolean hover, pressed;
 
        RoundedButton(String text, boolean outlined, float fontSize) {
            super(text);
            this.outlined = outlined;
            setFont(font(Font.BOLD, fontSize));
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