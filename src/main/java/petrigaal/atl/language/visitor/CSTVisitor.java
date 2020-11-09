package petrigaal.atl.language.visitor;

import petrigaal.antlr.ATLBaseVisitor;
import petrigaal.antlr.ATLParser.*;
import petrigaal.atl.language.ATLNode;
import petrigaal.atl.language.nodes.Expression;
import petrigaal.atl.language.nodes.Temporal;
import petrigaal.atl.language.nodes.expression.*;
import petrigaal.atl.language.nodes.predicate.BooleanLiteral;
import petrigaal.atl.language.nodes.predicate.RelationalPredicate;
import petrigaal.atl.language.nodes.temporal.BinaryQuantifierTemporal;
import petrigaal.atl.language.nodes.temporal.BinaryTemporal;
import petrigaal.atl.language.nodes.temporal.UnaryQuantifierTemporal;
import petrigaal.atl.language.nodes.temporal.UnaryTemporal;
import petrigaal.petri.Path;

public class CSTVisitor extends ATLBaseVisitor<ATLNode> {
    @Override
    public ATLNode visitStart(StartContext ctx) {
        return visitTemporalQuantifier(ctx.temporalQuantifier());
    }

    @Override
    public Temporal visitTemporalQuantifier(TemporalQuantifierContext ctx) {
        if (ctx.children.size() == 1) {
            return visitTemporalBinary(ctx.temporalBinary());
        } else if (ctx.children.size() < 6) {
            UnaryQuantifierTemporal uqt = new UnaryQuantifierTemporal();
            String playerString = ctx.Path().toString();

            uqt.setPath(playerString.contains("E") ? Path.E : Path.A);
            uqt.setOperator(ctx.getChild(1).getText());
            uqt.setFirstOperand(visitTemporalQuantifier(ctx.temporalQuantifier(0)));

            return uqt;
        } else {
            BinaryQuantifierTemporal bqt = new BinaryQuantifierTemporal();
            String playerString = ctx.Path().toString();

            bqt.setPath(playerString.contains("E") ? Path.E : Path.A);
            bqt.setFirstOperand(visitTemporalQuantifier(ctx.temporalQuantifier(0)));
            bqt.setOperator(ctx.getChild(3).getText());
            bqt.setSecondOperand(visitTemporalQuantifier(ctx.temporalQuantifier(1)));

            return bqt;
        }
    }

    @Override
    public Expression visitExpressionPrimary(ExpressionPrimaryContext ctx) {
        if (ctx.Identifier() != null) {
            return new VariableExpression(ctx.Identifier().getText());
        } else if (ctx.IntLiteral() != null) {
            return new IntegerLiteralExpression(ctx.IntLiteral().getText());
        } else {
            return visitExpressionAdditive(ctx.expressionAdditive());
        }
    }

    @Override
    public Expression visitExpressionUnary(ExpressionUnaryContext ctx) {
        if (ctx.children.size() == 1) {
            return visitExpressionPrimary(ctx.expressionPrimary());
        } else {
            String sign = ctx.children.get(0).getText();
            Expression expr = visitExpressionPrimary(ctx.expressionPrimary());

            if (sign.equals("+")) {
                return expr;
            }

            if (expr instanceof IntegerLiteralExpression) {
                IntegerLiteralExpression intExpr = (IntegerLiteralExpression) expr;
                intExpr.setValue(-intExpr.getValue());
                return expr;
            } else if (expr instanceof VariableExpression) {
                return new UnaryExpression(sign, expr);
            }
        }

        return null;
    }

    @Override
    public Expression visitExpressionMultiplicative(ExpressionMultiplicativeContext ctx) {
        if (ctx.children.size() == 1) {
            return visitExpressionUnary(ctx.expressionUnary());
        } else {
            BinaryExpression bExpr = new BinaryExpression();
            bExpr.setFirstOperand(visitExpressionMultiplicative(ctx.expressionMultiplicative()));
            bExpr.setOperator(ctx.children.get(1).getText());
            bExpr.setSecondOperand(visitExpressionUnary(ctx.expressionUnary()));
            return bExpr;
        }
    }

    @Override
    public Expression visitExpressionAdditive(ExpressionAdditiveContext ctx) {
        if (ctx.children.size() == 1) {
            return visitExpressionMultiplicative(ctx.expressionMultiplicative());
        } else {
            BinaryExpression bExpr = new BinaryExpression();
            bExpr.setFirstOperand(visitExpressionAdditive(ctx.expressionAdditive()));
            bExpr.setOperator(ctx.children.get(1).getText());
            bExpr.setSecondOperand(visitExpressionMultiplicative(ctx.expressionMultiplicative()));
            return bExpr;
        }
    }

    @Override
    public Temporal visitPredicate(PredicateContext ctx) {
        if (ctx.BoolLiteral() != null) {
            return new BooleanLiteral(ctx.BoolLiteral().getText());
        } else if (ctx.Bowtie() != null) {
            RelationalPredicate rp = new RelationalPredicate();
            rp.setFirstOperand(visitExpressionAdditive(ctx.expressionAdditive(0)));
            rp.setSecondOperand(visitExpressionAdditive(ctx.expressionAdditive(1)));
            rp.setOperator(ctx.Bowtie().getText());
            return rp;
        } else {
            return visitTemporalQuantifier(ctx.temporalQuantifier());
        }
    }

    @Override
    public Temporal visitTemporalUnary(TemporalUnaryContext ctx) {
        if (ctx.predicate() != null) {
            return visitPredicate(ctx.predicate());
        } else {
            UnaryTemporal ut = new UnaryTemporal();
            ut.setOperator(ctx.getChild(0).getText());
            ut.setFirstOperand(visitTemporalQuantifier(ctx.temporalQuantifier()));
            return ut;
        }
    }

    @Override
    public Temporal visitTemporalBinary(TemporalBinaryContext ctx) {
        if (ctx.children.size() == 1) {
            return visitTemporalUnary(ctx.temporalUnary(0));
        } else {
            BinaryTemporal bt = new BinaryTemporal();
            bt.setFirstOperand(visitTemporalUnary(ctx.temporalUnary(0)));
            bt.setOperator(ctx.getChild(1).getText());
            bt.setSecondOperand(visitTemporalUnary(ctx.temporalUnary(1)));
            return bt;
        }
    }
}
