// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.tools.workbench.expressions;

import oracle.toplink.expressions.Expression;
import oracle.toplink.internal.expressions.ParameterExpression;

/**
 * INTERNAL:
 * An ParameterArgumentRepresentation is only used as the right hand side of a BinaryExpressionRepresentation
 */
public final class ParameterArgumentRepresentation extends ExpressionArgumentRepresentation {
    private String parameterName;

    /**
     * Default constructor - for TopLink use only.
     */
    private ParameterArgumentRepresentation() {
        super();
    }

    ParameterArgumentRepresentation(String parameterName) {
        this();
        setQueryParameterName(parameterName);
    }

    public String displayString() {
        return "getParameter(\"" + getQueryParameterName() + "\")";
    }

    public String getQueryParameterName() {
        return this.parameterName;
    }

    public boolean isParameterArgument() {
        return true;
    }

    public void setQueryParameterName(String queryParameter) {
        this.parameterName = queryParameter;
    }

    //Conversion to Runtime
    public Expression convertToRuntime(Expression builder) {
        return builder.getParameter(getQueryParameterName());
    }

    public static ParameterArgumentRepresentation convertFromRuntime(ParameterExpression runtimeExpression) {
        String parameterName = runtimeExpression.getField().getName();
        return new ParameterArgumentRepresentation(parameterName);
    }

    public String convertToRuntimeString(String builderString) {
        return builderString + "." + displayString();
    }
}