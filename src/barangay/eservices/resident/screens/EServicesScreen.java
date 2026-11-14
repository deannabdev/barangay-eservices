package barangay.eservices;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.List;

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

    @Override public String getDescription() { return description; }
    @Override public boolean isFree() { return false; }
}

class FreeFee implements Fee {
    @Override public String getDescription() { return "Free"; }
    @Override public boolean isFree() { return true; }
}

class EServicesScreen extends ScrollableScreen {
    @Override protected String getHeaderTitle()    { return "Barangay E-Services"; }
    @Override protected boolean hasHeader()        { return false; }
    @Override protected JComponent getFixedHeader() {
        return new HomeComponents.HeaderBar("Barangay E-Services", "Central Signal, Taguig");
    }
    @Override protected AppSection createContent() { return new DocumentList(EServicesData.getDocuments()); }
}

class DocumentList extends AppSection implements Scrollable {
    private final List<DocumentRequest> requests;

    DocumentList(List<DocumentRequest> requests) { this.requests = requests; }

    @Override
    protected void buildUI() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(Theme.BG);
        setBorder(new EmptyBorder(16, 16, 32, 16));

        JLabel heading = new JLabel("Official Document Requests");
        heading.setFont(Theme.bold(19));
        heading.setForeground(Theme.TEXT);

        JLabel desc = new JLabel("<html><body width='310'>Request necessary local clearances and "
                + "certifications digitally. Complete requirements to ensure seamless processing.</body></html>");
        desc.setFont(Theme.regular(12));
        desc.setForeground(Theme.TEXT_SUB);

        addFullWidth(heading);
        add(Box.createVerticalStrut(6));
        addFullWidth(desc);
        add(Box.createVerticalStrut(14));

        for (DocumentRequest r : requests) {
            addFullWidth(new DocumentCard(r));
            add(Box.createVerticalStrut(14));
        }

        RoundedPanel note = new RoundedPanel(Color.WHITE, Theme.BORDER, 14);
        note.setLayout(new BorderLayout(8, 0));
        note.setBorder(new EmptyBorder(10, 12, 10, 12));
        note.add(new IconLabel(IconType.INFO, 14, Theme.MUTED));
        JLabel nl = new JLabel("<html><body width='255'>For offline assistance, visit the Central Signal "
                + "Barangay Hall, Balleer St. from Mon-Fri, 8 AM \u2013 5 PM.</body></html>");
        nl.setFont(Theme.regular(10));
        nl.setForeground(Theme.TEXT_SUB);
        note.add(nl);
        addFullWidth(note);
    }

    private void addFullWidth(JComponent component) {
        component.setAlignmentX(LEFT_ALIGNMENT);
        Dimension preferred = component.getPreferredSize();
        component.setMinimumSize(new Dimension(0, preferred.height));
        component.setMaximumSize(new Dimension(Integer.MAX_VALUE, preferred.height));
        add(component);
    }

    @Override public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
    @Override public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) { return 16; }
    @Override public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        return visibleRect.height;
    }
    @Override public boolean getScrollableTracksViewportWidth() { return true; }
    @Override public boolean getScrollableTracksViewportHeight() { return false; }
}

class DocumentCard extends RoundedPanel {
    private final DocumentRequest request;

    DocumentCard(DocumentRequest request) {
        super(Color.WHITE, Theme.BORDER, 16);
        this.request = request;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(new EmptyBorder(14, 14, 14, 14));
        build();
    }

    private void build() {
        JPanel head = new JPanel(new BorderLayout());
        head.setOpaque(false);
        JLabel title = new JLabel(request.getTitle());
        title.setFont(Theme.bold(14));
        title.setForeground(Theme.TEXT);
        head.add(title, BorderLayout.WEST);
        head.add(Theme.wrap(request.getFee().createTag()), BorderLayout.EAST); // polymorphic tag
        head.setBorder(new EmptyBorder(0, 0, 8, 0));
        head.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

        add(head);
        add(new Divider());
        add(Box.createVerticalStrut(10));
        add(infoRow(IconType.DOC, "Requirements:", request.getRequirements()));
        add(Box.createVerticalStrut(4));
        add(infoRow(IconType.CLOCK, "Processing:", request.getProcessing()));
        add(Box.createVerticalStrut(12));

        RoundedButton btn = new RoundedButton("Request Document");
        btn.addActionListener(e -> new DocumentRequestDialog(request).show(this));
        add(btn);

        for (Component c : getComponents()) ((JComponent) c).setAlignmentX(LEFT_ALIGNMENT);
    }

    private JPanel infoRow(IconType icon, String label, String value) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        row.setOpaque(false);
        row.add(new IconLabel(icon, 12, Theme.TEXT_SUB));
        JLabel l = new JLabel("<html><b>" + label + "</b> " + value + "</html>");
        l.setFont(Theme.regular(11));
        l.setForeground(Theme.TEXT_SUB);
        row.add(l);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
        return row;
    }
}

/** The "Document Request" pop-up: asks for full name + contact number and lists the requirements. */
class DocumentRequestDialog {
    private final DocumentRequest request;                 // encapsulated: the dialog owns its own state
    private final JTextField nameField = new JTextField(22);
    private final JTextField contactField = new JTextField(22);

    DocumentRequestDialog(DocumentRequest request) {
        this.request = request;
    }

    void show(Component parent) {
        JPanel form = buildForm();
        while (true) {
            int choice = JOptionPane.showConfirmDialog(parent, form, "Document Request",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (choice != JOptionPane.OK_OPTION) return;             // Cancel / closed

            String name = nameField.getText().trim();
            String contact = contactField.getText().trim();
            if (name.isEmpty() || contact.isEmpty()) {
                JOptionPane.showMessageDialog(parent, "Please enter your full name and contact number.",
                        "Document Request", JOptionPane.WARNING_MESSAGE);
                continue;                                              // show the form again
            }
            JOptionPane.showMessageDialog(parent,
                    "Request submitted!\n\nDocument: " + request.getTitle()
                            + "\nName: " + name + "\nContact: " + contact
                            + "\nProcessing: " + request.getProcessing(),
                    "Document Request", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
    }

    private JPanel buildForm() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));

        p.add(boldLabel("Document: " + request.getTitle()));
        p.add(Box.createVerticalStrut(8));
        p.add(boldLabel("Full name:"));
        p.add(nameField);
        p.add(Box.createVerticalStrut(8));
        p.add(boldLabel("Contact number:"));
        p.add(contactField);
        p.add(Box.createVerticalStrut(8));
        p.add(boldLabel("Requirements:"));
        p.add(new JLabel(request.getRequirements()));

        for (Component c : p.getComponents()) ((JComponent) c).setAlignmentX(Component.LEFT_ALIGNMENT);
        return p;
    }

    private JLabel boldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(l.getFont().deriveFont(Font.BOLD));
        return l;
    }
}

/** Sample data for this screen. */
class EServicesData {
    static List<DocumentRequest> getDocuments() {
        List<DocumentRequest> list = new ArrayList<>();
        list.add(new DocumentRequest("Barangay Clearance", "1 Valid ID, Cedula", "1 Working Day", new PaidFee("\u20B150.00")));
        list.add(new DocumentRequest("Certificate of Residency", "Voter's ID or Proof of Billing", "Same Day", new FreeFee()));
        list.add(new DocumentRequest("Certificate of Indigency", "Affidavit / Case Study", "Same Day", new FreeFee()));
        list.add(new DocumentRequest("Barangay Business Clearance", "DTI/SEC Reg, Mayor's Permit App", "2 Working Days", new PaidFee("\u20B1500.00")));
        return list;
    }
}
