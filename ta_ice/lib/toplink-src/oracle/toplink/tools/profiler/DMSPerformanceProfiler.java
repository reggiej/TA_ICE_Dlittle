// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.tools.profiler;

import java.util.*;
import java.io.*;
import oracle.dms.instrument.*;
import oracle.dms.spy.*;
import oracle.toplink.sessions.Record;
import oracle.toplink.sessions.SessionProfiler;
import oracle.toplink.logging.SessionLog;
import oracle.toplink.queryframework.*;
import oracle.toplink.threetier.ServerSession;
import oracle.toplink.internal.localization.DMSLocalization;
import oracle.toplink.internal.sessions.AbstractRecord;
import oracle.toplink.internal.sessions.AbstractSession;

/**
 * <b>Purpose</b>: Define the interface of TopLink profiler for using DMS gate.<p>
 * <b>Description</b>: A mechanism used to provide a link for TopLink performance profiling by using the DMS tool.
 *                     The predefined TopLink metrics will be monitored by using DMS sensors. TopLink library
 *                     instrumentation will be done by inserting DMS calls for the purpose of measuring its performance<p>
 * <b>Responsibilities</b>:<ul>
 * <li> Define the TopLink metrics.
 * <li> Provide APIs to monitor the sensors at runtime.
 * <li> Change DMS sensor weight at runtime
 * </ul>
 * @since TopLink 10.1.3
 */
public class DMSPerformanceProfiler implements Serializable, Cloneable, SessionProfiler {
    protected AbstractSession session;
    protected Noun root;
    protected Map normalWeightSensors;
    protected Map heavyWeightSensors;
    protected Map allWeightSensors;
    protected Map normalAndHeavyWeightSensors;
    protected Map normalHeavyAndAllWeightSensors;
    protected Map normalWeightNouns;
    protected Map heavyWeightNouns;
    protected Map allWeightNouns;
    protected ThreadLocal operationStartTokenThreadLocal;
    protected static boolean isDMSSpyInitialized;
    protected int weight;

    /**
     * PUBLIC:
     * Create a new dms profiler.
     * The profiler can be registered with a session to log performance information.
     */
    public DMSPerformanceProfiler() {
        this(null);
    }
    
    /**
     * PUBLIC:
     * Create a new dms profiler.
     * The profiler can be registered with a session to log performance information.
     */
    public DMSPerformanceProfiler(oracle.toplink.sessions.Session session) {
        this.session = (AbstractSession)session;
        this.normalWeightNouns = new Hashtable(1);
        this.heavyWeightNouns = new Hashtable(5);
        this.allWeightNouns = new Hashtable(1);
        this.normalWeightSensors = new Hashtable(4);
        this.heavyWeightSensors = new Hashtable();
        this.allWeightSensors = new Hashtable(22);
        this.normalAndHeavyWeightSensors = new Hashtable();
        this.normalHeavyAndAllWeightSensors = new Hashtable();
        this.operationStartTokenThreadLocal = new ThreadLocal();
        this.weight = DMSConsole.getSensorWeight();
        if (!isDMSSpyInitialized) {
            isDMSSpyInitialized = true;
            initSpy();
        }
    }

