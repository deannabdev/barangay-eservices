package barangay.eservices;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.List;

class DirectoryScreen extends ScrollableScreen {
    @Override protected String getHeaderTitle()    { return "Barangay Directory"; }
    @Override protected AppSection createContent() { return new DirectoryContent(DirectoryData.getBusinesses()); }
}

class DirectoryContent extends AppSection {
    private final List<Business> businesses;
    private final List<FilterChip> chips = new ArrayList<>();
    private final List<BusinessType> chipTypes = new ArrayList<>();   // null = "All"
    private final JPanel listPanel = new JPanel();
    private String query = "";
    private BusinessType selected = null;

    DirectoryContent(List<Business> businesses) { this.businesses = businesses; }

    @Override
    protected void buildUI() {
        setLayout(new BorderLayout());
        setBackground(Theme.BG);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(Theme.BG);
        content.setBorder(new EmptyBorder(16, 16, 16, 16));

        JLabel heading = new JLabel("Local Businesses");
        heading.setFont(Theme.bold(19));
        heading.setForeground(Theme.TEXT);

        JLabel desc = new JLabel("<html><body width='310'>Support local registered enterprises in Barangay "
                + "Central Signal. Verified under municipal business permits.</body></html>");
        desc.setFont(Theme.regular(12));
        desc.setForeground(Theme.TEXT_SUB);

        content.add(heading);
        content.add(Box.createVerticalStrut(6));
        content.add(desc);
        content.add(Box.createVerticalStrut(14));
        content.add(new SearchBar("Search pharmacies, eateries, groceries...", text -> {
            query = text;
            refresh();
        }));
        content.add(Box.createVerticalStrut(12));

        // filter chips
        JPanel chipRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        chipRow.setOpaque(false);
        addChip(chipRow, "All", null);
        for (BusinessType t : BusinessType.values()) addChip(chipRow, t.getLabel(), t);
        chipRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        content.add(chipRow);
        content.add(Box.createVerticalStrut(14));

        listPanel.setOpaque(false);
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        content.add(listPanel);

        for (Component c : content.getComponents()) ((JComponent) c).setAlignmentX(LEFT_ALIGNMENT);
        add(content, BorderLayout.NORTH);
        refresh();
    }

    private void addChip(JPanel row, String label, BusinessType type) {
        FilterChip chip = new FilterChip(label, () -> {
            selected = type;
            for (int i = 0; i < chips.size(); i++) chips.get(i).setSelected(chipTypes.get(i) == selected);
            refresh();
        });
        chip.setSelected(type == selected);
        chips.add(chip);
        chipTypes.add(type);
        row.add(chip);
    }

    private void refresh() {
        listPanel.removeAll();
        int count = 0;
        for (Business b : businesses) {
            if (!b.matches(query, selected)) continue;
            BusinessCard card = new BusinessCard(b);
            card.setAlignmentX(LEFT_ALIGNMENT);
            listPanel.add(card);
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
}

/** Receives the text typed in the search bar. */
interface TextListener {
    void onText(String text);
}

class SearchBar extends RoundedPanel {
    SearchBar(String hint, TextListener listener) {
        super(Color.WHITE, Theme.BORDER, 14);
        setLayout(new BorderLayout(8, 0));
        setBorder(new EmptyBorder(10, 12, 10, 12));

        PlaceholderField field = new PlaceholderField(hint);
        field.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            private void changed() { listener.onText(field.getText()); }
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e)  { changed(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e)  { changed(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { changed(); }
        });

        add(Theme.wrap(new IconLabel(IconType.MAGNIFIER, 14, new Color(0x6B, 0x72, 0x80))), BorderLayout.WEST);
        add(field, BorderLayout.CENTER);
    }
}

class FilterChip extends JPanel {
    private final String label;
    private boolean selected;

    FilterChip(String label, Runnable onClick) {
        this.label = label;
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        int w = getFontMetrics(Theme.bold(10)).stringWidth(label) + 28;
        setPreferredSize(new Dimension(w, 28));
        addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { onClick.run(); }
        });
    }

