/**
 * GuideScreen.java - Citizen Guide - history, real OpenStreetMap map (MapPanel/MapDialog) and barangay officials.
 */
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.ArrayList;
import java.util.List;

class GuideScreen extends ScrollableScreen {
    @Override protected String getHeaderTitle()    { return "Citizen Guide"; }
    @Override protected AppSection createContent() { return new GuideContent(GuideData.getOfficials()); }
}

class GuideContent extends AppSection {
    private final List<Official> officials;

    GuideContent(List<Official> officials) { this.officials = officials; }

    @Override
    protected void buildUI() {
        setLayout(new BorderLayout());
        setBackground(Theme.BG);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(Theme.BG);
        content.setBorder(new EmptyBorder(14, 16, 16, 16));

        // --- history ---
        content.add(sectionTitle("Barangay History"));
        content.add(Box.createVerticalStrut(10));

        RoundedPanel history = new RoundedPanel(Color.WHITE, Theme.BORDER, 12);
        history.setLayout(new BoxLayout(history, BoxLayout.Y_AXIS));
        history.setBorder(new EmptyBorder(12, 12, 12, 12));
        JLabel text = new JLabel("<html><body width='315'>Established in the highly active city of Taguig, "
                + "Barangay Central Signal has consistently grown from a humble neighborhood into a bustling, dynamic "
                + "center of community life and commerce in the metropolis.</body></html>");
        text.setFont(Theme.regular(11));
        text.setForeground(Theme.TEXT_SUB);
        JLabel link = new JLabel("Read Full Barangay History \u2192");
        link.setFont(Theme.bold(10));
        link.setForeground(Theme.RED);
        link.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        link.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                JOptionPane.showMessageDialog(link, "Full Barangay History - coming soon",
                        "Barangay History", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        history.add(text);
        history.add(Box.createVerticalStrut(10));
        history.add(link);
        for (Component c : history.getComponents()) ((JComponent) c).setAlignmentX(LEFT_ALIGNMENT);
        content.add(history);

        // --- map ---
        content.add(Box.createVerticalStrut(18));
        content.add(sectionTitle("Barangay Boundaries Map"));
        content.add(Box.createVerticalStrut(10));
        content.add(new MapPanel());

        // --- officials ---
        content.add(Box.createVerticalStrut(18));
        content.add(sectionTitle("Barangay Officials (2025-2028)"));
        content.add(Box.createVerticalStrut(10));
        for (Official o : officials) {
            content.add(new OfficialCard(o));
            content.add(Box.createVerticalStrut(8));
        }

        for (Component c : content.getComponents()) ((JComponent) c).setAlignmentX(LEFT_ALIGNMENT);
        add(content, BorderLayout.NORTH);
    }

    private JLabel sectionTitle(String text) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.bold(13));
        l.setForeground(Theme.TEXT);
        return l;
    }
}

class OfficialCard extends RoundedPanel {
    OfficialCard(Official o) {
        super(Color.WHITE, Theme.BORDER, 12);
        setLayout(new BorderLayout(12, 0));
        setBorder(new EmptyBorder(10, 12, 10, 12));

        RoundedPanel avatar = new RoundedPanel(Theme.RED_SOFT, null, 32);
        avatar.setLayout(new GridBagLayout());
        avatar.setPreferredSize(new Dimension(32, 32));
        avatar.add(new IconLabel(IconType.PERSON, 14, Theme.RED));

        JLabel name = new JLabel(o.getName());
        name.setFont(Theme.bold(11));
        name.setForeground(Theme.TEXT);
        JLabel role = new JLabel(o.getPosition());
        role.setFont(Theme.regular(9));
        role.setForeground(Theme.TEXT_SUB);

        JPanel texts = new JPanel();
        texts.setOpaque(false);
        texts.setLayout(new BoxLayout(texts, BoxLayout.Y_AXIS));
        texts.add(name);
        texts.add(role);
        JPanel textWrap = new JPanel(new GridBagLayout());
        textWrap.setOpaque(false);
        GridBagConstraints gc = new GridBagConstraints();
        gc.anchor = GridBagConstraints.WEST;
        gc.weightx = 1;
        textWrap.add(texts, gc);

        add(Theme.wrap(avatar), BorderLayout.WEST);
        add(textWrap, BorderLayout.CENTER);
    }
}

/** Map locations. Change these numbers if you want to point the map somewhere else. */
final class TaguigMap {
    static final double BRGY_LAT = 14.5115, BRGY_LON = 121.0563;   // Barangay Central Signal Village (approx.)
    static final double CITY_LAT = 14.5200, CITY_LON = 121.0600;   // centre of the Taguig City preview
    private TaguigMap() {}
}

/**
 * A real map of Taguig City made from OpenStreetMap tiles (needs an internet connection).
 * Preview mode (Guide screen): small, click to open the big map.
 * Interactive mode (pop-up): drag to move, mouse wheel to zoom.
 */
