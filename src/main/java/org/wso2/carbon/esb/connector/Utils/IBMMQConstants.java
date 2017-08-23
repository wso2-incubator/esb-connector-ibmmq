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
package org.wso2.carbon.esb.connector.Utils;

/**
 * IBM MQ constants
 */
public class IBMMQConstants {

    /**
     * Username for IBM WebSphere MQ user group
     */
    public static final String USERNAME = "uri.var.username";

    /**
     * Use this property to specify the password of the user specified
     * by the value typed in the Username property
     */
    public static final String PASSWORD = "uri.var.password";

    /**
     * Topic String for the topic
     */
    public static final String TOPIC_STRING = "uri.var.topicString";

    /**
     * Topic name for publish messages
     */
    public static final String TOPIC_NAME = "uri.var.topicName";

    /**
     * Port allowing IBM MQ for TCP/IP connections
     */
    public static final String PORT = "uri.var.port";

    /**
     * The host name of the QueueManager to use.
     */
    public static final String HOST = "uri.var.host";

    /**
     * Ends connections that are not used for this time in customized
     * connection pool for ibm mq connections
     *
     * @see http://www.ibm.com/support/knowledgecenter/en/SSFKSJ_7.5.0/com.ibm.mq.dev.doc/q031110_.htm
     */
    public static final String TIMEOUT = "uri.var.timeout";

    /**
     * number of maximum connections managed by the customized connection
     * pool for ibm mq connections
     *
     * @see http://www.ibm.com/support/knowledgecenter/en/SSFKSJ_7.5.0/com.ibm.mq.dev.doc/q031110_.htm
     */
    public static final String MAX_CONNECTIONS = "uri.var.maxConnections";

    /**
     * the number of mamximum unused connections in the customized connection
     * pool for ibm mq connections
     *
     * @see http://www.ibm.com/support/knowledgecenter/en/SSFKSJ_7.5.0/com.ibm.mq.dev.doc/q031110_.htm
     */
    public static final String MAX_UNUSED_CONNECTIONS = "uri.var.maxUnusedConnections";

    /**
     * Name of the queue manager
     */
    public static final String QMANAGER = "uri.var.queueMmanager";

    /**
     * Name of the queue which the messages need to be placed
     */
    public static final String QUEUE = "uri.var.queue";

    /**
     * The name of the client connection channel through which messages are
     * sent from the connector to the remote queue manager.
     */
    public static final String CHANNEL = "uri.var.channel";

    /**
     * Whether to use a local binding or client/server TCP binding
     */
    public static final String TRANSPORT_TYPE = "uri.var.transportType";

    /**
     * cipher suit specification for ibm mq connections.Note that IBM MQ versions
     * below 8.0.0.3 does not support many cipher specs.Update the IBM MQ using fix packs.
     *
     * @see http://www.ibm.com/support/knowledgecenter/en/SSFKSJ_7.5.0/com.ibm.mq.dev.doc/q031290_.htm
     * @see http://www-01.ibm.com/support/docview.wss?uid=swg27006037
     */
    public static final String CIPHERSUIT = "uri.var.cipherSuite";

    /**
     * Specify whether you want to enable FIPS support for an agent
     */
    public static final String FIPS_REQUIRED = "uri.var.fipsRequired";

    /**
     * whether or not the ssl connection is needed or not (true/false)
     */
    public static final String SSL_ENABLE = "uri.var.sslEnable";

    /**
     * Name of the truststore.Use the wso2 keystore after importing the certificates.
     */
    public static final String TRUST_STORE = "uri.var.trustStore";

    /**
     * truststore password
     */
    public static final String TRUST_PASSWORD = "uri.var.trustPassword";

    /**
     * Name of the keystore.Use the wso2 keystore after importing the certificates.
     */
    public static final String KEY_STORE = "uri.var.keyStore";

    /**
     * keystore password
     */
    public static final String KEY_PASSWORD = "uri.var.keyPassword";

    /**
     * Use the properties in this group to specify the message identifier for messages.
     */
    public static final String MESSAGE_ID = "uri.var.messageID";

    /**
     * Use the properties in this group to specify the correlation identifier for messages.
     */
    public static final String CORRELATION_ID = "uri.var.correlationID";

    /**
     * If a queue manager is restarted after a failure, it recovers these persistent
     * messages as necessary from the logged data.
     */
    public static final String PERSISTENT = "uri.var.persistent";

    /**
     * You can set a numeric value for the priority, or you can let the message
     * take the default priority of the queue.
     */
    public static final String PRIORITY = "uri.var.priority";

    /**
     * Type of the message from MQMT_DATAGRAM,MQMT_REPLY,MQMT_REQUEST and MQMT_REPORT
     */
    public static final String MESSAGE_TYPE = "uri.var.messageType";

    /**
     * Reconnection parameters in case of connection failure.Add the list of hosts
     * and ports here to connector to retry for the connections.
     */
    public static final String CONNECTION_NAMELIST = "uri.var.connectionNameList";

    /**
     * Reconnection parameters in case of connection failure .Add reconnection
     * timeout for the reconnection.
     */
    public static final String RECONNECT_TIMEOUT = "uri.var.reconnectTimeout";

    /**
     * If set, this property overrides the coded character set property
     * of the destination queue or topic.
     */
    public static final String CHARACTER_SET = "uri.var.charSet";

    /**
     * whether the producer is publishing messages to a queue or a topic
     */
    public static final String PRODUCER_TYPE = "uri.var.producerType";

    /**
     * Integer constant to identify the message priority and charset
     */
    public static final int INTEGER_CONST = -1;
}
