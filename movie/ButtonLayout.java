package movie;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import javax.swing.JButton;
import javax.swing.JPanel;

public class ButtonLayout extends JPanel implements ActionListener {

    JButton play_pause, rewind, forward, restart;

    public ButtonLayout() {
        play_pause = new JButton("Pause");
        play_pause.setActionCommand("pause");
        play_pause.setMnemonic(KeyEvent.VK_P);

        forward = new JButton("Forward");
        forward.setActionCommand("forward");
        forward.setMnemonic(KeyEvent.VK_F);
        forward.setEnabled(false);

        rewind = new JButton("Rewind");
        rewind.setActionCommand("backward");
        rewind.setMnemonic(KeyEvent.VK_R);
        rewind.setEnabled(true);

        restart = new JButton("Restart");
        restart.setActionCommand("restart");

        addListener(this);

        setSize(400, 200);
        add(rewind);
        add(play_pause);
        add(forward);
        add(restart);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand()) {
            case "pause":
                play_pause.setText("Play");
                play_pause.setActionCommand("play");
                break;
            case "forward":
                forward.setEnabled(false);
                rewind.setEnabled(true);
                break;
            case "backward":
                rewind.setEnabled(false);
                forward.setEnabled(true);
                break;
            case "play":
                play_pause.setText("Pause");
                play_pause.setActionCommand("pause");
                break;
        }
    }

    public void addListener(ActionListener a) {
        play_pause.addActionListener(a);
        forward.addActionListener(a);
        rewind.addActionListener(a);
        restart.addActionListener(a);
    }
}
