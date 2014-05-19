// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.tools.sessionconfiguration;

import java.io.*;
import oracle.toplink.ox.XMLContext;
import oracle.toplink.ox.XMLMarshaller;
import oracle.toplink.exceptions.ValidationException;
import oracle.toplink.tools.sessionconfiguration.model.TopLinkSessions;

/**
 * INTERNAL:
 * This class is used by the Mapping Workbench Session Configuration to write the session config
 * to XML.
 */
public class XMLSessionConfigWriter {
    public XMLSessionConfigWriter() {
        super();
    }

    /**
     * Given the file name (including path), and a TopLinkSessions,
     * this writes out the session XML file.
     *
     * @param fileName file to write to (including path)
     * @param topLinkSessions the TopLinkSessions instance to write
     */
    public static void write(TopLinkSessions toplinkSessions, String fileName) {
        Writer writer;
        try {
			//Bug#4305370 Needs to be utf-8 encoded.
	    	writer = new OutputStreamWriter(new FileOutputStream(fileName), "UTF-8");
            write(toplinkSessions, writer);
            writer.close();
        } catch (IOException exception) {
            throw ValidationException.fileError(exception);
        }
    }

    /**
     * Given the writer, and a TopLinkSessions,
     * this writes out the session XML file.
     *
     * @param writer writer to writer to
     * @param topLinkSessions the TopLinkSessions instance to write
     */
    public static void write(TopLinkSessions toplinkSessions, Writer writer) {
        XMLContext context = new XMLContext(new XMLSessionConfigProject_11_1_1());
        XMLMarshaller marshaller = context.createMarshaller();

        // this is throwing a null pointer exception right now, bug entered
        //marshaller.setNoNamespaceSchemaLocation("sessions_11_1_1.xsd");
        marshaller.marshal(toplinkSessions, writer);

        try {
            writer.flush();
        } catch (IOException exception) {
            throw ValidationException.fileError(exception);
        }
    }
}