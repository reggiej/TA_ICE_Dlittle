// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.xml;


/**
 * This policy is used by <code>XMLFileAccessorFilePolicy</code> to
 * "normalize" file names; i.e. convert invalid characters into something
 * acceptable to the current O/S.
 *
 * @author Big Country
 * @since TOPLink/Java 4.5
 * @deprecated since OracleAS TopLink 10<i>g</i> (10.1.3).  This class is replaced by
 *         {@link oracle.toplink.ox}
 */
public interface FileNameNormalizer {

    /**
     * Convert the specified, <i>unqualified</i> file name into something
     * that should be palatable as a file name
     * (e.g. replace invalid characters with escape sequences).
     * The name must be unqualified so we don't convert any legitimate
     * file name separators.
     */
    String normalize(String unqualifiedFileName);
}