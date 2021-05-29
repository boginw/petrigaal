package petrigaal.ctl;

import org.antlr.v4.runtime.*;
import petrigaal.antlr.CTLLexer;
import petrigaal.antlr.CTLParser;
import petrigaal.ctl.language.CTLNode;
import petrigaal.ctl.language.visitor.CSTVisitor;

public class Parser {
    public CTLNode parse(String atlFormula) {
        CTLLexer lexer = new CTLLexer(CharStreams.fromString(atlFormula));
        CTLParser parser = new CTLParser(new CommonTokenStream(lexer));
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
