// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.tools.ejbjar;

import oracle.toplink.exceptions.EJBJarXMLException;
import oracle.toplink.exceptions.i18n.ExceptionMessageGenerator;
import java.util.*;

/**
 * INTERNAL:
 */
public class ValidationManager {
    public static ListMap validateEjbJar(EjbJar ejbJar) {
        ListMap errors = new ListMap();
        if (ejbJar == null) {
            return errors;
        }

        validateRequiredAttribute(ejbJar.getEnterpriseObjects(), "enterprise-beans", "ejb-jar", errors);

        errors.putAll(validateEnterpiseObjects(ejbJar.getEnterpriseObjects()));
        errors.putAll(validateRelationships(ejbJar.getRelationships()));
        errors.putAll(validateEjbNameFromRelationRoleSources(ejbJar.getRelationships(), ejbJar.getEntities()));

        return errors;
    }

    public static ListMap validateEjbJarXMLDocument(EjbJarXMLDocument doc) {
        if (doc != null) {
            return validateEjbJar(doc.getEjbJar());
        }
        return new ListMap();
    }

    private static ListMap validateEjbNameFromRelationRoleSources(Relationships relationships, Vector entities) {
        ListMap errors = new ListMap();
        HashSet entityEjbNames = new HashSet();

        if ((relationships == null) || (relationships.getRelationships() == null) || (entities == null)) {
            return errors;
        }

        String ejbName;
        for (Enumeration enumtr = entities.elements(); enumtr.hasMoreElements();) {
            ejbName = ((Entity)enumtr.nextElement()).getEjbName();
            if (ejbName != null) {
                entityEjbNames.add(ejbName.trim());
            }
        }

        String message;
        EjbJarError error;
        for (Enumeration enumtr = relationships.getRelationships().elements();
                 enumtr.hasMoreElements();) {
            Relationship relationship = (Relationship)enumtr.nextElement();
            if ((relationship.getRole1() != null) && (relationship.getRole1().getRelationshipRoleSource() != null) && (relationship.getRole1().getRelationshipRoleSource().getEjbName() != null)) {
                ejbName = relationship.getRole1().getRelationshipRoleSource().getEjbName().trim();
                if (!entityEjbNames.contains(ejbName)) {
                    Object[] arguments = new Object[] { ejbName, relationship.getRole1().getRoleName() };
                    message = ExceptionMessageGenerator.buildMessage(EJBJarXMLException.class, EJBJarXMLException.INVALID_EJB_NAME_FOR_RELATIONSHIP_ROLE, arguments);
                    error = new EjbJarError(message);
                    errors.put(error, error);
                }
            }

            if ((relationship.getRole2() != null) && (relationship.getRole2().getRelationshipRoleSource() != null) && (relationship.getRole2().getRelationshipRoleSource().getEjbName() != null)) {
                ejbName = relationship.getRole2().getRelationshipRoleSource().getEjbName().trim();
                if (!entityEjbNames.contains(ejbName)) {
                    Object[] arguments = new Object[] { ejbName, relationship.getRole2().getRoleName() };
                    message = ExceptionMessageGenerator.buildMessage(EJBJarXMLException.class, EJBJarXMLException.INVALID_EJB_NAME_FOR_RELATIONSHIP_ROLE, arguments);
                    error = new EjbJarError(message);
                    errors.put(error, error);
                }
            }
        }
        return errors;
    }

    private static ListMap validateEnterpiseObjects(Vector enterpiseObjects) {
        ListMap errors = new ListMap();
        Vector entities = new Vector();
        Vector ejbNames = new Vector();
        String message;
        EjbJarError error;

        if (enterpiseObjects == null) {
            return errors;
        }

        for (Enumeration enumtr = enterpiseObjects.elements(); enumtr.hasMoreElements();) {
            EnterpriseObject enterpriseObject = (EnterpriseObject)enumtr.nextElement();

            // validate ejb-name
            if (enterpriseObject.getEjbName() == null) {
                //do nothing here
            } else if (ejbNames.contains(enterpriseObject.getEjbName().trim())) {
                Object[] arguments = new Object[] { enterpriseObject.getEjbName() };
                message = ExceptionMessageGenerator.buildMessage(EJBJarXMLException.class, EJBJarXMLException.MULTIPLE_ENTITIES_FOUND_FOR_EJB_NAME, arguments);
                error = new EjbJarError(message);
                errors.put(error, error);
            } else {
                ejbNames.add(enterpriseObject.getEjbName().trim());
            }

            // initialize entities
            if (enterpriseObject.isEntity()) {
                entities.add(enterpriseObject);
            }
        }

        // Just validate entityBeans
        errors.putAll(validateEntities(entities));
        return errors;
    }

