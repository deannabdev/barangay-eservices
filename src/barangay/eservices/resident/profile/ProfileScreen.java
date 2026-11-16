package barangay.eservices;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ProfileScreen extends ProfileScreenBackgroundPanel {

    private static final int W = 351, H = 775;
    private static final int M = 29, CW = W - 2 * M;   // side margin and card width

    // ---- User data shown on the screen (call setUser to change) ----
    private final JLabel nameLabel    = new JLabel("Your Name", SwingConstants.CENTER);
    private final JLabel contactLabel = new JLabel("", SwingConstants.CENTER);
    private final JPanel activeItems = new JPanel();
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
        if (navigator != null) {
            navigator.attach(screen -> {
                if ("Profile".equals(screen)) refreshActiveItems();
            });
        }
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
            } else {
                StringBuilder name = new StringBuilder();
                appendNamePart(name, profile.firstName);
                appendNamePart(name, profile.middleName);
                appendNamePart(name, profile.lastName);
                appendNamePart(name, profile.suffix);
                setUser(name.toString(), profile.email, profile.mobile);
            }
            refreshActiveItems();
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

        JLabel requests = new JLabel("Your Active Reports & Requests");
        requests.setFont(ProfileTheme.font(Font.BOLD, 12f));
        requests.setForeground(ProfileTheme.NAVY);
        requests.setBounds(M, 530, CW, 18);
        root.add(requests);

        activeItems.setOpaque(false);
        activeItems.setLayout(new BoxLayout(activeItems, BoxLayout.Y_AXIS));
        JScrollPane activeItemsScroll = new JScrollPane(activeItems);
        activeItemsScroll.setBounds(M, 551, CW, 130);
        activeItemsScroll.setBorder(null);
        activeItemsScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        activeItemsScroll.getViewport().setOpaque(false);
        activeItemsScroll.setOpaque(false);
        activeItemsScroll.getVerticalScrollBar().setUnitIncrement(12);
        root.add(activeItemsScroll);

        // Bottom navigation
        if (navigator == null) {
            ProfileNavBar nav = new ProfileNavBar(4, this::navigate);
            nav.setBounds(0, 689, W, H - 689);
            root.add(nav);
        }
    }

    private void refreshActiveItems() {
        activeItems.removeAll();
        try {
            List<Submissions.SubmissionRecord> records = new ArrayList<>();
            records.addAll(AccountsStore.getCurrentSubmissions("REPORT"));
            records.addAll(AccountsStore.getCurrentSubmissions("DOCUMENT"));
            records.sort((first, second) -> Long.compare(second.getSubmittedAt(), first.getSubmittedAt()));

            if (records.isEmpty()) {
                JLabel empty = new JLabel("No active reports or requests yet.");
                empty.setFont(ProfileTheme.font(Font.PLAIN, 9.5f));
                empty.setForeground(ProfileTheme.GRAY);
                empty.setBorder(new EmptyBorder(10, 4, 0, 0));
                activeItems.add(empty);
            } else {
                for (Submissions.SubmissionRecord record : records) {
                    String type = "REPORT".equals(record.kind) ? "Report: " : "Request: ";
                    ProfileRequestCard card = new ProfileRequestCard(
                            type + record.title, record.reference, record.status);
                    card.setPreferredSize(new Dimension(CW, 54));
                    card.setMinimumSize(new Dimension(CW, 54));
                    card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 54));
                    card.setAlignmentX(LEFT_ALIGNMENT);
                    activeItems.add(card);
                    activeItems.add(Box.createVerticalStrut(4));
                }
            }
        } catch (IOException ex) {
            JLabel error = new JLabel("Could not load active items.");
            error.setFont(ProfileTheme.font(Font.PLAIN, 9.5f));
            error.setForeground(ProfileTheme.GRAY);
            activeItems.add(error);
            JOptionPane.showMessageDialog(this,
                    "Could not load your active reports and requests:\n" + ex.getMessage(),
                    "Active items error", JOptionPane.ERROR_MESSAGE);
        }
        activeItems.revalidate();
        activeItems.repaint();
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