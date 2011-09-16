
package jp.co.c_lis.gdd11jp.slidepuzzle.util;

import java.util.Random;

import jp.co.c_lis.gdd11jp.slidepuzzle.entity.Puzzle;

public class Utils {

    public static String byteArray2String(byte[] array) {
        StringBuffer sb = new StringBuffer();
        int len = array.length;
        for (int i = 0; i < len; i++) {
            sb.append(byte2char(array[i]));
        }
        return sb.toString();
    }

    public static final char[] ACTIONS_CHAR_ARRAY = new char[] {
            'L', 'R', 'U', 'D'
    };

    // int型の変数にactionと順番を保存する
    // 0xFFFFFFFF
    // 0x0000000F 0
    // 0x000000F0 1
    // 0x00000F00 2
    // 0x0000F000 3
    // 0x0F000000 length
    
    public static int makeActionsList(char direction, Puzzle puzzle) {
        int actions = 0;
        int idx = 0;

        if (direction != Puzzle.DIRECTION_RIGHT && puzzle.canMoveLeft()) {
            actions = setAction(actions, Puzzle.DIRECTION_LEFT, idx);
            idx++;
        }

        if (direction != Puzzle.DIRECTION_LEFT && puzzle.canMoveRight()) {
            actions = setAction(actions, Puzzle.DIRECTION_RIGHT, idx);
            idx++;
        }

        if (direction != Puzzle.DIRECTION_UP && puzzle.canMoveDown()) {
            actions = setAction(actions, Puzzle.DIRECTION_DOWN, idx);
            idx++;
        }

        if (direction != Puzzle.DIRECTION_DOWN && puzzle.canMoveUp()) {
            actions = setAction(actions, Puzzle.DIRECTION_UP, idx);
            idx++;
        }

        actions = setActionLength(actions, idx);

        return shaffleActionsList(actions);
    }

    public static int getActionLength(int actions) {
        int shift = 4 * 6;
        int mask = 0xF << shift;
        return ((actions & mask) >> shift);
    }

    public static int setActionLength(int actions, int len) {
        // 現在設定されている値を削除
        int value = getAction(actions, 6);
        actions ^= value;

        int shift = 4 * 6;
        actions |= (len << shift);
        return actions;
    }

    public static char getActionChar(int actions, int pos) {
        return ACTIONS_CHAR_ARRAY[getAction(actions, pos)];
    }

    public static int getAction(int actions, int pos) {
        int shift = 4 * pos;
        int mask = 0x0000000F << shift;
        return (actions & mask) >>> shift;
    }

    public static int setAction(int actions, char action, int pos) {
        int a = 0;
        switch (action) {
            case 'L':
                a = 0;
                break;
            case 'R':
                a = 1;
                break;
            case 'U':
                a = 2;
                break;
            case 'D':
                a = 3;
                break;
        }

        int shift = 4 * pos;

        // 現在設定されている値を削除
        int value = getAction(actions, pos);
        actions -= (value << shift);

        actions |= (a << shift);
        return actions;
    }

    private static final Random rand = new Random(System.currentTimeMillis());

    public static int shaffleActionsList(int actions) {
        int len = getActionLength(actions);
        if (len <= 1) {
            return actions;
        }
        int count = rand.nextInt(len);
        count *= count;

        for (int i = 0; i < count;) {
            int pos1 = rand.nextInt(len);
            int pos2 = rand.nextInt(len);
            if (pos1 != pos2) {
                char c1 = getActionChar(actions, pos1);
                char c2 = getActionChar(actions, pos2);
                actions = setAction(actions, c1, pos2);
                actions = setAction(actions, c2, pos1);
                i++;
            }
        }
        // System.out.print("af: ");
        // printActions(actions);
        return actions;
    }

    public static void printActions(int actions) {
        int len = getActionLength(actions);
        System.out.print("|");
        for (int i = 0; i < len; i++) {
            char c = getActionChar(actions, i);
            System.out.print(c + " ");
        }
        System.out.print("| ");
        System.out.println(len);
    }

    public static byte char2byte(char c) {
        switch (c) {
            case '0':
                return 0;
            case '1':
                return 1;
            case '2':
                return 2;
            case '3':
                return 3;
            case '4':
                return 4;
            case '5':
                return 5;
            case '6':
                return 6;
            case '7':
                return 7;
            case '8':
                return 8;
            case '9':
                return 9;
            case 'A':
                return 10;
            case 'B':
                return 11;
            case 'C':
                return 12;
            case 'D':
                return 13;
            case 'E':
                return 14;
            case 'F':
                return 15;
            case 'G':
                return 16;
            case 'H':
                return 17;
            case 'I':
                return 18;
            case 'J':
                return 19;
            case 'K':
                return 20;
            case 'L':
                return 21;
            case 'M':
                return 22;
            case 'N':
                return 23;
            case 'O':
                return 24;
            case 'P':
                return 25;
            case 'Q':
                return 26;
            case 'R':
                return 27;
            case 'S':
                return 28;
            case 'T':
                return 29;
            case 'U':
                return 30;
            case 'V':
                return 31;
            case 'W':
                return 32;
            case 'X':
                return 33;
            case 'Y':
                return 34;
            case 'Z':
                return 35;
            default:
                return -1;
        }
    }

    public static char byte2char(byte b) {
        switch (b) {
            case 0:
                return '0';
            case 1:
                return '1';
            case 2:
                return '2';
            case 3:
                return '3';
            case 4:
                return '4';
            case 5:
                return '5';
            case 6:
                return '6';
            case 7:
                return '7';
            case 8:
                return '8';
            case 9:
                return '9';
            case 10:
                return 'A';
            case 11:
                return 'B';
            case 12:
                return 'C';
            case 13:
                return 'D';
            case 14:
                return 'E';
            case 15:
                return 'F';
            case 16:
                return 'G';
            case 17:
                return 'H';
            case 18:
                return 'I';
            case 19:
                return 'J';
            case 20:
                return 'K';
            case 21:
                return 'L';
            case 22:
                return 'M';
            case 23:
                return 'N';
            case 24:
                return 'O';
            case 25:
                return 'P';
            case 26:
                return 'Q';
            case 27:
                return 'R';
            case 28:
                return 'S';
            case 29:
                return 'T';
            case 30:
                return 'U';
            case 31:
                return 'V';
            case 32:
                return 'W';
            case 33:
                return 'X';
            case 34:
                return 'Y';
            case 35:
                return 'Z';
            default:
                return '#';
        }
    }

    public static byte[] getByteArray(String map) {
        byte[] result = new byte[map.length()];
        int len = result.length;
        for (int i = 0; i < len; i++) {
            result[i] = char2byte(map.charAt(i));
        }
        return result;
    }

}