class MapPanel extends JPanel {
    private static final int TILE = 256;
    private static final int MIN_ZOOM = 10, MAX_ZOOM = 18;
    private static final String USER_AGENT = "BarangayCentralSignalApp/1.0 (school project)";

    // tiles are shared by every map on screen, so each tile is downloaded only once
    private static final Map<String, BufferedImage> CACHE = new ConcurrentHashMap<>();
    private static final Set<String> LOADING = ConcurrentHashMap.newKeySet();
    private static final Set<String> FAILED = ConcurrentHashMap.newKeySet();
    private static final ExecutorService POOL = Executors.newFixedThreadPool(4, r -> {
        Thread t = new Thread(r, "map-tiles");
        t.setDaemon(true);
        return t;
    });

    private final boolean interactive;
    private final int startZoom;
    private final double startLat, startLon;
    private final Dimension size;
    private final double pinLat, pinLon;
    private final String pinLabel;

    private int zoom;
    private double lat, lon;

    MapPanel() {
        this(false, 12, TaguigMap.CITY_LAT, TaguigMap.CITY_LON, new Dimension(358, 140),
                TaguigMap.BRGY_LAT, TaguigMap.BRGY_LON, "Brgy. Central Signal");
    }

    MapPanel(boolean interactive, int zoom, double lat, double lon, Dimension size,
             double pinLat, double pinLon, String pinLabel) {
        this.interactive = interactive;
        this.pinLat = pinLat;
        this.pinLon = pinLon;
        this.pinLabel = pinLabel;
        this.startZoom = zoom;
        this.startLat = lat;
        this.startLon = lon;
        this.size = size;
        this.zoom = zoom;
        this.lat = lat;
        this.lon = lon;
        setOpaque(false);
        setPreferredSize(size);

        MouseAdapter ma = new MouseAdapter() {
            private Point last;

            @Override public void mousePressed(MouseEvent e) {
                last = e.getPoint();
                FAILED.clear();                      // lets the user retry after a failed download
                repaint();
            }
            @Override public void mouseClicked(MouseEvent e) {
                if (!interactive) MapDialog.show(MapPanel.this);
            }
            @Override public void mouseDragged(MouseEvent e) {
                if (!interactive || last == null) return;
                pan(last.x - e.getX(), last.y - e.getY());
                last = e.getPoint();
            }
            @Override public void mouseWheelMoved(java.awt.event.MouseWheelEvent e) {
                if (interactive) zoomBy(e.getWheelRotation() < 0 ? 1 : -1);
            }
        };
        addMouseListener(ma);
        addMouseMotionListener(ma);
        addMouseWheelListener(ma);
        setCursor(Cursor.getPredefinedCursor(interactive ? Cursor.MOVE_CURSOR : Cursor.HAND_CURSOR));
    }

    @Override public Dimension getMaximumSize() {
        return interactive ? size : new Dimension(Integer.MAX_VALUE, size.height);
    }

    /* ---------- controls (used by the pop-up's buttons) ---------- */
    void zoomBy(int delta) {
        zoom = Math.max(MIN_ZOOM, Math.min(MAX_ZOOM, zoom + delta));
        repaint();
    }

    void recenter() {
        zoom = startZoom;
        lat = startLat;
        lon = startLon;
        repaint();
    }

    private void pan(int dx, int dy) {
        double wx = worldX(lon, zoom) + dx;
        double wy = worldY(lat, zoom) + dy;
        lon = lonFromX(wx, zoom);
        lat = latFromY(wy, zoom);
        repaint();
    }

    /* ---------- Web-Mercator maths ---------- */
    private static double worldX(double lon, int z) {
        return (lon + 180.0) / 360.0 * TILE * (1 << z);
    }
    private static double worldY(double lat, int z) {
        double r = Math.toRadians(lat);
        return (1.0 - Math.log(Math.tan(r) + 1.0 / Math.cos(r)) / Math.PI) / 2.0 * TILE * (1 << z);
    }
    private static double lonFromX(double x, int z) {
        return x / (TILE * (double) (1 << z)) * 360.0 - 180.0;
    }
    private static double latFromY(double y, int z) {
        double n = Math.PI - 2.0 * Math.PI * y / (TILE * (double) (1 << z));
        return Math.toDegrees(Math.atan(Math.sinh(n)));
    }

