/**
 * This software is a work of the U.S. Government. It is not subject to copyright 
 * protection and is in the public domain. It may be used as-is or modified and 
 * re-used. The author and the Air Force Institute of Technology would appreciate 
 * credit if this software or parts of it are used or modified for re-use.
 * 
 * Created by Brian Woolley on Sep 23, 2011
 */
package edu.ucf.eplex.mazeNavigation;

import edu.ucf.eplex.mazeNavigation.behaviorFramework.ANN_Behavior;
import edu.ucf.eplex.mazeNavigation.behaviorFramework.Behavior;
import edu.ucf.eplex.mazeNavigation.gui.EvolutionPanel;
import edu.ucf.eplex.mazeNavigation.model.Environment;
import edu.ucf.eplex.mazeNavigation.model.Maze;
import edu.ucf.eplex.mazeNavigation.model.Path;
import edu.ucf.eplex.mazeNavigation.model.Position;
import edu.ucf.eplex.mazeNavigation.util.MazeRenderingTool;
import edu.ucf.eplex.naiecFramework.NaiecSession;
import edu.ucf.eplex.naiecFramework.domain.*;

import com.anji.util.Properties;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.util.*;

import javax.swing.GroupLayout;
import javax.swing.JCheckBox;
import javax.swing.LayoutStyle;

import org.jgap.BehaviorVector;

/**
 *
 * @author Brian Woolley (brian.woolley at ieee.org)
 */
public class MazeNavigationDomain extends EvaluationDomain<Path> {

    /**
     * @param args the command line arguments
     * @throws IOException 
     */
    public static void main(final String args[]) throws IOException {
    	MazeDomainProperties props;
    	if (args.length == 1) {
    		props = new MazeDomainProperties("./properties/" + args[0]);
    	} else {
    		props = new MazeDomainProperties();
    	}
    	
    	new MazeNavigationDomain(props);
    	// TODO  setup domain
    	// TODO  launch the NA-IEC session window 
    }

    private Map<Candidate, BehaviorVector> allPoints;
    private MazeDomainNoveltyMetric noveltyMetric;
    private MazeRenderingTool render = new MazeRenderingTool();

    public MazeNavigationDomain() {
        super();
    }
    
    public MazeNavigationDomain(NaiecDomainProperties props) {
        setDomainProperties(props);
        NaiecSession session = new NaiecSession(this);
        setDomainOptions(session.getDomainOptionsPanel());
    }
    
	@Override
    public final void setDomainProperties(NaiecDomainProperties props) {
        super.setDomainProperties(props);
        init(getProperties());
    }

    private Collection<Position> getPopulationPoints() {
        Collection<Position> results = new HashSet<Position>();
        for (Path path : population.values()) {
            results.add(path.getLast());
        }
        return results;
    }

    private Collection<Position> getArchivePoints() {
        Collection<Position> results = new HashSet<Position>();
        for (Path path : noveltyMetric.getArchivedBehaviors()) {
            results.add(path.getLast());
        }
        return results;
    }

    @Override
    public void paintComponent(EvaluationPanel panel, Graphics g, Dimension size) {
        if (simulations.containsKey(panel)) {
            Path path = simulations.get(panel);
            render.renderEnvironment(getMaze(), path, getCurrentTimeStep(), g, size);
        }
    }

    @Override
    public void gotoSimulationTimestep(int simulationTimeStep) {
        simulationTimeStep = Math.max(Math.min(simulationTimeStep, getMaxSimulationTimesteps()), 0);
        setCurrentTimeStep(simulationTimeStep);
    }

    @Override
    public int getMaxSimulationTimesteps() {
        return maxTimesteps;
    }

    @Override
    public boolean atLastStep() {
        return getCurrentTimeStep() >= getMaxSimulationTimesteps();
    }

    @Override
    public void evaluateFitness(Candidate subject) {
        evaluate(subject);
        Path path = population.get(subject);
//        if (subject.isSolution()) {
//            subject.setFitnessValue(fitnessFunction.getMaxFitnessValue());
//        } else {
            subject.setFitnessValue(fitnessFunction.calculateFitness(path));
//        }
    }

    @Override
    public void evaluateFitness(List<Candidate> subjects) {
        for (Candidate subject : subjects) {
            evaluateFitness(subject);
        }
    }

