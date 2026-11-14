/**
 * ReportsScreen.java - Reports - concern categories, "Submit a New Report" form with photo, active reports.
 */
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.*;
import javax.imageio.ImageIO;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

class ReportsScreen extends ScrollableScreen {
    @Override protected String getHeaderTitle()    { return "Barangay Reports"; }
    @Override protected AppSection createContent() { return new ReportsContent(); }
}

class ReportsContent extends AppSection {
    private final List<ConcernCategory> categories = ReportsData.getCategories();
    private final List<ActiveReport> reports = new ArrayList<>(ReportsData.getActiveReports());
    private final JPanel reportList = new JPanel();
    private int nextNumber = 895;   // next reference number (0894 is the sample report)

    @Override
    protected void buildUI() {
        setLayout(new BorderLayout());
        setBackground(Theme.BG);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(Theme.BG);
        content.setBorder(new EmptyBorder(16, 16, 16, 16));

        JLabel heading = new JLabel("Help Us Improve Our Community");
        heading.setFont(Theme.bold(19));
        heading.setForeground(Theme.TEXT);

        JLabel desc = new JLabel("<html><body width='310'>File municipal and community concerns directly "
                + "to Barangay Central Signal officers. We prioritize resolution for public safety.</body></html>");
        desc.setFont(Theme.regular(12));
        desc.setForeground(Theme.TEXT_SUB);

        content.add(heading);
        content.add(Box.createVerticalStrut(6));
        content.add(desc);
        content.add(Box.createVerticalStrut(16));
        content.add(new SubmitBanner(() -> openForm(null)));
        content.add(Box.createVerticalStrut(18));
        content.add(sectionTitle("Select Concern Category"));
        content.add(Box.createVerticalStrut(10));

        JPanel grid = new JPanel(new GridLayout(2, 3, 10, 10));
        grid.setOpaque(false);
        for (ConcernCategory c : categories) grid.add(new CategoryCard(c, name -> openForm(name)));
        grid.setPreferredSize(new Dimension(358, 178));
        grid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 178));
        content.add(grid);

        content.add(Box.createVerticalStrut(18));
        content.add(sectionTitle("Your Active Reports"));
        content.add(Box.createVerticalStrut(10));
        reportList.setOpaque(false);
        reportList.setLayout(new BoxLayout(reportList, BoxLayout.Y_AXIS));
        content.add(reportList);
        refreshReports();

        for (Component c : content.getComponents()) ((JComponent) c).setAlignmentX(LEFT_ALIGNMENT);
        add(content, BorderLayout.NORTH);
    }

    /** Opens the report form; when submitted, the new report is added to "Your Active Reports". */
    private void openForm(String preselectedCategory) {
        List<String> names = new ArrayList<>();
        for (ConcernCategory c : categories) names.add(c.getName());

        ReportDraft draft = new ReportFormDialog(names, preselectedCategory).show(this);
        if (draft == null) return;                       // cancelled

        IconType icon = IconType.WARNING;
        for (ConcernCategory c : categories) {
            if (c.getName().equals(draft.getCategory())) icon = c.getIcon();
        }
        String ref = String.format("Ref: #BCS-2026-%04d", nextNumber++);
        reports.add(0, new ActiveReport(draft.getCategory() + " - " + draft.getLocation(),
                ref, ReportStatus.PENDING, icon, draft));
        refreshReports();

        JOptionPane.showMessageDialog(this,
                "Report submitted!\n\n" + ref + "\nCategory: " + draft.getCategory()
                        + "\nLocation: " + draft.getLocation()
                        + "\nPhoto: " + (draft.getPhoto() == null ? "none" : draft.getPhoto().getName()),
                "Submit a New Report", JOptionPane.INFORMATION_MESSAGE);
    }

    private void refreshReports() {
        reportList.removeAll();
        for (ActiveReport r : reports) {
            ReportCard card = new ReportCard(r);
            card.setAlignmentX(LEFT_ALIGNMENT);
            reportList.add(card);
            reportList.add(Box.createVerticalStrut(8));
        }
        reportList.revalidate();
        reportList.repaint();
    }

    private JLabel sectionTitle(String text) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.bold(13));
        l.setForeground(Theme.TEXT);
        return l;
    }
}

