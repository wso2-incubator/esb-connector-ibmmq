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
                <topicname>mytopic</topicname>
                <topicstring>topic</topicstring>
                <transportType>1</transportType>
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
                <ciphersuit>SSL_RSA_WITH_3DES_EDE_CBC_SHA</ciphersuit>
                <flipsRequired>false</flipsRequired>
                <trustStore>wso2carbon.jks</trustStore>
                <trustPassword>wso2carbon</trustPassword>
                <keyStore>wso2carbon.jks</keyStore>
                <keyPassword>wso2carbon</keyPassword>
            </ibmmq.init>
            <ibmmq.sendmessage/>
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
                <topicname>mytopic</topicname>
                <topicstring>topic</topicstring>
                <transportType>1</transportType>
                <messageType>MQMT_DATAGRAM(or MQMT_REPLY)</messageType>
                <persistent>true</persistent>
                <priority>3</priority>>
                <maxconnections>75</maxconnections>
                <maxunusedconnections>50</maxunusedconnections>
                <timeout>3600000</timeout>
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
            <ibmmq.sendmessage/>
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
                <topicname>mytopic</topicname>
                <topicstring>topic</topicstring>
                <transportType>1</transportType>
                <messageType>MQMT_REQUEST</messageType>
                <persistent>true</persistent>
                <priority>3</priority>>
                <maxconnections>75</maxconnections>
                <maxunusedconnections>50</maxunusedconnections>
                <timeout>3600000</timeout>
                <replyTimeout>5</replyTimeout>
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
            <ibmmq.sendmessage/>
        </inSequence>
    </target>
    <description/>
</proxy>

```