package gomoku;

public interface Config {
    int SIZE = 15;

    int FRAME_WIDTH = 800;
    int FRAME_HEIGHT = 700;

    int CONTROL_PANEL_WIDTH = 150;

    int BLACK = 0;
    int WHILE = 1;
    int EMPTY = 2;
    int DRAW = 3;//平局
    int ING = 4;//尚未终局
    int NOBEGIN = 5;//未开始
}
