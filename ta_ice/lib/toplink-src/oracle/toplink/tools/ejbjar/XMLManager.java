// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.tools.ejbjar;

import java.io.*;
import java.util.Enumeration;
import java.util.jar.*;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import org.w3c.dom.*;
import oracle.toplink.exceptions.EJBJarXMLException;
import oracle.toplink.internal.localization.ExceptionLocalization;
import oracle.toplink.platform.xml.XMLPlatform;
import oracle.toplink.platform.xml.XMLPlatformException;
import oracle.toplink.platform.xml.XMLPlatformFactory;

/**
 * INTERNAL:
 * XMLManager manages
 * <OL>
 * <LI> Reading and writing of ejb-jar.xml
 * <LI> Converting ejb-jar.xml to a DOM Document object
 * </OL>
 */
public class XMLManager implements EjbJarConstants {
    public final static String EJB_JAR_FILE_NAME = "ejb-jar.xml";
    public static String EJB_JAR_2_0_DTD_FILE_NAME = "ejb-jar_2_0.dtd";
    public static String EJB_JAR_1_1_DTD_FILE_NAME = "ejb-jar_1_1.dtd";
    public static String EJB_JAR_DTD_LOCAL_DIR_PATH = "/dtd/";
    public static String EJB_JAR_DTD_2_0_LOCAL_RESOURCE = EJB_JAR_DTD_LOCAL_DIR_PATH + EJB_JAR_2_0_DTD_FILE_NAME;
    public final static String EJB_JAR_DOC_NAME = "ejb-jar";
    public final static String EJB_JAR_2_0_DOCTYPE_DESC = "-//Sun Microsystems, Inc.//DTD Enterprise JavaBeans 2.0//EN";
    public final static String EJB_JAR_1_1_DOCTYPE_DESC = "-//Sun Microsystems, Inc.//DTD Enterprise JavaBeans 1.1//EN";
    public final static String EJB_JAR_2_0_DTD_URL = "http://java.sun.com/dtd/ejb-jar_2_0.dtd";
    public final static String EJB_JAR_1_1_DTD_URL = "http://java.sun.com/j2ee/dtds/ejb-jar_1_1.dtd";
    public final static String EJB_JAR_XML_ENTRY_NAME = "META-INF/ejb-jar.xml";

    //xml schema support
    public final static String XML_NAMESPACE = "http://java.sun.com/xml/ns/j2ee";
    public final static String XML_SCHEMA_INSTANCE = "http://www.w3.org/2001/XMLSchema-instance";
    public final static String XML_SCHEMA_LOCATION = "http://java.sun.com/xml/ns/j2ee http://java.sun.com/xml/ns/j2ee/ejb-jar_2_1.xsd";

    /**
      * Creates a DOM Document from a either a) a Jar file that contains ejb-jar.xml
      * OR b)from an ejb-jar.xml file at a given location
      * @param name it could be a) the location of the Jar file including full path OR b) the location of the ejb-jar.xml file
      * @return Document the DOM object
    * @deprecated see readEjbXmlFile(EjbJarXMLDocument ejbDocument, String fileName, int validationMode)
      */
    public static Document readEjbXmlFile(EjbJarXMLDocument ejbDocument, String fileName) throws FileNotFoundException, NullPointerException, EJBJarXMLException {
        return readEjbXmlFile(ejbDocument, fileName, XMLParser.OLD_VALIDATION);
    }

