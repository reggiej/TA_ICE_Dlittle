// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.tools.workbench.expressions;

import oracle.toplink.expressions.Expression;
import oracle.toplink.internal.expressions.FunctionExpression;
import oracle.toplink.internal.expressions.QueryKeyExpression;

/**
 * INTERNAL:
 *  This class is used for expressions which only have one argument.
 *  The current operators types are isNull and notNull
 *
 *  If a user chooses a reference object for their queryable argument, they
 *  must choose a unary operator.  A neediness message is given if the user does
 *  not follow this rule.
 *
 *  An example topLink expression which corresponds to a MWUnaryExpression is:
 *  get("address").isNull();
 */
public final class UnaryExpressionRepresentation extends BasicExpressionRepresentation {
    //Operators
    public static final String IS_NULL = "Is Null";
    public static final String NOT_NULL = "Not Null";

    /**
     * Default constructor - for TopLink use only.
     */
    private UnaryExpressionRepresentation() {
        super();
    }

    //parent will always be a BldrCompoundExpression
    UnaryExpressionRepresentation(String operatorType) {
        super(operatorType);
    }

    public boolean isUnaryExpression() {
        return true;
    }

    //Conversion to Runtime
    public Expression convertToRuntime(Expression builder) {
        Expression firstExpression = getFirstArgument().convertToRuntime(builder);

        if (getOperatorType() == IS_NULL) {
            return firstExpression.isNull();
        } else {// if (getOperatorType() == NOT_NULL)
            return firstExpression.notNull();
        }
    }

    public BasicExpressionRepresentation convertFromRuntimeDirectly(Expression expression) {
        Expression firstChildExpression = ((FunctionExpression)expression).getBaseExpression();

        //the first child can only be a queryableArgument if a unary operator is used
        this.setFirstArgument(QueryableArgumentRepresentation.convertFromRuntime((QueryKeyExpression)firstChildExpression));
        return this;
    }

    public String convertToRuntimeString(String builderString) {
        String firstString = getFirstArgument().convertToRuntimeString(builderString);
        String operator;

        if (getOperatorType() == IS_NULL) {
            operator = "isNull";
        } else {// if (getOperatorType() == NOT_NULL)
            operator = "notNull";
        }
        return firstString + "." + operator + "()";
    }
}