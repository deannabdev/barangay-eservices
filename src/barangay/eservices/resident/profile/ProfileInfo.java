import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.basic.BasicComboBoxUI;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.Arrays;

public class ProfileInfo extends JFrame {

    // ---- Colors ----
    private static final Color RED    = new Color(211, 47, 47);
    private static final Color MAROON = new Color(150, 28, 31);
    private static final Color NAVY   = new Color(27, 27, 47);
    private static final Color HINT   = new Color(125, 125, 135);
    private static final Color LOCKED = new Color(110, 110, 120);

    private static final int W = 351, H = 775;
    private static final int X0 = 28, FW = 288, HW = 134, RX = X0 + FW - HW;   // form edges / half width / right column

    /**
     * Set to true if your barangay verifies name and date of birth against the ID.
     * Those fields then become read-only (gray) and a short note appears under the header.
     */
    private static final boolean LOCK_ID_VERIFIED_FIELDS = false;

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
    private final PlaceholderField houseNo  = new PlaceholderField("e.g. Block 5 Lot 12 or #42");
    private final PlaceholderField street   = new PlaceholderField("e.g. Mabini St.");
    private final PlaceholderField barangay = new PlaceholderField("");
    private final PlaceholderField city     = new PlaceholderField("");
    private final PlaceholderField mobile   = new PlaceholderField("e.g. 09XX XXX XXXX");
    private final PlaceholderField email    = new PlaceholderField("e.g. yourmail@example.com");

    private RoundedButton save;
    private String[] saved;   // the values as last saved, used to detect edits

    public ProfileInfo() {
        super("My Account");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);

        barangay.setText("Barangay Central Signal");
        city.setText("Taguig City");

        BackgroundPanel root = new BackgroundPanel();
        root.setLayout(null);
        root.setPreferredSize(new Dimension(W, H));
        buildUI(root);

        // TODO: replace this sample data with the logged-in user's saved details (database / API).
        setProfile("Juan", "", "Santos", "Dela Cruz", "01/15/2000", "Male", "Single",
                "Block 5 Lot 12", "Mabini St.", "09123456789", "juandelacruz@gmail.com");
        watchForEdits();

