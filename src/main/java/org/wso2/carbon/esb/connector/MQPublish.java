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
import com.ibm.mq.constants.MQConstants;
import com.ibm.mq.headers.MQMD;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axis2.AxisFault;
import org.apache.axis2.builder.Builder;
import org.apache.axis2.builder.BuilderUtil;
import org.apache.axis2.builder.SOAPBuilder;
import org.apache.axis2.transport.TransportUtils;
import org.apache.commons.io.input.AutoCloseInputStream;
import org.apache.commons.lang.StringUtils;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.wso2.carbon.connector.core.AbstractConnector;
import org.wso2.carbon.connector.core.ConnectException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.ibm.mq.constants.CMQC.*;

/**
 * Add messages to queue
 */
public class MQPublish extends AbstractConnector {

    MQConnectionBuilder connectionBuilder;
    MQConfiguration config;
    String qname = "";
    String topicname = "";
    MQQueue queue = null;
    MQTopic topic = null;

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

            connectionBuilder = new MQConnectionBuilder(messageContext);
            config = connectionBuilder.getConfig();

            String newqueue = config.getQueue();
            String newtopic = config.getTopicName();

            if (newqueue != null) {
                if (!newqueue.equals(qname) && queue != null) {
                    queue.close();
                    queue = setQueue(connectionBuilder, config, 0);
                } else if (!newqueue.equals(qname) || queue == null) {
                    queue = setQueue(connectionBuilder, config, 0);
                }
            }

            if (newtopic != null) {
                if (!newtopic.equals(topicname) && topic != null) {
                    topic.close();
                    topic = setTopic(connectionBuilder, config);
                } else if (!newtopic.equals(topicname) || topic == null) {
                    topic = setTopic(connectionBuilder, config);
                }
            }

            qname = config.getQueue();
            topicname = config.getTopicName();

            MQMessage mqMessage = buildMessage(config, queueMessage, messageContext);

            if (queue == null) {
                log.error("Cannot write to queue.Error in queue.");
            } else {
                log.info("queue initialized");
                queue.put(mqMessage);
                log.info("Message successfully placed at " + config.getQueue() + "queue");
            }

