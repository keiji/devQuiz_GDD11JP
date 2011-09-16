
package jp.co.c_lis.gdd11jp.slidepuzzle.entity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

public class Problems {

    public static class State {
        public int limitL = 0;
        public int limitR = 0;
        public int limitU = 0;
        public int limitD = 0;

        public float weightL = 0;
        public float weightR = 0;
        public float weightU = 0;
        public float weightD = 0;

        private State() {
        }

        public static State getState(String line) {
            String[] cols = line.split(" ");

            State result = new State();
            result.limitL = Integer.parseInt(cols[0]);
            result.limitR = Integer.parseInt(cols[1]);
            result.limitU = Integer.parseInt(cols[2]);
            result.limitD = Integer.parseInt(cols[3]);
            result.calcWeight();

            return result;
        }

        public void print() {
            System.out.print("利用できる操作数: ");
            System.out.print(String.format("l : %d, r : %d, u : %d , d : %d", limitL, limitR,
                    limitU,
                    limitD));
            System.out.println("");
        }

        public void printWeight() {
            System.out.print("操作の重み: ");
            System.out.print(String.format("l : %2f, r : %2f, u : %2f , d : %2f", weightL, weightR,
                    weightU,
                    weightD));
            System.out.println("");
        }

        private static final int P = 3;

        private void calcWeight() {
            float average = (limitL + limitR + limitU + limitD) / (float) 4;
            weightL = 1.0F - (limitL - average) / average;
            weightL = (float) Math.pow(weightL, P);

            weightR = 1.0F - (limitR - average) / average;
            weightR = (float) Math.pow(weightR, P);

            weightU = 1.0F - (limitU - average) / average;
            weightU = (float) Math.pow(weightU, P);

            weightD = 1.0F - (limitD - average) / average;
            weightD = (float) Math.pow(weightD, P);
        }

        public float calcStep(String route) {
            float l = 0;
            float r = 0;
            float u = 0;
            float d = 0;
            char[] chars = route.toCharArray();
            for (char c : chars) {
                switch (c) {
                    case 'L':
                        l++;
                        break;
                    case 'R':
                        r++;
                        break;
                    case 'U':
                        u++;
                        break;
                    case 'D':
                        d++;
                        break;
                }
            }

            l *= weightL;
            r *= weightR;
            u *= weightU;
            d *= weightD;
            return (l + r + u + d);
        }

        public boolean processRoute(String route) {
            char[] chars = route.toCharArray();
            for (char c : chars) {
                switch (c) {
                    case 'L':
                        limitL--;
                        break;
                    case 'R':
                        limitR--;
                        break;
                    case 'U':
                        limitU--;
                        break;
                    case 'D':
                        limitD--;
                        break;
                }
            }

            if (limitL < 0 || limitR < 0 || limitU < 0 || limitD < 0) {
                return false;
            }

            calcWeight();
            return true;
        }
    }

    public State state = new State();

    public int number = 5000;

    public List<String> problemList;

    public Problems(String fileName) throws FileNotFoundException {
        this(new FileInputStream(new File(fileName)));
    }

    public Problems(InputStream inputStream) {

        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));

            problemList = new LinkedList<String>();

            String line = null;
            int num = 0;
            while ((line = br.readLine()) != null) {
                switch (num) {
                    case 0:
                        state = State.getState(line);
                        state.print();
                        break;
                    case 1:
                        number = Integer.parseInt(line);
                        break;
                    default:
                        problemList.add(line);
                        break;
                }
                num++;
            }
        } catch (IOException e) {
            System.out.println("IOException - " + e.getLocalizedMessage());
        }
    }

    public void printQuote() {
        System.out.println("パズル数 = " + number);
    }

}
