/**
 * This software is a work of the U.S. Government. It is not subject to copyright 
 * protection and is in the public domain. It may be used as-is or modified and 
 * re-used. The author and the Air Force Institute of Technology would appreciate 
 * credit if this software or parts of it are used or modified for re-use.
 * 
 * Created by Brian Woolley on Sep 23, 2011
 */
package edu.ucf.eplex.naiecFramework.controller;

import com.anji.integration.StepType;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import org.jgap.Chromosome;
import org.jgap.Genotype;
import org.w3c.dom.Node;

/**
 *
 * @author Brian Woolley (brian.woolley at ieee.org)
 */
@XmlRootElement
public class IecStep {

    private Long id;
    private StepType action;
    private Integer champFitness;
    private List<Chromosome> population = new ArrayList<Chromosome>();
    private List<Chromosome> selected = new ArrayList<Chromosome>();
    private List<Chromosome> unselected = new ArrayList<Chromosome>();
    private List<Chromosome> solutions = new ArrayList<Chromosome>();
    private Integer evaluationCount;
    private Integer speciesSize;
    private Integer archiveSize;
    private long startTime = System.currentTimeMillis();
    private long iecEvalTime = -1;
    private long runTime = -1;

    public IecStep(long anId, StepType userAction) {
        this(null, anId, userAction);
    }
    
    public IecStep(Genotype aGenotype, long anId, StepType userAction) {

        id = new Long(anId);
        action = userAction;
        if (aGenotype != null) {
            update(aGenotype);
        }
    }

    IecStep(IecStep anIecStep) {
        id = anIecStep.id;
        action = anIecStep.action;
        champFitness = anIecStep.champFitness;
        population.addAll(anIecStep.population);
        selected.addAll(anIecStep.selected);
        unselected.addAll(anIecStep.unselected);
        solutions.addAll(anIecStep.solutions);
        evaluationCount = anIecStep.evaluationCount;
        speciesSize = anIecStep.speciesSize;
        archiveSize = anIecStep.archiveSize;
        startTime = anIecStep.startTime;
        iecEvalTime = anIecStep.iecEvalTime;
        runTime = anIecStep.runTime;
    }
    
    

    IecStep(Node anXmlStep) {
        loadFromXml(anXmlStep);
    }

    public StepType getAction() {
        return action;
    }

    @XmlAttribute
    public void setAction(StepType action) {
        iecEvalTime = Math.max(System.currentTimeMillis() - startTime, 0);
        this.action = action;
    }

    public Integer getArchiveSize() {
        return archiveSize;
    }

    @XmlElement
    public void setArchiveSize(Integer archiveSize) {
        this.archiveSize = archiveSize;
    }

    public Integer getEvaluationCount() {
        return evaluationCount;
    }

    @XmlElement
    public void setEvaluationCount(Integer evaluationCount) {
        this.evaluationCount = evaluationCount;
    }

    public Long getId() {
        return id;
    }

    @XmlAttribute
    public void setId(Long id) {
        this.id = id;
    }

    public Integer getSpeciesSize() {
        return speciesSize;
    }

    @XmlElement
    public void setSpeciesSize(Integer speciesSize) {
        this.speciesSize = speciesSize;
    }

    public Integer getChampFitness() {
        return champFitness;
    }

    @XmlElement
    public void setChampFitness(Integer champFitness) {
        this.champFitness = champFitness;
    }

    public List<Chromosome> getChromosomes() {
        for (Chromosome chrom : population) {
            if (selected.contains(chrom)) {
                chrom.setIsSelectedForNextGeneration(true);
            } else {
                chrom.setIsSelectedForNextGeneration(false);
            }

            if (solutions.contains(chrom)) {
                chrom.setAsSolution(true);
            } else {
                chrom.setAsSolution(false);
            }
        }
        return new ArrayList<Chromosome>(population);
    }

    public void update(Genotype aGenotype) {
        if (aGenotype != null) {
            setSpeciesSize(aGenotype.getSpecies().size());
            setArchiveSize(aGenotype.getCurrentNoveltyArchiveSize());
            setEvaluationCount(aGenotype.getStepEvaluationCount());
            if (aGenotype.getFittestChromosome() != null) {
                setChampFitness(aGenotype.getFittestChromosome().getFitnessValue());
            }
            if (!population.containsAll(aGenotype.getChromosomes())) {
                population.clear();
                population.addAll(aGenotype.getChromosomes());
            }
            for (Chromosome chrom : population) {
                if (chrom.isSolution()) {
                    solutions.add(chrom);
                }
                if (chrom.isSelectedForNextGeneration()) {
                    selected.add(chrom);
                }
                if (!chrom.isSelectedForNextGeneration() && !chrom.isSolution()) {
                    unselected.add(chrom);
                }
            }
        } else {
            speciesSize = null;
            archiveSize = null;
            evaluationCount = null;
        }
    }