    /**
      * @param String fileName Creates a DOM Document from a either a) fileName is a Jar file that contains ejb-jar.xml
      * OR b) fileName is a file or a directory contains ejb-jar.xml
      * @param name it could be a) the location of the Jar file including full path OR b) the location of the ejb-jar.xml file
    * @param validationMode should a parser validate the document against DTD or schema. The modes are XMLParser.OLD_VALIDATION,
    * XMLParser.NONVALIDATING, XMLParser.SCHEMA_VALIDATION, and XMLParser.DTD_VALIDATION.
      * @return Document the DOM object
      */
    public static Document readEjbXmlFile(EjbJarXMLDocument ejbDocument, String fileName, int validationMode) throws FileNotFoundException, NullPointerException, EJBJarXMLException {
        Document doc = null;

        if (fileName.endsWith(".jar")) {
            doc = readEjbXmlFileFromJarFile(fileName, validationMode);
        } else {
            doc = readEjbXmlFileFromDirectory(fileName, validationMode);
        }

        ejbDocument.setDocument(doc);
        ejbDocument.setXmlFileLocation(fileName);
        return doc;
    }

    /**
      * Creates a DOM Document from an ejb-jar.xml file at a given location
      * @param dirPath the location of the ejb-jar.xml file
    * @param validationMode should a parser validate the document against DTD or schema. The modes are XMLParser.OLD_VALIDATION,
    * XMLParser.NONVALIDATING, XMLParser.SCHEMA_VALIDATION, and XMLParser.DTD_VALIDATION.
      * @return Document the DOM object
      */
    private static Document readEjbXmlFileFromDirectory(String dirPath, int validationMode) throws FileNotFoundException, NullPointerException, EJBJarXMLException {
        File file = null;
        org.xml.sax.InputSource inputSource = null;

        if (!(new File(dirPath)).exists()) {
            // check whether the path exists
            Object[] args = { dirPath };
            throw new FileNotFoundException(ExceptionLocalization.buildMessage("directory_not_exist", args));
        }

        String fileName = EJB_JAR_FILE_NAME;

        if (dirPath != null) {
            if (dirPath.endsWith(".xml")) {
                fileName = dirPath;
            } else {
                fileName = dirPath + System.getProperty("file.separator") + fileName;
            }
        }

        FileInputStream inStream = null;
        file = new File(fileName);
        Document doc = null;

        try {
            inStream = new FileInputStream(file);
            //Bug2631348  Only UTF-8 is supported
            inputSource = new org.xml.sax.InputSource(new InputStreamReader(inStream, "UTF-8"));
            doc = readEjbXmlFileFromInputSource(inputSource, validationMode);
            inStream.close();
        } catch (IOException ioe) {
            throw EJBJarXMLException.errorReadingDescriptor(ioe);
        }

        return doc;
    }

    /**
      * Creates a DOM Document from a Jar file that contains ejb-jar.xml
      * @param jarFileName the location of the Jar file including full path
    * @param validationMode should a parser validate the document against DTD or schema. The modes are XMLParser.OLD_VALIDATION,
    * XMLParser.NONVALIDATING, XMLParser.SCHEMA_VALIDATION, and XMLParser.DTD_VALIDATION.
      * @return Document the DOM object
      */
    private static Document readEjbXmlFileFromJarFile(String jarFileName, int validationMode) throws FileNotFoundException, NullPointerException, EJBJarXMLException {
        File file = null;
        org.xml.sax.InputSource inputSource = null;
        Document doc = null;
        Object[] args = { jarFileName };

        if (!(new File(jarFileName)).exists()) {
            throw new FileNotFoundException(ExceptionLocalization.buildMessage("jar_not_exist", args));
        }

        file = new File(jarFileName);
        InputStream inStream = null;

        try {
            if (file.toString().endsWith(".jar")) {
                JarFile jar = new JarFile(file);
                JarEntry entry = getEjbJarXmlEntry(jar);

                if (null != entry) {
                    inStream = jar.getInputStream(entry);
                    //Bug2631348  Only UTF-8 is supported
                    inputSource = new org.xml.sax.InputSource(new InputStreamReader(inStream, "UTF-8"));
                } else {
                    Object[] args2 = { jarFileName, EJB_JAR_XML_ENTRY_NAME };
                    throw new FileNotFoundException(ExceptionLocalization.buildMessage("may_not_contain_xml_entry", args2));

                }
            } else {
                throw new FileNotFoundException(ExceptionLocalization.buildMessage("not_jar_file", args));
            }

            doc = readEjbXmlFileFromInputSource(inputSource, validationMode);
            inStream.close();
        } catch (FileNotFoundException fnfe) {
            throw fnfe;
        } catch (IOException ioe) {
            throw EJBJarXMLException.errorReadingDescriptor(ioe);
        }

        return doc;
    }

