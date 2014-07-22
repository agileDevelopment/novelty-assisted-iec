/**
 * This software is a work of the U.S. Government. It is not subject to copyright 
 * protection and is in the public domain. It may be used as-is or modified and 
 * re-used. The author and the Air Force Institute of Technology would appreciate 
 * credit if this software or parts of it are used or modified for re-use.
 * 
 * Created by Brian Woolley on Sep 23, 2011
 */
package edu.ucf.eplex.mazeNavigation;

import edu.ucf.eplex.mazeNavigation.model.Maze;
import edu.ucf.eplex.mazeNavigation.model.Path;

/**
 *
 * @author Brian Woolley (brian.woolley at ieee.org)
 */
public abstract class FitnessStrategy {
    protected final Maze theMaze;

    /**
     * 
     * @param maze 
     */
    public FitnessStrategy(Maze maze) {
        theMaze = maze;
    }

    public abstract int calculateFitness(Path path);

    public int getMaxFitnessValue() {
        return MAX_FITNESS;
    }

    protected static final int MIN_FITNESS = 1;
    protected static final int MAX_FITNESS = 300;

}