    /* ---------- tile downloading ---------- */
    private void request(String key, int z, int x, int y) {
        if (!LOADING.add(key)) return;               // already being downloaded
        POOL.submit(() -> {
            try {
                HttpURLConnection c = (HttpURLConnection)
                        new URL("https://tile.openstreetmap.org/" + z + "/" + x + "/" + y + ".png").openConnection();
                c.setRequestProperty("User-Agent", USER_AGENT);   // required by the OpenStreetMap tile policy
                c.setConnectTimeout(8000);
                c.setReadTimeout(8000);
                BufferedImage img;
                try (java.io.InputStream in = c.getInputStream()) {
                    img = ImageIO.read(in);
                }
                if (img != null) CACHE.put(key, img); else FAILED.add(key);
            } catch (Exception ex) {
                FAILED.add(key);
            } finally {
                LOADING.remove(key);
                SwingUtilities.invokeLater(this::repaint);
            }
        });
    }

    /* ---------- painting ---------- */
    @Override
    protected void paintComponent(Graphics g0) {
        Graphics2D g = (Graphics2D) g0.create();
        Theme.smooth(g);
        int w = getWidth(), h = getHeight();
        g.setClip(new RoundRectangle2D.Double(0, 0, w, h, 12, 12));
        g.setColor(new Color(0xE8, 0xEE, 0xF2));
        g.fillRect(0, 0, w, h);

        double left = worldX(lon, zoom) - w / 2.0;
        double top = worldY(lat, zoom) - h / 2.0;
        int n = 1 << zoom;
        boolean failedTile = false, anyTile = false;
        for (int ty = (int) Math.floor(top / TILE); ty <= (int) Math.floor((top + h) / TILE); ty++) {
            for (int tx = (int) Math.floor(left / TILE); tx <= (int) Math.floor((left + w) / TILE); tx++) {
                if (tx < 0 || ty < 0 || tx >= n || ty >= n) continue;
                String key = zoom + "/" + tx + "/" + ty;
                BufferedImage img = CACHE.get(key);
                if (img != null) {
                    g.drawImage(img, (int) Math.round(tx * TILE - left), (int) Math.round(ty * TILE - top), null);
                    anyTile = true;
                } else if (FAILED.contains(key)) {
                    failedTile = true;
                } else {
                    request(key, zoom, tx, ty);
                }
            }
        }

        // pin (barangay hall in the Guide preview, or the selected business)
        double px = worldX(pinLon, zoom) - left;
        double py = worldY(pinLat, zoom) - top;
        drawPin(g, px, py);

        // messages
        if (failedTile && !anyTile) {
            drawChip(g, "Map could not load. Check your internet connection.", w / 2, h / 2, true);
        } else if (!anyTile) {
            drawChip(g, "Loading map...", w / 2, h / 2, true);
        }

        // overlays (preview only)
        if (!interactive) {
            drawChip(g, "TAGUIG CITY, METRO MANILA", w / 2, 14, true);
            g.setFont(Theme.bold(8));
            String label = "INTERACTIVE MAP";
            int pw = g.getFontMetrics().stringWidth(label) + 34, ph = 20;
            int pxl = w - pw - 10, pyl = 28;
            g.setColor(new Color(255, 255, 255, 240));
            g.fill(new RoundRectangle2D.Double(pxl, pyl, pw, ph, 12, 12));
            g.setColor(Theme.RED);
            g.setStroke(new BasicStroke(1.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            IconType.SEARCH.draw(g, pxl + 8, pyl + 4, 12);
            g.drawString(label, pxl + 24, pyl + 13);
        }

        // OpenStreetMap attribution (required by their licence)
        g.setFont(Theme.regular(8));
        String attr = "\u00A9 OpenStreetMap contributors";
        FontMetrics fm = g.getFontMetrics();
        int aw = fm.stringWidth(attr) + 8;
        g.setColor(new Color(255, 255, 255, 200));
        g.fillRect(w - aw, h - 12, aw, 12);
        g.setColor(new Color(0x37, 0x41, 0x51));
        g.drawString(attr, w - aw + 4, h - 3);
        g.dispose();
    }

    private void drawPin(Graphics2D g, double x, double y) {
        Path2D pin = new Path2D.Double();
        pin.moveTo(x, y);
        pin.curveTo(x - 12, y - 14, x - 9, y - 28, x, y - 28);
        pin.curveTo(x + 9, y - 28, x + 12, y - 14, x, y);
        g.setColor(Theme.RED);
        g.fill(pin);
        g.setColor(Color.WHITE);
        g.setStroke(new BasicStroke(1.2f));
        g.draw(pin);
        g.fill(new Ellipse2D.Double(x - 3.5, y - 21, 7, 7));
        if (zoom >= 12 && pinLabel != null) drawChip(g, pinLabel, (int) x, (int) y + 12, false);
    }

    /** Small white label with dark text, centred on (cx, cy). */
    private void drawChip(Graphics2D g, String text, int cx, int cy, boolean bold) {
        g.setFont(bold ? Theme.bold(8) : Theme.regular(8));
        FontMetrics fm = g.getFontMetrics();
        int tw = fm.stringWidth(text) + 12, th = 15;
        g.setColor(new Color(255, 255, 255, 235));
        g.fill(new RoundRectangle2D.Double(cx - tw / 2.0, cy - th / 2.0, tw, th, 10, 10));
        g.setColor(new Color(0x37, 0x41, 0x51));
        g.drawString(text, cx - tw / 2 + 6, cy + fm.getAscent() / 2 - 1);
    }
}

/** Pop-up with a big, draggable map. Used by the Guide (barangay) and the Directory (a business). */
final class MapDialog {
    private MapDialog() {}

    /** Default: Taguig City with the barangay pin. */
    static void show(Component parent) {
        show(parent, "Taguig City, Metro Manila", "Barangay Central Signal Village",
                TaguigMap.BRGY_LAT, TaguigMap.BRGY_LON, "Brgy. Central Signal", 15);
    }

    static void show(Component parent, String title, String subtitle,
                     double lat, double lon, String pinLabel, int zoom) {
        JDialog d = new JDialog(SwingUtilities.getWindowAncestor(parent), title,
                Dialog.ModalityType.APPLICATION_MODAL);

        MapPanel map = new MapPanel(true, zoom, lat, lon, new Dimension(380, 380), lat, lon, pinLabel);

        JLabel heading = new JLabel(title);
        heading.setFont(Theme.bold(14));
        heading.setForeground(Theme.TEXT);
        JLabel sub = new JLabel(subtitle);
        sub.setFont(Theme.regular(11));
        sub.setForeground(Theme.TEXT_SUB);
        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);
        top.add(heading);
        top.add(Box.createVerticalStrut(2));
        top.add(sub);

        JLabel hint = new JLabel("Drag to move  \u00B7  mouse wheel or +/- to zoom");
        hint.setFont(Theme.regular(10));
        hint.setForeground(Theme.MUTED);

        RoundedButton plus = small("+");
        plus.addActionListener(e -> map.zoomBy(1));
        RoundedButton minus = small("\u2212");
        minus.addActionListener(e -> map.zoomBy(-1));
        RoundedButton center = new RoundedButton("Recenter", Theme.RED_SOFT, Theme.RED);
        center.setPreferredSize(new Dimension(86, 30));
        center.setFont(Theme.bold(11));
        center.addActionListener(e -> map.recenter());
        RoundedButton browser = new RoundedButton("Open in browser", new Color(0xF3, 0xF4, 0xF6), Theme.TEXT);
        browser.setPreferredSize(new Dimension(124, 30));
        browser.setFont(Theme.bold(11));
        browser.addActionListener(e -> openInBrowser(d, lat, lon));

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        controls.setOpaque(false);
        controls.add(plus);
        controls.add(minus);
        controls.add(center);
        controls.add(browser);

        JPanel bottom = new JPanel();
        bottom.setOpaque(false);
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));
        hint.setAlignmentX(Component.LEFT_ALIGNMENT);
        controls.setAlignmentX(Component.LEFT_ALIGNMENT);
        bottom.add(hint);
        bottom.add(Box.createVerticalStrut(8));
        bottom.add(controls);

