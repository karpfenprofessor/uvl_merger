package testcases;

import util.UVLParser;
import util.Merger;
import util.Merger.MergeResult;
import util.analyse.statistics.MergeStatistics;
import model.choco.Region;
import model.recreate.RecreationModel;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Analysis class for merging all finance models with each other and collecting statistics.
 * This class performs a comprehensive analysis by merging every finance model with every other
 * finance model and writes detailed merge statistics to a results file.
 */
public class FinanceMergeAnalysis {
    private static final Logger logger = LogManager.getLogger(FinanceMergeAnalysis.class);
    
    private static final String FINANCE_MODELS_PATH = "uvl/finance/";
    private static final String RESULTS_DIRECTORY = "results/";
    private static final String RESULTS_FILE_PREFIX = "finance_merge_analysis_";
    
    public static void main(String[] args) throws Exception {
        logger.info("[FinanceMergeAnalysis] Starting comprehensive finance model merge analysis");
        
        // Create results directory if it doesn't exist
        createResultsDirectory();
        
        // Get all finance model files
        List<String> financeModelPaths = getFinanceModelPaths();
        logger.info("[FinanceMergeAnalysis] Found {} finance models to analyze", financeModelPaths.size());
        
        // Generate timestamp for results file
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String resultsFilePath = RESULTS_DIRECTORY + RESULTS_FILE_PREFIX + timestamp + ".txt";
        
        // Perform all pairwise merges and write results incrementally
        List<MergeAnalysisResult> allResults = performAllPairwiseMergesWithIncrementalWriting(financeModelPaths, resultsFilePath);
        
        // Write final summary
        writeFinalSummary(allResults, resultsFilePath);
        
        // Print summary
        printSummary(allResults, resultsFilePath);
        
        logger.info("[FinanceMergeAnalysis] Analysis completed successfully");
    }
    
    /**
     * Creates the results directory if it doesn't exist
     */
    private static void createResultsDirectory() throws IOException {
        File resultsDir = new File(RESULTS_DIRECTORY);
        if (!resultsDir.exists()) {
            boolean created = resultsDir.mkdirs();
            if (created) {
                logger.info("[FinanceMergeAnalysis] Created results directory: {}", RESULTS_DIRECTORY);
            } else {
                logger.warn("[FinanceMergeAnalysis] Failed to create results directory: {}", RESULTS_DIRECTORY);
            }
        }
    }
    
    /**
     * Gets all finance model file paths
     */
    private static List<String> getFinanceModelPaths() {
        List<String> modelPaths = new ArrayList<>();
        
        // Add all finance models (finance_1.uvl through finance_10.uvl)
        for (int i = 2; i <= 10; i++) {
            modelPaths.add(FINANCE_MODELS_PATH + "finance_" + i + ".uvl");
        }
        
        return modelPaths;
    }
    
