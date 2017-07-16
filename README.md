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
* keytool -importcert -file <certificate file> -keystore <ESB>/repository/resources/security/wso2carbon.jks -alias "ibmwebspheremqqmanager"

#### Description of the parameters

1. username - Username of the IBM MQ user group
2. password - Password of the IBM MQ user group
3. port - Port allowing IBM MQ for TCP/IP connections
4. qmanager - Name of the IBM MQ queue manager
5. channel - Name of the IBM MQ remote channel
6. queue - Name of the queue
7. messageType - MQMT_REPLY,MQMT_DATAGRAM,MQMT_REQUEST,MQMT_REPORT
8. persistent - true/false whether the message need to be logged and stored or not
9. priority - defines a ten-level priority value with 0 as the lowest priority and 9 as the highest.
10. maxconnections - number of maximum connections managed by the connection pool
11. maxunusedconnections - the number of mamximum unused connections in the pool
12. timeout - Ends connections that are not used for "timeout" time
13. sslenabled - true/false
14. ciphersuit - cipher suit for the connection
15. trustStore - wso2carbon.jks
16. trustpassword - wso2carbon
17. keyStore - wso2carbon.jks
18. keyPassword - wso2carbon
19. correlationID/messageID/groupID - IDs for link messages
20. accessMode - Exclusive/Shared whether the queue operations can initialize in parallel or sequential pattern
21. replyQueue - The queue which the reply message or the report message should dispatch if the message type is MQMT_REQUEST or MQMT_REPORT
22. replyTimeout - Timeout for listener of the replyQueue 

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
                <messageType>MQMT_DATAGRAM(or MQMT_REPLY)</messageType>
                <persistent>true</persistent>
                <priority>3</priority>>
                <maxconnections>75</maxconnections>
                <maxunusedconnections>50</maxunusedconnections>
                <timeout>3600000</timeout>
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
                <messageType>MQMT_DATAGRAM(or MQMT_REPLY)</messageType>
                <persistent>true</persistent>
                <priority>3</priority>>
                <maxconnections>75</maxconnections>
                <maxunusedconnections>50</maxunusedconnections>
                <timeout>3600000</timeout>
                <messageID>MessageID@IBMMQ123</messageID>
                <accessMode>Exclusive</accessMode>
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

#### Sample proxy service for request or a report message
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
                <messageType>MQMT_REQUEST</messageType>
                <persistent>true</persistent>
                <priority>3</priority>>
                <maxconnections>75</maxconnections>
                <maxunusedconnections>50</maxunusedconnections>
                <timeout>3600000</timeout>
                <replyTimeout>5</replyTimeout>
                <accessMode>Exclusive</accessMode>
                <messageID>MessageID@IBMMQ123</messageID>
                <correlationID>CorrelationID@IBMMQ123</correlationID>
                <groupID>GroupID@IBMMQ123</groupID>
                <replyQueue>test</replyQueue>
                <sslEnable>false</sslEnable>
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
#### Sample proxy service for publish to topic
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
                <topicname>mytopic</topicname>
                <topicstring>topic</topicstring>
                <messageType>MQMT_REQUEST</messageType>
                <persistent>true</persistent>
                <priority>3</priority>>
                <maxconnections>75</maxconnections>
                <maxunusedconnections>50</maxunusedconnections>
                <timeout>3600000</timeout>
                <accessMode>Exclusive</accessMode>
                <messageID>MessageID@IBMMQ123</messageID>
                <correlationID>CorrelationID@IBMMQ123</correlationID>
                <groupID>GroupID@IBMMQ123</groupID>
                <sslEnable>false</sslEnable>
                <ciphersuit>SSL_RSA_WITH_3DES_EDE_CBC_SHA</ciphersuit>
                <flipsRequired>false</flipsRequired>
                <trustStore>wso2carbon.jks</trustStore>
                <trustPassword>wso2carbon</trustPassword>
                <keyStore>wso2carbon.jks</keyStore>
                <keyPassword>wso2carbon</keyPassword>
            </ibmmq.init>
            <ibmmq.topic/>
        </inSequence>
    </target>
    <description/>
</proxy>

```