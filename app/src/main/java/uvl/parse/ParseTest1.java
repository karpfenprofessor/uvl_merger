package uvl.parse;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import uvl.UVLJavaBaseListener;
import uvl.UVLJavaLexer;
import uvl.UVLJavaParser;

public class ParseTest1 {
    public static void main(String[] args) throws Exception {
        Path filePath = Paths.get(ParseTest1.class.getClassLoader()
                .getResource("uvl/model_test1.uvl").toURI());
        String content = new String(Files.readAllBytes(filePath));

        CharStream charStream = CharStreams.fromString(content);
        UVLJavaLexer lexer = new UVLJavaLexer(charStream);
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        UVLJavaParser parser = new UVLJavaParser(tokenStream);
        UVLJavaParser.FeatureModelContext featureModelContext = parser.featureModel();

        ParseTreeWalker walker = new ParseTreeWalker();
        walker.walk(new UVLJavaBaseListener(), featureModelContext);

        System.out.println(featureModelContext.toStringTree(parser));
    }
}
