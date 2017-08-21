package org.wso2.carbon.esb.connector.Utils;

import com.ibm.mq.MQException;
import com.ibm.mq.MQQueueManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Class to handle connection pool for IBM MQ queue managers
 */
public class IBMMQManageConnectionPool {

    private static final Log logger = LogFactory.getLog(IBMMQManageConnectionPool.class);
    //Global queue manager connection pool
    private static Map<String, Object[]> connectionPool = new HashMap<>();
    private static IBMMQManageConnectionPool poolObject = null;

    /**
     * This method returns the global poolObject
     *
     * @return IBMMQManageConnectionPoolObject
     */
    public synchronized static IBMMQManageConnectionPool getInstance() {
        if (poolObject == null) {
            startPoolCleaner();
            poolObject = new IBMMQManageConnectionPool();
        }
        return poolObject;
    }

    /**
     * This method clean the pool
     */
    private static void startPoolCleaner() {
        final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public synchronized void run() {
                for (Map.Entry<String, Object[]> entry : connectionPool.entrySet()) {
                    Object[] data = entry.getValue();
                    long timeout = Long.valueOf(data[1] + "");
                    long lastModified = Long.valueOf(data[2] + "");
                    if ((timeout < (System.currentTimeMillis() - lastModified)) || !((MQQueueManager) data[0]).isConnected()) {
                        String key = entry.getKey();
                        disconnectQueueManager(((MQQueueManager) connectionPool.get(key)[0]));
                        connectionPool.remove(key);
                        logger.debug(entry.getKey().split("/")[0] + " queue manager removed from connection pool");
                    }
                }
            }
        }, 0, 5, TimeUnit.SECONDS);
    }

    /**
     * Terminate the connection with queue manager.
     *
     * @param queueManager queue manager to close connection.
     */
    public static void disconnectQueueManager(MQQueueManager queueManager) {
        try {
            if (queueManager.isConnected()) {
                queueManager.disconnect();
                logger.info("Queue manager disconnected");
            }
        } catch (MQException e) {
            logger.error("Fail to disconnect queue manger" + e);
        }
    }

    /**
     * This method insert the QueueManagers to connection pool which are
     * not available for reuse
     *
     * @param queueManager new queue manager instance
     * @param config       IBMMQConfiguration object for get the connection parameters
     */
    public synchronized void InsertNewConnection(MQQueueManager queueManager, IBMMQConfiguration config) {
        if (config.getMaxConnections() > connectionPool.size()) {
            Object managerData[] = {queueManager, config.getTimeout() + "", System.currentTimeMillis() + "", System.currentTimeMillis() + ""};
            connectionPool.put(config.getqManger() + "/" + config.getCipherSuit(), managerData);
        } else {
            for (Map.Entry<String, Object[]> entry : connectionPool.entrySet()) {
                Object[] data = entry.getValue();
                long timeout = Long.valueOf(data[1] + "");
                long lastModified = Long.valueOf(data[2] + "");
                if ((timeout < (System.currentTimeMillis() - lastModified)) || !((MQQueueManager) data[0]).isConnected()) {
                    String key = entry.getKey();
                    disconnectQueueManager(((MQQueueManager) connectionPool.get(key)[0]));
                    connectionPool.remove(key);
                    Object managerData[] = {queueManager, config.getTimeout() + "", System.currentTimeMillis() + "", System.currentTimeMillis() + ""};
                    connectionPool.put(config.getqManger(), managerData);
                    break;
                }
            }
        }
    }

    /**
     * This method returns queue manager object if available in the connection pool
     *
     * @param config IBMMQConfiguration object for get the connection parameters
     */
    public synchronized MQQueueManager PooledQueueManager(IBMMQConfiguration config) {
        if (connectionPool.containsKey(config.getqManger() + "/" + config.getCipherSuit())) {
            Object[] data = connectionPool.get(config.getqManger() + "/" + config.getCipherSuit());
            MQQueueManager queueManager = (MQQueueManager) data[0];
            if (queueManager.isConnected()) {
                data[2] = System.currentTimeMillis();
                return queueManager;
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Queue manager not available in connection pool");
        }
        return null;
    }
}
