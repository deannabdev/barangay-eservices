/**
 * UiKit.java - Shared look: colours/fonts (Theme), icons (IconType) and reusable components.
 */
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.*;

enum IconType {
    SHIELD {
        void draw(Graphics2D g, int x, int y, int s) {
            Path2D p = new Path2D.Double();
            p.moveTo(x + s * .5, y);
            p.lineTo(x + s * .92, y + s * .18);
            p.lineTo(x + s * .92, y + s * .52);
            p.quadTo(x + s * .92, y + s * .84, x + s * .5, y + s);
            p.quadTo(x + s * .08, y + s * .84, x + s * .08, y + s * .52);
            p.lineTo(x + s * .08, y + s * .18);
            p.closePath();
            g.draw(p);
        }
    },
    DOC {
        void draw(Graphics2D g, int x, int y, int s) {
            g.draw(new RoundRectangle2D.Double(x + s * .15, y, s * .7, s, 3, 3));
            g.draw(new Line2D.Double(x + s * .32, y + s * .35, x + s * .68, y + s * .35));
            g.draw(new Line2D.Double(x + s * .32, y + s * .55, x + s * .68, y + s * .55));
            g.draw(new Line2D.Double(x + s * .32, y + s * .75, x + s * .55, y + s * .75));
        }
    },
    CLOCK {
        void draw(Graphics2D g, int x, int y, int s) {
            g.draw(new Ellipse2D.Double(x, y, s, s));
            g.draw(new Line2D.Double(x + s * .5, y + s * .25, x + s * .5, y + s * .5));
            g.draw(new Line2D.Double(x + s * .5, y + s * .5, x + s * .7, y + s * .62));
        }
    },
    INFO {
        void draw(Graphics2D g, int x, int y, int s) {
            g.draw(new Ellipse2D.Double(x, y, s, s));
            g.draw(new Line2D.Double(x + s * .5, y + s * .45, x + s * .5, y + s * .72));
            g.fill(new Ellipse2D.Double(x + s * .5 - 1, y + s * .26, 2, 2));
        }
    },
    HOME {
        void draw(Graphics2D g, int x, int y, int s) {
            Path2D p = new Path2D.Double();
            p.moveTo(x + s * .05, y + s * .48);
            p.lineTo(x + s * .5, y + s * .08);
            p.lineTo(x + s * .95, y + s * .48);
            g.draw(p);
            Path2D b = new Path2D.Double();
            b.moveTo(x + s * .17, y + s * .4);
            b.lineTo(x + s * .17, y + s * .92);
            b.lineTo(x + s * .83, y + s * .92);
            b.lineTo(x + s * .83, y + s * .4);
            g.draw(b);
            g.draw(new Rectangle2D.Double(x + s * .4, y + s * .6, s * .2, s * .32));
        }
    },
    WARNING {
        void draw(Graphics2D g, int x, int y, int s) {
            Path2D p = new Path2D.Double();
            p.moveTo(x + s * .5, y + s * .06);
            p.lineTo(x + s * .96, y + s * .9);
            p.lineTo(x + s * .04, y + s * .9);
            p.closePath();
            g.draw(p);
            g.draw(new Line2D.Double(x + s * .5, y + s * .38, x + s * .5, y + s * .62));
            g.fill(new Ellipse2D.Double(x + s * .5 - 1, y + s * .72, 2, 2));
        }
    },
    TRIANGLE {
        void draw(Graphics2D g, int x, int y, int s) {
            Path2D p = new Path2D.Double();
            p.moveTo(x + s * .5, y + s * .06);
            p.lineTo(x + s * .96, y + s * .9);
            p.lineTo(x + s * .04, y + s * .9);
            p.closePath();
            g.draw(p);
        }
    },
    BOOK {
        void draw(Graphics2D g, int x, int y, int s) {
            Path2D p = new Path2D.Double();
            p.moveTo(x + s * .5, y + s * .22);
            p.quadTo(x + s * .25, y + s * .08, x + s * .04, y + s * .16);
            p.lineTo(x + s * .04, y + s * .82);
            p.quadTo(x + s * .25, y + s * .74, x + s * .5, y + s * .88);
            p.quadTo(x + s * .75, y + s * .74, x + s * .96, y + s * .82);
            p.lineTo(x + s * .96, y + s * .16);
            p.quadTo(x + s * .75, y + s * .08, x + s * .5, y + s * .22);
            g.draw(p);
            g.draw(new Line2D.Double(x + s * .5, y + s * .22, x + s * .5, y + s * .88));
        }
    },
    USER {
        void draw(Graphics2D g, int x, int y, int s) {
            g.draw(new Ellipse2D.Double(x, y, s, s));
            g.draw(new Ellipse2D.Double(x + s * .36, y + s * .24, s * .28, s * .28));
            g.draw(new Arc2D.Double(x + s * .24, y + s * .56, s * .52, s * .5, 0, 180, Arc2D.OPEN));
        }
    },
    SOS {
        void draw(Graphics2D g, int x, int y, int s) {
            g.fill(new Ellipse2D.Double(x + s * .38, y + s * .38, s * .24, s * .24));
            g.draw(new Arc2D.Double(x + s * .2, y + s * .2, s * .6, s * .6, 120, 120, Arc2D.OPEN));
            g.draw(new Arc2D.Double(x + s * .2, y + s * .2, s * .6, s * .6, -60, 120, Arc2D.OPEN));
            g.draw(new Arc2D.Double(x, y, s, s, 125, 110, Arc2D.OPEN));
            g.draw(new Arc2D.Double(x, y, s, s, -55, 110, Arc2D.OPEN));
        }
    },
    BULB {
        void draw(Graphics2D g, int x, int y, int s) {
            g.draw(new Ellipse2D.Double(x + s * .2, y + s * .05, s * .6, s * .6));
            g.draw(new Line2D.Double(x + s * .38, y + s * .78, x + s * .62, y + s * .78));
            g.draw(new Line2D.Double(x + s * .42, y + s * .92, x + s * .58, y + s * .92));
            g.draw(new Line2D.Double(x + s * .05, y + s * .05, x + s * .95, y + s * .95)); // "broken" slash
        }
    },
    TRASH {
        void draw(Graphics2D g, int x, int y, int s) {
            g.draw(new Line2D.Double(x + s * .1, y + s * .22, x + s * .9, y + s * .22));
            g.draw(new Rectangle2D.Double(x + s * .36, y + s * .06, s * .28, s * .16));
            Path2D p = new Path2D.Double();
            p.moveTo(x + s * .2, y + s * .3);
            p.lineTo(x + s * .27, y + s * .94);
            p.lineTo(x + s * .73, y + s * .94);
            p.lineTo(x + s * .8, y + s * .3);
            g.draw(p);
            g.draw(new Line2D.Double(x + s * .42, y + s * .46, x + s * .42, y + s * .78));
            g.draw(new Line2D.Double(x + s * .58, y + s * .46, x + s * .58, y + s * .78));
        }
    },
    GAVEL {
        void draw(Graphics2D g0, int x, int y, int s) {
            Graphics2D g = (Graphics2D) g0.create();
            g.rotate(Math.PI / 4, x + s / 2.0, y + s / 2.0);
            g.draw(new RoundRectangle2D.Double(x + s * .22, y + s * .08, s * .56, s * .28, 3, 3));
            g.draw(new Line2D.Double(x + s * .5, y + s * .36, x + s * .5, y + s * .96));
            g.dispose();
        }
    },
    PLUS {
        void draw(Graphics2D g, int x, int y, int s) {
            g.draw(new Line2D.Double(x + s * .5, y + s * .1, x + s * .5, y + s * .9));
            g.draw(new Line2D.Double(x + s * .1, y + s * .5, x + s * .9, y + s * .5));
        }
    },
    CHEVRON {
        void draw(Graphics2D g, int x, int y, int s) {
            Path2D p = new Path2D.Double();
            p.moveTo(x + s * .35, y + s * .15);
            p.lineTo(x + s * .7, y + s * .5);
            p.lineTo(x + s * .35, y + s * .85);
            g.draw(p);
        }
    },
    TRUCK {
        void draw(Graphics2D g, int x, int y, int s) {
            g.draw(new Rectangle2D.Double(x + s * .04, y + s * .22, s * .56, s * .5));
            Path2D cab = new Path2D.Double();
            cab.moveTo(x + s * .6, y + s * .38);
            cab.lineTo(x + s * .82, y + s * .38);
            cab.lineTo(x + s * .96, y + s * .55);
            cab.lineTo(x + s * .96, y + s * .72);
            cab.lineTo(x + s * .6, y + s * .72);
            g.draw(cab);
            g.draw(new Line2D.Double(x + s * .32, y + s * .36, x + s * .32, y + s * .58)); // medical cross
            g.draw(new Line2D.Double(x + s * .21, y + s * .47, x + s * .43, y + s * .47));
            g.draw(new Ellipse2D.Double(x + s * .16, y + s * .72, s * .18, s * .18));
            g.draw(new Ellipse2D.Double(x + s * .68, y + s * .72, s * .18, s * .18));
        }
    },
    USERS {
        void draw(Graphics2D g, int x, int y, int s) {
            g.draw(new Ellipse2D.Double(x + s * .14, y + s * .1, s * .3, s * .3));
            g.draw(new Arc2D.Double(x + s * .02, y + s * .52, s * .54, s * .56, 0, 180, Arc2D.OPEN));
            g.draw(new Arc2D.Double(x + s * .6, y + s * .14, s * .26, s * .26, 90, 180, Arc2D.OPEN)); // 2nd person head
            g.draw(new Arc2D.Double(x + s * .6, y + s * .54, s * .36, s * .5, 0, 100, Arc2D.OPEN));
        }
    },
    BOX {
        void draw(Graphics2D g, int x, int y, int s) {
            Path2D p = new Path2D.Double();
            p.moveTo(x + s * .5, y + s * .04);
            p.lineTo(x + s * .92, y + s * .27);
            p.lineTo(x + s * .92, y + s * .73);
            p.lineTo(x + s * .5, y + s * .96);
            p.lineTo(x + s * .08, y + s * .73);
            p.lineTo(x + s * .08, y + s * .27);
            p.closePath();
            g.draw(p);
            Path2D q = new Path2D.Double();
            q.moveTo(x + s * .08, y + s * .27);
            q.lineTo(x + s * .5, y + s * .5);
            q.lineTo(x + s * .92, y + s * .27);
            g.draw(q);
            g.draw(new Line2D.Double(x + s * .5, y + s * .5, x + s * .5, y + s * .96));
        }
    },
    CIRCLE_X {
        void draw(Graphics2D g, int x, int y, int s) {
            g.draw(new Ellipse2D.Double(x, y, s, s));
            g.draw(new Line2D.Double(x + s * .34, y + s * .34, x + s * .66, y + s * .66));
            g.draw(new Line2D.Double(x + s * .66, y + s * .34, x + s * .34, y + s * .66));
        }
    },
    STORE {
        void draw(Graphics2D g, int x, int y, int s) {
            Path2D awning = new Path2D.Double();
            awning.moveTo(x + s * .12, y + s * .08);
            awning.lineTo(x + s * .88, y + s * .08);
            awning.lineTo(x + s * .98, y + s * .4);
            awning.lineTo(x + s * .02, y + s * .4);
            awning.closePath();
            g.draw(awning);
            Path2D body = new Path2D.Double();
            body.moveTo(x + s * .12, y + s * .42);
            body.lineTo(x + s * .12, y + s * .94);
            body.lineTo(x + s * .88, y + s * .94);
            body.lineTo(x + s * .88, y + s * .42);
            g.draw(body);
            g.draw(new Rectangle2D.Double(x + s * .4, y + s * .62, s * .2, s * .32));
        }
    },
    STAR {
        void draw(Graphics2D g, int x, int y, int s) {
            Path2D p = new Path2D.Double();
            double cx = x + s / 2.0, cy = y + s * .54, ro = s * .52, ri = s * .22;
            for (int i = 0; i < 10; i++) {
                double r = (i % 2 == 0) ? ro : ri;
                double a = -Math.PI / 2 + i * Math.PI / 5;
                double px = cx + r * Math.cos(a), py = cy + r * Math.sin(a);
                if (i == 0) p.moveTo(px, py); else p.lineTo(px, py);
            }
            p.closePath();
            g.fill(p);
            g.draw(p);
        }
    },
    MAGNIFIER {
        void draw(Graphics2D g, int x, int y, int s) {
            g.draw(new Ellipse2D.Double(x + s * .05, y + s * .05, s * .65, s * .65));
            g.draw(new Line2D.Double(x + s * .6, y + s * .6, x + s * .95, y + s * .95));
        }
    },
    PERSON {
        void draw(Graphics2D g, int x, int y, int s) {
            g.draw(new Ellipse2D.Double(x + s * .3, y + s * .08, s * .4, s * .4));
            g.draw(new Arc2D.Double(x + s * .12, y + s * .56, s * .76, s * .8, 0, 180, Arc2D.OPEN));
        }
    },
    SEARCH {
        void draw(Graphics2D g, int x, int y, int s) {
            g.draw(new Ellipse2D.Double(x + s * .05, y + s * .05, s * .6, s * .6));
            g.draw(new Line2D.Double(x + s * .56, y + s * .56, x + s * .95, y + s * .95));
            g.draw(new Line2D.Double(x + s * .35, y + s * .24, x + s * .35, y + s * .46)); // zoom +
            g.draw(new Line2D.Double(x + s * .24, y + s * .35, x + s * .46, y + s * .35));
        }
    },
    PHONE {
        void draw(Graphics2D g, int x, int y, int s) {
            Path2D p = new Path2D.Double();
            p.moveTo(x + s * .22, y + s * .08);
            p.quadTo(x + s * .08, y + s * .08, x + s * .1, y + s * .26);
            p.quadTo(x + s * .2, y + s * .78, x + s * .74, y + s * .9);
            p.quadTo(x + s * .92, y + s * .92, x + s * .92, y + s * .78);
            p.lineTo(x + s * .78, y + s * .62);
            p.lineTo(x + s * .62, y + s * .7);
            p.quadTo(x + s * .4, y + s * .62, x + s * .3, y + s * .38);
            p.lineTo(x + s * .38, y + s * .22);
            p.closePath();
            g.draw(p);
        }
    },
    BUILDING {
        void draw(Graphics2D g, int x, int y, int s) {
            g.draw(new RoundRectangle2D.Double(x + s * .18, y + s * .04, s * .64, s * .92, 3, 3));
            for (int r = 0; r < 3; r++) {
                g.draw(new Rectangle2D.Double(x + s * .32, y + s * (.16 + r * .2), s * .1, s * .08));
                g.draw(new Rectangle2D.Double(x + s * .58, y + s * (.16 + r * .2), s * .1, s * .08));
            }
            g.draw(new Rectangle2D.Double(x + s * .42, y + s * .74, s * .16, s * .22));
        }
    },
    ALERT_CIRCLE {
        void draw(Graphics2D g, int x, int y, int s) {
            g.draw(new Ellipse2D.Double(x, y, s, s));
            g.draw(new Line2D.Double(x + s * .5, y + s * .26, x + s * .5, y + s * .56));
            g.fill(new Ellipse2D.Double(x + s * .5 - 1, y + s * .68, 2, 2));
        }
    },
    ARROW_LEFT {
        void draw(Graphics2D g, int x, int y, int s) {
            g.draw(new Line2D.Double(x + s * .15, y + s * .5, x + s * .88, y + s * .5));
            Path2D p = new Path2D.Double();
            p.moveTo(x + s * .45, y + s * .2);
            p.lineTo(x + s * .15, y + s * .5);
            p.lineTo(x + s * .45, y + s * .8);
            g.draw(p);
        }
    };

