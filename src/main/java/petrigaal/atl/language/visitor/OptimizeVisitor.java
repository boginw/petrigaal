package petrigaal.atl.language.visitor;

import petrigaal.atl.language.ATLFormula;
import petrigaal.atl.language.Visitor;
import petrigaal.atl.language.nodes.Expression;
import petrigaal.atl.language.nodes.Predicate;
import petrigaal.atl.language.nodes.Temporal;
import petrigaal.atl.language.nodes.expression.*;
import petrigaal.atl.language.nodes.predicate.BinaryPredicate;
import petrigaal.atl.language.nodes.predicate.BooleanLiteral;
import petrigaal.atl.language.nodes.predicate.RelationalPredicate;
import petrigaal.atl.language.nodes.temporal.BinaryTemporal;
import petrigaal.atl.language.nodes.temporal.UnaryTemporal;

public class OptimizeVisitor implements Visitor<ATLFormula> {
    @Override
    public ATLFormula visit(ATLFormula ATLFormula) {
        return ATLFormula.visit(this);
    }

    @Override
    public ATLFormula visit(Expression expression) {
        return expression.visit(this);
    }

    @Override
    public ATLFormula visit(IntegerLiteralExpression intLiteral) {
        return intLiteral;
    }

    @Override
    public ATLFormula visit(UnaryExpression unaryExpression) {
        unaryExpression.setFirstOperand((Expression) unaryExpression.visit(this));
        return unaryExpression;
    }

    @Override
    public ATLFormula visit(BinaryExpression binaryExpression) {
        ATLFormula left = binaryExpression.getFirstOperand().visit(this);
        ATLFormula right = binaryExpression.getSecondOperand().visit(this);

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
    public ATLFormula visit(VariableExpression variableExpression) {
        return variableExpression;
    }

    @Override
    public ATLFormula visit(EnabledActions enabledActions) {
        return enabledActions;
    }

    @Override
    public ATLFormula visit(BooleanLiteral boolLiteral) {
        return boolLiteral;
    }

    @Override
    public ATLFormula visit(BinaryPredicate binaryPredicate) {
        ATLFormula left = binaryPredicate.getFirstOperand().visit(this);
        ATLFormula right = binaryPredicate.getSecondOperand().visit(this);

        binaryPredicate.setFirstOperand((Predicate) left);
        binaryPredicate.setFirstOperand((Predicate) right);
        return binaryPredicate;
    }

    @Override
    public ATLFormula visit(RelationalPredicate relationalPredicate) {
        ATLFormula left = relationalPredicate.getFirstOperand().visit(this);
        ATLFormula right = relationalPredicate.getSecondOperand().visit(this);

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
    public ATLFormula visit(BinaryTemporal binaryTemporal) {
        ATLFormula left = binaryTemporal.getFirstOperand().visit(this);
        ATLFormula right = binaryTemporal.getSecondOperand().visit(this);

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

        binaryTemporal.setFirstOperand((Predicate) left);
        binaryTemporal.setSecondOperand((Predicate) right);

        return binaryTemporal;
    }

    @Override
    public ATLFormula visit(UnaryTemporal unaryTemporal) {
        unaryTemporal.setFirstOperand((Temporal) visit(unaryTemporal.getFirstOperand()));
        return unaryTemporal;
    }
}