    /**
     * Performs all pairwise merges between finance models and writes results incrementally
     */
    private static List<MergeAnalysisResult> performAllPairwiseMergesWithIncrementalWriting(List<String> modelPaths, String resultsFilePath) {
        List<MergeAnalysisResult> results = new ArrayList<>();
        int totalMerges = modelPaths.size() * (modelPaths.size() - 1) / 2;
        int currentMerge = 0;
        
        logger.info("[FinanceMergeAnalysis] Starting {} pairwise merges", totalMerges);
        
        // Write file header
        try {
            writeFileHeader(resultsFilePath, totalMerges);
        } catch (IOException e) {
            logger.error("[FinanceMergeAnalysis] Failed to write file header: {}", e.getMessage(), e);
            return results;
        }
        
        for (int i = 0; i < modelPaths.size(); i++) {
            for (int j = i + 1; j < modelPaths.size(); j++) {
                currentMerge++;
                String modelPathA = modelPaths.get(i);
                String modelPathB = modelPaths.get(j);
                
                logger.info("[FinanceMergeAnalysis] Merge {}/{}: {} + {}", 
                    currentMerge, totalMerges, modelPathA, modelPathB);
                
                try {
                    // Parse models
                    RecreationModel modelA = UVLParser.parseUVLFile(modelPathA, Region.A);
                    RecreationModel modelB = UVLParser.parseUVLFile(modelPathB, Region.B);
                    
                    // Get shared features analysis before merging
                    String sharedFeaturesAnalysis = getSharedFeaturesAnalysis(modelA, modelB);
                    
                    // Perform merge
                    MergeResult mergeResult = Merger.fullMerge(modelA, modelB);
                    
                    // Store result
                    MergeAnalysisResult analysisResult = new MergeAnalysisResult(
                        modelPathA, modelPathB, mergeResult.mergedStatistics(), sharedFeaturesAnalysis, null
                    );
                    results.add(analysisResult);
                    
                    // Write result immediately to file
                    try {
                        writeMergeResult(resultsFilePath, analysisResult, currentMerge, totalMerges);
                    } catch (IOException e) {
                        logger.error("[FinanceMergeAnalysis] Failed to write merge result {}/{}: {}", 
                            currentMerge, totalMerges, e.getMessage(), e);
                    }
                    
                    logger.info("[FinanceMergeAnalysis] Merge {}/{} completed successfully", 
                        currentMerge, totalMerges);
                    
                } catch (Exception e) {
                    logger.error("[FinanceMergeAnalysis] Merge {}/{} failed: {}", 
                        currentMerge, totalMerges, e.getMessage(), e);
                    
                    // Store error result
                    MergeAnalysisResult errorResult = new MergeAnalysisResult(
                        modelPathA, modelPathB, null, null, e.getMessage()
                    );
                    results.add(errorResult);
                    
                    // Write error result immediately to file
                    try {
                        writeMergeResult(resultsFilePath, errorResult, currentMerge, totalMerges);
                    } catch (IOException e2) {
                        logger.error("[FinanceMergeAnalysis] Failed to write error result {}/{}: {}", 
                            currentMerge, totalMerges, e2.getMessage(), e2);
                    }
                }
            }
        }
        
        return results;
    }
    
    /**
     * Writes the file header
     */
    private static void writeFileHeader(String filePath, int totalMerges) throws IOException {
        logger.info("[FinanceMergeAnalysis] Initializing results file: {}", filePath);
        
        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write("FINANCE MODEL MERGE ANALYSIS RESULTS\n");
            writer.write("===================================\n");
            writer.write("Generated: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "\n");
            writer.write("Total merge operations: " + totalMerges + "\n\n");
        }
        
        logger.info("[FinanceMergeAnalysis] File header written successfully");
    }
    
    /**
     * Writes a single merge result to the file (appends to existing file)
     */
    private static void writeMergeResult(String filePath, MergeAnalysisResult result, int currentMerge, int totalMerges) throws IOException {
        try (FileWriter writer = new FileWriter(filePath, true)) { // true for append mode
            writer.write(String.format("MERGE OPERATION %d/%d\n", currentMerge, totalMerges));
            writer.write("─".repeat(50) + "\n");
            writer.write("Model A: " + result.modelPathA() + "\n");
            writer.write("Model B: " + result.modelPathB() + "\n");
            
            if (result.errorMessage() != null) {
                writer.write("STATUS: FAILED\n");
                writer.write("ERROR: " + result.errorMessage() + "\n");
            } else {
                writer.write("STATUS: SUCCESS\n");
                writer.write("\n" + result.sharedFeaturesAnalysis() + "\n");
                writer.write("\n" + result.statistics().toString() + "\n");
            }
            
            writer.write("\n" + "=".repeat(80) + "\n\n");
        }
    }
    
