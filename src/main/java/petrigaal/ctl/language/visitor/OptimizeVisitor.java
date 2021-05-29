package petrigaal.ctl.language.visitor;

import petrigaal.ctl.language.CTLNode;
import petrigaal.ctl.language.Visitor;
import petrigaal.ctl.language.nodes.Expression;
import petrigaal.ctl.language.nodes.Temporal;
import petrigaal.ctl.language.nodes.expression.*;
import petrigaal.ctl.language.nodes.predicate.BooleanLiteral;
import petrigaal.ctl.language.nodes.predicate.RelationalPredicate;
import petrigaal.ctl.language.nodes.temporal.BinaryQuantifierTemporal;
import petrigaal.ctl.language.nodes.temporal.BinaryTemporal;
import petrigaal.ctl.language.nodes.temporal.UnaryQuantifierTemporal;
import petrigaal.ctl.language.nodes.temporal.UnaryTemporal;

public class OptimizeVisitor implements Visitor<CTLNode> {
    @Override
    public CTLNode visit(CTLNode CTLNode) {
        return CTLNode.visit(this);
    }

    @Override
    public CTLNode visit(Expression expression) {
        return expression.visit(this);
    }

    @Override
    public CTLNode visit(IntegerLiteralExpression intLiteral) {
        return intLiteral;
    }

    @Override
    public CTLNode visit(UnaryExpression unaryExpression) {
        unaryExpression.setFirstOperand((Expression) unaryExpression.visit(this));
        return unaryExpression;
    }

    @Override
    public CTLNode visit(BinaryExpression binaryExpression) {
        CTLNode left = binaryExpression.getFirstOperand().visit(this);
        CTLNode right = binaryExpression.getSecondOperand().visit(this);

        if (left instanceof IntegerLiteralExpression && right instanceof IntegerLiteralExpression) {
            int l = ((IntegerLiteralExpression) left).getValue();
            int r = ((IntegerLiteralExpression) right).getValue();

            switch (binaryExpression.getOperator()) {
                case "+":
                    return new IntegerLiteralExpression(l + r);
                case "-":
                    return new IntegerLiteralExpression(l - r);
                case "*":
                    return new IntegerLiteralExpression(l * r);
            }
        }

        binaryExpression.setFirstOperand((Expression) left);
        binaryExpression.setSecondOperand((Expression) right);

        return binaryExpression;
    }

    @Override
    public CTLNode visit(VariableExpression variableExpression) {
        return variableExpression;
    }

    @Override
    public CTLNode visit(BooleanLiteral boolLiteral) {
        return boolLiteral;
    }

    @Override
    public CTLNode visit(RelationalPredicate relationalPredicate) {
        CTLNode left = relationalPredicate.getFirstOperand().visit(this);
        CTLNode right = relationalPredicate.getSecondOperand().visit(this);

        if (left instanceof IntegerLiteralExpression && right instanceof IntegerLiteralExpression) {
            int l = ((IntegerLiteralExpression) left).getValue();
            int r = ((IntegerLiteralExpression) right).getValue();

            switch (relationalPredicate.getOperator()) {
                case "<":
                    return new BooleanLiteral(l < r);
                case "<=":
                    return new BooleanLiteral(l <= r);
                case "==":
                    return new BooleanLiteral(l == r);
                case ">=":
                    return new BooleanLiteral(l >= r);
                case ">":
                    return new BooleanLiteral(l > r);
            }
        }

        relationalPredicate.setFirstOperand((Expression) left);
        relationalPredicate.setSecondOperand((Expression) right);

        return relationalPredicate;
    }

    @Override
    public CTLNode visit(BinaryTemporal binaryTemporal) {
        CTLNode left = binaryTemporal.getFirstOperand().visit(this);
        CTLNode right = binaryTemporal.getSecondOperand().visit(this);

        if (left instanceof BooleanLiteral && right instanceof BooleanLiteral) {
            boolean l = ((BooleanLiteral) left).getValue();
            boolean r = ((BooleanLiteral) right).getValue();

            switch (binaryTemporal.getOperator()) {
                case "|":
                    return new BooleanLiteral(l || r);
                case "&":
                    return new BooleanLiteral(l && r);
            }
        }

        binaryTemporal.setFirstOperand((Temporal) left);
        binaryTemporal.setSecondOperand((Temporal) right);

        return binaryTemporal;
    }

    @Override
    public CTLNode visit(UnaryTemporal unaryTemporal) {
        unaryTemporal.setFirstOperand((Temporal) visit(unaryTemporal.getFirstOperand()));
        return unaryTemporal;
    }

    @Override
    public CTLNode visit(BinaryQuantifierTemporal binaryQuantifierTemporal) {
        binaryQuantifierTemporal.setFirstOperand(
                (Temporal) visit(binaryQuantifierTemporal.getFirstOperand())
        );
        binaryQuantifierTemporal.setSecondOperand(
                (Temporal) visit(binaryQuantifierTemporal.getSecondOperand())
        );
        return binaryQuantifierTemporal;
    }

    @Override
    public CTLNode visit(UnaryQuantifierTemporal unaryQuantifierTemporal) {
        unaryQuantifierTemporal.setFirstOperand(
                (Temporal) visit(unaryQuantifierTemporal.getFirstOperand())
        );
        return unaryQuantifierTemporal;
    }
}
