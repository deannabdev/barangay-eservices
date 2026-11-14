package barangay.eservices;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

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

    default Badge createTag() {
        Color background = isFree() ? Theme.GREEN_SOFT : Theme.RED_SOFT;
        Color foreground = isFree() ? Theme.GREEN : Theme.RED;
        return new Badge(getDescription(), background, foreground, 8);
    }
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
            drawVector(g, size, color, HomeComponents.VectorIcon.Type.DOCUMENT);
        }
    },
    REPORT {
        @Override
        public void draw(Graphics2D g, int size, Color color) {
            drawVector(g, size, color, HomeComponents.VectorIcon.Type.ALERT_CIRCLE);
        }
    },
    TRUCK {
        @Override
        public void draw(Graphics2D g, int size, Color color) {
            drawVector(g, size, color, HomeComponents.VectorIcon.Type.TRUCK);
        }
    },
    SHOP {
        @Override
        public void draw(Graphics2D g, int size, Color color) {
            drawVector(g, size, color, HomeComponents.VectorIcon.Type.BUILDING);
        }
    },
    SOS {
        @Override
        public void draw(Graphics2D g, int size, Color color) {
            drawVector(g, size, color, HomeComponents.VectorIcon.Type.ALERT_TRIANGLE);
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
            drawReportIcon(g, size, color, ReportsComponents.ReportIcon.Type.TRASH);
        }
    },
    LIGHT {
        @Override
        public void draw(Graphics2D g, int size, Color color) {
            drawReportIcon(g, size, color, ReportsComponents.ReportIcon.Type.STREETLIGHT_OFF);
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
            drawVector(g, size, color, HomeComponents.VectorIcon.Type.SHIELD);
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
            drawVector(g, size, color, HomeComponents.VectorIcon.Type.PHONE);
        }
    },
    ARROW_LEFT {
        @Override
        public void draw(Graphics2D g, int size, Color color) {
            drawTransportIcon(g, size, color, TransportComponents.TransportIcon.Type.ARROW_LEFT);
        }
    },
    WARNING {
        @Override
        public void draw(Graphics2D g, int size, Color color) {
            drawVector(g, size, color, HomeComponents.VectorIcon.Type.ALERT_TRIANGLE);
        }
    },
    MAGNIFIER {
        @Override
        public void draw(Graphics2D g, int size, Color color) {
            drawDirectoryIcon(g, size, color, DirectoryComponents.DirectoryIcon.Type.SEARCH);
        }
    },
    SEARCH {
        @Override
        public void draw(Graphics2D g, int size, Color color) {
            drawDirectoryIcon(g, size, color, DirectoryComponents.DirectoryIcon.Type.SEARCH);
        }
    },
    STORE {
        @Override
        public void draw(Graphics2D g, int size, Color color) {
            drawDirectoryIcon(g, size, color, DirectoryComponents.DirectoryIcon.Type.STORE);
        }
    },
    STAR {
        @Override
        public void draw(Graphics2D g, int size, Color color) {
            drawDirectoryIcon(g, size, color, DirectoryComponents.DirectoryIcon.Type.STAR);
        }
    },
    USERS {
        @Override
        public void draw(Graphics2D g, int size, Color color) {
            drawTransportIcon(g, size, color, TransportComponents.TransportIcon.Type.USERS);
        }
    },
    BOX {
        @Override
        public void draw(Graphics2D g, int size, Color color) {
            drawTransportIcon(g, size, color, TransportComponents.TransportIcon.Type.BOX);
        }
    },
    CIRCLE_X {
        @Override
        public void draw(Graphics2D g, int size, Color color) {
            drawTransportIcon(g, size, color, TransportComponents.TransportIcon.Type.X_CIRCLE);
        }
    },
    PLUS {
        @Override
        public void draw(Graphics2D g, int size, Color color) {
            drawReportIcon(g, size, color, ReportsComponents.ReportIcon.Type.PLUS);
        }
    },
    CHEVRON {
        @Override
        public void draw(Graphics2D g, int size, Color color) {
            drawVector(g, size, color, HomeComponents.VectorIcon.Type.CHEVRON);
        }
    },
    BULB {
        @Override
        public void draw(Graphics2D g, int size, Color color) {
            LIGHT.draw(g, size, color);
        }
    },
    TRIANGLE {
        @Override
        public void draw(Graphics2D g, int size, Color color) {
            drawVector(g, size, color, HomeComponents.VectorIcon.Type.ALERT_TRIANGLE);
        }
    },
    DOC {
        @Override
        public void draw(Graphics2D g, int size, Color color) {
            DOCUMENT.draw(g, size, color);
        }
    },
    GAVEL {
        @Override
        public void draw(Graphics2D g, int size, Color color) {
            drawReportIcon(g, size, color, ReportsComponents.ReportIcon.Type.GAVEL);
        }
    },
    PERSON {
        @Override
        public void draw(Graphics2D g, int size, Color color) {
            drawVector(g, size, color, HomeComponents.VectorIcon.Type.USER);
        }
    },
    INFO {
        @Override
        public void draw(Graphics2D g, int size, Color color) {
            g.setColor(color);
            g.setStroke(new BasicStroke(Math.max(1f, size / 10f)));
            g.drawOval(size / 6, size / 6, size * 2 / 3, size * 2 / 3);
            g.fillOval(size / 2 - size / 16, size / 3, size / 8, size / 8);
            g.drawLine(size / 2, size / 2, size / 2, size * 2 / 3);
        }
    },
    CLOCK {
        @Override
        public void draw(Graphics2D g, int size, Color color) {
            g.setColor(color);
            g.setStroke(new BasicStroke(Math.max(1f, size / 10f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.drawOval(size / 6, size / 6, size * 2 / 3, size * 2 / 3);
            g.drawLine(size / 2, size / 3, size / 2, size / 2);
            g.drawLine(size / 2, size / 2, size * 2 / 3, size * 3 / 5);
        }
    },
    NAV_HOME {
        @Override
        public void draw(Graphics2D g, int size, Color color) {
            drawVector(g, size, color, HomeComponents.VectorIcon.Type.HOME);
        }
    },
    NAV_DOCUMENT {
        @Override
        public void draw(Graphics2D g, int size, Color color) {
            drawVector(g, size, color, HomeComponents.VectorIcon.Type.DOCUMENT);
        }
    },
    NAV_REPORT {
        @Override
        public void draw(Graphics2D g, int size, Color color) {
            drawVector(g, size, color, HomeComponents.VectorIcon.Type.ALERT_CIRCLE);
        }
    },
    NAV_GUIDE {
        @Override
        public void draw(Graphics2D g, int size, Color color) {
            drawVector(g, size, color, HomeComponents.VectorIcon.Type.BOOK);
        }
    },
    NAV_PROFILE {
        @Override
        public void draw(Graphics2D g, int size, Color color) {
            drawVector(g, size, color, HomeComponents.VectorIcon.Type.USER);
        }
    };

    public abstract void draw(Graphics2D g, int size, Color color);

    private static void drawVector(Graphics2D g, int size, Color color, HomeComponents.VectorIcon.Type type) {
        new HomeComponents.VectorIcon(type, size, color).paintIcon(null, g, 0, 0);
    }

    private static void drawReportIcon(Graphics2D g, int size, Color color,
                                       ReportsComponents.ReportIcon.Type type) {
        new ReportsComponents.ReportIcon(type, size, color).paintIcon(null, g, 0, 0);
    }

    private static void drawTransportIcon(Graphics2D g, int size, Color color,
                                          TransportComponents.TransportIcon.Type type) {
        new TransportComponents.TransportIcon(type, size, color).paintIcon(null, g, 0, 0);
    }

    private static void drawDirectoryIcon(Graphics2D g, int size, Color color,
                                          DirectoryComponents.DirectoryIcon.Type type) {
        new DirectoryComponents.DirectoryIcon(type, size, color).paintIcon(null, g, 0, 0);
    }

    public void draw(Graphics2D g, int x, int y, int size) {
        Graphics2D icon = (Graphics2D) g.create(x, y, size, size);
        draw(icon, size, g.getColor());
        icon.dispose();
    }

}

// ===================== SHARED UI COMPONENTS =====================
class BackButton extends JButton {
    BackButton(int size, Runnable action) {
        setPreferredSize(new Dimension(size, size));
        setMinimumSize(new Dimension(size, size));
        setMaximumSize(new Dimension(size, size));
        getAccessibleContext().setAccessibleName("Back");
        setToolTipText("Back");
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addActionListener(event -> action.run());
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        Theme.smooth(g2);
        int inset = 2;
        g2.setColor(new Color(255, 255, 255, getModel().isRollover() ? 48 : 28));
        g2.fillOval(inset, inset, getWidth() - inset * 2, getHeight() - inset * 2);
        g2.setColor(new Color(255, 255, 255, 180));
        g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        int centerX = getWidth() / 2, centerY = getHeight() / 2;
        g2.drawLine(centerX + 6, centerY, centerX - 6, centerY);
        g2.drawLine(centerX - 6, centerY, centerX, centerY - 6);
        g2.drawLine(centerX - 6, centerY, centerX, centerY + 6);
        g2.dispose();
    }
}

class Divider extends JComponent {
    Divider() {
        setOpaque(false);
        setPreferredSize(new Dimension(1, 1));
        setMinimumSize(new Dimension(1, 1));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
    }

    @Override
    protected void paintComponent(Graphics g) {
        g.setColor(Theme.BORDER);
        g.drawLine(0, 0, getWidth(), 0);
    }
}

class ShadowLabel extends JLabel {
    private final Color shadowColor;

    ShadowLabel(String text, Color shadowColor) {
        super(text);
        this.shadowColor = shadowColor;
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D shadow = (Graphics2D) g.create();
        Theme.smooth(shadow);
        shadow.setFont(getFont());
        shadow.setColor(shadowColor);
        FontMetrics metrics = shadow.getFontMetrics();
        Insets insets = getInsets();
        int x = switch (getHorizontalAlignment()) {
            case CENTER -> (getWidth() - metrics.stringWidth(getText())) / 2;
            case RIGHT, TRAILING -> getWidth() - insets.right - metrics.stringWidth(getText());
            default -> insets.left;
        };
        int y = switch (getVerticalAlignment()) {
            case TOP -> insets.top + metrics.getAscent();
            case BOTTOM -> getHeight() - insets.bottom - metrics.getDescent();
            default -> (getHeight() - metrics.getHeight()) / 2 + metrics.getAscent();
        };
        shadow.drawString(getText(), x + 2, y + 2);
        shadow.dispose();
        super.paintComponent(g);
    }
}

class RoundedButton extends JButton {
    private final Color backgroundColor;

    RoundedButton(String text) {
        this(text, Theme.RED, Color.WHITE);
    }

    RoundedButton(String text, Color backgroundColor, Color foregroundColor) {
        super(text);
        this.backgroundColor = backgroundColor;
        setForeground(foregroundColor);
        setFont(Theme.bold(12));
        setMargin(new Insets(7, 14, 7, 14));
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);
        setRolloverEnabled(true);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        Theme.smooth(g2);
        ButtonModel model = getModel();
        Color fill = backgroundColor;
        if (!isEnabled()) {
            fill = new Color(fill.getRed(), fill.getGreen(), fill.getBlue(), 120);
        } else if (model.isPressed()) {
            fill = fill.darker();
        } else if (model.isRollover()) {
            fill = fill.brighter();
        }
        g2.setColor(fill);
        g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
        g2.dispose();
        super.paintComponent(g);
    }
}

class IconLabel extends JComponent {
    private final IconType type;
    private Color color;

    IconLabel(IconType type, int size, Color color) {
        this.type = type;
        this.color = color;
        setPreferredSize(new Dimension(size, size));
        setMinimumSize(new Dimension(size, size));
    }

    void setColor(Color color) {
        this.color = color;
        repaint();
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
        this(text, bg, fg, null, radius);
    }

    Badge(String text, Color bg, Color fg, IconType icon, int radius) {
        super(bg, null, radius);
        setLayout(new FlowLayout(FlowLayout.LEFT, icon == null ? 0 : 4, 0));
        if (icon != null) add(new IconLabel(icon, 12, fg));
        JLabel l = new JLabel(text);
        l.setFont(Theme.bold(8));
        l.setForeground(fg);
        setBorder(new EmptyBorder(2, 8, 2, 8));
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
    protected JComponent getFixedHeader() { return null; }
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

        JComponent fixedHeader = getFixedHeader();
        if (fixedHeader != null) {
            add(fixedHeader, BorderLayout.NORTH);
        }

        JScrollPane scroll = new JScrollPane(createContent().init());
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.getViewport().setBackground(getBackgroundColor());
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        add(scroll, BorderLayout.CENTER);
    }
}

class Navigator implements NavListener {
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
    private final List<JLabel> labels = new ArrayList<>();
    private final List<IconLabel> icons = new ArrayList<>();

    BottomNav(Navigator nav) {
        this.nav = nav;
        setLayout(new GridLayout(1, 5));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.BORDER));
    }

    public void init() {
        Object[][] items = {
            {"Home", "Home", IconType.NAV_HOME},
            {"E-Services", "E-Services", IconType.NAV_DOCUMENT},
            {"Reports", "Reports", IconType.NAV_REPORT},
            {"Guide", "Guide", IconType.NAV_GUIDE},
            {"Profile", "Profile", IconType.NAV_PROFILE}
        };
        for (Object[] item : items) {
            String labelText = (String) item[0];
            String screen = (String) item[1];
            IconLabel icon = new IconLabel((IconType) item[2], 22, Theme.MUTED);
            JLabel label = new JLabel(labelText, SwingConstants.CENTER);
            label.setFont(Theme.regular(9));
            label.setForeground(Theme.MUTED);

            JButton b = new JButton();
            b.setLayout(new BoxLayout(b, BoxLayout.Y_AXIS));
            b.add(Box.createVerticalGlue());
            icon.setAlignmentX(Component.CENTER_ALIGNMENT);
            label.setAlignmentX(Component.CENTER_ALIGNMENT);
            b.add(icon);
            b.add(Box.createVerticalStrut(3));
            b.add(label);
            b.add(Box.createVerticalGlue());
            b.setPreferredSize(new Dimension(64, 58));
            b.setToolTipText(labelText);
            b.getAccessibleContext().setAccessibleName(labelText);
            b.setFocusPainted(false);
            b.setBorderPainted(false);
            b.setContentAreaFilled(false);
            b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            b.putClientProperty("screen", screen);
            b.addActionListener(e -> nav.onNavigate((String) b.getClientProperty("screen")));
            buttons.add(b);
            labels.add(label);
            icons.add(icon);
            add(b);
        }
        nav.attach(this);
    }

    @Override
    public void onNavigate(String screen) {
        for (int i = 0; i < buttons.size(); i++) {
            JButton b = buttons.get(i);
            boolean selected = screen.equals(b.getClientProperty("screen"));
            Color color = selected ? Theme.RED : Theme.MUTED;
            labels.get(i).setFont(selected ? Theme.bold(9) : Theme.regular(9));
            labels.get(i).setForeground(color);
            icons.get(i).setColor(color);
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

/**
 * Reusable UI pieces for the Barangay Central Signal Hub home screen.
 * Icons are drawn with Graphics2D, so no image files are needed.
 */
final class HomeComponents {

    private HomeComponents() {}

    // ---------------------------------------------------------------- palette
    public static final Color PRIMARY        = new Color(0xD32F2F);
    public static final Color PRIMARY_DARK   = new Color(0xC62828);
    public static final Color PRIMARY_SOFT   = new Color(0xFDECEC);
    public static final Color PRIMARY_BORDER = new Color(0xF5A3A3);
    public static final Color BACKGROUND     = new Color(0xF8F9FB);
    public static final Color CARD           = Color.WHITE;
    public static final Color CARD_BORDER    = new Color(0xE5E7EB);
    public static final Color CARD_HOVER     = new Color(0xF3F4F6);
    public static final Color TEXT_DARK      = new Color(0x111827);
    public static final Color TEXT_BODY      = new Color(0x4B5563);
    public static final Color TEXT_MUTED     = new Color(0x4B5563);

    /** Screen size (the window's content area). */
    public static final int W = 351;
    public static final int H = 775;

    /** Width inside the Home screen's 16 px gutters and vertical scroll bar. */
    public static final int CONTENT_WIDTH = W - 48;

    public static Font font(int style, int size) {
        return new Font("Segoe UI", style, size);
    }

    // ---------------------------------------------------------------- helpers

    /** Left-aligns a component and lets it stretch horizontally inside a vertical BoxLayout. */
    public static <T extends JComponent> T fullWidth(T c) {
        c.setAlignmentX(Component.LEFT_ALIGNMENT);
        Dimension d = c.getPreferredSize();
        c.setMaximumSize(new Dimension(Integer.MAX_VALUE, d.height));
        return c;
    }

    public static void makeClickable(JComponent c, Runnable action) {
        c.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        c.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (action != null) action.run();
            }
        });
    }

    private static void antialias(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    }

    // ------------------------------------------------------------ vector icons

    /** Simple line icons drawn on a 24x24 grid and scaled to the requested size. */
    public static class VectorIcon implements Icon {

        public enum Type {
            SHIELD, TRUCK, DOCUMENT, ALERT_CIRCLE, ALERT_TRIANGLE,
            PHONE, BOOK, BUILDING, HOME, USER, CHEVRON
        }

        private final Type type;
        private final int size;
        private Color color;

        public VectorIcon(Type type, int size, Color color) {
            this.type = type;
            this.size = size;
            this.color = color;
        }

        public void setColor(Color color) { this.color = color; }

        @Override public int getIconWidth()  { return size; }
        @Override public int getIconHeight() { return size; }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            antialias(g2);
            g2.translate(x, y);
            double s = size / 24.0;
            g2.scale(s, s);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            switch (type) {
                case SHIELD: {
                    Path2D p = new Path2D.Double();
                    p.moveTo(12, 3);
                    p.lineTo(19, 6);
                    p.lineTo(19, 12);
                    p.curveTo(19, 16.5, 15.5, 19.5, 12, 21);
                    p.curveTo(8.5, 19.5, 5, 16.5, 5, 12);
                    p.lineTo(5, 6);
                    p.closePath();
                    g2.draw(p);
                    break;
                }
                case TRUCK: {
                    g2.draw(new RoundRectangle2D.Double(2, 6, 12, 10, 2, 2));
                    Path2D cab = new Path2D.Double();
                    cab.moveTo(14, 9.5);
                    cab.lineTo(18, 9.5);
                    cab.lineTo(21.5, 13);
                    cab.lineTo(21.5, 16);
                    cab.lineTo(14, 16);
                    g2.draw(cab);
                    g2.fill(new Ellipse2D.Double(5, 14.5, 4.5, 4.5));
                    g2.fill(new Ellipse2D.Double(15.5, 14.5, 4.5, 4.5));
                    break;
                }
                case DOCUMENT: {
                    Path2D p = new Path2D.Double();
                    p.moveTo(6, 3);
                    p.lineTo(14, 3);
                    p.lineTo(19, 8);
                    p.lineTo(19, 21);
                    p.lineTo(6, 21);
                    p.closePath();
                    g2.draw(p);
                    Path2D fold = new Path2D.Double();
                    fold.moveTo(14, 3);
                    fold.lineTo(14, 8);
                    fold.lineTo(19, 8);
                    g2.draw(fold);
                    g2.draw(new Line2D.Double(9, 13, 16, 13));
                    g2.draw(new Line2D.Double(9, 17, 16, 17));
                    break;
                }
                case ALERT_CIRCLE: {
                    g2.draw(new Ellipse2D.Double(3, 3, 18, 18));
                    g2.draw(new Line2D.Double(12, 7.5, 12, 13));
                    g2.fill(new Ellipse2D.Double(11, 15.2, 2, 2));
                    break;
                }
                case ALERT_TRIANGLE: {
                    Path2D p = new Path2D.Double();
                    p.moveTo(12, 3.5);
                    p.lineTo(22, 20.5);
                    p.lineTo(2, 20.5);
                    p.closePath();
                    g2.draw(p);
                    g2.draw(new Line2D.Double(12, 9.5, 12, 14));
                    g2.fill(new Ellipse2D.Double(11, 16, 2, 2));
                    break;
                }
                case PHONE: {
                    Path2D p = new Path2D.Double();
                    p.moveTo(5, 4);
                    p.lineTo(9, 4);
                    p.lineTo(11, 9);
                    p.lineTo(9, 11);
                    p.curveTo(10, 13, 11, 14, 13, 15);
                    p.lineTo(15, 13);
                    p.lineTo(20, 15);
                    p.lineTo(20, 19);
                    p.curveTo(20, 20, 19, 21, 18, 21);
                    p.curveTo(10, 21, 3, 14, 3, 6);
                    p.curveTo(3, 5, 4, 4, 5, 4);
                    p.closePath();
                    g2.draw(p);
                    break;
                }
                case BOOK: {
                    Path2D left = new Path2D.Double();
                    left.moveTo(12, 6);
                    left.curveTo(10, 4, 6, 4, 3, 5);
                    left.lineTo(3, 19);
                    left.curveTo(6, 18, 10, 18, 12, 20);
                    g2.draw(left);
                    Path2D right = new Path2D.Double();
                    right.moveTo(12, 6);
                    right.curveTo(14, 4, 18, 4, 21, 5);
                    right.lineTo(21, 19);
                    right.curveTo(18, 18, 14, 18, 12, 20);
                    g2.draw(right);
                    g2.draw(new Line2D.Double(12, 6, 12, 20));
                    break;
                }
                case BUILDING: {
                    g2.draw(new Rectangle2D.Double(5, 3, 14, 18));
                    g2.fill(new Rectangle2D.Double(8, 6, 2.5, 2.5));
                    g2.fill(new Rectangle2D.Double(13.5, 6, 2.5, 2.5));
                    g2.fill(new Rectangle2D.Double(8, 11, 2.5, 2.5));
                    g2.fill(new Rectangle2D.Double(13.5, 11, 2.5, 2.5));
                    g2.draw(new Rectangle2D.Double(10, 16, 4, 5));
                    break;
                }
                case HOME: {
                    Path2D roof = new Path2D.Double();
                    roof.moveTo(3, 11);
                    roof.lineTo(12, 3);
                    roof.lineTo(21, 11);
                    g2.draw(roof);
                    Path2D body = new Path2D.Double();
                    body.moveTo(5, 10);
                    body.lineTo(5, 21);
                    body.lineTo(19, 21);
                    body.lineTo(19, 10);
                    g2.draw(body);
                    Path2D door = new Path2D.Double();
                    door.moveTo(10, 21);
                    door.lineTo(10, 14);
                    door.lineTo(14, 14);
                    door.lineTo(14, 21);
                    g2.draw(door);
                    break;
                }
                case USER: {
                    g2.draw(new Ellipse2D.Double(3, 3, 18, 18));
                    g2.draw(new Ellipse2D.Double(9, 7, 6, 6));
                    g2.draw(new Arc2D.Double(6.5, 14, 11, 9, 0, 180, Arc2D.OPEN));
                    break;
                }
                case CHEVRON: {
                    Path2D p = new Path2D.Double();
                    p.moveTo(9, 5);
                    p.lineTo(16, 12);
                    p.lineTo(9, 19);
                    g2.draw(p);
                    break;
                }
            }

            g2.dispose();
        }
    }

    // --------------------------------------------------------- base components

    /** Panel with rounded corners, optional border and optional diagonal gradient. */
    public static class RoundedPanel extends JPanel {
        private final int arc;
        private Color fill;
        private final Color border;
        private Color gradientEnd;

        public RoundedPanel(int arc, Color fill, Color border) {
            this.arc = arc;
            this.fill = fill;
            this.border = border;
            setOpaque(false);
        }

        public void setFill(Color fill) {
            this.fill = fill;
            repaint();
        }

        public void setGradient(Color start, Color end) {
            this.fill = start;
            this.gradientEnd = end;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            antialias(g2);
            int w = getWidth(), h = getHeight();
            if (gradientEnd != null) {
                g2.setPaint(new GradientPaint(0, 0, fill, w, h, gradientEnd));
            } else {
                g2.setColor(fill);
            }
            g2.fill(new RoundRectangle2D.Double(0, 0, w - 1, h - 1, arc, arc));
            if (border != null) {
                g2.setColor(border);
                g2.setStroke(new BasicStroke(1f));
                g2.draw(new RoundRectangle2D.Double(0.5, 0.5, w - 2, h - 2, arc, arc));
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /** Fixed-size rounded square (or circle) with a centered icon. */
    public static class IconTile extends JComponent {
        private final int box;
        private final int arc;
        private final Color bg;
        private final Icon icon;

        public IconTile(int box, int arc, Color bg, Icon icon) {
            this.box = box;
            this.arc = arc;
            this.bg = bg;
            this.icon = icon;
            Dimension d = new Dimension(box, box);
            setPreferredSize(d);
            setMinimumSize(d);
            setMaximumSize(d);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            antialias(g2);
            int x = (getWidth() - box) / 2;
            int y = (getHeight() - box) / 2;
            g2.setColor(bg);
            g2.fill(new RoundRectangle2D.Double(x, y, box, box, arc, arc));
            icon.paintIcon(this,
                    g2,
                    x + (box - icon.getIconWidth()) / 2,
                    y + (box - icon.getIconHeight()) / 2);
            g2.dispose();
        }
    }

    /** Bold heading used above each section. */
    public static class SectionTitle extends JLabel {
        public SectionTitle(String text) {
            super(text);
            setFont(font(Font.BOLD, 16));
            setForeground(TEXT_DARK);
        }
    }

    /** Scroll-friendly panel that always matches the viewport width. */
    public static class ScrollablePanel extends JPanel implements Scrollable {
        @Override public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
        @Override public int getScrollableUnitIncrement(Rectangle r, int o, int d) { return 16; }
        @Override public int getScrollableBlockIncrement(Rectangle r, int o, int d) { return r.height; }
        @Override public boolean getScrollableTracksViewportWidth() { return true; }
        @Override public boolean getScrollableTracksViewportHeight() { return false; }
    }

    // ----------------------------------------------------------- screen parts

    /** Full-width top bar with the barangay name and location. */
    public static class HeaderBar extends JPanel {
        public HeaderBar(String title, String subtitle) {
            this(title, subtitle, null);
        }

        public HeaderBar(String title, String subtitle, Runnable onBack) {
            setLayout(new BorderLayout(10, 0));
            setBackground(Color.WHITE);
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, CARD_BORDER),
                    BorderFactory.createEmptyBorder(12, 16, 12, 16)));

            JComponent leading;
            if (onBack == null) {
                leading = new IconTile(40, 40, PRIMARY_SOFT,
                        new VectorIcon(VectorIcon.Type.SHIELD, 22, PRIMARY));
            } else {
                leading = new IconTile(40, 40, PRIMARY_SOFT,
                        new TransportComponents.TransportIcon(
                                TransportComponents.TransportIcon.Type.ARROW_LEFT, 20, PRIMARY));
                makeClickable(leading, onBack);
            }

            JLabel t = new JLabel(title);
            t.setFont(font(Font.BOLD, 15));
            t.setForeground(TEXT_DARK);
            JLabel s = new JLabel(subtitle);
            s.setFont(font(Font.PLAIN, 11));
            s.setForeground(TEXT_MUTED);

            JPanel text = new JPanel();
            text.setOpaque(false);
            text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
            text.add(Box.createVerticalGlue());
            text.add(t);
            text.add(s);
            text.add(Box.createVerticalGlue());

            add(leading, BorderLayout.WEST);
            add(text, BorderLayout.CENTER);
        }
    }

    /** Red welcome banner. */
    public static class HeroBanner extends RoundedPanel {
        public HeroBanner(String title, String message) {
            super(20, new Color(0x8B2323), null);
            setGradient(new Color(0x8B2323), new Color(0xB04545));
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setBorder(BorderFactory.createEmptyBorder(22, 18, 22, 18));

            JLabel t = new JLabel(title);
            t.setFont(font(Font.BOLD, 20));
            t.setForeground(Color.WHITE);
            t.setAlignmentX(LEFT_ALIGNMENT);

            int textWidth = CONTENT_WIDTH - 36 - 8;
            JLabel m = new JLabel("<html><body width='" + textWidth + "'>" + message + "</body></html>");
            m.setFont(font(Font.PLAIN, 13));
            m.setForeground(new Color(0xFBE4E4));
            m.setAlignmentX(LEFT_ALIGNMENT);

            add(t);
            add(Box.createVerticalStrut(8));
            add(m);
        }
    }

    /** Square card used in the Quick Access Portal grid. */
    public static class QuickAccessCard extends RoundedPanel {
        public QuickAccessCard(VectorIcon.Type iconType, Color tileBg, Color iconColor,
                               String title, String subtitle, Runnable onClick) {
            super(14, CARD, CARD_BORDER);
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setBorder(BorderFactory.createEmptyBorder(12, 8, 10, 2));
            setPreferredSize(new Dimension(100, 104));

            IconTile tile = new IconTile(40, 12, tileBg, new VectorIcon(iconType, 22, iconColor));
            tile.setAlignmentX(LEFT_ALIGNMENT);

            JLabel t = new JLabel(title);
            t.setFont(font(Font.BOLD, 10));
            t.setForeground(TEXT_DARK);
            t.setAlignmentX(LEFT_ALIGNMENT);

            JLabel s = new JLabel(subtitle);
            s.setFont(font(Font.PLAIN, 11));
            s.setForeground(TEXT_MUTED);
            s.setAlignmentX(LEFT_ALIGNMENT);

            add(tile);
            add(Box.createVerticalStrut(10));
            add(t);
            add(Box.createVerticalStrut(2));
            add(s);

            makeClickable(this, onClick);
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { setFill(CARD_HOVER); }
                @Override public void mouseExited(MouseEvent e)  { setFill(CARD); }
            });
        }
    }

    /** News / advisory card shown under "Barangay Announcements". */
    public static class AnnouncementCard extends RoundedPanel {
        public AnnouncementCard(String tag, String date, String title, Runnable onClick) {
            super(14, CARD, CARD_BORDER);
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

            RoundedPanel pill = new RoundedPanel(8, PRIMARY_SOFT, null);
            pill.setLayout(new FlowLayout(FlowLayout.LEFT, 8, 3));
            JLabel tagLabel = new JLabel(tag.toUpperCase());
            tagLabel.setFont(font(Font.BOLD, 10));
            tagLabel.setForeground(PRIMARY_DARK);
            pill.add(tagLabel);

            JPanel pillWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            pillWrap.setOpaque(false);
            pillWrap.add(pill);

            JLabel dateLabel = new JLabel(date);
            dateLabel.setFont(font(Font.PLAIN, 12));
            dateLabel.setForeground(TEXT_BODY);

            JPanel top = new JPanel(new BorderLayout());
            top.setOpaque(false);
            top.add(pillWrap, BorderLayout.WEST);
            top.add(dateLabel, BorderLayout.EAST);
            top.setAlignmentX(LEFT_ALIGNMENT);
            top.setMaximumSize(new Dimension(Integer.MAX_VALUE, top.getPreferredSize().height));

            JLabel titleLabel = new JLabel("<html><body width='" + (CONTENT_WIDTH - 32) + "'>"
                    + title + "</body></html>");
            titleLabel.setFont(font(Font.BOLD, 14));
            titleLabel.setForeground(TEXT_DARK);
            titleLabel.setAlignmentX(LEFT_ALIGNMENT);

            add(top);
            add(Box.createVerticalStrut(10));
            add(titleLabel);

            makeClickable(this, onClick);
        }
    }

    /** Bottom tab bar (Home, E-Services, Reports, Guide, Profile). */
    public static class BottomNavBar extends JPanel {

        private final NavItem[] items;

        public BottomNavBar(String selected, Consumer<String> onSelect) {
            setLayout(new GridLayout(1, 5));
            setBackground(Color.WHITE);
            setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, CARD_BORDER));

            items = new NavItem[] {
                new NavItem("Home",       VectorIcon.Type.HOME),
                new NavItem("E-Services", VectorIcon.Type.DOCUMENT),
                new NavItem("Reports",    VectorIcon.Type.ALERT_TRIANGLE),
                new NavItem("Guide",      VectorIcon.Type.BOOK),
                new NavItem("Profile",    VectorIcon.Type.USER)
            };

            for (NavItem item : items) {
                add(item);
                makeClickable(item, () -> {
                    setSelected(item.label);
                    if (onSelect != null) onSelect.accept(item.label);
                });
            }
            setSelected(selected);
        }

        public void setSelected(String label) {
            for (NavItem item : items) {
                item.setSelected(item.label.equals(label));
            }
        }

        private static class NavItem extends JPanel {
            final String label;
            private final VectorIcon icon;
            private final JLabel iconLabel;
            private final JLabel textLabel;

            NavItem(String label, VectorIcon.Type type) {
                this.label = label;
                setOpaque(false);
                setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
                setBorder(BorderFactory.createEmptyBorder(9, 0, 9, 0));

                icon = new VectorIcon(type, 22, TEXT_MUTED);
                iconLabel = new JLabel(icon);
                iconLabel.setAlignmentX(CENTER_ALIGNMENT);

                textLabel = new JLabel(label);
                textLabel.setFont(font(Font.PLAIN, 10));
                textLabel.setForeground(TEXT_MUTED);
                textLabel.setAlignmentX(CENTER_ALIGNMENT);

                add(iconLabel);
                add(Box.createVerticalStrut(3));
                add(textLabel);
            }

            void setSelected(boolean selected) {
                Color c = selected ? PRIMARY : TEXT_MUTED;
                icon.setColor(c);
                textLabel.setForeground(c);
                textLabel.setFont(font(selected ? Font.BOLD : Font.PLAIN, 10));
                repaint();
            }
        }
    }
}

