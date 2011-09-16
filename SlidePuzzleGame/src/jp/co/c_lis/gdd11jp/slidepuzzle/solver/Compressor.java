
package jp.co.c_lis.gdd11jp.slidepuzzle.solver;

import java.util.HashMap;

import jp.co.c_lis.gdd11jp.slidepuzzle.entity.Puzzle;

/**
 * 圧縮用.
 * 
 * @author keiji_ariyama
 */
public class Compressor {

    private final StringBuffer route = new StringBuffer();

    public String setRoute(String r) {
        int len = r.length();
        for (int i = 0; i < len; i++) {
            move(r.charAt(i));

            if (cache.containsKey(mPuzzle)) {
                String rt = cache.get(mPuzzle);
                if(rt.length() < route.length()) {
                    cache.put(mPuzzle.duplicate(), route.toString());
                    
                    // ルートをより短いもので上書きする
                    route.delete(0, route.length());
                    route.append(rt);
                } else {
                    // キャッシュから削除
                    cache.remove(mPuzzle);
                }
            } else {
                cache.put(mPuzzle.duplicate(), route.toString());
            }

        }
        
        if (!mPuzzle.isFinished()) {
           System.out.println("不正解です"); 
        }
        return route.toString();
    }

    private void move(char c) {
        switch (c) {
            case Puzzle.DIRECTION_LEFT:
                mPuzzle.moveLeft();
                break;
            case Puzzle.DIRECTION_RIGHT:
                mPuzzle.moveRight();
                break;
            case Puzzle.DIRECTION_UP:
                mPuzzle.moveUp();
                break;
            case Puzzle.DIRECTION_DOWN:
                mPuzzle.moveDown();
                break;
        }
        route.append(c);

    }

    public void printRoute() {
        int l = 0;
        int r = 0;
        int u = 0;
        int d = 0;

        for (int i = 0; i < route.length(); i++) {
            char c = route.charAt(i);
            switch (c) {
                case Puzzle.DIRECTION_LEFT:
                    l++;
                    break;
                case Puzzle.DIRECTION_RIGHT:
                    r++;
                    break;
                case Puzzle.DIRECTION_UP:
                    u++;
                    break;
                case Puzzle.DIRECTION_DOWN:
                    d++;
                    break;
            }
        }
        System.out.println(String.format("%d: step %d, %s",
                mPuzzle.getNumber(),
                route.length(),
                route));
    }

    public void revert() {
        if (route.length() > 0) {
            route.deleteCharAt(route.length() - 1);
        }
    }

    private final HashMap<Puzzle, String> cache = new HashMap<Puzzle, String>();

    private int cacheHit = 0;

    public int getCacheHit() {
        return cacheHit;
    }

    public void clearCache() {
        cache.clear();
        cacheHit = 0;
    }

    private Puzzle mPuzzle = null;

    /**
     * コンストラクタ.
     */
    public Compressor(Puzzle puzzle) {
        mPuzzle = puzzle;
    }
}
