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
public class ibmmqConnector extends AbstractConnector {

    @Override
    public void connect(MessageContext messageContext) throws ConnectException {
        try {
            log.info("Message received at ibm-mq connector");

            SOAPEnvelope soapEnvelope = messageContext.getEnvelope();
            OMElement getElement = soapEnvelope.getBody().getFirstElement();
            String queueMessage="";

            if(getElement!=null){
                queueMessage=getElement.getText();
            }

            MQConnectionBuilder connectionBuilder = new MQConnectionBuilder(messageContext);
            MQConfiguration config = connectionBuilder.getConfig();
            MQQueueManager manager = connectionBuilder.getQueueManager();

            MQQueue queue = manager.accessQueue(config.getQueue(), CMQC.MQOO_OUTPUT);

            if (queue == null) {
                log.error("error in queue creation");
            } else {
                log.info("queue created");
                MQMessage mqMessage = new MQMessage();
                mqMessage.writeString(queueMessage);
                MQPutMessageOptions pmo = new MQPutMessageOptions();
                queue.put(mqMessage, pmo);
                queue.close();
            }
            connectionBuilder.closeConnection();
        } catch (Exception e) {
            log.error("Problem in IBMMQProducer" + e);
            throw new ConnectException(e);
        }
    }
}