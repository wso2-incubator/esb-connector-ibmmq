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

import com.ibm.mq.MQException;
import com.ibm.mq.MQMessage;
import com.ibm.mq.MQQueue;
import com.ibm.mq.MQQueueManager;
import com.ibm.mq.MQTopic;
import com.ibm.mq.constants.CMQC;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseConstants;
import org.wso2.carbon.connector.core.AbstractConnector;
import org.wso2.carbon.connector.core.ConnectException;
import org.wso2.carbon.esb.connector.IBMQQueueManageConfiguration.IBMMQConfiguration;
import org.wso2.carbon.esb.connector.IBMQQueueManageConfiguration.IBMMQPropertyUtils;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Map;

import static com.ibm.mq.constants.CMQC.MQPER_NOT_PERSISTENT;
import static com.ibm.mq.constants.CMQC.MQPER_PERSISTENT;
import static org.wso2.carbon.esb.connector.IBMMQQueueManagerInitializer.getQueueManager;

/**
 * Add messages to queue
 */
public class IBMMQMessagePublisher extends AbstractConnector {

    /**
     * Connect method which is generating authentication of the connector for each request.
     *
     * @param messageContext ESB messageContext.
     */
    @Override
    public void connect(MessageContext messageContext) throws ConnectException {

        //get payload from messageContext
        SOAPEnvelope soapEnvelope = messageContext.getEnvelope();
        OMElement getElement = soapEnvelope.getBody().getFirstElement();
        String queueMessage = getElement.toString();

        //get queue manager parameters from message context and initialize a connection with IBM WebSphere MQ
        Map<String, String> properties = IBMMQPropertyUtils.getProperties(messageContext);
        IBMMQConfiguration config = new IBMMQConfiguration(properties);

        //initialize the queue and put the message in queue
        try {
            MQQueueManager queueManager = getQueueManager(config);
            if (config.getQueue() != null) {
                MQQueue queue = queueManager.accessQueue(config.getQueue(), CMQC.MQOO_OUTPUT);
                MQMessage mqMessage = buildMQmessage(config, queueMessage);
                queue.put(mqMessage);
                queue.close();
                log.info("Message successfully placed at " + config.getQueue());
            }
            if (config.getTopicName() != null && config.getTopicString() != null) {
                MQTopic publisher = queueManager.accessTopic(config.getTopicString(), config.getTopicName(),
                        CMQC.MQTOPIC_OPEN_AS_PUBLICATION, CMQC.MQOO_OUTPUT);
                MQMessage mqMessage = buildMQmessage(config, queueMessage);
                publisher.put(mqMessage);
                publisher.close();
                log.info("Message successfully placed at " + config.getTopicName());
            }
        } catch (MQException mqe) {
            storeErrorResponseStatus(messageContext, mqe, mqe.reasonCode);
            handleException("Exception in queue", mqe, messageContext);
        } catch (IOException ioe) {
            storeErrorResponseStatus(messageContext, ioe, ioe.hashCode());
            handleException("Exception in queue", ioe, messageContext);
        } catch (CertificateException ce) {
            storeErrorResponseStatus(messageContext, ce, ce.hashCode());
            handleException("Certificate error", ce, messageContext);
        } catch (NoSuchAlgorithmException iae) {
            storeErrorResponseStatus(messageContext, iae, iae.hashCode());
            handleException("Invalid Algorithm", iae, messageContext);
        } catch (UnrecoverableKeyException uke) {
            storeErrorResponseStatus(messageContext, uke, uke.hashCode());
            handleException("Key is unrecoverable", uke, messageContext);
        } catch (KeyStoreException ke) {
            storeErrorResponseStatus(messageContext, ke, ke.hashCode());
            handleException("KeyStore is not valid", ke, messageContext);
        } catch (ClassNotFoundException cne) {
            storeErrorResponseStatus(messageContext, cne, cne.hashCode());
            handleException("Class not found", cne, messageContext);
        } catch (KeyManagementException ikme) {
            storeErrorResponseStatus(messageContext, ikme, ikme.hashCode());
            handleException("KeyManagement is invalid", ikme, messageContext);
        } catch (Exception e) {
            log.error("Error occured in connector", e);
            throw new ConnectException(e);
        }
    }

    /**
     * Create new MQMessage to place on queue
     *
     * @param config       IBMMQConfiguration object for message attributes
     * @param queueMessage The payload of the MessageContext to put in MQMessage
     * @return New MQMessage to put on queue
     */
    private MQMessage buildMQmessage(IBMMQConfiguration config, String queueMessage) throws IOException {

        MQMessage mqMessage = new MQMessage();
        mqMessage.messageType = config.getMessageType();
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(new Date(System.currentTimeMillis()));
        mqMessage.putDateTime = cal;
        if (config.getMessageID() != null) {
            mqMessage.messageId = config.getMessageID().getBytes();
        }
        if (config.getCorrelationID() != null) {
            mqMessage.correlationId = config.getCorrelationID().getBytes();
        }
        if (config.getMessageType() == 4) {
            mqMessage.report = CMQC.MQRO_EXCEPTION_WITH_FULL_DATA | CMQC.MQRO_COA_WITH_FULL_DATA | CMQC.MQRO_COD_WITH_FULL_DATA | CMQC.MQRO_EXPIRATION_WITH_FULL_DATA | CMQC.MQRO_NEW_MSG_ID | CMQC.MQRO_COPY_MSG_ID_TO_CORREL_ID;
        }
        if (config.getReplyToQueueName() != null) {
            mqMessage.replyToQueueName = config.getReplyToQueueName();
        }
        if (config.getReplyToQueueManager() != null) {
            mqMessage.replyToQueueManagerName = config.getReplyToQueueManager();
        }
        if (config.getPriority() != -1000) {
            mqMessage.priority = config.getPriority();
        }
        if (config.getCharSet() > 0) {
            mqMessage.encoding = config.getCharSet();
        }
        if (config.isPersistent()) {
            mqMessage.persistence = MQPER_PERSISTENT;
        } else {
            mqMessage.persistence = MQPER_NOT_PERSISTENT;
        }
        mqMessage.writeString(queueMessage);
        return mqMessage;
    }

    /**
     * Add a message to message context, the message from the throwable is embedded as the Synapse Constant
     * ERROR_MESSAGE.
     *
     * @param ctxt      message context to which the error tags need to be added
     * @param throwable Throwable that needs to be parsed and added
     * @param errorCode errorCode mapped to the exception
     */
    public final void storeErrorResponseStatus(final MessageContext ctxt, final Throwable throwable, final int errorCode) {
        ctxt.setProperty(SynapseConstants.ERROR_CODE, errorCode);
        ctxt.setProperty(SynapseConstants.ERROR_MESSAGE, throwable.getMessage());
        ctxt.setFaultResponse(true);
    }
}