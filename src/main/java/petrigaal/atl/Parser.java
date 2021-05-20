package petrigaal.atl;

import org.antlr.v4.runtime.*;
import petrigaal.antlr.ATLLexer;
import petrigaal.antlr.ATLParser;
import petrigaal.atl.language.ATLNode;
import petrigaal.atl.language.visitor.CSTVisitor;

public class Parser {
    public ATLNode parse(String atlFormula) {
        ATLLexer lexer = new ATLLexer(CharStreams.fromString(atlFormula));
        ATLParser parser = new ATLParser(new CommonTokenStream(lexer));
        parser.addErrorListener(new BaseErrorListener() {
            @Override
            public void syntaxError(
                    Recognizer<?, ?> recognizer,
                    Object offendingSymbol,
                    int line,
                    int charPositionInLine,
                    String msg,
                    RecognitionException e
            ) {
                throw new CTLSyntaxErrorException("Syntax Error: " + msg);
            }
        });
        parser.setBuildParseTree(true);

        return new CSTVisitor().visitStart(parser.start());
    }
}
