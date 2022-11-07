package gomoku;

import javax.swing.*;
import java.awt.event.*;
import java.util.Random;

import static gomoku.UIChessboardPanel.*;

public class GomokuListener extends MouseAdapter implements ActionListener {
    Chessboard chessboard;
    UIFrame uiFrame;
    UIChessboardPanel uiChessboardPanel;
    String mode;//对应2个单选按钮，双人对战、人机对战
    AI ai;
    int aiColor;

    public GomokuListener(UIFrame uiFrame, UIChessboardPanel uiChessboardPanel) {
        chessboard = new Chessboard();//Chessboard就这一个
        this.uiFrame = uiFrame;
        this.uiChessboardPanel = uiChessboardPanel;
        mode = "双人对战";
        ai = new AI();
    }

    public void mouseClicked(MouseEvent e) {
        //转化
        int x = e.getX();
        int y = e.getY();
        int[] logicPos = new int[2];
        boolean ok;
        ok = posRealToLogic(x - 10, y - 30, logicPos);
        if (!ok) {//没有落在棋盘交叉点上
            return;
        }
        //落在了棋盘交叉点上
        int x1 = logicPos[0], y1 = logicPos[1];
        //逻辑上试落子
        ok = chessboard.tryAct(x1, y1);
        if (ok) {//若逻辑上落子成功
            //UI上更新一下
            uiChessboardPanel.actInUI(chessboard.lastColor, chessboard.getLastMove()[0], chessboard.getLastMove()[1]);
            if (!DoSthIfEnd()) {//尚未结局
                if (mode.equals("人机对战")) {//呼唤AI落子
                    aiPlease();
                }
            }
        }
    }

    //功能：请AI下一步棋
    private void aiPlease() {
        //AI思考之前，先把玩家落的子展示出来
        uiChessboardPanel.paint(uiChessboardPanel.getGraphics());
        uiFrame.removeMouseListener(this);//AI思考时，人不能下
        ai.thinkAndAct(chessboard);
        //UI上更新一下
        uiChessboardPanel.actInUI(chessboard.lastColor, chessboard.getLastMove()[0], chessboard.getLastMove()[1]);
        //是否终局
        DoSthIfEnd();
        uiFrame.addMouseListener(this);
    }

    //返回：是否结局
    private boolean DoSthIfEnd() {
        //判断是否结局
        if (chessboard.state != ING) {
            uiFrame.removeMouseListener(this);//游戏结束后不许再下
            if (mode.equals("双人对战")) {
                endOfPVP();
            } else if (mode.equals("人机对战")) {
                endOfPVE();
            }
            return true;
        }
        return false;
    }

    private void endOfPVE() {
        String color = null;
        switch (chessboard.state) {
            case BLACK:
                color = "黑";
                break;
            case WHILE:
                color = "白";
                break;
            case DRAW:
                JOptionPane.showMessageDialog(uiFrame, "平局了(⊙o⊙)…");
                return;
            default:
                assert (false);
        }
        String winner;
        if (chessboard.state == aiColor) {//若AI赢了
            winner = "AI";
        } else {
            winner = "人类玩家";
        }
        JOptionPane.showMessageDialog(uiFrame, winner + "执" + color + "胜！");
    }

    private void endOfPVP() {
        switch (chessboard.state) {
            case BLACK:
                JOptionPane.showMessageDialog(uiFrame, "执黑者胜。");
                break;
            case WHILE:
                JOptionPane.showMessageDialog(uiFrame, "执白者胜。");
                break;
            case DRAW:
                JOptionPane.showMessageDialog(uiFrame, "平局了(⊙o⊙)…");
                break;
            default:
                assert (false);
        }
    }

    private boolean posRealToLogic(int x, int y, int[] logicPos) {
        double x1 = (double) (x - X0) / SQUARE_SIZE;
        int x2 = Math.toIntExact(Math.round(x1));
        double y1 = (double) (y - Y0) / SQUARE_SIZE;
        int y2 = Math.toIntExact(Math.round(y1));
        if (Math.abs(x2 - x1) < 0.4 && Math.abs(y2 - y1) < 0.4) {
            logicPos[0] = x2;
            logicPos[1] = y2;
            return true;
        }
        return false;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() instanceof JRadioButton) {//单选按钮
            mode = e.getActionCommand();
            uiFrame.removeMouseListener(this);
            reset();
            return;
        }
        if (e.getSource() instanceof JButton) {//普通按钮
            String command = e.getActionCommand();
            switch (command) {
                case "开始/重新开始":
                    MouseListener[] mls = uiFrame.getMouseListeners();//防止反复点击导致加了太多
                    if (mls.length > 0) {
                        uiFrame.removeMouseListener(this);
                    }
                    reset();
                    uiFrame.addMouseListener(this);
                    if (mode.equals("人机对战")) {
                        if (whoFirst() == 2) {//玩家2为AI，AI先行
                            aiColor = BLACK;
                            JOptionPane.showMessageDialog(uiFrame, "猜先结果：AI执黑！");
                            aiPlease();
                        } else {
                            JOptionPane.showMessageDialog(uiFrame, "猜先结果：玩家执黑！");
                            aiColor = WHILE;
                        }
                    }
                    return;
                case "认输":
                    if (chessboard.state != ING) return;//只有游戏进行中时才能认输
                    if (mode.equals("人机对战")) {//即使AI正在思考时人点击了认输，也应该认为是人认输
                        chessboard.state = aiColor;
                    } else {
                        switch (chessboard.curColor) {
                            case BLACK:
                                chessboard.state = WHILE;
                                break;
                            case WHILE:
                                chessboard.state = BLACK;
                                break;
                            default:
                                assert (false);
                        }
                    }
                    DoSthIfEnd();
                    return;
                case "暂停":
                    if (chessboard.state != ING) return;
                    uiFrame.removeMouseListener(this);
                    return;
                case "恢复":
                    if (chessboard.state != ING) return;
                    mls = uiFrame.getMouseListeners();
                    if (mls.length > 0) {
                        uiFrame.removeMouseListener(this);
                    }
                    uiFrame.addMouseListener(this);
                    return;
            }
        }
    }

    //等概率返回1或2
    private int whoFirst() {
        Random r = new Random();
        return r.nextInt(2) + 1;
    }

    private void reset() {
        chessboard.initBoard();
        uiChessboardPanel.initBoard();
        ai.initAI();
        uiFrame.repaint();
    }

}