    void setSelected(boolean selected) {
        this.selected = selected;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g0) {
        Graphics2D g = (Graphics2D) g0.create();
        Theme.smooth(g);
        RoundRectangle2D shape = new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, 28, 28);
        g.setColor(selected ? Theme.RED : Color.WHITE);
        g.fill(shape);
        if (!selected) {
            g.setColor(Theme.BORDER);
            g.draw(shape);
        }
        g.setFont(selected ? Theme.bold(10) : Theme.regular(10));
        g.setColor(selected ? Color.WHITE : Theme.TEXT);
        FontMetrics fm = g.getFontMetrics();
        g.drawString(label, (getWidth() - fm.stringWidth(label)) / 2,
                (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
        g.dispose();
    }
}

class BusinessCard extends RoundedPanel {
    BusinessCard(Business b) {
        super(Color.WHITE, Theme.BORDER, 14);
        setLayout(new BorderLayout(10, 0));
        setBorder(new EmptyBorder(14, 12, 14, 14));

        RoundedPanel icon = new RoundedPanel(new Color(0xFD, 0xEE, 0xEE), null, 10);
        icon.setLayout(new GridBagLayout());
        icon.setPreferredSize(new Dimension(32, 32));
        icon.add(new IconLabel(IconType.STORE, 16, Theme.RED));

        JLabel name = new JLabel(b.getName());
        name.setFont(Theme.bold(11));
        name.setForeground(Theme.TEXT);

        // category + open/closed status (colour depends on the business state)
        String statusColor = b.isOpen() ? "#16A34A" : "#DC2626";
        String statusText = b.isOpen() ? "Open Now" : "Closed";
        JLabel sub = new JLabel("<html><span style='color:#4B5563'>" + b.getCategory() + "</span> \u00B7 <b style='color:"
                + statusColor + "'>" + statusText + "</b></html>");
        sub.setFont(Theme.regular(9));

        JLabel addr = new JLabel(b.getAddress());
        addr.setFont(Theme.regular(9));
        addr.setForeground(Theme.MUTED);

        JPanel texts = new JPanel();
        texts.setOpaque(false);
        texts.setLayout(new BoxLayout(texts, BoxLayout.Y_AXIS));
        texts.add(name);
        texts.add(Box.createVerticalStrut(2));
        texts.add(sub);
        texts.add(Box.createVerticalStrut(2));
        texts.add(addr);
        JLabel view = new JLabel("View on map \u2192");
        view.setFont(Theme.bold(9));
        view.setForeground(Theme.RED);
        texts.add(Box.createVerticalStrut(3));
        texts.add(view);
        for (Component c : texts.getComponents()) ((JComponent) c).setAlignmentX(LEFT_ALIGNMENT);
        JPanel textWrap = new JPanel(new GridBagLayout());
        textWrap.setOpaque(false);
        GridBagConstraints gc = new GridBagConstraints();
        gc.anchor = GridBagConstraints.WEST;
        gc.weightx = 1;
        textWrap.add(texts, gc);

        // rating (top right)
        JPanel rating = new JPanel(new FlowLayout(FlowLayout.RIGHT, 3, 0));
        rating.setOpaque(false);
        rating.add(new IconLabel(IconType.STAR, 11, new Color(0xF5, 0x9E, 0x0B)));
        JLabel rl = new JLabel(String.format("%.1f", b.getRating()));
        rl.setFont(Theme.bold(10));
        rl.setForeground(Theme.TEXT);
        rating.add(rl);
        JPanel ratingWrap = new JPanel(new GridBagLayout());
        ratingWrap.setOpaque(false);
        GridBagConstraints top = new GridBagConstraints();
        top.anchor = GridBagConstraints.NORTH;
        top.weighty = 1;
        ratingWrap.add(rating, top);

        add(Theme.wrap(icon), BorderLayout.WEST);
        add(textWrap, BorderLayout.CENTER);
        add(ratingWrap, BorderLayout.EAST);

        // tap the card to see where the business is located
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                MapDialog.show(BusinessCard.this, b.getName(), b.getCategory() + "  \u00B7  " + b.getAddress(),
                        b.getLatitude(), b.getLongitude(), b.getName(), 17);
            }
        });
    }
}

/** Sample data for this screen. */
class DirectoryData {
    /** NOTE: the business coordinates below are SAMPLE values near the barangay - replace them with real ones. */
    static List<Business> getBusinesses() {
        List<Business> list = new ArrayList<>();
        list.add(new Business("Signal Central General Pharmacy", BusinessType.PHARMACY, "Pharmacy", true,
                "Ballecer St., Taguig City", 4.8, 14.5124, 121.0571));
        list.add(new Business("Kabayan Puregold Mini Grocery", BusinessType.RETAIL, "Retail Store", true,
                "E. Rodriguez Ave, Taguig", 4.5, 14.5108, 121.0552));
        list.add(new Business("Central Signal Eatery & Bakery", BusinessType.FOOD, "Food & Dining", false,
                "General Santos Ave, Taguig", 4.7, 14.5119, 121.0545));
        list.add(new Business("Signal Hills Hardware Supplies", BusinessType.HARDWARE, "Construction", true,
                "Ballecer St., Taguig City", 4.3, 14.5101, 121.0577));
        return list;
    }
}
