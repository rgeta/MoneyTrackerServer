import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.*;

public class MoneyTrackerServer extends JFrame {

    private JTextArea textArea = new JTextArea();

    // Серверная "нить"
    private ServerThread serverThread;

    private MoneyTrackerServer() {
        setTitle("MoneyTrackerServer 2.1 (Swing version)");
        setIconImage(Toolkit.getDefaultToolkit().getImage(this.getClass().getClassLoader().getResource("images/icon16x16.png")));
        setSize(800, 600);
        setLocationRelativeTo(null);

        // Верхнее меню
        final JMenuBar menuBar = new JMenuBar();
        final JMenu fileMenu = new JMenu("File");
        final JMenu helpMenu = new JMenu("Help");
        menuBar.add(fileMenu);
        menuBar.add(helpMenu);

        // Пункт меню подключения
        final JMenuItem miStart = new JMenuItem("Start server");

        // Пункт меню отключения
        final JMenuItem miStop = new JMenuItem("Stop server");
        miStop.setEnabled(false);

        miStart.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {

                // Запрещаем кнопку запуска сервера
                miStart.setEnabled(false);
                try {

                    // Запускаем сервер
                    serverThread = new ServerThread(textArea);
                    serverThread.start();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {

                    // Разрешаем кнопку остановки сервера
                    miStop.setEnabled(true);
                }
            }
        });

        miStop.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {

                // Запрещаем кнопку остановки сервера
                miStop.setEnabled(false);
                try {

                    // Отдаем команду закрытия сокета
                    serverThread.closeSocket();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {

                    // Разрешаем кнопку запуска сервера
                    miStart.setEnabled(true);
                }

                // Ставим флаг завершения сервера
                serverThread.interrupt();
                try {
                    serverThread.finish();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        fileMenu.add(miStart);
        fileMenu.add(miStop);

        // Разделитель
        fileMenu.addSeparator();

        // Пункт меню отключения
        final JMenuItem miExit = new JMenuItem("Exit");
        miExit.setMnemonic(KeyEvent.VK_X);
        miExit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_DOWN_MASK));

        miExit.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                stopAndExit();
            }
        });
        fileMenu.add(miExit);

        final JMenuItem miServerManual = new JMenuItem("Server manual");
        helpMenu.add(miServerManual);
        helpMenu.addSeparator();

        final JMenuItem miAbout = new JMenuItem("About");
        miAbout.setMnemonic(KeyEvent.VK_A);
        miAbout.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_DOWN_MASK));

        miAbout.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                AboutDialog dialog = new AboutDialog(MoneyTrackerServer.this);
                dialog.setVisible(true);
            }
        });
        helpMenu.add(miAbout);
        setJMenuBar(menuBar);

        JPanel padder = new JPanel();
        padder.setLayout(new BoxLayout(padder, BoxLayout.Y_AXIS));
        padder.setBorder(new EmptyBorder(8, 8, 8, 8));
        add(padder);
        final JLabel label = new JLabel("Server log area:");
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        padder.add(label);

        // В эту текстовую область будем выводить текстовые сообщения сервера
        textArea.setEditable(false);
        textArea.setAutoscrolls(true);

        // Чтобы текст автоматически прокручивался по мере добавления
        DefaultCaret caret = (DefaultCaret) textArea.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

        // Добавляем полосу прокрутки
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
        padder.add(scrollPane);

        // Обработчик закрытия окна
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                stopAndExit();
            }
        });

        // Ограничение на минимальный размер
        setMinimumSize(new Dimension(400, 300));
    }

    private void stopAndExit() {
        if (serverThread != null && serverThread.isAlive()) {
            try {

                // Отдаем команду закрытия сокета
                serverThread.closeSocket();

                // Ставим флаг завершения сервера
                serverThread.interrupt();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        System.exit(0);
    }

    public static void main(String[] args) {
        MoneyTrackerServer mts = new MoneyTrackerServer();
        mts.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mts.setVisible(true);
    }
}