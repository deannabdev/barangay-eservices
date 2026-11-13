import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.Arrays;
import java.util.function.IntConsumer;

public class ProfileScreen extends JFrame {

    // ---- Colors ----
    private static final Color RED        = new Color(211, 47, 47);
    private static final Color RED_TOP    = new Color(214, 42, 42);
    private static final Color RED_BOTTOM = new Color(160, 28, 30);
    private static final Color MAROON     = new Color(150, 28, 31);
    private static final Color NAVY       = new Color(27, 27, 47);
    private static final Color GRAY       = new Color(125, 125, 135);

    private static final int W = 351, H = 775;
    private static final int M = 29, CW = W - 2 * M;   // side margin and card width

    private static Font font(int style, float size) {
        String[] names = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        String family = Arrays.asList(names).contains("Lexend") ? "Lexend" : "SansSerif";
        return new Font(family, style, 12).deriveFont(size);
    }

    private static void smooth(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
    }

    // ---- User data shown on the screen (call setUser to change) ----
    private final JLabel nameLabel    = new JLabel("Your Name", SwingConstants.CENTER);
    private final JLabel contactLabel = new JLabel("", SwingConstants.CENTER);

    public ProfileScreen() {
        super("Account Profile");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);

        BackgroundPanel root = new BackgroundPanel();
        root.setLayout(null);
        root.setPreferredSize(new Dimension(W, H));
        buildUI(root);
        setUser("Juan Dela Cruz", "juandelacruz@gmail.com", "09123456789");

