package barangay.eservices;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

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
                // TODO: open your "forgot password" flow.
                JOptionPane.showMessageDialog(Security.this, "Forgot password - coming soon.");
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
                + "When you delete your account, your main profile and everything else you've added "
                + "will be permanently deleted. You won't be able to retrieve any information from "
                + "this account.</div></html>");
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
        String current = new String(currentPassword.getPassword());
        String next    = new String(newPassword.getPassword());
        String retype  = new String(retypePassword.getPassword());

        String problem = null;
        if (current.isEmpty())              problem = "Please enter your current password.";
        else if (next.length() < 8)         problem = "Your new password must be at least 8 characters.";
        else if (next.equals(current))      problem = "Your new password must be different from the current one.";
        else if (!next.equals(retype))      problem = "The new passwords do not match.";

        if (problem != null) {
            JOptionPane.showMessageDialog(this, problem, "Change password", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // TODO: check the current password and save the new one (database / API) here.
        JOptionPane.showMessageDialog(this, "Your password has been changed.");
        goBack();
    }

    private void onDeleteAccount() {
        Object[] options = {"Delete", "Cancel"};
        int answer = JOptionPane.showOptionDialog(this,
                "Your account and everything you've added will be permanently deleted.\n"
                + "This cannot be undone.",
                "Delete account?", JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
                null, options, options[1]);
        if (answer == 0) {
            // TODO: delete the account (database / API) here.
            JOptionPane.showMessageDialog(this, "Your account has been deleted.");
            new LoginScreen().setVisible(true);
            dispose();
        }
    }

}