    /**
     * Writes the final summary to the file
     */
    private static void writeFinalSummary(List<MergeAnalysisResult> results, String filePath) throws IOException {
        logger.info("[FinanceMergeAnalysis] Writing final summary to file");
        
        int successfulMerges = (int) results.stream().mapToLong(r -> r.errorMessage() == null ? 1 : 0).sum();
        int failedMerges = results.size() - successfulMerges;
        
        try (FileWriter writer = new FileWriter(filePath, true)) { // true for append mode
            writer.write("SUMMARY\n");
            writer.write("=======\n");
            writer.write("Total operations: " + results.size() + "\n");
            writer.write("Successful merges: " + successfulMerges + "\n");
            writer.write("Failed merges: " + failedMerges + "\n");
            writer.write("Success rate: " + String.format("%.2f%%", (double) successfulMerges / results.size() * 100) + "\n");
        }
        
        logger.info("[FinanceMergeAnalysis] Final summary written successfully");
    }
    
    /**
     * Creates a string representation of shared features analysis between two models
     */
    private static String getSharedFeaturesAnalysis(RecreationModel modelA, RecreationModel modelB) {
        StringBuilder sb = new StringBuilder();
        
        List<Set<String>> featureSets = Arrays.asList(
            new HashSet<>(modelA.getFeatures().keySet()),
            new HashSet<>(modelB.getFeatures().keySet())
        );
        
        Set<String> sharedFeatures = new HashSet<>(featureSets.get(0));
        sharedFeatures.retainAll(featureSets.get(1));
        
        // Calculate unique features for each model
        Set<String> exclusiveToModelA = new HashSet<>(featureSets.get(0));
        exclusiveToModelA.removeAll(featureSets.get(1));
        
        Set<String> exclusiveToModelB = new HashSet<>(featureSets.get(1));
        exclusiveToModelB.removeAll(featureSets.get(0));
        
        int totalUniqueFeatures = sharedFeatures.size() + exclusiveToModelA.size() + exclusiveToModelB.size();
        float shareRatio = totalUniqueFeatures > 0 ? (float) sharedFeatures.size() / totalUniqueFeatures * 100 : 0;
        
        sb.append("SHARED FEATURES ANALYSIS\n");
        sb.append("─".repeat(40)).append("\n");
        sb.append("Model A features: ").append(featureSets.get(0).size()).append("\n");
        sb.append("Model B features: ").append(featureSets.get(1).size()).append("\n");
        sb.append("Shared features: ").append(sharedFeatures.size()).append("\n");
        sb.append("Features exclusive to Model A: ").append(exclusiveToModelA.size()).append("\n");
        sb.append("Features exclusive to Model B: ").append(exclusiveToModelB.size()).append("\n");
        sb.append("Total unique features: ").append(totalUniqueFeatures).append("\n");
        sb.append("Share ratio: ").append(String.format("%.2f%%", shareRatio)).append("\n");
        
        return sb.toString();
    }
    
    /**
     * Prints a summary of the analysis to the console
     */
    private static void printSummary(List<MergeAnalysisResult> results, String resultsFilePath) {
        int successfulMerges = (int) results.stream().mapToLong(r -> r.errorMessage() == null ? 1 : 0).sum();
        int failedMerges = results.size() - successfulMerges;
        
        logger.info("[FinanceMergeAnalysis] ═══════════════════════════════════════");
        logger.info("[FinanceMergeAnalysis] ANALYSIS SUMMARY");
        logger.info("[FinanceMergeAnalysis] ═══════════════════════════════════════");
        logger.info("[FinanceMergeAnalysis] Total merge operations: {}", results.size());
        logger.info("[FinanceMergeAnalysis] Successful merges: {}", successfulMerges);
        logger.info("[FinanceMergeAnalysis] Failed merges: {}", failedMerges);
        logger.info("[FinanceMergeAnalysis] Success rate: {:.2f}%", (double) successfulMerges / results.size() * 100);
        logger.info("[FinanceMergeAnalysis] Results file: {}", resultsFilePath);
        logger.info("[FinanceMergeAnalysis] ═══════════════════════════════════════");
    }
    
    /**
     * Record to hold the result of a single merge analysis
     */
    private record MergeAnalysisResult(
        String modelPathA,
        String modelPathB,
        MergeStatistics statistics,
        String sharedFeaturesAnalysis,
        String errorMessage
    ) {}
}
