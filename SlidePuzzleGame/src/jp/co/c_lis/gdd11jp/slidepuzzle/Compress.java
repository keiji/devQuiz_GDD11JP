
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
import java.nio.charset.Charset;
import java.util.List;

import jp.co.c_lis.gdd11jp.slidepuzzle.entity.Answer;
import jp.co.c_lis.gdd11jp.slidepuzzle.entity.Problems;
import jp.co.c_lis.gdd11jp.slidepuzzle.entity.Puzzle;
import jp.co.c_lis.gdd11jp.slidepuzzle.solver.Compressor;

/**
 * スライドパズルの解を探索する処理を行うエントリクラス.
 * 
 * @author Keiji Ariyama <keiji_ariyama@c-lis.co.jp>
 */
public class Compress {
    private static final boolean DEBUG_FLG = true;

    private static long sId = System.currentTimeMillis() % 100000; // 131484236154
                                                                   // => 31543
    /**
     * 問題ファイルの名前.
     */
    private static String sProblemFileName = "problems.txt";

    private static void printSetting() {
    }

    private static Problems sProblems;

    /**
     * エントリポイント.
     * 
     * @param args
     */
    public static void main(String[] args) {
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

        // 回答一覧
        Answer[] list = Answer.getAnswerList(sProblems);

        for (int i = 0; i < list.length; i++) {

            Answer answer = list[i];
            List<String> answerList = answer.getList();
            String map = problems.get(answer.getNumber() - 1);
            Puzzle puzzle = new Puzzle(answer.getNumber(), map);

            int success = 0;
            for (String route : answerList) {
                puzzle.reset();

                Compressor comp = new Compressor(puzzle);
                String shorter = comp.setRoute(route);

                if (shorter.length() != route.length()) {
                    puzzle.reset();
                    if (puzzle.solve(shorter)) {
                        success++;
//                        System.out.println((route.length() - shorter.length()) + "ステップの短縮");
                        outputAnswer(answer.getNumber(), shorter);
                    }
                } else {
                    outputAnswer(answer.getNumber(), shorter);
                }
            }
            if (success > 0) {
                System.out.println(answer.getNumber() + ": 短縮成功 " + success + "件");
            }
        }
    }

    private static void outputAnswer(int number, String answer) {
        File answerFile = new File(String.format("answers/answer%d.txt", number));

        BufferedWriter bw = null;
        try {
            answerFile.createNewFile();
            bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(answerFile, true), Charset.forName("UTF-8")));
            bw.write(answer);
            bw.write("\n");
            bw.flush();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        } finally {
            if (bw != null) {
                try {
                    bw.flush();
                    bw.close();
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                }
            }
        }
    }
}