        setContentPane(root);
        pack();
        setLocationRelativeTo(null);
    }

    /** Updates the name, email and phone shown in the profile card. */
    public void setUser(String name, String email, String phone) {
        nameLabel.setText(name);
        contactLabel.setText(email + " | " + phone);
    }

    private void buildUI(JPanel root) {
        // Header title
        JLabel title = new JLabel("Account Profile", SwingConstants.CENTER);
        title.setFont(font(Font.BOLD, 20f));
        title.setForeground(Color.WHITE);
        title.setBounds(0, 52, W, 36);
        root.add(title);

        // Profile card (background, then avatar + text on top)
        CardBox profileCard = new CardBox();
        profileCard.setBounds(M, 157, CW, 198);

        AvatarView avatar = new AvatarView(() -> comingSoon("Change profile photo"));
        avatar.setBounds(W / 2 - 70, 163, 140, 140);
        root.add(avatar);

        nameLabel.setFont(font(Font.BOLD, 12.5f));
        nameLabel.setForeground(MAROON);
        nameLabel.setBounds(M, 304, CW, 20);
        root.add(nameLabel);

        contactLabel.setFont(font(Font.BOLD, 10.5f));
        contactLabel.setForeground(MAROON);
        contactLabel.setBounds(M, 322, CW, 18);
        root.add(contactLabel);

        root.add(profileCard);

        // Menu rows
        MenuRow edit = new MenuRow("Edit profile information", MenuRow.Icon.NEWS, 42,
                () -> { new ProfileInfo().setVisible(true); dispose(); });
        edit.setBounds(M, 383, CW, 46);
        root.add(edit);

        MenuRow security = new MenuRow("Security & Log In", MenuRow.Icon.NEWS, 42,
                () -> { new Security().setVisible(true); dispose(); });
        security.setBounds(M, 435, CW, 46);
        root.add(security);

        MenuRow logout = new MenuRow("Log Out", MenuRow.Icon.EXIT, 28, this::logOut);
        logout.setBounds(M, 487, CW, 32);
        root.add(logout);

        // Active requests
        JLabel requests = new JLabel("Your Active Requests");
        requests.setFont(font(Font.BOLD, 12f));
        requests.setForeground(NAVY);
        requests.setBounds(M, 530, CW, 18);
        root.add(requests);

        String[][] data = {
                {"Barangay Clearance Request", "Ref: #BCS-2026-0894", "PENDING"},
                // add more: {"Title", "Ref: #...", "APPROVED"}
        };
        int y = 557;
        for (String[] r : data) {
            RequestCard card = new RequestCard(r[0], r[1], r[2]);
            card.setBounds(M, y, CW, 57);
            root.add(card);
            y += 63;
        }

        // Bottom navigation
        NavBar nav = new NavBar(4, this::navigate);
        nav.setBounds(0, 689, W, H - 689);
        root.add(nav);
    }

    private void navigate(int index) {
        // 0 Home, 1 E-Services, 2 Reports, 3 Guide, 4 Profile (this screen)
        if (index == 4) return;
        String[] names = {"Home", "E-Services", "Reports", "Guide"};
        // TODO: open the matching screen here and dispose() this one.
        comingSoon(names[index]);
    }

    private void logOut() {
        int answer = JOptionPane.showConfirmDialog(this, "Are you sure you want to log out?",
                "Log Out", JOptionPane.YES_NO_OPTION);
        if (answer == JOptionPane.YES_OPTION) {
            new LoginScreen().setVisible(true);
            dispose();
        }
    }

    private void comingSoon(String what) {
        JOptionPane.showMessageDialog(this, what + " - coming soon.");
    }

    // =====================================================================
    //  Custom components
    // =====================================================================

    /** White background with the dark-red curved header. */
    private static class BackgroundPanel extends JPanel {
        @Override protected void paintComponent(Graphics g0) {
            super.paintComponent(g0);
            Graphics2D g = (Graphics2D) g0.create();
            smooth(g);
            int w = getWidth();
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, w, getHeight());

            Path2D header = new Path2D.Double();
            header.moveTo(0, 0);
            header.lineTo(w, 0);
            header.lineTo(w, 62);
            header.quadTo(w / 2.0, 164, 0, 62);   // curve dips to about y=113 in the middle
            header.closePath();
            g.setPaint(new GradientPaint(0, 0, RED_TOP, 0, 110, RED_BOTTOM));
            g.fill(header);
            g.dispose();
        }
    }

    /** Rounded white card with a light border and a soft shadow underneath. h excludes the 4px shadow. */
    private static void drawCard(Graphics2D g, int w, int h, boolean hover) {
        for (int i = 3; i >= 1; i--) {
            g.setColor(new Color(0, 0, 0, 18));
            g.fill(new RoundRectangle2D.Double(0.5, i, w - 1, h, 14, 14));
        }
        g.setColor(hover ? new Color(250, 246, 246) : Color.WHITE);
        g.fill(new RoundRectangle2D.Double(0, 0, w, h, 14, 14));
        g.setColor(new Color(228, 228, 232));
        g.setStroke(new BasicStroke(1f));
        g.draw(new RoundRectangle2D.Double(0.5, 0.5, w - 1, h - 1, 14, 14));
    }

    /** Plain card used as the profile card background. */
    private static class CardBox extends JComponent {
        @Override protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            smooth(g);
            drawCard(g, getWidth(), getHeight() - 4, false);
            g.dispose();
        }
    }

    /** Maroon ring with a person outline and a small edit badge. */
    private static class AvatarView extends JComponent {
        private static final double BADGE_X = 107, BADGE_Y = 112;

        AvatarView(Runnable onEdit) {
            setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) {
                    if (Point2D.distance(e.getX(), e.getY(), BADGE_X, BADGE_Y) <= 11) onEdit.run();
                }
            });
            addMouseMotionListener(new MouseMotionAdapter() {
                @Override public void mouseMoved(MouseEvent e) {
                    boolean onBadge = Point2D.distance(e.getX(), e.getY(), BADGE_X, BADGE_Y) <= 11;
                    setCursor(Cursor.getPredefinedCursor(onBadge ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
                }
            });
        }

        @Override protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            smooth(g);

            // ring
            g.setColor(MAROON);
            g.setStroke(new BasicStroke(4f));
            g.draw(new Ellipse2D.Double(5, 5, 130, 130));

            // person (clipped to the inside of the ring)
            g.setClip(new Ellipse2D.Double(7, 7, 126, 126));
            g.setStroke(new BasicStroke(2f));
            g.draw(new Ellipse2D.Double(50, 24, 40, 40));                       // head
            g.draw(new Arc2D.Double(36, 68, 68, 64, 0, 180, Arc2D.OPEN));       // shoulders
            g.setClip(null);

            // edit badge
            g.setColor(new Color(0, 0, 0, 40));
            g.fill(new Ellipse2D.Double(BADGE_X - 10, BADGE_Y - 9, 22, 22));
            g.setColor(Color.WHITE);
            g.fill(new Ellipse2D.Double(BADGE_X - 10, BADGE_Y - 10, 20, 20));
            g.setColor(NAVY);
            g.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.draw(new Line2D.Double(BADGE_X - 3, BADGE_Y + 1.5, BADGE_X + 3, BADGE_Y - 4.5));   // pencil
            g.setStroke(new BasicStroke(1.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.draw(new Line2D.Double(BADGE_X - 5, BADGE_Y + 5, BADGE_X + 4, BADGE_Y + 5));       // underline
            g.dispose();
        }
    }

    /** Clickable card row: icon, text and a chevron. */
    private static class MenuRow extends JComponent {
        enum Icon { NEWS, EXIT }
        private final String text;
        private final Icon icon;
        private final int cardHeight;
        private boolean hover;

        MenuRow(String text, Icon icon, int cardHeight, Runnable onClick) {
            this.text = text;
            this.icon = icon;
            this.cardHeight = cardHeight;
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hover = true;  repaint(); }
                @Override public void mouseExited(MouseEvent e)  { hover = false; repaint(); }
                @Override public void mouseClicked(MouseEvent e) { onClick.run(); }
            });
        }

        @Override protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            smooth(g);
            int w = getWidth(), h = cardHeight;
            drawCard(g, w, h, hover);

            double cy = h / 2.0;
            g.setColor(NAVY);
            g.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            if (icon == Icon.NEWS) {
                double x = 14, y = cy - 8;
                g.draw(new RoundRectangle2D.Double(x, y, 17, 15, 3, 3));
                g.fill(new Rectangle2D.Double(x + 3, y + 3.5, 5, 4.5));
                g.draw(new Line2D.Double(x + 10, y + 4.5, x + 14, y + 4.5));
                g.draw(new Line2D.Double(x + 10, y + 7.5, x + 14, y + 7.5));
                g.draw(new Line2D.Double(x + 3, y + 11, x + 14, y + 11));
            } else {
                double x = 14, y = cy - 8;
                Path2D door = new Path2D.Double();
                door.moveTo(x + 9, y);
                door.lineTo(x + 16, y);
                door.lineTo(x + 16, y + 16);
                door.lineTo(x + 9, y + 16);
                g.draw(door);
                g.draw(new Line2D.Double(x, y + 8, x + 11, y + 8));
                g.draw(new Line2D.Double(x + 7, y + 4, x + 11, y + 8));
                g.draw(new Line2D.Double(x + 7, y + 12, x + 11, y + 8));
            }

            g.setFont(font(Font.PLAIN, 11.5f));
            FontMetrics fm = g.getFontMetrics();
            g.drawString(text, 42, (int) (cy - fm.getHeight() / 2.0 + fm.getAscent()));

            // chevron
            double cx = w - 22;
            g.setColor(MAROON);
            g.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            Path2D chevron = new Path2D.Double();
            chevron.moveTo(cx - 2.5, cy - 4.5);
            chevron.lineTo(cx + 2.5, cy);
            chevron.lineTo(cx - 2.5, cy + 4.5);
            g.draw(chevron);
            g.dispose();
        }
    }

    /** One active request: document icon, title, reference number and a status pill. */
    private static class RequestCard extends JComponent {
        private final String title, ref, status;

        RequestCard(String title, String ref, String status) {
            this.title = title;
            this.ref = ref;
            this.status = status;
        }

        @Override protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            smooth(g);
            int w = getWidth(), h = getHeight() - 4;
            drawCard(g, w, h, false);
            double cy = h / 2.0;

            // documents icon
            g.setColor(NAVY);
            g.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.draw(new RoundRectangle2D.Double(19, cy - 10, 11, 14, 3, 3));
            g.setColor(Color.WHITE);
            g.fill(new RoundRectangle2D.Double(15, cy - 6, 12, 16, 3, 3));
            g.setColor(NAVY);
            g.draw(new RoundRectangle2D.Double(15, cy - 6, 12, 16, 3, 3));
            g.draw(new Line2D.Double(18, cy + 1, 24, cy + 1));
            g.draw(new Line2D.Double(18, cy + 5, 24, cy + 5));

            // title + reference
            g.setColor(NAVY);
            g.setFont(font(Font.BOLD, 10f));
            FontMetrics fm = g.getFontMetrics();
            g.drawString(title, 44, (int) (cy - 3));
            g.setColor(GRAY);
            g.setFont(font(Font.PLAIN, 8.5f));
            g.drawString(ref, 44, (int) (cy + 12));

            // status pill
            Color bg, fg;
            switch (status.toUpperCase()) {
                case "APPROVED": bg = new Color(214, 240, 222); fg = new Color(30, 110, 55);  break;
                case "REJECTED": bg = new Color(250, 218, 218); fg = new Color(180, 30, 30);  break;
                default:         bg = new Color(255, 236, 179); fg = new Color(230, 140, 0);  break;
            }
            g.setFont(font(Font.BOLD, 8f));
            fm = g.getFontMetrics();
            int pw = fm.stringWidth(status) + 18, ph = 19;
            int px = w - pw - 12, py = (int) (cy - ph / 2.0);
            g.setColor(bg);
            g.fill(new RoundRectangle2D.Double(px, py, pw, ph, 8, 8));
            g.setColor(fg);
            g.drawString(status, px + (pw - fm.stringWidth(status)) / 2, py + (ph - fm.getHeight()) / 2 + fm.getAscent());
            g.dispose();
        }
    }

    /** Bottom navigation bar: Home, E-Services, Reports, Guide, Profile. */
    private static class NavBar extends JComponent {
        private static final String[] LABELS = {"Home", "E-Services", "Reports", "Guide", "Profile"};
        private final int active;

        NavBar(int active, IntConsumer onSelect) {
            this.active = active;
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) {
                    onSelect.accept(Math.min(4, e.getX() * 5 / getWidth()));
                }
            });
        }

        @Override protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            smooth(g);
            int w = getWidth();
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, w, getHeight());
            g.setColor(new Color(225, 225, 230));
            g.drawLine(0, 0, w, 0);

            for (int i = 0; i < 5; i++) {
                double cx = w * (i + 0.5) / 5;
                g.setColor(i == active ? RED : NAVY);
                g.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                drawIcon(g, i, cx, 25);

                g.setFont(font(i == active ? Font.BOLD : Font.PLAIN, 9f));
                FontMetrics fm = g.getFontMetrics();
                g.drawString(LABELS[i], (int) (cx - fm.stringWidth(LABELS[i]) / 2.0), 49);
            }
            g.dispose();
        }

        private void drawIcon(Graphics2D g, int index, double cx, double cy) {
            Path2D p = new Path2D.Double();
            switch (index) {
                case 0: // home
                    p.moveTo(cx - 10, cy - 1); p.lineTo(cx, cy - 10); p.lineTo(cx + 10, cy - 1);
                    g.draw(p);
                    p = new Path2D.Double();
                    p.moveTo(cx - 7.5, cy - 3.5); p.lineTo(cx - 7.5, cy + 9);
                    p.lineTo(cx + 7.5, cy + 9);   p.lineTo(cx + 7.5, cy - 3.5);
                    g.draw(p);
                    g.draw(new Rectangle2D.Double(cx - 2.5, cy + 3, 5, 6));
                    break;
                case 1: // e-services (document)
                    p.moveTo(cx - 7, cy - 10); p.lineTo(cx + 2, cy - 10); p.lineTo(cx + 8, cy - 4);
                    p.lineTo(cx + 8, cy + 10); p.lineTo(cx - 7, cy + 10); p.closePath();
                    g.draw(p);
                    p = new Path2D.Double();
                    p.moveTo(cx + 2, cy - 10); p.lineTo(cx + 2, cy - 4); p.lineTo(cx + 8, cy - 4);
                    g.draw(p);
                    g.draw(new Line2D.Double(cx - 3, cy + 1, cx + 4, cy + 1));
                    g.draw(new Line2D.Double(cx - 3, cy + 5, cx + 4, cy + 5));
                    break;
                case 2: // reports (warning triangle)
                    p.moveTo(cx, cy - 10); p.lineTo(cx + 10.5, cy + 8); p.lineTo(cx - 10.5, cy + 8); p.closePath();
                    g.draw(p);
                    g.draw(new Line2D.Double(cx, cy - 3, cx, cy + 2));
                    g.fill(new Ellipse2D.Double(cx - 1, cy + 3.5, 2, 2));
                    break;
                case 3: // guide (open book)
                    p.moveTo(cx, cy - 6);
                    p.quadTo(cx - 5, cy - 9, cx - 10, cy - 7);
                    p.lineTo(cx - 10, cy + 7);
                    p.quadTo(cx - 5, cy + 5, cx, cy + 9);
                    p.quadTo(cx + 5, cy + 5, cx + 10, cy + 7);
                    p.lineTo(cx + 10, cy - 7);
                    p.quadTo(cx + 5, cy - 9, cx, cy - 6);
                    g.draw(p);
                    g.draw(new Line2D.Double(cx, cy - 6, cx, cy + 9));
                    break;
                default: // profile
                    g.draw(new Ellipse2D.Double(cx - 10.5, cy - 10.5, 21, 21));
                    g.draw(new Ellipse2D.Double(cx - 3.5, cy - 7, 7, 7));
                    g.draw(new Arc2D.Double(cx - 6.5, cy + 1.5, 13, 12, 0, 180, Arc2D.OPEN));
            }
        }
    }

}