    /**
      * Creates a DOM Document from an inputstream
      * @param inputSource
    * @param validationMode should a parser validate the document against DTD or schema. The modes are XMLParser.OLD_VALIDATION,
    * XMLParser.NONVALIDATING, XMLParser.SCHEMA_VALIDATION, and XMLParser.DTD_VALIDATION.
      * @return Document the DOM object
      */
    private static Document readEjbXmlFileFromInputSource(org.xml.sax.InputSource inputSource, int validationMode) throws FileNotFoundException, EJBJarXMLException {
        Document doc;
        try {
            doc = XMLParser.parseXML(inputSource, validationMode);
            inputSource.getCharacterStream().close();
        } catch (XMLPlatformException e) {
            throw EJBJarXMLException.errorReadingDescriptor(e);
        } catch (FileNotFoundException fnfe) {
            throw fnfe;// we want to keep this one
        } catch (IOException ioe) {
            throw EJBJarXMLException.errorReadingDescriptor(ioe);
        }

        return doc;
    }

    /**
      * @param ejbJarXMLDocument contains an EJBJar instance that is used to write the ejb-jar.xml
        * @param outFile is the full file name or directory where the ejb-jar.xml file is written
        * If the output file is the same as the last read one then the last modified time of
        * ejbJarXMLDocument is updated after the ouput file was written.
        */
    public static void writeEjbXmlFile(EjbJarXMLDocument ejbJarXmlDocument, String outFile, boolean isEjb_2_0) throws FileNotFoundException, IOException {
        writeEjbXmlFileFromEjbJar(ejbJarXmlDocument.getEjbJar(), outFile, isEjb_2_0);
        ejbJarXmlDocument.setXmlFileLocation(outFile);
    }

