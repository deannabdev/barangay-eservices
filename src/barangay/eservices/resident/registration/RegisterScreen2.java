package registration;
import data.AccountsStore.RegistrationData;
import components.PlaceholderField;
import javax.swing.*;
import java.awt.*;

public class RegisterScreen2 extends JFrame {

    private static final int W = 351, H = 775;

    // ---- Form fields ----
    private final PlaceholderField houseNumber = RegistrationTheme.field("e.g. Block 5 Lot 12");
    private final PlaceholderField street = RegistrationTheme.field("e.g. Mabini St.");
    private final PlaceholderField barangay = RegistrationTheme.field("");
    private final PlaceholderField city = RegistrationTheme.field("");
    private final PlaceholderField mobileNumber = RegistrationTheme.field("e.g. 09XX XXX XXXX");
    private final PlaceholderField emailAddress = RegistrationTheme.field("e.g. yourmail@example.com");
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
        
        RegistrationBackgroundPanel root = new RegistrationBackgroundPanel();
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
                RegistrationTheme.font(Font.BOLD, 12f), RegistrationTheme.RED, true);
        steps.setStep(2);
        steps.setBounds(0, 140, W, 44);
        root.add(steps);

        // Address Section
        RegistrationSectionTitle address = new RegistrationSectionTitle("Address",
                RegistrationTheme.font(Font.BOLD, 12f), RegistrationTheme.NAVY,
                RegistrationSectionTitle.Icon.HOUSE);
        address.setBounds(sectionX, 190, 270, 22);
        root.add(address);

        addField(root, "House/Lot No.", houseNumber, fieldX, 230, 124, 26);
        addField(root, "Street", street, fieldX + 143, 230, 124, 26);
        addField(root, "Barangay", barangay, fieldX, 284, 267, 26);
        addField(root, "City", city, fieldX, 338, 267, 26);

        // Contact Section
        RegistrationSectionTitle contact = new RegistrationSectionTitle("Contact",
                RegistrationTheme.font(Font.BOLD, 12f), RegistrationTheme.NAVY,
                RegistrationSectionTitle.Icon.CONTACT);
        contact.setBounds(sectionX, 391, 270, 22);
        root.add(contact);

        addField(root, "Mobile Number", mobileNumber, fieldX, 431, 267, 26);
        addField(root, "Email Address", emailAddress, fieldX, 483, 267, 26);

        //Back button
        RegistrationButton back = new RegistrationButton("Back",
                RegistrationTheme.font(Font.BOLD, 13f), RegistrationTheme.RED, true);
        back.setBounds(sectionX, 558, 127, 30);
        back.addActionListener(e -> {
            updateData();
            new RegisterScreen1(data).setVisible(true);
            dispose();
        });
        root.add(back);

        // Next button
        RegistrationButton next = new RegistrationButton("Next",
                RegistrationTheme.font(Font.BOLD, 13f), RegistrationTheme.RED);
        next.setBounds(sectionX + 140, 558, 127, 30);
        next.addActionListener(e -> onNext());
        root.add(next);
    }

    /** Adds a small label above a rounded input box. x, y, w, h describe the visible box. */
    private void addField(JPanel root, String label, JComponent comp,
                          int x, int y, int w, int h) {
        JLabel l = new JLabel(label);
        l.setFont(RegistrationTheme.font(Font.PLAIN, 10f));
        l.setForeground(RegistrationTheme.NAVY);
        l.setBounds(x, y - 17, w, 14);
        root.add(l);

        RegistrationFieldBox box = new RegistrationFieldBox(comp);
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

}