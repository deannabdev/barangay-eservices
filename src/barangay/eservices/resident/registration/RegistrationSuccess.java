import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.util.Arrays;

public class RegistrationSuccess extends JFrame {

    // ---- Colors ----
    private static final Color RED        = new Color(211, 47, 47);
    private static final Color RED_TOP    = new Color(214, 42, 42);
    private static final Color RED_BOTTOM = new Color(160, 28, 30);
    private static final Color GREEN      = new Color(36, 94, 56);
    private static final Color NAVY       = new Color(27, 27, 47);

    private static final int W = 351, H = 775;

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

    public RegistrationSuccess() {
        super("Registration Successful");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);

        BackgroundPanel root = new BackgroundPanel();
        root.setLayout(null);
        root.setPreferredSize(new Dimension(W, H));
        buildUI(root);

        setContentPane(root);
        pack();
        setLocationRelativeTo(null);
    }

    private void buildUI(JPanel root) {
        // Green check circle
        CheckCircle check = new CheckCircle();
        check.setBounds(W / 2 - 27, 195, 54, 54);
        root.add(check);

        // "Registration Successful!" (green, centered, drop shadow)
        ShadowLabel title = new ShadowLabel("Registration Successful!", GREEN);
        title.setFont(font(Font.BOLD, 22f));
        title.setBounds(0, 256, W, 44);
        root.add(title);

        // Pending verification badge
        PendingBadge badge = new PendingBadge("PENDING VERIFICATION");
        badge.setBounds(W / 2 - 58, 297, 116, 18);
        root.add(badge);

        // Message
        JLabel message = new JLabel(
                "<html><div style='text-align:center'>"
                + "Thank you for registering! The admin team will<br>"
                + "review your information and send a confirmation to<br>"
                + "your email once verified.</div></html>",
                SwingConstants.CENTER);
        message.setFont(font(Font.PLAIN, 11f));
        message.setForeground(NAVY);
        message.setBounds(0, 338, W, 56);
        root.add(message);

        // Back to Login button
        RoundedButton back = new RoundedButton("Back to Login");
        back.setBounds((W - 207) / 2, 603, 207, 32);
        back.addActionListener(e -> {
            new LoginScreen().setVisible(true);
            dispose();
        });
        root.add(back);
    }

    // =====================================================================
    //  Custom components
    // =====================================================================

    /** Red gradient header with a white rounded sheet. */
    private static class BackgroundPanel extends JPanel {
        @Override protected void paintComponent(Graphics g0) {
            super.paintComponent(g0);
            Graphics2D g = (Graphics2D) g0.create();
            smooth(g);
            g.setPaint(new GradientPaint(0, 0, RED_TOP, 0, 80, RED_BOTTOM));
            g.fillRect(0, 0, getWidth(), getHeight());
            g.setColor(Color.WHITE);
            g.fill(new RoundRectangle2D.Double(0, 62, getWidth(), getHeight(), 80, 80));
            g.dispose();
        }
    }

    /** Solid green circle with a white check mark. */
    private static class CheckCircle extends JComponent {
        @Override protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            smooth(g);
            int w = getWidth(), h = getHeight();
            g.setColor(GREEN);
            g.fill(new Ellipse2D.Double(0, 0, w, h));

            double cx = w / 2.0, cy = h / 2.0;
            Path2D check = new Path2D.Double();
            check.moveTo(cx - 11, cy + 1);
            check.lineTo(cx - 4, cy + 8);
            check.lineTo(cx + 11, cy - 8);
            g.setColor(Color.WHITE);
            g.setStroke(new BasicStroke(5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.draw(check);
            g.dispose();
        }
    }

    /** Centered title with Figma drop shadow: X 0, Y 4, blur 4, #000000 25%. */
    private static class ShadowLabel extends JLabel {
        private final Color color;

        ShadowLabel(String text, Color color) {
            super(text, SwingConstants.CENTER);
            this.color = color;
        }

        @Override protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            smooth(g);
            g.setFont(getFont());
            FontMetrics fm = g.getFontMetrics();
            int x = (getWidth() - fm.stringWidth(getText())) / 2;
            int y = fm.getAscent();

            BufferedImage img = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics2D ig = img.createGraphics();
            smooth(ig);
            ig.setFont(getFont());
            ig.setColor(new Color(0, 0, 0, 64));
            ig.drawString(getText(), x, y + 4);
            ig.dispose();

            float[] k = new float[121];
            float sum = 0;
            for (int i = 0; i < 121; i++)
                sum += k[i] = (float) Math.exp(-(Math.pow(i % 11 - 5, 2) + Math.pow(i / 11 - 5, 2)) / 8);
            for (int i = 0; i < 121; i++) k[i] /= sum;
            g.drawImage(new ConvolveOp(new Kernel(11, 11, k)).filter(img, null), 0, 0, null);

            g.setColor(color);
            g.drawString(getText(), x, y);
            g.dispose();
        }
    }

    /** Small yellow pill with orange text. */
    private static class PendingBadge extends JComponent {
        private final String text;
        PendingBadge(String text) { this.text = text; }

        @Override protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            smooth(g);
            g.setColor(new Color(255, 236, 179));
            g.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 8, 8));
            g.setColor(new Color(230, 140, 0));
            g.setFont(font(Font.BOLD, 8f));
            FontMetrics fm = g.getFontMetrics();
            g.drawString(text, (getWidth() - fm.stringWidth(text)) / 2,
                    (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
            g.dispose();
        }
    }

    /** Rounded red button. */
    private static class RoundedButton extends JButton {
        private boolean hover, pressed;

        RoundedButton(String text) {
            super(text);
            setFont(font(Font.BOLD, 13f));
            setForeground(Color.WHITE);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e)  { hover = true;  repaint(); }
                @Override public void mouseExited(MouseEvent e)   { hover = false; repaint(); }
                @Override public void mousePressed(MouseEvent e)  { pressed = true;  repaint(); }
                @Override public void mouseReleased(MouseEvent e) { pressed = false; repaint(); }
            });
        }

        @Override protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            smooth(g);
            g.setColor(pressed ? RED.darker() : hover ? new Color(228, 52, 56) : RED);
            g.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 14, 14));
            g.setColor(Color.WHITE);
            g.setFont(getFont());
            FontMetrics fm = g.getFontMetrics();
            g.drawString(getText(), (getWidth() - fm.stringWidth(getText())) / 2,
                    (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
            g.dispose();
        }
    }

}