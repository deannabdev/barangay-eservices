/**
 * TransportScreen.java - Transport - Utility Vehicle Requests; travel requests are saved to travel_requests.csv.
 */
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

class TransportScreen extends ScrollableScreen {
    private final NavListener nav;
    TransportScreen(NavListener nav) { this.nav = nav; }

    @Override protected String getHeaderTitle()    { return "Barangay Transport"; }
    @Override protected Runnable getBackAction()   { return () -> nav.onNavigate("Home"); }
    @Override protected AppSection createContent() { return new TransportContent(TransportData.getTransportServices()); }
}

class TransportContent extends AppSection {
    private final List<TransportService> services;

    TransportContent(List<TransportService> services) { this.services = services; }

    @Override
    protected void buildUI() {
        setLayout(new BorderLayout());
        setBackground(Theme.BG);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(Theme.BG);
        content.setBorder(new EmptyBorder(16, 16, 16, 16));

        JLabel heading = new JLabel("Utility Vehicle Requests");
        heading.setFont(Theme.bold(19));
        heading.setForeground(Theme.TEXT);

        JLabel desc = new JLabel("<html><body width='310'>Request valid non-emergency transport services "
                + "offered by Barangay Central Signal. Please request at least 2 days prior to schedule.</body></html>");
        desc.setFont(Theme.regular(12));
        desc.setForeground(Theme.TEXT_SUB);

        content.add(heading);
        content.add(Box.createVerticalStrut(6));
        content.add(desc);
        content.add(Box.createVerticalStrut(14));

        // IMPORTANT notice
        RoundedPanel warn = new RoundedPanel(new Color(0xFE, 0xEC, 0xEC), new Color(0xF3, 0xA6, 0xA6), 10);
        warn.setLayout(new FlowLayout(FlowLayout.LEFT, 8, 0));
        warn.setBorder(new EmptyBorder(10, 10, 10, 10));
        warn.add(new IconLabel(IconType.WARNING, 14, Theme.RED));
        JLabel wl = new JLabel("<html><body width='285'><b>IMPORTANT:</b> This is purely for non-emergency "
                + "utility use. For urgent or life-threatening medical emergencies, please tap the SOS button or use "
                + "the Emergency Hotline.</body></html>");
        wl.setFont(Theme.regular(10));
        wl.setForeground(new Color(0xB9, 0x1C, 0x1C));
        warn.add(wl);
        content.add(warn);
        content.add(Box.createVerticalStrut(14));

        for (TransportService t : services) {
            content.add(new TransportCard(t));
            content.add(Box.createVerticalStrut(12));
        }

        for (Component c : content.getComponents()) ((JComponent) c).setAlignmentX(LEFT_ALIGNMENT);
        add(content, BorderLayout.NORTH);
    }
}

class TransportCard extends RoundedPanel {
    private final TransportService service;

    TransportCard(TransportService service) {
        super(Color.WHITE, Theme.BORDER, 16);
        this.service = service;
        setLayout(new BorderLayout(12, 0));
        setBorder(new EmptyBorder(14, 12, 14, 12));

        RoundedPanel box = new RoundedPanel(Theme.RED_SOFT, null, 10);
        box.setLayout(new GridBagLayout());
        box.setPreferredSize(new Dimension(34, 34));
        box.add(new IconLabel(service.getIcon(), 17, Theme.RED));
        JPanel boxWrap = new JPanel(new GridBagLayout());
        boxWrap.setOpaque(false);
        GridBagConstraints top = new GridBagConstraints();
        top.anchor = GridBagConstraints.NORTH;
        top.weighty = 1;
        boxWrap.add(box, top);

        JLabel title = new JLabel(service.getTitle());
        title.setFont(Theme.bold(12));
        title.setForeground(Theme.TEXT);

        JLabel desc = new JLabel("<html><body width='245'>" + service.getDescription() + "</body></html>");
        desc.setFont(Theme.regular(10));
        desc.setForeground(Theme.TEXT_SUB);

        JLabel link = new JLabel("File Travel Request \u2192");
        link.setFont(Theme.bold(10));
        link.setForeground(Theme.RED);
        link.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        link.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                new TravelRequestDialog(TransportCard.this.service).show(TransportCard.this);
            }
        });

        JPanel texts = new JPanel();
        texts.setOpaque(false);
        texts.setLayout(new BoxLayout(texts, BoxLayout.Y_AXIS));
        texts.add(title);
        texts.add(Box.createVerticalStrut(3));
        texts.add(desc);
        texts.add(Box.createVerticalStrut(6));
        texts.add(link);
        for (Component c : texts.getComponents()) ((JComponent) c).setAlignmentX(LEFT_ALIGNMENT);

        add(boxWrap, BorderLayout.WEST);
        add(texts, BorderLayout.CENTER);
    }
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
