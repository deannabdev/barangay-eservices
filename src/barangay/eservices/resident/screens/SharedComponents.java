import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

// ============================== THEME ==============================
final class Theme {
    static final Color RED = new Color(0xDC, 0x2F, 0x2F);
    static final Color RED_DARK = new Color(0xA8, 0x1C, 0x1C);
    static final Color DEEP_RED = new Color(0x84, 0x1B, 0x1B);
    static final Color RED_SOFT = new Color(0xFD, 0xE8, 0xE8);
    static final Color BG = new Color(0xF8, 0xF9, 0xFB);
    static final Color WHITE = Color.WHITE;
    static final Color BORDER = new Color(0xE5, 0xE7, 0xEB);
    static final Color TEXT = new Color(0x11, 0x18, 0x27);
    static final Color TEXT_SUB = new Color(0x4B, 0x55, 0x63);
    static final Color MUTED = new Color(0x6B, 0x72, 0x80);
    static final Color GREEN = new Color(0x16, 0xA3, 0x4A);
    static final Color GREEN_SOFT = new Color(0xDC, 0xFC, 0xE7);
    static final Color AMBER = new Color(0xD9, 0x77, 0x06);
    static final Color AMBER_SOFT = new Color(0xFE, 0xF3, 0xC7);

    private Theme() {}

    static Font regular(int size) { return new Font("Segoe UI", Font.PLAIN, size); }
    static Font bold(int size)    { return new Font("Segoe UI", Font.BOLD, size); }

    static void smooth(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
    }

    static JPanel wrap(JComponent c) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);
        p.add(c);
        return p;
    }
}

// ===================== ENCAPSULATION: MODELS =====================
class DocumentRequest {
    private final String title;
    private final String requirements;
    private final String processing;
    private final Fee fee;

    DocumentRequest(String title, String requirements, String processing, Fee fee) {
        this.title = title;
        this.requirements = requirements;
        this.processing = processing;
        this.fee = fee;
    }

    public String getTitle()        { return title; }
    public String getRequirements() { return requirements; }
    public String getProcessing()   { return processing; }
    public Fee getFee()             { return fee; }
}

class ConcernCategory {
    private final String name;
    private final IconType icon;

    ConcernCategory(String name, IconType icon) {
        this.name = name;
        this.icon = icon;
    }

    public String getName()   { return name; }
    public IconType getIcon() { return icon; }
}

enum BusinessType {
    FOOD("Food"), PHARMACY("Pharmacy"), RETAIL("Retail"), HARDWARE("Hardware");

    private final String label;
    BusinessType(String label) { this.label = label; }
    public String getLabel() { return label; }
}

class Business {
    private final String name;
    private final BusinessType type;
    private final String category;
    private final boolean open;
    private final String address;
    private final double rating;
    private final double latitude;
    private final double longitude;

    Business(String name, BusinessType type, String category, boolean open, String address, double rating,
             double latitude, double longitude) {
        this.name = name;
        this.type = type;
        this.category = category;
        this.open = open;
        this.address = address;
        this.rating = rating;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getName()         { return name; }
    public BusinessType getType()   { return type; }
    public String getCategory()     { return category; }
    public boolean isOpen()         { return open; }
    public String getAddress()      { return address; }
    public double getRating()       { return rating; }
    public double getLatitude()     { return latitude; }
    public double getLongitude()    { return longitude; }

    public boolean matches(String query, BusinessType filter) {
        if (filter != null && filter != type) return false;
        String q = query == null ? "" : query.trim().toLowerCase();
        if (q.isEmpty()) return true;
        return name.toLowerCase().contains(q) || category.toLowerCase().contains(q)
                || address.toLowerCase().contains(q) || type.getLabel().toLowerCase().contains(q);
    }
}

enum HotlineStyle {
    FEATURED(new Color(0, 0, 0, 0), Color.WHITE, new Color(0xF3, 0xB8, 0xB8), Color.WHITE,
             new Color(0xA3, 0x2B, 0x2B), new Color(0xFF, 0xE4, 0xE4), Color.WHITE, Theme.RED),
    STANDARD(Color.WHITE, Theme.TEXT, Theme.TEXT_SUB, Theme.RED, Theme.RED_SOFT, Theme.RED, Theme.RED, Color.WHITE);

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

class Official {
    private final String name;
    private final String position;

