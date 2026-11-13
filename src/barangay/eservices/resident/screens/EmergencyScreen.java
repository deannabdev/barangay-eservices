/**
 * EmergencyScreen.java - Emergency Hotlines - dark red screen with CALL buttons.
 */
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.List;

enum HotlineStyle {
    //          card background           title            subtitle                      tagline                       icon bg                         icon fg                       CALL bg    CALL fg
    FEATURED(new Color(0, 0, 0, 0),       Color.WHITE,     new Color(0xF3, 0xB8, 0xB8),  Color.WHITE,                  new Color(0xA3, 0x2B, 0x2B),    new Color(0xFF, 0xE4, 0xE4),  Color.WHITE, Theme.RED),
    STANDARD(Color.WHITE,                 Theme.TEXT,      Theme.TEXT_SUB,               Theme.RED,                    Theme.RED_SOFT,                 Theme.RED,                    Theme.RED,   Color.WHITE);

    private final Color cardBg, title, subtitle, tagline, iconBg, iconFg, callBg, callFg;

    HotlineStyle(Color cardBg, Color title, Color subtitle, Color tagline,
                 Color iconBg, Color iconFg, Color callBg, Color callFg) {
        this.cardBg = cardBg;
        this.title = title;
        this.subtitle = subtitle;
        this.tagline = tagline;
        this.iconBg = iconBg;
        this.iconFg = iconFg;
        this.callBg = callBg;
        this.callFg = callFg;
    }

    public Color getCardBg()   { return cardBg; }
    public Color getTitle()    { return title; }
    public Color getSubtitle() { return subtitle; }
    public Color getTagline()  { return tagline; }
    public Color getIconBg()   { return iconBg; }
    public Color getIconFg()   { return iconFg; }
    public Color getCallBg()   { return callBg; }
    public Color getCallFg()   { return callFg; }
}

class Hotline {
    private final String name;
    private final String agency;
    private final String tagline;
    private final HotlineStyle style;

    Hotline(String name, String agency, String tagline, HotlineStyle style) {
        this.name = name;
        this.agency = agency;
        this.tagline = tagline;
        this.style = style;
    }

    public String getName()          { return name; }
    public String getAgency()        { return agency; }
    public String getTagline()       { return tagline; }
    public HotlineStyle getStyle()   { return style; }
}

class EmergencyScreen extends ScrollableScreen {
    private final NavListener nav;
    EmergencyScreen(NavListener nav) { this.nav = nav; }

    @Override protected String getHeaderTitle()    { return "Emergency Hotlines"; }
    @Override protected boolean hasHeader()        { return false; }
    @Override protected Color getBackgroundColor() { return Theme.DEEP_RED; }
    @Override protected AppSection createContent() { return new EmergencyContent(EmergencyData.getHotlines(), nav); }
}

class EmergencyContent extends AppSection {
    private final List<Hotline> hotlines;
    private final NavListener nav;

    EmergencyContent(List<Hotline> hotlines, NavListener nav) {
        this.hotlines = hotlines;
        this.nav = nav;
    }

    @Override
    protected void buildUI() {
        setLayout(new BorderLayout());
        setBackground(Theme.DEEP_RED);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(Theme.DEEP_RED);
        content.setBorder(new EmptyBorder(14, 16, 16, 16));

        JLabel heading = new JLabel("Emergency Hotlines");
        heading.setFont(Theme.bold(20));
        heading.setForeground(Color.WHITE);

        JLabel desc = new JLabel("<html><body width='310'>Tap any hotline below to initiate direct emergency "
                + "voice response. Available 24 hours a day, 7 days a week.</body></html>");
        desc.setFont(Theme.regular(12));
        desc.setForeground(new Color(0xF3, 0xCB, 0xCB));

        content.add(new BackButton(40, () -> nav.onNavigate("Home")));   // back to Home
        content.add(Box.createVerticalStrut(16));
        content.add(heading);
        content.add(Box.createVerticalStrut(8));
        content.add(desc);
        content.add(Box.createVerticalStrut(18));

        for (Hotline h : hotlines) {
            content.add(new HotlineRow(h));
            content.add(Box.createVerticalStrut(12));
        }

        for (Component c : content.getComponents()) ((JComponent) c).setAlignmentX(LEFT_ALIGNMENT);
        add(content, BorderLayout.NORTH);
    }
}

