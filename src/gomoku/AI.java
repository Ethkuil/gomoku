package gomoku;

import java.util.Comparator;
import java.util.LinkedList;

public class AI implements Config {
    private static final int DEPTH = 5;
    
    public int[] posToAct;//存储要下的地方
    private final String[] patterns;//评分模式
    private final int[] valueOfPattern;//模式的估值
    private final LinkedList<int[]>[] legalMoves;//各层的可行步. int[]有3个元素，[横坐标、纵坐标、直接估值].
    private Chessboard chessboard;
    
    private static final int NOT_BAD = 20;
    private static final int GOOD = NOT_BAD * 6;
    private static final int GREAT = GOOD * 6;
    private static final int NEAR_WIN = GREAT * 6;
    private static final int WIN = NEAR_WIN * 6;
    private static final int INF_VALUE = WIN - 2 * NEAR_WIN;
    private static final String me = "1";
    private static final String enemy = "0";
    private static final String empty = "2";
    
    public void thinkAndAct(Chessboard chessboard) {
        this.chessboard = chessboard;
        if (chessboard.numOfChess == 0) {
            chessboard.act(SIZE / 2, SIZE / 2);
        } else {
            int value = think(-INF_VALUE, INF_VALUE, DEPTH);//将最优解存入posToAct数组
            //由于调低了剪枝阈值，在必败的情况下可能找不到能下的地方
            if (value == -INF_VALUE) {
                //找到对方能赢的地方，能堵一个是一个
                chessboard.changeColor();
                for (int[] pos : legalMoves[0]) {
                    evaluateOnePos(pos);
                }
                legalMoves[0].sort(new Comparator<>() {
                    @Override
                    public int compare(int[] o1, int[] o2) {
                        return o2[2] - o1[2];
                    }
                });
                chessboard.changeColor();
                posToAct[0] = legalMoves[0].getFirst()[0];
                posToAct[1] = legalMoves[0].getFirst()[1];
            }
            chessboard.act(posToAct[0], posToAct[1]);
        }
        generateLegalMoves(0);//走一步生成一次
    }
    
    AI() {
        legalMoves = new LinkedList[DEPTH + 1];
        for (int i = 0; i < legalMoves.length; i++) {
            legalMoves[i] = new LinkedList<>();
        }
        posToAct = new int[2];
        patterns = new String[15];
        valueOfPattern = new int[15];
        patterns[0] = me + me + me + me + me;//5
        valueOfPattern[0] = WIN;
        patterns[1] = empty + me + me + me + me + empty;//活4
        valueOfPattern[1] = NEAR_WIN;
        patterns[2] = empty + me + me + me + me;//4-侧断, 1
        valueOfPattern[2] = GREAT;
        patterns[3] = me + me + me + me + empty;//4-侧断, 2
        valueOfPattern[3] = GREAT;
        patterns[4] = me + empty + me + me + me;//4-侧中断, 1
        valueOfPattern[4] = GREAT;
        patterns[5] = me + me + me + empty + me;//4-侧中断, 2
        valueOfPattern[5] = GREAT;
        patterns[6] = me + me + empty + me + me;//4-中断
        valueOfPattern[6] = GREAT;
        patterns[7] = empty + me + me + me + empty;//活3
        valueOfPattern[7] = GREAT;
        patterns[8] = empty + me + empty + me + me + empty;//3-中断, 1
        valueOfPattern[8] = GREAT;
        patterns[9] = empty + me + me + empty + me + empty;//3-中断, 2
        valueOfPattern[9] = GREAT;
        patterns[10] = empty + empty + me + me + empty + empty;//活2-连
        valueOfPattern[10] = GOOD;
        patterns[11] = empty + me + empty + me + empty + empty;//活2-断, 1
        valueOfPattern[11] = GOOD;
        patterns[12] = empty + empty + me + empty + me + empty;//活2-断, 2
        valueOfPattern[12] = GOOD;
        patterns[13] = empty + empty + me + empty + empty + empty;//1, 1
        valueOfPattern[13] = NOT_BAD;
        patterns[14] = empty + empty + empty + me + empty + empty;//1, 2
        valueOfPattern[14] = NOT_BAD;
    }
    
