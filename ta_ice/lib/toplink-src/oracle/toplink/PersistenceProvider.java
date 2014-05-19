// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink;

import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.*;
import javax.persistence.spi.*;

import oracle.toplink.exceptions.PersistenceUnitLoadingException;
import oracle.toplink.internal.ejb.cmp3.EntityManagerFactoryImpl;
import oracle.toplink.internal.ejb.cmp3.EntityManagerFactoryProvider;
import oracle.toplink.internal.ejb.cmp3.EntityManagerSetupImpl;
import oracle.toplink.internal.ejb.cmp3.JavaSECMPInitializer;
import oracle.toplink.internal.ejb.cmp3.PersistenceInitializationActivator;
import oracle.toplink.internal.ejb.cmp3.persistence.PersistenceUnitProcessor;
import oracle.toplink.internal.ejb.cmp3.persistence.SEPersistenceUnitInfo;

/**
 * This is the TopLink EJB 3.0 provider
 */
public class PersistenceProvider implements javax.persistence.spi.PersistenceProvider, PersistenceInitializationActivator {
    /**
     * Called by Persistence class when an EntityManagerFactory
     * is to be created.
     *
     * @param emName The name of the persistence unit
     * @param map A Map of properties for use by the
     * persistence provider. These properties may be used to
     * override the values of the corresponding elements in
     * the persistence.xml file or specify values for
     * properties not specified in the persistence.xml.
     * @return EntityManagerFactory for the persistence unit,
     * or null if the provider is not the right provider
     */
	public EntityManagerFactory createEntityManagerFactory(String emName, Map properties){
        Map nonNullProperties = (properties == null) ? new HashMap() : properties;
        String name = emName;
        if (name == null){
            name = "";
        }

        JavaSECMPInitializer initializer = JavaSECMPInitializer.getJavaSECMPInitializer();
        EntityManagerSetupImpl emSetupImpl = null;
        ClassLoader currentLoader = Thread.currentThread().getContextClassLoader();

        try {
            Enumeration<URL> resources = currentLoader.getResources("META-INF/persistence.xml");
            boolean initialized = false;
            while (resources.hasMoreElements()) {
                String puName = PersistenceUnitProcessor.buildPersistenceUnitName(PersistenceUnitProcessor.computePURootURL(resources.nextElement()), name);
            
                synchronized (EntityManagerFactoryProvider.emSetupImpls){
                    emSetupImpl = EntityManagerFactoryProvider.getEntityManagerSetupImpl(puName);
                    if (emSetupImpl == null || emSetupImpl.isUndeployed()){
                        if (!initialized) {
                            initializer.initialize(nonNullProperties, this);
                            initialized = true;
                        }
                        
                        emSetupImpl = EntityManagerFactoryProvider.getEntityManagerSetupImpl(puName);
                    }
                }

                // We found a match, stop looking.
                if (emSetupImpl != null) {
                    break;
                }
            }
        } catch (Exception e){
            throw PersistenceUnitLoadingException.exceptionSearchingForPersistenceResources(currentLoader, e);
        }

        //gf bug 854  Returns null if EntityManagerSetupImpl for the name doesn't exist (e.g. a non-existant PU)
        if (emSetupImpl == null) {
            return null;
        }
        
        if (!isPersistenceProviderSupported(emSetupImpl.getPersistenceUnitInfo().getPersistenceProviderClassName())){
            return null;
        }


        // synchronized to prevent overriding of the class loader
        // and also calls to predeploy and undeploy by other threads -
        // the latter may alter result of shouldRedeploy method.
        synchronized(emSetupImpl) {
            if(emSetupImpl.shouldRedeploy()) {
                SEPersistenceUnitInfo persistenceInfo = (SEPersistenceUnitInfo)emSetupImpl.getPersistenceUnitInfo();
                persistenceInfo.setClassLoader(JavaSECMPInitializer.getMainLoader());
                if (emSetupImpl.isUndeployed()){
                    persistenceInfo.setNewTempClassLoader(JavaSECMPInitializer.getMainLoader());
                }
            }
            // call predeploy
            // this will just increment the factory count since we should already be deployed
            emSetupImpl.predeploy(emSetupImpl.getPersistenceUnitInfo(), nonNullProperties);
        }
        
        EntityManagerFactoryImpl factory = null;
        try {
            factory = new EntityManagerFactoryImpl(emSetupImpl, nonNullProperties);
    
            // This code has been added to allow validation to occur without actually calling createEntityManager
            if (emSetupImpl.shouldGetSessionOnCreateFactory(nonNullProperties)) {
                factory.getServerSession();
            }
            return factory;
        } catch (RuntimeException ex) {
            if(factory != null) {
                factory.close();
            } else {
                emSetupImpl.undeploy();
            }
            throw ex;
        }
    }

    /**
     * Called by the container when an EntityManagerFactory
     * is to be created.
     *
     * @param info Metadata for use by the persistence provider
     * @return EntityManagerFactory for the persistence unit
     * specified by the metadata
     * @param map A Map of integration-level properties for use
     * by the persistence provider.
     */
    public EntityManagerFactory createContainerEntityManagerFactory(PersistenceUnitInfo info, Map properties){
        Map nonNullProperties = (properties == null) ? new HashMap() : properties;
        EntityManagerSetupImpl emSetupImpl = null;
        synchronized (EntityManagerFactoryProvider.emSetupImpls) {
            String puName = PersistenceUnitProcessor.buildPersistenceUnitName(info.getPersistenceUnitRootUrl(), info.getPersistenceUnitName());
            emSetupImpl = EntityManagerFactoryProvider.getEntityManagerSetupImpl(puName);
            if (emSetupImpl == null){
                emSetupImpl = new EntityManagerSetupImpl();
                emSetupImpl.setIsInContainerMode(true);        
                EntityManagerFactoryProvider.addEntityManagerSetupImpl(puName, emSetupImpl);
            }
        }
        
        ClassTransformer transformer = null;
        if(!emSetupImpl.isDeployed()) {
            transformer = emSetupImpl.predeploy(info, nonNullProperties);
        }
        if (transformer != null){
            info.addTransformer(transformer);
        }
        // When EntityManagerFactory is created, the session is only partially created
        // When the factory is actually accessed, the emSetupImpl will be used to complete the session construction
        EntityManagerFactoryImpl factory = new EntityManagerFactoryImpl(emSetupImpl, nonNullProperties);

        // This code has been added to allow validation to occur without actually calling createEntityManager
        if (emSetupImpl.shouldGetSessionOnCreateFactory(nonNullProperties)) {
        	factory.getServerSession();
        }
        return factory;
    }
    
    /**
     * Returns whether the given persistence provider class is supported by this implementation
     * @param providerClassName
     * @return
     */
    public boolean isPersistenceProviderSupported(String providerClassName){
        return (providerClassName == null) || providerClassName.equals("") || providerClassName.equals(PersistenceProvider.class.getName());
    }

}
