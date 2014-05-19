// Copyright (c) 1998, 2007, Oracle. All rights reserved.
package oracle.toplink.ox.mappings.nullpolicy;

/**
 * <b>Description</b>: 
 * An enum that is used within a Node Null Policy to determine what to marshal for a null node.<br>
 * We define 3 final instances available to the user (XSI_NIL, ABSENT_NODE(default) and EMPTY_NODE.
 * <p>
 * <p><table border="1">
 * <tr>
 * <th id="c1" align="left">Flag</th>
 * <th id="c2" align="left">Description</th>
 * </tr>
 * <tr>
 * <td headers="c1"> XSI_NIL </td>
 * <td headers="c2">Nillable: Write out an xsi:nil="true" attribute.</td>
 * </tr>
 * <tr>
 * <td headers="c1"> ABSENT_NODE(default) </td>
 * <td headers="c2">Optional: Write out no node.</td>
 * </tr>
 * <tr>
 * <td headers="c1" nowrap="true"> EMPTY_NODE </td>
 * <td headers="c2">Required: Write out an empty <node/> or node="" node.</td>
 * </tr>
 * </table>
 * @see oracle.toplink.ox.mappings.nullpolicy.AbstractNullPolicy
 */
public enum XMLNullRepresentationType {
	
	/**
	 * Write out an xsi:nil="true" attribute. Nillable policy behavior.
	 */
	XSI_NIL,
	
	/**
	 * Do not write out anything (default optional policy behavior). 
	 */
	ABSENT_NODE,
	
	/**
	 * Write out an empty node. Required policy behavior
	 */
	EMPTY_NODE	
	}