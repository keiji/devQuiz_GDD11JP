
package jp.co.c_lis.gdd11jp.slidepuzzle;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.List;

import jp.co.c_lis.gdd11jp.slidepuzzle.entity.Problems;
import jp.co.c_lis.gdd11jp.slidepuzzle.entity.Puzzle;

/**
 * 古い集計プログラム.
 * 
 * @deprecated
 * 
 * @author keiji_ariyama
 *
 */
public class Integrator {

    /**
     * DANGER!!!
     * 回答が一つも無いファイルを削除する.
     */
    private static final boolean UNCOMPLETE_FILE_WILL_BE_DELETED_FLG = true;

    /**
     * @param args
     */
    public static void main(String[] args) {

        // 問題ファイル名
        String problemFileName = "problems.txt";
        if (args.length > 0) {
            problemFileName = args[0];
        }

        // 回答ファイル名
        String outputFileName = "0answer.txt";
        if (args.length > 1) {
            outputFileName = args[1];
        }

        // 問題ファイルの読み込み
        Problems problems = null;
        try {
            problems = new Problems(problemFileName);
        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
        }

        if (problems == null) {
            System.out.println(String.format("ファイルが見つかりません - %s", problemFileName));
            return;
        }

        Problems.State state = problems.state;
        int limit = problems.number;

        BufferedWriter answerWriter = null;

        int result = 0;
        int proccess = 0;

        // 結果の出ていない探索で、次回設定時に最低必要と思われるステップ数
        int minStep = -1;

        try {
            File file = new File(outputFileName);
            if (!file.exists()) {
                file.createNewFile();
            }

            answerWriter = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(file)));

            List<String> problemList = problems.problemList;

            for (int i = 1; i <= limit; i++) {

                String fileName = String.format("./answer%d.txt", i);
                File targetFile = new File(fileName);

                File lockFile = new File(String.format("./lock%d.lock", i));

                if (!targetFile.exists() || lockFile.exists()) {
                    answerWriter.write("\n");
                    continue;
                }

                proccess++;

                BufferedReader targetReader = new BufferedReader(new InputStreamReader(
                        new FileInputStream(targetFile)));

                // 最適な回答
                String topAnswer = null;
                float topAnswerStep = -1;

                Puzzle puzzle = new Puzzle(i, problemList.get(i - 1));

                // 回答ファイルに記載されている最大試行ステップ数
                int maxStep = -1;
                String line = null;
                while ((line = targetReader.readLine()) != null) {
                    if (line != null) {
                        if (line.length() > 0) {
                            if (line.charAt(0) == '#') {
                                int s = Integer.parseInt(line.substring(1, line.length()));
                                if (maxStep == -1 || s > maxStep) {
                                    maxStep = s;
                                }
                                continue;
                            }
                            // 正答か確認
                            if (puzzle.solve(line)) {
                                // System.out.println(String.format("*** 正解です : %s - %s",
                                // fileName, line));
                                float step = state.calcStep(line);
                                if (topAnswerStep == -1 || step < topAnswerStep) {
                                    if (topAnswerStep != -1) {
                                        // System.out.println(String.format(
                                        // "* 最適な回答の変更 : %s, step %f",
                                        // line,
                                        // step));
                                    }
                                    topAnswer = line;
                                    topAnswerStep = step;
                                }
                            } else {
                                System.out.println(String.format("!!!! 答えが間違っています : %s - %s",
                                        fileName,
                                        line));
                            }

                            puzzle.reset();
                        }
                    }
                }

                if (topAnswer != null) {
                    answerWriter.write(topAnswer);

                    // 制限値から引く
                    state.processRoute(topAnswer);
                    // state.print();
                    // state.printWeight();

                    result++;
                } else if (UNCOMPLETE_FILE_WILL_BE_DELETED_FLG) {
                    System.out.println(String.format("%s は、削除されます...", targetFile.getName()));
                    targetFile.delete();
                } else {
                    if (minStep == -1 || minStep > maxStep) {
                        minStep = maxStep;
                    }
                }

                answerWriter.write("\n");
                answerWriter.flush();

                targetReader.close();
                // targetFile.delete();
            }

        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
        } catch (IOException e) {
            System.out.println(e.getMessage());
        } finally {
            if (answerWriter != null) {
                try {
                    answerWriter.close();
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                }
            }
        }

        state.print();
        state.printWeight();
        System.out.println(String.format("result = %d / %d", result, proccess));
        System.out.println(String.format("残りの問題の最低ステップ数 : %d", minStep));
    }
}