final class ReportsComponents {

    private ReportsComponents() {}

    public static final Color ICON_CIRCLE       = new Color(0xFEF2F2);
    public static final Color STATUS_PENDING_BG = new Color(0xFEF3C7);
    public static final Color STATUS_PENDING_FG = new Color(0xD97706);

    // ------------------------------------------------------------------ icons

    /** Extra line icons (24x24 grid) needed by the Reports screen. */
    public static class ReportIcon implements Icon {

        public enum Type { PLUS, STREETLIGHT_OFF, TRASH, TREE, GAVEL }

        private final Type type;
        private final int size;
        private final Color color;

        public ReportIcon(Type type, int size, Color color) {
            this.type = type;
            this.size = size;
            this.color = color;
        }

        @Override public int getIconWidth()  { return size; }
        @Override public int getIconHeight() { return size; }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
            g2.translate(x, y);
            double s = size / 24.0;
            g2.scale(s, s);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            switch (type) {
                case PLUS: {
                    g2.draw(new Line2D.Double(12, 5, 12, 19));
                    g2.draw(new Line2D.Double(5, 12, 19, 12));
                    break;
                }
                case STREETLIGHT_OFF: {
                    Path2D lamp = new Path2D.Double();
                    lamp.moveTo(7, 9);
                    lamp.lineTo(17, 9);
                    lamp.lineTo(15, 5);
                    lamp.lineTo(9, 5);
                    lamp.closePath();
                    g2.draw(lamp);
                    g2.draw(new Line2D.Double(12, 9, 12, 20));
                    g2.draw(new Line2D.Double(9, 20, 15, 20));
                    g2.draw(new Line2D.Double(3.5, 3.5, 20.5, 20.5)); // "off" slash
                    break;
                }
                case TRASH: {
                    g2.draw(new Line2D.Double(4, 7, 20, 7));
                    Path2D handle = new Path2D.Double();
                    handle.moveTo(9, 7);
                    handle.lineTo(9, 4);
                    handle.lineTo(15, 4);
                    handle.lineTo(15, 7);
                    g2.draw(handle);
                    Path2D body = new Path2D.Double();
                    body.moveTo(6, 7);
                    body.lineTo(7, 20);
                    body.lineTo(17, 20);
                    body.lineTo(18, 7);
                    g2.draw(body);
                    g2.draw(new Line2D.Double(10, 11, 10, 16));
                    g2.draw(new Line2D.Double(14, 11, 14, 16));
                    break;
                }
                case TREE: {
                    Path2D p = new Path2D.Double();
                    p.moveTo(12, 3);
                    p.lineTo(18, 12);
                    p.lineTo(15.5, 12);
                    p.lineTo(19, 18);
                    p.lineTo(5, 18);
                    p.lineTo(8.5, 12);
                    p.lineTo(6, 12);
                    p.closePath();
                    g2.draw(p);
                    g2.draw(new Line2D.Double(12, 18, 12, 21));
                    break;
                }
                case GAVEL: {
                    AffineTransform old = g2.getTransform();
                    g2.translate(15.5, 8.5);
                    g2.rotate(Math.toRadians(45));
                    g2.draw(new RoundRectangle2D.Double(-5, -3, 10, 6, 2, 2));
                    g2.setTransform(old);
                    g2.draw(new Line2D.Double(13, 11, 4.5, 19.5));
                    break;
                }
            }

