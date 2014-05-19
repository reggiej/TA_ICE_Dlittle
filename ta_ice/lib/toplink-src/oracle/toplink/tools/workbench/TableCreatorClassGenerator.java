// Copyright (c) 1998, 2007, Oracle. All rights reserved.  
package oracle.toplink.tools.workbench;

import java.io.*;
import java.util.*;
import oracle.toplink.exceptions.*;
import oracle.toplink.internal.helper.*;
import oracle.toplink.sessions.*;
import oracle.toplink.tools.codegen.*;
import oracle.toplink.tools.schemaframework.*;

/**
 * <p><b>Purpose</b>: Allow for a class storing a TopLink table creator's tables (meta-data) to be generated.
 * This class can then be used at runtime to (re)create a project's database schema.
 *
 * @since TopLink 3.0
 * @author James Sutherland
 */
public class TableCreatorClassGenerator {
    protected String className;
    protected String packageName;
    protected String outputPath;
    protected String outputFileName;
    protected Writer outputWriter;
    protected TableCreator tableCreator;

    /**
     * PUBLIC:
     * Create a new generator.
     */
    public TableCreatorClassGenerator() {
        this.outputPath = "";
        this.outputFileName = "TableCreator.java";
        this.className = "TableCreator";
        this.packageName = "";
    }

    /**
     * PUBLIC:
     * Create a new generator to output the table creator.
     */
    public TableCreatorClassGenerator(TableCreator tableCreator) {
        this();
        this.tableCreator = tableCreator;
    }

    /**
     * PUBLIC:
     * Create a new generator to output the table creator.
     */
    public TableCreatorClassGenerator(TableCreator tableCreator, String projectClassName, Writer outputWriter) {
        this(tableCreator);
        this.outputWriter = outputWriter;
        setClassName(projectClassName);
    }

    /**
     * PUBLIC:
     * Create a new generator to output the table creator.
     */
    public TableCreatorClassGenerator(TableCreator tableCreator, String projectClassName, String fileName) {
        this(tableCreator);
        setClassName(projectClassName);
        setOutputFileName(fileName);
    }

    protected void addFieldLines(FieldDefinition field, NonreflectiveMethodDefinition method) {
        String fieldName = "field" + field.getName();
        method.addLine("FieldDefinition " + fieldName + " = new FieldDefinition();");
        method.addLine(fieldName + ".setName(\"" + field.getName() + "\");");
        String fieldTypeName = field.getTypeName();
        if (fieldTypeName != null) {
            method.addLine(fieldName + ".setTypeName(\"" + field.getTypeName() + "\");");
        } else {//did not set the field type name, so use the Java type data
            method.addLine(fieldName + ".setType(" + field.getType().getName() + ".class);");
        }
        method.addLine(fieldName + ".setSize(" + field.getSize() + ");");
        method.addLine(fieldName + ".setSubSize(" + field.getSubSize() + ");");
        method.addLine(fieldName + ".setIsPrimaryKey(" + field.isPrimaryKey() + ");");
        method.addLine(fieldName + ".setIsIdentity(" + field.isIdentity() + ");");
        method.addLine(fieldName + ".setUnique(" + field.isUnique() + ");");
        method.addLine(fieldName + ".setShouldAllowNull(" + field.shouldAllowNull() + ");");
        method.addLine("table.addField(" + fieldName + ");");
    }

    protected void addForeignKeyLines(ForeignKeyConstraint foreignKey, NonreflectiveMethodDefinition method) {
        String foreignKeyName = "foreignKey" + foreignKey.getName();
        method.addLine("ForeignKeyConstraint " + foreignKeyName + " = new ForeignKeyConstraint();");
        method.addLine(foreignKeyName + ".setName(\"" + foreignKey.getName() + "\");");
        method.addLine(foreignKeyName + ".setTargetTable(\"" + foreignKey.getTargetTable() + "\");");

        for (Enumeration sourceFieldsEnum = foreignKey.getSourceFields().elements();
                 sourceFieldsEnum.hasMoreElements();) {
            method.addLine(foreignKeyName + ".addSourceField(\"" + sourceFieldsEnum.nextElement() + "\");");
        }
        for (Enumeration targetFieldsEnum = foreignKey.getTargetFields().elements();
                 targetFieldsEnum.hasMoreElements();) {
            method.addLine(foreignKeyName + ".addTargetField(\"" + targetFieldsEnum.nextElement() + "\");");
        }

        method.addLine("table.addForeignKeyConstraint(" + foreignKeyName + ");");
    }

