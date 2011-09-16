
package jp.co.c_lis.gdd11jp.slidepuzzle;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import jp.co.c_lis.gdd11jp.slidepuzzle.entity.Problems;
import jp.co.c_lis.gdd11jp.slidepuzzle.entity.Puzzle;
import jp.co.c_lis.gdd11jp.slidepuzzle.solver.Solver;

/**
 * スライドパズルの解を探索する処理を行うエントリクラス.
 * 
 * @author Keiji Ariyama <keiji_ariyama@c-lis.co.jp>
 */
public class GetAnswer {
    private static final boolean DEBUG_FLG = true;

    private static long sId = System.currentTimeMillis() % 100000; // 1314842361543
                                                                   // => 61543

    /**
     * この値以下のサイズのパズルを探索する
     * -1 を指定した場合、全てのパズルを探索する
     */
    private static int sThresholdSize = 6 * 6;

    /**
     * 初期の探索ステップ数
     */
    private static int sInitStep = 1;
    private static int sTryStep = 1;

    /**
     * 探索ステップ数の限界.
     * この値に到達したら終了する。
     * -1が指定された場合探索ステップ数を増分しない。
     */
    private static int sStepLimit = -1;

    /**
     * 探索ステップ数の増分
     */
    private static int sStepAdd = 2;

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
     * 探索する問題番号を指定.
     * -1 は指定しない
     */
    private static int sTargetNumber = -1;

    /**
     * 既に回答がある問題の探索を行うか指定.
     */
    private static boolean sOverrideFlg = false;

    /**
     * 探索を開始するパズル番号.
     */
    private static int sStart = 0;

    /**
     * 探索を終了するパズル番号.
     */
    private static int sEnd = -1;

    /**
     * 一つの問題が解けるまでステップ数を増やす.
     */
    private static boolean sForceFlg = false;

    /**
     * キャッシュサイズ.
     */
    private static int sCacheSize = 20000;

    /**
     * 縦及び横のラインがそろった時に、そのラインを固定する.
     * 注意！
     * 壁の配置上、このオプションがtrueの場合、絶対に解が見つからない問題が存在する
     */
    private static boolean sFixLineCol = true;

    /**
     * 探索を同一ステップ数でリトライする回数.
     * TODO: 全パターンの探索が完了している場合でもリトライされる不具合を修正する。
     */
    private static int sRetry = 0;

    /**
     * パズルに与える初期ルート.
     */
    private static String sInitialRoute = null;

    /**
     * 探索を終了するパズル番号.
     */
    private static int sLogLineThreshold = -1;

    /**
     * 問題ファイルの名前.
     */
    private static String sProblemFileName = "problems.txt";

    private static void printSetting() {
        System.out.println("ID: " + sId);
        if (sStart != 0) {
            System.out.println("探索を開始するパズル番号: " + sStart);
        }
        if (sEnd != -1) {
            System.out.println("探索を終了するパズル番号: " + sEnd);
        }

        if (sThresholdSize != -1) {
            System.out.println("探索するパズルの限界サイズ: " + sThresholdSize);
        }
        if (sSeekLimit != -1) {
            System.out.println("探索する回答数: " + sSeekLimit);
        }

        System.out.println("探索ステップ数: " + sInitStep);

        if (sStepLimit != -1) {
            System.out.println("最大ステップ数: " + sStepLimit);
        }
        if (sStepAdd != -1) {
            System.out.println("ステップ数増分: " + sStepAdd);
        }
        if (sTimeLimit != -1) {
            System.out.println("探索制限時間: " + sTimeLimit);
        }
        if (sRetry > 0) {
            System.out.println("リトライ回数: " + sRetry);
        }
        System.out.println("キャッシュサイズ: " + sCacheSize);

        if (sForceFlg) {
            System.out.println("forceモード");
        }
        if (!sFixLineCol) {
            System.out.println("ラインがそろっても固定しない");
        }
        if (sInitialRoute != null) {
            System.out.println("初期ルート: " + sInitialRoute);
        }
        if (sLogLineThreshold != -1) {
            System.out.println(sLogLineThreshold + "行がそろった時点のルートをログに記録する");
        }

    }

