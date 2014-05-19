// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.tools.ejbjar;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.w3c.dom.Document;

/**
 * INTERNAL:
 * Represents query tag object.
 */
public class Query extends Description {
    QueryMethod queryMethod;// Required
    String resultTypeMapping;
    String ejbql;// Required
    public static final String TL_IMPLEMENTED_EQBQL = "TopLink_Implemented_Query";

    public Query() {
        super();
        queryMethod = new QueryMethod();
        ejbql = new String();
    }

    /**
      * @return String the EJBQL String
      */
    public String getEjbql() {
        return ejbql;
    }

    /**
      * @return QueryMethod the query method description
      */
    public QueryMethod getQueryMethod() {
        return queryMethod;
    }

    /**
      * @return ResultTypeMapping the result type mapping for ejbSelects
      */
    public String getResultTypeMapping() {
        return resultTypeMapping;
    }

    /**
      * Sets the ResultTypeMapping for ejbSelects
      */
    public void setResultTypeMapping(String resultTypeMapping) {
        this.resultTypeMapping = resultTypeMapping;
    }

    /**
      * Return the text value of the given element if it exists
      * Had to override from DomObject to return CDATA
      */
    public String getTextValue(Element element) {
        StringBuffer buffer = new StringBuffer();
        if (element == null) {
            return null;
        }
        NodeList valueChildren = element.getChildNodes();
        if (valueChildren.getLength() != 0) {
            // iterate
            int numItems = valueChildren.getLength();
            int i = 0;
            while (i != numItems) {
                Node valueChild = valueChildren.item(i);
                if (valueChild.getNodeType() == Node.TEXT_NODE) {
                    buffer.append(((Text)valueChild).getData().trim());
                } else if (valueChild.getNodeType() == Node.CDATA_SECTION_NODE) {
                    buffer.append(((Text)valueChild).getData().trim());
                }
                i++;
            }
            return buffer.toString();

            //Node valueChild = valueChildren.item(0); 
            //if(valueChild.getNodeType()==Node.TEXT_NODE) return((Text)valueChild).getData().trim();
        }
        return "";
    }

    public boolean isFinder() {
        return ((String)getQueryMethod().getMethodName()).startsWith("find");
    }

    public boolean isSelector() {
        return ((String)getQueryMethod().getMethodName()).startsWith("ejbSelect");
    }

    /**
      * @param ejbql the EJBQL String for this query
      */
    public void setEjbql(String ejbql) {
        this.ejbql = ejbql;
    }

    /**
      * @param queryMeth the QueryMethod object
      */
    public void setQueryMethod(QueryMethod queryMeth) {
        queryMethod = queryMeth;
    }

    /**
      * Load the data for this instance from the specified element.
      * @param e the DOM element
      */
    public void loadFromElement(Element e) {
        super.loadFromElement(e);
        queryMethod = (QueryMethod)objectFromElement(e, QUERY_METHOD, new QueryMethod());
        resultTypeMapping = optionalStringFromElement(e, RESULT_TYPE_MAPPING);
        ejbql = stringFromElement(e, EJB_QL);

        /* CR3386: Steven Vo
         * This is a TEMPORARY fix to write out non-empty ejbql string.
         * This fix should be removed when WebLogic makes the patch for this
         */
        if (ejbql.equals(Query.TL_IMPLEMENTED_EQBQL)) {
            ejbql = new String();
        }
    }

    /**
      * Return the data from this instance as a DOM element.
      * @param doc a Document instance used to create elements
      */
    public Element toElement(Document doc) {
        Element e = doc.createElement(QUERY);
        inheritedFields(doc, e);

        e.appendChild(queryMethod.toElement(doc));
        optionallyAddText(doc, e, RESULT_TYPE_MAPPING, resultTypeMapping);

        // Ejbql is in a CDATA section
        Element ejbqlElt = doc.createElement(EJB_QL);

        /* CR3386: Steven Vo
         * This is a TEMPORARY fix to write out non-empty ejbql string.
         * This fix should be removed when WebLogic makes the patch for this
         */
        if (ejbql.length() == 0) {
            ejbqlElt.appendChild(doc.createCDATASection(Query.TL_IMPLEMENTED_EQBQL));
        } else {
            ejbqlElt.appendChild(doc.createCDATASection(ejbql));
        }

        e.appendChild(ejbqlElt);
        return e;
    }
}