package barangay.eservices;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;

public class RegisterScreen3 extends JFrame {

    private static final int W = 351, H = 775;

    // ---- Form fields ----
    private final JComboBox<String> idType = new RegistrationComboBox(
            RegistrationTheme.font(Font.PLAIN, 11f), RegistrationTheme.NAVY,
            "Philippine National ID (PhilSys)", "Driver's License", "Passport", "UMID",
            "Voter's ID", "Postal ID", "PRC ID", "Senior Citizen ID");
    private final RegistrationPasswordField password = new RegistrationPasswordField("Enter password");
    private final RegistrationPasswordField confirm  = new RegistrationPasswordField("Re-enter password");
    private final RegistrationData data;
    private RegistrationUploadBox uploadBox;
    private File idPhoto;

    public RegisterScreen3() {
        this(new RegistrationData());
    }

    RegisterScreen3(RegistrationData data) {
        super("Resident Registration");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
        this.data = data;
        
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
        ShadowLabel title = new ShadowLabel("Resident Registration", RegistrationTheme.RED);
        title.setFont(RegistrationTheme.font(Font.BOLD, 22f));
        title.setBounds(fieldX - 8, 88, 267, 44);
        root.add(title);

        JLabel subtitle = new JLabel("Create your account to access barangay e-services");
        subtitle.setFont(RegistrationTheme.font(Font.PLAIN, 12f));
        subtitle.setForeground(RegistrationTheme.NAVY);
        subtitle.setBounds(fieldX, 120, 267, 18);
        root.add(subtitle);

        // Step indicator (1 - 2 - 3)
        RegistrationStepIndicator steps = new RegistrationStepIndicator(
                RegistrationTheme.font(Font.BOLD, 12f), RegistrationTheme.RED, true);
        steps.setStep(3);
        steps.setBounds(0, 140, W, 44);
        root.add(steps);

        // ID Verification Section
        RegistrationSectionTitle idSection = new RegistrationSectionTitle("ID Verification",
                RegistrationTheme.font(Font.BOLD, 12f), RegistrationTheme.NAVY,
                RegistrationSectionTitle.Icon.ID_CARD);
        idSection.setBounds(fieldX + 1, 194, 270, 22);
        root.add(idSection);

        addLabel(root, "ID Type", fieldX, 238);
        RegistrationFieldBox typeBox = new RegistrationFieldBox(
                idType, 3, 3, RegistrationFieldBox.Deco.NONE, RegistrationTheme.NAVY);
        typeBox.setBounds(fieldX, 238, 271, 30);
        root.add(typeBox);

        uploadBox = new RegistrationUploadBox(this::chooseIdPhoto);
        uploadBox.setBounds(fieldX, 278, 271, 96);
        root.add(uploadBox);

        // Security Section
        RegistrationSectionTitle security = new RegistrationSectionTitle("Security",
                RegistrationTheme.font(Font.BOLD, 12f), RegistrationTheme.NAVY,
                RegistrationSectionTitle.Icon.LOCK);
        security.setBounds(fieldX + 1, 400, 270, 22);
        root.add(security);

        addLabel(root, "Password", fieldX, 440);
        RegistrationFieldBox passBox = passwordBox(password);
        passBox.setBounds(fieldX, 440, 271, 30);
        root.add(passBox);

        JLabel hint = new JLabel("Must contain at least 8 characters");
        hint.setFont(RegistrationTheme.font(Font.PLAIN, 9f));
        hint.setForeground(RegistrationTheme.NAVY);
        hint.setBounds(fieldX + 2, 472, 270, 12);
        root.add(hint);

        addLabel(root, "Confirm Password", fieldX, 514);
        RegistrationFieldBox confirmBox = passwordBox(confirm);
        confirmBox.setBounds(fieldX, 514, 271, 30);
        root.add(confirmBox);

        //Back button
        RegistrationButton back = new RegistrationButton("Back",
                RegistrationTheme.font(Font.BOLD, 13f), RegistrationTheme.RED, true);
        back.setBounds(sectionX, 572, 127, 33);
        back.addActionListener(e -> {
            new RegisterScreen2(data).setVisible(true);
            dispose();
        });
        root.add(back);

        // Register button
        RegistrationButton register = new RegistrationButton("Register",
                RegistrationTheme.font(Font.BOLD, 13f), RegistrationTheme.RED);
        register.setBounds(sectionX + 140, 572, 127, 33);
        register.addActionListener(e -> onRegister());
        root.add(register);
    }

    /** Small label placed above an input whose visible box starts at (x, y). */
    private void addLabel (JPanel root, String text, int x, int y) {
        JLabel l = new JLabel(text);
        l.setFont(RegistrationTheme.font(Font.PLAIN, 10f));
        l.setForeground(RegistrationTheme.NAVY);
        l.setBounds(x, y - 17, 267, 14);
        root.add(l);
    }

    /** Password input: lock icon on the left, show/hide eye on the right. */
    private RegistrationFieldBox passwordBox(RegistrationPasswordField pf) {
        RegistrationFieldBox box = new RegistrationFieldBox(pf, 10, 36,
                RegistrationFieldBox.Deco.LOCK, RegistrationTheme.NAVY);
        RegistrationEyeButton eye = new RegistrationEyeButton();
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

}