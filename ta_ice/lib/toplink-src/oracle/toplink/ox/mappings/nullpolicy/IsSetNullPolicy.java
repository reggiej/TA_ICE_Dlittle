// Copyright (c) 1998, 2007, Oracle. All rights reserved.  
package oracle.toplink.ox.mappings.nullpolicy;

import java.lang.reflect.Method;

import oracle.toplink.internal.ox.NillableNodeValue;
import oracle.toplink.internal.ox.NodeValue;
import oracle.toplink.internal.ox.NullCapableValue;
import oracle.toplink.internal.ox.OptionalNodeValue;
import oracle.toplink.internal.ox.XPathFragment;
import oracle.toplink.internal.ox.XPathNode;
import oracle.toplink.internal.security.PrivilegedGetMethod;
import oracle.toplink.internal.security.PrivilegedMethodInvoker;
import oracle.toplink.ox.NamespaceResolver;
import oracle.toplink.ox.XMLConstants;
import oracle.toplink.ox.XMLField;
import oracle.toplink.ox.record.MarshalRecord;
import oracle.toplink.ox.record.XMLRecord;
import oracle.toplink.sessions.Session;


/**
 * PUBLIC:
 * <b>Description</b>: 
 * This null policy allows for various configurations of isSet behavior to be set.<br>
 * Marshal:<br>
 * The boolean value of the isSet() state of a node will determine whether a node will be written out
 * for a null value.
 * Unmarshal:<br>
 *  
 * <p><b>The following instance fields can be set</b>:<ul>
 * <li>isSetMethodName: </li>
 * <li>isSetParameterTypes: </li>
 * <li>isSetParameters: </li>
 * </ul>
 * <p>
 *&nbsp;<b>Usage</b>:<br>
 * <ul>
 * <li> Set to a non-null value</li><br/>IsSet==true, value=value
 * <li> Not set</li><br/>isSet=false, value=null
 * <li> Set to null value </li><br/>isSet=true, value=null
 * <li> Set to default value </li><br/>isSet=false, value=default
 * </ul>
 * 
 * @see oracle.toplink.internal.ox.NullCapableValue
 * @since Oracle TopLink 11<i>g</i> Release 1 (11.1.1)
 */
public class IsSetNullPolicy extends AbstractNullPolicy {
    private static final Class[] PARAMETER_TYPES = {};
    private static final Object[] PARAMETERS = {};
    private String isSetMethodName;
    private Class[] isSetParameterTypes = PARAMETER_TYPES;
    private Object[] isSetParameters = PARAMETERS;

    /**
     * Default Constructor
     * Set the IsSetPerformedForAbsentNode to false to enable the other 2 flags
     * isNullRepresentedByEmptyNode and isNullRepresentedByXsiNil 
     */
    public IsSetNullPolicy() {
    	super();
    	isSetPerformedForAbsentNode = false;	
    }

    /**
     * Specific Constructor to set the name for checking the isSet state of the mapping
     * @param anIsSetMethodName
     */
    public IsSetNullPolicy(String anIsSetMethodName) {
    	this();
    	setIsSetMethodName(anIsSetMethodName);	
    }

    /**
     * Specific Constructor to set both the Marshal enum and the Unmarshal flags.
     * @param anIsSetMethodName
     * @param bIsNullRepresentedByEmptyNode
     * @param bIsNullRepresentedByXsiNil
     * @param aMarshalNullRepresentation
     */
    public IsSetNullPolicy(String anIsSetMethodName, //
    		boolean bIsNullRepresentedByEmptyNode, boolean bIsNullRepresentedByXsiNil, //
    		XMLNullRepresentationType aMarshalNullRepresentation) {
    	this(anIsSetMethodName);
		setNullRepresentedByEmptyNode(bIsNullRepresentedByEmptyNode);
		setNullRepresentedByXsiNil(bIsNullRepresentedByXsiNil);
		setMarshalNullRepresentation(aMarshalNullRepresentation);		
    }