        JPanel root = new JPanel(new BorderLayout(0, 10));
        root.setBackground(Color.WHITE);
        root.setBorder(new EmptyBorder(14, 14, 14, 14));
        root.add(top, BorderLayout.NORTH);
        root.add(map, BorderLayout.CENTER);
        root.add(bottom, BorderLayout.SOUTH);

        d.setContentPane(root);
        d.pack();
        d.setResizable(false);
        d.setLocationRelativeTo(parent);
        d.setVisible(true);
    }

    private static RoundedButton small(String text) {
        RoundedButton b = new RoundedButton(text, Theme.RED, Color.WHITE);
        b.setPreferredSize(new Dimension(34, 30));
        b.setFont(Theme.bold(14));
        return b;
    }

    private static void openInBrowser(Component parent, double lat, double lon) {
        String url = "https://www.openstreetmap.org/?mlat=" + lat + "&mlon=" + lon
                + "#map=17/" + lat + "/" + lon;
        try {
            Desktop.getDesktop().browse(new URI(url));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(parent, "Could not open the browser.\n" + url,
                    "Open in browser", JOptionPane.WARNING_MESSAGE);
        }
    }
}

/** Sample data for this screen. */
class GuideData {
    static List<Official> getOfficials() {
        List<Official> list = new ArrayList<>();
        list.add(new Official("Hon. Christopher P. Ballecer", "Punong Barangay / Chairman"));
        list.add(new Official("Hon. Michelle T. Santos", "Sangguniang Barangay Member"));
        list.add(new Official("Hon. Renaldo G. Ramos", "Sangguniang Barangay Member"));
        list.add(new Official("Hon. Maria Clara V. Cruz", "Sangguniang Barangay Member"));
        return list;
    }
}
