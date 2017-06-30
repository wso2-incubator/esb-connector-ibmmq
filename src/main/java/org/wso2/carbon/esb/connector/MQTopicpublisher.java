package org.wso2.carbon.esb.connector;

import com.ibm.mq.*;
import com.ibm.mq.constants.CMQC;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.synapse.MessageContext;
import org.wso2.carbon.connector.core.AbstractConnector;
import org.wso2.carbon.connector.core.ConnectException;

/**
 * Created by hasitha on 6/30/17.
 */
public class MQTopicpublisher extends AbstractConnector {

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

            MQTopic topic = null;

            if (config.getTopicName() != null) {
                topic = setTopic(connectionBuilder, config);
            }

            //create message to write
            MQMessage mqMessage = new MQMessage();
            mqMessage.writeString(queueMessage);
            MQPutMessageOptions pmo = new MQPutMessageOptions();

            if (topic == null) {
                log.info("Cannot write to topic.Error in topic.");
            } else {
                log.info("topic created");
                topic.put(mqMessage, pmo);
                topic.close();
            }

            connectionBuilder.closeConnection();

        } catch (Exception e) {
            log.error("Problem in publishing to topic" + e);
            throw new ConnectException(e);
        }
    }

    MQTopic setTopic(MQConnectionBuilder connectionBuilder, MQConfiguration config) {
        MQQueueManager manager = connectionBuilder.getQueueManager();
        MQTopic publisher;
        try {
            publisher = manager.accessTopic(config.getTopicString(), config.getTopicName(),
                    CMQC.MQTOPIC_OPEN_AS_PUBLICATION, CMQC.MQOO_OUTPUT);
        } catch (MQException e) {
            log.info("No topic exist with the given name.Creating a new topic.." + e);
            return null;

        }
        return publisher;
    }
}
