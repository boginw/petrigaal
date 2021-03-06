package petrigaal.ctl.language;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.jupiter.api.Test;
import petrigaal.antlr.CTLLexer;
import petrigaal.antlr.CTLParser;
import petrigaal.antlr.CTLParser.*;
import petrigaal.ctl.language.nodes.Expression;
import petrigaal.ctl.language.nodes.Temporal;
import petrigaal.ctl.language.nodes.expression.*;
import petrigaal.ctl.language.nodes.predicate.BooleanLiteral;
import petrigaal.ctl.language.nodes.predicate.RelationalPredicate;
import petrigaal.ctl.language.nodes.temporal.BinaryQuantifierTemporal;
import petrigaal.ctl.language.nodes.temporal.BinaryTemporal;
import petrigaal.ctl.language.nodes.temporal.UnaryQuantifierTemporal;
import petrigaal.ctl.language.nodes.temporal.UnaryTemporal;
import petrigaal.ctl.language.visitor.CSTVisitor;

import static org.junit.jupiter.api.Assertions.*;
import static petrigaal.ctl.language.Path.E;
import static petrigaal.ctl.language.Path.A;

class CSTVisitorTest {

    @Test
    void visitPrimaryIdentifier() {
        String identifier = "a";
        ExpressionPrimaryContext primary = parser(identifier).expressionPrimary();
        Expression n = new CSTVisitor().visitExpressionPrimary(primary);
        VariableExpression expr = checkInstance(n, VariableExpression.class);
        assertEquals(identifier, expr.getIdentifier());
    }

    @Test
    void visitPrimaryIntegerLiteral() {
        Integer integer = 123;
        ExpressionPrimaryContext primary = parser(String.valueOf(integer)).expressionPrimary();
        Expression n = new CSTVisitor().visitExpressionPrimary(primary);
        IntegerLiteralExpression expr = checkInstance(n, IntegerLiteralExpression.class);
        assertEquals(integer, expr.getValue());
    }

    @Test
    void visitPrimaryGoesToVisitAdditive() {
        String additive = "1 + 2";
        String wrapped = String.format("(%s)", additive);
        ExpressionPrimaryContext primaryContext = parser(wrapped).expressionPrimary();
        ExpressionAdditiveContext additiveContext = parser(additive).expressionAdditive();
        assertEquals(
                new CSTVisitor().visitExpressionPrimary(primaryContext),
                new CSTVisitor().visitExpressionAdditive(additiveContext)
        );
    }

    @Test
    void visitUnaryExpressionPositive() {
        String add = "+1";
        ExpressionUnaryContext unaryContext = parser(add).expressionUnary();
        Expression n = new CSTVisitor().visitExpressionUnary(unaryContext);
        IntegerLiteralExpression expr = checkInstance(n, IntegerLiteralExpression.class);
        assertEquals(Integer.valueOf(+1), expr.getValue());
    }

    @Test
    void visitUnaryExpressionNegative() {
        String neg = "-1";
        ExpressionUnaryContext unaryContext = parser(neg).expressionUnary();
        Expression n = new CSTVisitor().visitExpressionUnary(unaryContext);
        IntegerLiteralExpression expr = checkInstance(n, IntegerLiteralExpression.class);
        assertEquals(Integer.valueOf(-1), expr.getValue());
    }

    @Test
    void visitUnaryExpressionPositiveVariable() {
        String add = "+a";
        ExpressionUnaryContext unaryContext = parser(add).expressionUnary();
        Expression n = new CSTVisitor().visitExpressionUnary(unaryContext);
        VariableExpression expr = checkInstance(n, VariableExpression.class);
        assertEquals("a", expr.getIdentifier());
    }

    @Test
    void visitUnaryExpressionNegativeVariable() {
        String add = "-a";
        ExpressionUnaryContext unaryContext = parser(add).expressionUnary();
        Expression n = new CSTVisitor().visitExpressionUnary(unaryContext);
        UnaryExpression expr = checkInstance(n, UnaryExpression.class);
        VariableExpression var = checkInstance(expr.getFirstOperand(), VariableExpression.class);
        assertEquals("-", expr.getOperator());
        assertEquals("a", var.getIdentifier());
    }

    @Test
    void visitUnaryGoesToPrimary() {
        String identifier = "a";
        ExpressionUnaryContext unaryContext = parser(identifier).expressionUnary();
        ExpressionPrimaryContext primaryContext = parser(identifier).expressionPrimary();
        assertEquals(
                new CSTVisitor().visitExpressionUnary(unaryContext),
                new CSTVisitor().visitExpressionPrimary(primaryContext)
        );
    }

