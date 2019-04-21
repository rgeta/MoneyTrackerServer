import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;

class AboutDialog extends JDialog {
    AboutDialog(JFrame owner)
    {
        super(owner, "About", true);
        JPanel padder = new JPanel();
        padder.setLayout(new BoxLayout(padder, BoxLayout.Y_AXIS));
        padder.setBorder(new EmptyBorder(8, 8, 8, 8));
        add(padder);

        final JLabel label = new JLabel("<html>Use MoneyTrackerServer if you want to backup or restore Android MoneyTracker data. "
                + "This program is experimental so please be careful! It may contain some hidden errors."
                + "<br><br>\u00A9 2017-2019 Rostislav Geta</html>");
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        padder.add(label);

        JButton ok = new JButton("OK");
        ok.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                setVisible(false);
            }
        });

        JPanel panel = new JPanel();
        JPanel innerPanel = new JPanel();
        innerPanel.add(ok);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setLayout(new BorderLayout());
        panel.add(innerPanel, BorderLayout.SOUTH);
        padder.add(panel);
        setSize(320, 200);
        setResizable(false);
        setLocationRelativeTo(owner);
    }
}