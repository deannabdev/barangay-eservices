package components;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public final class Models {
    private Models() {}

    public enum BusinessType {
        FOOD("Food"), PHARMACY("Pharmacy"), RETAIL("Retail"), HARDWARE("Hardware");

        private final String label;

        BusinessType(String label) { this.label = label; }

        public String getLabel() { return label; }
    }

    public static final class Business {
        private final String name;
        private final BusinessType type;
        private final String category;
        private final boolean open;
        private final String address;
        private final double rating;
        private final double latitude;
        private final double longitude;

        public Business(String name, BusinessType type, String category, boolean open, String address, double rating,
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

        public String getName() { return name; }
        public BusinessType getType() { return type; }
        public String getCategory() { return category; }
        public boolean isOpen() { return open; }
        public String getAddress() { return address; }
        public double getRating() { return rating; }
        public double getLatitude() { return latitude; }
        public double getLongitude() { return longitude; }

        public boolean matches(String query, BusinessType filter) {
            if (filter != null && filter != type) return false;
            String q = query == null ? "" : query.trim().toLowerCase();
            if (q.isEmpty()) return true;
            return name.toLowerCase().contains(q) || category.toLowerCase().contains(q)
                    || address.toLowerCase().contains(q) || type.getLabel().toLowerCase().contains(q);
        }
    }

}

interface Fee {
    String getDescription();
    boolean isFree();

    default CoreUi.Badge createTag() {
        Color background = isFree() ? Theme.GREEN_SOFT : Theme.RED_SOFT;
        Color foreground = isFree() ? Theme.GREEN : Theme.RED;
        return new CoreUi.Badge(getDescription(), background, foreground, 8);
    }
}

class PaidFee implements Fee {
    private final String description;

    PaidFee(String description) { this.description = description; }

    @Override public String getDescription() { return description; }
    @Override public boolean isFree() { return false; }
}

class FreeFee implements Fee {
    @Override public String getDescription() { return "Free"; }
    @Override public boolean isFree() { return true; }
}

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

    public String getTitle() { return title; }
    public String getRequirements() { return requirements; }
    public String getProcessing() { return processing; }
    public Fee getFee() { return fee; }
}

class ConcernCategory {
    private final String name;
    private final IconType icon;

    ConcernCategory(String name, IconType icon) {
        this.name = name;
        this.icon = icon;
    }

    public String getName() { return name; }
    public IconType getIcon() { return icon; }
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

    public Color getCardBg() { return cardBg; }
    public Color getTitle() { return title; }
    public Color getSubtitle() { return subtitle; }
    public Color getTagline() { return tagline; }
    public Color getIconBg() { return iconBg; }
    public Color getIconFg() { return iconFg; }
    public Color getCallBg() { return callBg; }
    public Color getCallFg() { return callFg; }
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

    public String getName() { return name; }
    public String getAgency() { return agency; }
    public String getTagline() { return tagline; }
    public HotlineStyle getStyle() { return style; }
}

class Official {
    private final String name;
    private final String position;

    Official(String name, String position) {
        this.name = name;
        this.position = position;
    }

    public String getName() { return name; }
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

    public String getTitle() { return title; }
    public String getSubtitle() { return subtitle; }
    public IconType getIcon() { return icon; }
    public Color getSoft() { return soft; }
    public Color getAccent() { return accent; }
    public String getTarget() { return target; }
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

    public String getTag() { return tag; }
    public String getDate() { return date; }
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

    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public IconType getIcon() { return icon; }
}

enum ReportStatus {
    PENDING("Pending", new Color(0xFEF3C7), new Color(0xD97706)),
    IN_PROGRESS("In Progress", new Color(0xDBEAFE), new Color(0x2563EB)),
    RESOLVED("Resolved", new Color(0xDCFCE7), new Color(0x16A34A));

    private final String label;
    private final Color bg;
    private final Color fg;

    ReportStatus(String label, Color bg, Color fg) {
        this.label = label;
        this.bg = bg;
        this.fg = fg;
    }

    public String getLabel() { return label; }
    public Color getBg() { return bg; }
    public Color getFg() { return fg; }
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
    public String getDetails() { return details; }
    public File getPhoto() { return photo; }
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

    public String getTitle() { return title; }
    public String getReference() { return reference; }
    public ReportStatus getStatus() { return status; }
    public IconType getIcon() { return icon; }
    public ReportDraft getDraft() { return draft; }
}

class DataRepository {
    static List<DocumentRequest> getDocuments() {
        List<DocumentRequest> list = new ArrayList<>();
        list.add(new DocumentRequest("Barangay Clearance", "1 Valid ID, Cedula", "1 Working Day", new PaidFee("₱50.00")));
        list.add(new DocumentRequest("Certificate of Residency", "Voter's ID or Proof of Billing", "Same Day", new FreeFee()));
        list.add(new DocumentRequest("Certificate of Indigency", "Affidavit / Case Study", "Same Day", new FreeFee()));
        list.add(new DocumentRequest("Barangay Business Clearance", "DTI/SEC Reg, Mayor's Permit App", "2 Working Days", new PaidFee("₱500.00")));
        return list;
    }

    static List<Models.Business> getBusinesses() {
        List<Models.Business> list = new ArrayList<>();
        list.add(new Models.Business("Signal Central General Pharmacy", Models.BusinessType.PHARMACY, "Pharmacy", true,
                "Ballecer St., Taguig City", 4.8, 14.5124, 121.0571));
        list.add(new Models.Business("Kabayan Puregold Mini Grocery", Models.BusinessType.RETAIL, "Retail Store", true,
                "Purok 2, Taguig City", 4.3, 14.5109, 121.0582));
        list.add(new Models.Business("Ronald's Hardware & Construction Supply", Models.BusinessType.HARDWARE, "Hardware Store", true,
                "M.L. Quezon St., Taguig City", 4.6, 14.5115, 121.0563));
        list.add(new Models.Business("Mang Larry's BBQ House", Models.BusinessType.FOOD, "Food & Dining", true,
                "F. Manalo St., Taguig City", 4.2, 14.5131, 121.0578));
        list.add(new Models.Business("Sunrise Drugstore", Models.BusinessType.PHARMACY, "Pharmacy", true,
                "Purok 3, Taguig City", 4.5, 14.5120, 121.0575));
        list.add(new Models.Business("Aling Maria's Sari-Sari Store", Models.BusinessType.RETAIL, "Retail Store", true,
                "Purok 1, Taguig City", 4.0, 14.5112, 121.0568));
        list.add(new Models.Business("Joe's Kitchenette", Models.BusinessType.FOOD, "Food & Dining", true,
                "M.L. Quezon St., Taguig City", 4.7, 14.5119, 121.0570));
        list.add(new Models.Business("ABC Hardware Supply", Models.BusinessType.HARDWARE, "Hardware Store", true,
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