    abstract void draw(Graphics2D g, int x, int y, int s);
}

final class Theme {
    static final Color RED = new Color(0xDC, 0x2F, 0x2F);
    static final Color RED_DARK = new Color(0xA8, 0x1C, 0x1C);
    static final Color DEEP_RED = new Color(0x84, 0x1B, 0x1B);
    static final Color RED_SOFT = new Color(0xFD, 0xE8, 0xE8);
    static final Color BG = new Color(0xF8, 0xF9, 0xFB);
    static final Color WHITE = Color.WHITE;
    static final Color BORDER = new Color(0xE5, 0xE7, 0xEB);
    static final Color TEXT = new Color(0x11, 0x18, 0x27);
    static final Color TEXT_SUB = new Color(0x4B, 0x55, 0x63);
    static final Color MUTED = new Color(0x6B, 0x72, 0x80);
    static final Color GREEN = new Color(0x16, 0xA3, 0x4A);
    static final Color GREEN_SOFT = new Color(0xDC, 0xFC, 0xE7);
    static final Color AMBER = new Color(0xD9, 0x77, 0x06);
    static final Color AMBER_SOFT = new Color(0xFE, 0xF3, 0xC7);

    private Theme() {}

    static Font regular(int size) { return new Font("Segoe UI", Font.PLAIN, size); }
    static Font bold(int size)    { return new Font("Segoe UI", Font.BOLD, size); }