    public boolean directMarshal(XPathFragment xPathFragment, MarshalRecord marshalRecord, //
    		Object object, Session session, NamespaceResolver namespaceResolver) {
    	// Do nothing when the value is not set or we are marshaling as ABSENT_NODE (optional) 
        if(!isSet(object)) {
            return false;
        } else {
        	return super.directMarshal(xPathFragment, marshalRecord, object, session, namespaceResolver);
        }
    }
    
    public boolean compositeObjectMarshal(XPathFragment xPathFragment, MarshalRecord marshalRecord, //
    		Object object, Session session, NamespaceResolver namespaceResolver) {
    	// Do nothing when the value is not set or we are marshaling as ABSENT_NODE (optional)    	
        if(!isSet(object)) {
            return false;
        } else {
        	return super.compositeObjectMarshal(xPathFragment, marshalRecord, object, session, namespaceResolver);
        }
    }
    
    public boolean compositeObjectMarshal(XMLRecord record, Object object, XMLField field) {
		if(!isSet(object)) {
			return false;
		} else {
			return super.compositeObjectMarshal(record, object, field);
		}
    }

    public void xPathNode(XPathNode xPathNode, NullCapableValue nullCapableValue) {
    	// isset optional only    	
    	if(!(isNullRepresentedByXsiNil() || getMarshalNullRepresentation().equals(XMLNullRepresentationType.XSI_NIL))) {
    		if(xPathNode.getXPathFragment().isAttribute()) {
    			return;
    		}
    	}
    	
    	// get the parent above the text() node    	
        XPathNode parentNode = xPathNode.getParent();
        
        // isset nillable only        
    	if(isNullRepresentedByXsiNil() || getMarshalNullRepresentation().equals(XMLNullRepresentationType.XSI_NIL)) {
    		XPathFragment xPathFragment = new XPathFragment();
    		xPathFragment.setXPath('@' + XMLConstants.SCHEMA_NIL_ATTRIBUTE);
    		xPathFragment.setNamespaceURI(XMLConstants.SCHEMA_INSTANCE_URL);
        	NodeValue aNodeValue = new NillableNodeValue(nullCapableValue);
            parentNode.addChild(xPathFragment, aNodeValue, null);        	
        } else {
        	NodeValue aNodeValue = new OptionalNodeValue(nullCapableValue);
            parentNode.setNodeValue(aNodeValue);        
        }
    }

    /**
     * INTERNAL:
     * Indicates if a null value has been set or not.
     * @param object
     * @return boolean (isSet status)
     */
    private boolean isSet(Object object) {
        Boolean isSet;
        try {
            Class objectClass = object.getClass();
            PrivilegedGetMethod privilegedGetMethod = new PrivilegedGetMethod(objectClass, getIsSetMethodName(), getIsSetParameterTypes(), false);
            Method isSetMethod = (Method) privilegedGetMethod.run();
            PrivilegedMethodInvoker privilegedMethodInvoker = new PrivilegedMethodInvoker(isSetMethod, object, isSetParameters); 
            isSet = (Boolean) privilegedMethodInvoker.run();
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
        return isSet.booleanValue();
    }

    /**
     * 
     * @return 
     */
    public String getIsSetMethodName() {
        return isSetMethodName;
    }

    /**
     * 
     * @param isSetMethodName
     */
    public void setIsSetMethodName(String anIsSetMethodName) {
        isSetMethodName = anIsSetMethodName;
    }

    /**
     * 
     * @return 
     */
    public Class[] getIsSetParameterTypes() {
        return isSetParameterTypes;
    }

    /**
     * 
     * @param parameterTypes
     */
    public void setIsSetParameterTypes(Class[] parameterTypes) {
        isSetParameterTypes = parameterTypes;
    }

    /**
     * 
     * @return
     */
    public Object[] getIsSetParameters() {
        return isSetParameters;
    }

    /**
     * 
     * @param parameters
     */
    public void setIsSetParameters(Object[] parameters) {
        isSetParameters = parameters;
    }
    
}
