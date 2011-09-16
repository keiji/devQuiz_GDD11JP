package jp.co.c_lis.gdd11jp.slidepuzzle.solver;

import java.io.File;

import jp.co.c_lis.gdd11jp.slidepuzzle.entity.Puzzle;

public interface ISolver {

    public boolean seek(File answerFile);
    public void setPuzzle(Puzzle puzzle);

}
