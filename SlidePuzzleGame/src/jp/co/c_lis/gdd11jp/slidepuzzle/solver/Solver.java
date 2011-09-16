
package jp.co.c_lis.gdd11jp.slidepuzzle.solver;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.HashSet;

import jp.co.c_lis.gdd11jp.slidepuzzle.entity.Puzzle;
import jp.co.c_lis.gdd11jp.slidepuzzle.util.Utils;

/**
 * 深さ優先探索.
 * 
 * @author keiji_ariyama
 */
public class Solver implements ISolver {

    private final StringBuffer route = new StringBuffer();

    public void setRoute(String r) {
        int len = r.length();
        for (int i = 0; i < len; i++) {
            addRoute(r.charAt(i));
        }
    }

    private void addRoute(char c) {
        switch (c) {
            case Puzzle.DIRECTION_LEFT:
                if (mPuzzle.canMoveLeft()) {
                    mPuzzle.moveLeft();
                }
                break;
            case Puzzle.DIRECTION_RIGHT:
                if (mPuzzle.canMoveRight()) {
                    mPuzzle.moveRight();
                }
                break;
            case Puzzle.DIRECTION_UP:
                if (mPuzzle.canMoveUp()) {
                    mPuzzle.moveUp();
                }
                break;
            case Puzzle.DIRECTION_DOWN:
                if (mPuzzle.canMoveDown()) {
                    mPuzzle.moveDown();
                }
                break;
        }
        route.append(c);

    }

    public void printRoute() {
        int l = 0;
        int r = 0;
        int u = 0;
        int d = 0;

        for (int i = 0; i < route.length(); i++) {
            char c = route.charAt(i);
            switch (c) {
                case Puzzle.DIRECTION_LEFT:
                    l++;
                    break;
                case Puzzle.DIRECTION_RIGHT:
                    r++;
                    break;
                case Puzzle.DIRECTION_UP:
                    u++;
                    break;
                case Puzzle.DIRECTION_DOWN:
                    d++;
                    break;
            }
        }
        System.out.println(String.format("%d: step %d, %s",
                mPuzzle.getNumber(),
                route.length(),
                route));
    }

    public void revert() {
        if (route.length() > 0) {
            route.deleteCharAt(route.length() - 1);
        }
    }

    private static int cacheSize = 20000;

    private static HashSet<Puzzle> cache = new HashSet<Puzzle>();

    public static int getCacheContains() {
        return cache.size();
    }

    public static void setCacheSize(int size) {
        cacheSize = size;
        if (cache != null) {
            cache.clear();
        }
        cache = new HashSet<Puzzle>(size);
    }

    private static int cacheHit = 0;

    public static int getCacheHit() {
        return cacheHit;
    }

    public static void clearCache() {
        if (cache != null) {
            cache.clear();
        }
        cacheHit = 0;
    }

    private File mAnswerFile;

    /**
     * 探索条件
     */
    private volatile int mSeekLimit = 0;

    public void setSeekLimit(int val) {
        mSeekLimit = val;
    }

    private volatile long mTimeLimit = 0;

    public void setTimeLimit(long val) {
        mTimeLimit = val;
    }

    private int mLimit = 0;

    public void setLimit(int val) {
        mLimit = val;
    }

    private int mMaxStep = -1;

    public void setMaxStep(int val) {
        mMaxStep = val;
    }

    private Puzzle mPuzzle = null;

    public void setPuzzle(Puzzle puzzle) {
        mPuzzle = puzzle;
    }

    private volatile long mTimeStart = 0;

    /**
     * コンストラクタ.
     */
    public Solver() {
    }

    private char mFirstDirection = Puzzle.DIRECTION_NOTSET;

    public void setFirstDirection(char val) {
        mFirstDirection = val;
    }

    /**
     * パズルを解く.
     */
    public boolean seek(File answerFile) {
        mAnswerFile = answerFile;

        mTimeStart = System.currentTimeMillis();

        System.out.println(String.format("%s 方向に探索を開始します... 探索の限界ステップ: %d",
                String.valueOf(mFirstDirection), mLimit));

        try {
            move(mFirstDirection, 0);
        } catch (SeekLimitOverException e) {
            System.out.println(String.format("得られた回答数が指定の %d に達しました。", mSeekLimit));
        }

        System.out.println(String.format("%s 方向の探索を終了します...", String.valueOf(mFirstDirection)));

        return (mFoundAnswerNumber > 0);
    }

    /**
     * 中止.
     */
    public void abort() {
        mSeekLimit = 0;
    }

    private int mFoundAnswerNumber = 0;

    public int getFoundAnswer() {
        return mFoundAnswerNumber;
    }

    private void outputForResume(String route) {
        System.out.println(route.length() + ": " + route);
        outputRoute(route, new File(mAnswerFile.getName() + ".resume"));
    }