    Official(String name, String position) {
        this.name = name;
        this.position = position;
    }

    public String getName()     { return name; }
    public String getPosition() { return position; }
}

class PortalService {
    private final String title;
    private final String subtitle;
    private final IconType icon;
    private final Color soft;
    private final Color accent;
    private final String target;

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

class TransportService {
    private final String title;
    private final String description;
    private final IconType icon;

    TransportService(String title, String description, IconType icon) {
        this.title = title;
        this.description = description;
        this.icon = icon;
    }

    public String getTitle()       { return title; }
    public String getDescription() { return description; }
    public IconType getIcon()      { return icon; }
}

enum ReportStatus {
    PENDING("PENDING", Theme.AMBER_SOFT, Theme.AMBER);

    private final String label;
    private final Color bg;
    private final Color fg;

    ReportStatus(String label, Color bg, Color fg) {
        this.label = label;
        this.bg = bg;
        this.fg = fg;
    }

    public String getLabel() { return label; }
    public Color getBg()     { return bg; }
    public Color getFg()     { return fg; }
}

class ReportDraft {
    private final String category;
    private final String location;
    private final String details;
    private final File photo;

    ReportDraft(String category, String location, String details, File photo) {
        this.category = category;
        this.location = location;
        this.details = details;
        this.photo = photo;
    }

    public String getCategory() { return category; }
    public String getLocation() { return location; }
    public String getDetails()  { return details; }
    public File getPhoto()      { return photo; }
}

class ActiveReport {
    private final String title;
    private final String reference;
    private final ReportStatus status;
    private final IconType icon;
    private final ReportDraft draft;

    ActiveReport(String title, String reference, ReportStatus status, IconType icon, ReportDraft draft) {
        this.title = title;
        this.reference = reference;
        this.status = status;
        this.icon = icon;
        this.draft = draft;
    }

    public String getTitle()          { return title; }
    public String getReference()      { return reference; }
    public ReportStatus getStatus()   { return status; }
    public IconType getIcon()         { return icon; }
    public ReportDraft getDraft()     { return draft; }
}

class DataRepository {
    static List<DocumentRequest> getDocuments() {
        List<DocumentRequest> list = new ArrayList<>();
        list.add(new DocumentRequest("Barangay Clearance", "1 Valid ID, Cedula", "1 Working Day", new PaidFee("\u20B150.00")));
        list.add(new DocumentRequest("Certificate of Residency", "Voter's ID or Proof of Billing", "Same Day", new FreeFee()));
        list.add(new DocumentRequest("Certificate of Indigency", "Affidavit / Case Study", "Same Day", new FreeFee()));
        list.add(new DocumentRequest("Barangay Business Clearance", "DTI/SEC Reg, Mayor's Permit App", "2 Working Days", new PaidFee("\u20B1500.00")));
        return list;
    }

    /** NOTE: the business coordinates below are SAMPLE values near the barangay */
    static List<Business> getBusinesses() {
        List<Business> list = new ArrayList<>();
        list.add(new Business("Signal Central General Pharmacy", BusinessType.PHARMACY, "Pharmacy", true,
                "Ballecer St., Taguig City", 4.8, 14.5124, 121.0571));
        list.add(new Business("Kabayan Puregold Mini Grocery", BusinessType.RETAIL, "Retail Store", true,
                "Purok 2, Taguig City", 4.3, 14.5109, 121.0582));
        list.add(new Business("Ronald's Hardware & Construction Supply", BusinessType.HARDWARE, "Hardware Store", true,
                "M.L. Quezon St., Taguig City", 4.6, 14.5115, 121.0563));
        list.add(new Business("Mang Larry's BBQ House", BusinessType.FOOD, "Food & Dining", true,
                "F. Manalo St., Taguig City", 4.2, 14.5131, 121.0578));
        list.add(new Business("Sunrise Drugstore", BusinessType.PHARMACY, "Pharmacy", true,
                "Purok 3, Taguig City", 4.5, 14.5120, 121.0575));
        list.add(new Business("Aling Maria's Sari-Sari Store", BusinessType.RETAIL, "Retail Store", true,
                "Purok 1, Taguig City", 4.0, 14.5112, 121.0568));
        list.add(new Business("Joe's Kitchenette", BusinessType.FOOD, "Food & Dining", true,
                "M.L. Quezon St., Taguig City", 4.7, 14.5119, 121.0570));
        list.add(new Business("ABC Hardware Supply", BusinessType.HARDWARE, "Hardware Store", true,
                "Ballecer St., Taguig City", 4.4, 14.5126, 121.0565));
        return list;
    }

