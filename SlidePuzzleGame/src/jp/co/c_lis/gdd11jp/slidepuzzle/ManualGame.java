
package jp.co.c_lis.gdd11jp.slidepuzzle;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import jp.co.c_lis.gdd11jp.slidepuzzle.entity.Problems;
import jp.co.c_lis.gdd11jp.slidepuzzle.entity.Puzzle;
import jp.co.c_lis.gdd11jp.slidepuzzle.solver.Solver;

/**
 * 手動用プログラム.
 * 一応作っておくけど、使わないことを祈る
 * 
 * @author keiji_ariyama
 */
public class ManualGame {

    private static StringBuffer sb = new StringBuffer();
    private static int step = 0;

    public static void setRoute(String route) {
        
        sb.append(route);
        step+=route.length();
    }

    private static void revert() {
        sb.deleteCharAt(sb.length() - 1);
        step--;
    }

    private static void printRoute() {
        System.out.println(sb.length() + ": " + sb.toString());
    }

    private static String sProblemFileName = "problems.txt";

    /**
     * @param args
     */
    public static void main(String[] args) {

        Problems problems = null;
        try {
            problems = new Problems(sProblemFileName);
        } catch (FileNotFoundException e1) {
            System.out.println(e1.getMessage());
        }

        if (problems == null) {
            System.out.println("問題ファイルが見つかりません");
            return;
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        System.out.print("問題番号: ");

        int number = 0;

        do {
            try {
                String num = reader.readLine();
                try {
                    number = Integer.parseInt(num);
                    break;
                } catch (NumberFormatException e) {
                    System.out.println("数字を入力してください。");
                }
            } catch (IOException e) {

            }
        } while (true);

        // ULURDLDDRUUULDDRRRRURULDDRDDLLLDLLUURUUURRRDRD
//        String defaultRoute = "ULURDLDDRUUULDDRRRRURULDDRDDLLLDLLUURUUURRRDRDDDDLLLLLUURURRDDRLLDLLUURURRRRDDLLLDLLUURURRDDRRDLLURDRUUULLDDRRUULLDDDRRULLUURRDDDLLURDLLLLUUURRRRRDDDUUULLDDDRULLRUULLDLRURRDDDLURDLLLUURURRDDLDLLUDRRRRRLLUUUDDLDLLUURURRDDLDLLUURURRDDLDLLUURURRDDDULDLLUUURDLDDRRRULDRRURUULLDDRRUULLDDLDLLUUURRRDDLDLLUURURRDDLDLLUURLURDLDDRRURUULLDLDDRRU";
        String defaultRoute = "";
        Puzzle puzzle = new Puzzle(number, problems.problemList.get(number - 1));
        puzzle.setFixingLineOrCol(false);
        Solver solver = new Solver();
        solver.setPuzzle(puzzle);
        solver.setRoute(defaultRoute);
        setRoute(defaultRoute);
        while (true) {
            try {
                Runtime.getRuntime().exec("clear");
                puzzle.print();
                printRoute();
                System.out.print("> ");
                String action = reader.readLine();
                if(action.equals("c")) {
                    puzzle.printCompletedPanel();
                } else {
                    solver.setRoute(action);
                    setRoute(action);
                }
            } catch (IOException e) {
                System.out.print(e.getMessage());
            }
        }
    }

}