    /**
     * INTERNAL:
     * Initialize TopLink noun tree based on dms weight.
     */
    protected void initializeNounTree(int newWeight) {
        if (newWeight == DMSConsole.NONE) {
            destroyNounsByWeight(DMSConsole.NORMAL);
            destroyNounsByWeight(DMSConsole.HEAVY);
            destroyNounsByWeight(DMSConsole.ALL);
            if (root != null) {
                root.destroy();
            }
            return;
        }

        if (newWeight == DMSConsole.NORMAL) {
            if (getProfileWeight() == DMSConsole.NONE) {
                initializeNormalWeightSensors();
            } else if (getProfileWeight() == DMSConsole.HEAVY) {
                destroyNounsByWeight(DMSConsole.HEAVY);
            } else if (getProfileWeight() == DMSConsole.ALL) {
                destroyNounsByWeight(DMSConsole.HEAVY);
                destroyNounsByWeight(DMSConsole.ALL);
            }
        }
        if (newWeight == DMSConsole.HEAVY) {
            if (getProfileWeight() == DMSConsole.NONE) {
                initializeNormalWeightSensors();
                initializeHeavyWeightSensors();
            } else if (getProfileWeight() == DMSConsole.NORMAL) {
                initializeHeavyWeightSensors();
            } else if (getProfileWeight() == DMSConsole.ALL) {
                destroyNounsByWeight(DMSConsole.ALL);
            }
        }

        if (newWeight == DMSConsole.ALL) {
            if (getProfileWeight() == DMSConsole.NONE) {
                initializeNormalWeightSensors();
                initializeHeavyWeightSensors();
                initializeAllWeightSensors();
            } else if (getProfileWeight() == DMSConsole.NORMAL) {
                initializeHeavyWeightSensors();
                initializeAllWeightSensors();
            } else if (getProfileWeight() == DMSConsole.HEAVY) {
                initializeAllWeightSensors();
            }
        }
    }

    /**
     * INTERNAL:
     * Api for changing dms weight dynamically.
     */
    public void setProfileWeight(int newWeight) {
        if (newWeight != this.weight) {
            getSession().setIsInProfile(!(newWeight == DMSConsole.NONE));

            // It is necessary to reset the weight to the real weight of NONE to trigger 
            //correct noun creation. This handles the case where the profiler 
            //instance is recreated and the weight is set to the DMSConsole weight 
            //which may differ since it currently does not change at runtime.
            if (getNormalWeightNouns().isEmpty()) {
                weight = DMSConsole.NONE;
            }
            initializeNounTree(newWeight);
            weight = newWeight;
        }
    }

    /**
     * INTERNAL:
     * Initialize TopLink noun tree by default (DMSConsole.getSensorWeight())
     */
    public void initialize() {
        weight = DMSConsole.NONE;
        initializeNounTree(DMSConsole.getSensorWeight());
        weight = DMSConsole.getSensorWeight();
    }

    /**
     * INTERNAL:
     * Return current TopLink dms weight.
     */
    public int getProfileWeight() {
        return weight;
    }

    /**
     * INTERNAL:
     * Link to the dms PhaseEvent api start().
     */
    public void startOperationProfile(String operationName) {
        //due to DMS bug3242994 can't set DMS weight to NONE
        //shortcut for NORMAL weight since no operation profiles are every done for this level.
        if (getProfileWeight() == DMSConsole.NORMAL) {
            return;
        }
        Sensor phaseEvent = getSensorByName(operationName);
        if (phaseEvent != null) {
            Long startToken = new Long(((PhaseEvent)phaseEvent).start());
            getPhaseEventStartToken().put(operationName, startToken);
        }
    }

    /**
     * INTERNAL:
     * Link to the dms PhaseEvent api start().  Intended to be used for query profiling.
     */
    public void startOperationProfile(String operationName, DatabaseQuery query, int weight) {
        //due to DMS bug3242994 can't set DMS weight to NONE
        //shortcut for NORMAL weight since no operation profiles are every done for this level.
        if (getProfileWeight() == DMSConsole.NORMAL) {
            return;
        }
        if (getProfileWeight() < weight) {
            return;
        }
        
        Sensor phaseEvent = getPhaseEventForQuery(operationName, query, weight);        
        if (phaseEvent != null) {
            Long startToken = new Long(((PhaseEvent)phaseEvent).start());
            if (query != null) {
                getPhaseEventStartToken().put(query.getSensorName(operationName, getSessionName()), startToken);
            } else {
                getPhaseEventStartToken().put(operationName, startToken);                
            }
        }
    }

