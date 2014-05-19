// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.tools.ejbjar;

import oracle.toplink.internal.security.PrivilegedAccessHelper;
import oracle.toplink.internal.security.PrivilegedNewInstanceFromClass;

import org.w3c.dom.Element;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.util.*;

/**
 * INTERNAL:
 * Abstract DOM class that implements behaviour
 * required for all DOM objects. Some helper
 * methods may also be made available on this class.
 */
public abstract class DomObject implements EjbJarConstants {

    /**
     * Add the collection to the parent element if a value is supplied.
     * Each element in the collection will be inserted as a separate element.
     * Signal an exception if no collection was specified.
     */
    protected void addCollection(Document doc, Element parent, Vector v) {
        if (v == null) {
            missingObjectValueError("child of " + parent.getTagName());
        }
        optionallyAddCollection(doc, parent, v);
    }

    /**
     * Add the text string to the parent element.
     * Signal an exception if no value is supplied.
     */
    protected void addText(Document doc, Element parent, String tag, String valueText) {
        if (valueText == null) {
            missingObjectValueError(tag);
        } else {
            optionallyAddText(doc, parent, tag, valueText);
        }
    }

    /**
     * Add the collection of String to the parent element.
     * Each element in the collection will get its own element with
     * the given tag.
     * Signal an exception if no collection exists.
     */
    protected void addTextCollection(Document doc, Element parent, String tag, Vector v) {
        if (v == null) {
            missingObjectValueError("child of " + parent.getTagName());
        }
        optionallyAddTextCollection(doc, parent, tag, v);
    }

    /**
     * Return an element with an optional text sub-element.
     */
    protected Element createElement(Document doc, String tag, String textValue) {
        Element elt = doc.createElement(tag);
        if (textValue != null) {
            elt.appendChild(doc.createTextNode(textValue));
        }
        return elt;
    }

    protected void emptyCollectionError(String component) {
        throw new Error("Collection of " + component + " must be non-empty");
    }

    /**
     * Return the first element in the parent with the given tag name.
     */
    protected Element getFirstElementByTagName(String tagName, Element parent) {
        if (parent == null) {
            return null;
        }

        NodeList subElements = parent.getChildNodes();

        for (int i = 0; i < subElements.getLength(); i++) {
            Node nextChild = subElements.item(i);

            if ((nextChild.getNodeName() != null) && nextChild.getNodeName().equals(tagName)) {
                return (Element)nextChild;
            }
        }

        // There were no elements
        return null;
    }

    /**
     * Return the direct children elements in the parent with the given tag name.
     */
    protected List getChildrenElementsByTagName(String tagName, Element parent) {
        List nodes = new ArrayList();
        for (Node child = parent.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (child.getNodeName().equals(tagName)) {
                nodes.add((Element)child);
            }
        }
        return nodes;
    }

    /**
     * Return the text value of the given element if it exists
     */
    protected String getTextValue(Element element) {
        if (element == null) {
            return null;
        }
        NodeList valueChildren = element.getChildNodes();
        if (valueChildren.getLength() != 0) {
            Node valueChild = valueChildren.item(0);
            if (valueChild.getNodeType() == Node.TEXT_NODE) {
                return ((Text)valueChild).getData().trim();
            }
        }
        return "";
    }

    protected void instantiationError(Exception ex) {
        throw new Error("Could not instantiate " + this + " - " + ex);
    }

    /**
     * Load the data for this instance from the specified element.
     *
     * @param e the DOM element
     */
    public abstract void loadFromElement(Element e);

    protected void missingElementError(String tag) {
        throw new Error("Missing " + tag + " element");
    }

    protected void missingObjectValueError(String tag) {
        throw new Error("Missing object value for " + tag + " element");
    }

    /**
     * Return an instance of the specified object by looking
     * in the specified parent element and loading a new
     * instance of the object. According to the DTD the element
     * must be there.
     */
    protected DomObject objectFromElement(Element e, String tag, DomObject obj) {
        Vector v = objectsFromElement(e, tag, obj);
        return (DomObject)v.elementAt(0);
    }

    /**
     * Return a Vector of the specified object type by looking
     * in the specified parent element and loading new instances
     * of the object. According to the DTD at least one element
     * must be there.
     */
    protected Vector objectsFromElement(Element e, String tag, DomObject obj) {
        Vector v = optionalObjectsFromElement(e, tag, obj);

        // Do the check anyway just because it feels right
        if (v == null) {
            missingElementError(tag);
        }
        return v;
    }

    /**
     * Add the optional collection to the parent element if a value is supplied.
     * Each element in the collection will be inserted as a separate element.
     * Do nothing if no collection exists.
     */
    protected void optionallyAddCollection(Document doc, Element parent, Vector v) {
        // If the parent is null, or the collection is null then don't add it
        if ((parent != null) && (v != null)) {
            int siz_ = v.size();
            for (int i = 0; i < siz_; i++) {
                Element child = ((DomObject)v.elementAt(i)).toElement(doc);
                parent.appendChild(child);
            }
        }
    }

    /**
     * Add the optional text string to the parent element if a value is supplied.
     * Do nothing if no value exists.
     */
    protected void optionallyAddText(Document doc, Element parent, String tag, String valueText) {
        // If the parent is null, or the valueText is null then don't add it
        if ((parent != null) && (valueText != null)) {
            Element child = createElement(doc, tag, valueText);
            parent.appendChild(child);
        }
    }

    /**
     * Add the attribute name:value pair to the elemnt if attrValue is defined.
     */
    protected void optionallyAddAttribute(Element elem, String attrName, String attrValue) {
        // Added only if both element and the attribute value are defined (not null)
        if ((elem != null) && (attrValue != null)) {
            elem.setAttribute(attrName, attrValue);
        }
    }