    @Test
    void visitMultiplicative() {
        String eq = "1 * 2";
        ExpressionMultiplicativeContext mc = parser(eq).expressionMultiplicative();
        Expression n = new CSTVisitor().visitExpressionMultiplicative(mc);
        BinaryExpression expr = checkInstance(n, BinaryExpression.class);
        assertEquals(new IntegerLiteralExpression("1"), expr.getFirstOperand());
        assertEquals("*", expr.getOperator());
        assertEquals(new IntegerLiteralExpression("2"), expr.getSecondOperand());
    }

    @Test
    void visitMultiplicativeGoesToUnary() {
        String identifier = "-a";

        ExpressionUnaryContext unaryContext = parser(identifier)
                .expressionUnary();
        ExpressionMultiplicativeContext multiplicativeContext = parser(identifier)
                .expressionMultiplicative();

        assertEquals(
                new CSTVisitor().visitExpressionMultiplicative(multiplicativeContext),
                new CSTVisitor().visitExpressionUnary(unaryContext)
        );
    }

    @Test
    void visitAdditiveExpressionPlus() {
        String eq = "a + b";
        ExpressionAdditiveContext mc = parser(eq).expressionAdditive();
        Expression n = new CSTVisitor().visitExpressionAdditive(mc);
        BinaryExpression expr = checkInstance(n, BinaryExpression.class);
        assertEquals(new VariableExpression("a"), expr.getFirstOperand());
        assertEquals("+", expr.getOperator());
        assertEquals(new VariableExpression("b"), expr.getSecondOperand());
    }

    @Test
    void visitAdditiveExpressionMinus() {
        String eq = "a - b";
        ExpressionAdditiveContext mc = parser(eq).expressionAdditive();
        Expression n = new CSTVisitor().visitExpressionAdditive(mc);
        BinaryExpression expr = checkInstance(n, BinaryExpression.class);
        assertEquals(new VariableExpression("a"), expr.getFirstOperand());
        assertEquals("-", expr.getOperator());
        assertEquals(new VariableExpression("b"), expr.getSecondOperand());
    }

    @Test
    void visitAdditiveGoesToMultiplicative() {
        String identifier = "a * b";

        ExpressionMultiplicativeContext multiplicativeContext = parser(identifier)
                .expressionMultiplicative();
        ExpressionAdditiveContext additiveContext = parser(identifier)
                .expressionAdditive();

        assertEquals(
                new CSTVisitor().visitExpressionAdditive(additiveContext),
                new CSTVisitor().visitExpressionMultiplicative(multiplicativeContext)
        );
    }

    @Test
    void visitPredicateBoolTrue() {
        String bool = "true";

        PredicateContext pc = parser(bool).predicate();
        Temporal n = new CSTVisitor().visitPredicate(pc);
        BooleanLiteral booleanLiteral = checkInstance(n, BooleanLiteral.class);
        assertTrue(booleanLiteral.getValue());
    }

    @Test
    void visitPredicateBoolFalse() {
        String bool = "false";

        PredicateContext pc = parser(bool).predicate();
        Temporal n = new CSTVisitor().visitPredicate(pc);
        BooleanLiteral booleanLiteral = checkInstance(n, BooleanLiteral.class);
        assertFalse(booleanLiteral.getValue());
    }

    @Test
    void visitPredicateRelational() {
        String rel = "1 < a";
        PredicateContext pc = parser(rel).predicate();
        Temporal n = new CSTVisitor().visitPredicate(pc);
        RelationalPredicate rp = checkInstance(n, RelationalPredicate.class);
        assertEquals(new IntegerLiteralExpression("1"), rp.getFirstOperand());
        assertEquals("<", rp.getOperator());
        assertEquals(new VariableExpression("a"), rp.getSecondOperand());
    }

    @Test
    void visitPredicateGoesToTemporalBinary() {
        String pred = "true";
        String wrapped = String.format("(%s)", pred);

        TemporalBinaryContext tbc = parser(pred).temporalBinary();
        PredicateContext pc = parser(wrapped).predicate();

        assertEquals(
                new CSTVisitor().visitTemporalBinary(tbc),
                new CSTVisitor().visitPredicate(pc)
        );
    }

    @Test
    void visitUnaryTemporalGoesToPredicate() {
        String pred = "true";

        TemporalUnaryContext tuc = parser(pred).temporalUnary();
        PredicateContext pc = parser(pred).predicate();

        assertEquals(
                new CSTVisitor().visitTemporalUnary(tuc),
                new CSTVisitor().visitPredicate(pc)
        );
    }