    /**
      * @param ejbJar that contains the ejb-jar.xml DOM
      * @param fileName which could either be a .jar or a .xml.  If the fileName is .jar then
      * the function tries to update the .jar file.  If the fileName is .xml then the function
      * will create a .xml file.  If the .xml already exists, the function will attempt to
      * overwrite it.
      */
    public static void writeEjbXmlFileFromEjbJar(EjbJar ejbJar, String fileName, boolean isEjb_2_0) throws FileNotFoundException, IOException {
        File file = null;
        if ((fileName == null) || (fileName.trim().length() == 0)) {
            Object[] args = { fileName };
            throw new FileNotFoundException(ExceptionLocalization.buildMessage("file_not_exist", args));
        }

        file = new File(fileName);
        StringBuffer buffer_ = getXML(ejbJar, isEjb_2_0);
        if (file.toString().endsWith(".jar")) {
            if (file.exists()) {
                File f = File.createTempFile("ejbjar", ".jar");

                JarFile jar = null;
                try {
                    jar = new JarFile(file);
                } catch (ZipException ze)// file is not actually a jar file yet
                 {
                    JarOutputStream out = new JarOutputStream(new FileOutputStream(file), new Manifest());
                    out.putNextEntry(new JarEntry(EJB_JAR_XML_ENTRY_NAME));
                    out.close();

                    jar = new JarFile(file);
                }

                JarOutputStream jarOut = new JarOutputStream(new FileOutputStream(f));

                // Copy old jar
                for (Enumeration entries = jar.entries(); entries.hasMoreElements();) {
                    JarEntry entry = (JarEntry)entries.nextElement();
                    if (entry.getName().equalsIgnoreCase(EJB_JAR_XML_ENTRY_NAME)) {
                        continue;
                    }
                    if (!entry.isDirectory()) {
                        InputStream in = jar.getInputStream(entry);
                        InputStream jarIn = in;
                        byte[] arr = new byte[(int)entry.getSize()];
                        jarIn.read(arr);
                        CRC32 crc = new CRC32();
                        crc.update(arr);
                        entry.setCrc(crc.getValue());
                        entry.setCompressedSize(arr.length);
                        entry.setMethod(ZipEntry.STORED);
                        jarOut.putNextEntry(entry);
                        jarOut.write(arr, 0, arr.length);
                        jarOut.flush();
                    } else {
                        jarOut.putNextEntry(entry);
                    }
                    jarOut.closeEntry();
                }
                byte[] arr = buffer_.toString().getBytes();
                JarEntry meta = new JarEntry(EJB_JAR_XML_ENTRY_NAME);
                meta.setSize(arr.length);
                meta.setCompressedSize(arr.length);
                CRC32 crc = new CRC32();
                crc.update(arr);
                meta.setCrc(crc.getValue());
                meta.setMethod(ZipEntry.STORED);
                jarOut.putNextEntry(meta);
                jarOut.write(arr, 0, arr.length);
                jarOut.closeEntry();
                jarOut.close();
                jar.close();
                boolean deleted = true;
                System.gc();
                // testFileExistence(file);
                try {
                    deleted = file.delete();
                } catch (Exception d) {
                    d.printStackTrace();
                }
                if (deleted) {
                    boolean renamed = f.renameTo(file);
                } else {
                    move(f, file, true);
                }
            } else {// the .jar file does not exist.
                JarOutputStream jarOut = new JarOutputStream(new FileOutputStream(file));

                jarOut.putNextEntry(new JarEntry(EJB_JAR_XML_ENTRY_NAME));
                byte[] arr = buffer_.toString().getBytes();
                jarOut.write(arr, 0, arr.length);
                jarOut.closeEntry();
                jarOut.close();
            }
        } else if (file.toString().endsWith(".xml")) {
            FileOutputStream fs = new FileOutputStream(file);
            DataOutputStream out = new DataOutputStream(fs);

            //Bug2631348  Only UTF-8 is supported
            out.write(buffer_.toString().getBytes("UTF-8"));
            out.close();
        }
    }

    public static JarEntry getEjbJarXmlEntry(JarFile ejbJarFile) {
        JarEntry ejbJarXmlEntry = (JarEntry)ejbJarFile.getEntry(EJB_JAR_XML_ENTRY_NAME);

        if (null == ejbJarXmlEntry) {
            ejbJarXmlEntry = (JarEntry)ejbJarFile.getEntry(EJB_JAR_XML_ENTRY_NAME.toLowerCase());
        }

        return ejbJarXmlEntry;
    }

    /**
      * @param ejbJar the ejbjar object
      * @return StringBuffer the buffer containing a String representation
      * (ejb-jar.xml contents) of the EjbJar object
      * Note that this returns PI and DOCTYPE hardoded in this method.
      * Compare this method with generateXML() which does not return
      * PI and DOCTYPE.
      */
    public static StringBuffer getXML(EjbJar ejbJar, boolean isEjb2_0) {
        final String NEWLINE = System.getProperty("line.separator");

        // Create XML document object to use to write out
        Document doc;
        if (isEjb2_0) {
            doc = documentForEjb_2_0(ejbJar);
        } else {
            doc = documentForEjb_1_1(ejbJar);
        }
        StringBuffer buf = new StringBuffer();
        buf.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        if (!ejbJar.usesXmlSchema()) {
            DocumentType docType = doc.getDoctype();
            buf.append(NEWLINE + "<!DOCTYPE " + docType.getName());
            if (docType.getPublicId() != null) {
                buf.append(" PUBLIC \"" + docType.getPublicId() + "\" ");
            } else {
                buf.append(" SYSTEM ");
            }
            buf.append("\"" + docType.getSystemId() + "\">");
            buf.append(NEWLINE);
        }

        //BUG 2790307: method not found
        buf.append((XMLManager.generateXML(doc.getDocumentElement(), "     ")).toString());

        return buf;
    }

