// Copyright (c) 1998, 2007, Oracle. All rights reserved.  
package oracle.toplink.ox.mappings.converters;

import oracle.toplink.mappings.converters.Converter;
import oracle.toplink.ox.XMLMarshaller;
import oracle.toplink.ox.XMLUnmarshaller;
import oracle.toplink.sessions.Session;


/**
 *  @version $Header: XMLConverter.java 09-aug-2007.15:24:25 dmccann Exp $
 *  @author  mmacivor
 *  @since   release specific (what release of product did this appear in)
 */

public interface XMLConverter extends Converter {
    Object convertObjectValueToDataValue(Object objectValue, Session session, XMLMarshaller marshaller);
    Object convertDataValueToObjectValue(Object dataValue, Session session, XMLUnmarshaller unmarshaller);

}