    /**
     * INTERNAL:
     * Link to the dms PhaseEvent api stop().
     */
    public void endOperationProfile(String operationName) {
        //due to DMS bug3242994 can't set DMS weight to NONE
        //shortcut for NORMAL weight since no operation profiles are every done for this level.
        if (getProfileWeight() == DMSConsole.NORMAL) {
            return;
        }
        Sensor phaseEvent = getSensorByName(operationName);
        if (phaseEvent != null) {
            Long startTime = (Long)getPhaseEventStartToken().get(operationName);
            ((PhaseEvent)phaseEvent).stop(startTime.longValue());
        }
    }

    /**
     * INTERNAL:
     * Link to the dms PhaseEvent api stop().  Intended to be used for query profiling.
     */
    public void endOperationProfile(String operationName, DatabaseQuery query, int weight) {
        //due to DMS bug3242994 can't set DMS weight to NONE
        //shortcut for NORMAL weight since no operation profiles are every done for this level.
        if (getProfileWeight() == DMSConsole.NORMAL) {
            return;
        }
        if (getProfileWeight() < weight) {
            return;
        }
        
        Sensor phaseEvent = getPhaseEventForQuery(operationName, query, weight);
        if (phaseEvent != null) {
            Long startTime;
            if (query != null) {
                startTime = (Long)getPhaseEventStartToken().get(query.getSensorName(operationName, getSessionName()));
            } else {
                startTime = (Long)getPhaseEventStartToken().get(operationName);                
            }
            ((PhaseEvent)phaseEvent).stop(startTime.longValue());
        }
    }

    /**
     * INTERNAL:
     * Link to the dms State api update().
     */
    public void update(String operationName, Object value) {
        Sensor state = getSensorByName(operationName);
        if (state != null) {
            ((State)state).update(value);
        }
    }

    /**
     * INTERNAL:
     * Link to the dms Event api occurred().
     */
    public void occurred(String operationName) {
        Sensor event = getSensorByName(operationName);
        if (event != null) {
            ((Event)event).occurred();
        }
    }

    /**
     * INTERNAL:
     * Look for sensor for the name: TopLink_<sessionName>_<domainClass>_<queryClass>_<queryName>(if exist)_<operationName>(if exist).
     * If not found, look for the noun the sensor should be built on.  If the noun is not found, create a new one.  Create the sensor
     * based on the noun.
     */
     protected Sensor getPhaseEventForQuery(String operationName, DatabaseQuery query, int weight) {    
        String sensorName;
        if (query != null) {
            sensorName = query.getSensorName(operationName, getSessionName());
        } else {
            sensorName = operationName;
        }
        
        Sensor phaseEvent = getSensorByName(sensorName);
        
        if (phaseEvent == null) {
            Noun queryNoun;
            if (query != null) {
                String queryNounName = query.getQueryNounName(getSessionName());
                queryNoun = getNounByType(queryNounName, null, DMSConsole.HEAVY);
                if (queryNoun == null) {
                    Noun domainClassNoun = getNounByType(query.getDomainClassNounName(getSessionName()), root, DMSConsole.HEAVY);
                    queryNoun = getNounByType(queryNounName, domainClassNoun, DMSConsole.HEAVY);
                }
                phaseEvent = PhaseEvent.create(queryNoun, sensorName, DMSLocalization.buildMessage("query", new Object[]{sensorName}));
            } else {
                queryNoun = (Noun)getAllWeightNouns().get(SessionProfiler.MiscellaneousNounType);                
                phaseEvent = PhaseEvent.create(queryNoun, sensorName, DMSLocalization.buildMessage("query_misc", new Object[]{sensorName}));
            }
            phaseEvent.deriveMetric(Sensor.all);
            if (weight == DMSConsole.HEAVY) {
                getHeavyWeightSensors().put(sensorName, phaseEvent);    
                getNormalAndHeavyWeightSensors().put(sensorName, phaseEvent);
            } else if (weight == DMSConsole.ALL) {
                getAllWeightSensors().put(sensorName, phaseEvent);       
                getNormalHeavyAndAllWeightSensors().put(sensorName, phaseEvent);
            }                        
        }
        
        return phaseEvent;
     }

