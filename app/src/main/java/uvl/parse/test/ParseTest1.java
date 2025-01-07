package uvl.parse.test;

import java.nio.charset.StandardCharsets;
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

public class ParseTest1 {

    protected final static Logger logger = LogManager.getLogger(ParseTest1.class);

    public static void main(String[] args) throws Exception {
        Path filePath = Paths.get(ParseTest1.class.getClassLoader()
                .getResource("uvl/automotive02_parts/automotive02_01.uvl").toURI());
        String content = new String(Files.readAllBytes(filePath));

        CharStream charStream = CharStreams.fromString(content);
        UVLJavaLexer lexer = new UVLJavaLexer(charStream);
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        UVLJavaParser parser = new UVLJavaParser(tokenStream);
        UVLJavaParser.FeatureModelContext featureModelContext = parser.featureModel(); 

        String parseTreeString = featureModelContext.toStringTree(parser);
        String formattedTree = parseTreeString
            .replace("\\t", "\t") // Replace literal "\t" with an actual tab
            .replace("\\n", "\n"); // Replace literal "\n" with an actual newline
        Files.write(Paths.get("logs/automotive02_parts_01.log"), formattedTree.getBytes(StandardCharsets.UTF_8));
        logger.warn("Parse tree written to logs/parse_tree.log");


        /*
        UVLJavaParser.FeaturesContext rootFeature = featureModelContext.features();
        String rootFeatureName = rootFeature.toInfoString(parser);
        System.out.println("rootFeatureTree:" + rootFeatureName);
        UVLJavaParser.FeatureContext featureContext = rootFeature.feature();
        String featureContextName = featureContext.toInfoString(parser);
        System.out.println("featureContextTree: " + featureContextName);
        if (!featureContext.group().isEmpty()) {
            System.out.println("group: " + featureContext.group().size());
            for(GroupContext g : featureContext.group()) {
                System.out.println(g.toInfoString(parser));
            }
        }

        if(!featureContext.children.isEmpty()) {
            System.out.println("children: " + featureContext.children.size());
            for(int i = 0; i < featureContext.children.size(); i++) {
                System.out.println(featureContext.children.get(i).toString());
            }
        }*/
    }
}
