package gomoku;

import javax.swing.*;
import java.awt.*;

public class UIFrame extends JFrame implements Config{
    public static void main(String[] args) {
        UIFrame uiFrame = new UIFrame();
        uiFrame.setVisible(true);
    }
    
    public UIFrame() {
        setTitle("五子棋 by 林智鑫");
        setSize(FRAME_WIDTH, FRAME_HEIGHT);
        setResizable(true);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        UIChessboardPanel chessboardPanel = new UIChessboardPanel();
        add(chessboardPanel, BorderLayout.CENTER);
        
        GomokuListener gl = new GomokuListener(this, chessboardPanel);
        
        UIControlPanel controlPanel = new UIControlPanel(gl);
        add(controlPanel, BorderLayout.EAST);
    }
    
}