            g2.dispose();
        }
    }

    // ------------------------------------------------------------- components

    /** Page heading plus short description. */
    public static class IntroBlock extends JPanel {
        public IntroBlock(String title, String description) {
            setOpaque(false);
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

            JLabel t = new JLabel(title);
            t.setFont(HomeComponents.font(Font.BOLD, 18));
            t.setForeground(HomeComponents.TEXT_DARK);
            t.setAlignmentX(LEFT_ALIGNMENT);

            JLabel d = new JLabel("<html><div style='width:" + (HomeComponents.CONTENT_WIDTH - 4) + "px'>"
                    + description + "</div></html>");
            d.setFont(HomeComponents.font(Font.PLAIN, 12));
            d.setForeground(HomeComponents.TEXT_BODY);
            d.setAlignmentX(LEFT_ALIGNMENT);

            add(t);
            add(Box.createVerticalStrut(6));
            add(d);
        }
    }

    /** Big red call-to-action button: "Submit a New Report". */
    public static class SubmitReportCard extends HomeComponents.RoundedPanel {
        public SubmitReportCard(String title, String subtitle, Runnable onClick) {
            super(14, HomeComponents.PRIMARY, null);
            setGradient(new Color(0xD32F2F), new Color(0xB71C1C));
            setLayout(new BorderLayout(14, 0));
            setBorder(BorderFactory.createEmptyBorder(16, 14, 16, 16));

            HomeComponents.IconTile plus = new HomeComponents.IconTile(44, 44, new Color(0x8E1B1B),
                    new ReportIcon(ReportIcon.Type.PLUS, 22, Color.WHITE));

            JLabel t = new JLabel(title);
            t.setFont(HomeComponents.font(Font.BOLD, 14));
            t.setForeground(Color.WHITE);
            JLabel s = new JLabel(subtitle);
            s.setFont(HomeComponents.font(Font.PLAIN, 11));
            s.setForeground(new Color(0xFAD4D4));

            JPanel text = new JPanel();
            text.setOpaque(false);
            text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
            text.add(Box.createVerticalGlue());
            text.add(t);
            text.add(Box.createVerticalStrut(2));
            text.add(s);
            text.add(Box.createVerticalGlue());

            add(plus, BorderLayout.WEST);
            add(text, BorderLayout.CENTER);
            add(new JLabel(new HomeComponents.VectorIcon(HomeComponents.VectorIcon.Type.CHEVRON, 16, Color.WHITE)),
                    BorderLayout.EAST);

            HomeComponents.makeClickable(this, onClick);
        }
    }

    /** Concern category tile (icon in a soft circle with a centered label). */
    public static class CategoryCard extends HomeComponents.RoundedPanel {
        /** Use '\n' in the label to force a line break. */
        public CategoryCard(Icon icon, String label, Runnable onClick) {
            super(14, HomeComponents.CARD, HomeComponents.CARD_BORDER);
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setBorder(BorderFactory.createEmptyBorder(10, 4, 10, 4));
            setPreferredSize(new Dimension(100, 96));

            HomeComponents.IconTile tile = new HomeComponents.IconTile(40, 40, ICON_CIRCLE, icon);
            tile.setAlignmentX(CENTER_ALIGNMENT);

            String html = "<html><div style='text-align:center'>"
                    + label.replace("&", "&amp;").replace("\n", "<br>")
                    + "</div></html>";
            JLabel t = new JLabel(html, SwingConstants.CENTER);
            t.setFont(HomeComponents.font(Font.BOLD, 10));
            t.setForeground(HomeComponents.TEXT_DARK);
            t.setAlignmentX(CENTER_ALIGNMENT);

            add(Box.createVerticalGlue());
            add(tile);
            add(Box.createVerticalStrut(8));
            add(t);
            add(Box.createVerticalGlue());

            HomeComponents.makeClickable(this, onClick);
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { setFill(HomeComponents.CARD_HOVER); }
                @Override public void mouseExited(MouseEvent e)  { setFill(HomeComponents.CARD); }
            });
        }
    }

    /** Small colored status badge such as PENDING. */
    public static class StatusPill extends HomeComponents.RoundedPanel {
        public StatusPill(String text, Color fg, Color bg) {
            super(8, bg, null);
            setLayout(new FlowLayout(FlowLayout.CENTER, 8, 3));
            JLabel l = new JLabel(text.toUpperCase());
            l.setFont(HomeComponents.font(Font.BOLD, 9));
            l.setForeground(fg);
            add(l);
        }
    }

    /** One row under "Your Active Reports". */
    public static class ActiveReportCard extends HomeComponents.RoundedPanel {
        public ActiveReportCard(Icon icon, String title, String reference,
                                String status, Color statusFg, Color statusBg, Runnable onClick) {
            super(12, HomeComponents.CARD, HomeComponents.CARD_BORDER);
            setLayout(new BorderLayout(8, 0));
            setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 10));

            JLabel t = new JLabel(title);
            t.setFont(HomeComponents.font(Font.BOLD, 12));
            t.setForeground(HomeComponents.TEXT_DARK);
            JLabel r = new JLabel(reference);
            r.setFont(HomeComponents.font(Font.PLAIN, 10));
            r.setForeground(HomeComponents.TEXT_MUTED);

            JPanel text = new JPanel();
            text.setOpaque(false);
            text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
            text.add(t);
            text.add(Box.createVerticalStrut(2));
            text.add(r);

            JPanel pillWrap = new JPanel(new GridBagLayout());
            pillWrap.setOpaque(false);
            pillWrap.add(new StatusPill(status, statusFg, statusBg));

            add(new JLabel(icon), BorderLayout.WEST);
            add(text, BorderLayout.CENTER);
            add(pillWrap, BorderLayout.EAST);

            HomeComponents.makeClickable(this, onClick);
        }
    }
}