    /**
     * INTERNAL:
     * Look for noun based on the given type and weight.  If not found and the parent noun is not null, create a new noun.
     */
    protected Noun getNounByType(String type, Noun parentNoun, int weight) {
        if (getProfileWeight() < weight) {
            return null;
        }
        Noun noun = null;
        Map map = null;
        if (weight == DMSConsole.NORMAL) {
            map = getNormalWeightNouns();
        } else if (weight == DMSConsole.HEAVY) {
            map = getHeavyWeightNouns();
        } else if (weight == DMSConsole.ALL) {
            map = getAllWeightNouns();
        }
        if (map != null) {
            noun = (Noun)map.get(type);
            if (noun == null) {
                if (parentNoun != null) {
                    noun = Noun.create(parentNoun, type, type);                
                    map.put(type, noun);
                }
            }
        }
        
        return noun;
    }

    /**
     * INTERNAL:
     * Return dms sensor which created by pre-defined TopLink metrics.
     */
    protected Sensor getSensorByName(String operationName) {
        Sensor sensor = null;
        if (getProfileWeight() == DMSConsole.NORMAL) {
            sensor = (Sensor)getNormalWeightSensors().get(operationName);
        } else if (getProfileWeight() == DMSConsole.HEAVY) {
            sensor = (Sensor)getNormalAndHeavyWeightSensors().get(operationName);
        } else if (getProfileWeight() == DMSConsole.ALL) {
            sensor = (Sensor)getNormalHeavyAndAllWeightSensors().get(operationName);
        }
        return sensor;
    }

    /**
     * INTERNAL:
     * Create root noun for TopLink dms metrics.
     */
    protected void createRootNoun() {
        root = Noun.create(SessionProfiler.TopLinkRootNoun);
    }

    /**
     * INTERNAL:
     * Build dms NORMAL weight sensors for TopLink dms metrics.
     */
    protected void initializeNormalWeightSensors() {
        createRootNoun();
        Noun sessionNoun = Noun.create(root, "Session" + getSessionName(), SessionProfiler.SessionNounType);

        //SessionName
        State sessionName = State.create(sessionNoun, SessionProfiler.SessionName, "", DMSLocalization.buildMessage("session_name"), (this.getSession().getName() == "") ? "session name not specified" : getSession().getName());

        //LoginTime
        State sessionLoginTime = State.create(sessionNoun, SessionProfiler.LoginTime, "", DMSLocalization.buildMessage("session_login_time"), "not available");
        this.getNormalWeightSensors().put(SessionProfiler.LoginTime, sessionLoginTime);
        this.getNormalWeightNouns().put(SessionProfiler.SessionNounType, sessionNoun);
    }

