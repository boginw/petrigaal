package petrigaal.atl;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import petrigaal.antlr.ATLLexer;
import petrigaal.antlr.ATLParser;
import petrigaal.atl.language.visitor.CSTVisitor;
import petrigaal.atl.language.ATLFormula;

public class Parser {
    public ATLFormula parse(String atlFormula) {
        ATLLexer lexer = new ATLLexer(CharStreams.fromString(atlFormula));
        ATLParser parser = new ATLParser(new CommonTokenStream(lexer));
        parser.setBuildParseTree(true);

        return new CSTVisitor().visitStart(parser.start());
    }
}
