package uvl.metrics;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uvl.model.recreate.RecreationModel;
import java.util.*;

public class ModelMetrics {
    private static final Logger logger = LogManager.getLogger(ModelMetrics.class);
    
    // Time measurements
    private Map<String, Long> mergeOperationTimes;
    private List<Long> consistencyCheckTimes;
    private long totalMergeTime;
    private long startTime;
    
    // Constraint metrics
    private int initialConstraints;
    private int finalConstraints;
    private float contextualizationShare;
    
    // Solution metrics
    private int consistencyChecks;
    private int successfulChecks;
    private long solutionSpaceSize;
    
    public ModelMetrics() {
        this.mergeOperationTimes = new HashMap<>();
        this.consistencyCheckTimes = new ArrayList<>();
        this.consistencyChecks = 0;
        this.successfulChecks = 0;
    }
    
    public void startMergeOperation() {
        this.startTime = System.nanoTime();
    }
    
    public void recordMergeOperation(String operationName) {
        long duration = System.nanoTime() - startTime;
        mergeOperationTimes.put(operationName, duration);
        totalMergeTime += duration;
    }
    
    public void recordConsistencyCheck(long duration, boolean wasConsistent) {
        consistencyCheckTimes.add(duration);
        consistencyChecks++;
        if (wasConsistent) successfulChecks++;
    }
    
    public void setInitialConstraints(RecreationModel model1, RecreationModel model2) {
        this.initialConstraints = model1.getConstraints().size() + model2.getConstraints().size();
    }
    
    public void setFinalModelMetrics(RecreationModel mergedModel) {
        this.finalConstraints = mergedModel.getConstraints().size();
        this.contextualizationShare = mergedModel.analyseContextualizationShare();
    }
    
    public void setSolutionSpaceSize(long solutions) {
        this.solutionSpaceSize = solutions;
    }
    
    public void printMetrics() {
        logger.info("=== Merge Performance Metrics ===");
        logger.info("Time Metrics:");
        logger.info("  Total merge time: {} ns", totalMergeTime);
        logger.info("  Average consistency check time: {} ns", 
            consistencyCheckTimes.stream().mapToLong(Long::longValue).average().orElse(0.0));
        
        logger.info("\nConstraint Metrics:");
        logger.info("  Initial constraints: {}", initialConstraints);
        logger.info("  Final constraints: {}", finalConstraints);
        logger.info("  Contextualization share: {}", contextualizationShare);
        
        logger.info("\nSolution Metrics:");
        logger.info("  Consistency checks performed: {}", consistencyChecks);
        logger.info("  Successful checks: {}", successfulChecks);
        logger.info("  Final solution space size: {}", solutionSpaceSize);
        
        logger.info("\nDetailed Operation Times:");
        mergeOperationTimes.forEach((op, time) -> 
            logger.info("  {}: {} ns", op, time));
    }
} 