package petrigaal.atl.language;

import petrigaal.atl.language.nodes.Expression;
import petrigaal.atl.language.nodes.expression.*;
import petrigaal.atl.language.nodes.predicate.BooleanLiteral;
import petrigaal.atl.language.nodes.predicate.RelationalPredicate;
import petrigaal.atl.language.nodes.temporal.BinaryQuantifierTemporal;
import petrigaal.atl.language.nodes.temporal.BinaryTemporal;
import petrigaal.atl.language.nodes.temporal.UnaryQuantifierTemporal;
import petrigaal.atl.language.nodes.temporal.UnaryTemporal;

public interface Visitor<T> {
    T visit(ATLNode ATLNode);

    // Expressions
    T visit(Expression expression);

    T visit(IntegerLiteralExpression intLiteral);

    T visit(UnaryExpression unaryExpression);

    T visit(BinaryExpression binaryExpression);

    T visit(VariableExpression variableExpression);

    T visit(EnabledActions enabledActions);

    // Predicates
    T visit(BooleanLiteral boolLiteral);

    T visit(RelationalPredicate relationalPredicate);

    // Temporal
    T visit(BinaryTemporal binaryTemporal);

    T visit(UnaryTemporal unaryTemporal);

    T visit(BinaryQuantifierTemporal binaryQuantifierTemporal);

    T visit(UnaryQuantifierTemporal unaryQuantifierTemporal);
}
