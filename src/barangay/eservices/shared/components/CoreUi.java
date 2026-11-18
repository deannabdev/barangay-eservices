package components;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;

class BackButton extends JButton {
    BackButton(int size, Runnable action) {
        setPreferredSize(new Dimension(size, size));
        setMinimumSize(new Dimension(size, size));
        setMaximumSize(new Dimension(size, size));
        getAccessibleContext().setAccessibleName("Back");
        setToolTipText("Back");
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addActionListener(event -> {
            if (action != null) action.run();
        });
    }

    @Override
    protected void paintComponent(Graphics g0) {
        Graphics2D g = (Graphics2D) g0.create();
        Theme.smooth(g);
        double cx = getWidth() / 2.0;
        double cy = getHeight() / 2.0;
        Path2D arrow = new Path2D.Double();
        arrow.moveTo(cx + 8, cy);
        arrow.lineTo(cx - 8, cy);
        arrow.moveTo(cx - 8, cy);
        arrow.lineTo(cx, cy - 8);
        arrow.moveTo(cx - 8, cy);
        arrow.lineTo(cx, cy + 8);
        g.setColor(Color.WHITE);
        g.setStroke(new BasicStroke(2.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(arrow);
        g.dispose();
    }
}

class Divider extends JComponent {
    Divider() {
        setOpaque(false);
        setPreferredSize(new Dimension(1, 1));
        setMinimumSize(new Dimension(1, 1));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
    }

    @Override protected void paintComponent(Graphics g) {
        g.setColor(Theme.BORDER);
        g.drawLine(0, 0, getWidth(), 0);
    }
}

class ShadowLabel extends JLabel {
    private final Color shadowColor;

    ShadowLabel(String text, Color shadowColor) {
        super(text);
        this.shadowColor = shadowColor;
        setOpaque(false);
    }

    @Override protected void paintComponent(Graphics g) {
        Graphics2D shadow = (Graphics2D) g.create();
        Theme.smooth(shadow);
        shadow.setFont(getFont());
        shadow.setColor(shadowColor);
        FontMetrics metrics = shadow.getFontMetrics();
        Insets insets = getInsets();
        int x = switch (getHorizontalAlignment()) {
            case CENTER -> (getWidth() - metrics.stringWidth(getText())) / 2;
            case RIGHT, TRAILING -> getWidth() - insets.right - metrics.stringWidth(getText());
            default -> insets.left;
        };
        int y = switch (getVerticalAlignment()) {
            case TOP -> insets.top + metrics.getAscent();
            case BOTTOM -> getHeight() - insets.bottom - metrics.getDescent();
            default -> (getHeight() - metrics.getHeight()) / 2 + metrics.getAscent();
        };
        shadow.drawString(getText(), x + 2, y + 2);
        shadow.dispose();
        super.paintComponent(g);
    }
}

class RoundedButton extends JButton {
    private final Color backgroundColor;

    RoundedButton(String text) { this(text, Theme.RED, Color.WHITE); }

    RoundedButton(String text, Color backgroundColor, Color foregroundColor) {
        super(text);
        this.backgroundColor = backgroundColor;
        setForeground(foregroundColor);
        setFont(Theme.bold(12));
        setMargin(new Insets(7, 14, 7, 14));
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);
        setRolloverEnabled(true);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    @Override protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        Theme.smooth(g2);
        ButtonModel model = getModel();
        Color fill = backgroundColor;
        if (!isEnabled()) {
            fill = new Color(fill.getRed(), fill.getGreen(), fill.getBlue(), 120);
        } else if (model.isPressed()) {
            fill = fill.darker();
        } else if (model.isRollover()) {
            fill = fill.brighter();
        }
        g2.setColor(fill);
        g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
        g2.dispose();
        super.paintComponent(g);
    }
}

class IconLabel extends JComponent {
    private final IconType type;
    private Color color;

    IconLabel(IconType type, int size, Color color) {
        this.type = type;
        this.color = color;
        setPreferredSize(new Dimension(size, size));
        setMinimumSize(new Dimension(size, size));
    }

    void setColor(Color color) {
        this.color = color;
        repaint();
    }

    @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        Theme.smooth(g2);
        type.draw(g2, getWidth(), color);
        g2.dispose();
    }
}

class RoundedPanel extends JPanel {
    private final Color bg;
    private final Color border;
    private final int radius;

    RoundedPanel(Color bg, Color border, int radius) {
        this.bg = bg;
        this.border = border;
        this.radius = radius;
        setOpaque(false);
    }

    @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        Theme.smooth(g2);
        if (bg != null) {
            g2.setColor(bg);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
        }
        if (border != null) {
            g2.setColor(border);
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
        }
        g2.dispose();
    }

    @Override protected void paintBorder(Graphics g) {}
}

class Badge extends RoundedPanel {
    Badge(String text, Color bg, Color fg, int radius) {
        this(text, bg, fg, null, radius);
    }