    /**
     * Add the optional collection of String to the parent element if a
     * collection is supplied.
     * Each element in the collection will get its own element with the given tag.
     * Do nothing if no collection exists.
     */
    protected void optionallyAddTextCollection(Document doc, Element parent, String tag, Vector v) {
        // If the parent is null, or the valueText is null then don't add it
        if ((parent != null) && (v != null)) {
            int siz_ = v.size();
            for (int i = 0; i < siz_; i++) {
                Element child = createElement(doc, tag, (String)v.elementAt(i));
                parent.appendChild(child);
            }
        }
    }

    /**
     * Return an instance of the specified object by looking
     * in the specified parent element and loading a new
     * instance of the object. Return null if none was found.
     */
    protected DomObject optionalObjectFromElement(Element e, String tag, DomObject obj) {
        return optionalObjectFromElement(e, tag, obj, false);
    }

    /**
     * Return an instance of the specified object by looking
     * in the specified parent element and loading a new
     * instance of the object. Return null if none was found.
     */
    protected DomObject optionalObjectFromElement(Element e, String tag, DomObject obj, boolean isChildrenOnly) {
        Vector v = optionalObjectsFromElement(e, tag, obj, isChildrenOnly);
        return ((v == null) || v.isEmpty()) ? null : (DomObject)v.elementAt(0);
    }

    /**
     * Return a Vector of the specified object type by looking
     * in the specified parent element and loading new instances
     * of the object. Return null if none were found.
     * return all descendent.
     */
    protected Vector optionalObjectsFromElement(Element e, String tag, DomObject obj) {
        return optionalObjectsFromElement(e, tag, obj, false);
    }

    /**
     * Return a Vector of the specified object type by looking
     * in the specified parent element and loading new instances
     * of the object. Return null if none were found.
     * If boolean value isChildrenOnly is ture, only return the direct children level.
     */
    protected Vector optionalObjectsFromElement(Element e, String tag, DomObject obj, boolean isChildrenOnly) {
        List nodes = new ArrayList();
        if (isChildrenOnly) {
            nodes = getChildrenElementsByTagName(tag, e);
        } else {
            NodeList elts = e.getElementsByTagName(tag);
            if (elts == null) {
                return null;
            }
            int len_ = elts.getLength();
            for (int i = 0; i < len_; i++) {
                nodes.add((Element)elts.item(i));
            }
        }

        Vector v = new Vector();
        for (Iterator nodeIter = nodes.iterator(); nodeIter.hasNext();) {
            Element elem = (Element)nodeIter.next();
            DomObject newObj = null;
            try {
                if (PrivilegedAccessHelper.shouldUsePrivilegedAccess()){
                    try{
                        newObj = (DomObject)AccessController.doPrivileged(new PrivilegedNewInstanceFromClass(obj.getClass()));
                    }catch (PrivilegedActionException ex){
                        throw (RuntimeException)ex.getCause();
                    }
                }else{
                    newObj = (DomObject)PrivilegedAccessHelper.newInstanceFromClass(obj.getClass());
                }
            } catch (InstantiationException iEx) {
                instantiationError(iEx);
            } catch (IllegalAccessException iaEx) {
                instantiationError(iaEx);
            }
            newObj.loadFromElement(elem);
            v.add(newObj);
        }
        return v;
    }

    /**
     * Return a String by looking in the specified element
     * and pulling out the attribute value stored in the given attribute.
     * Return null if it was not present.
     */
    protected String optionalAttributeFromElement(Element elem, String attrName) {
        String attrValue = elem.getAttribute(attrName);//return empty string if not defined

        //default value could be null.
        return ((attrValue == null) || (attrValue == "")) ? null : attrValue;
    }

    /**
     * Return a String by looking in the specified parent element
     * and pulling out the textual value stored in the given tag.
     * Return null if it was not present.
     */
    protected String optionalStringFromElement(Element e, String tag) {
        Element elt = getFirstElementByTagName(tag, e);
        return (elt == null) ? null : getTextValue(elt);
    }

    /**
     * Return a Vector of Strings by looking in the specified
     * parent element for elements with the given tag and returning
     * the text values associated with them.
     * Return null if none were found.
     */
    protected Vector optionalStringsFromElement(Element e, String tag) {
        NodeList elts = e.getElementsByTagName(tag);
        if (elts == null) {
            return null;
        }

        Vector v = new Vector();
        int len_ = elts.getLength();
        for (int i = 0; i < len_; i++) {
            v.add(getTextValue((Element)elts.item(i)));
        }
        return v;
    }

    /**
     * Return a String by looking in the specified parent element
     * and pulling out the textual value stored in the given tag.
     * According to the DTD the element must be there, although
     * the text value could be empty.
     */
    protected String stringFromElement(Element e, String tag) {
        String s = optionalStringFromElement(e, tag);

        // Do the check anyway just because it feels right
        if (s == null) {
            missingElementError(tag);
        }
        return s;
    }

    /**
     * Return a Vector of Strings by looking in the specified
     * parent element for elements with the given tag and returning
     * the text values associated with them.
     * According to the DTD at least one element must be there,
     * although the text value could be empty.
     */
    protected Vector stringsFromElement(Element e, String tag) {
        Vector v = optionalStringsFromElement(e, tag);

        // Do the check anyway just because it feels right
        if (v == null) {
            missingElementError(tag);
        }
        return v;
    }

    /**
     * Return the data from this instance as a DOM element.
     *
     * @param doc a Document instance used to create elements
     */
    public abstract Element toElement(Document doc);

    public static void trace(String s) {
        System.out.println(s);
    }
}