    static List<ConcernCategory> getConcernCategories() {
        List<ConcernCategory> list = new ArrayList<>();
        list.add(new ConcernCategory("Road & Street Issues", IconType.ROAD));
        list.add(new ConcernCategory("Waste Management", IconType.TRASH));
        list.add(new ConcernCategory("Street Lighting", IconType.LIGHT));
        list.add(new ConcernCategory("Flooding & Drainage", IconType.WATER));
        list.add(new ConcernCategory("Peace & Order", IconType.SHIELD));
        list.add(new ConcernCategory("Health & Sanitation", IconType.HEALTH));
        return list;
    }

    static List<Official> getOfficials() {
        List<Official> list = new ArrayList<>();
        list.add(new Official("Hon. Juan dela Cruz", "Barangay Captain"));
        list.add(new Official("Hon. Maria Santos", "Barangay Kagawad"));
        list.add(new Official("Hon. Pedro Reyes", "Barangay Kagawad"));
        list.add(new Official("Hon. Ana Gonzales", "Barangay Kagawad"));
        list.add(new Official("Hon. Jose Mercado", "Barangay Kagawad"));
        list.add(new Official("Hon. Luzviminda Bautista", "Barangay Kagawad"));
        list.add(new Official("Hon. Ricardo Villanueva", "Barangay Kagawad"));
        list.add(new Official("Hon. Carmen Lopez", "Barangay Kagawad"));
        return list;
    }

    static List<PortalService> getPortalServices() {
        List<PortalService> list = new ArrayList<>();
        list.add(new PortalService("E-Services", "Request Documents", IconType.DOCUMENT,
                Theme.RED_SOFT, Theme.RED, "E-Services"));
        list.add(new PortalService("Reports", "Help Us Improve", IconType.REPORT,
                Theme.GREEN_SOFT, Theme.GREEN, "Reports"));
        list.add(new PortalService("Transport", "Utility Vehicles", IconType.TRUCK,
                Theme.AMBER_SOFT, Theme.AMBER, "Transport"));
        list.add(new PortalService("Directory", "Local Businesses", IconType.SHOP,
                Theme.RED_SOFT, Theme.RED, "Directory"));
        list.add(new PortalService("Emergency", "Hotlines & SOS", IconType.SOS,
                new Color(0xDC, 0xFC, 0xE7), Theme.RED, "Emergency"));
        return list;
    }

    static List<Announcement> getAnnouncements() {
        List<Announcement> list = new ArrayList<>();
        list.add(new Announcement("EVENT", "2026-11-20", "Barangay Clean-Up Drive"));
        list.add(new Announcement("ADVISORY", "2026-11-25", "Scheduled Power Interruption"));
        list.add(new Announcement("NEWS", "2026-11-15", "New Barangay Ordinance Passed"));
        return list;
    }

    static List<TransportService> getTransportServices() {
        List<TransportService> list = new ArrayList<>();
        list.add(new TransportService("Utility Van", "For transporting goods or small groups (max 8 pax)", IconType.VAN));
        list.add(new TransportService("Ambulance (Non-Emergency)", "For scheduled medical appointments (non-urgent)", IconType.AMBULANCE));
        list.add(new TransportService("Pick-Up Truck", "For hauling construction materials, debris, etc.", IconType.PICKUP));
        return list;
    }

    static List<Hotline> getHotlines() {
        List<Hotline> list = new ArrayList<>();
        list.add(new Hotline("911", "Philippine National Police (PNP)", "National Emergency Hotline", HotlineStyle.FEATURED));
        list.add(new Hotline("Barangay Hall", "Central Signal Barangay", "Landline: 8123-4567", HotlineStyle.STANDARD));
        list.add(new Hotline("Taguig City PNP", "Taguig City Police Station", "Hotline: 8-7000", HotlineStyle.STANDARD));
        list.add(new Hotline("BFP Taguig", "Bureau of Fire Protection", "Fire & Rescue: 8931-1234", HotlineStyle.STANDARD));
        list.add(new Hotline("Taguig LGU", "City Government Hotline", "Trunkline: 8628-1111", HotlineStyle.STANDARD));
        return list;
    }
}

// ===================== FEE ABSTRACTION =====================
interface Fee {
    String getDescription();
    boolean isFree();
}

class PaidFee implements Fee {
    private final String description;

