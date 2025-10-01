package testcases.merge_analysis;

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


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Analysis class for merging all smartwatch models with each other and collecting statistics.
 * This class performs a comprehensive analysis by merging every smartwatch model with every other
 * smartwatch model and writes detailed merge statistics to a results file.
 */
public class SmartwatchMergeAnalysis {
    private static final Logger logger = LogManager.getLogger(SmartwatchMergeAnalysis.class);
    private static final String SECTION_SEPARATOR = "=".repeat(80);
    
    private static final String SMARTWATCH_MODELS_PATH = "uvl/smartwatch/";
    private static final String RESULTS_DIRECTORY = "results/smartwatch/";
    private static final String RESULTS_FILE_PREFIX = "smartwatch_merge_analysis_";
    private static final String SEPARATOR_LINE = "[SmartwatchMergeAnalysis] ═══════════════════════════════════════";
    
    public static void main(String[] args) throws Exception {
        logger.info("[SmartwatchMergeAnalysis] Starting comprehensive smartwatch model merge analysis");
        
        // Create results directory if it doesn't exist
        createResultsDirectory();
        
        // Get all smartwatch model files
        List<String> smartwatchModelPaths = getSmartwatchModelPaths();
        logger.info("[SmartwatchMergeAnalysis] Found {} smartwatch models to analyze", smartwatchModelPaths.size());
        
        // Generate timestamp for results file
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String resultsFilePath = RESULTS_DIRECTORY + RESULTS_FILE_PREFIX + timestamp + ".txt";
        
        // Perform all pairwise merges and write results incrementally
        List<MergeAnalysisResult> allResults = performAllPairwiseMergesWithIncrementalWriting(smartwatchModelPaths, resultsFilePath);
        
        // Write final summary
        writeFinalSummary(allResults, resultsFilePath);
        
        // Print summary
        printSummary(allResults, resultsFilePath);
        
        logger.info("[SmartwatchMergeAnalysis] Analysis completed successfully");
    }
    
    /**
     * Creates the results directory if it doesn't exist
     */
    private static void createResultsDirectory() {
        File resultsDir = new File(RESULTS_DIRECTORY);
        if (!resultsDir.exists()) {
            boolean created = resultsDir.mkdirs();
            if (created) {
                logger.info("[SmartwatchMergeAnalysis] Created results directory: {}", RESULTS_DIRECTORY);
            } else {
                logger.warn("[SmartwatchMergeAnalysis] Failed to create results directory: {}", RESULTS_DIRECTORY);
            }
        }
    }
    
    /**
     * Gets all smartwatch model file paths (base variants only, excluding _realized.uvl files)
     */
    private static List<String> getSmartwatchModelPaths() {
        List<String> modelPaths = new ArrayList<>();
        
        // Add all smartwatch base models (miband1 through miband8, excluding realized versions)
        String[] modelNames = {"miband1", "miband1s", "miband2", "miband3", "miband4", "miband5", "miband6", "miband7", "miband8"};
        
        for (String modelName : modelNames) {
            String modelPath = SMARTWATCH_MODELS_PATH + modelName + ".uvl";
            modelPaths.add(modelPath);
        }
        
        return modelPaths;
    }
    
    /**
     * Performs all pairwise merges between smartwatch models and writes results incrementally
     */
    private static List<MergeAnalysisResult> performAllPairwiseMergesWithIncrementalWriting(List<String> modelPaths, String resultsFilePath) {
        List<MergeAnalysisResult> results = new ArrayList<>();
        int totalMerges = modelPaths.size() * (modelPaths.size() - 1) / 2;
        int currentMerge = 0;
        
        logger.info("[SmartwatchMergeAnalysis] Starting {} pairwise merges", totalMerges);
        
        // Write file header
        try {
            writeFileHeader(resultsFilePath, totalMerges);
        } catch (IOException e) {
            logger.error("[SmartwatchMergeAnalysis] Failed to write file header: {}", e.getMessage(), e);
            return results;
        }
        
        for (int i = 0; i < modelPaths.size(); i++) {
            for (int j = i + 1; j < modelPaths.size(); j++) {
                currentMerge++;
                String modelPathA = modelPaths.get(i);
                String modelPathB = modelPaths.get(j);
                
                logger.info("[SmartwatchMergeAnalysis] Merge {}/{}: {} + {}", 
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
                        logger.error("[SmartwatchMergeAnalysis] Failed to write merge result {}/{}: {}", 
                            currentMerge, totalMerges, e.getMessage(), e);
                    }
                    
                    logger.info("[SmartwatchMergeAnalysis] Merge {}/{} completed successfully", 
                        currentMerge, totalMerges);
                    
                } catch (Exception e) {
                    logger.error("[SmartwatchMergeAnalysis] Merge {}/{} failed: {}", 
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
                        logger.error("[SmartwatchMergeAnalysis] Failed to write error result {}/{}: {}", 
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
        logger.info("[SmartwatchMergeAnalysis] Initializing results file: {}", filePath);
        
        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write("SMARTWATCH MODEL MERGE ANALYSIS RESULTS\n");
            writer.write("======================================\n");
            writer.write("Generated: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "\n");
            writer.write("Total merge operations: " + totalMerges + "\n\n");
        }
        
        logger.info("[SmartwatchMergeAnalysis] File header written successfully");
    }
    
    /**
     * Writes a single merge result to the file (appends to existing file)
     */
    private static void writeMergeResult(String filePath, MergeAnalysisResult result, int currentMerge, int totalMerges) throws IOException {
        try (FileWriter writer = new FileWriter(filePath, true)) { // true for append mode
            writer.write(String.format("MERGE OPERATION %d/%d%n", currentMerge, totalMerges));
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
            
            writer.write("\n" + SECTION_SEPARATOR + "\n\n");
        }
    }
    
    /**
     * Writes the final summary to the file
     */
    private static void writeFinalSummary(List<MergeAnalysisResult> results, String filePath) throws IOException {
        logger.info("[SmartwatchMergeAnalysis] Writing final summary to file");
        
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
        
        logger.info("[SmartwatchMergeAnalysis] Final summary written successfully");
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
        
        logger.info(SEPARATOR_LINE);
        logger.info("[SmartwatchMergeAnalysis] ANALYSIS SUMMARY");
        logger.info(SEPARATOR_LINE);
        logger.info("[SmartwatchMergeAnalysis] Total merge operations: {}", results.size());
        logger.info("[SmartwatchMergeAnalysis] Successful merges: {}", successfulMerges);
        logger.info("[SmartwatchMergeAnalysis] Failed merges: {}", failedMerges);
        double successRate = !results.isEmpty() ? (double) successfulMerges / results.size() * 100 : 0.0;
        String formattedRate = String.format("%.2f", successRate);
        logger.info("[SmartwatchMergeAnalysis] Success rate: {}%", formattedRate);
        logger.info("[SmartwatchMergeAnalysis] Results file: {}", resultsFilePath);
        logger.info(SEPARATOR_LINE);
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
