package barangay.eservices;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.*;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

class TransportScreen extends ScrollableScreen {
    private final NavListener nav;
    TransportScreen(NavListener nav) { this.nav = nav; }

    @Override protected String getHeaderTitle()    { return "Barangay Transport"; }
    @Override protected boolean hasHeader()        { return false; }
    @Override protected JComponent getFixedHeader() {
        return new HomeComponents.HeaderBar(
                "Barangay Transport", "Central Signal, Taguig", () -> nav.onNavigate("Home"));
    }
    @Override protected AppSection createContent() { return new TransportContent(TransportData.getTransportServices()); }
}

class TransportContent extends AppSection implements Scrollable {
    private final List<TransportService> services;

    TransportContent(List<TransportService> services) { this.services = services; }

    @Override
    protected void buildUI() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(Theme.BG);
        setBorder(new EmptyBorder(16, 16, 32, 16));

        JLabel heading = new JLabel("Utility Vehicle Requests");
        heading.setFont(Theme.bold(19));
        heading.setForeground(Theme.TEXT);

        JLabel desc = new JLabel("<html><body width='" + (HomeComponents.CONTENT_WIDTH - 24)
                + "'>Request valid non-emergency transport services "
                + "offered by Barangay Central Signal. Please request at least 2 days prior to schedule.</body></html>");
        desc.setFont(Theme.regular(12));
        desc.setForeground(Theme.TEXT_SUB);

        addFullWidth(heading);
        add(Box.createVerticalStrut(6));
        addFullWidth(desc);
        add(Box.createVerticalStrut(14));

        addFullWidth(new TransportComponents.NoticeBanner("IMPORTANT:",
                "This is purely for non-emergency utility use. For urgent or life-threatening medical "
                        + "emergencies, please tap the SOS button or use the Emergency Hotline."));
        add(Box.createVerticalStrut(14));

        for (TransportService t : services) {
            addFullWidth(new TransportComponents.ServiceCard(
                    serviceIcon(t.getIcon(), 22, Theme.RED),
                    t.getTitle(),
                    t.getDescription(),
                    "File Travel Request",
                    () -> new TravelRequestDialog(t).show(this)));
            add(Box.createVerticalStrut(12));
        }
    }

    private void addFullWidth(JComponent component) {
        component.setAlignmentX(LEFT_ALIGNMENT);
        Dimension preferred = component.getPreferredSize();
        component.setMinimumSize(new Dimension(0, preferred.height));
        component.setMaximumSize(new Dimension(Integer.MAX_VALUE, preferred.height));
        add(component);
    }

    private static Icon serviceIcon(IconType type, int size, Color color) {
        return new Icon() {
            @Override public int getIconWidth() { return size; }
            @Override public int getIconHeight() { return size; }

            @Override
            public void paintIcon(Component component, Graphics graphics, int x, int y) {
                Graphics2D g2 = (Graphics2D) graphics.create(x, y, size, size);
                Theme.smooth(g2);
                type.draw(g2, size, color);
                g2.dispose();
            }
        };
    }

    @Override public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
    @Override public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) { return 16; }
    @Override public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        return visibleRect.height;
    }
    @Override public boolean getScrollableTracksViewportWidth() { return true; }
    @Override public boolean getScrollableTracksViewportHeight() { return false; }
}

class TravelRequest {
    private final String reference;
    private final String submittedAt;
    private final String service;
    private final String fullName;
    private final String contact;
    private final String travelDate;
    private final String destination;
    private final int passengers;
    private final String purpose;

    TravelRequest(String reference, String submittedAt, String service, String fullName, String contact,
                  String travelDate, String destination, int passengers, String purpose) {
        this.reference = reference;
        this.submittedAt = submittedAt;
        this.service = service;
        this.fullName = fullName;
        this.contact = contact;
        this.travelDate = travelDate;
        this.destination = destination;
        this.passengers = passengers;
        this.purpose = purpose;
    }

    public String getReference() { return reference; }

    static String csvHeader() {
        return "Reference,Submitted At,Service,Full Name,Contact Number,Travel Date,Destination,Passengers,Purpose,Status";
    }

    String toCsvLine() {
        return String.join(",", csv(reference), csv(submittedAt), csv(service), csv(fullName), csv(contact),
                csv(travelDate), csv(destination), String.valueOf(passengers), csv(purpose), csv("PENDING"));
    }

    private static String csv(String value) {
        String v = value.replace("\r", " ").replace("\n", " ").replace("\"", "\"\"");
        return "\"" + v + "\"";
    }
}

/** Saves travel requests to travel_requests.csv (in the folder the app is run from). Opens in Excel. */
final class TravelRequestStore {
    private static final Path FILE = Paths.get("travel_requests.csv");

    private TravelRequestStore() {}

    static Path getFile() { return FILE.toAbsolutePath(); }