    /**
     * INTERNAL:
     * Build dms HEAVY weight sensors for TopLink dms metrics.
     */
    protected void initializeHeavyWeightSensors() {
        Noun baseSessionNoun = (Noun)getNormalWeightNouns().get(SessionProfiler.SessionNounType);

        //clientSession
        Event clientSession = Event.create(baseSessionNoun, SessionProfiler.ClientSessionCreated, DMSLocalization.buildMessage("client_session_count"));
        getHeavyWeightSensors().put(SessionProfiler.ClientSessionCreated, clientSession);
        //UnitOfWork
        Event unitOfWork = Event.create(baseSessionNoun, SessionProfiler.UowCreated, DMSLocalization.buildMessage("unitofwork_count"));
        getHeavyWeightSensors().put(SessionProfiler.UowCreated, unitOfWork);

        Noun transactionNoun = Noun.create(root, "Transaction" + getSessionName(), SessionProfiler.TransactionNounType);
        getHeavyWeightNouns().put(SessionProfiler.TransactionNounType, transactionNoun);

        //UowOfWorkCommits
        PhaseEvent uowCommits = PhaseEvent.create(transactionNoun, SessionProfiler.UowCommit, DMSLocalization.buildMessage("unitofwork_commit"));
        uowCommits.deriveMetric(Sensor.all);
        getHeavyWeightSensors().put(SessionProfiler.UowCommit, uowCommits);
        //UnitOfWorkRollbacks
        Event uowRollbacks = Event.create(transactionNoun, SessionProfiler.UowRollbacks, DMSLocalization.buildMessage("unitofwork_rollback"));
        getHeavyWeightSensors().put(SessionProfiler.UowRollbacks, uowRollbacks);
        //OptimisticLocks
        Event optimisticLock = Event.create(transactionNoun, SessionProfiler.OptimisticLockException, DMSLocalization.buildMessage("optimistic_lock"));
        getHeavyWeightSensors().put(SessionProfiler.OptimisticLockException, optimisticLock);

        //RCM noun
        Noun rcmNoun = Noun.create(root, "RCM" + getSessionName(), SessionProfiler.RcmNounType);
        getHeavyWeightNouns().put(SessionProfiler.RcmNounType, rcmNoun);

        //Status
        State rcmStatus = State.create(rcmNoun, SessionProfiler.RcmStatus, "", DMSLocalization.buildMessage("rcm_status"), "not available");
        getHeavyWeightSensors().put(SessionProfiler.RcmStatus, rcmStatus);
        //MessagesReceived
        Event messagesReceived = Event.create(rcmNoun, SessionProfiler.RcmReceived, DMSLocalization.buildMessage("rcm_message_received"));
        getHeavyWeightSensors().put(SessionProfiler.RcmReceived, messagesReceived);
        //MessagesSent
        Event messagesSent = Event.create(rcmNoun, SessionProfiler.RcmSent, DMSLocalization.buildMessage("rcm_message_sent"));
        getHeavyWeightSensors().put(SessionProfiler.RcmSent, messagesSent);
        //RemoteChangeSets
        Event remoteChangeSets = Event.create(rcmNoun, SessionProfiler.RemoteChangeSet, DMSLocalization.buildMessage("remote_change_set"));
        getHeavyWeightSensors().put(SessionProfiler.RemoteChangeSet, remoteChangeSets);

        //connections noun
        Noun connectionsNoun = Noun.create(root, "Connection" + getSessionName(), SessionProfiler.ConnectionNounType);
        getHeavyWeightNouns().put(SessionProfiler.ConnectionNounType, connectionsNoun);
        //ConnectionsInUse
        if (getSession().isServerSession()) {
            Iterator enumtr = ((ServerSession)getSession()).getConnectionPools().keySet().iterator();
            while (enumtr.hasNext()) {
                String poolName = (String)enumtr.next();
                State connectionInUse = State.create(connectionsNoun, SessionProfiler.ConnectionInUse + "(" + poolName + ")", "", DMSLocalization.buildMessage("connection_in_used"), "not available");
                getHeavyWeightSensors().put(poolName, connectionInUse);
            }
        }

        //ConnectionHealth Ping
        PhaseEvent connectionPing = PhaseEvent.create(connectionsNoun, SessionProfiler.ConnectionPing, DMSLocalization.buildMessage("connection_ping"));
        connectionPing.deriveMetric(Sensor.all);
        this.getAllWeightSensors().put(SessionProfiler.ConnectionPing, connectionPing);
        
        //ConnectCalls
        Event tl_connects = Event.create(connectionsNoun, SessionProfiler.TlConnects, DMSLocalization.buildMessage("connect_call"));
        getHeavyWeightSensors().put(SessionProfiler.TlConnects, tl_connects);
        //DisconnectCalls
        Event tl_disconnects = Event.create(connectionsNoun, SessionProfiler.TlDisconnects, DMSLocalization.buildMessage("disconnect_call"));
        getHeavyWeightSensors().put(SessionProfiler.TlDisconnects, tl_disconnects);

        //cache noun
        Noun cacheNoun = Noun.create(root, "Cache" + getSessionName(), SessionProfiler.CacheNounType);
        getHeavyWeightNouns().put(SessionProfiler.CacheNounType, cacheNoun);
        //CacheHits
        Event cacheHits = Event.create(cacheNoun, SessionProfiler.CacheHits, DMSLocalization.buildMessage("cache_hits"));
        getHeavyWeightSensors().put(SessionProfiler.CacheHits, cacheHits);
        //CacheMisses
        Event cacheMisses = Event.create(cacheNoun, SessionProfiler.CacheMisses, DMSLocalization.buildMessage("cache_misses"));
        getHeavyWeightSensors().put(SessionProfiler.CacheMisses, cacheMisses);

        //put in NormalAndHeavyWeightSensors
        getNormalAndHeavyWeightSensors().putAll(getNormalWeightSensors());
        getNormalAndHeavyWeightSensors().putAll(getHeavyWeightSensors());
    }