    static void smooth(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
    }

    /** Centers a component inside a transparent wrapper. */
    static JPanel wrap(JComponent c) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);
        p.add(c);
        return p;
    }
}

class RoundedPanel extends JPanel {
    private final Color fill;
    private final Color line;
    private final int arc;

    RoundedPanel(Color fill, Color line, int arc) {
        this.fill = fill;
        this.line = line;
        this.arc = arc;
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g0) {
        Graphics2D g = (Graphics2D) g0.create();
        Theme.smooth(g);
        g.setColor(fill);
        g.fill(new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, arc, arc));
        if (line != null) {
            g.setColor(line);
            g.draw(new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, arc, arc));
        }
        g.dispose();
        super.paintComponent(g0);
    }

    @Override
    public Dimension getMaximumSize() {
        if (isMaximumSizeSet()) return super.getMaximumSize();   // respect an explicit setMaximumSize(...)
        return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
    }
}

class Badge extends RoundedPanel {
    Badge(String text, Color bg, Color fg, IconType icon) {
        this(text, bg, fg, icon, 10);
    }

    Badge(String text, Color bg, Color fg, IconType icon, int fontSize) {
        super(bg, null, 12);
        setLayout(new FlowLayout(FlowLayout.CENTER, 3, 0));
        setBorder(new EmptyBorder(3, 6, 3, 6));
        if (icon != null) add(new IconLabel(icon, 12, fg));
        JLabel l = new JLabel(text);
        l.setFont(Theme.bold(fontSize));
        l.setForeground(fg);
        add(l);
    }

