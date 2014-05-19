// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.platform.database;

import java.io.StringWriter;

import oracle.toplink.expressions.ExpressionOperator;
import oracle.toplink.queryframework.ValueReadQuery;

/**
 *    <B>Purpose</B>: Provides DB2 Mainframe specific behaviour.<P>
 *    <B>Responsibilities</B>:
 *        <UL>
 *            <LI>Specialized CONCAT syntax
 *        </UL>
 *
 * @since TopLink 3.0.3
 */
public class DB2MainframePlatform extends DB2Platform {

    /**
     * Initialize any platform-specific operators
     */
    protected void initializePlatformOperators() {
        super.initializePlatformOperators();

        addOperator(ExpressionOperator.simpleLogicalNoParens(ExpressionOperator.Concat, "CONCAT"));
    }

}