    static synchronized void save(TravelRequest request) throws IOException {
        boolean isNew = !Files.exists(FILE) || Files.size(FILE) == 0;
        StringBuilder sb = new StringBuilder();
        if (isNew) sb.append(TravelRequest.csvHeader()).append(System.lineSeparator());
        sb.append(request.toCsvLine()).append(System.lineSeparator());
        Files.write(FILE, sb.toString().getBytes(StandardCharsets.UTF_8),
                StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }
}

/** The "File Travel Request" form. Saves the request to the CSV file when OK is pressed. */
class TravelRequestDialog {
    private final TransportService service;
    private final JTextField nameField = new JTextField(24);
    private final JTextField contactField = new JTextField(24);
    private final JTextField dateField = new JTextField(LocalDate.now().plusDays(2).toString(), 24);
    private final JTextField destinationField = new JTextField(24);
    private final JSpinner passengersSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 60, 1));
    private final JTextArea purposeArea = new JTextArea(3, 24);

    TravelRequestDialog(TransportService service) {
        this.service = service;
    }

    void show(Component parent) {
        JPanel form = buildForm();
        while (true) {
            int choice = JOptionPane.showConfirmDialog(parent, form, "File Travel Request",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (choice != JOptionPane.OK_OPTION) return;

            String error = validateInput();
            if (error != null) {
                JOptionPane.showMessageDialog(parent, error, "File Travel Request", JOptionPane.WARNING_MESSAGE);
                continue;                                   // show the form again
            }

            TravelRequest request = new TravelRequest(
                    "TR-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")),
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                    service.getTitle(),
                    nameField.getText().trim(),
                    contactField.getText().trim(),
                    dateField.getText().trim(),
                    destinationField.getText().trim(),
                    (Integer) passengersSpinner.getValue(),
                    purposeArea.getText().trim());
            try {
                TravelRequestStore.save(request);
                JOptionPane.showMessageDialog(parent,
                        "Travel request saved!\n\nReference: " + request.getReference()
                                + "\nService: " + service.getTitle()
                                + "\nSaved to: " + TravelRequestStore.getFile(),
                        "File Travel Request", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(parent, "Could not save the request to the file:\n" + ex.getMessage(),
                        "File Travel Request", JOptionPane.ERROR_MESSAGE);
            }
            return;
        }
    }

    /** Returns an error message, or null when everything is valid. */
    private String validateInput() {
        if (nameField.getText().trim().isEmpty() || contactField.getText().trim().isEmpty()
                || destinationField.getText().trim().isEmpty() || purposeArea.getText().trim().isEmpty()) {
            return "Please fill in your name, contact number, destination and purpose.";
        }
        LocalDate date;
        try {
            date = LocalDate.parse(dateField.getText().trim());
        } catch (DateTimeParseException ex) {
            return "Please use the date format YYYY-MM-DD (example: " + LocalDate.now().plusDays(3) + ").";
        }
        LocalDate earliest = LocalDate.now().plusDays(2);
        if (date.isBefore(earliest)) {
            return "Please request at least 2 days before the travel date (earliest: " + earliest + ").";
        }
        return null;
    }

    private JPanel buildForm() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));

        purposeArea.setLineWrap(true);
        purposeArea.setWrapStyleWord(true);

        p.add(boldLabel("Service: " + service.getTitle()));
        p.add(Box.createVerticalStrut(8));
        p.add(boldLabel("Full name:"));
        p.add(nameField);
        p.add(Box.createVerticalStrut(6));
        p.add(boldLabel("Contact number:"));
        p.add(contactField);
        p.add(Box.createVerticalStrut(6));
        p.add(boldLabel("Travel date (YYYY-MM-DD):"));
        p.add(dateField);
        p.add(Box.createVerticalStrut(6));
        p.add(boldLabel("Destination:"));
        p.add(destinationField);
        p.add(Box.createVerticalStrut(6));
        p.add(boldLabel("Number of passengers:"));
        p.add(passengersSpinner);
        p.add(Box.createVerticalStrut(6));
        p.add(boldLabel("Purpose / details:"));
        p.add(new JScrollPane(purposeArea));
        p.add(Box.createVerticalStrut(6));
        JLabel note = new JLabel("Requests are saved to " + TravelRequestStore.getFile().getFileName());
        note.setFont(note.getFont().deriveFont(Font.ITALIC, 10f));
        p.add(note);

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
class TransportData {
    static List<TransportService> getTransportServices() {
        List<TransportService> list = new ArrayList<>();
        list.add(new TransportService("Medical Transport Assistance",
                "For pre-scheduled hospital visits, dialysis, or clinical checkups. Non-emergency only.", IconType.TRUCK));
        list.add(new TransportService("Community & Youth Programs",
                "Utility vehicles for community seminars, local youth sports, or official barangay tours.", IconType.USERS));
        list.add(new TransportService("Disaster Relief Operations",
                "Emergency transport support for evacuation, heavy rains, or general relief distribution.", IconType.BOX));
        list.add(new TransportService("Burial Transport Assistance",
                "Sincere community support vehicles for bereavement and funeral convoy assistance.", IconType.CIRCLE_X));
        return list;
    }
}
