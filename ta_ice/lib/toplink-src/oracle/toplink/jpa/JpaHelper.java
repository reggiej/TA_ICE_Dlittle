package oracle.toplink.jpa; 

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory; 
import javax.persistence.Query; 
import oracle.toplink.internal.ejb.cmp3.*; 
import oracle.toplink.internal.localization.ExceptionLocalization;
import oracle.toplink.queryframework.*; 
import oracle.toplink.threetier.Server; 
import oracle.toplink.threetier.ServerSession; 
import oracle.toplink.util.SessionFactory; 

/** 
 * This sample illustrates the JPA helper methods that may be of use 
 * to TopLink customers attempting to leverage TopLink specific functionality. 
 * 
 * @author Doug Clarke 
 * @since Oracle TopLink 11gR1-J1-Preview 
 */ 
public class JpaHelper { 
    /** 
     * Verify if the JPA provider is TopLink. If you are in a container 
     * and not in a transaction this method may incorrectly return false. 
     * It is always more reliable to check isTopLink on the EMF or Query. 
     */ 
    public static boolean isTopLink(EntityManager em) { 
        try { 
            EntityManager emImpl = getEntityManager(em); 
        } catch (IllegalArgumentException iae) { 
            return false; 
        } 

        return em != null; 
    } 

    /** 
     * Verify if the JPA provider is TopLink 
     */ 
    public static boolean isTopLink(EntityManagerFactory emf) { 
        try { 
            getEntityManagerFactory(emf); 
        } catch (IllegalArgumentException iae) { 
            return false; 
        } 

        return true; 
    } 

    /** 
     * Verify if the JPA provider is TopLink 
     */ 
    public static boolean isTopLink(Query query) { 
        try { 
            getReadAllQuery(query); 
        } catch (IllegalArgumentException iae) { 
            return false; 
        }

        return true; 
    } 

    /** 
     * Determine if the JPA query is a TopLink ReportQuery. Useful for 
     * frameworks that want to dtermine which get_X_Query method they can 
     * safely invoke. 
     */ 
    public static boolean isReportQuery(Query query) { 
        try { 
            getReportQuery(query); 
        } catch (IllegalArgumentException iae) { 
            return false; 
        } 
        return true; 
    } 

    /** 
     * Access the internal TopLink query wrapped within the JPA query. A 
     * TopLink JPA created from JP QL  contains a ReportQuery if multiple 
     * items or a non-entity type is being returned. This method will fail 
     * if a single entity type is being returned as the query is a ReadAllQuery. 
     * 
     * @see Helper#getReadAllQuery 
     */ 
    public static ReportQuery getReportQuery(Query query) { 
        if (EJBQueryImpl.class.isAssignableFrom(query.getClass())) { 
            DatabaseQuery dbQuery = ((EJBQueryImpl)query).getDatabaseQuery(); 
            if (dbQuery.isReportQuery()) { 
                return (ReportQuery)dbQuery; 
            } 

            throw new IllegalArgumentException(ExceptionLocalization.buildMessage("jpa_helper_invalid_report_query" + query.getClass()));
        } 
 
        throw new IllegalArgumentException(ExceptionLocalization.buildMessage("jpa_helper_invalid_query" + query.getClass()));
    } 

    /** 
     * Access the internal TopLink query wrapped within the JPA query. A TopLink 
     * JPA created from JP QL only contains a ReadAllQuery if only a single entity 
     * type is being returned. 
     * 
     * A ReadAllQuery is the super class of a ReportQuery so this method will 
     * always work for either a ReportQuery or ReadAllQuery. 
     */ 
    public static ReadAllQuery getReadAllQuery(Query query) { 
        if (EJBQueryImpl.class.isAssignableFrom(query.getClass())) { 
            DatabaseQuery dbQuery = ((EJBQueryImpl)query).getDatabaseQuery(); 
            if (dbQuery.isReadAllQuery()) { 
                return (ReadAllQuery)dbQuery; 
            } 

            throw new IllegalArgumentException(ExceptionLocalization.buildMessage("jpa_helper_invalid_read_all_query" + query.getClass()));
        } 
        
        throw new IllegalArgumentException(ExceptionLocalization.buildMessage("jpa_helper_invalid_query" + query.getClass()));
    } 

    /** 
     * Create a TopLink JPA query dynamically given a TopLink query. 
     */ 
    public static Query createQuery(ReadAllQuery query, EntityManager em) { 
        EntityManagerImpl emImpl = (EntityManagerImpl)getEntityManager(em); 
        return new EJBQueryImpl(query, emImpl); 
    } 

    /** 
     * Convert a JPA entityManager into a TopLink specific one. This will work 
     * both within a JavaSE deployment as well as within a container where the 
     * EntityManager may be wrapped. 
     * 
     * In the case where the container is not in a transaction it may return null 
     * for its delegate. When this happens the only way to access an EntityManager 
     * is to use the EntityManagerFactory to create a temporary one where the 
     * application manage its lifecycle. 
     */ 
    public static JpaEntityManager getEntityManager(EntityManager entityManager) { 
        if (JpaEntityManager.class.isAssignableFrom(entityManager.getClass())) { 
            return (JpaEntityManager)entityManager; 
        } 

        if (entityManager.getDelegate() != null) { 
            return getEntityManager((EntityManager)entityManager.getDelegate()); 
        } 

        return null; 
    } 

    /** 
     * Given a JPA EntityManagerFactory attempt to cast it to a TopLink EMF. 
     */ 
    public static EntityManagerFactoryImpl getEntityManagerFactory(EntityManagerFactory emf) { 
        if (EntityManagerFactoryImpl.class.isAssignableFrom(emf.getClass())) { 
            return (EntityManagerFactoryImpl)emf; 
        } 

        throw new IllegalArgumentException(ExceptionLocalization.buildMessage("jpa_helper_invalid_entity_manager_factory" + emf.getClass()));
    } 

    /** 
     * Retrieve the shared server session from the EMF. 
     */ 
    public static Server getServerSession(EntityManagerFactory emf) { 
        return getEntityManagerFactory(emf).getServerSession(); 
    } 

    /** 
     * Create a TopLink EMF given a ServerSession that has already been created 
     * and logged in. 
     */ 
    public static EntityManagerFactory createEntityManagerFactory(Server session) { 
        return new EntityManagerFactoryImpl((ServerSession)session); 
    } 

    /** 
     * Create a TopLink EMF using a session name and sessions.xml. This is 
     * equivalent to using the toplink.session-xml and toplink.session-name PU 
     * properties with the exception that no persistence.xml is required. 
     * 
     * The application would be required to manage this singleton EMF. 
     */ 
    public static EntityManagerFactoryImpl createEntityManagerFactory(String sessionName) { 
        SessionFactory sf = new SessionFactory(sessionName); 
        // Verify that shared session is a ServerSession 
        return new EntityManagerFactoryImpl((ServerSession)sf.getSharedSession()); 
    } 
}