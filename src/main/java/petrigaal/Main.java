package petrigaal;

import petrigaal.atl.Optimizer;
import petrigaal.atl.Parser;
import petrigaal.atl.language.ATLFormula;

public class Main {
    public static void main(String[] args) {
        ATLFormula tree = new Parser().parse("a > (66 * 55)");
        ATLFormula optimizedTree = new Optimizer().optimize(tree);
        System.out.println(optimizedTree.getLiteral());
    }
}
