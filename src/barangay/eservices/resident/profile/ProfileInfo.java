package profile;
import data.AccountsStore;
import data.AccountsStore.AccountProfile;
import data.AccountsStore.RegistrationData;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.io.IOException;
import java.util.Arrays;

public class ProfileInfo extends JFrame {

    private static final int W = 351, H = 775;
    private static final int X0 = 28, FW = 288, HW = 134, RX = X0 + FW - HW;   // form edges / half width / right column

    /**
     * Set to true if your barangay verifies name and date of birth against the ID.
     * Those fields then become read-only (gray) and a short note appears under the header.
     */
    private static final boolean LOCK_ID_VERIFIED_FIELDS = false;

    // ---- Form fields ----
    private final ProfilePlaceholderField firstName  = new ProfilePlaceholderField("Enter first name");
    private final ProfilePlaceholderField suffix     = new ProfilePlaceholderField("Enter suffix (optional)");
    private final ProfilePlaceholderField middleName = new ProfilePlaceholderField("Enter middle name (optional)");
    private final ProfilePlaceholderField lastName   = new ProfilePlaceholderField("Enter last name");
    private final ProfilePlaceholderField birthDate  = new ProfilePlaceholderField("MM/DD/YYYY");
    private final JComboBox<String> gender      = ProfileTheme.combo("Male", "Female", "Prefer not to say");
    private final JComboBox<String> civilStatus = ProfileTheme.combo("Single", "Married", "Widowed", "Separated", "Divorced");
    private final ProfilePlaceholderField houseNo  = new ProfilePlaceholderField("e.g. Block 5 Lot 12 or #42");
    private final ProfilePlaceholderField street   = new ProfilePlaceholderField("e.g. Mabini St.");
    private final ProfilePlaceholderField barangay = new ProfilePlaceholderField("");
    private final ProfilePlaceholderField city     = new ProfilePlaceholderField("");
    private final ProfilePlaceholderField mobile   = new ProfilePlaceholderField("e.g. 09XX XXX XXXX");
    private final ProfilePlaceholderField email    = new ProfilePlaceholderField("e.g. yourmail@example.com");

    private ProfileActionButton save;
    private String[] saved;   // the values as last saved, used to detect edits
    private boolean profileLoaded;

    public ProfileInfo() {
        super("My Account");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);

        barangay.setText("Barangay Central Signal");
        city.setText("Taguig City");

        ProfileInfoBackgroundPanel root = new ProfileInfoBackgroundPanel();
        root.setLayout(null);
        root.setPreferredSize(new Dimension(W, H));
        buildUI(root);

        IOException profileError = null;
        try {
            AccountProfile profile = AccountsStore.getCurrentProfile();
            if (profile == null) {
                throw new IOException("No signed-in account is available.");
            }
            setProfile(profile.firstName, profile.suffix, profile.middleName, profile.lastName,
                    profile.birthDate, profile.gender, profile.civilStatus, profile.houseNumber,
                    profile.street, profile.mobile, profile.email);
            barangay.setText(profile.barangay);
            city.setText(profile.city);
            profileLoaded = true;
            refreshState();
        } catch (IOException ex) {
            profileError = ex;
        }
        watchForEdits();

