package org.wso2.carbon.esb.connector;

import org.apache.synapse.MessageContext;

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
    private int receiveTimeout;
    private String ciphersuit;
    private Boolean flipRequired;
    private boolean sslEnable;
    private String trustStore;
    private String trustPassword;
    private String keyStore;
    private String keyPassword;

    MQConfiguration(MessageContext msg) {

        if (msg.getProperty(MQConstants.PORT) != null) {
            this.port = Integer.valueOf((String) msg.getProperty(MQConstants.PORT));
        } else {
            this.port = 1414;
        }

        if (msg.getProperty(MQConstants.TOPICNAME) != null) {
            this.topicName = (String) msg.getProperty(MQConstants.TOPICNAME);
        } else {
            this.topicName = null;
        }

        if (msg.getProperty(MQConstants.TOPICSTRING) != null) {
            this.topicString = (String) msg.getProperty(MQConstants.TOPICSTRING);
        } else {
            this.topicString = null;
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

        if (msg.getProperty(MQConstants.TRUST_STORE) != null) {
            this.trustStore = System.getProperty("user.dir") + "/" + (String) msg.getProperty(MQConstants.TRUST_STORE);
        } else {
            this.trustStore = null;
        }

        if (msg.getProperty(MQConstants.TRUST_PASSWORD) != null) {
            this.trustPassword = (String) msg.getProperty(MQConstants.TRUST_PASSWORD);
        } else {
            this.trustPassword = null;
        }

        if (msg.getProperty(MQConstants.KEY_STORE) != null) {
            this.keyStore = System.getProperty("user.dir") + "/" + (String) msg.getProperty(MQConstants.KEY_STORE);
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
            this.transportType = 1; //Default client type
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

        if (msg.getProperty(MQConstants.RECEIVE_TIMEOUT) != null) {
            this.receiveTimeout = Integer.valueOf((String) msg.getProperty(MQConstants.RECEIVE_TIMEOUT));
        } else {
            this.receiveTimeout = 1000;
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

    public int getReceiveTimeout() {
        return receiveTimeout;
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

    public int getTransportType() {
        return transportType;
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
}