            if (topic == null) {
                log.info("Cannot write to topic.Error in topic.");
            } else {
                log.info("topic initialized");
                topic.put(mqMessage);
                log.info("Message successfully placed at " + config.getTopicName() + "topic");
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
    MQQueue setQueue(MQConnectionBuilder connectionBuilder, MQConfiguration config, int qFlag) {
        MQQueueManager manager = connectionBuilder.getQueueManager();
        MQQueue queue;
        try {
            if (qFlag == 0) {
                queue = manager.accessQueue(config.getQueue(), CMQC.MQOO_OUTPUT);
            } else {
                queue = manager.accessQueue(config.getreplyQueue(), CMQC.MQRC_READ_AHEAD_MSGS);
            }
        } catch (MQException e) {
            if (qFlag == 0) {
                log.error("Error creating queue " + e);
            } else {
                log.error("Error creating reply queue" + e);
            }
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

    /**
     * Create mq message
     */
    MQMessage buildMessage(MQConfiguration config, String queueMessage, MessageContext msgCtx) {

        MQMessage mqMessage = new MQMessage();
        mqMessage.encoding = MQENC_NATIVE;
        mqMessage.format = MQFMT_STRING;
        mqMessage.messageId = config.getMessageID().getBytes();
        mqMessage.correlationId = config.getCorrelationID().getBytes();
        int messageType = config.getMessageType();
        mqMessage.messageType = messageType;
        switch (messageType) {
            case 1://request message
                String replyQueue = config.getreplyQueue();
                if (replyQueue != null) {
                    mqMessage.replyToQueueName = replyQueue;
                    mqMessage.report = CMQC.MQRO_COA_WITH_FULL_DATA | CMQC.MQRO_EXCEPTION_WITH_FULL_DATA | CMQC.MQRO_COD_WITH_FULL_DATA | CMQC.MQRO_EXPIRATION_WITH_FULL_DATA;
                    MQQueue reply;
                    reply = setQueue(this.connectionBuilder, this.config, 1);
                    if (reply != null) {
                        getReplyMessage(reply, this.config, msgCtx, 0);
                    }
                } else {
                    log.info("Reply queue not specified");
                }
                break;
            case 2://reply message
                break;
            case 4://report message else datagram
                String reportQueue = config.getreplyQueue();
                if (reportQueue != null) {
                    mqMessage.replyToQueueName = reportQueue;
                    mqMessage.report = CMQC.MQRO_COA_WITH_FULL_DATA | CMQC.MQRO_EXCEPTION_WITH_FULL_DATA | CMQC.MQRO_COD_WITH_FULL_DATA | CMQC.MQRO_EXPIRATION_WITH_FULL_DATA;
                    MQQueue reply;
                    reply = setQueue(this.connectionBuilder, this.config, 1);
                    if (reply != null) {
                        getReplyMessage(reply, this.config, msgCtx, 1);
                    }
                } else {
                    log.info("Reply queue not specified");
                }
                break;
        }

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
            mqMessage.setStringProperty("ContentType", config.getContentType());
            mqMessage.setStringProperty("CHARACTER_SET_ENCODING", config.getCharsetEncoding());
            mqMessage.writeString(queueMessage);
        } catch (Exception e) {
            log.info("Error creating mq message" + e);
        }
        return mqMessage;
    }

    /**
     * Get reply message
     */
    void getReplyMessage(final MQQueue replyQueue, MQConfiguration config, final MessageContext msgCtx, final int msgFlag) {

        final MQMessage message = new MQMessage();
        final MQGetMessageOptions gmo = new MQGetMessageOptions();
        message.messageId = config.getMessageID().getBytes();
        message.correlationId = config.getCorrelationID().getBytes();
        gmo.matchOptions = MQConstants.MQMO_MATCH_CORREL_ID;
        gmo.matchOptions = MQConstants.MQMO_MATCH_GROUP_ID;

        ExecutorService executorService = Executors.newSingleThreadExecutor();

        executorService.execute(new Runnable() {
            public void run() {
                while (true) {
                    try {
                        replyQueue.get(message, gmo);
                        MQMD md = new MQMD();
                        md.copyFrom(message);
                        int strLen = message.getDataLength();
                        String cType = message.getStringProperty("usr.ContentType") != null ? message.getStringProperty("usr.ContentType") : org.wso2.carbon.esb.connector.MQConstants.CONTENT_TYPE;
                        byte[] strData = new byte[strLen];
                        message.readFully(strData);
                        if (msgFlag == 0) {
                            msgCtx.setProperty("Format", md.getFormat());
                            msgCtx.setProperty("Feedback", md.getFeedback());
                            msgCtx.setProperty("QueueMgr_Report", reportStr(md.getFeedback()));
                            buildreplyMessage(new String(strData), cType, msgCtx);
                        } else if (msgFlag == 1) {
                            log.info("Received report details-" + reportStr(md.getFeedback()));
                        }
                        log.info("Reply received");
                        break;
                    } catch (MQException e) {
                        log.info("Waiting for reply message");
                    } catch (IOException e) {
                        log.info("Error retrieving data from reply message");
                    }
                }
            }
        });
        executorService.shutdown();
    }

    /**
     * Create reply message if message type is request
     */
    void buildreplyMessage(String strMessage, String contentType, MessageContext msgCtx) {
        AutoCloseInputStream in = new AutoCloseInputStream(new ByteArrayInputStream(strMessage.getBytes()));
        try {
            org.apache.axis2.context.MessageContext axis2MsgCtx = ((Axis2MessageContext) msgCtx).getAxis2MessageContext();
            Builder builder;
            if (StringUtils.isEmpty(contentType)) {
                contentType = org.wso2.carbon.esb.connector.MQConstants.DEFAULT_CONTENT_TYPE;
            }
            int index = contentType.indexOf(';');
            String type = index > 0 ? contentType.substring(0, index) : contentType;
            builder = BuilderUtil.getBuilderFromSelector(type, axis2MsgCtx);
            if (builder == null) {
                builder = new SOAPBuilder();
            }
            OMElement documentElement = builder.processDocument(in, contentType, axis2MsgCtx);
            msgCtx.setEnvelope(TransportUtils.createSOAPEnvelope(documentElement));

        } catch (AxisFault axisFault) {
            axisFault.printStackTrace();
        }
    }

    /**
     * generate report details
     */
    String reportStr(int code) {
        switch (code) {
            case MQFB_EXPIRATION:
                return MQReportConstants.MQFB_EXPIRATION;
            case MQFB_COA:
                return MQReportConstants.MQFB_COA;
            case MQFB_COD:
                return MQReportConstants.MQFB_COD;
            case MQFB_QUIT:
                return MQReportConstants.MQFB_QUIT;
            case MQFB_APPL_CANNOT_BE_STARTED:
                return MQReportConstants.MQFB_APPL_CANNOT_BE_STARTED;
            case MQFB_TM_ERROR:
                return MQReportConstants.MQFB_TM_ERROR;
            case MQFB_APPL_TYPE_ERROR:
                return MQReportConstants.MQFB_APPL_TYPE_ERROR;
            case MQFB_STOPPED_BY_MSG_EXIT:
                return MQReportConstants.MQFB_STOPPED_BY_MSG_EXIT;
            case MQFB_XMIT_Q_MSG_ERROR:
                return MQReportConstants.MQFB_XMIT_Q_MSG_ERROR;
            case MQFB_ACTIVITY:
                return MQReportConstants.MQFB_ACTIVITY;
            case MQFB_MAX_ACTIVITIES:
                return MQReportConstants.MQFB_MAX_ACTIVITIES;
            case MQFB_NOT_FORWARDED:
                return MQReportConstants.MQFB_NOT_FORWARDED;
            case MQFB_NOT_DELIVERED:
                return MQReportConstants.MQFB_NOT_DELIVERED;
            case MQFB_UNSUPPORTED_FORWARDING:
                return MQReportConstants.MQFB_UNSUPPORTED_FORWARDING;
            case MQFB_UNSUPPORTED_DELIVERY:
                return MQReportConstants.MQFB_UNSUPPORTED_DELIVERY;
            default://MQFB_NONE
                return MQReportConstants.MQFB_NONE;
        }
    }
}