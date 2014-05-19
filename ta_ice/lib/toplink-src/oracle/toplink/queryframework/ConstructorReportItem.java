// Copyright (c) 1998, 2007, Oracle. All rights reserved.  
package oracle.toplink.queryframework;

import java.util.ArrayList;
import java.util.List;
import java.lang.reflect.Constructor;
import java.security.AccessController;
import java.security.PrivilegedActionException;

import oracle.toplink.exceptions.QueryException;
import oracle.toplink.expressions.Expression;
import oracle.toplink.internal.helper.ClassConstants;
import oracle.toplink.internal.queryframework.ReportItem;
import oracle.toplink.internal.security.PrivilegedAccessHelper;
import oracle.toplink.internal.security.PrivilegedGetConstructorFor;
import oracle.toplink.mappings.DatabaseMapping;


/**
 * <b>Purpose</b>: An item specifying a class constructor method to be used in a ReportQuery's returned results.  
 * <p>Example:
 * <blockquote><pre>
 *      ConstructorReportItem item = new ConstructorReportItem("Employee");
 *      item.setResultType(Employee.class);
 *      item.addAttribute("firstName", employees.get("firstName"));
 *      query.addConstructorReportItem(item);
 * </blockquote></pre>
 * <p>
 * When executed will return a collection of ReportQueryResults that contain Employee objects created using
 * the new Employee(firstname) constructor. 
 *
 * @author Chris Delahunt
 * @since TopLink Essentials 1.0
 */
public class ConstructorReportItem extends ReportItem  {

    protected Class[] constructorArgTypes;
    protected List constructorMappings;
    protected List reportItems;
    protected Constructor constructor;
    
    /**
     * Create a new constructor item.
     */
    public ConstructorReportItem() {
    }
    
    /**
     * Create a new constructor item.
     * @param name string used to look up this result in the ReportQueryResult.
     */
    public ConstructorReportItem(String name) {
        super(name, null);
    }
    
    /**
     * Method to add an expression to be used to return the parameter that is then passed into the constructor method.
     * Similar to ReportQuery's addAttribute method, but name is not needed.
     */
    public void addAttribute(Expression attributeExpression) {
        ReportItem item = new ReportItem(getName()+getReportItems().size(), attributeExpression);
        getReportItems().add(item);
    }
    
    /**
     * Add the attribute with joining.
     */
    public void addAttribute(String attributeName, Expression attributeExpression, List joinedExpressions) {
        ReportItem item = new ReportItem(attributeName, attributeExpression);
        item.getJoinedAttributeManager().setJoinedAttributeExpressions_(joinedExpressions);
        getReportItems().add(item);
    }
    
    public void addItem(ReportItem item) {
        getReportItems().add(item);
    }
    
    public Class[] getConstructorArgTypes(){
        return constructorArgTypes;
    }

    /**
     * INTERNAL:
     * Return the mappings for the items.
     */
    public List getConstructorMappings(){
        return constructorMappings;
    }

    /**
     * INTERNAL:
     * Return the constructor.
     */
    public Constructor getConstructor(){
        return constructor;
    }

    /**
     * INTERNAL:
     * Set the constructor.
     */
    public void setConstructor(Constructor constructor){
        this.constructor = constructor;
    }

    public List getReportItems(){
        if (reportItems == null) {
            reportItems = new ArrayList();
        }
        return reportItems;
    }

    /**
     * INTERNAL:
     * Looks up mapping for attribute during preExecute of ReportQuery
     */
    public void initialize(ReportQuery query) throws QueryException {
        int size= getReportItems().size();
        List mappings = new ArrayList();
        for (int index = 0; index < size; index++) {
            ReportItem item = (ReportItem)reportItems.get(index);
            item.initialize(query);
            mappings.add(item.getMapping());
        }
        setConstructorMappings(mappings);
        
        int numberOfItems = getReportItems().size();
        // Arguments may be initialized depending on how the query was constructed, so types may be undefined though.
        if (getConstructorArgTypes() == null) {
            setConstructorArgTypes(new Class[numberOfItems]);
        }
        Class[] constructorArgTypes = getConstructorArgTypes();
        for (int index = 0; index < numberOfItems; index++) {
            if (constructorArgTypes[index] == null) {
                ReportItem argumentItem = (ReportItem)getReportItems().get(index);
                if (mappings.get(index) != null) {
                    constructorArgTypes[index] = ((DatabaseMapping)constructorMappings.get(index)).getAttributeClassification();
                } else if (argumentItem.getResultType() != null) {
                    constructorArgTypes[index] = argumentItem.getResultType();
                } else if (argumentItem.getDescriptor() != null) {
                    constructorArgTypes[index] = argumentItem.getDescriptor().getJavaClass();
                } else {
                    // Use Object.class by default.
                    constructorArgTypes[index] = ClassConstants.OBJECT;
                }
            }
        }
        try {
            Constructor constructor = null;
            if (PrivilegedAccessHelper.shouldUsePrivilegedAccess()){
                try {
                    constructor = (Constructor)AccessController.doPrivileged(new PrivilegedGetConstructorFor(getResultType(), constructorArgTypes, true));
                } catch (PrivilegedActionException exception) {
                    throw QueryException.exceptionWhileUsingConstructorExpression(exception.getException(), query);
                }
            } else {
                constructor = PrivilegedAccessHelper.getConstructorFor(getResultType(), constructorArgTypes, true);
            }
            setConstructor(constructor);
        } catch (NoSuchMethodException exception) {
            throw QueryException.exceptionWhileUsingConstructorExpression(exception, query);
        }
    }
    
    public boolean isConstructorItem(){
        return true;
    }

    public void setConstructorArgTypes(Class[] constructorArgTypes){
        this.constructorArgTypes = constructorArgTypes;
    }
    
    /**
     * INTERNAL:
     * Return the mappings for the items.
     */
    public void setConstructorMappings(List constructorMappings){
        this.constructorMappings = constructorMappings;
    }
    
    public void setReportItems(List reportItems){
        this.reportItems = reportItems;
    }
    
    public String toString() {
        String string = "ConstructorReportItem(" + getName() + " -> [";
        //don't use getReportItems to avoid creating collection.  
        if (reportItems!=null){
            int size=reportItems.size();
            for(int i=0;i<size;i++){
                string =string + reportItems.get(i).toString();
            }
        }
        return string +"])";
    }
    
}
