package gomoku;

import java.util.Stack;

public class Chessboard implements Config {
    //断言：尝试落子的一方颜色合法
    public boolean isCanGo(int x, int y) {
        if (x < 0 || x > 14) return false;
        if (y < 0 || y > 14) return false;
        return board[x][y] == EMPTY;
    }

    public int[] getLastMove() {
        return movesMade.peek();
    }

    //断言：所给坐标可落子
    //作用：
    // 1. 落子
    // 2. 修改棋局状态
    public void act(int x, int y) {
        board[x][y] = curColor;
        changeColor();
        numOfChess++;
        if (hasGetFive()) {
            state = lastColor;
        } else if (numOfChess == capacity) {
            state = DRAW;
        } else {
            state = ING;
        }
        movesMade.push(new int[]{x, y});//保存棋步
    }

    public void unAct() {
        int[] lastMove = movesMade.pop();
        state = ING;
        int x = lastMove[0], y = lastMove[1];
        numOfChess--;
        changeColor();
        board[x][y] = EMPTY;
    }

    //若能落子，落子并返回true；否则返回false
    public boolean tryAct(int x, int y) {
        if (isCanGo(x, y)) {
            act(x, y);
            return true;
        } else {
            return false;
        }
    }

    public Chessboard() {
        capacity = SIZE * SIZE;
        board = new int[SIZE][SIZE];
        movesMade = new Stack<>();
        initBoard();
    }

    public void initBoard() {
        numOfChess = 0;
        state = NOBEGIN;
        curColor = BLACK;
        lastColor = WHILE;
        movesMade.clear();
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                board[i][j] = EMPTY;
            }
        }
    }

    int[][] board;//元素值为EMPTY、BLACK、WHITE之一
    int capacity;

    int numOfChess;
    int state; //若已终局，赢家颜色，平局则DRAW. 未终局，ING. 未开始，NOBEGIN
    int curColor;//即当前要落子方的颜色
    int lastColor;//刚落完子颜色；与curColor相反
    Stack<int[]> movesMade;

    private boolean hasGetFive() {
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE - 4; j++) {
                if (board[i][j] == lastColor) {
                    if (board[i][j + 1] == lastColor && board[i][j + 2] == lastColor && board[i][j + 3] == lastColor && board[i][j + 4] == lastColor) {
                        return true;
                    }
                }
            }
        }
        for (int i = 0; i < SIZE - 4; i++) {
            for (int j = 0; j < SIZE; j++) {
                if (board[i][j] == lastColor) {
                    if (board[i + 1][j] == lastColor && board[i + 2][j] == lastColor && board[i + 3][j] == lastColor && board[i + 4][j] == lastColor) {
                        return true;
                    }
                }
            }
        }
        for (int i = 0; i < SIZE - 4; i++) {
            for (int j = 0; j < SIZE - 4; j++) {
                if (board[i][j] == lastColor) {
                    if (board[i + 1][j + 1] == lastColor && board[i + 2][j + 2] == lastColor && board[i + 3][j + 3] == lastColor && board[i + 4][j + 4] == lastColor) {
                        return true;
                    }
                }
            }
        }
        for (int i = 4; i < SIZE; i++) {
            for (int j = 0; j < SIZE - 4; j++) {
                if (board[i][j] == lastColor) {
                    if (board[i - 1][j + 1] == lastColor && board[i - 2][j + 2] == lastColor && board[i - 3][j + 3] == lastColor && board[i - 4][j + 4] == lastColor) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public void changeColor() {
        if (curColor == BLACK) {
            curColor = WHILE;
            lastColor = BLACK;
        } else {
            curColor = BLACK;
            lastColor = WHILE;
        }
    }
}