        setContentPane(root);
        pack();
        setLocationRelativeTo(null);
        if (profileError != null) {
            save.setEnabled(false);
            String message = "Could not load the signed-in account profile:\n" + profileError.getMessage();
            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, message,
                    "Profile error", JOptionPane.ERROR_MESSAGE));
        }
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
        ProfileBackButton back = new ProfileBackButton(this::cancel);
        back.setBounds(18, 46, 32, 32);
        root.add(back);

        JLabel title = new JLabel("MY ACCOUNT", SwingConstants.CENTER);
        title.setFont(ProfileTheme.font(Font.BOLD, 20f));
        title.setForeground(Color.BLACK);
        title.setBounds(0, 44, W, 36);
        root.add(title);

        // Name
        ProfileFieldBox firstBox  = addField(root, "First Name", firstName, X0, 127, HW, 10, 6, false);
        ProfileFieldBox suffixBox = addField(root, "Suffix",     suffix,    RX, 127, HW, 10, 6, false);
        ProfileFieldBox middleBox = addField(root, "Middle Name", middleName, X0, 186, FW, 10, 6, false);
        ProfileFieldBox lastBox   = addField(root, "Last Name",   lastName,   X0, 244, FW, 10, 6, false);
        ProfileFieldBox dobBox    = addField(root, "Date of Birth", birthDate, X0, 303, FW, 10, 30, true);

        // Gender + civil status
        addField(root, "Gender",       gender,      X0, 361, HW, 3, 3, false);
        addField(root, "Civil Status", civilStatus, RX, 361, HW, 3, 3, false);

        // Address
        addField(root, "House/Unit No.", houseNo, X0, 411, HW, 10, 6, false);
        addField(root, "Street",         street,  RX, 411, HW, 10, 6, false);
        ProfileFieldBox barangayBox = addField(root, "Barangay", barangay, X0, 469, FW, 10, 28, false);
        ProfileFieldBox cityBox     = addField(root, "City",     city,     X0, 528, FW, 10, 28, false);

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
            note.setFont(ProfileTheme.font(Font.PLAIN, 9f));
            note.setForeground(ProfileTheme.HINT);
            note.setBounds(X0, 94, FW, 13);
            root.add(note);
        }

        // Buttons: Cancel (outlined) + Save Changes (disabled until something is edited)
        ProfileActionButton cancel = new ProfileActionButton("Cancel", true);
        cancel.setBounds(X0 + FW - 88 - 8 - 72, 669, 72, 22);
        cancel.addActionListener(e -> cancel());
        root.add(cancel);

        save = new ProfileActionButton("Save Changes", false);
        save.setBounds(X0 + FW - 88, 669, 88, 22);
        save.addActionListener(e -> onSave());
        root.add(save);
    }

    /** Adds a small label above a rounded input box. x, y, w describe the visible box (height 26). */
    private ProfileFieldBox addField(JPanel root, String label, JComponent comp,
                              int x, int y, int w, int padL, int padR, boolean calendar) {
        JLabel l = new JLabel(label);
        l.setFont(ProfileTheme.font(Font.PLAIN, 10f));
        l.setForeground(ProfileTheme.NAVY);
        l.setBounds(x, y - 17, w, 14);
        root.add(l);

        ProfileFieldBox box = new ProfileFieldBox(comp, padL, padR, calendar);
        box.setBounds(x, y, w + 4, 30);   // 26 visible + 4 for the shadow
        root.add(box);
        return box;
    }

    /** Makes a text field read-only and gives its box the gray "locked" look. */
    private void lock(JTextField field, ProfileFieldBox box, String tooltip) {
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
        if (save != null) save.setEnabled(profileLoaded && isDirty());
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
        dispose();
        ProfileScreen.showWindow();
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

        RegistrationData profile = new RegistrationData();
        profile.firstName = firstName.getText().trim();
        profile.suffix = suffix.getText().trim();
        profile.middleName = middleName.getText().trim();
        profile.lastName = lastName.getText().trim();
        profile.birthDate = birthDate.getText().trim();
        profile.gender = selected(gender);
        profile.civilStatus = selected(civilStatus);
        profile.houseNumber = houseNo.getText().trim();
        profile.street = street.getText().trim();
        profile.barangay = barangay.getText().trim();
        profile.city = city.getText().trim();
        profile.mobile = mobile.getText().trim();
        profile.email = email.getText().trim();
        try {
            AccountsStore.updateCurrentProfile(profile);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                    "Could not save your profile:\n" + ex.getMessage(),
                    "Profile save error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JOptionPane.showMessageDialog(this, "Your changes have been saved.");
        saved = values();

        // Back to the profile screen showing the updated details
        String fullName = (firstName.getText().trim() + " " + middleName.getText().trim() + " "
                + lastName.getText().trim() + " " + suffix.getText().trim()).trim();
        dispose();
        ProfileScreen.showWindow(fullName, email.getText().trim(), mobile.getText().trim());
    }


}