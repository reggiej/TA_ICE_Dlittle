// Copyright (c) 2006, Oracle. All rights reserved.  
package oracle.toplink.platform.database.converters;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Struct;

/**
 * PUBLIC:
 * A StuctConverter can be added to a DatabasePlatform
 * It allows custom processing dealing java.sql.Struct types
 * When added to the DatabasePlatform:
 * 1. convertToObject(Struct) will be invoked
 * when a Struct named by the value returned by getStructName() is returned from the database.  This
 * conversion happens immediately after the object is returned from the database
 * 2. convertToStruct(Object, Connection) will be invoked when an Object of the class
 * returned by getJavaType() is written to the database.  This conversion happens immediately before the
 * object is written to the database
 * 
 * Note: Many Structs can also be mapped with mappings StructureMapping and ObjectRelationalDescriptor
 * This class provides support for advanced types of Structures that require some processing immediately 
 * before writing to the database or immediately after reading from the database.
 * 
 * @see oracle.toplink.objectrelational.StructureMapping
 * @see oracle.toplink.objectrelational.ObjectRelationalDescriptor
 */
public interface StructConverter {
    
    /**
     * PUBLIC:
     * @return The value return by getSQLTypeName() called when called on the appropriate Struct
     */
    public String getStructName();

    /**
     * PUBLIC:
     * @return The Java Class to perform conversions on
     */
    public Class getJavaType();

    /**
     * PUBLIC:
     * This method will be invoked internally when reading a Struct from the database
     * Implementers should put any custom conversion logic in this method
     * @param struct the Struct that will be read
     * @return
     * @throws SQLException
     */
    public Object convertToObject(Struct struct) throws SQLException;

    /**
     * PUBLIC:
     * This method will be invoked internally when writing an Object to the database
     * Implementers should put any custom conversion logic in this method
     * @param struct The Object to convert
     * @param connection The JDBC connection
     * @return
     * @throws SQLException
     */
    public Struct convertToStruct(Object struct, 
                           Connection connection) throws SQLException;
}