    @Override
    public Dimension getMaximumSize() { return getPreferredSize(); }
}

class RoundedButton extends JButton {
    private final Color base;

    RoundedButton(String text) {
        this(text, Theme.RED, Color.WHITE);
    }

    RoundedButton(String text, Color base, Color textColor) {
        super(text);
        this.base = base;
        setFont(Theme.bold(13));
        setForeground(textColor);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setPreferredSize(new Dimension(100, 40));
    }

    private static Color darker(Color c, int amount) {
        return new Color(Math.max(0, c.getRed() - amount), Math.max(0, c.getGreen() - amount),
                Math.max(0, c.getBlue() - amount));
    }

    @Override
    protected void paintComponent(Graphics g0) {
        Graphics2D g = (Graphics2D) g0.create();
        Theme.smooth(g);
        Color c = base;
        if (getModel().isPressed()) c = darker(base, 35);
        else if (getModel().isRollover()) c = darker(base, 15);
        g.setColor(c);
        g.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 10, 10));
        g.dispose();
        super.paintComponent(g0);
    }

    @Override
    public Dimension getMaximumSize() { return new Dimension(Integer.MAX_VALUE, 40); }
}

class Divider extends JComponent {
    @Override protected void paintComponent(Graphics g) {
        g.setColor(new Color(0xEE, 0xEF, 0xF2));
        g.fillRect(0, 0, getWidth(), 1);
    }
    @Override public Dimension getPreferredSize() { return new Dimension(1, 1); }
    @Override public Dimension getMaximumSize()   { return new Dimension(Integer.MAX_VALUE, 1); }
}

