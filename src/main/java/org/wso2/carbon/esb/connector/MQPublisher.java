/*
*  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

/**
 * Sample method implementation.
 */
public class MQPublisher extends AbstractConnector {

    @Override
    public void connect(MessageContext messageContext) throws ConnectException {
        try {

            log.info("Message received at ibm-mq connector");

            SOAPEnvelope soapEnvelope = messageContext.getEnvelope();
            OMElement getElement = soapEnvelope.getBody().getFirstElement();
            String queueMessage = "";

            if (getElement != null) {
                queueMessage = getElement.getText();
            }

            MQConnectionBuilder connectionBuilder = new MQConnectionBuilder(messageContext);
            MQConfiguration config = connectionBuilder.getConfig();

            MQQueue queue = null;
            MQTopic topic = null;

            if (config.getTopicName() != null) {
                topic = setTopic(connectionBuilder, config);
            }
            if (config.getQueue() != null) {
                queue = setQueue(connectionBuilder, config);
            }

            //create message to write
            MQMessage mqMessage = new MQMessage();
            mqMessage.writeString(queueMessage);
            MQPutMessageOptions pmo = new MQPutMessageOptions();

            //execute queue and topic commands
            if (queue == null) {
                log.error("Cannot write to queue.Error in queue.");
            } else {
                log.info("queue created");
                queue.put(mqMessage, pmo);
                queue.close();
            }

            if (topic == null) {
                log.info("Cannot write to topic.Error in topic.");
            } else {
                log.info("topic created");
                topic.put(mqMessage, pmo);
                topic.close();
            }
            connectionBuilder.closeConnection();
        } catch (Exception e) {
            log.error("Problem in IBMMQProducer" + e);
            throw new ConnectException(e);
        }
    }

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

    MQTopic setTopic(MQConnectionBuilder connectionBuilder, MQConfiguration config) {
        MQQueueManager manager = connectionBuilder.getQueueManager();
        MQTopic publisher;
        try {
            publisher = manager.accessTopic(config.getTopicString(), config.getTopicName(),
                    CMQC.MQTOPIC_OPEN_AS_PUBLICATION, CMQC.MQOO_OUTPUT);
        } catch (MQException e) {
            log.info("No topic exist with the given name.Creating a new topic.."+e);
            return null;

        }
        return publisher;
    }
}