    private static ListMap validateEntities(Vector entities) {
        ListMap errors = new ListMap();
        String persistenceType = null;
        String cmpVersion = null;
        String message;
        EjbJarError error;
        EjbJarError singlePersistentTypeError = null;

        if (entities == null) {
            return errors;
        }

        for (Enumeration enumtr = entities.elements(); enumtr.hasMoreElements();) {
            Entity entity = (Entity)enumtr.nextElement();

            // validate required attributes    
            validateRequiredAttribute(entity.getCmpVersion(), "cmp-version", "entity", errors);
            validateRequiredAttribute(entity.getPersistenceType(), "persistence-type", "entity", errors);
            validateRequiredAttribute(entity.getEjbName(), "ejb-name", "entity", errors);
            validateRequiredAttribute(entity.getEjbClass(), "ejb-class", "entity", errors);
            validateRequiredAttribute(entity.getPrimaryKeyClass(), "prim-key-class", "entity", errors);
            validateRequiredAttribute(entity.getQueries(), "query", "entity", errors);

            if (persistenceType == null) {
                persistenceType = entity.getPersistenceType();
            }
            if (cmpVersion == null) {
                cmpVersion = entity.getCmpVersion();
            }
            if ((entity.getCmpVersion() != null) && (!entity.getCmpVersion().equalsIgnoreCase("1.x")) && (!entity.getCmpVersion().equalsIgnoreCase("2.x"))) {
                Object[] arguments = new Object[] { entity.getEjbName() };
                message = ExceptionMessageGenerator.buildMessage(EJBJarXMLException.class, EJBJarXMLException.INVALID_CMP_VERSION, arguments);
                error = new EjbJarError(message);
                errors.put(error, error);
            }
            if ((entity.getPersistenceType() != null) && (!entity.getPersistenceType().equalsIgnoreCase("Bean")) && (!entity.getPersistenceType().equalsIgnoreCase("Container"))) {
                Object[] arguments = new Object[] { entity.getEjbName() };
                message = ExceptionMessageGenerator.buildMessage(EJBJarXMLException.class, EJBJarXMLException.INVALID_PERSISTENCE_TYPE, arguments);
                error = new EjbJarError(message);
                errors.put(error, error);
            }
            if ((cmpVersion != null) && (persistenceType != null) && (!persistenceType.equalsIgnoreCase(entity.getPersistenceType()) || (!cmpVersion.equalsIgnoreCase(entity.getCmpVersion())))) {
                Object[] arguments = new Object[] {  };
                message = ExceptionMessageGenerator.buildMessage(EJBJarXMLException.class, EJBJarXMLException.NOT_SINGLE_PERSISTENCE_TYPE, arguments);
                singlePersistentTypeError = new EjbJarError(message);
                errors.put(singlePersistentTypeError, singlePersistentTypeError);
            }

            // validate queries
            if (entity.getQueries() != null) {
                errors.putAll(validateQueries(entity.getQueries()));
            }
        }
        return errors;
    }

    private static ListMap validateQueries(Vector queries) {
        ListMap errors = new ListMap();
        if (queries == null) {
            return errors;
        }

        for (Enumeration enumtr = queries.elements(); enumtr.hasMoreElements();) {
            Query query = (Query)enumtr.nextElement();
            validateRequiredAttribute(query.getQueryMethod(), "query-method", "query", errors);
            errors.putAll(validateQueryMethod(query.getQueryMethod()));
        }
        return errors;
    }