    PaidFee(String description) { this.description = description; }

    @Override
    public String getDescription() { return description; }

    @Override
    public boolean isFree() { return false; }
}

class FreeFee implements Fee {
    @Override
    public String getDescription() { return "Free"; }

    @Override
    public boolean isFree() { return true; }
}

// ===================== ICON SYSTEM =====================
enum IconType {
    DOCUMENT {
        @Override
        public void draw(Graphics2D g, int size, Color color) {
            g.setColor(color);
            int w = size * 3 / 4, h = size * 4 / 5;
            int x = (size - w) / 2, y = (size - h) / 2;
            g.fillRect(x, y + h / 4, w, h * 3 / 4);
            g.fillRect(x + w / 4, y, w / 2, h / 4);
            g.fillRect(x + w / 4, y + h / 2, w / 2, h / 6);
        }
    },
    REPORT {
        @Override
        public void draw(Graphics2D g, int size, Color color) {
            g.setColor(color);
            int cx = size / 2, cy = size / 2, r = size / 3;
            g.fillOval(cx - r, cy - r, 2 * r, 2 * r);
            g.fillRect(cx - r / 3, cy - r / 4, r * 2 / 3, r / 2);
        }
    },
    TRUCK {
        @Override
        public void draw(Graphics2D g, int size, Color color) {
            g.setColor(color);
            g.fillRect(size / 5, size / 3, size * 3 / 10, size / 3);
            g.fillRect(size / 2, size / 4, size * 2 / 5, size / 2);
            g.fillOval(size / 4, size * 2 / 3, size / 5, size / 5);
            g.fillOval(size / 2, size * 2 / 3, size / 5, size / 5);
        }
    },
    SHOP {
        @Override
        public void draw(Graphics2D g, int size, Color color) {
            g.setColor(color);
            g.fillRect(size / 4, size / 3, size / 2, size / 2);
            g.fillRect(size / 3, size / 6, size / 3, size / 4);
            g.setStroke(new BasicStroke(size / 20));
            g.drawLine(size / 4, size / 3, size / 4, size * 5 / 6);
            g.drawLine(size * 3 / 4, size / 3, size * 3 / 4, size * 5 / 6);
        }
    },
    SOS {
        @Override
        public void draw(Graphics2D g, int size, Color color) {
            g.setColor(color);
            Font f = new Font("Arial", Font.BOLD, size / 2);
            g.setFont(f);
            g.drawString("SOS", size / 6, size * 3 / 5);
        }
    },
    ROAD {
        @Override
        public void draw(Graphics2D g, int size, Color color) {
            g.setColor(color);
            g.fillRect(size / 3, size / 6, size / 3, size * 2 / 3);
            g.fillRect(size / 6, size / 3, size * 2 / 3, size / 3);
        }
    },
    TRASH {
        @Override
        public void draw(Graphics2D g, int size, Color color) {
            g.setColor(color);
            g.fillRect(size / 3, size / 4, size / 3, size / 2);
            g.fillRect(size / 2 - size / 8, size / 6, size / 4, size / 8);
        }
    },
    LIGHT {
        @Override
        public void draw(Graphics2D g, int size, Color color) {
            g.setColor(color);
            g.fillOval(size / 4, size / 6, size / 2, size / 2);
            g.fillRect(size / 2 - size / 12, size * 2 / 3, size / 6, size / 4);
        }
    },
    WATER {
        @Override
        public void draw(Graphics2D g, int size, Color color) {
            g.setColor(color);
            g.fillOval(size / 4, size / 4, size / 2, size / 3);
            g.fillOval(size / 3, size / 3, size / 3, size / 3);
        }
    },
    SHIELD {
        @Override
        public void draw(Graphics2D g, int size, Color color) {
            g.setColor(color);
            g.fillRect(size / 3, size / 4, size / 3, size / 2);
            g.fillOval(size / 4, size / 4, size / 2, size / 3);
        }
    },
    HEALTH {
        @Override
        public void draw(Graphics2D g, int size, Color color) {
            g.setColor(color);
            g.fillRect(size / 3, size / 4, size / 3, size / 2);
            g.fillRect(size / 4, size / 2 - size / 8, size / 2, size / 4);
        }
    },
    VAN {
        @Override
        public void draw(Graphics2D g, int size, Color color) {
            g.setColor(color);
            g.fillRect(size / 4, size / 3, size / 2, size / 3);
            g.fillRect(size / 2, size / 4, size / 3, size / 4);
            g.fillOval(size / 4 + size / 10, size * 2 / 3, size / 6, size / 6);
            g.fillOval(size / 2 + size / 10, size * 2 / 3, size / 6, size / 6);
        }
    },
    AMBULANCE {
        @Override
        public void draw(Graphics2D g, int size, Color color) {
            g.setColor(color);
            g.fillRect(size / 4, size / 3, size / 2, size / 3);
            g.fillRect(size / 2, size / 4, size / 3, size / 4);
            g.fillOval(size / 4 + size / 10, size * 2 / 3, size / 6, size / 6);
            g.fillOval(size / 2 + size / 10, size * 2 / 3, size / 6, size / 6);
            g.fillRect(size / 2 + size / 12, size / 3 - size / 12, size / 8, size / 6);
        }
    },
    PICKUP {
        @Override
        public void draw(Graphics2D g, int size, Color color) {
            g.setColor(color);
            g.fillRect(size / 6, size / 3, size * 2 / 3, size / 3);
            g.fillRect(size / 2, size / 4, size / 3, size / 4);
            g.fillOval(size / 4, size * 2 / 3, size / 5, size / 5);
            g.fillOval(size * 3 / 4 - size / 10, size * 2 / 3, size / 5, size / 5);
        }
    },
    PHONE {
        @Override
        public void draw(Graphics2D g, int size, Color color) {
            g.setColor(color);
            g.fillOval(size / 3, size / 6, size / 3, size * 2 / 3);
            g.fillOval(size / 2 - size / 8, size / 4 - size / 8, size / 4, size / 4);
        }
    },
    ARROW_LEFT {
        @Override
        public void draw(Graphics2D g, int size, Color color) {
            g.setColor(color);
            g.fillPolygon(new int[]{size / 3, size * 2 / 3, size / 2},
                          new int[]{size / 3, size / 3, size * 2 / 3}, 3);
        }
    },
    WARNING {
        @Override
        public void draw(Graphics2D g, int size, Color color) {
            g.setColor(color);
            g.fillPolygon(new int[]{size / 2, size / 4, size * 3 / 4},
                          new int[]{size / 6, size * 5 / 6, size * 5 / 6}, 3);
            g.fillRect(size / 2 - size / 12, size / 3 - size / 12, size / 6, size / 6);
        }
    };

