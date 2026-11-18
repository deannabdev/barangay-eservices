package components;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.*;
import java.util.Locale;
import java.util.function.Consumer;

class HomeComponents {
    public static final Color PRIMARY = new Color(0xD32F2F);
    public static final Color PRIMARY_DARK = new Color(0xC62828);
    public static final Color PRIMARY_SOFT = new Color(0xFDECEC);
    public static final Color PRIMARY_BORDER = new Color(0xF5A3A3);
    public static final Color BACKGROUND = new Color(0xF8F9FB);
    public static final Color CARD = Color.WHITE;
    public static final Color CARD_BORDER = new Color(0xE5E7EB);
    public static final Color CARD_HOVER = new Color(0xF3F4F6);
    public static final Color TEXT_DARK = new Color(0x111827);
    public static final Color TEXT_BODY = new Color(0x4B5563);
    public static final Color TEXT_MUTED = new Color(0x4B5563);
    public static final int W = 351;
    public static final int H = 775;
    public static final int CONTENT_WIDTH = W - 48;

    HomeComponents() {}

    public static Font font(int style, int size) {
        return new Font("Segoe UI", style, size);
    }

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

    public static class VectorIcon implements Icon {
        public enum Type { SHIELD, TRUCK, DOCUMENT, ALERT_CIRCLE, ALERT_TRIANGLE, PHONE, BOOK, BUILDING, HOME, USER, CHEVRON }

        private final Type type;
        private final int size;
        private Color color;

        public VectorIcon(Type type, int size, Color color) {
            this.type = type;
            this.size = size;
            this.color = color;
        }

        public void setColor(Color color) { this.color = color; }

        @Override public int getIconWidth() { return size; }
        @Override public int getIconHeight() { return size; }

        @Override public void paintIcon(Component c, Graphics g, int x, int y) {
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

        @Override protected void paintComponent(Graphics g) {
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

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            antialias(g2);
            int x = (getWidth() - box) / 2;
            int y = (getHeight() - box) / 2;
            g2.setColor(bg);
            g2.fill(new RoundRectangle2D.Double(x, y, box, box, arc, arc));
            icon.paintIcon(this, g2, x + (box - icon.getIconWidth()) / 2, y + (box - icon.getIconHeight()) / 2);
            g2.dispose();
        }
    }

    public static class SectionTitle extends JLabel {
        public SectionTitle(String text) {
            super(text);
            setFont(font(Font.BOLD, 13));
            setForeground(TEXT_DARK);
            setAlignmentX(Component.LEFT_ALIGNMENT);
        }
    }

    public static class ScrollablePanel extends JPanel implements Scrollable {
        public ScrollablePanel() {
            setOpaque(false);
        }

        @Override public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
        @Override public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) { return 16; }
        @Override public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) { return visibleRect.height; }
        @Override public boolean getScrollableTracksViewportWidth() { return true; }
        @Override public boolean getScrollableTracksViewportHeight() { return false; }
    }

    public static class HeaderBar extends JPanel {
        public HeaderBar(String title, String subtitle) { this(title, subtitle, null); }

        public HeaderBar(String title, String subtitle, Runnable onBack) {
            setLayout(new BorderLayout(10, 0));
            setBackground(Color.WHITE);
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, CARD_BORDER),
                    BorderFactory.createEmptyBorder(12, 16, 12, 16)));

            JComponent leading;
            if (onBack == null) {
                leading = new IconTile(40, 40, PRIMARY_SOFT, new VectorIcon(VectorIcon.Type.SHIELD, 22, PRIMARY));
            } else {
                leading = new IconTile(40, 40, PRIMARY_SOFT,
                        new TransportComponents.TransportIcon(TransportComponents.TransportIcon.Type.ARROW_LEFT, 20, PRIMARY));
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
                @Override public void mouseExited(MouseEvent e) { setFill(CARD); }
            });
        }
    }

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

            JLabel titleLabel = new JLabel("<html><body width='" + (CONTENT_WIDTH - 32) + "'>" + title + "</body></html>");
            titleLabel.setFont(font(Font.BOLD, 14));
            titleLabel.setForeground(TEXT_DARK);
            titleLabel.setAlignmentX(LEFT_ALIGNMENT);

            add(top);
            add(Box.createVerticalStrut(10));
            add(titleLabel);
            makeClickable(this, onClick);
        }
    }
}

