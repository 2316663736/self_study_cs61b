package game2048;

import java.util.Formatter;
import java.util.Iterator;
import java.util.Observable;


/** The state of a game of 2048.
 *  @author  qianye
 */
public class Model extends Observable {
    /** Current contents of the board. */
    private final Board board;
    /** Current score. */
    private int score;
    /** Maximum score so far.  Updated when game ends. */
    private int maxScore;
    /** True iff game is ended. */
    private boolean gameOver;

    /* Coordinate System: column C, row R of the board (where row 0,
     * column 0 is the lower-left corner of the board) will correspond
     * to board.tile(c, r).  Be careful! It works like (x, y) coordinates.
     */

    /** Largest piece value. */
    public static final int MAX_PIECE = 2048;

    /** A new 2048 game on a board of size SIZE with no pieces
     *  and score 0. */
    public Model(int size) {
        board = new Board(size);
        score = maxScore = 0;
        gameOver = false;
    }

    /** A new 2048 game where RAWVALUES contain the values of the tiles
     * (0 if null). VALUES is indexed by (row, col) with (0, 0) corresponding
     * to the bottom-left corner. Used for testing purposes. */
    public Model(int[][] rawValues, int score, int maxScore, boolean gameOver) {
        int size = rawValues.length;
        board = new Board(rawValues, score);
        this.score = score;
        this.maxScore = maxScore;
        this.gameOver = gameOver;
    }

    /** Return the current Tile at (COL, ROW), where 0 <= ROW < size(),
     *  0 <= COL < size(). Returns null if there is no tile there.
     *  Used for testing. Should be deprecated and removed.
     *  */
    public Tile tile(int col, int row) {
        return board.tile(col, row);
    }

    /** Return the number of squares on one side of the board.
     *  Used for testing. Should be deprecated and removed. */
    public int size() {
        return board.size();
    }

    /** Return true iff the game is over (there are no moves, or
     *  there is a tile with value 2048 on the board). */
    public boolean gameOver() {
        checkGameOver();
        if (gameOver) {
            maxScore = Math.max(score, maxScore);
        }
        return gameOver;
    }

    /** Return the current score. */
    public int score() {
        return score;
    }

    /** Return the current maximum game score (updated at end of game). */
    public int maxScore() {
        return maxScore;
    }

    /** Clear the board to empty and reset the score. */
    public void clear() {
        score = 0;
        gameOver = false;
        board.clear();
        setChanged();
    }

    /** Add TILE to the board. There must be no Tile currently at the
     *  same position. */
    public void addTile(Tile tile) {
        board.addTile(tile);
        checkGameOver();
        setChanged();
    }

    /** Tilt the board toward SIDE. Return true iff this changes the board.
     *
     * 1. If two Tile objects are adjacent in the direction of motion and have
     *    the same value, they are merged into one Tile of twice the original
     *    value and that new value is added to the score instance variable
     * 2. A tile that is the result of a merge will not merge again on that
     *    tilt. So each move, every tile will only ever be part of at most one
     *    merge (perhaps zero).
     * 3. When three adjacent tiles in the direction of motion have the same
     *    value, then the leading two tiles in the direction of motion merge,
     *    and the trailing tile does not.
     * */
    public boolean tilt(Side side) {
        boolean changed;
        changed = false;

        // TODO: Modify this.board (and perhaps this.score) to account
        // for the tilt to the Side SIDE. If the board changed, set the
        // changed local variable to true.

        //由于有side这个东西，所以不管什么方向，我们都可以认为是向上移动，并进行处理
        board.setViewingPerspective(side);
        int size=board.size();
        Tile[][] all=get_all_Tile();
        changed=move_null(all,side)||changed;
        all=get_all_Tile();
        changed=move_merge(all,side)||changed;

//        for (int c=0;c<size;c++) {
//            Tile last=null;
//            int col_last=side.col(c,0,size);
//            int row_last=side.row(c,0,size);
//            for (int r=0; r<size; r++) {
//                Tile now=all[r][c];
//                int col_now=side.col(c,r,size);
//                int row_now=side.row(c,r,size);
//                if(now==null) {
//                        continue;
//                }
//                else if (last==null)
//                    last=now;
//                else if (last.value()==now.value()) {
//                    changed = board.move(now.col(), now.row(), last) || changed;
//                }
//                else if ((Math.abs(last.row()-now.row())+Math.abs(last.col()-now.col()))>1)
//                    changed=board.move(now.col(), now.row(), last)||changed;
//
//            }
//        }

        checkGameOver();
        if (changed) {
            setChanged();
        }
        return changed;
    }

    //获取在特定方向视角下所有的Tile
    private Tile[][] get_all_Tile()
    {
        int size=board.size();
        Tile[][] all=new Tile[size][size];
        Iterator<Tile> iter=board.iterator();

        for (int i = size-1; i >=0; i--)
            for (int j = 0; j < size; j++)
                all[i][j] = iter.next();
        return all;
    }

