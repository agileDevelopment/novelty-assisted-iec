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
public class EuclidianDistance extends FitnessStrategy {

    /**
     * 
     * @param maze 
     */
    public EuclidianDistance(Maze maze) {
        super(maze);
    }

    @Override
    public int calculateFitness(Path path) {
        double distance = theMaze.getGoal().distance(path.getLast());
        distance = Math.min(Math.max(distance, MIN_FITNESS), MAX_FITNESS);
        return MAX_FITNESS - (int) Math.round(distance);
    }
}