class IconLabel extends JComponent {
    private final IconType type;
    private final int size;
    private final Color color;

    IconLabel(IconType type, int size, Color color) {
        this.type = type;
        this.size = size;
        this.color = color;
    }

    @Override protected void paintComponent(Graphics g0) {
        Graphics2D g = (Graphics2D) g0.create();
        Theme.smooth(g);
        g.setColor(color);
        g.setStroke(new BasicStroke(Math.max(1.2f, size / 12f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        type.draw(g, 1, 1, size - 2); // POLYMORPHISM: each icon draws itself
        g.dispose();
    }
    @Override public Dimension getPreferredSize() { return new Dimension(size, size); }
    @Override public Dimension getMinimumSize()   { return getPreferredSize(); }
    @Override public Dimension getMaximumSize()   { return getPreferredSize(); }
}

class PlaceholderField extends JTextField {
    private final String hint;

    PlaceholderField(String hint) {
        this.hint = hint;
        setOpaque(false);
        setBorder(null);
        setFont(Theme.regular(11));
        setForeground(Theme.TEXT);
    }

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        if (getText().isEmpty()) {
            Graphics2D g = (Graphics2D) g0.create();
            Theme.smooth(g);
            g.setColor(new Color(0x9C, 0xA3, 0xAF));
            g.setFont(getFont());
            FontMetrics fm = g.getFontMetrics();
            g.drawString(hint, getInsets().left, (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
            g.dispose();
        }
    }
}

/** Pink rounded-square button with a red left arrow. */
class BackButton extends RoundedPanel {
    BackButton(int size, Runnable onClick) {
        super(Theme.RED_SOFT, null, 14);
        setLayout(new GridBagLayout());
        setPreferredSize(new Dimension(size, size));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        add(new IconLabel(IconType.ARROW_LEFT, size * 2 / 5, Theme.RED));
        addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { onClick.run(); }
        });
    }

    @Override public Dimension getMaximumSize() { return getPreferredSize(); }
}
