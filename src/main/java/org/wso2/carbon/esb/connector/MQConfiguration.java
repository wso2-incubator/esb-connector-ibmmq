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

import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.ibm.mq.constants.CMQC.*;

/**
 * IBM MQ configuration parameters
 */
public class MQConfiguration {

    private final int port;
    private final String host;
    private String qManger;
    private String topicName;
    private String topicString;
    private String queue;
    private String channel;
    private String userName;
    private String password;
    private int transportType;
    private long timeout;
    private int maxconnections;
    private int maxunusedconnections;
    private String ciphersuit;
    private boolean flipRequired;
    private boolean sslEnable;
    private String trustStore;
    private String trustPassword;
    private String keyStore;
    private String keyPassword;
    private String messageID;
    private String correlationID;
    private String groupID;
    private boolean persistent;
    private int messageType = MQMT_DATAGRAM;
    private String replyQueue;
    private int priority;
    private int replyTimeout;
    private boolean islistenerEnabled;
    private List<String> reconnectList = new ArrayList();
    private int reconnectTimeout;
    private List<String> channelList = new ArrayList<>();

    MQConfiguration(MessageContext msg) {

        if (msg.getProperty(MQConstants.PORT) != null) {
            this.port = Integer.valueOf((String) msg.getProperty(MQConstants.PORT));
        } else {
            this.port = 1414;
        }

        if (msg.getProperty(MQConstants.TOPIC_NAME) != null) {
            this.topicName = (String) msg.getProperty(MQConstants.TOPIC_NAME);
        } else {
            this.topicName = null;
        }

        String messageTypeString = (String) msg.getProperty(MQConstants.MESSAGE_TYPE);
        if (messageTypeString != null) {
            if (messageTypeString.equals("MQMT_REQUEST")) {
                this.messageType = MQMT_REQUEST;
            } else if (messageTypeString.equals("MQMT_DATAGRAM")) {
                this.messageType = MQMT_DATAGRAM;
            } else if (messageTypeString.equals("MQMT_REPORT")) {
                this.messageType = MQMT_REPORT;
            } else if (messageTypeString.equals("MQMT_REPLY")) {
                this.messageType = MQMT_REPLY;
            }
        }

        if (msg.getProperty(MQConstants.REPLY_MESSAGE_QUEUE) != null) {
            this.replyQueue = (String) msg.getProperty(MQConstants.REPLY_MESSAGE_QUEUE);
        } else {
            this.replyQueue = null;
        }

        if (msg.getProperty(MQConstants.CONNECTION_NAMELIST) != null) {
            Arrays.asList(((String) msg.getProperty(MQConstants.CONNECTION_NAMELIST)).split(",")).forEach(item -> reconnectList.add(item));
        } else {
            reconnectList = null;
        }

        if (msg.getProperty(MQConstants.TOPIC_STRING) != null) {
            this.topicString = (String) msg.getProperty(MQConstants.TOPIC_STRING);
        } else {
            this.topicString = null;
        }

        if (msg.getProperty(MQConstants.CONNECTION_CHANNELLIST) != null) {
            Arrays.asList(((String) msg.getProperty(MQConstants.CONNECTION_CHANNELLIST)).split(",")).forEach(item -> channelList.add(item));
        } else {
            channelList = null;
        }

        if (msg.getProperty(MQConstants.SSL_ENABLE) != null) {
            boolean sslProps = Boolean.valueOf((String) msg.getProperty(MQConstants.SSL_ENABLE));
            if (sslProps) {
                this.sslEnable = true;
            } else {
                this.sslEnable = false;
            }
        } else {
            this.sslEnable = false;
        }

        if (msg.getProperty(MQConstants.LISTENER_ENABLED) != null) {
            islistenerEnabled = Boolean.valueOf((String) msg.getProperty(MQConstants.SSL_ENABLE));
        } else {
            islistenerEnabled = false;
        }

        if (msg.getProperty(MQConstants.TIMEOUT) != null) {
            this.timeout = Long.valueOf((String) msg.getProperty(MQConstants.TIMEOUT));
        } else {
            this.timeout = 3600000;
        }

        if (msg.getProperty(MQConstants.REPLY_TIMEOUT) != null) {
            this.replyTimeout = Integer.valueOf((String) msg.getProperty(MQConstants.REPLY_TIMEOUT));
        } else {
            this.replyTimeout = 1;
        }

        if (msg.getProperty(MQConstants.RECONNECT_TIMEOUT) != null) {
            this.reconnectTimeout = Integer.valueOf((String) msg.getProperty(MQConstants.RECONNECT_TIMEOUT));
        } else {
            this.reconnectTimeout = 5;
        }

        if (msg.getProperty(MQConstants.MAX_CONNECTIONS) != null) {
            this.maxconnections = Integer.valueOf((String) msg.getProperty(MQConstants.MAX_CONNECTIONS));
        } else {
            this.maxconnections = 75;
        }

        if (msg.getProperty(MQConstants.PRIORITY) != null) {
            this.priority = Integer.valueOf((String) msg.getProperty(MQConstants.PRIORITY));
        } else {
            this.priority = -1000;
        }

        if (msg.getProperty(MQConstants.MAX_UNUSED_CONNECTIONS) != null) {
            this.maxunusedconnections = Integer.valueOf((String) msg.getProperty(MQConstants.CIPHERSUIT));
        } else {
            this.maxunusedconnections = 50;
        }

        if (msg.getProperty(MQConstants.CIPHERSUIT) != null) {
            this.ciphersuit = (String) msg.getProperty(MQConstants.CIPHERSUIT);
        } else {
            this.ciphersuit = "SSL_RSA_WITH_3DES_EDE_CBC_SHA";
        }

        if (msg.getProperty(MQConstants.FLIP_REQUIRED) != null) {
            this.flipRequired = Boolean.valueOf((String) msg.getProperty(MQConstants.FLIP_REQUIRED));
        } else {
            this.flipRequired = false;
        }

        if (msg.getProperty(MQConstants.PERSISTENT) != null) {
            this.persistent = Boolean.valueOf((String) msg.getProperty(MQConstants.PERSISTENT));
        } else {
            this.persistent = false;
        }

        if (msg.getProperty(MQConstants.TRUST_STORE) != null) {
            this.trustStore = System.getProperty("user.dir") + "/repository/resources/security/" + (String) msg.getProperty(MQConstants.TRUST_STORE);
        } else {
            this.trustStore = null;
        }

        if (msg.getProperty(MQConstants.TRUST_PASSWORD) != null) {
            this.trustPassword = (String) msg.getProperty(MQConstants.TRUST_PASSWORD);
        } else {
            this.trustPassword = null;
        }

        if (msg.getProperty(MQConstants.MESSAGE_ID) != null) {
            this.messageID = (String) msg.getProperty(MQConstants.MESSAGE_ID);
        } else {
            this.messageID = ((Axis2MessageContext) msg).getAxis2MessageContext().getMessageID();
        }

        if (msg.getProperty(MQConstants.CORRELATION_ID) != null) {
            this.correlationID = (String) msg.getProperty(MQConstants.CORRELATION_ID);
        } else {
            this.correlationID = ((Axis2MessageContext) msg).getAxis2MessageContext().getLogCorrelationID();
        }

        if (msg.getProperty(MQConstants.GROUP_ID) != null) {
            this.groupID = (String) msg.getProperty(MQConstants.GROUP_ID);
        } else {
            this.groupID = null;
        }

        if (msg.getProperty(MQConstants.KEY_STORE) != null) {
            this.keyStore = System.getProperty("user.dir") + "/repository/resources/security/" + (String) msg.getProperty(MQConstants.KEY_STORE);
        } else {
            this.keyStore = null;
        }

        if (msg.getProperty(MQConstants.KEY_PASSWORD) != null) {
            this.keyPassword = (String) msg.getProperty(MQConstants.KEY_PASSWORD);
        } else {
            this.keyPassword = null;
        }

        if (msg.getProperty(MQConstants.HOST) != null) {
            this.host = (String) msg.getProperty(MQConstants.HOST);
        } else {
            this.host = "localhost";
        }

        if (msg.getProperty(MQConstants.TRANSPORT_TYPE) != null) {
            this.transportType = Integer.valueOf((String) msg.getProperty(MQConstants.TRANSPORT_TYPE));
        } else {
            this.transportType = 1;
        }

        if (msg.getProperty(MQConstants.QMANAGER) != null) {
            this.qManger = (String) msg.getProperty(MQConstants.QMANAGER);
        } else {
            this.qManger = null;
        }

        if (msg.getProperty(MQConstants.QUEUE) != null) {
            this.queue = (String) msg.getProperty(MQConstants.QUEUE);
        } else {
            this.queue = null;
        }

        if (msg.getProperty(MQConstants.CHANNEL) != null) {
            this.channel = (String) msg.getProperty(MQConstants.CHANNEL);
        } else {
            this.channel = null;
        }

        if (msg.getProperty(MQConstants.USERNAME) != null) {
            this.userName = (String) msg.getProperty(MQConstants.USERNAME);
        } else {
            this.userName = null;
        }

        if (msg.getProperty(MQConstants.PASSWORD) != null) {
            this.password = (String) msg.getProperty(MQConstants.PASSWORD);
        } else {
            this.password = null;
        }
    }

