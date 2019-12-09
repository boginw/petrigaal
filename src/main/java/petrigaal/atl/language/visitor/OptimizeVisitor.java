package petrigaal.atl.language.visitor;

import petrigaal.atl.language.ATLNode;
import petrigaal.atl.language.Visitor;
import petrigaal.atl.language.nodes.Expression;
import petrigaal.atl.language.nodes.Temporal;
import petrigaal.atl.language.nodes.expression.*;
import petrigaal.atl.language.nodes.predicate.BooleanLiteral;
import petrigaal.atl.language.nodes.predicate.RelationalPredicate;
import petrigaal.atl.language.nodes.temporal.BinaryQuantifierTemporal;
import petrigaal.atl.language.nodes.temporal.BinaryTemporal;
import petrigaal.atl.language.nodes.temporal.UnaryQuantifierTemporal;
import petrigaal.atl.language.nodes.temporal.UnaryTemporal;

public class OptimizeVisitor implements Visitor<ATLNode> {
    @Override
    public ATLNode visit(ATLNode ATLNode) {
        return ATLNode.visit(this);
    }

    @Override
    public ATLNode visit(Expression expression) {
        return expression.visit(this);
    }

    @Override
    public ATLNode visit(IntegerLiteralExpression intLiteral) {
        return intLiteral;
    }

    @Override
    public ATLNode visit(UnaryExpression unaryExpression) {
        unaryExpression.setFirstOperand((Expression) unaryExpression.visit(this));
        return unaryExpression;
    }

    @Override
    public ATLNode visit(BinaryExpression binaryExpression) {
        ATLNode left = binaryExpression.getFirstOperand().visit(this);
        ATLNode right = binaryExpression.getSecondOperand().visit(this);

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
    public ATLNode visit(VariableExpression variableExpression) {
        return variableExpression;
    }

    @Override
    public ATLNode visit(EnabledActions enabledActions) {
        return enabledActions;
    }

    @Override
    public ATLNode visit(BooleanLiteral boolLiteral) {
        return boolLiteral;
    }

    @Override
    public ATLNode visit(RelationalPredicate relationalPredicate) {
        ATLNode left = relationalPredicate.getFirstOperand().visit(this);
        ATLNode right = relationalPredicate.getSecondOperand().visit(this);

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
    public ATLNode visit(BinaryTemporal binaryTemporal) {
        ATLNode left = binaryTemporal.getFirstOperand().visit(this);
        ATLNode right = binaryTemporal.getSecondOperand().visit(this);

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
    public ATLNode visit(UnaryTemporal unaryTemporal) {
        unaryTemporal.setFirstOperand((Temporal) visit(unaryTemporal.getFirstOperand()));
        return unaryTemporal;
    }

    @Override
    public ATLNode visit(BinaryQuantifierTemporal binaryQuantifierTemporal) {
        binaryQuantifierTemporal.setFirstOperand(
                (Temporal) visit(binaryQuantifierTemporal.getFirstOperand())
        );
        binaryQuantifierTemporal.setSecondOperand(
                (Temporal) visit(binaryQuantifierTemporal.getSecondOperand())
        );
        return binaryQuantifierTemporal;
    }

    @Override
    public ATLNode visit(UnaryQuantifierTemporal unaryQuantifierTemporal) {
        unaryQuantifierTemporal.setFirstOperand(
                (Temporal) visit(unaryQuantifierTemporal.getFirstOperand())
        );
        return unaryQuantifierTemporal;
    }
}