    protected NonreflectiveMethodDefinition buildConstructor() {
        NonreflectiveMethodDefinition methodDefinition = new NonreflectiveMethodDefinition();

        methodDefinition.setName(getClassName());
        methodDefinition.setIsConstructor(true);

        methodDefinition.addLine("setName(\"" + getTableCreator().getName() + "\");");

        methodDefinition.addLine("");

        for (Enumeration tablesEnum = getTableCreator().getTableDefinitions().elements();
                 tablesEnum.hasMoreElements();) {
            TableDefinition table = (TableDefinition)tablesEnum.nextElement();
            methodDefinition.addLine("addTableDefinition(build" + table.getName() + "Table());");
        }

        return methodDefinition;
    }

    protected NonreflectiveMethodDefinition buildLoginMethod(DatabaseLogin login) {
        NonreflectiveMethodDefinition method = new NonreflectiveMethodDefinition();

        method.setName("applyLogin");

        String loginClassName = login.getClass().getName();
        if (login.getClass().equals(DatabaseLogin.class)) {
            loginClassName = Helper.getShortClassName(login);
        }
        method.addLine(loginClassName + " login = new " + loginClassName + "();");

        method.addLine("login.usePlatform(new " + login.getPlatformClassName() + "());");
        method.addLine("login.setDriverClass(" + login.getDriverClassName() + ".class);");
        method.addLine("login.setConnectionString(\"" + login.getConnectionString() + "\");");

        if (login.getUserName() != null) {
            method.addLine("login.setUserName(\"" + login.getUserName() + "\");");
        }

        if (login.getPassword() != null) {
            method.addLine("login.setPassword(\"" + login.getPassword() + "\");");
        }

        method.addLine("");
        method.addLine("// Configuration properties.");
        method.addLine("login.setUsesNativeSequencing(" + login.shouldUseNativeSequencing() + ");");
        if (!login.shouldUseNativeSequencing()) {
            method.addLine("login.setSequenceTableName(\"" + login.getSequenceTableName() + "\");");
            method.addLine("login.setSequenceNameFieldName(\"" + login.getSequenceNameFieldName() + "\");");
            method.addLine("login.setSequenceCounterFieldName(\"" + login.getSequenceCounterFieldName() + "\");");
        }
        method.addLine("login.setShouldBindAllParameters(" + login.shouldBindAllParameters() + ");");
        method.addLine("login.setShouldCacheAllStatements(" + login.shouldCacheAllStatements() + ");");
        method.addLine("login.setUsesByteArrayBinding(" + login.shouldUseByteArrayBinding() + ");");
        method.addLine("login.setUsesStringBinding(" + login.shouldUseStringBinding() + ");");
        method.addLine("if (login.shouldUseByteArrayBinding()) { // Can only be used with binding.");
        method.addLine("\tlogin.setUsesStreamsForBinding(" + login.shouldUseStreamsForBinding() + ");");
        method.addLine("}");
        method.addLine("login.setShouldForceFieldNamesToUpperCase(" + login.shouldForceFieldNamesToUpperCase() + ");");
        method.addLine("login.setShouldOptimizeDataConversion(" + login.shouldOptimizeDataConversion() + ");");
        method.addLine("login.setShouldTrimStrings(" + login.shouldTrimStrings() + ");");
        method.addLine("login.setUsesBatchWriting(" + login.shouldUseBatchWriting() + ");");
        method.addLine("if (login.shouldUseBatchWriting()) { // Can only be used with batch writing.");
        method.addLine("\tlogin.setUsesJDBCBatchWriting(" + login.shouldUseJDBCBatchWriting() + ");");
        method.addLine("}");
        method.addLine("login.setUsesExternalConnectionPooling(" + login.shouldUseExternalConnectionPooling() + ");");
        method.addLine("login.setUsesExternalTransactionController(" + login.shouldUseExternalTransactionController() + ");");

        method.addLine("setLogin(login);");

        return method;
    }