class SubmitBanner extends RoundedPanel {
    SubmitBanner(Runnable onClick) {
        super(new Color(0xD8, 0x2E, 0x2E), null, 16);
        setLayout(new BorderLayout(12, 0));
        setBorder(new EmptyBorder(12, 14, 12, 14));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        RoundedPanel circle = new RoundedPanel(Theme.RED_DARK, null, 32);
        circle.setLayout(new GridBagLayout());
        circle.setPreferredSize(new Dimension(32, 32));
        circle.add(new IconLabel(IconType.PLUS, 14, Color.WHITE));

        JLabel title = new JLabel("Submit a New Report");
        title.setFont(Theme.bold(13));
        title.setForeground(Color.WHITE);
        JLabel sub = new JLabel("Attach photo, location, and details");
        sub.setFont(Theme.regular(10));
        sub.setForeground(new Color(0xFF, 0xD4, 0xD4));

        JPanel texts = new JPanel();
        texts.setOpaque(false);
        texts.setLayout(new BoxLayout(texts, BoxLayout.Y_AXIS));
        texts.add(title);
        texts.add(sub);
        JPanel textWrap = new JPanel(new GridBagLayout());
        textWrap.setOpaque(false);
        GridBagConstraints gc = new GridBagConstraints();
        gc.anchor = GridBagConstraints.WEST;
        gc.weightx = 1;
        textWrap.add(texts, gc);

        add(Theme.wrap(circle), BorderLayout.WEST);
        add(textWrap, BorderLayout.CENTER);
        add(Theme.wrap(new IconLabel(IconType.CHEVRON, 16, Color.WHITE)), BorderLayout.EAST);

        addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { onClick.run(); }
        });
    }
}

/** Receives the category the user tapped. */
interface CategoryListener {
    void onCategory(String categoryName);
}

class CategoryCard extends RoundedPanel {
    private final ConcernCategory category;

    CategoryCard(ConcernCategory category, CategoryListener listener) {
        super(Color.WHITE, Theme.BORDER, 14);
        this.category = category;
        setLayout(new GridBagLayout());
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        RoundedPanel circle = new RoundedPanel(new Color(0xFD, 0xEE, 0xEE), null, 36);
        circle.setLayout(new GridBagLayout());
        circle.setPreferredSize(new Dimension(36, 36));
        circle.add(new IconLabel(category.getIcon(), 16, Theme.RED));

        JLabel name = new JLabel("<html><body width='72'><center>" + category.getName() + "</center></body></html>",
                SwingConstants.CENTER);
        name.setFont(Theme.bold(10));
        name.setForeground(Theme.TEXT);

        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0;
        gc.insets = new Insets(0, 0, 8, 0);
        add(circle, gc);
        gc.insets = new Insets(0, 0, 0, 0);
        add(name, gc);

        addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                listener.onCategory(CategoryCard.this.category.getName());
            }
        });
    }

    @Override
    public Dimension getMaximumSize() { return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE); }
}

class ReportCard extends RoundedPanel {
    ReportCard(ActiveReport report) {
        super(Color.WHITE, Theme.BORDER, 14);
        setLayout(new BorderLayout(10, 0));
        setBorder(new EmptyBorder(12, 12, 12, 12));

        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        JLabel title = new JLabel("<html><body width='215'>" + report.getTitle() + "</body></html>");
        title.setFont(Theme.bold(11));
        title.setForeground(Theme.TEXT);
        JLabel ref = new JLabel(report.getReference());
        ref.setFont(Theme.regular(9));
        ref.setForeground(Theme.MUTED);

        JPanel texts = new JPanel();
        texts.setOpaque(false);
        texts.setLayout(new BoxLayout(texts, BoxLayout.Y_AXIS));
        texts.add(title);
        texts.add(ref);
        JPanel textWrap = new JPanel(new GridBagLayout());
        textWrap.setOpaque(false);
        GridBagConstraints gc = new GridBagConstraints();
        gc.anchor = GridBagConstraints.WEST;
        gc.weightx = 1;
        textWrap.add(texts, gc);

        ReportStatus st = report.getStatus();
        add(Theme.wrap(new IconLabel(report.getIcon(), 15, Theme.RED)), BorderLayout.WEST);
        add(textWrap, BorderLayout.CENTER);
        add(Theme.wrap(new Badge(st.getLabel(), st.getBg(), st.getFg(), null, 8)), BorderLayout.EAST);

        addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { ReportDetailsDialog.show(ReportCard.this, report); }
        });
    }
}

final class ImageUtil {
    private ImageUtil() {}