    /**
     * INTERNAL:
     * Build dms ALL weight sensors for TopLink dms metrics.
     */
    protected void initializeAllWeightSensors() {
        //MergeTime
        Noun baseTransactionNoun = (Noun)getHeavyWeightNouns().get(SessionProfiler.TransactionNounType);
        PhaseEvent mergeTime = PhaseEvent.create(baseTransactionNoun, SessionProfiler.MergeTime, DMSLocalization.buildMessage("merge_time"));
        mergeTime.deriveMetric(Sensor.all);
        this.getAllWeightSensors().put(SessionProfiler.Merge, mergeTime);

        //JTS afterCompletion
        PhaseEvent jtsAferCompletion = PhaseEvent.create(baseTransactionNoun, SessionProfiler.JtsAfterCompletion, DMSLocalization.buildMessage("jts_aftercompletion"));
        jtsAferCompletion.deriveMetric(Sensor.all);
        this.getAllWeightSensors().put(SessionProfiler.JtsAfterCompletion, jtsAferCompletion);

        //JTS beforeCompletion
        PhaseEvent jtsBeforeCompletion = PhaseEvent.create(baseTransactionNoun, SessionProfiler.JtsBeforeCompletion, DMSLocalization.buildMessage("jts_beforecompletion"));
        jtsBeforeCompletion.deriveMetric(Sensor.all);
        this.getAllWeightSensors().put(SessionProfiler.JtsBeforeCompletion, jtsBeforeCompletion);

        //UnitOfWorkRegister
        PhaseEvent uowRegister = PhaseEvent.create(baseTransactionNoun, SessionProfiler.UnitOfWorkRegister, DMSLocalization.buildMessage("unitofwork_register"));
        uowRegister.deriveMetric(Sensor.all);
        this.getAllWeightSensors().put(SessionProfiler.Register, uowRegister);
        //DistributedMerge
        PhaseEvent distributedMerge = PhaseEvent.create(baseTransactionNoun, SessionProfiler.DistributedMergeDmsDisplayName, DMSLocalization.buildMessage("distributed_merge"));
        distributedMerge.deriveMetric(Sensor.all);
        this.getAllWeightSensors().put(SessionProfiler.DistributedMerge, distributedMerge);
        //DeletedObject
        Event deletedObject = Event.create(baseTransactionNoun, SessionProfiler.DeletedObject, DMSLocalization.buildMessage("deleted_object"));
        this.getAllWeightSensors().put(SessionProfiler.DeletedObject, deletedObject);
        
        //assigning sequence numbers
        PhaseEvent sequence = PhaseEvent.create(baseTransactionNoun, SessionProfiler.Sequencing, DMSLocalization.buildMessage("assigning_sequence_numbers"));
        sequence.deriveMetric(Sensor.all);
        this.getAllWeightSensors().put(SessionProfiler.AssignSequence, sequence);

        //Caching
        Noun baseCacheNoun = (Noun)getHeavyWeightNouns().get(SessionProfiler.CacheNounType);
        PhaseEvent cache = PhaseEvent.create(baseCacheNoun, SessionProfiler.Caching, DMSLocalization.buildMessage("caching"));
        cache.deriveMetric(Sensor.all);
        this.getAllWeightSensors().put(SessionProfiler.CACHE, cache);

        //Connection
        Noun baseConnectionNoun = (Noun)getHeavyWeightNouns().get(SessionProfiler.ConnectionNounType);
        PhaseEvent dbConnect = PhaseEvent.create(baseConnectionNoun, SessionProfiler.ConnectionManagement, DMSLocalization.buildMessage("connection"));
        dbConnect.deriveMetric(Sensor.all);
        this.getAllWeightSensors().put(SessionProfiler.CONNECT, dbConnect);

        //rcm
        Noun baseRcmNoun = (Noun)getHeavyWeightNouns().get(SessionProfiler.RcmNounType);

        //ChangeSetsMerged 
        Event changeSetsProcessed = Event.create(baseRcmNoun, SessionProfiler.ChangeSetsProcessed, DMSLocalization.buildMessage("change_set_processed"));
        getAllWeightSensors().put(SessionProfiler.ChangeSetsProcessed, changeSetsProcessed);
        //ChangeSetsNotMerged 
        Event changeSetsNotProcessed = Event.create(baseRcmNoun, SessionProfiler.ChangeSetsNotProcessed, DMSLocalization.buildMessage("change_set_not_processed"));
        getAllWeightSensors().put(SessionProfiler.ChangeSetsNotProcessed, changeSetsNotProcessed);

        //miscellaneous noun
        Noun miscellaneousNoun = Noun.create(root, "Miscellaneous" + getSessionName(), SessionProfiler.MiscellaneousNounType);
        getAllWeightNouns().put(SessionProfiler.MiscellaneousNounType, miscellaneousNoun);

        //Logging
        PhaseEvent logging = PhaseEvent.create(miscellaneousNoun, SessionProfiler.Logging, DMSLocalization.buildMessage("logging"));
        logging.deriveMetric(Sensor.all);
        this.getAllWeightSensors().put(SessionProfiler.Logging, logging);
        //wrapping
        PhaseEvent wrapping = PhaseEvent.create(miscellaneousNoun, SessionProfiler.Wrapping, DMSLocalization.buildMessage("wrapping"));
        wrapping.deriveMetric(Sensor.all);
        this.getAllWeightSensors().put(SessionProfiler.Wrapping, wrapping);
        //DescriptorEvents 
        PhaseEvent descriptorEvent = PhaseEvent.create(miscellaneousNoun, SessionProfiler.DescriptorEvent, DMSLocalization.buildMessage("descriptor_event"));
        descriptorEvent.deriveMetric(Sensor.all);
        this.getAllWeightSensors().put(SessionProfiler.DescriptorEvent, descriptorEvent);

        //SessionEvents 
        PhaseEvent sessionEvent = PhaseEvent.create(miscellaneousNoun, SessionProfiler.SessionEvent, DMSLocalization.buildMessage("session_event"));
        sessionEvent.deriveMetric(Sensor.all);
        this.getAllWeightSensors().put(SessionProfiler.SessionEvent, sessionEvent);

        //put in NormalHeavyAndAllWeightSensors
        getNormalHeavyAndAllWeightSensors().putAll(getNormalAndHeavyWeightSensors());
        getNormalHeavyAndAllWeightSensors().putAll(getAllWeightSensors());
    }