class HotlineRow extends RoundedPanel {
    private final Hotline hotline;

    HotlineRow(Hotline hotline) {
        super(hotline.getStyle().getCardBg(), null, 16);     // style decides how each row looks
        this.hotline = hotline;
        HotlineStyle st = hotline.getStyle();
        setLayout(new BorderLayout(12, 0));
        setBorder(new EmptyBorder(14, 12, 14, 12));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        RoundedPanel circle = new RoundedPanel(st.getIconBg(), null, 36);
        circle.setLayout(new GridBagLayout());
        circle.setPreferredSize(new Dimension(36, 36));
        circle.add(new IconLabel(IconType.PHONE, 16, st.getIconFg()));

        JLabel name = new JLabel("<html><body width='165'>" + hotline.getName() + "</body></html>");
        name.setFont(Theme.bold(12));
        name.setForeground(st.getTitle());
        JLabel agency = new JLabel(hotline.getAgency());
        agency.setFont(Theme.regular(9));
        agency.setForeground(st.getSubtitle());
        JLabel tag = new JLabel(hotline.getTagline());
        tag.setFont(Theme.bold(10));
        tag.setForeground(st.getTagline());

        JPanel texts = new JPanel();
        texts.setOpaque(false);
        texts.setLayout(new BoxLayout(texts, BoxLayout.Y_AXIS));
        texts.add(name);
        texts.add(Box.createVerticalStrut(2));
        texts.add(agency);
        texts.add(Box.createVerticalStrut(2));
        texts.add(tag);
        for (Component c : texts.getComponents()) ((JComponent) c).setAlignmentX(LEFT_ALIGNMENT);
        JPanel textWrap = new JPanel(new GridBagLayout());
        textWrap.setOpaque(false);
        GridBagConstraints gc = new GridBagConstraints();
        gc.anchor = GridBagConstraints.WEST;
        gc.weightx = 1;
        textWrap.add(texts, gc);

        add(Theme.wrap(circle), BorderLayout.WEST);
        add(textWrap, BorderLayout.CENTER);
        add(Theme.wrap(new CallPill(st.getCallBg(), st.getCallFg(), this::call)), BorderLayout.EAST);

        addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { call(); }
        });
    }

    private void call() {
        int choice = JOptionPane.showConfirmDialog(this, "Call " + hotline.getName() + " now?",
                "Emergency Call", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (choice == JOptionPane.YES_OPTION) {
            JOptionPane.showMessageDialog(this, "Dialing " + hotline.getName() + "...",
                    "Emergency Call", JOptionPane.INFORMATION_MESSAGE);
        }
    }
}

class CallPill extends RoundedPanel {
    CallPill(Color bg, Color fg, Runnable onClick) {
        super(bg, null, 16);
        setLayout(new GridBagLayout());
        setPreferredSize(new Dimension(46, 22));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        JLabel l = new JLabel("CALL");
        l.setFont(Theme.bold(9));
        l.setForeground(fg);
        add(l);
        addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { onClick.run(); }
        });
    }

    @Override public Dimension getMaximumSize() { return getPreferredSize(); }
}

/** Sample data for this screen. */
class EmergencyData {
    static List<Hotline> getHotlines() {
        List<Hotline> list = new ArrayList<>();
        list.add(new Hotline("Barangay Rescue Command", "Local Central Signal Emergency Team",
                "Available 24/7", HotlineStyle.FEATURED));
        list.add(new Hotline("Local Police Station", "Taguig City Police Substation",
                "Immediate Police Response", HotlineStyle.STANDARD));
        list.add(new Hotline("Local Fire Department", "Taguig City Fire Station Central",
                "Fire and Arson Emergencies", HotlineStyle.STANDARD));
        list.add(new Hotline("Red Cross & Medical Ambulance", "Emergency Medical Transport",
                "Crisis & Ambulance Service", HotlineStyle.STANDARD));
        return list;
    }
}