    public static void createDirectoryStructure(File file) {
        File parentDirectory = file.getParentFile();
        if (!parentDirectory.exists()) {
            createDirectoryStructure(parentDirectory);
        }

        parentDirectory.mkdir();
    }

    public static void createEjbJarFile(String fileLocation) throws IOException {
        File ejbJarFile = createFile(fileLocation);

        JarOutputStream jarOut = new JarOutputStream(new FileOutputStream(ejbJarFile), new Manifest());
        jarOut.putNextEntry(new JarEntry(EJB_JAR_XML_ENTRY_NAME));
        jarOut.close();
    }

    public static void createEjbJarXmlFile(String fileLocation) throws IOException {
        createFile(fileLocation);
    }

    public static File createFile(String fileLocation) throws IOException {
        File file = new File(fileLocation);
        createDirectoryStructure(file);
        file.createNewFile();
        return file;
    }

    /**
      * @return Document the DOM object from a given ejbJar object.
      * @param ejbJar an EjbJar object
      */
    private static Document documentForEjb_1_1(EjbJar ejbJar) {
        //BUG # 2612131: remove dependencies on Xerces
        XMLPlatform xmlPlatform = XMLPlatformFactory.getInstance().getXMLPlatform();
        Document publicDocument = xmlPlatform.createDocumentWithPublicIdentifier(EJB_JAR_DOC_NAME, EJB_JAR_1_1_DOCTYPE_DESC, EJB_JAR_1_1_DTD_URL);

        // Bug2792054 ejbJar is added on publicDocument directly instead of creating a new root.
        ejbJar.toElement(publicDocument);
        return publicDocument;
    }

    /**
      * @return Document the DOM object from a given ejbJar object.
      * @param ejbJar an EjbJar object
      */
    private static Document documentForEjb_2_0(EjbJar ejbJar) {
        //BUG # 2612131: remove dependencies on Xerces
        XMLPlatform xmlPlatform = XMLPlatformFactory.getInstance().getXMLPlatform();
        Document publicDocument = xmlPlatform.createDocumentWithPublicIdentifier(EJB_JAR_DOC_NAME, EJB_JAR_2_0_DOCTYPE_DESC, EJB_JAR_2_0_DTD_URL);

        // Bug2792054 ejbJar is added on publicDocument directly instead of creating a new root.
        ejbJar.toElement(publicDocument);
        return publicDocument;
    }

