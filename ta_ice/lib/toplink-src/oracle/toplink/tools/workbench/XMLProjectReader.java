// Copyright (c) 1998, 2007, Oracle. All rights reserved.  
package oracle.toplink.tools.workbench;

import java.io.*;
import java.net.URL;
import org.w3c.dom.Document;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import oracle.toplink.exceptions.*;
import oracle.toplink.internal.helper.ConversionManager;
import oracle.toplink.platform.xml.XMLParser;
import oracle.toplink.platform.xml.XMLPlatform;
import oracle.toplink.platform.xml.XMLPlatformFactory;
import oracle.toplink.queryframework.*;
import oracle.toplink.sessions.*;
import oracle.toplink.ox.*;
import oracle.toplink.xml.XMLFileLogin;
import oracle.toplink.xml.XMLReadCall;

/**
 * <p><b>Purpose</b>: Allow for a TopLink Mapping Workbench generated deployment XML project file to be read.
 * This reader returns an instance of Project to be used to initialize a TopLink session.
 * This class supports reading the 11g 11.1.1 and 10g 10.1.3 and (currently) the 9.0.4/10.1.2 formats.
 *
 * @since TopLink 3.0
 * @author James Sutherland
 */
public class XMLProjectReader {

    /** Allow for usage of schema validation to be configurable. */
    protected static boolean shouldUseSchemaValidation = true;

    /** Cache the creation and initialization of the TopLink XML mapping project. */
    protected static ObjectPersistenceRuntimeXMLProject project;
    private static final String TOPLINK_SCHEMA = "xsd/toplink-object-persistence_11_1_1.xsd";

    /**
     * PUBLIC:
     * Return if schema validation will be used when parsing the deployment XML.
     */
    public static boolean shouldUseSchemaValidation() {
        return shouldUseSchemaValidation;
    }

    /**
     * PUBLIC:
     * Set if schema validation will be used when parsing the deployment XML.
     * By default schema validation is on, but can be turned off if validation problems occur,
     * or to improve parsing performance.
     */
    public static void setShouldUseSchemaValidation(boolean value) {
        shouldUseSchemaValidation = value;
    }

    public XMLProjectReader() {
        super();
    }

    /**
     * PUBLIC:
     * Read the TopLink project deployment XML from the file or resource name.
     * If a resource name is used the default class loader will be used to resolve the resource.
     * Note the default class loader must be able to resolve the domain classes.
     * Note the file must be the deployment XML, not the Mapping Workbench project file.
     */
    public static Project read(String fileOrResourceName) {
        return read(fileOrResourceName, null);
    }

    /**
     * INTERNAL:
     * This should not be used.
     * @deprecated
     * @see read(String, ClassLoader)
     */
    public static Project readWithClassLoader(String fileOrResourceName, ClassLoader classLoader) {
        return read(fileOrResourceName, classLoader);
    }

