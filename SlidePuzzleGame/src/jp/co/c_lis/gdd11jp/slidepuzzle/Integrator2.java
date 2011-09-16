
package jp.co.c_lis.gdd11jp.slidepuzzle;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;

import jp.co.c_lis.gdd11jp.slidepuzzle.entity.Answer;
import jp.co.c_lis.gdd11jp.slidepuzzle.entity.Problems;
import jp.co.c_lis.gdd11jp.slidepuzzle.entity.Puzzle;

public class Integrator2 {

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
        String outputFileName = "answer.txt";
        if (args.length > 1) {
            outputFileName = args[1];
        }

        // このステップ数を越えていたら警告する
        int warningStepThreshold = 180;
        if (args.length > 2) {
            warningStepThreshold = Integer.parseInt(args[2]);
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
        state.printWeight();
        Answer[] list = Answer.getAnswerList(problems);

        String[] resultArray = new String[problems.number];
        for (Answer answer : list) {

            // 最適な回答
            String topAnswer = null;
            float topAnswerStep = -1;

            List<String> answers = answer.getList();

            for (String line : answers) {
                float step = state.calcStep(line);
                if (topAnswerStep == -1 || step < topAnswerStep) {
                    topAnswer = line;
                    topAnswerStep = step;
                }
            }

            if (topAnswer != null) {
                // 制限値から引く
                boolean result = state.processRoute(topAnswer);

                if (result) {
                    resultArray[answer.getNumber() - 1] = topAnswer;
                } else {
                    System.out.println("書くステップの合計値が、制約の値を超えました。");
                }

                if (topAnswer.length() > warningStepThreshold) {
                    System.out.println(answer.getNumber() + ": " + topAnswer.length());
                }

            }
        }

        BufferedWriter answerWriter = null;

        try {
            File file = new File(outputFileName);
            if (!file.exists()) {
                file.createNewFile();
            }

            answerWriter = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(file)));

            int len = resultArray.length;
            for (int i = 0; i < len; i++) {
                String answer = resultArray[i];
                if (answer != null) {
                    answerWriter.write(answer + "\n");
                } else {
                    System.out.println("未回答 : " + (i + 1));
                    Puzzle puzzle = new Puzzle(i + 1, problems.problemList.get(i));
                    puzzle.print();
                    answerWriter.write("\n");
                }
                answerWriter.flush();
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
        System.out.println(String.format("%d 個の回答があります。(%d 個中)", list.length, problems.number));
    }

}
