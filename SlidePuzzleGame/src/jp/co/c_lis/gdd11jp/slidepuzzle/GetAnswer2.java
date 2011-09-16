
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
import jp.co.c_lis.gdd11jp.slidepuzzle.solver.Solver2;

/**
 * スライドパズルの解を探索する処理を行うエントリクラス.
 * 
 * @author Keiji Ariyama <keiji_ariyama@c-lis.co.jp>
 */
public class GetAnswer2 {
    private static final boolean DEBUG_FLG = true;

    private static long sId = System.currentTimeMillis() % 10000; // 1314842361543
                                                                  // => 1543
    /**
     * この値以下のサイズのパズルを探索する
     * -1 を指定した場合、全てのパズルを探索する
     */
    private static int sThresholdSize = 6 * 6;

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
     * 問題ファイルの名前.
     */
    private static String sProblemFileName = "problems.txt";

    private static void printSetting() {
        if (sThresholdSize != -1) {
            System.out.println("探索するパズルの限界サイズ: " + sThresholdSize);
        }

        if (sStart != 0) {
            System.out.println("探索を開始するパズル番号: " + sStart);
        }
        if (sEnd != -1) {
            System.out.println("探索を終了するパズル番号: " + sEnd);
        }

        if (sSeekLimit != -1) {
            System.out.println("探索する回答数: " + sSeekLimit);
        }
        if (sTimeLimit != -1) {
            System.out.println("探索制限時間: " + sTimeLimit);
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
            } else if (option.equals("order")) {
                // TODO : 処理順序の分岐オプション
            } else if (option.equals("time")) {
                sTimeLimit = Long.parseLong(arg);
            } else if (option.equals("answer-limit")) {
                sSeekLimit = Integer.parseInt(arg);
            } else if (option.equals("target-number")) {
                sTargetNumber = Integer.parseInt(arg);
            } else if (option.equals("override")) {
                sOverrideFlg = Boolean.parseBoolean(arg);
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
                    if (!isLocked(puzzle)) {
                        boolean result = false;
                        result = doPuzzle(puzzle);
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

        } while (targetExistFlg
                || lockedExistFlg);

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
                        } else if (line.length() > 0 && c != '#') {
                            answerNumber++;
                            int l = line.length();
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
    private static boolean doPuzzle(final Puzzle puzzle) {

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

        // 探索の開始
        setSuccess(0);

        Solver2 solver = new Solver2();
        solver.setPuzzle(puzzle);
        solver.setTimeLimit(sTimeLimit);
        boolean result = solver.seek(answerFile);
        if (result) {
            setSuccess(getSuccess() + 1);
        }

        // ロックファイルの削除
        lockFile.delete();

        return (getSuccess() > 0);
    }

    private static volatile int sSuccess = 0;

    private static synchronized int getSuccess() {
        return sSuccess;
    }

    private static synchronized void setSuccess(int success) {
        sSuccess = success;
    }

}