final class TransportComponents {

    private TransportComponents() {}

    public static class TransportIcon implements Icon {

        public enum Type { ARROW_LEFT, USERS, BOX, X_CIRCLE }

        private final Type type;
        private final int size;
        private final Color color;

        public TransportIcon(Type type, int size, Color color) {
            this.type = type;
            this.size = size;
            this.color = color;
        }

        @Override public int getIconWidth() { return size; }
        @Override public int getIconHeight() { return size; }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
            g2.translate(x, y);
            g2.scale(size / 24.0, size / 24.0);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            switch (type) {
                case ARROW_LEFT:
                    g2.draw(new Line2D.Double(19, 12, 5, 12));
                    Path2D head = new Path2D.Double();
                    head.moveTo(11, 6);
                    head.lineTo(5, 12);
                    head.lineTo(11, 18);
                    g2.draw(head);
                    break;
                case USERS:
                    g2.draw(new Ellipse2D.Double(5, 4.5, 6, 6));
                    g2.draw(new Arc2D.Double(3, 14, 10, 10, 0, 180, Arc2D.OPEN));
                    g2.draw(new Ellipse2D.Double(14.5, 6, 5, 5));
                    g2.draw(new Arc2D.Double(14, 14.5, 8, 8, 0, 180, Arc2D.OPEN));
                    break;
                case BOX:
                    Path2D hex = new Path2D.Double();
                    hex.moveTo(12, 3);
                    hex.lineTo(20, 7.5);
                    hex.lineTo(20, 16.5);
                    hex.lineTo(12, 21);
                    hex.lineTo(4, 16.5);
                    hex.lineTo(4, 7.5);
                    hex.closePath();
                    g2.draw(hex);
                    Path2D inner = new Path2D.Double();
                    inner.moveTo(4, 7.5);
                    inner.lineTo(12, 12);
                    inner.lineTo(20, 7.5);
                    g2.draw(inner);
                    g2.draw(new Line2D.Double(12, 12, 12, 21));
                    break;
                case X_CIRCLE:
                    g2.draw(new Ellipse2D.Double(3, 3, 18, 18));
                    g2.draw(new Line2D.Double(9, 9, 15, 15));
                    g2.draw(new Line2D.Double(15, 9, 9, 15));
                    break;
            }
            g2.dispose();
        }
    }

    public static class BackHeader extends JPanel {
        public BackHeader(String title, String subtitle, Runnable onBack, Runnable onSos) {
            setLayout(new BorderLayout(10, 0));
            setBackground(Color.WHITE);
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, HomeComponents.CARD_BORDER),
                    BorderFactory.createEmptyBorder(12, 16, 12, 16)));

            HomeComponents.IconTile back = new HomeComponents.IconTile(36, 36, HomeComponents.PRIMARY_SOFT,
                    new TransportIcon(TransportIcon.Type.ARROW_LEFT, 18, HomeComponents.PRIMARY));
            HomeComponents.makeClickable(back, onBack);

            JLabel titleLabel = new JLabel(title);
            titleLabel.setFont(HomeComponents.font(Font.BOLD, 15));
            titleLabel.setForeground(HomeComponents.TEXT_DARK);
            JLabel subtitleLabel = new JLabel(subtitle);
            subtitleLabel.setFont(HomeComponents.font(Font.PLAIN, 11));
            subtitleLabel.setForeground(HomeComponents.TEXT_MUTED);

            JPanel text = new JPanel();
            text.setOpaque(false);
            text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
            text.add(Box.createVerticalGlue());
            text.add(titleLabel);
            text.add(subtitleLabel);
            text.add(Box.createVerticalGlue());

            HomeComponents.RoundedPanel sos = new HomeComponents.RoundedPanel(
                    999, HomeComponents.PRIMARY_SOFT, HomeComponents.PRIMARY_BORDER);
            sos.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
            sos.add(new JLabel(new HomeComponents.VectorIcon(
                    HomeComponents.VectorIcon.Type.TRUCK, 16, HomeComponents.PRIMARY)));
            JLabel sosText = new JLabel("SOS");
            sosText.setFont(HomeComponents.font(Font.BOLD, 12));
            sosText.setForeground(HomeComponents.PRIMARY);
            sos.add(sosText);
            HomeComponents.makeClickable(sos, onSos);

            JPanel sosWrap = new JPanel(new GridBagLayout());
            sosWrap.setOpaque(false);
            sosWrap.add(sos);
            add(back, BorderLayout.WEST);
            add(text, BorderLayout.CENTER);
            add(sosWrap, BorderLayout.EAST);
        }
    }

    public static class NoticeBanner extends HomeComponents.RoundedPanel {
        public NoticeBanner(String lead, String message) {
            super(12, new Color(0xFEF2F2), HomeComponents.PRIMARY_BORDER);
            setLayout(new BorderLayout(10, 0));
            setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

            JLabel text = new JLabel();
            text.setFont(HomeComponents.font(Font.PLAIN, 11));
            text.setForeground(HomeComponents.TEXT_BODY);
            add(new JLabel(new HomeComponents.VectorIcon(
                    HomeComponents.VectorIcon.Type.ALERT_TRIANGLE, 18, HomeComponents.PRIMARY_DARK)),
                    BorderLayout.WEST);
            add(text, BorderLayout.CENTER);

            Runnable updateTextWidth = () -> {
                int textWidth = getWidth() > 0
                        ? Math.max(100, getWidth() - 72)
                        : HomeComponents.CONTENT_WIDTH - 72;
                text.setText("<html><body width='" + textWidth + "'>"
                        + "<font color='#C62828'><b>" + escapeHtml(lead) + "</b></font> "
                        + escapeHtml(message) + "</body></html>");
                revalidate();
            };
            updateTextWidth.run();
            addComponentListener(new ComponentAdapter() {
                @Override public void componentResized(ComponentEvent e) { updateTextWidth.run(); }
            });
        }
    }

    public static class ServiceCard extends HomeComponents.RoundedPanel {
        public ServiceCard(Icon icon, String title, String description, String action, Runnable onClick) {
            super(14, HomeComponents.CARD, HomeComponents.CARD_BORDER);
            setLayout(new BorderLayout(12, 0));
            setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 12));

            HomeComponents.IconTile tile = new HomeComponents.IconTile(
                    44, 12, HomeComponents.PRIMARY_SOFT, icon);
            JLabel titleLabel = new JLabel(title);
            titleLabel.setFont(HomeComponents.font(Font.BOLD, 13));
            titleLabel.setForeground(HomeComponents.TEXT_DARK);
            titleLabel.setAlignmentX(LEFT_ALIGNMENT);
            JLabel descriptionLabel = new JLabel();
            descriptionLabel.setFont(HomeComponents.font(Font.PLAIN, 11));
            descriptionLabel.setForeground(HomeComponents.TEXT_MUTED);
            descriptionLabel.setAlignmentX(LEFT_ALIGNMENT);
            RoundedButton actionButton = new RoundedButton(
                    action + " \u2192", HomeComponents.PRIMARY, Color.WHITE);
            actionButton.setFont(HomeComponents.font(Font.BOLD, 10));
            actionButton.setMargin(new Insets(5, 10, 5, 10));
            actionButton.setAlignmentX(LEFT_ALIGNMENT);
            actionButton.addActionListener(e -> {
                if (onClick != null) onClick.run();
            });

            JPanel text = new JPanel();
            text.setOpaque(false);
            text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
            text.add(Box.createVerticalGlue());
            text.add(titleLabel);
            text.add(Box.createVerticalStrut(3));
            text.add(descriptionLabel);
            text.add(Box.createVerticalStrut(8));
            text.add(actionButton);
            text.add(Box.createVerticalGlue());

            add(tile, BorderLayout.WEST);
            add(text, BorderLayout.CENTER);
            Runnable updateTextWidth = () -> {
                int textWidth = getWidth() > 0
                        ? Math.max(100, getWidth() - 100)
                        : HomeComponents.CONTENT_WIDTH - 100;
                titleLabel.setText("<html><body width='" + textWidth + "'>"
                        + escapeHtml(title) + "</body></html>");
                descriptionLabel.setText("<html><body width='" + textWidth + "'>"
                        + escapeHtml(description) + "</body></html>");
                text.revalidate();
                revalidate();
            };
            updateTextWidth.run();
            addComponentListener(new ComponentAdapter() {
                @Override public void componentResized(ComponentEvent e) { updateTextWidth.run(); }
            });
            HomeComponents.makeClickable(this, onClick);
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { setFill(HomeComponents.CARD_HOVER); }
                @Override public void mouseExited(MouseEvent e) { setFill(HomeComponents.CARD); }
            });
        }
    }

    private static String escapeHtml(String text) {
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}

