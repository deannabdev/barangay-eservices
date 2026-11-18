package auth;
import data.AccountsStore;
import profile.ProfileScreen;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;

public class Security extends JFrame {

    private static final int W = 351, H = 775;
    private static final int X0 = 29, FW = 291;   // left edge and width of the content column

    // ---- Form fields ----
    private final SecurityPasswordField currentPassword = new SecurityPasswordField("Current password");
    private final SecurityPasswordField newPassword     = new SecurityPasswordField("New password");
    private final SecurityPasswordField retypePassword  = new SecurityPasswordField("Re-type new password");

    public Security() {
        super("Security & Log In");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);

        SecurityBackgroundPanel root = new SecurityBackgroundPanel();
        root.setLayout(null);
        root.setPreferredSize(new Dimension(W, H));
        buildUI(root);

        setContentPane(root);
        pack();
        setLocationRelativeTo(null);
    }

    private void buildUI(JPanel root) {
        // Header: back arrow + title
        SecurityBackButton back = new SecurityBackButton(this::goBack);
        back.setBounds(18, 50, 32, 32);
        root.add(back);

        JLabel title = new JLabel("SECURITY & LOG IN", SwingConstants.CENTER);
        title.setFont(SecurityTheme.font(Font.BOLD, 20f));
        title.setForeground(Color.BLACK);
        title.setBounds(0, 48, W, 36);
        root.add(title);

        // ---- Change password ----
        JLabel changeTitle = new JLabel("Change password");
        changeTitle.setFont(SecurityTheme.font(Font.BOLD, 13f));
        changeTitle.setForeground(Color.BLACK);
        changeTitle.setBounds(X0, 101, FW, 18);
        root.add(changeTitle);

        JLabel changeHint = new JLabel("Your password must be at least 8 characters.");
        changeHint.setFont(SecurityTheme.font(Font.PLAIN, 9f));
        changeHint.setForeground(Color.BLACK);
        changeHint.setBounds(X0, 117, FW, 13);
        root.add(changeHint);

        addPasswordField(root, currentPassword, 139);
        addPasswordField(root, newPassword, 185);
        addPasswordField(root, retypePassword, 231);

        JLabel forgot = new JLabel("Forgot your password?", SwingConstants.RIGHT);
        forgot.setFont(SecurityTheme.font(Font.PLAIN, 9.5f));
        forgot.setForeground(new Color(110, 110, 120));
        forgot.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        forgot.setBounds(X0, 270, FW, 14);
        forgot.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                new LoginScreen.ForgotPasswordScreen().setVisible(true);
                dispose();
            }
        });
        root.add(forgot);

        SecurityButton change = new SecurityButton("Change password", false);
        change.setBounds(X0, 297, FW, 35);
        change.addActionListener(e -> onChangePassword());
        root.add(change);

        // ---- Delete account ----
        JLabel deleteTitle = new JLabel("Delete account");
        deleteTitle.setFont(SecurityTheme.font(Font.BOLD, 13f));
        deleteTitle.setForeground(Color.BLACK);
        deleteTitle.setBounds(X0, 354, FW, 18);
        root.add(deleteTitle);

        JLabel deleteText = new JLabel(
                "<html><div style='width:" + FW + "px'><b>Deleting your account is permanent.</b> "
                + "Your profile and ID photo will be deleted. Previously submitted requests and "
                + "reports will remain in the system.</div></html>");
        deleteText.setFont(SecurityTheme.font(Font.PLAIN, 9.5f));
        deleteText.setForeground(Color.BLACK);
        deleteText.setVerticalAlignment(SwingConstants.TOP);
        deleteText.setBounds(X0, 376, FW, 74);
        root.add(deleteText);

        SecurityButton delete = new SecurityButton("Delete Account", true);
        delete.setBounds(X0, 462, FW, 35);
        delete.addActionListener(e -> onDeleteAccount());
        root.add(delete);
    }

    /** Password input (30px tall) with a show/hide eye on the right. */
    private void addPasswordField(JPanel root, SecurityPasswordField pf, int y) {
        SecurityFieldBox box = new SecurityFieldBox(pf, 10, 36);
        box.setBounds(X0, y, FW + 4, 34);

        SecurityEyeButton eye = new SecurityEyeButton();
        eye.setBounds(FW - 32, 4, 24, 22);
        eye.addActionListener(e -> {
            eye.setShown(!eye.isShown());
            pf.setEchoChar(eye.isShown() ? (char) 0 : '\u2022');
        });
        box.add(eye);
        root.add(box);
    }

    // ---- Actions ----

    private void goBack() {
        dispose();
        ProfileScreen.showWindow();
    }

    private void onChangePassword() {
        char[] current = currentPassword.getPassword();
        char[] next = newPassword.getPassword();
        char[] retype = retypePassword.getPassword();
        try {
            String problem = null;
            if (current.length == 0) problem = "Please enter your current password.";
            else if (next.length < 8) problem = "Your new password must be at least 8 characters.";
            else if (Arrays.equals(next, current)) {
                problem = "Your new password must be different from the current one.";
            } else if (!Arrays.equals(next, retype)) {
                problem = "The new passwords do not match.";
            }

            if (problem != null) {
                JOptionPane.showMessageDialog(this, problem, "Change password", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                if (!AccountsStore.changeCurrentPassword(current, next)) {
                    JOptionPane.showMessageDialog(this, "Your current password is incorrect.",
                            "Change password", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                JOptionPane.showMessageDialog(this, "Your password has been changed.");
                currentPassword.setText("");
                newPassword.setText("");
                retypePassword.setText("");
                goBack();
            } catch (IOException | GeneralSecurityException ex) {
                JOptionPane.showMessageDialog(this, "Could not change your password:\n" + ex.getMessage(),
                        "Change password error", JOptionPane.ERROR_MESSAGE);
            }
        } finally {
            Arrays.fill(current, '\0');
            Arrays.fill(next, '\0');
            Arrays.fill(retype, '\0');
        }
    }

    private void onDeleteAccount() {
        Object[] options = {"Delete", "Cancel"};
        int answer = JOptionPane.showOptionDialog(this,
                "Your account, profile, and ID photo will be permanently deleted. "
                + "Previously submitted requests and reports will remain.\nThis cannot be undone.",
                "Delete account?", JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
                null, options, options[1]);
        if (answer == 0) {
            try {
                AccountsStore.deleteCurrentAccount();
                JOptionPane.showMessageDialog(this,
                        "Your account, profile, and ID photo have been deleted. Previously submitted requests "
                                + "and reports remain in the system.",
                        "Account deleted", JOptionPane.INFORMATION_MESSAGE);
                new LoginScreen().setVisible(true);
                dispose();
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Could not delete your account:\n" + ex.getMessage(),
                        "Delete account error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

}