    protected NonreflectiveMethodDefinition buildTableMethod(TableDefinition table) {
        NonreflectiveMethodDefinition method = new NonreflectiveMethodDefinition();

        method.setName("build" + table.getName() + "Table");
        method.setReturnType("TableDefinition");

        // Table
        method.addLine("TableDefinition table = new TableDefinition();");
        method.addLine("table.setName(\"" + table.getName() + "\");");

        // Fields
        for (Enumeration fieldsEnum = table.getFields().elements(); fieldsEnum.hasMoreElements();) {
            method.addLine("");
            FieldDefinition field = (FieldDefinition)fieldsEnum.nextElement();
            addFieldLines(field, method);
        }

        // Constraints
        for (Enumeration constraintsEnum = table.getForeignKeys().elements();
                 constraintsEnum.hasMoreElements();) {
            method.addLine("");
            ForeignKeyConstraint foreignKey = (ForeignKeyConstraint)constraintsEnum.nextElement();
            addForeignKeyLines(foreignKey, method);
        }

        method.addLine("");

        method.addLine("return table;");

        return method;
    }

    /**
     * PUBLIC:
     * Generate the creator class, output the java source code to the stream or file.
     * useUnicode determines if unicode escaped characters for non_ASCII charaters will be used.
     */
    public void generate(boolean useUnicode) throws ValidationException {
        if (getOutputWriter() == null) {
            try {
                setOutputWriter(new OutputStreamWriter(new FileOutputStream(getOutputPath() + getOutputFileName())));
            } catch (IOException exception) {
                throw ValidationException.fileError(exception);
            }
        }

        CodeGenerator generator = new CodeGenerator(useUnicode);
        generator.setOutput(getOutputWriter());
        generateCreatorClass().write(generator);
        try {
            getOutputWriter().flush();
            getOutputWriter().close();
        } catch (IOException exception) {
            throw ValidationException.fileError(exception);
        }
    }

    /**
     * PUBLIC:
     * Generate the project class, output the java source code to the stream or file.
     * Unicode escaped characters for non_ASCII charaters will be used.
     */
    public void generate() throws ValidationException {
        generate(true);
    }

    /**
     * Return a class definition object representing the code to be generated for the table creator.
     * This class will have one method per descriptor and its toString can be used to convert it to code.
     */
    protected ClassDefinition generateCreatorClass() {
        ClassDefinition classDefinition = new ClassDefinition();

        classDefinition.setName(getClassName());
        classDefinition.setSuperClass("oracle.toplink.tools.schemaframework.TableCreator");
        classDefinition.setPackageName(getPackageName());

        classDefinition.addImport("oracle.toplink.sessions.*");
        classDefinition.addImport("oracle.toplink.tools.schemaframework.*");

        classDefinition.setComment("This class was generated by the TopLink table creator generator." + Helper.cr() + "It stores the meta-data (tables) that define the database schema." + Helper.cr() + "@see oracle.toplink.tools.workbench.TableCreatorClassGenerator");

        classDefinition.addMethod(buildConstructor());

        for (Enumeration tablesEnum = getTableCreator().getTableDefinitions().elements();
                 tablesEnum.hasMoreElements();) {
            TableDefinition table = (TableDefinition)tablesEnum.nextElement();
            classDefinition.addMethod(buildTableMethod(table));
        }

        return classDefinition;
    }

    /**
     * PUBLIC:
     * Return the name of class to be generated.
     * This is the unqualified name.
     */
    public String getClassName() {
        return className;
    }