        setContentPane(root);
        pack();
        setLocationRelativeTo(null);
    }

    /**
     * Fills the form with the user's saved details. These become the "saved" values, so
     * Save Changes stays disabled until something is edited.
     */
    public void setProfile(String first, String suffixText, String middle, String last, String dob,
                           String genderValue, String civilValue, String house, String streetName,
                           String mobileNo, String emailAddress) {
        firstName.setText(first);
        suffix.setText(suffixText);
        middleName.setText(middle);
        lastName.setText(last);
        birthDate.setText(dob);
        gender.setSelectedItem(genderValue);
        civilStatus.setSelectedItem(civilValue);
        houseNo.setText(house);
        street.setText(streetName);
        mobile.setText(mobileNo);
        email.setText(emailAddress);
        saved = values();
        refreshState();
    }

    private void buildUI(JPanel root) {
        // Header: back arrow + title
        BackButton back = new BackButton(this::cancel);
        back.setBounds(18, 46, 32, 32);
        root.add(back);

        JLabel title = new JLabel("MY ACCOUNT", SwingConstants.CENTER);
        title.setFont(font(Font.BOLD, 20f));
        title.setForeground(Color.BLACK);
        title.setBounds(0, 44, W, 36);
        root.add(title);

        // Name
        FieldBox firstBox  = addField(root, "First Name", firstName, X0, 127, HW, 10, 6, false);
        FieldBox suffixBox = addField(root, "Suffix",     suffix,    RX, 127, HW, 10, 6, false);
        FieldBox middleBox = addField(root, "Middle Name", middleName, X0, 186, FW, 10, 6, false);
        FieldBox lastBox   = addField(root, "Last Name",   lastName,   X0, 244, FW, 10, 6, false);
        FieldBox dobBox    = addField(root, "Date of Birth", birthDate, X0, 303, FW, 10, 30, true);

        // Gender + civil status
        addField(root, "Gender",       gender,      X0, 361, HW, 3, 3, false);
        addField(root, "Civil Status", civilStatus, RX, 361, HW, 3, 3, false);

        // Address
        addField(root, "House/Unit No.", houseNo, X0, 411, HW, 10, 6, false);
        addField(root, "Street",         street,  RX, 411, HW, 10, 6, false);
        FieldBox barangayBox = addField(root, "Barangay", barangay, X0, 469, FW, 10, 28, false);
        FieldBox cityBox     = addField(root, "City",     city,     X0, 528, FW, 10, 28, false);

        // Contact
        addField(root, "Mobile Number", mobile, X0, 577, FW, 10, 6, false);
        addField(root, "Email Address", email,  X0, 632, FW, 10, 6, false);

        // Fixed fields look read-only (gray background + small lock)
        lock(barangay, barangayBox, "Fixed for this barangay");
        lock(city, cityBox, "Fixed for this barangay");
        if (LOCK_ID_VERIFIED_FIELDS) {
            String tip = "Verified from your ID";
            lock(firstName, firstBox, tip);
            lock(suffix, suffixBox, tip);
            lock(middleName, middleBox, tip);
            lock(lastName, lastBox, tip);
            lock(birthDate, dobBox, tip);

            JLabel note = new JLabel("Name and birthdate are verified and can't be edited here.");
            note.setFont(font(Font.PLAIN, 9f));
            note.setForeground(HINT);
            note.setBounds(X0, 94, FW, 13);
            root.add(note);
        }

        // Buttons: Cancel (outlined) + Save Changes (disabled until something is edited)
        RoundedButton cancel = new RoundedButton("Cancel", true);
        cancel.setBounds(X0 + FW - 88 - 8 - 72, 669, 72, 22);
        cancel.addActionListener(e -> cancel());
        root.add(cancel);

        save = new RoundedButton("Save Changes", false);
        save.setBounds(X0 + FW - 88, 669, 88, 22);
        save.addActionListener(e -> onSave());
        root.add(save);
    }

    /** Adds a small label above a rounded input box. x, y, w describe the visible box (height 26). */
    private FieldBox addField(JPanel root, String label, JComponent comp,
                              int x, int y, int w, int padL, int padR, boolean calendar) {
        JLabel l = new JLabel(label);
        l.setFont(font(Font.PLAIN, 10f));
        l.setForeground(NAVY);
        l.setBounds(x, y - 17, w, 14);
        root.add(l);

        FieldBox box = new FieldBox(comp, padL, padR, calendar);
        box.setBounds(x, y, w + 4, 30);   // 26 visible + 4 for the shadow
        root.add(box);
        return box;
    }

    /** Makes a text field read-only and gives its box the gray "locked" look. */
    private void lock(JTextField field, FieldBox box, String tooltip) {
        field.setEditable(false);
        field.setToolTipText(tooltip);
        box.setLocked(true);
    }

    // ---- Edit tracking ----

    private String[] values() {
        return new String[]{
                firstName.getText().trim(), suffix.getText().trim(), middleName.getText().trim(),
                lastName.getText().trim(), birthDate.getText().trim(), selected(gender), selected(civilStatus),
                houseNo.getText().trim(), street.getText().trim(), mobile.getText().trim(), email.getText().trim()
        };
    }

    private static String selected(JComboBox<String> c) {
        Object o = c.getSelectedItem();
        return o == null ? "" : o.toString();
    }

    private boolean isDirty() {
        return saved != null && !Arrays.equals(saved, values());
    }

    private void refreshState() {
        if (save != null) save.setEnabled(isDirty());
    }

    private void watchForEdits() {
        DocumentListener dl = new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e)  { refreshState(); }
            @Override public void removeUpdate(DocumentEvent e)  { refreshState(); }
            @Override public void changedUpdate(DocumentEvent e) { refreshState(); }
        };
        for (JTextField f : new JTextField[]{firstName, suffix, middleName, lastName, birthDate, houseNo, street, mobile, email})
            f.getDocument().addDocumentListener(dl);
        gender.addActionListener(e -> refreshState());
        civilStatus.addActionListener(e -> refreshState());
    }

    // ---- Actions ----

    /** Back arrow and Cancel: leave the screen, asking first if there are unsaved edits. */
    private void cancel() {
        if (isDirty()) {
            int answer = JOptionPane.showConfirmDialog(this, "Discard your changes?",
                    "Unsaved changes", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (answer != JOptionPane.YES_OPTION) return;
        }
        new ProfileScreen().setVisible(true);
        dispose();
    }

    private void onSave() {
        StringBuilder missing = new StringBuilder();
        if (firstName.getText().trim().isEmpty()) missing.append("\n- First name");
        if (lastName.getText().trim().isEmpty())  missing.append("\n- Last name");
        if (!birthDate.getText().trim().matches("(0[1-9]|1[0-2])/(0[1-9]|[12]\\d|3[01])/\\d{4}"))
            missing.append("\n- Date of birth (MM/DD/YYYY)");
        if (gender.getSelectedIndex() < 0)      missing.append("\n- Gender");
        if (civilStatus.getSelectedIndex() < 0) missing.append("\n- Civil status");
        if (houseNo.getText().trim().isEmpty()) missing.append("\n- House/Unit No.");
        if (street.getText().trim().isEmpty())  missing.append("\n- Street");
        if (!mobile.getText().replaceAll("\\s", "").matches("(09|\\+639)\\d{9}"))
            missing.append("\n- Mobile number (e.g. 09XX XXX XXXX)");
        if (!email.getText().trim().matches("[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+"))
            missing.append("\n- Valid email address");

        if (missing.length() > 0) {
            JOptionPane.showMessageDialog(this, "Please check:" + missing,
                    "Missing information", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // TODO: save the changes (database / API) here.
        JOptionPane.showMessageDialog(this, "Your changes have been saved.");
        saved = values();

        // Back to the profile screen showing the updated details
        String fullName = (firstName.getText().trim() + " " + lastName.getText().trim()
                + " " + suffix.getText().trim()).trim();
        ProfileScreen profile = new ProfileScreen();
        profile.setUser(fullName, email.getText().trim(), mobile.getText().trim());
        profile.setVisible(true);
        dispose();
    }

    // =====================================================================
    //  Custom components
    // =====================================================================

    /** White background with a thin separator under the header. */
    private static class BackgroundPanel extends JPanel {
        @Override protected void paintComponent(Graphics g0) {
            super.paintComponent(g0);
            Graphics2D g = (Graphics2D) g0.create();
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, getWidth(), getHeight());
            for (int i = 0; i < 4; i++) {                       // soft shadow under the header
                g.setColor(new Color(0, 0, 0, 10 - i * 2));
                g.fillRect(0, 91 + i, getWidth(), 1);
            }
            g.setColor(new Color(228, 228, 232));
            g.drawLine(0, 90, getWidth(), 90);
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

    /** Rounded box with a navy border, soft shadow and a child component. Can be shown as "locked" (gray). */
    private static class FieldBox extends JPanel {
        private final JComponent child;
        private final int padL, padR;
        private final boolean calendar;
        private boolean locked;

        FieldBox(JComponent child, int padL, int padR, boolean calendar) {
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
            child.setForeground(locked ? LOCKED : NAVY);
            repaint();
        }

        @Override public void doLayout() {
            child.setBounds(padL, 1, getWidth() - 4 - padL - padR, getHeight() - 6);
        }

        @Override protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            smooth(g);
            int w = getWidth() - 4, h = getHeight() - 4;

            for (int i = 3; i >= 1; i--) {                 // shadow to the right / bottom
                g.setColor(new Color(20, 20, 50, locked ? 14 : 28));
                g.fill(new RoundRectangle2D.Double(i, i, w, h, 8, 8));
            }
            g.setColor(locked ? new Color(241, 241, 244) : Color.WHITE);
            g.fill(new RoundRectangle2D.Double(0, 0, w, h, 8, 8));
            g.setColor(locked ? new Color(170, 170, 180) : NAVY);
            g.setStroke(new BasicStroke(1.2f));
            g.draw(new RoundRectangle2D.Double(0.6, 0.6, w - 1.2, h - 1.2, 8, 8));

            Color icon = locked ? new Color(150, 150, 160) : NAVY;
            if (calendar) {                                // calendar icon (date of birth)
                int x = w - 24, y = (h - 14) / 2 + 1;
                g.setColor(icon);
                g.fillRoundRect(x, y, 14, 13, 3, 3);
                g.fillRect(x + 3, y - 2, 2, 3);
                g.fillRect(x + 9, y - 2, 2, 3);
                g.setColor(locked ? new Color(241, 241, 244) : Color.WHITE);
                g.fillRect(x + 2, y + 5, 10, 6);
            } else if (locked) {                           // small lock for read-only fields
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

    /** Combo box with a painted arrow and no border. */
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

    /** Small rounded button: solid red (Save Changes, gray when disabled) or red outline (Cancel). */
    private static class RoundedButton extends JButton {
        private final boolean outlined;
        private boolean hover, pressed;

        RoundedButton(String text, boolean outlined) {
            super(text);
            this.outlined = outlined;
            setFont(font(Font.BOLD, 9f));
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

        @Override public void setEnabled(boolean enabled) {
            super.setEnabled(enabled);
            setCursor(Cursor.getPredefinedCursor(enabled ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
            repaint();
        }

        @Override protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            smooth(g);
            int w = getWidth(), h = getHeight();

            if (outlined) {
                g.setColor(pressed ? new Color(250, 225, 225) : hover ? new Color(253, 240, 240) : Color.WHITE);
                g.fill(new RoundRectangle2D.Double(0, 0, w, h, 8, 8));
                g.setColor(RED);
                g.setStroke(new BasicStroke(1.2f));
                g.draw(new RoundRectangle2D.Double(0.6, 0.6, w - 1.2, h - 1.2, 8, 8));
                g.setColor(RED);
            } else {
                Color fill = !isEnabled() ? new Color(205, 205, 211)
                        : pressed ? RED.darker() : hover ? new Color(228, 52, 56) : RED;
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

}