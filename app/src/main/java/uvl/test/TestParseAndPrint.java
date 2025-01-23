package uvl.test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import uvl.UVLJavaLexer;
import uvl.UVLJavaParser;
import uvl.model.base.Region;
import uvl.model.recreate.RecreationModel;
import uvl.util.RecreationModelAnalyser;
import uvl.util.UVLParser;

public class TestParseAndPrint {

    protected final static Logger logger = LogManager.getLogger(TestParseAndPrint.class);

    public static void main(String[] args) throws Exception {
        //String filePathString = "uvl/model_test2.uvl";
        String filePathString = "uvl/automotive02_parts/automotive02_01.uvl";
        
        Path filePath = Paths.get(TestParseAndPrint.class.getClassLoader()
                .getResource(filePathString).toURI());
        String content = new String(Files.readAllBytes(filePath));

        CharStream charStream = CharStreams.fromString(content);
        UVLJavaLexer lexer = new UVLJavaLexer(charStream);
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        UVLJavaParser parser = new UVLJavaParser(tokenStream);
        UVLJavaParser.FeatureModelContext featureContext = parser.featureModel(); 

        RecreationModel testModel = new RecreationModel(Region.TESTING);
        UVLParser.parseFeatureModel(featureContext, testModel);
        UVLParser.parseConstraints(featureContext.constraints(), testModel);

        RecreationModelAnalyser.printFeatures(testModel);
        RecreationModelAnalyser.printConstraints(testModel);
    }
}