    private static Problems sProblems;

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
            } else if (option.equals("size")) {
                sThresholdSize = Integer.parseInt(arg);
            } else if (option.equals("init-step")) {
                sTryStep = Integer.parseInt(arg);
                if (sTryStep <= 0) {
                    sTryStep = 0;
                }
                sInitStep = sTryStep;
            } else if (option.equals("max-step")) {
                sStepLimit = Integer.parseInt(arg);
            } else if (option.equals("add-step")) {
                sStepAdd = Integer.parseInt(arg);
                sStepAdd += sStepAdd % 2;
            } else if (option.equals("order")) {
                // TODO : 処理順序の分岐オプション
            } else if (option.equals("time")) {
                sTimeLimit = Long.parseLong(arg);
            } else if (option.equals("answer-limit")) {
                sSeekLimit = Integer.parseInt(arg);
            } else if (option.equals("target-number")) {
                sTargetNumber = Integer.parseInt(arg);
            } else if (option.equals("cache-size")) {
                sCacheSize = Integer.parseInt(arg);
            } else if (option.equals("retry")) {
                sRetry = Integer.parseInt(arg);
            } else if (option.equals("override")) {
                sOverrideFlg = Boolean.parseBoolean(arg);
            } else if (option.equals("force")) {
                sForceFlg = Boolean.parseBoolean(arg);
            } else if (option.equals("fix-line-col")) {
                sFixLineCol = Boolean.parseBoolean(arg);
            } else if (option.equals("start")) {
                sStart = Integer.parseInt(arg);
                if (sStart < 0) {
                    sStart = 0;
                }
            } else if (option.equals("end")) {
                sEnd = Integer.parseInt(arg);
                if (sEnd < 0) {
                    sEnd = 0;
                }
            } else if (option.equals("init-route")) {
                sInitialRoute = arg;
            } else if (option.equals("log-line")) {
                sLogLineThreshold = Integer.parseInt(arg);
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

        boolean targetExistFlg = false;
        boolean lockedExistFlg = false;
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

        do {
            targetExistFlg = false;
            lockedExistFlg = false;

            for (int i = sStart; isEnd(i); i++) {
                if (!abortFile.exists()) {
                    System.out.println(abortFileName + " の削除によって中止が指示されました。プロセスを終了します...");
                    break;
                }
                if (abortAllFile.exists()) {
                    System.out.println(ABORT_ALL_FILE_NAME + " によって中止が指示されました。プロセスを終了します...");
                    break;
                }

                int targetNumber = (sTargetNumber == -1) ? i : (sTargetNumber - 1);
                String map = problems.get(targetNumber);
                Puzzle puzzle = new Puzzle(targetNumber + 1, map);

                if (isTarget(puzzle)) {
                    printSetting();

                    long time = (System.currentTimeMillis() - start) / 1000;
                    System.out.println(String.format("問題 %d を探索します。 - %d秒 - 回答済 : %d",
                            puzzle.getNumber(),
                            time, success));

                    // パズルの探索
                    if (!isLocked(puzzle) || sOverrideFlg) {
                        boolean result = false;

                        // 一つの問題が解けるまで継続するモード指定
                        if (sForceFlg) {
                            int step = sTryStep;
                            do {
                                result = doPuzzle(puzzle, step);
                                step += sStepAdd;
                            } while (result == false);
                        } else {
                            int retry = 0;
                            do {
                                if (retry > 0) {
                                    System.out.println(String.format("リトライ %d 回目...", retry));
                                }
                                result = doPuzzle(puzzle, sTryStep);
                                retry++;
                            } while (result == false && sTimeLimit != -1 && retry < sRetry);
                        }

                        System.out.println(String.format("問題 %d を探索しました。 - %d秒",
                                puzzle.getNumber(),
                                (((System.currentTimeMillis() - start) / 1000) - time), success));
                        System.out.println("Solver.cache = " + Solver.getCacheContains() + " "
                                + Solver.getCacheHit());
                        System.out.println();

                        success += result ? 1 : 0;
                        targetExistFlg = true;
                        if (sTargetNumber > 0) {
                            break;
                        }

                    } else {
                        System.out.println(String.format("問題%dは、他のプロセスによって処理中です。",
                                puzzle.getNumber()));
                        lockedExistFlg = true;
                    }
                }
            }

            System.out.println(String.format("%d: %d問回答しました。", sId, success));
            sTryStep += sStepAdd;

            if (sStepLimit != -1 && sStepLimit < sTryStep) {
                sTryStep = sInitStep + 2;
            }
        } while (targetExistFlg
                || lockedExistFlg
                && (sStepAdd < 0 && sTryStep > 0));

        // 中断ファイルを削除
        abortFile.deleteOnExit();

        long time = (System.currentTimeMillis() - start) / 1000;
        System.out.println("所要時間 = " + time + "秒");
    }

    private static boolean isEnd(int num) {
        return (sEnd != -1 && num < sEnd) || (sEnd == -1 && num < sProblems.number);
    }

    private static boolean isTarget(Puzzle puzzle) {
        // 限界サイズに一致しているか
        if ((sThresholdSize != -1 && puzzle.size > sThresholdSize)) {
            return false;
        }

        File file = new File(String.format("./answer%d.txt", puzzle.getNumber()));

        int answerNumber = 0;
        int maxStep = 0;
        int minStep = 0;

        if (file.exists()) {
            BufferedReader br = null;
            try {
                // 回答ファイルの読み込み
                br = new BufferedReader(new InputStreamReader(
                        new FileInputStream(file)));

                String line = null;
                while ((line = br.readLine()) != null) {
                    if (line.length() > 0) {
                        char c = line.charAt(0);
                        if (c == '#') {
                            // コメントの処理
                            int l = Integer.parseInt(line.substring(1));
                            minStep = Math.max(minStep, l);
                        } else if (line.length() > 0 && c != '#') {
                            answerNumber++;
                            int l = line.length();
                            maxStep = Math.max(maxStep, l);
                            minStep = Math.min(minStep, l);
                        }
                    }
                }
            } catch (FileNotFoundException e) {
                System.out.println(e.getMessage());
            } catch (IOException e) {
                System.out.println(e.getMessage());
            } finally {
                if (br != null) {
                    // 回答ファイルを閉じる
                    try {
                        br.close();
                    } catch (IOException e) {
                        System.out.println(e.getMessage());
                    }
                }
            }
        }
        return (sOverrideFlg || answerNumber == 0);
    }

    private static boolean isLocked(Puzzle puzzle) {
        // ロック確認
        return new File(String.format("./lock%d.lock", puzzle.getNumber())).exists();
    }

    /**
     * パズルを探索.
     * 
     * @param puzzle Puzzleクラスのオブジェクト
     * @param maxStep 試行する探索ステップ数
     * @return 回答が得られればtrue
     */
    private static boolean doPuzzle(final Puzzle puzzle, int maxStep) {

        // 指定された探索ステップ数が、パズルの想定最短ステップ数より下であれば失敗する
        if (puzzle.getLowerBoundValue() > maxStep) {
            System.out.println(String.format("問題%dは、指定されたステップ数 %d が、想定する最低ステップ数 %d 以下なのでスキップします。",
                    puzzle.getNumber(),
                    maxStep, puzzle.getLowerBoundValue()));
            return false;
        }

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

        // 縦横ライン固定の設定
        puzzle.setFixingLineOrCol(sFixLineCol);

        Puzzle sample = puzzle;
        if (sInitialRoute != null) {
            Puzzle p = sample.duplicate();
            Solver s = new Solver();
            s.setPuzzle(p);
            s.setRoute(sInitialRoute);
            sample = p;
        }

        System.out.println("sample --");
        sample.print();
        sample.printCompletedPanel();

        // 探索ステップ数の補正
        if (((maxStep % 2) == 0 && puzzle.isExpectedOddStep())
                || ((maxStep % 2) == 1 && !puzzle.isExpectedOddStep())) {
            maxStep += 1;
        }

        // 探索の開始
        setSuccess(0);

        // キャッシュのクリア
        Solver.clearCache();

        Solver.setCacheSize(sCacheSize);

        Solver left = null;
        if (sample.canMoveLeft()) {
            left = startThread(puzzle, Puzzle.DIRECTION_LEFT, answerFile, maxStep);
        }

        Solver right = null;
        if (sample.canMoveRight()) {
            right = startThread(puzzle, Puzzle.DIRECTION_RIGHT, answerFile, maxStep);
        }

        Solver up = null;
        if (sample.canMoveUp()) {
            up = startThread(puzzle, Puzzle.DIRECTION_UP, answerFile, maxStep);
        }

        Solver down = null;
        if (sample.canMoveDown()) {
            down = startThread(puzzle, Puzzle.DIRECTION_DOWN, answerFile, maxStep);
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
            final File answerFile, final int maxStep) {
        Puzzle p = puzzle.duplicate();
        final Solver s = new Solver();
        s.setLogLineThreshold(sLogLineThreshold);
        s.setPuzzle(p);
        if (sInitialRoute != null) {
            s.setRoute(sInitialRoute);
            p.print();
        }
        s.setMaxStep(sStepLimit);

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