final class ReportsComponents {
    private ReportsComponents() {}

    public static final Color ICON_CIRCLE = new Color(0xFEF2F2);
    public static final Color STATUS_PENDING_BG = new Color(0xFEF3C7);
    public static final Color STATUS_PENDING_FG = new Color(0xD97706);

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

        @Override public int getIconWidth() { return size; }
        @Override public int getIconHeight() { return size; }

        @Override public void paintIcon(Component c, Graphics g, int x, int y) {
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
                    g2.draw(new Line2D.Double(3.5, 3.5, 20.5, 20.5));
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

    public static class IntroBlock extends JPanel {
        public IntroBlock(String title, String description) {
            setOpaque(false);
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

            JLabel t = new JLabel(title);
            t.setFont(HomeComponents.font(Font.BOLD, 18));
            t.setForeground(HomeComponents.TEXT_DARK);
            t.setAlignmentX(LEFT_ALIGNMENT);

            JLabel d = new JLabel("<html><div style='width:" + (HomeComponents.CONTENT_WIDTH - 4) + "px'>" + description + "</div></html>");
            d.setFont(HomeComponents.font(Font.PLAIN, 12));
            d.setForeground(HomeComponents.TEXT_BODY);
            d.setAlignmentX(LEFT_ALIGNMENT);

            add(t);
            add(Box.createVerticalStrut(6));
            add(d);
        }
    }

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
            add(new JLabel(new HomeComponents.VectorIcon(HomeComponents.VectorIcon.Type.CHEVRON, 16, Color.WHITE)), BorderLayout.EAST);
            HomeComponents.makeClickable(this, onClick);
        }
    }

    public static class CategoryCard extends HomeComponents.RoundedPanel {
        public CategoryCard(Icon icon, String label, Runnable onClick) {
            super(14, HomeComponents.CARD, HomeComponents.CARD_BORDER);
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setBorder(BorderFactory.createEmptyBorder(10, 4, 10, 4));
            setPreferredSize(new Dimension(100, 96));

            HomeComponents.IconTile tile = new HomeComponents.IconTile(40, 40, ICON_CIRCLE, icon);
            tile.setAlignmentX(CENTER_ALIGNMENT);

            String html = "<html><div style='text-align:center'>" + label.replace("&", "&amp;").replace("\n", "<br>") + "</div></html>";
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
                @Override public void mouseExited(MouseEvent e) { setFill(HomeComponents.CARD); }
            });
        }
    }

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

    public static class ActiveReportCard extends HomeComponents.RoundedPanel {
        public ActiveReportCard(Icon icon, String title, String reference, String status, Color statusFg, Color statusBg, Runnable onClick) {
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

        @Override public void paintIcon(Component c, Graphics g, int x, int y) {
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

            HomeComponents.RoundedPanel sos = new HomeComponents.RoundedPanel(999, HomeComponents.PRIMARY_SOFT, HomeComponents.PRIMARY_BORDER);
            sos.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
            sos.add(new JLabel(new HomeComponents.VectorIcon(HomeComponents.VectorIcon.Type.TRUCK, 16, HomeComponents.PRIMARY)));
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
            add(new JLabel(new HomeComponents.VectorIcon(HomeComponents.VectorIcon.Type.ALERT_TRIANGLE, 18, HomeComponents.PRIMARY_DARK)), BorderLayout.WEST);
            add(text, BorderLayout.CENTER);

            Runnable updateTextWidth = () -> {
                int textWidth = getWidth() > 0 ? Math.max(100, getWidth() - 72) : HomeComponents.CONTENT_WIDTH - 72;
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

        private String escapeHtml(String value) {
            return value == null ? "" : value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
        }
    }

    public static class ServiceCard extends HomeComponents.RoundedPanel {
        public ServiceCard(Icon icon, String title, String description, String action, Runnable onClick) {
            super(14, HomeComponents.CARD, HomeComponents.CARD_BORDER);
            setLayout(new BorderLayout(12, 0));
            setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 12));

            HomeComponents.IconTile tile = new HomeComponents.IconTile(44, 12, HomeComponents.PRIMARY_SOFT, icon);
            JLabel titleLabel = new JLabel(title);
            titleLabel.setFont(HomeComponents.font(Font.BOLD, 13));
            titleLabel.setForeground(HomeComponents.TEXT_DARK);
            titleLabel.setAlignmentX(LEFT_ALIGNMENT);
            JLabel descriptionLabel = new JLabel(description);
            descriptionLabel.setFont(HomeComponents.font(Font.PLAIN, 11));
            descriptionLabel.setForeground(HomeComponents.TEXT_MUTED);
            descriptionLabel.setAlignmentX(LEFT_ALIGNMENT);
            RoundedButton actionButton = new RoundedButton(action + " →", HomeComponents.PRIMARY, Color.WHITE);
            actionButton.setFont(HomeComponents.font(Font.BOLD, 10));
            actionButton.setMargin(new Insets(5, 10, 5, 10));
            actionButton.setAlignmentX(LEFT_ALIGNMENT);
            actionButton.addActionListener(e -> { if (onClick != null) onClick.run(); });

            JPanel text = new JPanel();
            text.setOpaque(false);
            text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
            text.add(titleLabel);
            text.add(Box.createVerticalStrut(2));
            text.add(descriptionLabel);
            text.add(Box.createVerticalStrut(10));
            text.add(actionButton);

            add(tile, BorderLayout.WEST);
            add(text, BorderLayout.CENTER);
            HomeComponents.makeClickable(this, onClick);
        }
    }
}

