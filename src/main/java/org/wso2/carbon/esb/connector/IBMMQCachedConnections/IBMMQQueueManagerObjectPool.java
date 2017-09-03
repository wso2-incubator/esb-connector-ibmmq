/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.esb.connector.IBMMQCachedConnections;

import com.ibm.mq.MQException;
import com.ibm.mq.MQQueueManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.esb.connector.IBMQQueueManageConfiguration.IBMMQConfiguration;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Class for get the IBM MQ Queue Manager objects from pool
 */
public class IBMMQQueueManagerObjectPool implements Runnable {

    private static final Log logger = LogFactory.getLog(IBMMQQueueManagerObjectPool.class);
    //global IBMMQQueueManagerObjectPool object
    private static IBMMQQueueManagerObjectPool cachedConnections;
    //global cached queue manager list
    private ConcurrentHashMap<String, ConcurrentHashMap<String, Object[]>> mqQueueManagerList;

    /**
     * Private constructor to make the class singleton and start the cached pool operations
     */
    private IBMMQQueueManagerObjectPool() {
        this.mqQueueManagerList = new ConcurrentHashMap<>();
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(this, 0, 5, TimeUnit.MINUTES);
    }

    /**
     * Method to return the instance.
     *
     * @return singleton instance of the class
     */
    public static IBMMQQueueManagerObjectPool getInstance() {
        if (cachedConnections == null) {
            cachedConnections = new IBMMQQueueManagerObjectPool();
        }
        return cachedConnections;
    }

    /**
     * Ends the connection with queue manager.
     *
     * @param queueManager queue manager to close connection.
     */
    public static void disconnectQueueManager(MQQueueManager queueManager) {
        try {
            if (queueManager.isConnected()) {
                queueManager.disconnect();
                logger.info(queueManager.getName() + " Queue manager disconnected");
            }
        } catch (MQException e) {
            logger.error("Fail to disconnect queue manger" + e);
        }
    }

    /**
     * This method puts new queue managers to the pool
     *
     * @param configuration IBMMQConfiguration parameter to identify the keys and values
     * @return queue manager object if exist in the pool else null
     */
    public MQQueueManager getCachedQueueManager(IBMMQConfiguration configuration) {
        String key = configuration.getTimeout() + "/" + configuration.getMaxConnections();
        if (mqQueueManagerList.containsKey(key)) {
            String poolKey = configuration.getQueueManager() + configuration.getHost() + configuration.getPort() + configuration.getChannel() + (configuration.getCipherSuit() == null ? "" : configuration.getCipherSuit());
            if (mqQueueManagerList.get(key).containsKey(poolKey)) {
                Object[] data = mqQueueManagerList.get(key).get(poolKey);
                MQQueueManager queueManager = (MQQueueManager) data[0];
                if (!queueManager.isConnected()) {
                    //queue manager disconnected
                    mqQueueManagerList.get(key).remove(poolKey);
                    return null;
                } else {
                    data[1] = System.currentTimeMillis();
                    return queueManager;
                }
            }
            //configuration doesn't exist in the pool
            return null;
        }
        //pool not exist
        return null;
    }

    /**
     * This method puts new queue managers to the pool
     *
     * @param configuration IBMMQConfiguration parameter to identify the keys and values
     * @param queueManager  New queue manager to insert
     */
    public void insertQueueManager(IBMMQConfiguration configuration, MQQueueManager queueManager) {
        String key = configuration.getTimeout() + "/" + configuration.getMaxConnections();
        int maxConnections = Integer.parseInt(key.split("/")[1]);
        String newKey = configuration.getQueueManager() + configuration.getHost() + configuration.getPort() + configuration.getChannel() + (configuration.getCipherSuit() == null ? "" : configuration.getCipherSuit());
        if (mqQueueManagerList.containsKey(key)) {
            ConcurrentHashMap<String, Object[]> pool = mqQueueManagerList.get(key);
            if (pool.size() >= maxConnections) {
                this.cleanupKeyInMap(key);
                if (pool.size() < maxConnections) {
                    long lastModifiedTime = System.currentTimeMillis();
                    Object data[] = {queueManager, lastModifiedTime};
                    pool.put(newKey, data);
                }
            } else {
                long lastModifiedTime = System.currentTimeMillis();
                Object data[] = {queueManager, lastModifiedTime};
                pool.put(newKey, data);
            }
        } else {
            ConcurrentHashMap<String, Object[]> newQueueManager = new ConcurrentHashMap<>();
            long lastModifiedTime = System.currentTimeMillis();
            Object data[] = {queueManager, lastModifiedTime};
            newQueueManager.put(newKey, data);
            mqQueueManagerList.put(key, newQueueManager);
        }
    }

    /**
     * Method to clean up the cached connection pool
     */
    private void cleanupMap() {
        for (Map.Entry<String, ConcurrentHashMap<String, Object[]>> entry : mqQueueManagerList.entrySet()) {
            ConcurrentHashMap<String, Object[]> pool = entry.getValue();
            long poolTimeout = Long.parseLong(entry.getKey().split("/")[0]);
            for (Map.Entry<String, Object[]> queueManagerEntry : pool.entrySet()) {
                Object data[] = queueManagerEntry.getValue();
                long currentTime = System.currentTimeMillis();
                if ((currentTime - Long.parseLong(data[1].toString())) > poolTimeout) {
                    disconnectQueueManager(((MQQueueManager) data[0]));
                    if (logger.isDebugEnabled()) {
                        logger.debug("Queue manager removed");
                    }
                    pool.remove(queueManagerEntry.getKey());
                } else if (!((MQQueueManager) data[0]).isConnected()) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Queue manager removed");
                    }
                    pool.remove(queueManagerEntry.getKey());
                }
            }
        }
    }

    /**
     * Method to clean up the cached connection pool specified by the key
     *
     * @param key key to check and remove
     */
    private void cleanupKeyInMap(String key) {
        ConcurrentHashMap<String, Object[]> pool = mqQueueManagerList.get(key);
        long poolTimeout = Long.parseLong(key.split("/")[0]);
        for (Map.Entry<String, Object[]> queueManagerEntry : pool.entrySet()) {
            Object data[] = queueManagerEntry.getValue();
            long currentTime = System.currentTimeMillis();
            if ((currentTime - Long.parseLong(data[1].toString())) > poolTimeout) {
                disconnectQueueManager(((MQQueueManager) data[0]));
                pool.remove(queueManagerEntry.getKey());
            } else if (!((MQQueueManager) data[0]).isConnected()) {
                pool.remove(queueManagerEntry.getKey());
            }
        }
    }

    @Override
    public void run() {
        this.cleanupMap();
    }
}