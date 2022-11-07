package gomoku;

import javax.swing.*;
import java.awt.*;

import static java.lang.Math.min;

public class UIChessboardPanel extends JPanel implements Config {
    private int[][] board;
    private int lastX;
    private int lastY;
    private boolean gaming;

    public UIChessboardPanel() {
        setBackground(Color.ORANGE);
        board = new int[SIZE][SIZE];
        initBoard();
        updateConstants();
    }

    public void initBoard() {
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                board[i][j] = EMPTY;
            }
        }
        gaming = false;
    }

    public void actInUI(int color, int x, int y) {
        gaming = true;
        board[x][y] = color;
        lastX = x;
        lastY = y;
        repaint();
    }

    public void paint(Graphics g) {
        super.paint(g);
        updateConstants();
        drawChessboard(g);
        paintChesses(g);
    }

    public static int BOARD_WIDTH;
    public static int BOARD_HEIGHT;
    public static int SQUARE_SIZE;//棋格大小
    public static int CHESS_SIZE;//棋子大小
    //棋盘起点坐标
    public static int X0;
    public static int Y0;
    //行列标记到棋盘的距离
    public static int offset;

    private void updateConstants() {
        BOARD_WIDTH = getWidth();
        BOARD_HEIGHT = getHeight();
        SQUARE_SIZE = min(BOARD_WIDTH / SIZE, BOARD_HEIGHT / SIZE);
        CHESS_SIZE = SQUARE_SIZE;
        X0 = (BOARD_WIDTH - SQUARE_SIZE * (SIZE - 1)) / 2;
        Y0 = (BOARD_HEIGHT - SQUARE_SIZE * (SIZE - 1)) / 2;
        offset = SQUARE_SIZE / 3;
    }

    public void drawChessboard(Graphics g) {
        //画线
        g.setColor(new Color(78, 52, 25));
        for (int x = 0; x < SIZE; x++) {
            g.drawLine(X0, Y0 + x * SQUARE_SIZE, X0 + (SIZE - 1) * SQUARE_SIZE, Y0 + x * SQUARE_SIZE);
        }
        for (int y = 0; y < SIZE; y++) {
            g.drawLine(X0 + SQUARE_SIZE * y, Y0, X0 + SQUARE_SIZE * y, Y0 + (SIZE - 1) * SQUARE_SIZE);
        }
        //画标记点
        g.setColor(new Color(59, 42, 22));
        drawAnOval(g, SIZE / 2, SIZE / 2, CHESS_SIZE / 4);
        drawAnOval(g, 3, 3, CHESS_SIZE / 4);
        drawAnOval(g, 3, 11, CHESS_SIZE / 4);
        drawAnOval(g, 11, 3, CHESS_SIZE / 4);
        drawAnOval(g, 11, 11, CHESS_SIZE / 4);
        //标记行列
        int x0 = X0 - offset - 2;
        int y0 = Y0 - offset + 1;
        for (int i = 0, x = X0, y = Y0; i < SIZE; i++, x += SQUARE_SIZE, y += SQUARE_SIZE) {
            g.setFont(new Font("Calibri", Font.PLAIN, SQUARE_SIZE / 3));
            g.drawString(Character.toString('A' + i), x, y0);
            g.drawString(Integer.toString(1 + i), x0, y);
        }
    }

    public void paintChesses(Graphics g) {
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                if (board[i][j] == WHILE) {
                    g.setColor(Color.WHITE);
                    drawAnOval(g, i, j, CHESS_SIZE);
                } else if (board[i][j] == BLACK) {
                    g.setColor(Color.BLACK);
                    drawAnOval(g, i, j, CHESS_SIZE);
                }
            }
        }
        //给最后一步棋做标记
        if (gaming) {
            g.setColor(Color.BLUE);
            drawAnOval(g, lastX, lastY, CHESS_SIZE / 4);
        }
    }

    public static void drawAnOval(Graphics g, int x, int y, int size) {
        g.fillOval(X0 - size / 2 + x * SQUARE_SIZE, Y0 - size / 2 + y * SQUARE_SIZE, size, size);
    }
}