    /** Loads an image file and scales it down to fit inside maxW x maxH. Returns null if it can't be read. */
    static ImageIcon scaled(File file, int maxW, int maxH) {
        try {
            BufferedImage img = ImageIO.read(file);
            if (img == null) return null;
            double scale = Math.min(1.0, Math.min((double) maxW / img.getWidth(), (double) maxH / img.getHeight()));
            int w = Math.max(1, (int) (img.getWidth() * scale));
            int h = Math.max(1, (int) (img.getHeight() * scale));
            return new ImageIcon(img.getScaledInstance(w, h, Image.SCALE_SMOOTH));
        } catch (Exception e) {
            return null;
        }
    }
}

/** The "Submit a New Report" form: category, location, details and an optional photo attachment. */
class ReportFormDialog {
    private final List<String> categories;
    private final String preselected;

    private final JComboBox<String> categoryBox = new JComboBox<>();
    private final PlaceholderField locationField = new PlaceholderField("e.g. Zone 2, Ballecer St.");
    private final JTextArea detailsArea = new JTextArea(4, 24);
    private final JLabel photoPreview = new JLabel();
    private final JLabel photoName = new JLabel("No photo attached");
    private final JLabel removeLink = new JLabel("Remove");

    private JDialog dialog;
    private File photo;             // null until the user attaches one
    private ReportDraft result;     // stays null if the user cancels

    ReportFormDialog(List<String> categories, String preselected) {
        this.categories = categories;
        this.preselected = preselected;
    }

    /** Shows the form (modal). Returns the filled-in report, or null if cancelled. */
    ReportDraft show(Component parent) {
        dialog = new JDialog(SwingUtilities.getWindowAncestor(parent), "Submit a New Report",
                Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setContentPane(buildForm());
        dialog.pack();
        dialog.setResizable(false);
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
        return result;
    }

    private JPanel buildForm() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(Color.WHITE);
        p.setBorder(new EmptyBorder(18, 18, 18, 18));

        JLabel title = new JLabel("Submit a New Report");
        title.setFont(Theme.bold(16));
        title.setForeground(Theme.TEXT);
        JLabel sub = new JLabel("Attach photo, location, and details");
        sub.setFont(Theme.regular(11));
        sub.setForeground(Theme.MUTED);
        p.add(title);
        p.add(sub);

        // category
        for (String c : categories) categoryBox.addItem(c);
        if (preselected != null) categoryBox.setSelectedItem(preselected);
        categoryBox.setFont(Theme.regular(11));
        categoryBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        p.add(section("Concern Category"));
        p.add(categoryBox);

        // location
        p.add(section("Location"));
        p.add(fieldBox(locationField));

        // details
        detailsArea.setLineWrap(true);
        detailsArea.setWrapStyleWord(true);
        detailsArea.setFont(Theme.regular(11));
        detailsArea.setOpaque(false);
        detailsArea.setBorder(null);
        JScrollPane sp = new JScrollPane(detailsArea);
        sp.setBorder(null);
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        sp.setPreferredSize(new Dimension(300, 80));
        p.add(section("Details"));
        p.add(fieldBox(sp));

        // photo
        p.add(section("Photo (optional)"));
        RoundedButton attach = new RoundedButton("Attach Photo", Theme.RED_SOFT, Theme.RED);
        attach.setPreferredSize(new Dimension(120, 30));
        attach.setFont(Theme.bold(11));
        attach.addActionListener(e -> choosePhoto());

        photoName.setFont(Theme.regular(10));
        photoName.setForeground(Theme.MUTED);
        removeLink.setFont(Theme.bold(10));
        removeLink.setForeground(Theme.RED);
        removeLink.setVisible(false);
        removeLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        removeLink.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { clearPhoto(); }
        });

        JPanel photoRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        photoRow.setOpaque(false);
        photoRow.add(attach);
        photoRow.add(photoName);
        photoRow.add(removeLink);
        photoRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        p.add(photoRow);

        photoPreview.setBorder(new EmptyBorder(8, 0, 0, 0));
        p.add(photoPreview);

        // buttons
        p.add(Box.createVerticalStrut(14));
        RoundedButton cancel = new RoundedButton("Cancel", new Color(0xF3, 0xF4, 0xF6), Theme.TEXT);
        cancel.setPreferredSize(new Dimension(90, 36));
        cancel.addActionListener(e -> dialog.dispose());
        RoundedButton submit = new RoundedButton("Submit Report");
        submit.setPreferredSize(new Dimension(140, 36));
        submit.addActionListener(e -> submit());
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttons.setOpaque(false);
        buttons.add(cancel);
        buttons.add(submit);
        buttons.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        p.add(buttons);

        for (Component c : p.getComponents()) ((JComponent) c).setAlignmentX(Component.LEFT_ALIGNMENT);
        return p;
    }

    private JLabel section(String text) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.bold(11));
        l.setForeground(Theme.TEXT);
        l.setBorder(new EmptyBorder(12, 0, 4, 0));
        return l;
    }

    private RoundedPanel fieldBox(JComponent inner) {
        RoundedPanel box = new RoundedPanel(Color.WHITE, Theme.BORDER, 10);
        box.setLayout(new BorderLayout());
        box.setBorder(new EmptyBorder(8, 10, 8, 10));
        box.add(inner, BorderLayout.CENTER);
        return box;
    }

    private void choosePhoto() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Attach Photo");
        chooser.setFileFilter(new FileNameExtensionFilter("Images (jpg, png, gif)", "jpg", "jpeg", "png", "gif"));
        if (chooser.showOpenDialog(dialog) != JFileChooser.APPROVE_OPTION) return;

        File f = chooser.getSelectedFile();
        ImageIcon icon = ImageUtil.scaled(f, 200, 130);
        if (icon == null) {
            JOptionPane.showMessageDialog(dialog, "That file could not be read as an image.",
                    "Attach Photo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        photo = f;
        photoPreview.setIcon(icon);
        photoName.setText(f.getName());
        removeLink.setVisible(true);
        dialog.pack();
    }

    private void clearPhoto() {
        photo = null;
        photoPreview.setIcon(null);
        photoName.setText("No photo attached");
        removeLink.setVisible(false);
        dialog.pack();
    }

    private void submit() {
        String location = locationField.getText().trim();
        String details = detailsArea.getText().trim();
        if (location.isEmpty() || details.isEmpty()) {
            JOptionPane.showMessageDialog(dialog, "Please enter the location and the details of your concern.",
                    "Submit a New Report", JOptionPane.WARNING_MESSAGE);
            return;
        }
        result = new ReportDraft((String) categoryBox.getSelectedItem(), location, details, photo);
        dialog.dispose();
    }
}

