package barangay.eservices;
import javax.swing.*;
import java.awt.*;

public class ProfileScreen extends JFrame {

    private static final int W = 351, H = 775;
    private static final int M = 29, CW = W - 2 * M;   // side margin and card width

    // ---- User data shown on the screen (call setUser to change) ----
    private final JLabel nameLabel    = new JLabel("Your Name", SwingConstants.CENTER);
    private final JLabel contactLabel = new JLabel("", SwingConstants.CENTER);

    public ProfileScreen() {
        super("Account Profile");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);

        ProfileScreenBackgroundPanel root = new ProfileScreenBackgroundPanel();
        root.setLayout(null);
        root.setPreferredSize(new Dimension(W, H));
        buildUI(root);
        setUser("Juan Dela Cruz", "juandelacruz@gmail.com", "09123456789");

        setContentPane(root);
        pack();
        setLocationRelativeTo(null);
    }

    /** Updates the name, email and phone shown in the profile card. */
    public void setUser(String name, String email, String phone) {
        nameLabel.setText(name);
        contactLabel.setText(email + " | " + phone);
    }

    private void buildUI(JPanel root) {
        // Header title
        JLabel title = new JLabel("Account Profile", SwingConstants.CENTER);
        title.setFont(ProfileTheme.font(Font.BOLD, 20f));
        title.setForeground(Color.WHITE);
        title.setBounds(0, 52, W, 36);
        root.add(title);

        // Profile card (background, then avatar + text on top)
        ProfileCardBox profileCard = new ProfileCardBox();
        profileCard.setBounds(M, 157, CW, 198);

        ProfileAvatarView avatar = new ProfileAvatarView(() -> comingSoon("Change profile photo"));
        avatar.setBounds(W / 2 - 70, 163, 140, 140);
        root.add(avatar);

        nameLabel.setFont(ProfileTheme.font(Font.BOLD, 12.5f));
        nameLabel.setForeground(ProfileTheme.MAROON);
        nameLabel.setBounds(M, 304, CW, 20);
        root.add(nameLabel);

        contactLabel.setFont(ProfileTheme.font(Font.BOLD, 10.5f));
        contactLabel.setForeground(ProfileTheme.MAROON);
        contactLabel.setBounds(M, 322, CW, 18);
        root.add(contactLabel);

        root.add(profileCard);

        // Menu rows
        ProfileMenuRow edit = new ProfileMenuRow("Edit profile information", ProfileMenuRow.Icon.NEWS, 42,
                () -> { new ProfileInfo().setVisible(true); dispose(); });
        edit.setBounds(M, 383, CW, 46);
        root.add(edit);

        ProfileMenuRow security = new ProfileMenuRow("Security & Log In", ProfileMenuRow.Icon.NEWS, 42,
                () -> { new Security().setVisible(true); dispose(); });
        security.setBounds(M, 435, CW, 46);
        root.add(security);

        ProfileMenuRow logout = new ProfileMenuRow("Log Out", ProfileMenuRow.Icon.EXIT, 28, this::logOut);
        logout.setBounds(M, 487, CW, 32);
        root.add(logout);

        // Active requests
        JLabel requests = new JLabel("Your Active Requests");
        requests.setFont(ProfileTheme.font(Font.BOLD, 12f));
        requests.setForeground(ProfileTheme.NAVY);
        requests.setBounds(M, 530, CW, 18);
        root.add(requests);

        String[][] data = {
                {"Barangay Clearance Request", "Ref: #BCS-2026-0894", "PENDING"},
                // add more: {"Title", "Ref: #...", "APPROVED"}
        };
        int y = 557;
        for (String[] r : data) {
            ProfileRequestCard card = new ProfileRequestCard(r[0], r[1], r[2]);
            card.setBounds(M, y, CW, 57);
            root.add(card);
            y += 63;
        }

        // Bottom navigation
        ProfileNavBar nav = new ProfileNavBar(4, this::navigate);
        nav.setBounds(0, 689, W, H - 689);
        root.add(nav);
    }

    private void navigate(int index) {
        // 0 Home, 1 E-Services, 2 Reports, 3 Guide, 4 Profile (this screen)
        if (index == 4) return;
        String[] names = {"Home", "E-Services", "Reports", "Guide"};
        // TODO: open the matching screen here and dispose() this one.
        comingSoon(names[index]);
    }

    private void logOut() {
        int answer = JOptionPane.showConfirmDialog(this, "Are you sure you want to log out?",
                "Log Out", JOptionPane.YES_NO_OPTION);
        if (answer == JOptionPane.YES_OPTION) {
            new LoginScreen().setVisible(true);
            dispose();
        }
    }

    private void comingSoon(String what) {
        JOptionPane.showMessageDialog(this, what + " - coming soon.");
    }


}