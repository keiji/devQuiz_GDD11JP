
package jp.co.c_lis.gdd11jp.slidepuzzle.entity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public class Answer {
    private List<String> answerList = new LinkedList<String>();

    public List<String> getList() {
        return answerList;
    }

    public int getCount() {
        return answerList.size();
    }

    private int maxStep = 0;

    public int getMaxStep() {
        return maxStep;
    }

    private int minStep = 0;

    public int getMinStep() {
        return minStep;
    }

    public void addAnswer(String answer) {
        if (answer == null) {
            return;
        }
        int step = answer.length();
        maxStep = Math.max(maxStep, step);
        minStep = Math.max(minStep, step);
        answerList.add(answer);
    }

    private int number;

    public int getNumber() {
        return number;
    }

    public Answer(int number) {
        this.number = number;
    }

    /**
     * 全ての回答ファイルを読み込んで、並び替えた結果の配列を取得する.
     * 
     * @param problems
     * @return
     */
    public static Answer[] getAnswerList(Problems problems) {
        int limit = problems.number;

        List<String> problemList = problems.problemList;
        List<Answer> list = new LinkedList<Answer>();

        // 回答ファイルの読み込み
        for (int i = 1; i <= limit; i++) {

            String fileName = String.format("./answer%d.txt", i);
            File targetFile = new File(fileName);

            if (!targetFile.exists()) {
                continue;
            }

            Answer answer = new Answer(i);
            Puzzle puzzle = new Puzzle(i, problemList.get(i - 1));

            // System.out.println("問題" + i);

            try {
                BufferedReader targetReader = new BufferedReader(new InputStreamReader(
                        new FileInputStream(targetFile)));

                String line = null;
                while ((line = targetReader.readLine()) != null) {
                    if (line != null) {
                        if (line.length() > 0) {
                            if (line.charAt(0) == '#') {
                                // int s = Integer.parseInt(line.substring(1,
                                // line.length()));
                                continue;
                            }

                            boolean ok = false;

                            /*
                             * 不正なデータによって発生する例外に対応する
                             */
                            try {
                                // 正答か確認
                                ok = puzzle.solve(line);
                            } catch (ArrayIndexOutOfBoundsException e) {
                                System.out.println(e.getMessage());
                            }

                            if (ok) {
                                // 追加
                                answer.addAnswer(line);
                            } else {
                                System.out.println(String.format("!!!! 答えが間違っています : %s - %s",
                                        fileName,
                                        line));
                            }

                            puzzle.reset();
                        }
                    }
                }
                targetReader.close();

                if (answer.getCount() > 0) {
                    list.add(answer);
                }
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
        }

        Answer[] array = new Answer[list.size()];
        list.toArray(array);
        Arrays.sort(array, new AnswerComparator());

        return array;
    }

    /**
     * 回答の並べ替えに用いるインターフェースの実装.
     */
    public static class AnswerComparator implements Comparator<Answer> {
        @Override
        public int compare(Answer o1, Answer o2) {
            int a = o1.getCount();
            int b = o2.getCount();

            if (a == b) {
                return 0;
            } else if (a > b) {
                return 1;
            } else {
                return -1;
            }
        }
    }

}