final class DirectoryComponents {

    private DirectoryComponents() {}

    public static final Color OPEN_GREEN = new Color(0x16A34A);
    public static final Color STAR_AMBER = new Color(0xF59E0B);

    // ------------------------------------------------------------------ icons

    /** Extra icons (24x24 grid) needed by the Directory screen. */
    public static class DirectoryIcon implements Icon {

        public enum Type { SEARCH, STORE, STAR, MAP_PIN }

        private final Type type;
        private final int size;
        private final Color color;

        public DirectoryIcon(Type type, int size, Color color) {
            this.type = type;
            this.size = size;
            this.color = color;
        }

        @Override public int getIconWidth()  { return size; }
        @Override public int getIconHeight() { return size; }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
            g2.translate(x, y);
            double s = size / 24.0;
            g2.scale(s, s);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            switch (type) {
                case SEARCH: {
                    g2.draw(new Ellipse2D.Double(3.5, 3.5, 12, 12));
                    g2.draw(new Line2D.Double(13.5, 13.5, 20.5, 20.5));
                    break;
                }
                case STORE: {
                    Path2D roof = new Path2D.Double();
                    roof.moveTo(4, 4);
                    roof.lineTo(20, 4);
                    roof.lineTo(21, 9);
                    roof.lineTo(3, 9);
                    roof.closePath();
                    g2.draw(roof);
                    g2.draw(new Line2D.Double(9, 4, 8, 9));
                    g2.draw(new Line2D.Double(15, 4, 16, 9));
                    Path2D body = new Path2D.Double();
                    body.moveTo(5, 9);
                    body.lineTo(5, 20);
                    body.lineTo(19, 20);
                    body.lineTo(19, 9);
                    g2.draw(body);
                    g2.draw(new Rectangle2D.Double(10, 14, 4, 6));
                    break;
                }
                case STAR: {
                    Path2D star = new Path2D.Double();
                    for (int i = 0; i < 10; i++) {
                        double r = (i % 2 == 0) ? 10 : 4.2;
                        double a = Math.toRadians(-90 + i * 36);
                        double px = 12 + r * Math.cos(a);
                        double py = 12.8 + r * Math.sin(a);
                        if (i == 0) star.moveTo(px, py); else star.lineTo(px, py);
                    }
                    star.closePath();
                    g2.fill(star);
                    g2.draw(star);
                    break;
                }
                case MAP_PIN: {
                    Path2D pin = new Path2D.Double();
                    pin.moveTo(12, 21);
                    pin.curveTo(10, 18, 5, 12.5, 5, 9);
                    pin.curveTo(5, 5, 8, 2.5, 12, 2.5);
                    pin.curveTo(16, 2.5, 19, 5, 19, 9);
                    pin.curveTo(19, 12.5, 14, 18, 12, 21);
                    pin.closePath();
                    g2.draw(pin);
                    g2.draw(new Ellipse2D.Double(10, 7, 4, 4));
                    break;
                }
            }
            g2.dispose();
        }
    }

    // ------------------------------------------------------------- components

    /** Text field that shows a grey hint while it is empty. */
    public static class HintTextField extends JTextField {
        private final String hint;

        public HintTextField(String hint) {
            this.hint = hint;
            setOpaque(false);
            setBorder(null);
            setFont(HomeComponents.font(Font.PLAIN, 12));
            setForeground(HomeComponents.TEXT_DARK);
            setCaretColor(HomeComponents.PRIMARY);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (getText().isEmpty()) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2.setColor(HomeComponents.TEXT_MUTED);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                Insets in = getInsets();
                g2.drawString(hint, in.left, (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
                g2.dispose();
            }
        }
    }

    /** Rounded search box with a magnifier icon; calls onChange on every edit. */
    public static class SearchBar extends HomeComponents.RoundedPanel {
        private final HintTextField field;

        public SearchBar(String hint, Consumer<String> onChange) {
            super(14, HomeComponents.CARD, HomeComponents.CARD_BORDER);
            setLayout(new BorderLayout(8, 0));
            setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));

            field = new HintTextField(hint);
            field.getDocument().addDocumentListener(new DocumentListener() {
                private void changed() { if (onChange != null) onChange.accept(field.getText()); }
                @Override public void insertUpdate(DocumentEvent e)  { changed(); }
                @Override public void removeUpdate(DocumentEvent e)  { changed(); }
                @Override public void changedUpdate(DocumentEvent e) { changed(); }
            });

            add(new JLabel(new DirectoryIcon(DirectoryIcon.Type.SEARCH, 16, HomeComponents.TEXT_MUTED)),
                    BorderLayout.WEST);
            add(field, BorderLayout.CENTER);
        }
    }

    /** Row of pill-shaped category filters; one is selected at a time. */
    public static class FilterChips extends JPanel {
        private final Chip[] chips;

        public FilterChips(String[] labels, String selected, Consumer<String> onSelect) {
            super(new FlowLayout(FlowLayout.LEFT, 6, 8));
            setOpaque(false);

            chips = new Chip[labels.length];
            for (int i = 0; i < labels.length; i++) {
                final Chip chip = new Chip(labels[i]);
                chips[i] = chip;
                add(chip);
                HomeComponents.makeClickable(chip, () -> {
                    select(chip.label);
                    if (onSelect != null) onSelect.accept(chip.label);
                });
            }
            select(selected);
        }

        public void select(String label) {
            for (Chip c : chips) c.setSelected(c.label.equals(label));
        }

        private static class Chip extends JPanel {
            final String label;
            private final JLabel text;
            private boolean selected;

            Chip(String label) {
                this.label = label;
                setOpaque(false);
                setLayout(new BorderLayout());
                setBorder(BorderFactory.createEmptyBorder(5, 11, 5, 11));
                text = new JLabel(label);
                text.setFont(HomeComponents.font(Font.PLAIN, 11));
                add(text, BorderLayout.CENTER);
            }

            void setSelected(boolean selected) {
                this.selected = selected;
                text.setForeground(selected ? Color.WHITE : HomeComponents.TEXT_BODY);
                repaint();
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();
                g2.setColor(selected ? HomeComponents.PRIMARY : HomeComponents.CARD);
                g2.fill(new RoundRectangle2D.Double(0, 0, w - 1, h - 1, h, h));
                g2.setColor(selected ? HomeComponents.PRIMARY : HomeComponents.CARD_BORDER);
                g2.setStroke(new BasicStroke(1f));
                g2.draw(new RoundRectangle2D.Double(0.5, 0.5, w - 2, h - 2, h, h));
                g2.dispose();
                super.paintComponent(g);
            }
        }
    }

    /** Business row: store icon, name, category + open status, address and rating. */
    public static class BusinessCard extends HomeComponents.RoundedPanel {
        public BusinessCard(String name, String type, boolean open, String address,
                            double rating, Runnable onClick) {
            super(14, HomeComponents.CARD, HomeComponents.CARD_BORDER);
            setLayout(new BorderLayout(10, 0));
            setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

            HomeComponents.IconTile tile = new HomeComponents.IconTile(40, 12, HomeComponents.PRIMARY_SOFT,
                    new DirectoryIcon(DirectoryIcon.Type.STORE, 22, HomeComponents.PRIMARY));

            JLabel nameLabel = new JLabel(name);
            nameLabel.setFont(HomeComponents.font(Font.BOLD, 12).deriveFont(11.5f));
            nameLabel.setForeground(HomeComponents.TEXT_DARK);
            nameLabel.setAlignmentX(LEFT_ALIGNMENT);

            String status = open
                    ? "<font color='#16A34A'><b>Open Now</b></font>"
                    : "<b>Closed</b>";
            JLabel statusLabel = new JLabel("<html>" + type.replace("&", "&amp;")
                    + " \u00B7 " + status + "</html>");
            statusLabel.setFont(HomeComponents.font(Font.PLAIN, 10));
            statusLabel.setForeground(HomeComponents.TEXT_BODY);
            statusLabel.setAlignmentX(LEFT_ALIGNMENT);

            JLabel addressLabel = new JLabel(address);
            addressLabel.setFont(HomeComponents.font(Font.PLAIN, 10));
            addressLabel.setForeground(HomeComponents.TEXT_MUTED);
            addressLabel.setAlignmentX(LEFT_ALIGNMENT);

            JPanel locationLink = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
            locationLink.setOpaque(false);
            locationLink.setAlignmentX(LEFT_ALIGNMENT);
            locationLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            JLabel pin = new JLabel(new DirectoryIcon(DirectoryIcon.Type.MAP_PIN, 12, HomeComponents.PRIMARY));
            pin.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            locationLink.add(pin);
            JLabel locationText = new JLabel("<html>View location</html>");
            locationText.setFont(HomeComponents.font(Font.BOLD, 10));
            locationText.setForeground(HomeComponents.PRIMARY);
            locationText.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            locationLink.add(locationText);
            MouseAdapter openLocation = new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) {
                    if (onClick != null) onClick.run();
                }
            };
            locationLink.addMouseListener(openLocation);
            pin.addMouseListener(openLocation);
            locationText.addMouseListener(openLocation);

            JPanel text = new JPanel();
            text.setOpaque(false);
            text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
            text.add(nameLabel);
            text.add(Box.createVerticalStrut(3));
            text.add(statusLabel);
            text.add(Box.createVerticalStrut(1));
            text.add(addressLabel);
            text.add(Box.createVerticalStrut(4));
            text.add(locationLink);

            JPanel rate = new JPanel(new FlowLayout(FlowLayout.RIGHT, 3, 0));
            rate.setOpaque(false);
            rate.add(new JLabel(new DirectoryIcon(DirectoryIcon.Type.STAR, 12, STAR_AMBER)));
            JLabel ratingLabel = new JLabel(String.format(Locale.US, "%.1f", rating));
            ratingLabel.setFont(HomeComponents.font(Font.BOLD, 11));
            ratingLabel.setForeground(HomeComponents.TEXT_DARK);
            rate.add(ratingLabel);

            JPanel rateWrap = new JPanel(new BorderLayout());
            rateWrap.setOpaque(false);
            rateWrap.add(rate, BorderLayout.NORTH);

            add(tile, BorderLayout.WEST);
            add(text, BorderLayout.CENTER);
            add(rateWrap, BorderLayout.EAST);

            HomeComponents.makeClickable(this, onClick);
        }
    }

    /**
     * Vertical list whose maximum height follows its current content,
     * so it can be rebuilt (filtered) without leaving a stale size behind.
     */
    public static class ListPanel extends JPanel {
        public ListPanel() {
            setOpaque(false);
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setAlignmentX(LEFT_ALIGNMENT);
        }

        @Override
        public Dimension getMaximumSize() {
            return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
        }
    }
}
