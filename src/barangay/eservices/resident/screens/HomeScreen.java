/**
 * HomeScreen.java - Home - Central Signal Hub (hero banner, hotlines card, quick access portal, announcements).
 */
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

class PortalService {
    private final String title;
    private final String subtitle;
    private final IconType icon;
    private final Color soft;
    private final Color accent;
    private final String target;   // screen key to open, or null for a simple dialog

    PortalService(String title, String subtitle, IconType icon, Color soft, Color accent, String target) {
        this.title = title;
        this.subtitle = subtitle;
        this.icon = icon;
        this.soft = soft;
        this.accent = accent;
        this.target = target;
    }

    public String getTitle()    { return title; }
    public String getSubtitle() { return subtitle; }
    public IconType getIcon()   { return icon; }
    public Color getSoft()      { return soft; }
    public Color getAccent()    { return accent; }
    public String getTarget()   { return target; }
}

class Announcement {
    private final String tag;
    private final String date;
    private final String title;

    Announcement(String tag, String date, String title) {
        this.tag = tag;
        this.date = date;
        this.title = title;
    }

    public String getTag()   { return tag; }
    public String getDate()  { return date; }
    public String getTitle() { return title; }
}

class HomeScreen extends ScrollableScreen {
    private final NavListener nav;
    HomeScreen(NavListener nav) { this.nav = nav; }

    @Override protected String getHeaderTitle()    { return "Central Signal Hub"; }
    @Override protected AppSection createContent() { return new HomeContent(nav); }
}

class HomeContent extends AppSection {
    private final NavListener nav;
    HomeContent(NavListener nav) { this.nav = nav; }

    @Override
    protected void buildUI() {
        setLayout(new BorderLayout());
        setBackground(Theme.BG);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(Theme.BG);
        content.setBorder(new EmptyBorder(14, 16, 16, 16));

        content.add(new HeroBanner());
        content.add(Box.createVerticalStrut(14));
        content.add(new HotlineCard(nav));
        content.add(Box.createVerticalStrut(18));
        content.add(sectionTitle("Quick Access Portal"));
        content.add(Box.createVerticalStrut(10));

        JPanel grid = new JPanel(new GridLayout(2, 3, 10, 10));
        grid.setOpaque(false);
        for (PortalService p : HomeData.getPortalServices()) grid.add(new PortalTile(p, nav));
        grid.setPreferredSize(new Dimension(358, 184));
        grid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 184));
        content.add(grid);

        content.add(Box.createVerticalStrut(18));
        content.add(sectionTitle("Barangay Announcements"));
        content.add(Box.createVerticalStrut(10));
        for (Announcement a : HomeData.getAnnouncements()) {
            content.add(new AnnouncementCard(a));
            content.add(Box.createVerticalStrut(10));
        }

        for (Component c : content.getComponents()) ((JComponent) c).setAlignmentX(LEFT_ALIGNMENT);
        add(content, BorderLayout.NORTH);
    }

    private JLabel sectionTitle(String text) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.bold(13));
        l.setForeground(Theme.TEXT);
        return l;
    }
}

class HeroBanner extends RoundedPanel {
    HeroBanner() {
        super(new Color(0x86, 0x26, 0x26), null, 16);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(new EmptyBorder(26, 16, 26, 16));

        JLabel title = new JLabel("Mabuhay, Kabarangay!");
        title.setFont(Theme.bold(18));
        title.setForeground(Color.WHITE);

        JLabel text = new JLabel("<html><body width='300'>Welcome to the official digital portal of "
                + "Barangay Central Signal. Request documents, report incidents, and stay updated with local civic "
                + "news.</body></html>");
        text.setFont(Theme.regular(11));
        text.setForeground(new Color(0xF6, 0xCF, 0xCF));

        add(title);
        add(Box.createVerticalStrut(8));
        add(text);
        for (Component c : getComponents()) ((JComponent) c).setAlignmentX(LEFT_ALIGNMENT);
    }
}

class HotlineCard extends RoundedPanel {
    HotlineCard(NavListener nav) {
        super(new Color(0xFE, 0xEC, 0xEC), new Color(0xF3, 0xA6, 0xA6), 14);
        setLayout(new BorderLayout(12, 0));
        setBorder(new EmptyBorder(12, 12, 12, 12));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        RoundedPanel circle = new RoundedPanel(new Color(0xD3, 0x2F, 0x2F), null, 34);
        circle.setLayout(new GridBagLayout());
        circle.setPreferredSize(new Dimension(34, 34));
        circle.add(new IconLabel(IconType.TRUCK, 17, Color.WHITE));

        JLabel title = new JLabel("Emergency Hotlines (24/7)");
        title.setFont(Theme.bold(12));
        title.setForeground(new Color(0xC6, 0x28, 0x28));
        JLabel sub = new JLabel("<html><body width='230'>Immediate one-tap access to Brgy Rescue, "
                + "Police, and Fire Dept.</body></html>");
        sub.setFont(Theme.regular(10));
        sub.setForeground(new Color(0x7F, 0x1D, 0x1D));

        JPanel texts = new JPanel();
        texts.setOpaque(false);
        texts.setLayout(new BoxLayout(texts, BoxLayout.Y_AXIS));
        texts.add(title);
        texts.add(Box.createVerticalStrut(2));
        texts.add(sub);
        for (Component c : texts.getComponents()) ((JComponent) c).setAlignmentX(LEFT_ALIGNMENT);
        JPanel textWrap = new JPanel(new GridBagLayout());
        textWrap.setOpaque(false);
        GridBagConstraints gc = new GridBagConstraints();
        gc.anchor = GridBagConstraints.WEST;
        gc.weightx = 1;
        textWrap.add(texts, gc);

        add(Theme.wrap(circle), BorderLayout.WEST);
        add(textWrap, BorderLayout.CENTER);
        add(Theme.wrap(new IconLabel(IconType.CHEVRON, 14, new Color(0xC6, 0x28, 0x28))), BorderLayout.EAST);

        addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                nav.onNavigate("Emergency");
            }
        });
    }
}

