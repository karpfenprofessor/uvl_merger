package uvl.parse.test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import uvl.UVLJavaLexer;
import uvl.UVLJavaParser;
import uvl.UVLJavaParser.GroupContext;

public class ParseTest1 {

    public static void main(String[] args) throws Exception {
        Path filePath = Paths.get(ParseTest1.class.getClassLoader()
                .getResource("uvl/model_test2.uvl").toURI());
        String content = new String(Files.readAllBytes(filePath));

        CharStream charStream = CharStreams.fromString(content);
        UVLJavaLexer lexer = new UVLJavaLexer(charStream);
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        UVLJavaParser parser = new UVLJavaParser(tokenStream);
        UVLJavaParser.FeatureModelContext featureModelContext = parser.featureModel(); 

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
        }
    }
}