    /**
     * PUBLIC:
     * Return the file name that the generate .java file will be output to.
     */
    public String getOutputFileName() {
        return outputFileName;
    }

    /**
     * PUBLIC:
     * Return the path that the generate .java file will be output to.
     */
    public String getOutputPath() {
        return outputPath;
    }

    /**
     * PUBLIC:
     * Return the writer the output to.
     */
    public Writer getOutputWriter() {
        return outputWriter;
    }

    /**
     * PUBLIC:
     * Return the package name of class to be generated.
     */
    public String getPackageName() {
        return packageName;
    }

    /**
     * PUBLIC:
     * Return the table creator to generate from.
     */
    public TableCreator getTableCreator() {
        return tableCreator;
    }

    /**
     * Return the printed version of the primitive value object.
     * This must determine the class and use the correct constrcutor arguments.
     */
    protected String printString(Object value) {
        if ((value == null) || (value == Helper.getNullWrapper())) {
            return "null";
        }

        if (value instanceof String) {
            return "\"" + value + "\"";
        }

        // This handles most cases.
        return "new " + Helper.getShortClassName(value) + "(" + value + ")";
    }

    protected String removeDots(String packageName) {
        StringWriter writer = new StringWriter();
        int startIndex = 0;
        int dotIndex = packageName.indexOf('.', startIndex);
        while (dotIndex >= 0) {
            writer.write(packageName.substring(startIndex, dotIndex));
            startIndex = dotIndex + 1;
            dotIndex = packageName.indexOf('.', startIndex);
        }
        writer.write(packageName.substring(startIndex, packageName.length()));

        return writer.toString();
    }

    /**
     * PUBLIC:
     * Set the name of class to be generated.
     * This can be qualified or unqualified name and will set the file name to match.
     */
    public void setClassName(String newClassName) {
        int lastDotIndex = newClassName.lastIndexOf(".");
        if (lastDotIndex >= 0) {
            className = newClassName.substring(lastDotIndex + 1, newClassName.length());
            setPackageName(newClassName.substring(0, lastDotIndex));
        } else {
            className = newClassName;
        }
        setOutputFileName(newClassName);
    }

    /**
     * PUBLIC:
     * Set the file name that the generate .java file will be output to.
     * If the file does not include .java it will be appended.
     */
    public void setOutputFileName(String newOutputFileName) {
        if (newOutputFileName.indexOf(".java") < 0) {
            outputFileName = newOutputFileName + ".java";
        } else {
            outputFileName = newOutputFileName;
        }
    }

    /**
     * PUBLIC:
     * Set the path that the generate .java file will be output to.
     */
    public void setOutputPath(String newOutputPath) {
        outputPath = newOutputPath;
    }

    /**
     * PUBLIC:
     * Set the writer the output to.
     */
    public void setOutputWriter(Writer outputWriter) {
        this.outputWriter = outputWriter;
    }

    /**
     * PUBLIC:
     * Set the package name of class to be generated.
     */
    public void setPackageName(String newPackageName) {
        packageName = newPackageName;
    }

    /**
     * PUBLIC:
     * Set the table creator to generate from.
     * All of the creator's tables will be stored into the file.
     */
    public void setTableCreator(TableCreator tableCreator) {
        this.tableCreator = tableCreator;
    }

    /**
     * PUBLIC:
     * Generate the source code to a table creator class to the table creator's tables into the writer.
     */
    public static void write(TableCreator tableCreator, String creatorClassName, Writer writer) {
        TableCreatorClassGenerator generator = new TableCreatorClassGenerator(tableCreator, creatorClassName, writer);
        generator.generate();
    }

    /**
     * PUBLIC:
     * Generate the source code to a table creator class to the table creator's tables into the file.
     */
    public static void write(TableCreator tableCreator, String creatorClassName, String fileName) {
        TableCreatorClassGenerator generator = new TableCreatorClassGenerator(tableCreator, creatorClassName, fileName);
        generator.generate();
    }
}