class PortalTile extends RoundedPanel {
    private final PortalService service;

    PortalTile(PortalService service, NavListener nav) {
        super(Color.WHITE, Theme.BORDER, 14);
        this.service = service;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(new EmptyBorder(12, 10, 12, 10));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        RoundedPanel box = new RoundedPanel(service.getSoft(), null, 10);
        box.setLayout(new GridBagLayout());
        box.setPreferredSize(new Dimension(30, 30));
        box.setMaximumSize(new Dimension(30, 30));
        box.add(new IconLabel(service.getIcon(), 15, service.getAccent()));

        JLabel title = new JLabel(service.getTitle());
        title.setFont(Theme.bold(10));
        title.setForeground(Theme.TEXT);
        JLabel sub = new JLabel(service.getSubtitle());
        sub.setFont(Theme.regular(9));
        sub.setForeground(Theme.MUTED);

        add(box);
        add(Box.createVerticalStrut(10));
        add(title);
        add(sub);
        for (Component c : getComponents()) ((JComponent) c).setAlignmentX(LEFT_ALIGNMENT);

        addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (PortalTile.this.service.getTarget() != null) {
                    nav.onNavigate(PortalTile.this.service.getTarget());
                } else {
                    JOptionPane.showMessageDialog(PortalTile.this,
                            PortalTile.this.service.getTitle() + " - coming soon",
                            PortalTile.this.service.getTitle(), JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });
    }

    @Override
    public Dimension getMaximumSize() { return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE); }
}

class AnnouncementCard extends RoundedPanel {
    AnnouncementCard(Announcement a) {
        super(Color.WHITE, Theme.BORDER, 14);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(new EmptyBorder(12, 12, 12, 12));

        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        JPanel tagWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        tagWrap.setOpaque(false);
        tagWrap.add(new Badge(a.getTag(), Theme.RED_SOFT, Theme.RED, null, 8));
        JLabel date = new JLabel(a.getDate());
        date.setFont(Theme.regular(9));
        date.setForeground(Theme.MUTED);
        row.add(tagWrap, BorderLayout.WEST);
        row.add(Theme.wrap(date), BorderLayout.EAST);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));

        JLabel title = new JLabel("<html><body width='310'>" + a.getTitle() + "</body></html>");
        title.setFont(Theme.bold(12));
        title.setForeground(Theme.TEXT);

        add(row);
        add(Box.createVerticalStrut(8));
        add(title);
        for (Component c : getComponents()) ((JComponent) c).setAlignmentX(LEFT_ALIGNMENT);
    }
}

/** Sample data for this screen. */
class HomeData {
    static List<PortalService> getPortalServices() {
        List<PortalService> list = new ArrayList<>();
        list.add(new PortalService("E-Services", "Request Docs", IconType.DOC,
                new Color(0xDB, 0xEA, 0xFE), new Color(0x25, 0x63, 0xEB), "E-Services"));
        list.add(new PortalService("Reports", "File Incident", IconType.ALERT_CIRCLE,
                new Color(0xFE, 0xF3, 0xC7), new Color(0xD9, 0x77, 0x06), "Reports"));
        list.add(new PortalService("Transport", "Utility Service", IconType.TRUCK,
                new Color(0xDC, 0xFC, 0xE7), new Color(0x16, 0xA3, 0x4A), "Transport"));
        list.add(new PortalService("Emergency", "Local Hotlines", IconType.PHONE,
                new Color(0xFE, 0xE2, 0xE2), new Color(0xDC, 0x26, 0x26), "Emergency"));
        list.add(new PortalService("Citizen Guide", "Brgy Info", IconType.BOOK,
                new Color(0xF3, 0xE8, 0xFF), new Color(0x93, 0x33, 0xEA), "Guide"));
        list.add(new PortalService("Local Businesses", "Directory", IconType.BUILDING,
                new Color(0xE0, 0xF2, 0xFE), new Color(0x02, 0x84, 0xC7), "Directory"));
        return list;
    }

    static List<Announcement> getAnnouncements() {
        List<Announcement> list = new ArrayList<>();
        list.add(new Announcement("HEALTH ADVISORY", "Oct 24, 2026",
                "Free Pediatric Vaccination Drive at Ballecer St. Health Center"));
        return list;
    }
}
