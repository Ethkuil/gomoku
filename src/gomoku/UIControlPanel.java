package gomoku;

import javax.swing.*;
import java.awt.*;

public class UIControlPanel extends JPanel implements Config {
    public UIControlPanel(GomokuListener gl) {
        setBackground(new Color(240, 240, 240));
        setPreferredSize(new Dimension(CONTROL_PANEL_WIDTH, FRAME_HEIGHT));

        setLayout(new FlowLayout());

        String[] buttons = {"开始/重新开始", "认输", "暂停", "恢复"};//功能按钮命令
        for (String s : buttons) {
            JButton button = new JButton(s);
            button.setPreferredSize(new Dimension(120, 50));
            add(button);
            button.addActionListener(gl);
        }
        String[] radioButtons = {"双人对战", "人机对战"};//单选按钮命令
        ButtonGroup bg = new ButtonGroup();
        for (int i = 0; i < radioButtons.length; i++) {
            JRadioButton radioButton = new JRadioButton(radioButtons[i]);
            bg.add(radioButton);
            radioButton.setPreferredSize(new Dimension(120, 50));
            radioButton.setOpaque(false);//不透明
            radioButton.setForeground(Color.BLACK);//前景色为黑
            if (i == 0) {//默认选中第一个，即双人对战
                radioButton.setSelected(true);
            }
            add(radioButton);
            radioButton.addActionListener(gl);
        }
    }
}