    /**
     * PUBLIC:
     * Read the TopLink project deployment XML from the reader on the file.
     * Note the class loader must be able to resolve the domain classes.
     * Note the file must be the deployment XML, not the Mapping Workbench project file.
     * This API supports the 10g (9.0.4) (currently), 10g (10.0.3), 11g (11.1.1) formats.
     */
    public static Project read(Reader reader, ClassLoader classLoader) {
        // Since a reader is pass and it can only be streamed once (mark does not work)
        // It must be first read into a buffer so multiple readers can be uesed to
        // determine the format.  This does not effect performance severly.
        StringWriter writer;
        Document document;
        if (project == null) {
            project = new ObjectPersistenceRuntimeXMLProject_11_1_1();
        }
        try {
            writer = new StringWriter(4096);
            char[] c = new char[4096];
            int r = 0;
            while ((r = reader.read(c)) != -1) {
                writer.write(c, 0, r);
            }

            XMLLogin xmlLogin = new XMLLogin();
            xmlLogin.setDatasourcePlatform(new oracle.toplink.ox.platform.DOMPlatform());
            project.setDatasourceLogin(xmlLogin);

            // Create the OPM prooject.
            if (classLoader != null) {
                project.getDatasourceLogin().getDatasourcePlatform().getConversionManager().setLoader(classLoader);
            }

            // Assume the format is OPM parse the document with OPM validation on.
            XMLPlatform xmlPlatform = XMLPlatformFactory.getInstance().getXMLPlatform();
            XMLParser parser = xmlPlatform.newXMLParser();
            parser.setNamespaceAware(true);
            parser.setWhitespacePreserving(false);
            if (shouldUseSchemaValidation()) {
                parser.setValidationMode(XMLParser.SCHEMA_VALIDATION);
                XMLDescriptor projectDescriptor = (XMLDescriptor)project.getDescriptors().get(Project.class);

                // Workaround for bug #3503583.
                XMLSchemaResolver xmlSchemaResolver = new XMLSchemaResolver();
                URL toplinkSchemaURL = xmlSchemaResolver.resolveURL(TOPLINK_SCHEMA);
                parser.setEntityResolver(xmlSchemaResolver);
                parser.setXMLSchema(toplinkSchemaURL);
            }
            try {
                document = parser.parse(new StringReader(writer.toString()));
            } catch (Exception parseException) {
                // If the parse validation fails, it may be because the format was 904.
                try {
                    parser = xmlPlatform.newXMLParser();
                    parser.setNamespaceAware(false);
                    parser.setWhitespacePreserving(false);
                    document = parser.parse(new StringReader(writer.toString()));
                } catch (Exception exception904) {
                    // Assume was in OPM format, just not valid, through original exception.
                    throw parseException;
                }

                // If not in 904 format was invalid OPM, throw old exception.
                if (!document.getDocumentElement().getTagName().equals("project")) {
                    String version = document.getDocumentElement().getAttribute("version");
                    // If 10.1.3 format use old format read.
                    if ((version == null) || (version.indexOf("10.1.3") == -1)) {
                        throw parseException;
                    }
                }
            }
        } catch (Exception exception) {
            throw XMLMarshalException.unmarshalException(exception);
        }

        // If 9.0.4 format use old format read.
        if (document.getDocumentElement().getTagName().equals("project")) {
            return read904Format(new StringReader(writer.toString()), classLoader);
        }
        
        String version = document.getDocumentElement().getAttribute("version");
        // If 10.1.3 format use old format read.
        if ((version != null) && (version.indexOf("10.1.3") != -1)) {
            return read1013Format(document, classLoader);
        }

        // Marshall OPM format.
        XMLContext context = new XMLContext(project);
        context.getSession(Project.class).getEventManager().addListener(new MissingDescriptorListener());
        XMLUnmarshaller unmarshaller = context.createUnmarshaller();
        Project project = (Project)unmarshaller.unmarshal(document);

        // Set the project's class loader.
        if ((classLoader != null) && (project.getDatasourceLogin() != null)) {
            project.getDatasourceLogin().getDatasourcePlatform().getConversionManager().setLoader(classLoader);
        }
        return project;
    }

