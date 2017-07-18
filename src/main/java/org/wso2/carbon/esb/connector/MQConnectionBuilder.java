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
package org.wso2.carbon.esb.connector;

import com.ibm.mq.MQEnvironment;
import com.ibm.mq.MQException;
import com.ibm.mq.MQQueueManager;
import com.ibm.mq.MQSimpleConnectionManager;
import com.ibm.mq.constants.CMQC;
import com.ibm.mq.constants.CMQXC;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.util.Collection;
import java.util.Stack;
import java.util.Vector;
import java.util.concurrent.*;

/**
 * Start connection with IBM MQ queue manager
 */
public class MQConnectionBuilder {

    private static final Log logger = LogFactory.getLog(MQConnectionBuilder.class);

    private static MQQueueManager queueManager = null;

    private MQConfiguration config;

    MQConnectionBuilder(MessageContext msg) {

        this.config = new MQConfiguration(msg);

        if (config.isSslEnable()) {
            MQEnvironment.sslCipherSuite = config.getCiphersuit();
            MQEnvironment.sslSocketFactory = createSSLContext().getSocketFactory();
            MQEnvironment.sslCipherSuite = config.getCiphersuit();
            MQEnvironment.sslFipsRequired = config.getFlipRequired();
        }

        MQEnvironment.properties.put(CMQC.TRANSPORT_PROPERTY, CMQC.TRANSPORT_MQSERIES_CLIENT);
        MQEnvironment.properties.put(CMQC.USER_ID_PROPERTY, config.getUserName());
        MQEnvironment.properties.put(CMQC.PASSWORD_PROPERTY, config.getPassword());
        Collection headerComp = new Vector();
        headerComp.add(new Integer(CMQXC.MQCOMPRESS_SYSTEM));
        MQEnvironment.hdrCompList = headerComp;
        MQEnvironment.setDefaultConnectionManager(customizedPool(config.getTimeout(), config.getmaxConnections(), config.getmaxnusedConnections()));

        Stack<String> hostandport;
        Stack<String> channelList;

        if (config.getReconnectList() != null) {
            hostandport = config.getReconnectList();
        } else {
            hostandport = new Stack();
        }
        if (config.getChannelList() != null) {
            channelList = config.getChannelList();
        } else {
            channelList = new Stack();
        }

        hostandport.push(config.getHost() + "/" + config.getPort());
        channelList.push(config.getChannel());

        try {
            if (queueManager == null || !queueManager.isConnected()) {
                queueManager = getQueueManager(config.getHost(), config.getChannel(), config.getPort());
                if (queueManager == null) {

                    long start = System.currentTimeMillis();
                    long end = start + config.getReconnectTimeout() * 1000;

                    boolean isConnected = false;
                    boolean isTimeout = false;
                    String channel = "";

                    for (int i = 0; !hostandport.empty(); i++) {
                        String hostport[] = hostandport.pop().split("/");
                        Stack<String> dupChannelList = channelList;
                        for (int j = 0; j < dupChannelList.size(); j++) {
                            try {
                                logger.info("Trying to reconnect using host " + hostport[0] + ",port " + hostport[1] + " and channel " + channelList.peek());
                                channel = dupChannelList.pop();
                                queueManager = getQueueManager(hostport[0], channel, Integer.valueOf(hostport[1]));
                                if (queueManager != null) {
                                    isConnected = true;
                                    break;
                                }
                            } catch (MQException e1) {
                                logger.info("Reconnecting");
                            }

                            if (System.currentTimeMillis() > end) {
                                isTimeout = true;
                                break;
                            }
                        }
                        if (isConnected) {
                            logger.info("Queue Manager connected for " + hostport[0] + " " + hostport[1] + " " + channel);
                            break;
                        }
                        if (isTimeout) {
                            logger.info("Reconnection timed out");
                            break;
                        }

                    }
                }
            }
        } catch (MQException e) {
            logger.info("Error initializing queue manager");
        }
    }

    /**
     * Initialize queue manager
     */
    public MQQueueManager getQueueManager(String host, String channel, int port) throws MQException {

        MQEnvironment.hostname = host;
        MQEnvironment.channel = channel;
        MQEnvironment.port = port;

        Future<String> control
                = Executors.newSingleThreadExecutor().submit(new Callable<String>() {
            @Override
            public String call() throws Exception {
                queueManager = new MQQueueManager(config.getqManger());
                return "Initialized";
            }
        });

        try {
            control.get(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            logger.info("Connection timeout for " + host + " " + channel + " " + port);
            control.cancel(true);
            return null;
        }
        return queueManager;
    }

    /**
     * Get queue manager
     */
    public MQQueueManager getManager() {
        if (queueManager == null || !queueManager.isConnected()) {
            try {
                queueManager = new MQQueueManager(config.getqManger());
            } catch (MQException e) {
            }
        }
        return queueManager;
    }

    /**
     * Terminate connection with queue manager
     */
    public void closeConnection() {
        try {
            if (queueManager.isConnected()) {
                queueManager.close();
                queueManager = null;
            }
        } catch (MQException e) {

        }
    }

    /**
     * Setup customized connection pool for caching
     */
    MQSimpleConnectionManager customizedPool(long timeout, int maxConnections, int maxunusedConnections) {
        MQSimpleConnectionManager customizedPool = new MQSimpleConnectionManager();
        customizedPool.setActive(MQSimpleConnectionManager.MODE_AUTO);
        customizedPool.setTimeout(timeout);
        customizedPool.setMaxConnections(maxConnections);
        customizedPool.setMaxUnusedConnections(maxunusedConnections);
        return customizedPool;
    }

    public MQConfiguration getConfig() {
        return config;
    }

    /**
     * Creating SSLContext for ssl connection
     */
    public SSLContext createSSLContext() {
        try {
            Class.forName("com.sun.net.ssl.internal.ssl.Provider");

            KeyStore ks = KeyStore.getInstance("JKS");
            ks.load(new FileInputStream(config.getKeyStore()), config.getKeyPassword().toCharArray());

            KeyStore trustStore = KeyStore.getInstance("JKS");
            trustStore.load(new FileInputStream(config.getTrustStore()), config.getTrustPassword().toCharArray());

            TrustManagerFactory trustManagerFactory =
                    TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());

            KeyManagerFactory keyManagerFactory =
                    KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());

            trustManagerFactory.init(trustStore);
            keyManagerFactory.init(ks, config.getKeyPassword().toCharArray());

            SSLContext sslContext = SSLContext.getInstance("SSLv3");

            sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(),
                    null);

            return sslContext;
        } catch (Exception e) {
            return null;
        }
    }
}
