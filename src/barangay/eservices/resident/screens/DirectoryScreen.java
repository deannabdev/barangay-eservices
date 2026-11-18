package screens;
import components.CoreUi.HomeComponents;
import components.CoreUi.DirectoryComponents;
import components.CoreUi.AppSection;
import components.CoreUi.NavListener;
import components.CoreUi.ScrollableScreen;
import components.Models.Business;
import components.Models.BusinessType;
import components.Theme;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

class DirectoryScreen extends ScrollableScreen {
    private final NavListener nav;

    DirectoryScreen(NavListener nav) { this.nav = nav; }

    @Override protected String getHeaderTitle()    { return "Barangay Directory"; }
    @Override protected boolean hasHeader()        { return false; }
    @Override protected JComponent getFixedHeader() {
        return new HomeComponents.HeaderBar(
                "Barangay Directory", "Central Signal, Taguig", () -> nav.onNavigate("Home"));
    }
    @Override protected AppSection createContent() { return new DirectoryContent(DirectoryData.getBusinesses()); }
}

class DirectoryContent extends AppSection implements Scrollable {
    private final List<Business> businesses;
    private final DirectoryComponents.ListPanel listPanel = new DirectoryComponents.ListPanel();
    private String query = "";
    private BusinessType selected = null;
    private DirectoryComponents.FilterChips filterChips;

    DirectoryContent(List<Business> businesses) { this.businesses = businesses; }

    @Override
    protected void buildUI() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(Theme.BG);
        setBorder(new EmptyBorder(16, 16, 32, 16));

        JLabel heading = new JLabel("Local Businesses");
        heading.setFont(Theme.bold(19));
        heading.setForeground(Theme.TEXT);

        JLabel desc = new JLabel("<html><body width='" + (HomeComponents.CONTENT_WIDTH - 24)
                + "'>Support local registered enterprises in Barangay "
                + "Central Signal. Verified under municipal business permits.</body></html>");
        desc.setFont(Theme.regular(12));
        desc.setForeground(Theme.TEXT_SUB);

        addFullWidth(heading);
        add(Box.createVerticalStrut(6));
        addFullWidth(desc);
        add(Box.createVerticalStrut(14));
        addFullWidth(new DirectoryComponents.SearchBar("Search pharmacies, eateries, groceries...", text -> {
            query = text;
            refresh();
        }));
        add(Box.createVerticalStrut(12));

        String[] filters = new String[BusinessType.values().length + 1];
        filters[0] = "All";
        for (int i = 0; i < BusinessType.values().length; i++) {
            filters[i + 1] = BusinessType.values()[i].getLabel();
        }
        filterChips = new DirectoryComponents.FilterChips(filters, "All", label -> {
            selected = businessTypeForLabel(label);
            refresh();
        });
        filterChips.setAlignmentX(LEFT_ALIGNMENT);
        filterChips.setMinimumSize(new Dimension(0, 62));
        filterChips.setPreferredSize(new Dimension(0, 62));
        filterChips.setMaximumSize(new Dimension(Integer.MAX_VALUE, 62));
        add(filterChips);
        add(Box.createVerticalStrut(6));
        add(listPanel);
        refresh();
    }

    private void addFullWidth(JComponent component) {
        component.setAlignmentX(LEFT_ALIGNMENT);
        Dimension preferred = component.getPreferredSize();
        component.setMinimumSize(new Dimension(0, preferred.height));
        component.setMaximumSize(new Dimension(Integer.MAX_VALUE, preferred.height));
        add(component);
    }

    private static BusinessType businessTypeForLabel(String label) {
        for (BusinessType type : BusinessType.values()) {
            if (type.getLabel().equals(label)) return type;
        }
        return null;
    }

    private void refresh() {
        listPanel.removeAll();
        int count = 0;
        for (Business b : businesses) {
            if (!b.matches(query, selected)) continue;
            listPanel.add(new DirectoryComponents.BusinessCard(
                    b.getName(), b.getCategory(), b.isOpen(), b.getAddress(), b.getRating(),
                    () -> MapDialog.show(this, b.getName(),
                            b.getCategory() + "  \u00B7  " + b.getAddress(),
                            b.getLatitude(), b.getLongitude(), b.getName(), 17)));
            listPanel.add(Box.createVerticalStrut(12));
            count++;
        }
        if (count == 0) {
            JLabel none = new JLabel("No businesses found.");
            none.setFont(Theme.regular(12));
            none.setForeground(Theme.MUTED);
            none.setBorder(new EmptyBorder(20, 4, 0, 0));
            none.setAlignmentX(LEFT_ALIGNMENT);
            listPanel.add(none);
        }
        listPanel.revalidate();
        listPanel.repaint();
    }

    @Override public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
    @Override public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) { return 16; }
    @Override public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        return visibleRect.height;
    }
    @Override public boolean getScrollableTracksViewportWidth() { return true; }
    @Override public boolean getScrollableTracksViewportHeight() { return false; }
}

/** Sample data for this screen. */
class DirectoryData {
    /** NOTE: the business coordinates below are SAMPLE values near the barangay - replace them with real ones. */
    static List<Business> getBusinesses() {
        List<Business> list = new ArrayList<>();
        list.add(new Business("Signal Central General Pharmacy", BusinessType.PHARMACY, "Pharmacy", true,
                "Ballecer St., Taguig City", 4.8, 14.5124, 121.0571));
        list.add(new Business("Kabayan Puregold Mini Grocery", BusinessType.RETAIL, "Retail Store", true,
                "E. Rodriguez Ave., Taguig City", 4.5, 14.5108, 121.0552));
        list.add(new Business("Central Signal Eatery & Bakery", BusinessType.FOOD, "Food & Dining", false,
                "General Santos Ave., Taguig City", 4.7, 14.5119, 121.0545));
        list.add(new Business("Signal Hills Hardware Supplies", BusinessType.HARDWARE, "Construction", true,
                "Ballecer St., Taguig City", 4.3, 14.5101, 121.0577));
        return list;
    }
}