    /**
     * @return 
     * @see com.anji.util.XmlPersistable#toXml()
     */
    public String toXml() {
        StringBuilder result = new StringBuilder();

        result.append(indent(2)).append(open(IEC_STEP_TAG, STEP_ID_TAG + "=\"" + id + "\""));
        result.append(indent(3)).append(textContentElement(ACTION_TAG, action));
        if (evaluationCount != null) {
            result.append(indent(3)).append(textContentElement(EVALUATION_COUNT_TAG, evaluationCount));
        }
        result.append(indent(3)).append(textContentElement(IEC_EVAL_TIME_TAG, runTime));
        result.append(indent(3)).append(textContentElement(RUNTIME_TAG, runTime));
        if (champFitness != null) {
            result.append(indent(3)).append(textContentElement(CHAMP_FITNESS_TAG, champFitness));
        }
        if (archiveSize != null) {
            result.append(indent(3)).append(textContentElement(ARCHIVE_TAG, archiveSize));
        }
        if (speciesSize != null) {
            result.append(indent(3)).append(textContentElement(SPECIES_TAG, speciesSize));
        }

        for (Chromosome chrom : population) {
            boolean isSolution = chrom.isSolution();
            boolean isSelected = chrom.isSelectedForNextGeneration();
            if (solutions.contains(chrom) || selected.contains(chrom)) {
                chrom.setAsSolution(solutions.contains(chrom));
                chrom.setIsSelectedForNextGeneration(selected.contains(chrom));
                result.append(chrom.toXml());
                chrom.setAsSolution(isSolution);
                chrom.setIsSelectedForNextGeneration(isSelected);
            }
        }

        result.append(indent(2)).append(close(IEC_STEP_TAG));

        return result.toString();
    }

    private void loadFromXml(Node step) {
        if (step.hasAttributes()) {
            if (step.getAttributes().getNamedItem(STEP_ID_TAG).getNodeValue().matches("[0-9]+")) {
                id = Long.parseLong(step.getAttributes().getNamedItem(STEP_ID_TAG).getNodeValue());
            }
        }

        Node child = step.getFirstChild();
        while (child != null) {
            if (child.getNodeName().equalsIgnoreCase(ACTION_TAG)) {
                action = StepType.valueOf(child.getTextContent());

            }

            if (child.getNodeName().equalsIgnoreCase(EVALUATION_COUNT_TAG)) {
                if (child.getTextContent().matches("[0-9]+")) {
                    evaluationCount = Integer.parseInt(child.getTextContent());
                }
            }

            if (child.getNodeName().equalsIgnoreCase(CHAMP_FITNESS_TAG)) {
                if (child.getTextContent().matches("[0-9]+")) {
                    champFitness = Integer.parseInt(child.getTextContent());
                }
            }

            if (child.getNodeName().equalsIgnoreCase(ARCHIVE_TAG)) {
                if (child.getTextContent().matches("[0-9]+")) {
                    archiveSize = Integer.parseInt(child.getTextContent());
                }
            }

            if (child.getNodeName().equalsIgnoreCase(SPECIES_TAG)) {
                if (child.getTextContent().matches("[0-9]+")) {
                    speciesSize = Integer.parseInt(child.getTextContent());
                }
            }

            if (child.getNodeName().equalsIgnoreCase(Chromosome.CHROMOSOME_TAG)) {

                Node xmlChromosome = step.getFirstChild();
                while (xmlChromosome != null) {
                    if (xmlChromosome.getNodeName().equalsIgnoreCase(Chromosome.CHROMOSOME_TAG)) {
                        Chromosome chrom = new Chromosome(xmlChromosome);
                        population.add(chrom);
                    }
                    xmlChromosome = xmlChromosome.getNextSibling();
                }

                solutions = new ArrayList<Chromosome>();
                selected = new ArrayList<Chromosome>();
                for (Chromosome chrom : population) {
                    if (chrom.isSolution()) {
                        solutions.add(chrom);
                    } else {
                        selected.add(chrom);
                    }
                }

            }

            child = child.getNextSibling();
        }
    }

    /*
     *  <step id="0">
     *      <action>INITIAL</action>
     *      <evaluations>250</evaluations>
     *      <archive count="178" />
     *      <species count="1" />
     *      <chromosome id="120" >
     *              ...
     *      </chromosome>
     *  </step>
     */
    private String open(String label, String attributes) {
        return new StringBuilder().append("<").append(label).append(" ").append(attributes).append(">\n").toString();
    }

    private String close(String label) {
        return new StringBuilder().append("</").append(label).append(">\n").toString();
    }

    private String textContentElement(String label, Object value) {
        return new StringBuilder().append("<").append(label).append(">").append(value).append("</").append(label).append(">\n").toString();
    }

    private String indent(int x) {
        StringBuilder indention = new StringBuilder();
        for (int i = 0; i < x; i++) {
            indention.append("    ");
        }
        return indention.toString();
    }
    /**
     * XML base tag
     */
    public final static String IEC_STEP_TAG = "step";
    public final static String STEP_ID_TAG = "id";
    public final static String ACTION_TAG = "action";
    public final static String IEC_EVAL_TIME_TAG = "iecEvalTime";
    public final static String RUNTIME_TAG = "runTime";
    public final static String EVALUATION_COUNT_TAG = "evaluations";
    public final static String CHAMP_FITNESS_TAG = "champFitness";
    public final static String SPECIES_TAG = "species";
    public final static String ARCHIVE_TAG = "archive";
    public final static String COMMAND_TAG = "command";

    public boolean hasSolution() {
        return !solutions.isEmpty();
    }

    public int getChampConnectionCount() {
        if (solutions.isEmpty()) {
            return -1;
        } else {
            return solutions.get(0).countLinks();
        }
    }

    public int getChampNodeCount() {
        if (solutions.isEmpty()) {
            return -1;
        } else {
            return solutions.get(0).countNodes();
        }
    }

    void recordEndOfStep() {
        runTime = Math.max(System.currentTimeMillis() - startTime - iecEvalTime, 0);
    }
    
    /**
     * Reports the time (mS) spent building the current population  
     * @return the number of milliseconds spent by the operation
     */
    public long getRuntime() {
        if (runTime < 0) {
            return System.currentTimeMillis() - startTime;
        } else {
            return runTime;
        }
    }
    
    public long getIecEvalTime() {
        if (iecEvalTime < 0) {
            return System.currentTimeMillis() - startTime;
        } else {
            return iecEvalTime;
        }
    }
}
