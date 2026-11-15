package barangay.eservices;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class ProfileScreen extends ProfileScreenBackgroundPanel {

    private static final int W = 351, H = 775;
    private static final int M = 29, CW = W - 2 * M;   // side margin and card width

    // ---- User data shown on the screen (call setUser to change) ----
    private final JLabel nameLabel    = new JLabel("Your Name", SwingConstants.CENTER);
    private final JLabel contactLabel = new JLabel("", SwingConstants.CENTER);
    private final Navigator navigator;

    public ProfileScreen() {
        this(null);
    }

    ProfileScreen(Navigator navigator) {
        this.navigator = navigator;
        setLayout(null);
        setPreferredSize(new Dimension(W, navigator == null ? H : H - 86));
        buildUI(this);
        loadCurrentUser();
    }

    private void openWindow() {
        JFrame window = new JFrame("Account Profile");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setResizable(false);
        window.setContentPane(this);
        window.pack();
        window.setLocationRelativeTo(null);
        window.setVisible(true);
    }

    public static void showWindow(String name, String email, String phone) {
        ProfileScreen profile = new ProfileScreen();
        profile.setUser(name, email, phone);
        profile.openWindow();
    }

    public static void showWindow() {
        new ProfileScreen().openWindow();
    }

    /** Updates the name, email and phone shown in the profile card. */
    public void setUser(String name, String email, String phone) {
        nameLabel.setText(name == null || name.trim().isEmpty() ? "Your Name" : name.trim());
        String contact = email == null ? "" : email.trim();
        if (phone != null && !phone.trim().isEmpty()) {
            contact += (contact.isEmpty() ? "" : " | ") + phone.trim();
        }
        contactLabel.setText(contact);
    }

    private void loadCurrentUser() {
        try {
            AccountProfile profile = AccountsStore.getCurrentProfile();
            if (profile == null) {
                setUser("Your Name", "", "");
                return;
            }

            StringBuilder name = new StringBuilder();
            appendNamePart(name, profile.firstName);
            appendNamePart(name, profile.middleName);
            appendNamePart(name, profile.lastName);
            appendNamePart(name, profile.suffix);
            setUser(name.toString(), profile.email, profile.mobile);
        } catch (IOException ex) {
            setUser("Profile unavailable", "", "");
            JOptionPane.showMessageDialog(this,
                    "Could not read the local account profile:\n" + ex.getMessage(),
                    "Profile error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void appendNamePart(StringBuilder name, String part) {
        if (part != null && !part.trim().isEmpty()) {
            if (name.length() > 0) name.append(' ');
            name.append(part.trim());
        }
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
                () -> {
                    closeWindow();
                    new ProfileInfo().setVisible(true);
                });
        edit.setBounds(M, 383, CW, 46);
        root.add(edit);

        ProfileMenuRow security = new ProfileMenuRow("Security & Log In", ProfileMenuRow.Icon.NEWS, 42,
                () -> {
                    closeWindow();
                    new Security().setVisible(true);
                });
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
        if (navigator == null) {
            ProfileNavBar nav = new ProfileNavBar(4, this::navigate);
            nav.setBounds(0, 689, W, H - 689);
            root.add(nav);
        }
    }

    private void navigate(int index) {
        String[] names = {"Home", "E-Services", "Reports", "Guide", "Profile"};
        String screen = names[index];
        if (navigator != null) {
            navigator.onNavigate(screen);
        } else if (index != 4) {
            closeWindow();
            new HomeWindow(screen).setVisible(true);
        }
    }

    private void logOut() {
        int answer = JOptionPane.showConfirmDialog(this, "Are you sure you want to log out?",
                "Log Out", JOptionPane.YES_NO_OPTION);
        if (answer == JOptionPane.YES_OPTION) {
            AccountsStore.clearCurrentAccount();
            closeWindow();
            new LoginScreen().setVisible(true);
        }
    }

    private void closeWindow() {
        Window window = SwingUtilities.getWindowAncestor(this);
        if (window != null) window.dispose();
    }

    private void comingSoon(String what) {
        JOptionPane.showMessageDialog(this, what + " - coming soon.");
    }


}