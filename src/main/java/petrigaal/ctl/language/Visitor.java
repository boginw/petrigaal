package petrigaal.ctl.language;

import petrigaal.ctl.language.nodes.Expression;
import petrigaal.ctl.language.nodes.expression.*;
import petrigaal.ctl.language.nodes.predicate.BooleanLiteral;
import petrigaal.ctl.language.nodes.predicate.RelationalPredicate;
import petrigaal.ctl.language.nodes.temporal.BinaryQuantifierTemporal;
import petrigaal.ctl.language.nodes.temporal.BinaryTemporal;
import petrigaal.ctl.language.nodes.temporal.UnaryQuantifierTemporal;
import petrigaal.ctl.language.nodes.temporal.UnaryTemporal;

public interface Visitor<T> {
    T visit(CTLNode CTLNode);

    // Expressions
    T visit(Expression expression);

    T visit(IntegerLiteralExpression intLiteral);

    T visit(UnaryExpression unaryExpression);

    T visit(BinaryExpression binaryExpression);

    T visit(VariableExpression variableExpression);

    // Predicates
    T visit(BooleanLiteral boolLiteral);

    T visit(RelationalPredicate relationalPredicate);

    // Temporal
    T visit(BinaryTemporal binaryTemporal);

    T visit(UnaryTemporal unaryTemporal);

    T visit(BinaryQuantifierTemporal binaryQuantifierTemporal);

    T visit(UnaryQuantifierTemporal unaryQuantifierTemporal);
}
