package uvl.parse.test;

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
import uvl.utility.UVLUtilityParser;

public class ParseTest1 {

    protected final static Logger logger = LogManager.getLogger(ParseTest1.class);

    public static void main(String[] args) throws Exception {
        //String filePathString = "uvl/model_test2.uvl";
        String filePathString = "uvl/automotive02_parts/automotive02_01.uvl";
        
        Path filePath = Paths.get(ParseTest1.class.getClassLoader()
                .getResource(filePathString).toURI());
        String content = new String(Files.readAllBytes(filePath));

        CharStream charStream = CharStreams.fromString(content);
        UVLJavaLexer lexer = new UVLJavaLexer(charStream);
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        UVLJavaParser parser = new UVLJavaParser(tokenStream);
        UVLJavaParser.FeatureModelContext featureContext = parser.featureModel(); 

        RecreationModel testModel = new RecreationModel(Region.TESTING);
        UVLUtilityParser.parseFeatureModel(featureContext, testModel);
        UVLUtilityParser.parseConstraints(featureContext.constraints(), testModel);

        testModel.printConstraints();
        testModel.printFeatures();
    }
}
