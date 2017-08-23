# esb-connector-ibmmq

### Steps for testing the connector

1. Build the project using <b>mvn clean install -Dmaven.test.skip=true</b><br>
2. Copy the following three jars to <b>$CARBON_HOME/repository/components/lib</b>

* com.ibm.mq.allclient.jar
* providerutil.jar
* fscontext.jar
3. Upload the connector to wso2 esb through management console<br>
4. Enable the connector
5. Write a proxy service for testing the connector
6. For ssl import your certificate to the wso2carbon.jks (wso2 keystore) using following command.
```
keytool -importcert -file <certificate file> -keystore <ESB>/repository/resources/security/wso2carbon.jks -alias "ibmwebspheremqqmanager"
```
#### SSL CipherSpecs and CipherSuites

Following cipher suites tested with the given fips configuration.(Some weak cipher suites are no longer supported in IBM websphere version 8.0.0.x)

CipherSpec  | Equivalent CipherSuite (Oracle JRE)|FipsRequired
------------- | ------------- | ------------- 
TLS_RSA_WITH_AES_128_CBC_SHA  | TLS_RSA_WITH_AES_128_CBC_SHA | False
TLS_RSA_WITH_3DES_EDE_CBC_SHA  | SSL_RSA_WITH_3DES_EDE_CBC_SHA |False
TLS_RSA_WITH_AES_128_CBC_SHA256  | TLS_RSA_WITH_AES_128_CBC_SHA256 |False
TLS_RSA_WITH_AES_128_CBC_SHA  | TLS_RSA_WITH_AES_128_CBC_SHA |False
TLS_RSA_WITH_AES_256_CBC_SHA   | TLS_RSA_WITH_AES_256_CBC_SHA |False
TLS_RSA_WITH_AES_256_CBC_SHA256  | TLS_RSA_WITH_AES_256_CBC_SHA256 |False

#### Description of the parameters

