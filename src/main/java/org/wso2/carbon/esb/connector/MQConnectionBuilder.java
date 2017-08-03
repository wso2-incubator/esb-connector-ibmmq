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
import java.util.*;
import java.util.concurrent.*;

/**
 * Start connection with IBM MQ queue manager
 */
public class MQConnectionBuilder {

    private static final Log logger = LogFactory.getLog(MQConnectionBuilder.class);

    private MQQueueManager queueManager;

    private MQConfiguration config;

    MQConnectionBuilder(MessageContext msg) {

        this.config = new MQConfiguration(msg);
        if (config.getCiphersuit().contains("TLS")) {
            Properties props = System.getProperties();
            props.setProperty("com.ibm.mq.cfg.useIBMCipherMappings", "false");
        }
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

        List<String> hostandportList;
        List<String> channelList;

        if (config.getReconnectList() != null) {
            hostandportList = config.getReconnectList();
        } else {
            hostandportList = new ArrayList<>();
        }

        if (config.getChannelList() != null) {
            channelList = config.getChannelList();
        } else {
            channelList = new ArrayList<>();
        }

        hostandportList.add(0, config.getHost() + "/" + config.getPort());
        channelList.add(0, config.getChannel());

        if (queueManager == null || !queueManager.isConnected()) {
            queueManager = getQueueManager(config.getHost(), config.getChannel(), config.getPort());
            if (queueManager == null) {

                long start = System.currentTimeMillis();
                long end = start + config.getReconnectTimeout() * 1000;
                A:
                while (System.currentTimeMillis() < end) {
                    for (String hostandport : hostandportList) {
                        String[] hostandportArray = hostandport.split("/");
                        for (String channel : channelList) {
                            queueManager = getQueueManager(hostandportArray[0], channel, Integer.valueOf(hostandportArray[1]));
                            if (queueManager != null) {
                                break A;
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Initialize queue manager
     */
    public MQQueueManager getQueueManager(String host, String channel, int port) {

        MQEnvironment.hostname = host;
        MQEnvironment.channel = channel;
        MQEnvironment.port = port;

        Future<String> initializeQmanager
                = Executors.newSingleThreadExecutor().submit(() -> {
            try {
                queueManager = new MQQueueManager(config.getqManger());
                return "Initialized";
            } catch (MQException e) {
            }
            return null;
        });
        try {
            initializeQmanager.get(2, TimeUnit.SECONDS);
            logger.info("Queue manager connection established for " + host + " " + port + " " + channel);
        } catch (InterruptedException e) {
            return null;
        } catch (ExecutionException e) {
            return null;
        } catch (TimeoutException e) {
            logger.info("Connection timeout for " + host + " " + channel + " " + port);
            initializeQmanager.cancel(true);
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