    /**
      * @param Node the document node (preferably Document itself)
      * @param indent the indenting string
      * @return StringBuffer the buffer containing a String representation
      * (ejb-jar.xml contents) of the DOM Document object
      * Note that this DOES NOT return PI and DOCTYPE
      * Compare this method with getXML() which returns PI and DOCTYPE (hardcoded there).
      */
    public static StringBuffer generateXML(Node node, String indent) {
        final String NEWLINE = System.getProperty("line.separator");
        StringBuffer buffer = new StringBuffer();
        switch (node.getNodeType()) {
        case Node.DOCUMENT_NODE: {
            buffer.append("<xml version=\"1.0\">\n");
            // recurse on each child
            NodeList nodes = node.getChildNodes();
            if (nodes != null) {
                int len_ = nodes.getLength();
                for (int i = 0; i < len_; i++) {
                    //BUG 2790307: method not found
                    buffer.append(XMLManager.generateXML(nodes.item(i), "").toString());
                }
            }
            break;
        }
        case Node.ELEMENT_NODE: {
            String name = node.getNodeName();
            buffer.append(NEWLINE + indent + "<" + name);
            NamedNodeMap attributes = node.getAttributes();
            int len_ = attributes.getLength();
            for (int i = 0; i < len_; i++) {
                Node current = attributes.item(i);
                buffer.append(" " + current.getNodeName() + "=\"" + current.getNodeValue() + "\"");
            }

            // recurse on each child
            NodeList children = node.getChildNodes();
            if ((children != null) && (children.getLength() > 0)) {
                buffer.append(">");
                int chlen_ = children.getLength();
                for (int i = 0; i < chlen_; i++) {
                    //BUG 2790307: method not found
                    buffer.append(XMLManager.generateXML(children.item(i), indent + "     ").toString());
                }
                if ((children.getLength() == 1) && ((((Node)children.item(0)).getNodeType() == Node.TEXT_NODE) || (((Node)children.item(0)).getNodeType() == Node.CDATA_SECTION_NODE))) {
                    buffer.append("</" + name + ">");
                } else {
                    buffer.append(NEWLINE + indent + "</" + name + ">");
                }
            } else {
                buffer.append("></" + name + ">");
            }
            break;
        }
        case Node.TEXT_NODE: {
            buffer.append(node.getNodeValue());
            break;
        }
        case Node.CDATA_SECTION_NODE: {
            buffer.append("<![CDATA[");
            buffer.append(node.getNodeValue());
            buffer.append("]]>");
            break;
        }
        case Node.PROCESSING_INSTRUCTION_NODE: {
            buffer.append("<?" + node.getNodeName() + " " + node.getNodeValue() + "?>");
            break;
        }
        case Node.ENTITY_REFERENCE_NODE: {
            buffer.append("&" + node.getNodeName() + ";");
            break;
        }
        }
        return buffer;
    }
    // end printNodeMethod

    /**
      * Moves a given file to a given destination, and overwrites it if necessary.
      * @param src the source fileName
      * @param dest the destination fileName
      * @param overwrite the overwrite flag
      */
    public static void move(File src, File dst, boolean overwrite) throws IOException {
        if (src.equals(dst)) {
            return;
        }
        if (src.isDirectory()) {
            throw new IOException(ExceptionLocalization.buildMessage("can_not_move_directory", (Object[])null));
        }

        if (dst.isFile() && overwrite) {
            move(src, dst);
        } else if (dst.isFile() && !overwrite) {
            Object[] args = { dst };
            throw new IOException(ExceptionLocalization.buildMessage("file_exists", args));
        } else if (dst.isDirectory()) {
            move(src, new File(dst, src.getName()));
        }
        // try to figure out whether user wanted a to move to a new directory
        // or a new file
        else if (dst.getPath().endsWith(System.getProperty("file.separator"))) {
            if (!dst.mkdirs()) {
                Object[] args = { dst };
                throw new IOException(ExceptionLocalization.buildMessage("can_not_create_file", args));
            }
            move(src, new File(dst, src.getName()));
        } else {
            String dir = dst.getParent();

            // String name = dst.getName();
            if (dir != null) {
                if (!(new File(dir)).mkdirs()) {
                    Object[] args = { dir };
                    throw new IOException(ExceptionLocalization.buildMessage("can_not_create_directory", args));

                }
            }
            move(src, dst);
        }
    }

    /**
      * Moves a given file to a given destination.
      * @param src the source fileName
      * @param dest the destination fileName
      */
    private static void move(File src, File dest) throws IOException {
        FileInputStream fin = new FileInputStream(src);
        FileOutputStream fout = new FileOutputStream(dest);
        copy(fin, fout);
        fout.close();
        fin.close();
        src.delete();
    }
    // end move();

    /**
      * Copies the given inputStream to an outputStream
      * @param in the inputStream
      * @param  out the outputStream
      */
    public static void copy(InputStream in, OutputStream out) throws IOException {
        // do not allow other threads to read from the
        // input or write to the output while copying is
        // taking place
        synchronized (in) {
            synchronized (out) {
                byte[] buffer = new byte[256];
                while (true) {
                    int bytesRead = in.read(buffer);
                    if (bytesRead == -1) {
                        break;
                    }

                    out.write(buffer, 0, bytesRead);
                }
            }
        }
    }
}