    public abstract void draw(Graphics2D g, int size, Color color);
}

// ===================== SHARED UI COMPONENTS =====================
class IconLabel extends JComponent {
    private final IconType type;
    private final Color color;

    IconLabel(IconType type, int size, Color color) {
        this.type = type;
        this.color = color;
        setPreferredSize(new Dimension(size, size));
        setMinimumSize(new Dimension(size, size));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        Theme.smooth(g2);
        type.draw(g2, getWidth(), color);
        g2.dispose();
    }
}

class RoundedPanel extends JPanel {
    private final Color bg;
    private final Color border;
    private final int radius;

    RoundedPanel(Color bg, Color border, int radius) {
        this.bg = bg;
        this.border = border;
        this.radius = radius;
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        Theme.smooth(g2);
        if (bg != null) {
            g2.setColor(bg);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
        }
        if (border != null) {
            g2.setColor(border);
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
        }
        g2.dispose();
    }

    @Override
    protected void paintBorder(Graphics g) {}
}

class Badge extends RoundedPanel {
    Badge(String text, Color bg, Color fg, int radius) {
        super(bg, null, radius);
        JLabel l = new JLabel(text);
        l.setFont(Theme.bold(8));
        l.setForeground(fg);
        setBorder(new EmptyBorder(2, 8, 2, 8));
        setLayout(new GridBagLayout());
        add(l);
    }
}

// ===================== ABSTRACTIONS & INFRASTRUCTURE =====================
interface NavListener {
    void onNavigate(String screen);
}

abstract class AppSection extends JPanel {
    protected abstract void buildUI();

