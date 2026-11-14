import javax.swing.*;
import java.awt.*;

public class RegistrationSuccess extends JFrame {

    private static final int W = 351, H = 775;

    public RegistrationSuccess() {
        super("Registration Successful");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);

        RegistrationBackgroundPanel root = new RegistrationBackgroundPanel();
        root.setLayout(null);
        root.setPreferredSize(new Dimension(W, H));
        buildUI(root);

        setContentPane(root);
        pack();
        setLocationRelativeTo(null);
    }

    private void buildUI(JPanel root) {
        // Green check circle
        RegistrationSuccessCheck check = new RegistrationSuccessCheck();
        check.setBounds(W / 2 - 27, 195, 54, 54);
        root.add(check);

        // "Registration Successful!" (green, centered, drop shadow)
        RegistrationSuccessLabel title = new RegistrationSuccessLabel(
                "Registration Successful!", RegistrationTheme.SUCCESS_GREEN);
        title.setFont(RegistrationTheme.font(Font.BOLD, 22f));
        title.setBounds(0, 256, W, 44);
        root.add(title);

        // Pending verification badge
        RegistrationPendingBadge badge = new RegistrationPendingBadge("PENDING VERIFICATION");
        badge.setBounds(W / 2 - 58, 297, 116, 18);
        root.add(badge);

        // Message
        JLabel message = new JLabel(
                "<html><div style='text-align:center'>"
                + "Thank you for registering! The admin team will<br>"
                + "review your information and send a confirmation to<br>"
                + "your email once verified.</div></html>",
                SwingConstants.CENTER);
        message.setFont(RegistrationTheme.font(Font.PLAIN, 11f));
        message.setForeground(RegistrationTheme.NAVY);
        message.setBounds(0, 338, W, 56);
        root.add(message);

        // Back to Login button
        RegistrationButton back = new RegistrationButton(
                "Back to Login", RegistrationTheme.font(Font.BOLD, 13f), RegistrationTheme.RED);
        back.setBounds((W - 207) / 2, 603, 207, 32);
        back.addActionListener(e -> {
            new LoginScreen().setVisible(true);
            dispose();
        });
        root.add(back);
    }

}