    @Test
    void visitUnaryTemporalGoesToTemporalBinary() {
        String pred = "true";
        String wrapped = "!" + pred;

        TemporalUnaryContext tuc = parser(wrapped).temporalUnary();
        TemporalBinaryContext tbc = parser(pred).temporalBinary();
        Temporal t = new CSTVisitor().visitTemporalUnary(tuc);
        UnaryTemporal ut = checkInstance(t, UnaryTemporal.class);

        assertEquals("!", ut.getOperator());
        assertEquals(
                ut.getFirstOperand(),
                new CSTVisitor().visitTemporalBinary(tbc)
        );
    }

    @Test
    void visitTemporalBinaryGoesToTemporalUnary() {
        String unary = "!true";
        TemporalUnaryContext tuc = parser(unary).temporalUnary();
        TemporalBinaryContext tbc = parser(unary).temporalBinary();

        assertEquals(
                new CSTVisitor().visitTemporalUnary(tuc),
                new CSTVisitor().visitTemporalBinary(tbc)
        );
    }

    @Test
    void visitTemporalBinary() {
        String binary = "true & true";
        TemporalBinaryContext tbc = parser(binary).temporalBinary();
        Temporal t = new CSTVisitor().visitTemporalBinary(tbc);
        BinaryTemporal bt = checkInstance(t, BinaryTemporal.class);
        assertEquals(new BooleanLiteral("true"), bt.getFirstOperand());
        assertEquals("&", bt.getOperator());
        assertEquals(new BooleanLiteral("true"), bt.getSecondOperand());
    }

    @Test
    void visitTemporalQuantifier() {
        String tq = "E X(true)";
        TemporalQuantifierContext tqc = parser(tq).temporalQuantifier();
        Temporal t = new CSTVisitor().visitTemporalQuantifier(tqc);
        UnaryQuantifierTemporal ut = checkInstance(t, UnaryQuantifierTemporal.class);
        assertEquals(E, ut.getPath());
        assertEquals(new BooleanLiteral("true"), ut.getFirstOperand());
        assertEquals("X", ut.getOperator());
    }

    @Test
    void visitTemporalQuantifierEnvironment() {
        String tq = "A X(true)";
        TemporalQuantifierContext tqc = parser(tq).temporalQuantifier();
        Temporal t = new CSTVisitor().visitTemporalQuantifier(tqc);
        UnaryQuantifierTemporal ut = checkInstance(t, UnaryQuantifierTemporal.class);
        assertEquals(A, ut.getPath());
        assertEquals(new BooleanLiteral("true"), ut.getFirstOperand());
        assertEquals("X", ut.getOperator());
    }

    @Test
    void visitTemporalQuantifierUntil() {
        String tq = "E(true U false)";
        TemporalQuantifierContext tqc = parser(tq).temporalQuantifier();
        Temporal t = new CSTVisitor().visitTemporalQuantifier(tqc);
        BinaryQuantifierTemporal bt = checkInstance(t, BinaryQuantifierTemporal.class);
        assertEquals(E, bt.getPath());
        assertEquals(new BooleanLiteral("true"), bt.getFirstOperand());
        assertEquals(new BooleanLiteral("false"), bt.getSecondOperand());
        assertEquals("U", bt.getOperator());
    }

    @Test
    void visitTemporalQuantifierUntilEnvironment() {
        String tq = "A(true U false)";
        TemporalQuantifierContext tqc = parser(tq).temporalQuantifier();
        Temporal t = new CSTVisitor().visitTemporalQuantifier(tqc);
        BinaryQuantifierTemporal bt = checkInstance(t, BinaryQuantifierTemporal.class);
        assertEquals(A, bt.getPath());
        assertEquals(new BooleanLiteral("true"), bt.getFirstOperand());
        assertEquals(new BooleanLiteral("false"), bt.getSecondOperand());
        assertEquals("U", bt.getOperator());
    }

    @Test
    void visitStartGoesToTemporalQuantifier() {
        String tq = "E(true U false)";
        StartContext sc = parser(tq).start();
        TemporalQuantifierContext tbc = parser(tq).temporalQuantifier();
        assertEquals(
                new CSTVisitor().visitStart(sc),
                new CSTVisitor().visitTemporalQuantifier(tbc)
        );
    }

    @SuppressWarnings("unchecked")
    private <T, K> K checkInstance(T n, Class<K> kClass) {
        assertTrue(kClass.isInstance(n));
        return (K) n;
    }

    private CTLParser parser(String atlFormula) {
        CTLLexer lexer = new CTLLexer(CharStreams.fromString(atlFormula));
        CTLParser parser = new CTLParser(new CommonTokenStream(lexer));
        parser.setBuildParseTree(true);
        return parser;
    }
}