    //把所有Tile移动到顶上，把null移动到下面
    private boolean move_null(Tile[][] all,Side side) {
        int size=board.size();
        boolean change=false;
        Tile [][]see=get_all_Tile();
        //记录本处的上面有多少Tile不是null，便于移动
        int[][] num_null=new int[size][size];
        for(int c=0;c<size;c++) {
            num_null[0][c]=0;
            for (int r=1; r<size; r++)
                num_null[r][c]=num_null[r-1][c] + (all[r-1][c]==null?1:0);
        }

        for(int c=0;c<size;c++) {
            for (int r=0; r<size; r++) {
                Tile now=all[r][c];
                int num=num_null[r][c];
                if(now==null) 
                    continue;
                if(num==0)
                    continue;
                //now不是null，而且上面有null，那么它需要上移num
//                int row=side.row(c,r+num-1,size);
//                int col=side.col(c,r+num-1,size);
//                change=board.move(col, row, now)||change;
//                see=get_all_Tile();
                change=true;
                board.move(now.col(),now.row()+num,now);
            }
        }
        return change;
    }

    private boolean move_merge(Tile[][] all,Side side) {
        int size=board.size();
        boolean change=false;
        for(int c=0;c<size;c++) {
            Tile last=null;
            for (int r=0; r<size; r++) {
                Tile now=all[r][c];
                if(now==null)
                    break;
                if(last==null)
                    last=now;
                else if(last.value()==now.value())
                {
                    int row=last.row();
                    int col=last.col();
                    change=true;
                    board.move(col, row, now);
                    score+=2*last.value();
                    all=get_all_Tile();
                    move_null(all,side);
                    all=get_all_Tile();
                    last=null;
                    r--;
                }
                else
                    last=now;
            }
        }
        return change;
    }
    /** Checks if the game is over and sets the gameOver variable
     *  appropriately.
     */
    private void checkGameOver() {
        gameOver = checkGameOver(board);
    }

    /** Determine whether game is over. */
    private static boolean checkGameOver(Board b) {
        return maxTileExists(b) || !atLeastOneMoveExists(b);
    }

    /** Returns true if at least one space on the Board is empty.
     *  Empty spaces are stored as null.
     * */
    public static boolean emptySpaceExists(Board b) {
        // TODO: Fill in this function.
        int len=b.size();
        for(int i=0;i<len;i++) {
            for(int j=0;j<len;j++) {
                Tile now=b.tile(i,j);
                if(now==null)
                    return true;

            }
        }
        return false;
    }

    /**
     * Returns true if any tile is equal to the maximum valid value.
     * Maximum valid value is given by MAX_PIECE. Note that
     * given a Tile object t, we get its value with t.value().
     */
    public static boolean maxTileExists(Board b) {
        // TODO: Fill in this function.
        int len=b.size();
        for(int i=0;i<len;i++) {
            for(int j=0;j<len;j++) {
                Tile now=b.tile(i,j);
                if(now!=null && now.value()==MAX_PIECE)
                    return true;

            }
        }
        return false;
    }

    /**
     * Returns true if there are any valid moves on the board.
     * There are two ways that there can be valid moves:
     * 1. There is at least one empty space on the board.
     * 2. There are two adjacent tiles with the same value.
     */
    public static boolean atLeastOneMoveExists(Board b) {
        // TODO: Fill in this function.
        if(emptySpaceExists(b))
            return true;
        int len= b.size();
        for(int i=0;i<len;i++) {
            for(int j=0;j<len;j++) {
                if(i-1>=0 && b.tile(i-1,j).value()==b.tile(i,j).value())
                    return true;
                if(i+1<len  && b.tile(i+1,j).value()==b.tile(i,j).value())
                    return true;
                if(j-1>=0 && b.tile(i,j-1).value()==b.tile(i,j).value())
                    return true;
                if(j+1<len && b.tile(i,j+1).value()==b.tile(i,j).value())
                    return true;
            }
        }
        return false;
    }


    @Override
     /** Returns the model as a string, used for debugging. */
    public String toString() {
        Formatter out = new Formatter();
        out.format("%n[%n");
        for (int row = size() - 1; row >= 0; row -= 1) {
            for (int col = 0; col < size(); col += 1) {
                if (tile(col, row) == null) {
                    out.format("|    ");
                } else {
                    out.format("|%4d", tile(col, row).value());
                }
            }
            out.format("|%n");
        }
        String over = gameOver() ? "over" : "not over";
        out.format("] %d (max: %d) (game is %s) %n", score(), maxScore(), over);
        return out.toString();
    }

    @Override
    /** Returns whether two models are equal. */
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        } else if (getClass() != o.getClass()) {
            return false;
        } else {
            return toString().equals(o.toString());
        }
    }

    @Override
    /** Returns hash code of Model’s string. */
    public int hashCode() {
        return toString().hashCode();
    }
}
