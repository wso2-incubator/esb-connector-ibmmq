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

#### Sample proxy service
```
<?xml version="1.0" encoding="UTF-8"?>
<proxy xmlns="http://ws.apache.org/ns/synapse" name="ibmmqtest"
       transports="https,http" statistics="disable" trace="disable"
       startOnLoad="true">
    <target>
        <inSequence>
            <ibmmq.init>
                <username>{websphere mq user}</username>
                <password>{websphere mq user password}</password>
                <port>1414</port>
                <host>127.0.0.1</host>
                <qmanager>{websphere mq queue manager name}</qmanager>
                <queue>{websphere mq queue name}</queue>
                <channel>{websphere mq channel name}</channel>
                <transportType>1</transportType>
                <timeout>360000</timeOut>
                <maxconnections>75</maxconnections>
                <maxunusedconnections>50</maxunusedconnections>
                <sslEnable>false</sslEnable>
                <ciphersuit>SSL_RSA_WITH_3DES_EDE_CBC_SHA</ciphersuit>
                <flipsRequired>false</flipsRequired>
                <trustStore>null</trustStore>
                <trustPassword>null</trustPassword>
                <keyStore>null</keyStore>
                <keyPassword>null</keyPassword>
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
<proxy xmlns="http://ws.apache.org/ns/synapse" name="ibmmqtest"
       transports="https,http" statistics="disable" trace="disable"
       startOnLoad="true">
    <target>
        <inSequence>
            <ibmmq.init>
                <username>{websphere mq user}</username>
                <password>{websphere mq user password}</password>
                <port>1414</port>
                <host>127.0.0.1</host>
                <qmanager>{websphere mq queue manager name}</qmanager>
                <queue>{websphere mq queue name}</queue>
                <channel>{websphere mq channel name}</channel>
                <transportType>1</transportType>
                <timeout>360000</timeOut>
                <maxconnections>75</maxconnections>
                <maxunusedconnections>50</maxunusedconnections>
                <sslEnable>true</sslEnable>
                <ciphersuit>SSL_RSA_WITH_3DES_EDE_CBC_SHA</ciphersuit>
                <flipsRequired>false</flipsRequired>
                <trustStore>{keystorename.jks}</trustStore>
                <trustPassword>{keystore password}</trustPassword>
                <keyStore>{keystorename.jks}</keyStore>
                <keyPassword>{keystore password}</keyPassword>
            </ibmmq.init>
            <ibmmq.sendmessage/>
            <log level="full"/>
        </inSequence>
    </target>
    <description/>
</proxy>
```
#### Sample proxy service to publish message to topic

```
<?xml version="1.0" encoding="UTF-8"?>
<proxy xmlns="http://ws.apache.org/ns/synapse" name="ibmmqtest"
       transports="https,http" statistics="disable" trace="disable"
       startOnLoad="true">
    <target>
        <inSequence>
            <ibmmq.init>
                <username>{websphere mq user}</username>
                <password>{websphere mq user password}</password>
                <topicname>{websphere mq user topicname}</topicname>
                <topicstring>{websphere mq user topicpassword}</topicstring>
                <port>1414</port>
                <host>127.0.0.1</host>
                <qmanager>{websphere mq queue manager name}</qmanager>
                <channel>{websphere mq channel name}</channel>
                <transportType>1</transportType>
                <timeout>360000</timeOut>
                <maxconnections>75</maxconnections>
                <maxunusedconnections>50</maxunusedconnections>
                <sslEnable>true</sslEnable>
                <ciphersuit>SSL_RSA_WITH_3DES_EDE_CBC_SHA</ciphersuit>
                <flipsRequired>false</flipsRequired>
                <trustStore>{keystorename.jks}</trustStore>
                <trustPassword>{keystore password}</trustPassword>
                <keyStore>{keystorename.jks}</keyStore>
                <keyPassword>{keystore password}</keyPassword>
            </ibmmq.init>
            <ibmmq.publishtopic/>
            <log level="full"/>
        </inSequence>
    </target>
    <description/>
</proxy>
```