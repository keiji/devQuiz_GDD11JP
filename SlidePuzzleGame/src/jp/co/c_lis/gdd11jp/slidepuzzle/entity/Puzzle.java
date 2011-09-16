
package jp.co.c_lis.gdd11jp.slidepuzzle.entity;

import java.util.Arrays;

import jp.co.c_lis.gdd11jp.slidepuzzle.util.Utils;

public class Puzzle {
    private static final boolean DEBUG_FLG = true;

    public final int w;

    public int getWidth() {
        return w;
    }

    public final int hi;

    public int getHeight() {
        return hi;
    }

    public final int size;

    private String map;

    public String getMap() {
        return map;
    }

    private byte[] panels = null;

    public byte[] getPanels() {
        return panels;
    }

    public byte getPanel(int y, int x) {
        return panels[pos(y, x)];
    }

    // 空白の位置
    private int blankY = 0;

    public int getBlankY() {
        return blankY;
    }

    private int blankX = 0;

    public int getBlankX() {
        return blankX;
    }

    public boolean isExpectedOddStep() {
        // int dist = (hi - 1) - blankY + (w - 1) - blankX;
        // return (dist % 2 == 1);
        return ((((hi - 1) - blankY + (w - 1) - blankX) % 2) == 1);
    }

    private int number = 0;

    public int getNumber() {
        return number;
    }

    private boolean fixingLineOrColFlg = true;

    public void setFixingLineOrCol(boolean flg) {
        fixingLineOrColFlg = flg;
    }

    public boolean isFixingLineOrCol() {
        return fixingLineOrColFlg;
    }

    private byte[] completedPanels = null;

    public Puzzle duplicate() {
        Puzzle result = new Puzzle(w, hi);
        result.number = number;
        result.fixingLineOrColFlg = fixingLineOrColFlg;
        result.blankX = blankX;
        result.blankY = blankY;
        System.arraycopy(panels, 0, result.panels, 0, panels.length);
        result.mLowerBoundValue = mLowerBoundValue;
        result.completedPanels = completedPanels;
        return result;
    }

    /**
     * コンストラクタ.
     */
    private Puzzle(int w, int hi) {
        this.w = w;
        this.hi = hi;
        this.size = w * hi;
        panels = new byte[size];
    }

    /**
     * コンストラクタ.
     * 
     * @param line
     */
    public Puzzle(int number, String line) {
        this.number = number;

        String[] val = line.split(",");
        w = Integer.parseInt(val[0]);
        hi = Integer.parseInt(val[1]);
        size = hi * w;
        map = val[2];

        panels = new byte[size];

        setMap(map);

        completedPanels = Utils.getByteArray(sortMap(map));
        mLowerBoundValue = calcMdValues();
    }

    private int mLowerBoundValue = 0;

    public int getLowerBoundValue() {
        return mLowerBoundValue;
    }

    public void reset() {
        setMap(map);
    }

    private int calcMdValues() {
        int mdValue = 0;
        int count = 0;

        mFixedLines = 0;
        mFixedCols = 0;

        for (int i = 0; i < size; i++) {
            byte val = panels[i];
            if (val != WALL) {
                mdValue += calcMdValue(panels[i], i / w, i % w);
            }
            if ((i % w) == 0) {
                if (count == w && mFixedLines == ((i / w) - 1)) {
                    mFixedLines++;
                }
                count = 0;
            }
            if (val == completedPanels[i]) {
                count++;
            } else {
                count = 0;
            }
        }

        for (int i = 0; i < w; i++) {
            count = 0;
            for (int j = 0; j < hi; j++) {
                int idx = w * j + i;
                byte val = panels[idx];
                if (val == completedPanels[idx]) {
                    count++;
                } else {
                    count = 0;
                }
            }
            if (count == hi && i == mFixedCols) {
                mFixedCols++;
            }
        }

        return mdValue;
    }

    private int mFixedCols = 0;

    public int getFixedCols() {
        return mFixedCols;
    }

    private int mFixedLines = 0;

    public int getFixedLines() {
        return mFixedLines;
    }

    private int calcMdValue(byte val, int y, int x) {
        for (int i = 0; i < size; i++) {
            if (completedPanels[i] == val) {
                return Math.abs(y - (i / w)) + Math.abs(x - (i % w));
            }
        }
        return -1;
    }

    private static final byte WALL = -1;

    private static final byte BLANK = (byte) 128;

    /**
     * 回答に並び替える.
     * 
     * @param map
     * @return
     */
    private String sortMap(String map) {
        byte[] chars = Utils.getByteArray(map);
        byte[] result = Utils.getByteArray(map);

        // 並び替える
        Arrays.sort(chars);

        for (int i = 0; i < result.length; i++) {
            if (result[i] != WALL) {
                result[i] = BLANK;
            }
        }

        int idx = 0;
        for (int i = 0; i < result.length;) {
            byte resultChar = result[idx];
            byte c = chars[i];
            if (c == WALL || c == 0) {
                i++;
                continue;
            } else if (resultChar != WALL) {
                result[idx] = c;
                i++;
            }
            idx++;
        }

        result[result.length - 1] = 0;

        return Utils.byteArray2String(result);
    }

    private void setMap(String map) {
        byte[] chars = Utils.getByteArray(map);

        for (int i = 0; i < size; i++) {
            panels[i] = chars[i];
            if (panels[i] == 0) {
                blankY = (i / w);
                blankX = (i % w);
            } else if (panels[i] == WALL) {
                mWallCount++;
            }
        }
    }

