package components;

import javax.swing.*;
import java.awt.*;

public final class Theme {
    public static final Color RED = new Color(0xDC, 0x2F, 0x2F);
    public static final Color RED_DARK = new Color(0xA8, 0x1C, 0x1C);
    public static final Color DEEP_RED = new Color(0x84, 0x1B, 0x1B);
    public static final Color RED_SOFT = new Color(0xFD, 0xE8, 0xE8);
    public static final Color BG = new Color(0xF8, 0xF9, 0xFB);
    public static final Color WHITE = Color.WHITE;
    public static final Color BORDER = new Color(0xE5, 0xE7, 0xEB);
    public static final Color TEXT = new Color(0x11, 0x18, 0x27);
    public static final Color TEXT_SUB = new Color(0x4B, 0x55, 0x63);
    public static final Color MUTED = new Color(0x6B, 0x72, 0x80);
    public static final Color GREEN = new Color(0x16, 0xA3, 0x4A);
    public static final Color GREEN_SOFT = new Color(0xDC, 0xFC, 0xE7);
    public static final Color AMBER = new Color(0xD9, 0x77, 0x06);
    public static final Color AMBER_SOFT = new Color(0xFE, 0xF3, 0xC7);

    private Theme() {}

    public static Font regular(int size) { return new Font("Segoe UI", Font.PLAIN, size); }
    public static Font bold(int size) { return new Font("Segoe UI", Font.BOLD, size); }

    public static void smooth(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
    }

    public static JLabel shadowLabel(String text, Color shadowColor) {
        return new ShadowLabel(text, shadowColor);
    }

    public static JPanel wrap(JComponent c) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);
        p.add(c);
        return p;
    }
}