    public final JPanel init() {
        buildUI();
        return this;
    }
}

abstract class ScrollableScreen extends AppSection {
    protected abstract String getHeaderTitle();
    protected abstract AppSection createContent();
    protected Runnable getBackAction() { return null; }
    protected boolean hasHeader() { return true; }
    protected Color getBackgroundColor() { return Theme.BG; }

    @Override
    protected final void buildUI() {
        setLayout(new BorderLayout());
        setBackground(getBackgroundColor());

        if (hasHeader()) {
            JPanel header = new JPanel(new BorderLayout(10, 0));
            header.setBackground(Color.WHITE);
            header.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER),
                    new EmptyBorder(8, 16, 10, 16)));

            JLabel title = new JLabel(getHeaderTitle());
            title.setFont(Theme.bold(14));
            title.setForeground(Theme.TEXT);
            header.add(title, BorderLayout.CENTER);

            Runnable backAction = getBackAction();
            if (backAction != null) {
                JButton backButton = new JButton("\u2039");
                backButton.setFont(Theme.bold(24));
                backButton.setForeground(Theme.TEXT);
                backButton.setFocusPainted(false);
                backButton.setContentAreaFilled(false);
                backButton.setBorderPainted(false);
                backButton.addActionListener(event -> backAction.run());
                header.add(backButton, BorderLayout.WEST);
            }
            add(header, BorderLayout.NORTH);
        }

        JScrollPane scroll = new JScrollPane(createContent().init());
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.getViewport().setBackground(getBackgroundColor());
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        add(scroll, BorderLayout.CENTER);
    }
}

class Navigator {
    private final CardLayout cards;
    private final JPanel center;
    private final List<NavListener> listeners = new ArrayList<>();

    Navigator(CardLayout cards, JPanel center) {
        this.cards = cards;
        this.center = center;
    }

    public void onNavigate(String screen) {
        cards.show(center, screen);
        for (NavListener l : listeners) l.onNavigate(screen);
    }

    public void attach(NavListener l) { listeners.add(l); }
    public void detach(NavListener l) { listeners.remove(l); }
}

class BottomNav extends JPanel implements NavListener {
    private final Navigator nav;
    private final List<JButton> buttons = new ArrayList<>();

    BottomNav(Navigator nav) {
        this.nav = nav;
        setLayout(new GridLayout(1, 5));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.BORDER));
    }

    public void init() {
        String[][] items = {
            {"Home", "🏠"}, {"E-Services", "📄"}, {"Reports", "📋"}, {"Guide", "📖"}, {"Profile", "👤"}
        };
        for (String[] item : items) {
            JButton b = new JButton(item[0]);
            b.setFont(Theme.regular(9));
            b.setForeground(Theme.MUTED);
            b.setFocusPainted(false);
            b.setBorderPainted(false);
            b.setContentAreaFilled(false);
            b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            b.putClientProperty("screen", item[1]); // store screen key
            b.addActionListener(e -> nav.onNavigate((String) b.getClientProperty("screen")));
            buttons.add(b);
            add(b);
        }
        nav.attach(this);
    }

    @Override
    public void onNavigate(String screen) {
        for (JButton b : buttons) {
            boolean selected = screen.equals(b.getClientProperty("screen"));
            b.setFont(selected ? Theme.bold(9) : Theme.regular(9));
            b.setForeground(selected ? Theme.RED : Theme.MUTED);
        }
    }
}

class StatusBar extends JPanel implements NavListener {
    private final JLabel label;

    StatusBar() {
        setLayout(new FlowLayout(FlowLayout.LEFT, 12, 4));
        setBackground(Theme.RED_DARK);
        label = new JLabel("Barangay Central Signal");
        label.setFont(Theme.bold(11));
        label.setForeground(Color.WHITE);
        add(label);
    }

    public void init() {}

    @Override
    public void onNavigate(String screen) {
        label.setText("Barangay Central Signal — " + screen);
    }
}