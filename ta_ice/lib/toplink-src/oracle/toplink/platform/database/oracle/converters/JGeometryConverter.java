// Copyright (c) 2006, Oracle. All rights reserved.  
package oracle.toplink.platform.database.oracle.converters;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Struct;

import oracle.spatial.geometry.JGeometry;
import oracle.sql.STRUCT;
import oracle.toplink.platform.database.converters.StructConverter;

/**
 * PUBLIC:
 * A StructConverter that can be used to convert the oracle.spatial.geometry.JGeometry as
 * it is read and written from the database.  To use this StructConverter, it must be added
 * to the DatabasePlatform either with the addStructConverter(StructConverter) method or specified in
 * sessions.xml.  It requires that the oracle.spatial.geometry.JGeometry type is available on
 * the Classpath
 */
public class JGeometryConverter implements StructConverter {
    public static final String JGEOMETRY_DB_TYPE = "MDSYS.SDO_GEOMETRY";
    public static final Class JGEOMETRY_CLASS = JGeometry.class;

    public String getStructName() {
        return JGEOMETRY_DB_TYPE;
    }

    public Class getJavaType() {
        return JGEOMETRY_CLASS;
    }

    public Object convertToObject(Struct struct) throws SQLException {
        if (struct == null){
            return null;
        }
        return JGeometry.load((STRUCT)struct);
    }

    public Struct convertToStruct(Object geometry, Connection connection) throws SQLException {
        if (geometry == null){
            return null;
        }
        return JGeometry.store((JGeometry)geometry, connection);
    }
}