    private void outputRoute(String route, File file) {
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
                    file, true), Charset.forName("UTF-8")));
            writer.write(route.toString());
            writer.write('\n');
            writer.flush();
        } catch (IOException e) {
            System.err.println(e.getMessage());
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    System.err.println(e.getMessage());
                }
            }
        }
    }

    private int mLogLineThreshold = -1;

    public void setLogLineThreshold(int val) {
        mLogLineThreshold = val;
    }

    private boolean move(char direction, int step) throws SeekLimitOverException {

        switch (direction) {
            case Puzzle.DIRECTION_LEFT:
                mPuzzle.moveLeft();
                break;
            case Puzzle.DIRECTION_RIGHT:
                mPuzzle.moveRight();
                break;
            case Puzzle.DIRECTION_UP:
                mPuzzle.moveUp();
                break;
            case Puzzle.DIRECTION_DOWN:
                mPuzzle.moveDown();
                break;
        }

        if (direction != Puzzle.DIRECTION_NOTSET) {
            route.append(direction);
        }

        if (mPuzzle.isFinished()) {
            mFoundAnswerNumber++;

            synchronized (mAnswerFile) {
                printRoute();
                outputRoute(route.toString(), mAnswerFile);

            }
            return true;
        }

        // 一定の値を超えたらルートを記録する
        if (mLogLineThreshold != -1 && mPuzzle.getFixedLines() == mLogLineThreshold) {
            outputForResume(route.toString());
            mPuzzle.print();

            return false;
        }

        // 一定の値を超えたらルートを記録する
        // if (mPuzzle.getFixedCols() == 1) {
        // outputForResume(route.toString());
        // mPuzzle.print();
        //
        // return false;
        // }

        // 回答数チェック
        if (mSeekLimit != -1 && mFoundAnswerNumber >= mSeekLimit) {
            throw new SeekLimitOverException();
        }

        boolean success = false;
        int nowCacheSize = 0;
        synchronized (cache) {
            if (cache.contains(mPuzzle)) {
                cacheHit++;
                return false;
            }
            if (cache.size() < cacheSize) {
                cache.add(mPuzzle);
            }
            nowCacheSize = cache.size();
        }

        // if (nowCacheSize < cacheSize) {
        // mTimeStart = System.currentTimeMillis();
        // }

        // 制限時間チェック
        if (mTimeLimit != -1 && (System.currentTimeMillis() - mTimeStart) > getTimeLimit()) {
            return false;
        }

        step += 1;

        if ((mPuzzle.getLowerBoundValue() + step) < getStepLimit()) {

            int actions = Utils.makeActionsList(direction, mPuzzle);
            // printActions(actions);

            int len = Utils.getActionLength(actions);
            for (int i = 0; i < len; i++) {
                char a = Utils.getActionChar(actions, i);
                action(a, step);
            }
        }
        return success;
    }

    private boolean action(char action, int step) throws SeekLimitOverException {
        boolean success = false;
        switch (action) {
            case Puzzle.DIRECTION_LEFT:
                if (move(Puzzle.DIRECTION_LEFT, step)) {
                    success = true;
                }
                mPuzzle.moveRight();
                revert();
                break;
            case Puzzle.DIRECTION_RIGHT:
                if (move(Puzzle.DIRECTION_RIGHT, step)) {
                    success = true;
                }
                mPuzzle.moveLeft();
                revert();
                break;
            case Puzzle.DIRECTION_UP:
                if (move(Puzzle.DIRECTION_UP, step)) {
                    success = true;
                }
                mPuzzle.moveDown();
                revert();
                break;
            case Puzzle.DIRECTION_DOWN:
                if (move(Puzzle.DIRECTION_DOWN, step)) {
                    success = true;
                }
                mPuzzle.moveUp();
                revert();
                break;
        }
        return success;
    }

    private static final int STEP_LIMIT_WEIGHT = 1;

    private long getTimeLimit() {
        long time = mTimeLimit;

        // ボーナスを加算
        time += mTimeLimit
                * (Math.pow(mPuzzle.getFixedLines(), STEP_LIMIT_WEIGHT) + Math.pow(
                        mPuzzle.getFixedCols(), STEP_LIMIT_WEIGHT));

        return time;
    }

    private long getStepLimit() {
        int step = mLimit;

        // ボーナスを加算
        step += mPuzzle.hi * (Math.pow(mPuzzle.getFixedLines(), STEP_LIMIT_WEIGHT) +
                mPuzzle.w * Math.pow(mPuzzle.getFixedCols(), STEP_LIMIT_WEIGHT));

        return step;
    }

    private static class SeekLimitOverException extends Exception {
        private static final long serialVersionUID = -4999552366813656276L;
    }

}