class DirectoryComponents {
    DirectoryComponents() {}

    public static final Color STAR_AMBER = new Color(0xF59E0B);

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

        @Override public int getIconWidth() { return size; }
        @Override public int getIconHeight() { return size; }

        @Override public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
            g2.translate(x, y);
            double s = size / 24.0;
            g2.scale(s, s);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            switch (type) {
                case SEARCH:
                    g2.draw(new Ellipse2D.Double(3, 3, 15, 15));
                    g2.draw(new Line2D.Double(16, 16, 21, 21));
                    break;
                case STORE:
                    g2.draw(new Line2D.Double(3, 9, 5, 4));
                    g2.draw(new Line2D.Double(5, 4, 19, 4));
                    g2.draw(new Line2D.Double(19, 4, 21, 9));
                    g2.draw(new Line2D.Double(3, 9, 21, 9));
                    g2.draw(new Line2D.Double(4, 10, 4, 21));
                    g2.draw(new Line2D.Double(20, 10, 20, 21));
                    g2.draw(new Line2D.Double(4, 21, 20, 21));
                    g2.draw(new Line2D.Double(9, 9, 9, 12));
                    g2.draw(new Line2D.Double(15, 9, 15, 12));
                    g2.draw(new Rectangle2D.Double(10, 15, 4, 6));
                    g2.draw(new Rectangle2D.Double(6, 14, 2, 3));
                    break;
                case STAR:
                    Path2D star = new Path2D.Double();
                    for (int i = 0; i < 10; i++) {
                        double a = Math.PI / 2 + i * Math.PI / 5;
                        double r1 = i % 2 == 0 ? 10 : 4;
                        double x1 = 12 + Math.cos(a) * r1;
                        double y1 = 12 + Math.sin(a) * r1;
                        if (i == 0) star.moveTo(x1, y1); else star.lineTo(x1, y1);
                    }
                    star.closePath();
                    g2.draw(star);
                    break;
                case MAP_PIN:
                    Path2D pin = new Path2D.Double();
                    pin.moveTo(12, 3);
                    pin.curveTo(7, 3, 4, 7, 4, 10.5);
                    pin.curveTo(4, 16, 12, 21, 12, 21);
                    pin.curveTo(12, 21, 20, 16, 20, 10.5);
                    pin.curveTo(20, 7, 17, 3, 12, 3);
                    pin.closePath();
                    g2.draw(pin);
                    g2.fill(new Ellipse2D.Double(9, 8.5, 6, 6));
                    break;
            }
            g2.dispose();
        }
    }

    public static class HintTextField extends JTextField {
        HintTextField(String hint) {
            setText("");
            setOpaque(false);
            setBorder(null);
            setForeground(HomeComponents.TEXT_BODY);
            setFont(HomeComponents.font(Font.PLAIN, 11));
            setCaretColor(HomeComponents.PRIMARY);
            setToolTipText(hint);
            setDocument(new HintDocument());
        }

        private static class HintDocument extends PlainDocument {
            @Override public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
                super.insertString(offs, str, a);
            }
        }
    }

    public static class SearchBar extends HomeComponents.RoundedPanel {
        public SearchBar(String hint, Consumer<String> onChange) {
            super(14, Color.WHITE, HomeComponents.CARD_BORDER);
            setLayout(new BorderLayout(8, 0));
            setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));

            JLabel icon = new JLabel(new DirectoryIcon(DirectoryIcon.Type.SEARCH, 14, HomeComponents.TEXT_MUTED));
            JTextField field = new HintTextField(hint);
            field.getDocument().addDocumentListener(new DocumentListener() {
                public void insertUpdate(DocumentEvent e) { call(); }
                public void removeUpdate(DocumentEvent e) { call(); }
                public void changedUpdate(DocumentEvent e) { call(); }
                private void call() {
                    if (onChange != null) onChange.accept(field.getText());
                }
            });
            add(icon, BorderLayout.WEST);
            add(field, BorderLayout.CENTER);
        }
    }

    public static class FilterChips extends JPanel {
        public FilterChips(String[] labels, String selected, Consumer<String> onSelect) {
            setOpaque(false);
            setLayout(new FlowLayout(FlowLayout.LEFT, 4, 0));
            setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));
            for (String label : labels) {
                Chip chip = new Chip(label);
                chip.setSelected(label.equals(selected));
                MouseAdapter selectChip = new MouseAdapter() {
                    @Override public void mouseClicked(MouseEvent e) {
                        for (Component component : getComponents()) {
                            Chip candidate = (Chip) component;
                            candidate.setSelected(candidate == chip);
                        }
                        if (onSelect != null) onSelect.accept(label);
                    }
                };
                chip.addMouseListener(selectChip);
                chip.text.addMouseListener(selectChip);
                add(chip);
            }
        }

        private static class Chip extends JPanel {
            private final JLabel text;
            private boolean selected;

            Chip(String label) {
                setOpaque(false);
                setLayout(new BorderLayout());
                setBorder(BorderFactory.createEmptyBorder(5, 8, 5, 8));
                text = new JLabel(label);
                text.setFont(HomeComponents.font(Font.PLAIN, 11));
                add(text, BorderLayout.CENTER);
            }

            void setSelected(boolean selected) {
                this.selected = selected;
                text.setForeground(selected ? Color.WHITE : HomeComponents.TEXT_BODY);
                repaint();
            }

            @Override protected void paintComponent(Graphics g) {
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

    public static class BusinessCard extends HomeComponents.RoundedPanel {
        public BusinessCard(String name, String type, boolean open, String address, double rating, Runnable onClick) {
            super(14, HomeComponents.CARD, HomeComponents.CARD_BORDER);
            setLayout(new BorderLayout(10, 0));
            setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

            HomeComponents.IconTile tile = new HomeComponents.IconTile(40, 12, HomeComponents.PRIMARY_SOFT,
                    new DirectoryIcon(DirectoryIcon.Type.STORE, 22, HomeComponents.PRIMARY));

            JLabel nameLabel = new JLabel(name);
            nameLabel.setFont(HomeComponents.font(Font.BOLD, 12).deriveFont(11.5f));
            nameLabel.setForeground(HomeComponents.TEXT_DARK);
            nameLabel.setAlignmentX(LEFT_ALIGNMENT);

            String status = open ? "<font color='#16A34A'><b>Open Now</b></font>" : "<b>Closed</b>";
            JLabel statusLabel = new JLabel("<html>" + type.replace("&", "&amp;") + " · " + status + "</html>");
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

    public static class ListPanel extends JPanel {
        public ListPanel() {
            setOpaque(false);
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setAlignmentX(LEFT_ALIGNMENT);
        }

        @Override public Dimension getMaximumSize() {
            return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
        }
    }
}