    public int getmaxConnections() {
        return maxconnections;
    }

    public int getmaxnusedConnections() {
        return maxunusedconnections;
    }

    public long getTimeout() {
        return timeout;
    }

    public int getReplyTimeout() {
        return replyTimeout;
    }

    public String getCiphersuit() {
        return ciphersuit;
    }

    public Boolean getFlipRequired() {
        return flipRequired;
    }

    public boolean isSslEnable() {
        return sslEnable;
    }

    public String getTrustStore() {
        return trustStore;
    }

    public String getTrustPassword() {
        return trustPassword;
    }

    public String getKeyStore() {
        return keyStore;
    }

    public String getKeyPassword() {
        return keyPassword;
    }

    public String getTopicName() {
        return topicName;
    }

    public String getTopicString() {
        return topicString;
    }

    public int getPort() {
        return port;
    }

    public String getHost() {
        return host;
    }

    public String getqManger() {
        return qManger;
    }

    public String getQueue() {
        return queue;
    }

    public String getChannel() {
        return channel;
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }

    public String getMessageID() {
        return messageID;
    }

    public String getCorrelationID() {
        return correlationID;
    }

    public String getgroupID() {
        return groupID;
    }

    public boolean isPersistent() {
        return persistent;
    }

    public int getMessageType() {
        return messageType;
    }

    public String getreplyQueue() {
        return replyQueue;
    }

    public int getPriority() {
        return priority;
    }

    public List<String> getReconnectList() {
        return reconnectList;
    }

    public int getReconnectTimeout() {
        return reconnectTimeout;
    }

    public List<String> getChannelList() {
        return channelList;
    }

    public boolean isIslistenerEnabled() {
        return islistenerEnabled;
    }

}
