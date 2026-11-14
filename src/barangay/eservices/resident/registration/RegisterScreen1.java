package barangay.eservices;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class RegisterScreen1 extends JFrame {

    private static final int W = 351, H = 775;

    // ---- Form fields ----
    private final PlaceholderField firstName  = registrationField("Enter first name");
    private final PlaceholderField suffix     = registrationField("Enter suffix (optional)");
    private final PlaceholderField middleName = registrationField("Enter middle name (optional)");
    private final PlaceholderField lastName   = registrationField("Enter last name");
    private final PlaceholderField birthDate  = registrationField("MM/DD/YYYY");
    private final JComboBox<String> gender = new RegistrationComboBox(RegistrationTheme.font(Font.PLAIN, 11f),
            RegistrationTheme.NAVY,
            "Male", "Female", "Prefer not to say");
    private final JComboBox<String> civilStatus = new RegistrationComboBox(
            RegistrationTheme.font(Font.PLAIN, 11f), RegistrationTheme.NAVY,
            "Single", "Married", "Widowed", "Separated", "Divorced");
    private final RegistrationData data;

    private static PlaceholderField registrationField(String hint) {
        return new PlaceholderField(hint, RegistrationTheme.font(Font.PLAIN, 11f),
                RegistrationTheme.NAVY, RegistrationTheme.HINT, RegistrationTheme.NAVY,
                RegistrationTheme.font(Font.PLAIN, 10f));
    }

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

        RegistrationBackgroundPanel root = new RegistrationBackgroundPanel();
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
        JLabel title = new JLabel("Resident Registration");
        title.setFont(RegistrationTheme.font(Font.BOLD, 22f));
        title.setForeground(RegistrationTheme.RED);
        title.setBounds(fieldX, 88, 267, 32);
        root.add(title);

        JLabel subtitle = new JLabel("Create your account to access barangay e-services");
        subtitle.setFont(RegistrationTheme.font(Font.PLAIN, 12f));
        subtitle.setForeground(RegistrationTheme.NAVY);
        subtitle.setBounds(fieldX, 120, 267, 18);
        root.add(subtitle);

        // Step indicator (1 - 2 - 3)
        RegistrationStepIndicator steps = new RegistrationStepIndicator(
                RegistrationTheme.font(Font.BOLD, 12f), RegistrationTheme.RED);
        steps.setStep(1);
        steps.setBounds(0, 140, W, 44);
        root.add(steps);

        // Section title
        RegistrationSectionTitle section = new RegistrationSectionTitle("Personal Information",
                RegistrationTheme.font(Font.BOLD, 12f), RegistrationTheme.NAVY);
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
        RegistrationButton next = new RegistrationButton(
                "Next", RegistrationTheme.font(Font.BOLD, 13f), RegistrationTheme.RED);
        next.setBounds(70, 557, 207, 34);
        next.addActionListener(e -> onNext());
        root.add(next);

        // Login link
        JLabel login = new JLabel(
                "<html>Already have an account? <b><font color='#D32F2F'>Login here</font></b></html>",
                SwingConstants.CENTER);
        login.setFont(RegistrationTheme.font(Font.PLAIN, 11f));
        login.setForeground(RegistrationTheme.NAVY);
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
        l.setFont(RegistrationTheme.font(Font.PLAIN, 10f));
        l.setForeground(RegistrationTheme.NAVY);
        l.setBounds(x, y - 17, w, 14);
        root.add(l);

        RegistrationFieldBox box = new RegistrationFieldBox(
                comp, padL, padR, calendar, RegistrationTheme.NAVY);
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

}