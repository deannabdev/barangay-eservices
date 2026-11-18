package components;

import javax.swing.*;
import java.awt.*;

public class PlaceholderField extends JTextField {
    private final String hint;
    private final Color hintColor;
    private final Font hintFont;

    public PlaceholderField(String hint) {
        this(hint, Theme.regular(11), Theme.TEXT, new Color(0x9C, 0xA3, 0xAF), null,
                Theme.regular(11));
    }

    public PlaceholderField(String hint, Font font, Color foreground, Color hintColor, Color caretColor) {
        this(hint, font, foreground, hintColor, caretColor, font);
    }

    public PlaceholderField(String hint, Font font, Color foreground, Color hintColor, Color caretColor,
                            Font hintFont) {
        this.hint = hint;
        this.hintColor = hintColor;
        this.hintFont = hintFont;
        setOpaque(false);
        setBorder(null);
        setFont(font);
        setForeground(foreground);
        if (caretColor != null) setCaretColor(caretColor);
    }

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        if (getText().isEmpty()) {
            Graphics2D g = (Graphics2D) g0.create();
            Theme.smooth(g);
            g.setColor(hintColor);
            g.setFont(hintFont);
            FontMetrics fm = g.getFontMetrics();
            g.drawString(hint, getInsets().left, (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
            g.dispose();
        }
    }
}