    /**
     * INTERNAL:
     * This method is useful for standalone TopLink application
     * Initialize DMS, should be called once and should be called before any other DMS calls.
     */
    protected void initSpy() {
        try {
            Spy.init("TopLink", null);
        } catch (PublisherError p) {
            getSession().log(SessionLog.WARNING, SessionLog.DMS, "an_error_occured_initializing_dms_listener");
            getSession().logThrowable(SessionLog.WARNING, SessionLog.DMS, p);
            setProfileWeight(DMSConsole.NONE);
        } catch (ConfigurationError c) {
            getSession().log(SessionLog.WARNING, SessionLog.DMS, "an_error_occured_initializing_dms_listener");
            getSession().logThrowable(SessionLog.WARNING, SessionLog.DMS, c);
            setProfileWeight(DMSConsole.NONE);
        }
    }

    /**
     * INTERNAL:
     * Destroy sensors based on dms weight when user changes the weight at runtime.
     */
    protected void destroySensorsByWeight(int weight) {
        Iterator iterator = null;
        if (weight == DMSConsole.HEAVY) {
            iterator = getHeavyWeightSensors().values().iterator();
        } else if (weight == DMSConsole.ALL) {
            iterator = getAllWeightSensors().values().iterator();
        }
        if (iterator != null) {
            while (iterator.hasNext()) {
                ((Sensor)iterator.next()).destroy();
            }
        }
    }

