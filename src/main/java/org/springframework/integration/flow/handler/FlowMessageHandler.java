/*
 * Copyright 2002-2011 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.springframework.integration.flow.handler;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.integration.Message;
import org.springframework.integration.MessageChannel;
import org.springframework.integration.MessagingException;
import org.springframework.integration.core.PollableChannel;
import org.springframework.integration.handler.AbstractReplyProducingMessageHandler;
import org.springframework.integration.message.ErrorMessage;
import org.springframework.integration.support.MessageBuilder;

/**
 * 
 * @author David Turanski
 * 
 */
public class FlowMessageHandler extends AbstractReplyProducingMessageHandler {

    /**
	 * 
	 */
	private static final String FLOW_CONVERSATION_ID_HEADER = "flow.conversation.id";

	private static Log log = LogFactory.getLog(FlowMessageHandler.class);

    private final MessageChannel flowInputChannel;

    private final PollableChannel flowOutputChannel;

    private final long timeout;

    public FlowMessageHandler(MessageChannel flowInputChannel, PollableChannel flowOutputChannel, long timeout) {
        this.flowInputChannel = flowInputChannel;
        this.flowOutputChannel = flowOutputChannel;
        this.timeout = timeout;
    }

    @Override
    protected Object handleRequestMessage(Message<?> requestMessage) {

        UUID conversationId = requestMessage.getHeaders().getId();
        Map<String, Object> flowConversationIdHeader = Collections.singletonMap(FLOW_CONVERSATION_ID_HEADER,
                (Object) conversationId);
         
        Message<?> message = MessageBuilder
        .fromMessage(requestMessage)
        .copyHeaders(flowConversationIdHeader)
       
         .build();
        Message<?> response = null;
        
        try {
           
            flowInputChannel.send(message);
         
            while ((response = flowOutputChannel.receive(timeout)) != null) {
                if (conversationId.equals(response.getHeaders().get(FLOW_CONVERSATION_ID_HEADER))) {
                    return response;
                } else {
                    
                    if (response.getPayload() instanceof MessagingException) {
                        MessagingException me = (MessagingException) response.getPayload();
                        log.debug("failed message: " + me.getFailedMessage());
                        if (conversationId.equals(me.getFailedMessage().getHeaders().get(FLOW_CONVERSATION_ID_HEADER))) {
                            return response;
                        }

                    }
                }
            }
        } catch (MessagingException me) {
            log.error(me.getMessage(), me);
            if (conversationId.equals(me.getFailedMessage().getHeaders().get(FLOW_CONVERSATION_ID_HEADER))) {
                return new ErrorMessage(me);
            }
        }
        return null;
    }
    
}