1. username - Username of the IBM MQ user group.
2. password - Password of the IBM MQ user group.
3. port - Port allowing IBM MQ for TCP/IP connections.
4. queueManager - Name of the IBM MQ queue manager.
5. channel - Name of the IBM MQ remote channel.
6. queue - Name of the queue.
7. messageType 
    * [MQMT_REPLY](https://www.ibm.com/support/knowledgecenter/en/SSFKSJ_7.5.0/com.ibm.mq.dev.doc/q022870_.htm#q022870___Reply_messages)-(2) Use a reply message when you reply to another message.
    * [MQMT_DATAGRAM](https://www.ibm.com/support/knowledgecenter/en/SSFKSJ_7.5.0/com.ibm.mq.dev.doc/q022870_.htm#q022870___Datagrams)-(8) Use a datagram when you do not require a reply from the application that receives the message (that is, gets the message from the queue).
    * [MQMT_REQUEST](https://www.ibm.com/support/knowledgecenter/en/SSFKSJ_7.5.0/com.ibm.mq.dev.doc/q022870_.htm#q022870___Request_messages)-(1)-Use a request message when you want a reply from the application that receives the message.
    * [MQMT_REPORT](https://www.ibm.com/support/knowledgecenter/en/SSFKSJ_7.5.0/com.ibm.mq.dev.doc/q022870_.htm#q022870___Report_messages)-(4)-Report messages inform applications about events such as the occurrence of an error when processing a message.
8. [persistent](https://www.ibm.com/support/knowledgecenter/en/SSFKSJ_8.0.0/com.ibm.mq.dev.doc/q023070_.htm) - If a queue manager is restarted after a failure, it recovers these persistent messages as necessary from the logged data. Messages that are not persistent are discarded if a queue manager stops, whether the stoppage is as a result of an operator command or because of the failure of some part of your system.
9. [priority](https://www.ibm.com/support/knowledgecenter/en/SSFKSJ_7.5.0/com.ibm.mq.dev.doc/q022910_.htm) - You can set a numeric value for the priority, or you can let the message take the default priority of the queue.The MsgDeliverySequence attribute of the queue determines whether messages on the queue are stored in FIFO (first in, first out) sequence, or in FIFO within priority sequence. If this attribute is set to MQMDS_PRIORITY, messages are enqueued with the priority specified in the Priority field of their message descriptors; but if it is set to MQMDS_FIFO, messages are enqueued with the default priority of the queue. Messages of equal priority are stored on the queue in order of arrival.
10. producerType - Whether the connector publish messages to a [queue](https://www.ibm.com/support/knowledgecenter/en/SSFKSJ_8.0.0/com.ibm.mq.explorer.doc/e_queues.htm) or a [topic](https://www.ibm.com/support/knowledgecenter/en/SSFKSJ_8.0.0/com.ibm.mq.pro.doc/q004990_.htm).
11. maxConnections - number of maximum connections managed by the customized [connection pool](https://www.ibm.com/support/knowledgecenter/en/SSFKSJ_7.5.0/com.ibm.mq.dev.doc/q031110_.htm) for ibm mq connections.
12. maxUnusedConnections - Number of maximum unused connections managed by the customized [connection pool](https://www.ibm.com/support/knowledgecenter/en/SSFKSJ_7.5.0/com.ibm.mq.dev.doc/q031110_.htm).
13. timeout - Ends connections that are not used for this time in customized connection pool for ibm mq connections.
14. sslEnable - whether or not the ssl connection is needed (true/false).
15. cipherSuite - cipher suit specification for ibm mq connections.For further understanding refer [here](https://www.ibm.com/support/knowledgecenter/en/SSFKSJ_7.5.0/com.ibm.mq.dev.doc/q031290_.htm)Note that IBM MQ versions below 8.0.0.3 does not support many cipher specs.Update the IBM MQ using fix packs as mentioned in [this](http://www-01.ibm.com/support/docview.wss?uid=swg27006037) tutorial. 
16. trustStore - wso2carbon.jks
17. trustPassword - wso2carbon
18. keyStore - wso2carbon.jks
19. keyPassword - wso2carbon
20. [correlationID](https://www.ibm.com/support/knowledgecenter/en/SSFKSJ_7.5.0/com.ibm.mq.dev.doc/q033280_.htm#q033280___s1)-The CorrelationId to be included in the MQMD of a message when put on a queue. Also the ID to be matched against when getting a message from a queue.
21. [messageID](https://www.ibm.com/support/knowledgecenter/en/SSFKSJ_7.5.0/com.ibm.mq.dev.doc/q033280_.htm#q033280___s1)-The MessageId to be included in the MQMD of a message when put on a queue. Also the ID to be matched against when getting a message from a queue.Its initial value is all nulls.
22. connectionNameList - Reconnection parameters in case of connection failure.Add the list of hosts and ports here to connector to retry for the connections.
23. reconnectionTimeout - Reconnection parameters in case of connection failure .Add reconnection timeout for the reconnection.
24. topicName - Name of the topic as initialized in the queue manager.
25. [topicString](https://www.ibm.com/support/knowledgecenter/SSFKSJ_8.0.0/com.ibm.mq.pro.doc/q005000_.htm) topicString attribute as initialized in the queue manager.

#### Important Notes

* The username and the password parameters must be provided in order to obtain the necessary permissions to access the queue manager.
* The channel and the queueManager parameters should be provided with the correct configuration of sslEnable parameter to establish a successful connection.
* If the host and the port parameters not provided the connector will attempt to establish a connnection through the host "localhost" and port "1414".
* If the messageType parameter not provided the connector will use the default message type MQMT_DATAGRAM when publishing messages.
* If the timeout,the maxConnections and the maxUnusedConnections parameters not specified the default values of 3600,75 and 50 will be used.(3600s - 1Hr).
* The two timeout parameters(timeout and reconnectionTimeout) should be provided in seconds.
* If the producerType is "topic" topicString and topicName parameters should be provided.
* If the producerType is "queue" queue parameter should be provided.

#### Sample proxy service without ssl
```
<?xml version="1.0" encoding="UTF-8"?>
<proxy xmlns="http://ws.apache.org/ns/synapse"
       name="ibmmqtest"
       startOnLoad="true"
       statistics="disable"
       trace="disable"
       transports="http,https">
    <target>
        <inSequence>
            <ibmmq.init>
                <username>mqm</username>
                <password>upgs5423</password>
                <port>1414</port>
                <host>127.0.0.1</host>
                <qmanager>qmanager</qmanager>
                <channel>PASSWORD.SVRCONN</channel>
                <queue>myqueue</queue>
                <producerType>queue</producerType>
                <connectionNamelist>12.0.0.1/1414,127.0.0.1/1414</connectionNamelist>
                <reconnectTimeout>10000</reconnectTimeout>
                <messageType>8(MQMT_DATAGRAM see the parameter description)</messageType>
                <persistent>true</persistent>
                <priority>3</priority>>
                <maxconnections>75</maxconnections>
                <timeout>3600</timeout>
                <sslEnable>false</sslEnable>
            </ibmmq.init>
            <ibmmq.producer/>
        </inSequence>
    </target>
    <description/>
</proxy>

```
#### Sample proxy service with ssl

```
<?xml version="1.0" encoding="UTF-8"?>
<proxy xmlns="http://ws.apache.org/ns/synapse"
       name="ibmmqtest"
       startOnLoad="true"
       statistics="disable"
       trace="disable"
       transports="http,https">
    <target>
        <inSequence>
            <ibmmq.init>
                <username>mqm</username>
                <password>upgs5423</password>
                <port>1414</port>
                <host>127.0.0.1</host>
                <qmanager>qmanager</qmanager>
                <channel>PASSWORD.SVRCONN</channel>
                <queue>myqueue</queue>
                <producerType>queue</producerType>
                <connectionNamelist>12.0.0.1/1414,127.0.0.1/1414</connectionNamelist>
                <reconnectTimeout>10000</reconnectTimeout>
                <messageType>8(MQMT_DATAGRAM see the parameter description)</messageType>
                <persistent>true</persistent>
                <priority>3</priority>>
                <maxconnections>75</maxconnections>
                <timeout>3600</timeout>
                <sslEnable>true</sslEnable>
                <ciphersuit>SSL_RSA_WITH_3DES_EDE_CBC_SHA</ciphersuit>
                <flipsRequired>false</flipsRequired>
                <trustStore>wso2carbon.jks</trustStore>
                <trustPassword>wso2carbon</trustPassword>
                <keyStore>wso2carbon.jks</keyStore>
                <keyPassword>wso2carbon</keyPassword>
            </ibmmq.init>
            <ibmmq.producer/>
        </inSequence>
    </target>
    <description/>
</proxy>
```
#### Sample proxy service without ssl for topic
```
<?xml version="1.0" encoding="UTF-8"?>
<proxy xmlns="http://ws.apache.org/ns/synapse"
       name="ibmmqtest"
       startOnLoad="true"
       statistics="disable"
       trace="disable"
       transports="http,https">
   <target>
      <inSequence>
         <ibmmq.init>
            <username>mqm</username>
            <password>upgs5423</password>
            <port>1414</port>
            <host>12.0.0.1</host>
            <qmanager>qmanager</qmanager>
            <connectionNamelist>12.0.0.1/1414,127.0.0.1/1414</connectionNamelist>
            <reconnectTimeout>1000</reconnectTimeout>
            <messageType>8</messageType>
            <channel>PASSWORD.SVRCONN</channel>
            <producerType>topic</producerType>
            <topicName>mytopic</topicname>
            <topicString>topic</topicstring>
            <transportType>1</transportType>
            <timeout>3600</timeout>
         </ibmmq.init>
         <ibmmq.producer/>
      </inSequence>
   </target>
   <description/>
</proxy>
```
#### Sample proxy service with ssl for topic
```
<?xml version="1.0" encoding="UTF-8"?>
<proxy xmlns="http://ws.apache.org/ns/synapse"
       name="ibmmqtest"
       startOnLoad="true"
       statistics="disable"
       trace="disable"
       transports="http,https">
   <target>
      <inSequence>
         <ibmmq.init>
            <username>mqm</username>
            <password>upgs5423</password>
            <port>1414</port>
            <host>12.0.0.1</host>
            <qmanager>qmanager</qmanager>
            <connectionNamelist>12.0.0.1/1414,127.0.0.1/1414</connectionNamelist>
            <reconnectTimeout>1000</reconnectTimeout>
            <messageType>8</messageType>
            <channel>PASSWORD.SVRCONN</channel>
            <producerType>topic</producerType>
            <topicName>mytopic</topicname>
            <topicString>topic</topicstring>
            <transportType>1</transportType>
            <timeout>3600</timeout>
            <sslEnable>true</sslEnable>
            <ciphersuit>SSL_RSA_WITH_3DES_EDE_CBC_SHA</ciphersuit>
            <flipsRequired>false</flipsRequired>
            <trustStore>wso2carbon.jks</trustStore>
            <trustPassword>wso2carbon</trustPassword>
            <keyStore>wso2carbon.jks</keyStore>
            <keyPassword>wso2carbon</keyPassword>
         </ibmmq.init>
         <ibmmq.producer/>
      </inSequence>
   </target>
   <description/>
</proxy>
```
