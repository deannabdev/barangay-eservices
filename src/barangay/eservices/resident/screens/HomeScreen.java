package barangay.eservices;
import javax.swing.*;
import java.awt.*;

class HomeScreen extends ScrollableScreen {
    private final NavListener nav;
    HomeScreen(NavListener nav) { this.nav = nav; }

    @Override protected String getHeaderTitle()    { return "Central Signal Hub"; }
    @Override protected boolean hasHeader()        { return false; }
    @Override protected JComponent getFixedHeader() {
        return new HomeComponents.HeaderBar("Barangay Central Signal Hub", "Central Signal, Taguig");
    }
    @Override protected AppSection createContent() { return new HomeContent(nav); }
}

class HomeContent extends AppSection implements Scrollable {
    private final NavListener nav;
    HomeContent(NavListener nav) { this.nav = nav; }

    @Override
    protected void buildUI() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(HomeComponents.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(0, 16, 32, 16));

        add(Box.createVerticalStrut(14));
        add(HomeComponents.fullWidth(new HomeComponents.HeroBanner(
                "Mabuhay, Kabarangay!",
                "Welcome to the official digital portal of Barangay Central Signal. "
                        + "Request documents, report incidents, and stay updated with local civic news.")));
        add(Box.createVerticalStrut(22));
        add(HomeComponents.fullWidth(new HomeComponents.SectionTitle("Quick Access Portal")));
        add(Box.createVerticalStrut(10));
        add(HomeComponents.fullWidth(buildQuickAccessGrid()));
        add(Box.createVerticalStrut(22));
        add(HomeComponents.fullWidth(new HomeComponents.SectionTitle("Barangay Announcements")));
        add(Box.createVerticalStrut(10));
        add(HomeComponents.fullWidth(new HomeComponents.AnnouncementCard(
                "Health Advisory",
                "Oct 24, 2026",
                "Free Pediatric Vaccination Drive at Ballecer St. Health Center",
                () -> JOptionPane.showMessageDialog(this, "Announcement details screen is not available yet."))));
        add(Box.createVerticalStrut(16));
    }

    private JPanel buildQuickAccessGrid() {
        JPanel grid = new JPanel(new GridLayout(2, 3, 6, 6));
        grid.setOpaque(false);
        grid.add(card(HomeComponents.VectorIcon.Type.DOCUMENT, 0xDBEAFE, 0x2563EB,
                "E-Services", "Request Docs", "E-Services"));
        grid.add(card(HomeComponents.VectorIcon.Type.ALERT_CIRCLE, 0xFEF3C7, 0xD97706,
                "Reports", "File Incident", "Reports"));
        grid.add(card(HomeComponents.VectorIcon.Type.TRUCK, 0xDCFCE7, 0x16A34A,
                "Transport", "Utility Service", "Transport"));
        grid.add(card(HomeComponents.VectorIcon.Type.PHONE, 0xFEE2E2, 0xDC2626,
                "Emergency", "Local Hotlines", "Emergency"));
        grid.add(card(HomeComponents.VectorIcon.Type.BOOK, 0xEDE9FE, 0x7C3AED,
                "Citizen Guide", "Brgy Info", "Guide"));
        grid.add(card(HomeComponents.VectorIcon.Type.BUILDING, 0xE0F2FE, 0x0284C7,
                "Local Businesses", "Directory", "Directory"));
        return grid;
    }

    private HomeComponents.QuickAccessCard card(HomeComponents.VectorIcon.Type icon,
                                                  int tileRgb, int iconRgb, String title,
                                                  String subtitle, String screen) {
        return new HomeComponents.QuickAccessCard(icon, new Color(tileRgb), new Color(iconRgb),
                title, subtitle, () -> nav.onNavigate(screen));
    }

    @Override public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
    @Override public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) { return 16; }
    @Override public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        return visibleRect.height;
    }
    @Override public boolean getScrollableTracksViewportWidth() { return true; }
    @Override public boolean getScrollableTracksViewportHeight() { return false; }
}

class HomeWindow extends JFrame {
    private static final int W = 351, H = 775;
    private final Navigator navigator;

    HomeWindow() {
        this("Home");
    }

    HomeWindow(String initialScreen) {
        super("Barangay Central Signal Hub");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);

        CardLayout cardLayout = new CardLayout();
        JPanel screens = new JPanel(cardLayout);
        navigator = new Navigator(cardLayout, screens);

        screens.add(new HomeScreen(navigator).init(), "Home");
        screens.add(new EServicesScreen().init(), "E-Services");
        screens.add(new ReportsScreen().init(), "Reports");
        screens.add(new TransportScreen(navigator).init(), "Transport");
        screens.add(new EmergencyScreen(navigator).init(), "Emergency");
        screens.add(new GuideScreen().init(), "Guide");
        screens.add(new DirectoryScreen(navigator).init(), "Directory");
        screens.add(new ProfileScreen(navigator), "Profile");

        String[] screenNames = {"Home", "E-Services", "Reports", "Guide", "Profile"};
        ProfileNavBar bottomNav = new ProfileNavBar(0, index -> {
            navigator.onNavigate(screenNames[index]);
        });

        JPanel root = new JPanel(new BorderLayout());
        root.add(screens, BorderLayout.CENTER);
        root.add(bottomNav, BorderLayout.SOUTH);
        root.setPreferredSize(new Dimension(W, H));
        setContentPane(root);

        navigator.attach(screen -> {
            int active = switch (screen) {
                case "E-Services" -> 1;
                case "Reports" -> 2;
                case "Guide" -> 3;
                case "Profile" -> 4;
                default -> 0;
            };
            bottomNav.setActive(active);
        });

        pack();
        setLocationRelativeTo(null);
        navigator.onNavigate(initialScreen);
    }
}
