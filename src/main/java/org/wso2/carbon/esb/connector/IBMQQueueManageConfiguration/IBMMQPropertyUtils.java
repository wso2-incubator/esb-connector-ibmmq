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
package org.wso2.carbon.esb.connector.IBMQQueueManageConfiguration;

import org.apache.synapse.MessageContext;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Class to get configuration details from MessageContext for IBM WebSphere MQ
 */
public class IBMMQPropertyUtils {

    /**
     * This method use getParametersMap method used to return list of parameter values
     * sorted by expected API parameter names.
     *
     * @param msgCtx ESB messageContext.
     * @return A hash map containing configuration details for IBM WebSphere MQ.If the the
     * required fields are not assigned by the client default fields will be assigned
     */
    public static Map<String, String> getProperties(MessageContext msgCtx) {
        Map<String, String> properties = new HashMap<>();
        Set<String> set = msgCtx.getPropertyKeySet();
        for (String s : set) {
            Object property = msgCtx.getProperty(s);
            if (property instanceof String) {
                properties.put(s, (String) msgCtx.getProperty(s));
            }
        }
        return properties;
    }
}