    /**
     * PUBLIC:
     * Read the TopLink project deployment XML from the file or resource name.
     * If a resource name is used the class loader will be used to resolve the resource.
     * Note the class loader must be able to resolve the domain classes.
     * Note the file must be the deployment XML, not the Mapping Workbench project file.
     */
    public static Project read(String fileOrResourceName, ClassLoader classLoader) {
        if (fileOrResourceName.toLowerCase().indexOf(".mwp") != -1) {
            throw ValidationException.invalidFileName(fileOrResourceName);
        }
        InputStream fileStream = null;
        if (classLoader == null) {
            fileStream = (new ConversionManager()).getLoader().getResourceAsStream(fileOrResourceName);
        } else {
            fileStream = classLoader.getResourceAsStream(fileOrResourceName);
        }
        if (fileStream == null) {
            File file = new File(fileOrResourceName);
            if (!file.exists()) {
                throw ValidationException.projectXMLNotFound(fileOrResourceName, null);
            }
            try {
                fileStream = new FileInputStream(fileOrResourceName);
            } catch (FileNotFoundException exception) {
                throw ValidationException.projectXMLNotFound(fileOrResourceName, exception);
            }
        }

        InputStreamReader reader = null;
        try {
            try {
                // Bug2631348  Only UTF-8 is supported
                reader = new InputStreamReader(fileStream, "UTF-8");
            } catch (UnsupportedEncodingException exception) {
                throw ValidationException.fatalErrorOccurred(exception);
            }

            Project project = read(reader, classLoader);
            return project;
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException exception) {
                throw ValidationException.fileError(exception);
            }
        }
    }

    /**
     * INTERNAL:
     * Read the TopLink 9.0.4 deployment XML format.
     */
    public static Project read904Format(Reader reader, ClassLoader classLoader) {
        XMLFileLogin login = new XMLFileLogin();
        if (classLoader != null) {
            login.getDatasourcePlatform().getConversionManager().setLoader(classLoader);
        }
        DescriptorXMLProject project = new DescriptorXMLProject();
        project.setLogin(login);

        DatabaseSession session = project.createDatabaseSession();
        session.dontLogMessages();
        session.getEventManager().addListener(new MissingDescriptorListener());
        session.login();

        XMLReadCall call = new XMLReadCall();
        call.setReader(reader);
        ReadObjectQuery query = new ReadObjectQuery(Project.class, call);

        return (Project)session.executeQuery(query);
    }
    
    /**
     * INTERNAL:
     * Read the TopLink 10.1.3 deployment XML format.
     */
    public static Project read1013Format(Document document, ClassLoader classLoader) {
        Project opmProject = new ObjectPersistenceRuntimeXMLProject();
        XMLLogin xmlLogin = new XMLLogin();
        xmlLogin.setDatasourcePlatform(new oracle.toplink.ox.platform.DOMPlatform());
        opmProject.setDatasourceLogin(xmlLogin);

        // Create the OPM prooject.
        if (classLoader != null) {
            opmProject.getDatasourceLogin().getDatasourcePlatform().getConversionManager().setLoader(classLoader);
        }
        // Marshall OPM format.
        XMLContext context = new XMLContext(opmProject);
        context.getSession(Project.class).getEventManager().addListener(new MissingDescriptorListener());
        XMLUnmarshaller unmarshaller = context.createUnmarshaller();
        Project project = (Project)unmarshaller.unmarshal(document);

        // Set the project's class loader.
        if ((classLoader != null) && (project.getDatasourceLogin() != null)) {
            project.getDatasourceLogin().getDatasourcePlatform().getConversionManager().setLoader(classLoader);
        }
        return project;
    }

    /**
     * PUBLIC:
     * Read the TopLink project deployment XML from the reader on the file.
     * Note the default class loader must be able to resolve the domain classes.
     * Note the file must be the deployment XML, not the Mapping Workbench project file.
     */
    public static Project read(Reader reader) {
        return read(reader, null);
    }

    /**
     * INTERNAL:
     * Workaround for bug #3503583.
     * This works around a bug in the xdk in resolving relative jar based xsd references in oc4j.
     */
    private static class XMLSchemaResolver implements EntityResolver {
        private static final String SCHEMA_DIR = "xsd/";
        private static final String OPM_SCHEMA = "object-persistence_1_0.xsd";

        /**
         * INTERNAL:
         */
        public XMLSchemaResolver() {
            super();
        }

        /**
         * INTERNAL:
         * Resolve the XSD.
         */
        public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
            if (OPM_SCHEMA.equals(systemId)) {
                URL url = resolveURL(SCHEMA_DIR + OPM_SCHEMA);
                if (null == url) {
                    return null;
                }
                return new InputSource(url.openStream());
            }
            return null;
        }

        /**
         * INTERNAL:
         * Return the URL for the resource.
         */
        public URL resolveURL(String resource) {
            // The xsd is always in the toplink.jar, use our class loader.
            return getClass().getClassLoader().getResource(resource);
        }
    }
}
