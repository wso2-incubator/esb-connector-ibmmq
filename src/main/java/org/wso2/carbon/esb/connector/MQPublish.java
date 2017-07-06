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
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.wso2.carbon.connector.core.AbstractConnector;
import org.wso2.carbon.connector.core.ConnectException;

/**
 * Add messages to queue
 */
public class MQPublish extends AbstractConnector {

    @Override
    public void connect(MessageContext messageContext) throws ConnectException {
        try {

            log.info("Message received at ibm-mq connector");

            SOAPEnvelope soapEnvelope = messageContext.getEnvelope();
            OMElement getElement = soapEnvelope.getBody().getFirstElement();
            String queueMessage = "";

            if (getElement != null) {
                queueMessage = getElement.toString();
            }

            MQConnectionBuilder connectionBuilder = new MQConnectionBuilder(messageContext);
            MQConfiguration config = connectionBuilder.getConfig();

            String contentType = (String) ((Axis2MessageContext) messageContext).getAxis2MessageContext().getProperty("ContentType");
            String charsetEncoding = (String) ((Axis2MessageContext) messageContext).getAxis2MessageContext().getProperty("CHARACTER_SET_ENCODING");

            MQQueue queue = null;
            MQTopic topic = null;

            if (config.getQueue() != null) {
                queue = setQueue(connectionBuilder, config);
            }

            if (config.getTopicName() != null) {
                topic = setTopic(connectionBuilder, config);
            }

            MQMessage mqMessage = new MQMessage();
            mqMessage.writeString(queueMessage);
            mqMessage.messageId = config.getMessageID().getBytes();
            mqMessage.correlationId=config.getCorrelationID().getBytes();
            mqMessage.setStringProperty("ContentType", contentType);
            mqMessage.setStringProperty("CHARACTER_SET_ENCODING", charsetEncoding);
            MQPutMessageOptions pmo = new MQPutMessageOptions();

            if (queue == null) {
                log.error("Cannot write to queue.Error in queue.");
            } else {
                log.info("queue initialized");
                queue.put(mqMessage, pmo);
                queue.close();
                log.info("Message sucessfully placed at "+config.getQueue()+"queue");
            }

            if (topic == null) {
                log.info("Cannot write to topic.Error in topic.");
            } else {
                log.info("topic initialized");
                topic.put(mqMessage, pmo);
                topic.close();
                log.info("Message sucessfully placed at "+config.getTopicName()+"topic");
            }

            connectionBuilder.closeConnection();

        } catch (Exception e) {
            log.error("Problem in publishing to queue" + e);
            throw new ConnectException(e);
        }
    }

    /**
     * Iniitialize queue
     */
    MQQueue setQueue(MQConnectionBuilder connectionBuilder, MQConfiguration config) {
        MQQueueManager manager = connectionBuilder.getQueueManager();
        MQQueue queue;
        try {
            queue = manager.accessQueue(config.getQueue(), CMQC.MQOO_OUTPUT);
        } catch (MQException e) {
            log.error("Error creating queue " + e);
            return null;

        }
        return queue;
    }

    /**
     * Initialize queue
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

}