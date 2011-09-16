
package jp.co.c_lis.gdd11jp.slidepuzzle.solver;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.LinkedList;
import java.util.List;

import jp.co.c_lis.gdd11jp.slidepuzzle.entity.Puzzle;
import jp.co.c_lis.gdd11jp.slidepuzzle.util.Utils;

/**
 * 幅優先探索.
 * 
 * @author keiji_ariyama
 */
public class Solver2 implements ISolver {

    private static class Result {
        final Puzzle puzzle;
        final String route;

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((puzzle == null) ? 0 : puzzle.hashCode());
            return result;
        }

        Result(Puzzle puzzle, String route) {
            this.puzzle = puzzle;
            this.route = route;
        }
    }

    private List<Result> mResult = new LinkedList<Result>();

    private File mAnswerFile = null;

    /*
     * (non-Javadoc)
     * @see jp.co.c_lis.gdd11jp.slidepuzzle.solver.ISolver#seek(java.io.File)
     */
    @Override
    public boolean seek(File answerFile) {
        mTimeStart = System.currentTimeMillis();
        mAnswerFile = answerFile;

        boolean result = false;
        try {
            result = move();
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        } finally {
            mResult.clear();
        }

        return result;
    }

    private void outputAnswer(Result res) {
        String route = res.route;
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
                    mAnswerFile, true)));
            writer.write(route);
            writer.write('\n');
            writer.flush();

            System.out.println("route : " + res.route);

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

    private int mLimit = 1;

    public void setLimit(int val) {
        mLimit = val;
    }

    private boolean move() throws InterruptedException {

        int num = 0;
        while (mResult.size() > 0 && num < mLimit) {
            Result res = mResult.get(0);
            mResult.remove(0);

            num += move(res) ? 1 : 0;
        }
        return true;
    }

    private long mTimeStart = 0;
    private long mTimeLimit = -1;

    public void setTimeLimit(long time) {
        mTimeLimit = time;
    }

    private boolean move(Result res) throws InterruptedException {
        char direction = Puzzle.DIRECTION_NOTSET;
        if (res.route.length() > 0) {
            direction = res.route.charAt(res.route.length() - 1);
        }

        if (res.puzzle.isFinished()) {
            outputAnswer(res);
            return true;
        }

        // 制限時間チェック
        if (mTimeLimit != -1 && (System.currentTimeMillis() - mTimeStart) > mTimeLimit) {
            throw new InterruptedException(String.format("制限時間 %dms を超過しました。探索を中止します。",
                    mTimeLimit));
        }

        int actions = Utils.makeActionsList(direction, res.puzzle);
        int len = Utils.getActionLength(actions);
        for (int i = 0; i < len; i++) {
            char action = Utils.getActionChar(actions, i);
            Puzzle puzzle = res.puzzle.duplicate();
            Result result = null;
            switch (action) {
                case Puzzle.DIRECTION_LEFT:
                    puzzle.moveLeft();
                    result = new Result(puzzle, res.route + 'L');
                    break;
                case Puzzle.DIRECTION_RIGHT:
                    puzzle.moveRight();
                    result = new Result(puzzle, res.route + 'R');
                    break;
                case Puzzle.DIRECTION_UP:
                    puzzle.moveUp();
                    result = new Result(puzzle, res.route + 'U');
                    break;
                case Puzzle.DIRECTION_DOWN:
                    puzzle.moveDown();
                    result = new Result(puzzle, res.route + 'D');
                    break;
            }
            if (result != null) {
                mResult.add(result);
            }
        }

        return false;
    }

    /*
     * (non-Javadoc)
     * @see
     * jp.co.c_lis.gdd11jp.slidepuzzle.solver.ISolver#setPuzzle(jp.co.c_lis.
     * gdd11jp.slidepuzzle.entity.Puzzle)
     */
    @Override
    public void setPuzzle(Puzzle puzzle) {
        mResult.add(new Result(puzzle, ""));
    }

}
