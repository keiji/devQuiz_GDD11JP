
package jp.co.c_lis.gdd11jp.slidepuzzle;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import jp.co.c_lis.gdd11jp.slidepuzzle.entity.Answer;
import jp.co.c_lis.gdd11jp.slidepuzzle.entity.Problems;
import jp.co.c_lis.gdd11jp.slidepuzzle.entity.Puzzle;
import jp.co.c_lis.gdd11jp.slidepuzzle.solver.Solver;

/**
 * スライドパズルの解を探索する処理を行うエントリクラス.
 * 
 * @author Keiji Ariyama <keiji_ariyama@c-lis.co.jp>
 */
public class RouteShorter {
    private static final boolean DEBUG_FLG = true;

    private static long sId = System.currentTimeMillis() % 100000; // 131484236154
                                                                   // => 31543
    private static final int DEFAULT_INIT_STEP = 78;

    /**
     * 探索する回答の数を制限する
     * -1 を指定した場合、制限をしない
     */
    private static int sSeekLimit = -1;

    /**
     * 一回の探索に使う制限時間
     * -1 を指定した場合、制限をしない
     */
    private static long sTimeLimit = -1;

    /**
     * 処理を中止するために置くファイル名.
     * 作業ディレクトリに、該当ファイル名が存在する場合、処理を中止する
     */
    private static final String ABORT_ALL_FILE_NAME = "abort-all.txt";

    /**
     * 処理を中止するために置くファイル名.
     * 作業ディレクトリに、該当ファイル名が存在する場合、処理を中止する
     */
    private static final String ABORT_FILE_NAME_POSTFIX = "abort-%d.txt"; // %d　=　sId

    /**
     * キャッシュサイズ.
     */
    private static int sCacheSize = 20000;

    /**
     * 縦及び横のラインがそろった時に、そのラインを固定する.
     * 
     * 注意！
     * 壁の配置上、このオプションがtrueの場合、絶対に解が見つからない問題が存在する
     */
    private static boolean sFixLineCol = true;

    /**
     * 問題ファイルの名前.
     */
    private static String sProblemFileName = "problems.txt";

    private static void printSetting() {
        if (sMinStep > 0) {
            System.out.println("回答のステップ数がこの値を超えていれば、短縮を試行する: " + sMinStep);
        }
        if (sMaxStep > 0) {
            System.out.println("回答のステップ数がこの値を超えていれば、短縮を試行しない: " + sMaxStep);
        }
        System.out.println("短縮を試行するステップ数: " + sCutStep);

        System.out.println("キャッシュサイズ: " + sCacheSize);

        if (sSeekLimit != -1) {
            System.out.println("探索する回答数: " + sSeekLimit);
        }
        if (sTimeLimit != -1) {
            System.out.println("探索制限時間: " + sTimeLimit);
        }
        if (!sFixLineCol) {
            System.out.println("ラインがそろっても固定しない");
        }
    }

    private static Problems sProblems;

    private static int sMinStep = -1;
    private static int sMaxStep = -1;
    private static int sCutStep = 2;

    /**
     * 引数からオプション指定.
     */
    private static void processArgs(String[] args) {
        int len = args.length - 1;
        for (int i = 0; i < len; i++) {
            String option = "";
            if (args[i].charAt(0) == '-') {
                option = args[i].substring(1);
            }

            String arg = args[i + 1];
            if (option.equals("file")) {
                sProblemFileName = arg;
            } else if (option.equals("min-step")) {
                sMinStep = Integer.parseInt(arg);
            } else if (option.equals("max-step")) {
                sMaxStep = Integer.parseInt(arg);
            } else if (option.equals("cut-step")) {
                sCutStep = Integer.parseInt(arg);
            } else if (option.equals("cache-size")) {
                sCacheSize = Integer.parseInt(arg);
            } else if (option.equals("time")) {
                sTimeLimit = Long.parseLong(arg);
            } else if (option.equals("answer-limit")) {
                sSeekLimit = Integer.parseInt(arg);
            } else if (option.equals("fix-line-col")) {
                sFixLineCol = Boolean.parseBoolean(arg);
            }
        }
    }