    //alpha-beta剪枝+启发式搜索
    //alpha为下界，估值比这低的棋步不值得走
    //beta为上界，估值比这高的棋，对手理应阻止
    //返回：当前局面评分
    private int think(int alpha, int beta, int depth) {
        int best_value = -INF_VALUE;
        int value;
        int i = DEPTH - depth;
        
        //生成可行步
        generateLegalMoves(i);
        //生成单个棋步的估值
        for (int[] pos : legalMoves[i]) {
            evaluateOnePos(pos);
        }
        //按此初步估值降序为可行步排序，以便搜索时按简单估值降序搜索
        legalMoves[i].sort(new Comparator<>() {
            @Override
            public int compare(int[] o1, int[] o2) {
                return o2[2] - o1[2];
            }
        });
        
        if (depth == 0 || Math.abs(legalMoves[i].getFirst()[2]) > INF_VALUE) {//叶节点，或胜负已分。此时无需进一步搜索。
            if (depth == DEPTH) {
                posToAct[0] = legalMoves[i].getFirst()[0];
                posToAct[1] = legalMoves[i].getFirst()[1];
            }
            return legalMoves[i].getFirst()[2];//以最优步的分数作为局面分数
        }
        for (int[] pos : legalMoves[i]) {
            chessboard.act(pos[0], pos[1]);
            value = -think(-beta, -alpha, depth - 1);//“本节点走这步棋后”的估值到手
            chessboard.unAct();
            //现在，除估值到手外，如同无事发生
            
            //若这步棋引发剪枝（对手理论上必然阻止本节点出现）
            if (value >= beta) {
                if (depth == DEPTH) {
                    posToAct[0] = pos[0];
                    posToAct[1] = pos[1];
                }
                return value;
            }
            //若发现更好的能走的走法
            if (value > best_value) {
                if (depth == DEPTH) {
                    posToAct[0] = pos[0];
                    posToAct[1] = pos[1];
                }
                best_value = value;
                if (value > alpha) alpha = value;
            }
        }
        //本节点可行步考虑完毕
        return best_value;
    }
    
    //作用：获取落子于(pos[0],pos[1])的估值，存在pos[2]
    private void evaluateOnePos(int[] pos) {
        int x = pos[0], y = pos[1];
        //所用评估表详见report
        //获取四条边的实际情况
        String[] rPatterns = new String[4];//落完会是怎样
        String[] rLastPatterns = new String[4];//落之前会怎样
        
        StringBuilder sb = new StringBuilder();
        StringBuilder lastSb = new StringBuilder();
        //横
        for (int i = -4; i <= 4; i++) {
            int x1 = x + i;
            if (x1 > -1 && x1 < SIZE) {
                if (i == 0) {
                    sb.append(me);
                    lastSb.append(empty);
                    continue;
                }
                int color = chessboard.board[x1][y];
                if (color == chessboard.curColor) {
                    sb.append(me);
                    lastSb.append(me);
                } else if (color == EMPTY) {
                    sb.append(empty);
                    lastSb.append(empty);
                } else {
                    sb.append(enemy);
                    lastSb.append(enemy);
                }
            }
        }
        rPatterns[0] = sb.toString();
        rLastPatterns[0] = lastSb.toString();
        
        sb = new StringBuilder();
        lastSb = new StringBuilder();
        //竖
        for (int i = -4; i <= 4; i++) {
            int y1 = y + i;
            if (y1 > -1 && y1 < SIZE) {
                if (i == 0) {
                    sb.append(me);
                    lastSb.append(empty);
                    continue;
                }
                int color = chessboard.board[x][y1];
                if (color == chessboard.curColor) {
                    sb.append(me);
                    lastSb.append(me);
                } else if (color == EMPTY) {
                    sb.append(empty);
                    lastSb.append(empty);
                } else {
                    sb.append(enemy);
                    lastSb.append(enemy);
                }
            }
        }
        rPatterns[1] = sb.toString();
        rLastPatterns[1] = lastSb.toString();
        
        sb = new StringBuilder();
        lastSb = new StringBuilder();
        //撇
        for (int i = -4; i <= 4; i++) {
            int x1 = x + i, y1 = y - i;
            if (x1 > -1 && x1 < SIZE && y1 > -1 && y1 < SIZE) {
                if (i == 0) {
                    sb.append(me);
                    lastSb.append(empty);
                    continue;
                }
                int color = chessboard.board[x1][y1];
                if (color == chessboard.curColor) {
                    sb.append(me);
                    lastSb.append(me);
                } else if (color == EMPTY) {
                    sb.append(empty);
                    lastSb.append(empty);
                } else {
                    sb.append(enemy);
                    lastSb.append(enemy);
                }
            }
        }
        rPatterns[2] = sb.toString();
        rLastPatterns[2] = lastSb.toString();
        
        sb = new StringBuilder();
        lastSb = new StringBuilder();
        //捺
        for (int i = -4; i <= 4; i++) {
            int x1 = x + i, y1 = y + i;
            if (x1 > -1 && x1 < SIZE && y1 > -1 && y1 < SIZE) {
                if (i == 0) {
                    sb.append(me);
                    lastSb.append(empty);
                    continue;
                }
                int color = chessboard.board[x1][y1];
                if (color == chessboard.curColor) {
                    sb.append(me);
                    lastSb.append(me);
                } else if (color == EMPTY) {
                    sb.append(empty);
                    lastSb.append(empty);
                } else {
                    sb.append(enemy);
                    lastSb.append(enemy);
                }
            }
        }
        rPatterns[3] = sb.toString();
        rLastPatterns[3] = lastSb.toString();
        
        //匹配上了就加分；若原本能匹配，扣分
        int value = 0;
        for (int i = 0; i < rPatterns.length; i++) {
            boolean flag1 = false, flag2 = false;
            //同时分别匹配
            for (int j = 0; j < patterns.length; j++) {
                if (!flag1 && rPatterns[i].contains(patterns[j])) {
                    value += valueOfPattern[j];
                    flag1 = true;
                }
                if (!flag2 && rLastPatterns[i].contains(patterns[j])) {
                    value -= valueOfPattern[j];
                    flag2 = true;
                }
                if (flag1 && flag2) {
                    break;
                }
            }
        }
        
        pos[2] = value;
    }
    
