// Copyright (c) 1998, 2007, Oracle. All rights reserved.  
package oracle.toplink.tools.workbench;

import java.io.*;
import oracle.toplink.exceptions.*;
import oracle.toplink.ox.*;
import oracle.toplink.sessions.*;

/**
 * <p><b>Purpose</b>: Write the deployment XML (object persistence meta-data) for the TopLink project instance to a file.
 * Note the TopLink deployment XML format has change as of the OracleAS TopLink 10<i>g</i> (10.0.3) release to use XML schema.
 *
 * @since TopLink 3.0
 * @author James Sutherland
 */
public class XMLProjectWriter {

    /**
     * Default constructor.
     */
    public XMLProjectWriter() {
        super();
    }

    /**
     * PUBLIC:
     * Given the file name (including path), and a project,
     * this writes out the deployment XML file storing the project's descriptor and mapping information.
     *
     * @param fileName file to write to (including path)
     * @param project the project instance to write
     */
    public static void write(Project project, String fileName) {
        Writer writer;
        try {
            FileOutputStream stream = new FileOutputStream(fileName);
            writer = new OutputStreamWriter(stream, "UTF-8");
            write(project, writer);
            writer.close();
        } catch (IOException exception) {
            throw ValidationException.fileError(exception);
        }
    }

    /**
     * PUBLIC:
     * Given the writer, and a project,
     * this writes out the deployment XML file storing the project's descriptor and mapping information.
     *
     * @param writer writer to writer to
     * @param project the project instance to write
     */
    public static void write(Project project, Writer writer) {
        XMLContext context = new XMLContext(new ObjectPersistenceWorkbenchXMLProject());
        context.getSession(project).getEventManager().addListener(new MissingDescriptorListener());
        XMLMarshaller marshaller = context.createMarshaller();
        marshaller.marshal(project, writer);
        try {
            writer.flush();
        } catch (IOException exception) {
            throw ValidationException.fileError(exception);
        }
    }

    /**
     * INTERNAL:
     * This should not be used.
     * @see write(Project, String)
     */
    public static void write(String fileName, Project project) {
        write(project, fileName);
    }

    /**
     * INTERNAL:
     * This should not be used.
     * @see write(Project, Writer)
     * @see write(Project, String)
     */
    public static void write(String fileName, Project project, Writer writer) {
        if (writer != null) {
            write(project, writer);
        } else {
            write(project, fileName);
        }
    }
}