    /**
     * エントリポイント.
     * 
     * @param args
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("ファイル名が指定されていません");
            return;
        }

        // オプション指定
        processArgs(args);

        long start = System.currentTimeMillis();

        try {
            sProblems = new Problems(sProblemFileName);
        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
        }

        if (sProblems == null) {
            System.out.println(String.format("ファイルが見つかりません - %s", sProblemFileName));
            return;
        }

        List<String> problems = sProblems.problemList;

        int success = 0;

        String abortFileName = String.format(ABORT_FILE_NAME_POSTFIX, sId);

        // 中止ファイル
        final File abortFile = new File(abortFileName);
        try {
            abortFile.createNewFile();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

        final File abortAllFile = new File(ABORT_ALL_FILE_NAME);

        boolean targetFlg = false;
        do {
            // 回答一覧
            Answer[] list = Answer.getAnswerList(sProblems);

            for (int i = 0; i < list.length; i++) {
                if (!abortFile.exists()) {
                    System.out.println(abortFileName + " の削除によって中止が指示されました。プロセスを終了します...");
                    break;
                }
                if (abortAllFile.exists()) {
                    System.out.println(ABORT_ALL_FILE_NAME + " によって中止が指示されました。プロセスを終了します...");
                    break;
                }

                Answer answer = list[i];

                if (isTarget(answer)) {
                    targetFlg = true;
                    printSetting();

                    String map = problems.get(answer.getNumber() - 1);
                    Puzzle puzzle = new Puzzle(answer.getNumber(), map);

                    long time = (System.currentTimeMillis() - start) / 1000;
                    System.out.println(String.format("問題 %d を探索します。ステップ %d - %d秒 - 回答済 : %d",
                            puzzle.getNumber(),
                            answer.getMinStep(),
                            time, success));

                    int maxStep = answer.getMinStep();
                    if (maxStep >= 0) {
                        int initStep = maxStep - sCutStep;
                        if ((initStep % 2) == 0) {
                            initStep += ((answer.getMinStep() % 2 == 1) ? 1 : 0);
                        }

                        // パズルの実行
                        boolean result = false;
                        do {
                            result = doPuzzle(puzzle, initStep, maxStep);
                            success += result ? 1 : 0;
                            initStep += 2;
                            System.out.println("Solver.cache = " + Solver.getCacheContains() + " " + Solver.getCacheHit());
                        } while (!result && initStep < maxStep);
                    }
                }
            }
        } while (targetFlg);

        System.out.println(String.format("%d: %d問回答しました。", sId, success));

        // 中断ファイルを削除
        abortFile.deleteOnExit();

        long time = (System.currentTimeMillis() - start) / 1000;
        System.out.println("所要時間 = " + time + "秒");
    }

    private static boolean isTarget(Answer answer) {
        // 指定したステップ数に一致すれば
        return (sMinStep == -1 || answer.getMinStep() > sMinStep) && (sMaxStep == -1 || answer.getMinStep() < sMaxStep);
    }

    /**
     * パズルを探索.
     * 
     * @param puzzle Puzzleクラスのオブジェクト
     * @param maxStep 試行する探索ステップ数
     * @return 回答が得られればtrue
     */
    private static boolean doPuzzle(final Puzzle puzzle, final int initStep, final int maxStep) {

        System.out.println(String.format("問題 %d を探索します。: %d - %d",
                puzzle.getNumber(), initStep, maxStep));

        // ロックファイル
        File lockFile = new File(String.format("./lock%d.lock", puzzle.getNumber()));

        // 回答ファイル
        final File answerFile = new File(String.format("./answer%d.txt", puzzle.getNumber()));

        try {
            // ロックファイルの新規作成
            lockFile.createNewFile();
            if (!answerFile.exists()) {
                // ファイルの新規作成
                answerFile.createNewFile();
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

        puzzle.print();

        // 縦横ライン固定の設定
        puzzle.setFixingLineOrCol(sFixLineCol);

        // 探索の開始
        setSuccess(0);

        // キャッシュのクリア
        Solver.clearCache();

        Solver.setCacheSize(sCacheSize);

        Solver left = null;
        if (puzzle.canMoveLeft()) {
            left = startThread(puzzle, Puzzle.DIRECTION_LEFT, answerFile, initStep, maxStep);
        }

        Solver right = null;
        if (puzzle.canMoveRight()) {
            right = startThread(puzzle, Puzzle.DIRECTION_RIGHT, answerFile, initStep, maxStep);
        }

        Solver up = null;
        if (puzzle.canMoveUp()) {
            up = startThread(puzzle, Puzzle.DIRECTION_UP, answerFile, initStep, maxStep);
        }

        Solver down = null;
        if (puzzle.canMoveDown()) {
            down = startThread(puzzle, Puzzle.DIRECTION_DOWN, answerFile, initStep, maxStep);
        }

        do {
            if (getStartingThread() > 0) {

                // 指定した数の回答があるか
                int answer = 0;
                if (left != null) {
                    answer += left.getFoundAnswer();
                }
                if (right != null) {
                    answer += right.getFoundAnswer();
                }
                if (up != null) {
                    answer += up.getFoundAnswer();
                }
                if (down != null) {
                    answer += down.getFoundAnswer();
                }

                // 探索の停止
                if (sSeekLimit != -1 && answer >= sSeekLimit) {
                    tryAbort(left);
                    tryAbort(right);
                    tryAbort(up);
                    tryAbort(down);
                }

                try {
                    synchronized (puzzle) {
                        Thread.yield();
                        Thread.sleep(1000 / 16);
                    }
                } catch (InterruptedException e) {
                }
            } else {
                break;
            }
        } while (true);

        // ロックファイルの削除
        lockFile.delete();

        return (getSuccess() > 0);
    }

    private static void tryAbort(Solver solver) {
        if (solver != null) {
            solver.abort();
        }
    }

    private static Solver startThread(final Puzzle puzzle, final char direction,
            final File answerFile, final int maxStep, final int minStep) {
        Puzzle p = puzzle.duplicate();
        final Solver s = new Solver();
        s.setPuzzle(p);
        // s.setMaxStep(maxStep);
        s.setLimit(minStep);

        new Thread() {
            public void run() {
                s.setLimit(maxStep);
                s.setSeekLimit(sSeekLimit);
                s.setTimeLimit(sTimeLimit);
                s.setFirstDirection(direction);
                incrementStartingThread();
                boolean result = s.seek(answerFile);
                if (result) {
                    setSuccess(getSuccess() + 1);
                }
                decrementStartingThread();
            }
        }.start();
        return s;
    }

    private static volatile int sSuccess = 0;

    private static synchronized int getSuccess() {
        return sSuccess;
    }

    private static synchronized void setSuccess(int success) {
        sSuccess = success;
    }

    private static volatile int sStartingThread = 0;

    private static synchronized int getStartingThread() {
        return sStartingThread;
    }

    private static synchronized void incrementStartingThread() {
        sStartingThread++;
    }

    private static synchronized void decrementStartingThread() {
        sStartingThread--;
    }

}
