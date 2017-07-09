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

/**
 * Constants for report message
 */
public class MQReportConstants {
    public static final String MQFB_EXPIRATION = "Feedback indicating that the message was discarded because it had not been removed from the destination queue before its expiry time had elapsed";
    public static final String MQFB_COA = "Feedback confirming arrival on the destination queue";
    public static final String MQFB_COD = "Feedback confirming delivery to the receiving application ";
    public static final String MQFB_QUIT = "Feedback indicating an application ended. This can be used by a workload scheduling program to control the number of instances of an application program that are running. Sending an MQMT_REPORT message with this feedback code to an instance of the application program indicates to that instance that it must stop processing.";
    public static final String MQFB_NONE = "This is used with a message of type report, and indicates no feedback is provided";
    public static final String MQFB_APPL_CANNOT_BE_STARTED = "Feedback indicating that an application processing a trigger message cannot start the application named in the ApplId field of the trigger message";
    public static final String MQFB_TM_ERROR = "Feedback indicating that the Format field in MQMD specifies MQFMT_TRIGGER, but the message does not begin with a valid MQTM structure";
    public static final String MQFB_APPL_TYPE_ERROR = "Feedback indicating that an application processing a trigger message cannot start the application because the ApplType field of the trigger message is not valid";
    public static final String MQFB_STOPPED_BY_MSG_EXIT = "Feedback indicating that a message was stopped by a channel message exit";
    public static final String MQFB_XMIT_Q_MSG_ERROR = "Feedback indicating that a message channel agent has found that a message on the transmission queue is not in the correct format. The message channel agent puts the message on the dead-letter queue using this feedback code";
    public static final String MQFB_ACTIVITY = "Feedback indicating that an activity was performed on behalf of message";
    public static final String MQFB_MAX_ACTIVITIES = "Feedback indicating that a trace-route message was discarded because it was involved in more than the specified maximum number of activities";
    public static final String MQFB_NOT_FORWARDED = "Feedback indicating that a trace-route message was discarded because it was about to be forwarded to a queue manager that is unable to honor the value of the specified forwarding options";
    public static final String MQFB_NOT_DELIVERED = "Feedback indicating that a trace-route message was discarded because it was about to be delivered to a local queue";
    public static final String MQFB_UNSUPPORTED_FORWARDING = "Feedback indicating that a trace-route message was discarded because at least one of the forwarding options was not recognized and was in the MQROUTE_FORWARD_REJ_UNSUP_MASK bitmask";
    public static final String MQFB_UNSUPPORTED_DELIVERY = "Feedback indicating that a trace-route message was discarded because at least one of the delivery options was not recognized and was in the MQROUTE_DELIVER_REJ_UNSUP_MASK bitmask";
}
