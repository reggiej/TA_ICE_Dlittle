// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.history;

import oracle.toplink.expressions.*;
import oracle.toplink.internal.expressions.*;
import oracle.toplink.internal.helper.*;

/**
 * <b>Purpose:</b>Wraps an immutable value for a past time, represented as a
 * database system change number.
 * <p>
 * This should be specified with an Oracle platform supporting flashback,
 * and the value will be written to the SQL FROM clause:
 * <p>
 * SELECT ... FROM EMPLOYEE AS OF SCN (value) t0, ...
 * @since OracleAS TopLink 10<i>g</i> (10.0.3)
 * @author Stephen McRitchie
 * @see AsOfClause
 * @see oracle.toplink.internal.databaseaccess.OraclePlatform#getSystemChangeNumberQuery
 */
public class AsOfSCNClause extends AsOfClause {
    public AsOfSCNClause(Number systemChangeNumber) {
        super(systemChangeNumber);
    }

    public AsOfSCNClause(Long systemChangeNumber) {
        super(systemChangeNumber);
    }

    public AsOfSCNClause(long systemChangeNumber) {
        super(new Long(systemChangeNumber));
    }

    public AsOfSCNClause(Expression expression) {
        super(expression);
    }

    /**
     * INTERNAL:
     * Prints the as of clause for an expression inside of the FROM clause.
     */
    public void printSQL(ExpressionSQLPrinter printer) {
        printer.printString("AS OF SCN (");
        Object value = getValue();
        if (value instanceof Expression) {
            // Sort of an implementation of native sql.
            // Print AS OF SCN (1000L - 45L) not AS OF ('1000L - 45L').
            if ((value instanceof ConstantExpression) && (((ConstantExpression)value).getValue() instanceof String)) {
                printer.printString((String)((ConstantExpression)value).getValue());
            } else {
                printer.printExpression((Expression)value);
            }
        } else {
            ConversionManager converter = ConversionManager.getDefaultManager();
            value = converter.convertObject(value, ClassConstants.LONG);
            printer.printPrimitive(value);
        }
        printer.printString(")");
    }

    /**
     * PUBLIC:
     */
    public boolean isAsOfSCNClause() {
        return true;
    }
}