    private static ListMap validateQueryMethod(QueryMethod method) {
        ListMap errors = new ListMap();
        validateRequiredAttribute(method.getMethodName(), "method-name", "query-method", errors);
        validateRequiredAttribute(method.getParams(), "method-params", "query-method", errors);
        String methodName = method.getMethodName();
        if ((methodName != null) && (!methodName.trim().startsWith("find")) && (!methodName.trim().startsWith("ejbSelect"))) {
            Object[] arguments = new Object[] { methodName };
            String message = ExceptionMessageGenerator.buildMessage(EJBJarXMLException.class, EJBJarXMLException.INVALID_QUERY_METHOD_NAME, arguments);
            EjbJarError error = new EjbJarError(message);
            errors.put(error, error);
        }
        return errors;
    }

    private static ListMap validateRelationRoleSource(RelationshipRoleSource roleSource) {
        ListMap errors = new ListMap();
        String message;
        EjbJarError error;

        if (roleSource == null) {
            return errors;
        }

        validateRequiredAttribute(roleSource.getEjbName(), "ejb-name", "relationship-role-sourcerelationship-role-source", errors);
        return errors;
    }

    private static ListMap validateRelations(Vector relations) {
        ListMap errors = new ListMap();
        Relationship relation;

        if (relations == null) {
            return errors;
        }

        for (Enumeration enumtr = relations.elements(); enumtr.hasMoreElements();) {
            relation = (Relationship)enumtr.nextElement();
            validateRequiredAttribute(relation.getRole1(), "ejb-relationship-role", "ejb-relation", errors);
            validateRequiredAttribute(relation.getRole2(), "ejb-relationship-role", "ejb-relation", errors);

            errors.putAll(validateRelationshipRole(relation.getRole1()));
            errors.putAll(validateRelationshipRole(relation.getRole2()));
        }
        return errors;
    }

    private static ListMap validateRelationshipRole(RelationshipRole role) {
        ListMap errors = new ListMap();
        String message;
        EjbJarError error;

        if (role == null) {
            return errors;
        }

        validateRequiredAttribute(role.getMultiplicity(), "multiplicity", "ejb-relationship-role", errors);
        validateRequiredAttribute(role.getRelationshipRoleSource(), "relationship-role-sourcerelationship-role-source", "ejb-relationship-role", errors);

        if ((!role.getMultiplicity().equalsIgnoreCase("One")) && (!role.getMultiplicity().equalsIgnoreCase("Many"))) {
            String argument = role.getRoleName();

            if ((argument == null) && (role.getRelationshipRoleSource() != null)) {
                argument = role.getRelationshipRoleSource().getEjbName();
            }

            Object[] arguments = new Object[] { argument };
            message = ExceptionMessageGenerator.buildMessage(EJBJarXMLException.class, EJBJarXMLException.INVALID_MULTIPLICITY, arguments);
            error = new EjbJarError(message);
            errors.put(error, error);
        }
        errors.putAll(validateRelationRoleSource(role.getRelationshipRoleSource()));
        return errors;
    }

    private static ListMap validateRelationships(Relationships relationships) {
        ListMap errors = new ListMap();

        if (relationships == null) {
            return errors;
        }

        validateRequiredAttribute(relationships.getRelationships(), "method-params", "query-method", errors);
        errors.putAll(validateRelations(relationships.getRelationships()));
        return errors;
    }

    private static void validateRequiredAttribute(Object value, String attributeName, String parentName, ListMap errors) {
        String message;
        EjbJarError error;
        Object[] arguments = new Object[] { attributeName, parentName };

        if (value == null) {
            message = ExceptionMessageGenerator.buildMessage(EJBJarXMLException.class, EJBJarXMLException.REQUIRED_ATTRIBUTE_NOT_EXIST, arguments);
            error = new EjbJarError(message);
            errors.put(error, error);
        } else if (value instanceof String) {
            validateTextAttribute((String)value, attributeName, parentName, errors);
        }
    }

    private static void validateTextAttribute(String value, String attributeName, String parentName, ListMap errors) {
        if ((value != null) && (value.length() == 0)) {
            String message;
            Object[] arguments = new Object[] { attributeName, parentName };
            message = ExceptionMessageGenerator.buildMessage(EJBJarXMLException.class, EJBJarXMLException.EMPTY_TEXT_ATTRIBUTE, arguments);
            EjbJarError error = new EjbJarError(message);
            errors.put(error, error);
        }
    }
}