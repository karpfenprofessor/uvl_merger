package uvl.test;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import uvl.model.base.BaseModel;
import uvl.model.recreate.RecreationModel;
import uvl.util.ChocoTranslator;
import uvl.util.UVLParser;

public class ParseTest3SolveAllFinancialParts {
    protected final static Logger logger = LogManager.getLogger(ParseTest3SolveAllFinancialParts.class);
    private static final String LOG_FILE = "logs/ParseTest3SolveAllFinancialParts_results.log";
    
    public static void main(String[] args) {
        String[] files = {
            "uvl/financial_parts/financial_01.uvl",
            "uvl/financial_parts/financial_02.uvl",
            "uvl/financial_parts/financial_03.uvl",
            "uvl/financial_parts/financial_04.uvl",
            "uvl/financial_parts/financial_05.uvl",
            "uvl/financial_parts/financial_06.uvl",
            "uvl/financial_parts/financial_07.uvl"
        };

        try (FileWriter writer = new FileWriter(LOG_FILE, true)) {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            writer.write("\n=== Test Run: " + timestamp + " ===\n");

            for (String file : files) {
                try {
                    writer.write("\nProcessing: " + file + "\n");

                    RecreationModel recModel = UVLParser.parseUVLFile(file);
                    BaseModel chocoModel = ChocoTranslator.convertToChocoModel(recModel);
                    
                    long solutions = chocoModel.solveAndReturnNumberOfSolutions();
                    
                    String result = String.format("Number of solutions: %d\n", solutions);
                    writer.write(result);
                    logger.info(result);

                } catch (Exception e) {
                    String error = "Error processing " + file + ": " + e.getMessage() + "\n";
                    writer.write(error);
                    logger.error(error, e);
                }
            }
        } catch (IOException e) {
            logger.error("Error writing to log file", e);
        }
    }
} 