    @Override
    public void evaluateNovelty(List<Candidate> subjects) {
        evaluate(subjects);
        evaluateFitness(subjects);
        noveltyMetric.score(population);
    }

    @Override
    public void evaluate(List<Candidate> subjects) {
        for (Candidate subject : subjects) {
            evaluate(subject);
        }
    }

    @Override
    public void evaluate(Candidate subject) {
        assert (subject != null);

        if (population.containsKey(subject)) {
            return;
        }

        // Build ANN Behavior from Chrom
        Behavior phenotype = new ANN_Behavior(subject);
        Environment env = new Environment(phenotype, getMaze());

        // Evaluate over x timesteps (or until distToGoal <= 5)
        for (int i = 0; i < maxTimesteps; i++) {
            env.step();
            if (env.distToGoal() <= goalThreshold) {
                System.out.println("<--------------------MAZE SOLVED!  GOAL FOUND BY CHROMOSOME " + subject.getId() + "!-------------------->");
                subject.setAsSolution(true);
//                for (Position pt : env.getPath()) {
//                    System.out.println("breadCrumbs.add(new Position(" + Math.round(pt.getX()) + ", " + Math.round(pt.getY()) + ", 0));");
//                }
                break;
            }
        }
        population.put(subject, env.getPath());
        allPoints.put(subject, new BehaviorVector(env.getPath().getLast().toArray()));
//        evaluateFitness(subject);
//        observationPoint.notifyObservers(getPopulationPoints(), getArchivePoints());
    }

    @Override
    public int getMaxFitnessValue() {
        return fitnessFunction.getMaxFitnessValue();
    }

    @Override
    public double getNoveltyThreshold() {
        return noveltyMetric.getNoveltyThreshold();
    }

    @Override
    public void setNoveltyThreshold(double aNewNoveltyThreshold) {
        noveltyMetric.setNoveltyThreshold(aNewNoveltyThreshold);
    }

    @Override
    public int getNoveltyArchiveSize() {
        return noveltyMetric.getArchiveSize();
    }

    @Override
    public Map<Candidate, BehaviorVector> getAllPointsVisited() {
        return allPoints;
    }

