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

import com.ibm.mq.*;
import com.ibm.mq.constants.CMQC;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.synapse.MessageContext;
import org.wso2.carbon.connector.core.AbstractConnector;
import org.wso2.carbon.connector.core.ConnectException;

import java.util.Date;
import java.util.GregorianCalendar;

import static com.ibm.mq.constants.CMQC.*;

/**
 * Add messages to queue
 */
public class MQPublishTopic extends AbstractConnector {

    MQConnectionBuilder connectionBuilder;
    MQConfiguration config;
    MQTopic topic = null;

    @Override
    public void connect(MessageContext messageContext) throws ConnectException {
        try {

            log.info("Message received at ibm-mq connector");
            connectionBuilder = new MQConnectionBuilder(messageContext);
            config = connectionBuilder.getConfig();

            SOAPEnvelope soapEnvelope = messageContext.getEnvelope();
            OMElement getElement = soapEnvelope.getBody().getFirstElement();
            String queueMessage = "";

            if (getElement != null) {
                queueMessage = getElement.toString();
            }

            MQMessage mqMessage= buildMQMessage(config,queueMessage);

            if(config.getTopicName()!=null) {
                topic = setTopic(connectionBuilder, config);
            }

            if (topic == null || mqMessage==null) {
                log.error("Error in publishing message");
            } else {
                log.info("topic initialized");
                topic.put(mqMessage);
                log.info("Message successfully placed at " + config.getTopicName() + "topic");
            }

            connectionBuilder.closeConnection();

        } catch (Exception e) {
            log.error("Problem in publishing to message" + e);
            throw new ConnectException(e);
        }
    }

    /**
     * Initialize topic
     */
    MQTopic setTopic(MQConnectionBuilder connectionBuilder, MQConfiguration config) {
        MQQueueManager manager = connectionBuilder.getQueueManager();
        MQTopic publisher;
        try {
            publisher = manager.accessTopic(config.getTopicString(), config.getTopicName(),
                    CMQC.MQTOPIC_OPEN_AS_PUBLICATION, CMQC.MQOO_OUTPUT);
        } catch (MQException e) {
            log.error("Error creating topic" + e);
            return null;
        }
        return publisher;
    }

    /**
     * Create MQMessage
     */
    MQMessage buildMQMessage(MQConfiguration config, String queueMessage) {

        MQMessage mqMessage = new MQMessage();
        mqMessage.messageId = config.getMessageID().getBytes();
        mqMessage.correlationId = config.getCorrelationID().getBytes();
        if (config.getPriority() != -1000) {
            mqMessage.priority = config.getPriority();
        }
        int messageType = config.getMessageType();
        mqMessage.messageType = messageType;

        if (config.isPersistent()) {
            mqMessage.persistence = MQPER_PERSISTENT;
        } else {
            mqMessage.persistence = MQPER_NOT_PERSISTENT;
        }
        if (config.getgroupID() != null) {
            mqMessage.groupId = config.getgroupID().getBytes();
        }

        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(new Date(System.currentTimeMillis()));
        mqMessage.putDateTime = cal;

        try {
            mqMessage.writeString(queueMessage);
        } catch (Exception e) {
            log.info("Error creating mq message" + e);
        }
        return mqMessage;
    }
}