    //作用：生成所有可行步，放在legalMoves里. 可行步的格式是[x, y, value].
    //在上一局面的可行步的基础上，生成当前局面的可行步
    //注：每次调用前都要下一步棋
    private void generateLegalMoves(int i) {
        //生成下一步可能下棋的点时，只考虑现有棋子旁边的点足矣
        //下一步可能下棋的点（新） = 下一步可能出现的点（旧） - 刚下的点 + 刚下的点旁边的点（且不能是已有的点）
        if (i != 0) {//i==0时自身即是旧
            legalMoves[i] = (LinkedList<int[]>) legalMoves[i - 1].clone();//拷贝上一层的结果，在此基础修改
            //legalMoves[i].getFirst()[2]=1000000;
        }
        int[] lastMove = chessboard.getLastMove();
        int lastX = lastMove[0];
        int lastY = lastMove[1];
        legalMoves[i].removeIf(move -> move[0] == lastX && move[1] == lastY);//移除刚下的点
        int[] xS = {lastX - 1, lastX, lastX + 1};
        int[] yS = {lastY - 1, lastY, lastY + 1};
        int len = legalMoves[i].size();
        for (int x : xS) {
            for (int y : yS) {
                if (x > -1 && x < SIZE && y > -1 && y < SIZE) {
                    int[] e = new int[]{x, y, -1};
                    if (chessboard.board[x][y] == EMPTY) {//是可行步
                        //而且不能重复
                        int count = 0;
                        boolean ok = true;
                        for (int[] legalMove : legalMoves[i]) {
                            if (legalMove[0] == e[0] && legalMove[1] == e[1]) {
                                ok = false;
                                break;//重了，丢掉
                            }
                            count++;
                            if (count >= len) break;//旧的检查完了就不可能重复了
                        }
                        if (ok) {
                            legalMoves[i].add(e);
                        }
                    }
                }
            }
        }
    }
    
    public void initAI() {
        for (LinkedList<int[]> legalMove : legalMoves) {
            legalMove.clear();
        }
        posToAct[0] = 0;
        posToAct[1] = 0;
    }
}