/** Pop-up that shows everything the user submitted for a report (including the attached photo). */
final class ReportDetailsDialog {
    private ReportDetailsDialog() {}

    static void show(Component parent, ActiveReport report) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));

        p.add(boldLabel(report.getTitle()));
        p.add(new JLabel(report.getReference() + "   \u2022   " + report.getStatus().getLabel()));

        ReportDraft d = report.getDraft();
        if (d != null) {
            p.add(Box.createVerticalStrut(8));
            p.add(boldLabel("Category:"));
            p.add(new JLabel(d.getCategory()));
            p.add(Box.createVerticalStrut(6));
            p.add(boldLabel("Location:"));
            p.add(new JLabel(d.getLocation()));
            p.add(Box.createVerticalStrut(6));
            p.add(boldLabel("Details:"));
            p.add(new JLabel("<html><body width='260'>" + d.getDetails() + "</body></html>"));
            p.add(Box.createVerticalStrut(6));
            p.add(boldLabel("Photo:"));
            ImageIcon icon = d.getPhoto() == null ? null : ImageUtil.scaled(d.getPhoto(), 260, 170);
            p.add(icon != null ? new JLabel(icon) : new JLabel("No photo attached"));
        }

        for (Component c : p.getComponents()) ((JComponent) c).setAlignmentX(Component.LEFT_ALIGNMENT);
        JOptionPane.showMessageDialog(parent, p, "Report Details", JOptionPane.INFORMATION_MESSAGE);
    }

    private static JLabel boldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(l.getFont().deriveFont(Font.BOLD));
        return l;
    }
}

/** Sample data for this screen. */
class ReportsData {
    static List<ConcernCategory> getCategories() {
        List<ConcernCategory> list = new ArrayList<>();
        list.add(new ConcernCategory("Broken Streetlight", IconType.BULB));
        list.add(new ConcernCategory("Uncollected Garbage", IconType.TRASH));
        list.add(new ConcernCategory("Pothole Concerns", IconType.WARNING));
        list.add(new ConcernCategory("Tree Trimming", IconType.TRIANGLE));
        list.add(new ConcernCategory("Blotters & Records", IconType.DOC));
        list.add(new ConcernCategory("Civic Complaints", IconType.GAVEL));
        return list;
    }

    static List<ActiveReport> getActiveReports() {
        List<ActiveReport> list = new ArrayList<>();
        list.add(new ActiveReport("Uncollected Garbage - Zone 2", "Ref: #BCS-2026-0894", ReportStatus.PENDING, IconType.TRASH, null));
        return list;
    }
}