    @Override
    public List<RenderedImage> getPhenotypeBehavior(Candidate subject, Dimension size) {
    	List<RenderedImage> phenotypeBehaviors = new ArrayList<RenderedImage>();
    	
    	if (!population.containsKey(subject)) {
    		evaluate(subject);
    	} 
    	
		Path result = population.get(subject);
		MazeRenderingTool renderer = new MazeRenderingTool();
		
		BufferedImage bi;
        for (Position pose : result) {
            bi = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_RGB);
            renderer.applyBackground(bi.getGraphics(), size, Color.WHITE);
            renderer.renderEnvironment(getMaze(), pose, result, bi.getGraphics(), size);
            phenotypeBehaviors.add(bi);
        }
        return phenotypeBehaviors;
    }

    @Override
    public RenderedImage getPhenotypeBehaviorCurrent(Dimension size) {
        EvolutionPanel panel = new EvolutionPanel();
        panel.setSize(size);
        BufferedImage bi = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_RGB);
        panel.paintComponent(bi.getGraphics());
        return bi;
    }

    public void init(Properties props) {
    	if (props == null) {
            System.out.println("WARN:  Property object is NULL!");
        }
        mazeType = props.getProperty(MAZE_TYPE_KEY, DEFAULT_MAZE_TYPE);
        maxTimesteps = props.getIntProperty(TIMESTEPS_KEY, DEFAULT_TIMESTEPS);
        goalThreshold = props.getIntProperty(GOAL_THRESHOLD_KEY, DEFAULT_GOAL_THRESHOLD);

        fitnessFunction = getFitnessFunction(props.getProperty(FITNESS_APPROACH_KEY, DEFAULT_FITNESS_APPROACH));
        
        viewerEnabled = props.getBooleanProperty(VIEWER_KEY, DEFAULT_VIEWER);

        noveltyMetric = new MazeDomainNoveltyMetric(props);
        population = new HashMap<Candidate, Path>();
        simulations = new HashMap<EvaluationPanel, Path>();
        allPoints = new HashMap<Candidate, BehaviorVector>();
    }
    private final static String MAZE_TYPE_KEY = "mazeDomain.map";
    private final static String DEFAULT_MAZE_TYPE = "medium.map";
    private static String mazeType = DEFAULT_MAZE_TYPE;

    /**
     *
     * The values:
     * <code>medium.map</code> and
     * <code>hard.map</code> are currently supported.
     *
     * @return
     */
    public Maze getMaze() {
        if (mazeType.equals("medium.map")) {
            return Maze.getMediumMap();
        } else if (mazeType.equals("hard.map")) {
            return Maze.getHardMap();
        } else {
            return Maze.getMediumMap();
        }
    }
    private final String TIMESTEPS_KEY = "mazeDomain.timesteps";
    private final int DEFAULT_TIMESTEPS = 1000;
    private int maxTimesteps = DEFAULT_TIMESTEPS;

    /**
     * The maximum number of timesteps in each trial. Trials end early when the
     * goal is reached Set by property
     * <code>mazeDomain.timesteps</code>.
     *
     * @return The maximum duration of each trial.
     */
    public int getMaxTimesteps() {
        return maxTimesteps;
    }
    private final String GOAL_THRESHOLD_KEY = "mazeDomain.goalThreshold";
    private final int DEFAULT_GOAL_THRESHOLD = 5;
    private int goalThreshold = DEFAULT_GOAL_THRESHOLD;

    /**
     * The maximum number of timesteps in each trial. Trials end early when the
     * goal is reached Set by property
     * <code>mazeDomain.timesteps</code>.
     *
     * @return The maximum duration of each trial.
     */
    public int getGoalThreshold() {
        return goalThreshold;
    }
    private final String FITNESS_APPROACH_KEY = "fitness.function.approach";
    private final String DEFAULT_FITNESS_APPROACH = "distance2goal";
    private FitnessStrategy fitnessFunction = getFitnessFunction(DEFAULT_FITNESS_APPROACH);

    public FitnessStrategy getFitnessFunction(String aFitnessStrategy) {
        if (aFitnessStrategy.equalsIgnoreCase("breadCrumbTrail")) {
            return new BreadCrumbTrail(getMaze());
        }
        if (aFitnessStrategy.equalsIgnoreCase("distance2goal")) {
            return new FastestEuclidianDistance(getMaze());
        }
        return new FastestEuclidianDistance(getMaze());
    }    

    /****************************/
    private JCheckBox showRobotBehavior;
    private void ShowBehaviorActionPerformed(ActionEvent evt) {
        render.setShowRobot(showRobotBehavior.isSelected());
    }

    private JCheckBox showPath;
    private void ShowTrajectoryActionPerformed(ActionEvent evt) {
        render.setShowPath(showPath.isSelected());
    }

    private void setDomainOptions(DomainOptionsPanel options) {
        showRobotBehavior = new javax.swing.JCheckBox();
        showPath = new javax.swing.JCheckBox();
        
        showRobotBehavior.setSelected(true);
        showRobotBehavior.setText("Show Robot Behavior");
        showRobotBehavior.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent evt) {
        		ShowBehaviorActionPerformed(evt);
        	}
        });

        showPath.setSelected(true);
        showPath.setText("Show Robot Path");
        showPath.setToolTipText("Show Robot Path");
        showPath.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent evt) {
        		ShowTrajectoryActionPerformed(evt);
        	}
        });

        GroupLayout domainOptionsPanel1Layout = new GroupLayout(options);
        options.setLayout(domainOptionsPanel1Layout);
        domainOptionsPanel1Layout.setHorizontalGroup(
        		domainOptionsPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
        		.addGroup(domainOptionsPanel1Layout.createSequentialGroup()
        				.addContainerGap()
        				.addGroup(domainOptionsPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
        						.addComponent(showRobotBehavior)
        						.addComponent(showPath))
        						.addContainerGap(531, Short.MAX_VALUE))
        		);
        domainOptionsPanel1Layout.setVerticalGroup(
        		domainOptionsPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
        		.addGroup(domainOptionsPanel1Layout.createSequentialGroup()
        				.addComponent(showRobotBehavior)
        				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        				.addComponent(showPath)
        				.addContainerGap())
        		);
	}

    /****************************/
    private final String VIEWER_KEY = "mazeDomain.enableViwer";
}