    Badge(String text, Color bg, Color fg, IconType icon, int radius) {
        super(bg, null, radius);
        setLayout(new FlowLayout(FlowLayout.LEFT, icon == null ? 0 : 4, 0));
        if (icon != null) add(new IconLabel(icon, 12, fg));
        JLabel l = new JLabel(text);
        l.setFont(Theme.bold(8));
        l.setForeground(fg);
        setBorder(new EmptyBorder(2, 8, 2, 8));
        add(l);
    }
}

public final class CoreUi {
    private CoreUi() {}

    public interface NavListener {
        void onNavigate(String screen);
    }

    public abstract static class AppSection extends JPanel {
        protected abstract void buildUI();

        public final JPanel init() {
            buildUI();
            return this;
        }
    }

    public abstract static class ScrollableScreen extends AppSection {
        protected abstract String getHeaderTitle();
        protected abstract AppSection createContent();
        protected Runnable getBackAction() { return null; }
        protected boolean hasHeader() { return true; }
        protected JComponent getFixedHeader() { return null; }
        protected Color getBackgroundColor() { return Theme.BG; }

        @Override
        protected final void buildUI() {
            setLayout(new BorderLayout());
            setBackground(getBackgroundColor());

            if (hasHeader()) {
                JPanel header = new JPanel(new BorderLayout(10, 0));
                header.setBackground(Color.WHITE);
                header.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER),
                        new EmptyBorder(8, 16, 10, 16)));

                JLabel title = new JLabel(getHeaderTitle());
                title.setFont(Theme.bold(14));
                title.setForeground(Theme.TEXT);
                header.add(title, BorderLayout.CENTER);

                Runnable backAction = getBackAction();
                if (backAction != null) {
                    JButton backButton = new JButton("\u2039");
                    backButton.setFont(Theme.bold(24));
                    backButton.setForeground(Theme.TEXT);
                    backButton.setFocusPainted(false);
                    backButton.setContentAreaFilled(false);
                    backButton.setBorderPainted(false);
                    backButton.addActionListener(event -> backAction.run());
                    header.add(backButton, BorderLayout.WEST);
                }
                add(header, BorderLayout.NORTH);
            }

            JComponent fixedHeader = getFixedHeader();
            if (fixedHeader != null) add(fixedHeader, BorderLayout.NORTH);

            JScrollPane scroll = new JScrollPane(createContent().init());
            scroll.setBorder(null);
            scroll.getVerticalScrollBar().setUnitIncrement(16);
            scroll.getViewport().setBackground(getBackgroundColor());
            scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            add(scroll, BorderLayout.CENTER);
        }
    }

    public static class Navigator implements NavListener {
        private final CardLayout cards;
        private final JPanel center;
        private final List<NavListener> listeners = new ArrayList<>();

        public Navigator(CardLayout cards, JPanel center) {
            this.cards = cards;
            this.center = center;
        }

        @Override
        public void onNavigate(String screen) {
            cards.show(center, screen);
            for (NavListener listener : listeners) listener.onNavigate(screen);
        }

        public void attach(NavListener listener) { listeners.add(listener); }
        public void detach(NavListener listener) { listeners.remove(listener); }
    }

    public static final class HomeComponents extends components.HomeComponents {
        private HomeComponents() {}
    }

    public static final class DirectoryComponents extends components.DirectoryComponents {
        private DirectoryComponents() {}
    }

    public static class Badge extends components.Badge {
        public Badge(String text, Color background, Color foreground, int radius) {
            super(text, background, foreground, radius);
        }
    }

    public static class Divider extends components.Divider {
        public Divider() {}
    }

    public static class IconLabel extends components.IconLabel {
        public IconLabel(IconType type, int size, Color color) {
            super(type, size, color);
        }
    }

    public static class RoundedButton extends components.RoundedButton {
        public RoundedButton(String text) {
            super(text);
        }

        public RoundedButton(String text, Color background, Color foreground) {
            super(text, background, foreground);
        }
    }

    public static class RoundedPanel extends components.RoundedPanel {
        public RoundedPanel(Color background, Color border, int radius) {
            super(background, border, radius);
        }
    }

    public static final class ReportsComponents {
        private ReportsComponents() {}

        public static class ReportIcon extends components.ReportsComponents.ReportIcon {
            public enum Type { PLUS, STREETLIGHT_OFF, TRASH, TREE, GAVEL }

            public ReportIcon(Type type, int size, Color color) {
                super(components.ReportsComponents.ReportIcon.Type.valueOf(type.name()), size, color);
            }
        }
    }

    public static final class TransportComponents {
        private TransportComponents() {}

        public static class NoticeBanner extends components.TransportComponents.NoticeBanner {
            public NoticeBanner(String lead, String message) {
                super(lead, message);
            }
        }

        public static class ServiceCard extends components.TransportComponents.ServiceCard {
            public ServiceCard(Icon icon, String title, String description, String action, Runnable onClick) {
                super(icon, title, description, action, onClick);
            }
        }
    }
}
