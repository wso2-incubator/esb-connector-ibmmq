# esb-connector-ibmmq

### Steps for testing the connector

1. Build the project using <b>mvn clean install -Dmaven.test.skip=true</b><br>
2. Copy the following three jars at <b>{basedir}/src/main/resources/lib/</b>  to <b>$CARBON_HOME/repository/components/lib</b>

* com.ibm.mq.allclient.jar
* providerutil.jar
* fscontext.jar
3. Upload the connector to wso2 esb through management console<br>
4. Enable the connector
5. Write a proxy service for testing the connector
6. For ssl import the certificate to the wso2carbon.jks using following command.
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

1. username - Username of the IBM MQ user group
2. password - Password of the IBM MQ user group
3. port - Port allowing IBM MQ for TCP/IP connections
4. qmanager - Name of the IBM MQ queue manager
5. channel - Name of the IBM MQ remote channel
6. queue - Name of the queue
7. messageType 
    * [MQMT_REPLY](https://www.ibm.com/support/knowledgecenter/en/SSFKSJ_7.5.0/com.ibm.mq.dev.doc/q022870_.htm#q022870___Reply_messages)-(2) Use a reply message when you reply to another message.
    * [MQMT_DATAGRAM](https://www.ibm.com/support/knowledgecenter/en/SSFKSJ_7.5.0/com.ibm.mq.dev.doc/q022870_.htm#q022870___Datagrams)-(8) Use a datagram when you do not require a reply from the application that receives the message (that is, gets the message from the queue).
    * [MQMT_REQUEST](https://www.ibm.com/support/knowledgecenter/en/SSFKSJ_7.5.0/com.ibm.mq.dev.doc/q022870_.htm#q022870___Request_messages)-(1)-Use a request message when you want a reply from the application that receives the message.
    * [MQMT_REPORT](https://www.ibm.com/support/knowledgecenter/en/SSFKSJ_7.5.0/com.ibm.mq.dev.doc/q022870_.htm#q022870___Report_messages)-(4)-Report messages inform applications about events such as the occurrence of an error when processing a message.
8. [persistent](https://www.ibm.com/support/knowledgecenter/en/SSFKSJ_8.0.0/com.ibm.mq.dev.doc/q023070_.htm) - If a queue manager is restarted after a failure, it recovers these persistent messages as necessary from the logged data. Messages that are not persistent are discarded if a queue manager stops, whether the stoppage is as a result of an operator command or because of the failure of some part of your system.
9. [priority](https://www.ibm.com/support/knowledgecenter/en/SSFKSJ_7.5.0/com.ibm.mq.dev.doc/q022910_.htm) - You can set a numeric value for the priority, or you can let the message take the default priority of the queue.The MsgDeliverySequence attribute of the queue determines whether messages on the queue are stored in FIFO (first in, first out) sequence, or in FIFO within priority sequence. If this attribute is set to MQMDS_PRIORITY, messages are enqueued with the priority specified in the Priority field of their message descriptors; but if it is set to MQMDS_FIFO, messages are enqueued with the default priority of the queue. Messages of equal priority are stored on the queue in order of arrival.
10. maxconnections - number of maximum connections managed by the customized [connection pool](https://www.ibm.com/support/knowledgecenter/en/SSFKSJ_7.5.0/com.ibm.mq.dev.doc/q031110_.htm) for ibm mq connections 
11. timeout - Ends connections that are not used for this time in customized connection pool for ibm mq connections
12. sslenabled - whether or not the ssl connection is needed or not (true/false)
13. ciphersuit - cipher suit specification for ibm mq connections.For further understanding refer [here](https://www.ibm.com/support/knowledgecenter/en/SSFKSJ_7.5.0/com.ibm.mq.dev.doc/q031290_.htm)Note that IBM MQ versions below 8.0.0.3 does not support many cipher specs.Update the IBM MQ using fix packs as mentioned in [this](http://www-01.ibm.com/support/docview.wss?uid=swg27006037) tutorial. 
14. trustStore - wso2carbon.jks
15. trustpassword - wso2carbon
16. keyStore - wso2carbon.jks
17. keyPassword - wso2carbon
18. [correlationID](https://www.ibm.com/support/knowledgecenter/en/SSFKSJ_7.5.0/com.ibm.mq.dev.doc/q033280_.htm#q033280___s1)-The CorrelationId to be included in the MQMD of a message when put on a queue. Also the ID to be matched against when getting a message from a queue.
19. [messageID](https://www.ibm.com/support/knowledgecenter/en/SSFKSJ_7.5.0/com.ibm.mq.dev.doc/q033280_.htm#q033280___s1)-The MessageId to be included in the MQMD of a message when put on a queue. Also the ID to be matched against when getting a message from a queue.Its initial value is all nulls.
20. [groupID](https://www.ibm.com/support/knowledgecenter/en/SSFKSJ_7.5.0/com.ibm.mq.dev.doc/q033280_.htm#q033280___s1)-This is a byte string that is used to identify the particular message group or logical message to which the physical message belongs.
21. connectionNamelist - Reconnection parameters in case of connection failure.Add the list of hosts and ports here to connector to retry for the connections.
22. reconnectionTimeout - Reconnection parameters in case of connection failure .Add reconnection timeout for the reconnection.

#### Basic flow chart of the connector operation

![finaldiagram](https://user-images.githubusercontent.com/11781930/28656285-ab6723aa-72be-11e7-81b4-fa5d66f8ac51.png)

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
                <connectionNamelist>12.0.0.1/1414,127.0.0.1/1414</connectionNamelist>
                <reconnectTimeout>10000</reconnectTimeout>
                <messageType>8(MQMT_DATAGRAM see the parameter description)</messageType>
                <persistent>true</persistent>
                <priority>3</priority>>
                <maxconnections>75</maxconnections>
                <timeout>3600</timeout>
                <messageID>MessageID@IBMMQ123</messageID>
                <correlationID>CorrelationID@IBMMQ123</correlationID>
                <groupID>GroupID@IBMMQ123</groupID>
                <sslEnable>false</sslEnable>
            </ibmmq.init>
            <ibmmq.queue/>
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
                <connectionNamelist>12.0.0.1/1414,127.0.0.1/1414</connectionNamelist>
                <reconnectTimeout>10000</reconnectTimeout>
                <messageType>8(MQMT_DATAGRAM see the parameter description)</messageType>
                <persistent>true</persistent>
                <priority>3</priority>>
                <maxconnections>75</maxconnections>
                <timeout>3600</timeout>
                <messageID>MessageID@IBMMQ123</messageID>
                <correlationID>CorrelationID@IBMMQ123</correlationID>
                <groupID>GroupID@IBMMQ123</groupID>
                <sslEnable>true</sslEnable>
                <ciphersuit>SSL_RSA_WITH_3DES_EDE_CBC_SHA</ciphersuit>
                <flipsRequired>false</flipsRequired>
                <trustStore>wso2carbon.jks</trustStore>
                <trustPassword>wso2carbon</trustPassword>
                <keyStore>wso2carbon.jks</keyStore>
                <keyPassword>wso2carbon</keyPassword>
            </ibmmq.init>
            <ibmmq.queue/>
        </inSequence>
    </target>
    <description/>
</proxy>
```