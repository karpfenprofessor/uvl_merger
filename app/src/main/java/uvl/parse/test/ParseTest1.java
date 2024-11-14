package uvl.parse.test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

import uvl.UVLJavaLexer;
import uvl.UVLJavaParser;

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
        
        printTree(featureModelContext, parser);
    }

    public static void printTree(ParseTree tree, UVLJavaParser parser) {
        printTree(tree, parser, 0);
    }

    private static void printTree(ParseTree tree, UVLJavaParser parser, int level) {
        String indent = " ".repeat(level);
        String nodeText = tree.getText();

        if (tree.getChildCount() > 0) {
            nodeText = parser.getRuleNames()[((org.antlr.v4.runtime.RuleContext) tree).getRuleIndex()];
        }

        System.out.println(indent + nodeText);

        for (int i = 0; i < tree.getChildCount(); i++) {
            printTree(tree.getChild(i), parser, level + 1);
        }
    }
}
