// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.tools.workbench.expressions;

import oracle.toplink.expressions.Expression;

/**
 * INTERNAL:
 *  An argument is one side of a MWBasicExpression
 */
public abstract class ExpressionArgumentRepresentation {

    /**
     * Default constructor - for TopLink use only.
     */
    protected ExpressionArgumentRepresentation() {
        super();
    }

    abstract String displayString();

    public boolean isQueryableArgument() {
        return false;
    }

    public boolean isParameterArgument() {
        return false;
    }

    public boolean isLiteralArgument() {
        return false;
    }

    //Conversion to Runtime
    public abstract Expression convertToRuntime(Expression builder);

    //Conversion to Runtime
    public abstract String convertToRuntimeString(String builderString);
}