    /**
     * INTERNAL:
     * Destroy nouns based on dms weight when user changes the weight at runtime.
     */
    protected void destroyNounsByWeight(int weight) {
        if (weight == DMSConsole.NORMAL) {
            Iterator iterator = getNormalWeightNouns().values().iterator();
            while (iterator.hasNext()) {
                ((Noun)iterator.next()).destroy();
            }
            getNormalWeightNouns().clear();
            getNormalWeightSensors().clear();
        }
        if (weight == DMSConsole.HEAVY) {
            Iterator iterator = getHeavyWeightNouns().values().iterator();
            while (iterator.hasNext()) {
                ((Noun)iterator.next()).destroy();
            }
            getHeavyWeightNouns().clear();
            destroySensorsByWeight(DMSConsole.HEAVY);
            getNormalAndHeavyWeightSensors().clear();
            getHeavyWeightSensors().clear();
        }
        if (weight == DMSConsole.ALL) {
            Iterator iterator = getAllWeightNouns().values().iterator();
            while (iterator.hasNext()) {
                ((Noun)iterator.next()).destroy();
            }
            getAllWeightNouns().clear();
            destroySensorsByWeight(DMSConsole.ALL);
            getNormalHeavyAndAllWeightSensors().clear();
            getAllWeightSensors().clear();
        }
    }

    protected HashMap getPhaseEventStartToken() {
        if (getOperationStartTokenThreadLocal().get() == null) {
            getOperationStartTokenThreadLocal().set(new HashMap());
        }
        return (HashMap)getOperationStartTokenThreadLocal().get();
    }

    protected Map getNormalWeightSensors() {
        return normalWeightSensors;
    }

    protected Map getHeavyWeightSensors() {
        return heavyWeightSensors;
    }

    protected Map getAllWeightSensors() {
        return allWeightSensors;
    }

    protected Map getNormalAndHeavyWeightSensors() {
        return normalAndHeavyWeightSensors;
    }

    protected Map getNormalHeavyAndAllWeightSensors() {
        return normalHeavyAndAllWeightSensors;
    }

    protected Map getNormalWeightNouns() {
        return normalWeightNouns;
    }

    protected Map getHeavyWeightNouns() {
        return heavyWeightNouns;
    }

    protected Map getAllWeightNouns() {
        return allWeightNouns;
    }

    protected ThreadLocal getOperationStartTokenThreadLocal() {
        return operationStartTokenThreadLocal;
    }

    public AbstractSession getSession() {
        return session;
    }

    public String getSessionName() {
        if (getSession().getName() != "") {
            return "_" + getSession().getName();
        } else {
            return getSession().getName();
        }
    }

    public void setSession(oracle.toplink.sessions.Session session) {
        this.session = (AbstractSession)session;
    }

    public Object profileExecutionOfQuery(DatabaseQuery query, Record row, oracle.toplink.internal.sessions.AbstractSession session) {
        //This is to profile the query execution and no operation name is given
        startOperationProfile(null, query, DMSConsole.HEAVY);
        Object result = null;
        try {
            result = session.internalExecuteQuery(query, (AbstractRecord)row);
        } finally {
            endOperationProfile(null, query, DMSConsole.HEAVY);
        }
        return result;
    }
}