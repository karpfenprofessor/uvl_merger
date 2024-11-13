package uvl.parse.test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import model.recreate.constraints.AbstractConstraint;
import uvl.UVLJavaLexer;
import uvl.UVLJavaParser;
import uvl.parse.FeatureModelMapper;

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

        List<AbstractConstraint> constraints = FeatureModelMapper.mapToConstraints(featureModelContext);

        constraints.forEach(System.out::println);
    }
}