    private int mWallCount = 0;

    /**
     * 完成したパネルの内容を表示.
     */
    public void printCompletedPanel() {
        print(completedPanels, true);
    }

    public void print() {
        print(panels, false);
    }

    /**
     * 内容を表示.
     */
    private void print(byte[] p, boolean finishedFlg) {
        String line = !finishedFlg ? "=================="
                : "*******************";

        System.out.println(line);

        for (int i = 0; i < size; i++) {
            if (i > 0 && (i % w) == 0) {
                System.out.println("");
            }

            System.out.print(Utils.byte2char(p[i]));
            System.out.print(" ");
        }
        System.out.println();
        System.out.println(line);
    }

    /**
     * Y, X座標から１次元配列上の座標に変換.
     * 
     * @param y
     * @param x
     * @return
     */
    public int pos(int y, int x) {
        return y * w + x;
    }

    public static final char DIRECTION_NOTSET = '0';
    public static final char DIRECTION_LEFT = 'L';
    public static final char DIRECTION_RIGHT = 'R';
    public static final char DIRECTION_UP = 'U';
    public static final char DIRECTION_DOWN = 'D';

    public boolean canMoveLeft() {
        if (blankX == 0) {
            return false;
        }
        if (panels[pos(blankY, blankX - 1)] == WALL) {
            return false;
        }
        
        if (isDontTouch(panels[pos(blankY, blankX - 1)])) {
            return false;
        }

        // 左のラインが固定されている
        if (fixingLineOrColFlg && (blankX - 1) < mFixedCols) {
            return false;
        }
        return true;
    }

    public boolean moveLeft() {
        // 移動
        panels[pos(blankY, blankX)] = panels[pos(blankY, blankX - 1)];

        // 空白の移動
        blankX -= 1;
        panels[pos(blankY, blankX)] = 0;

        mLowerBoundValue = calcMdValues();

        return true;
    }

    public boolean canMoveRight() {
        if (blankX == (w - 1)) {
            return false;
        }
        if (panels[pos(blankY, blankX + 1)] == WALL) {
            return false;
        }
        
        if (isDontTouch(panels[pos(blankY, blankX + 1)])) {
            return false;
        }
        
        return true;
    }

    public boolean moveRight() {
        // 移動
        panels[pos(blankY, blankX)] = panels[pos(blankY, blankX + 1)];

        // 空白の移動
        blankX += 1;
        panels[pos(blankY, blankX)] = 0;

        mLowerBoundValue = calcMdValues();

        return true;
    }

    private static final byte[] DONT_TOUCH = new byte[] {
            1, 7, 6, 12, 20, 27, 35, 30
    };

    private boolean isDontTouch(byte val) {
        return false;
//        int len = DONT_TOUCH.length;
//        for (int i = 0; i < len; i++) {
//            if (val == DONT_TOUCH[i]) {
//                System.out.println("don't touch" + Utils.byte2char(val));
//                return true;
//            }
//        }
//        return false;
    }

    public boolean canMoveUp() {
        if (blankY == 0) {
            return false;
        }
        if (panels[pos(blankY - 1, blankX)] == WALL) {
            return false;
        }

        if (isDontTouch(panels[pos(blankY - 1, blankX)])) {
            return false;
        }

        // 上のラインが固定されている
        if (fixingLineOrColFlg && (blankY - 1) < mFixedLines) {
            return false;
        }

        return true;
    }

    public boolean moveUp() {

        // 移動
        panels[pos(blankY, blankX)] = panels[pos(blankY - 1, blankX)];

        // 空白の移動
        blankY -= 1;
        panels[pos(blankY, blankX)] = 0;

        mLowerBoundValue = calcMdValues();

        return true;
    }

    public boolean canMoveDown() {
        if (blankY == (hi - 1)) {
            return false;
        }
        if (panels[pos(blankY + 1, blankX)] == WALL) {
            return false;
        }
        if (isDontTouch(panels[pos(blankY + 1, blankX)])) {
            return false;
        }

        return true;
    }

    public boolean moveDown() {
        // 移動
        panels[pos(blankY, blankX)] = panels[pos(blankY + 1, blankX)];

        // 空白の移動
        blankY += 1;
        panels[pos(blankY, blankX)] = 0;

        mLowerBoundValue = calcMdValues();

        return true;
    }

    public boolean isFinished() {
        if (panels[pos((hi - 1), (w - 1))] != 0) {
            return false;
        }

        byte point = WALL;
        for (int i = 0; i < size; i++) {
            if (point == WALL) {
                point = panels[i];
            } else if (panels[i] == WALL || panels[i] == 0) {
                continue;
            } else if (point < panels[i]) {
                point = panels[i];
            } else {
                return false;
            }
        }
        return true;
    }

    public boolean solve(String route) {
        char[] routes = route.toCharArray();

        for (int i = 0; i < routes.length; i++) {
            switch (routes[i]) {
                case 'L':
                    moveLeft();
                    break;
                case 'R':
                    moveRight();
                    break;
                case 'U':
                    moveUp();
                    break;
                case 'D':
                    moveDown();
                    break;
            }
        }

        return isFinished();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + hi;
        result = prime * result + number;
        result = prime * result + Arrays.hashCode(panels);
        result = prime * result + w;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        Puzzle other = (Puzzle) obj;
        if (hi != other.hi) return false;
        if (number != other.number) return false;
        if (!Arrays.equals(panels, other.panels)) return false;
        if (w != other.w) return false;
        return true;
    }
}
