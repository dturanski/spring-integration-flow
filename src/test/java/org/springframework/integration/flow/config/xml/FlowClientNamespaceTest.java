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
package org.springframework.integration.flow.config.xml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.integration.Message;
import org.springframework.integration.MessageChannel;
import org.springframework.integration.core.PollableChannel;
import org.springframework.integration.message.GenericMessage;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
/**
 * 
 * @author David Turanski
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:/FlowClientNamespaceTest-context.xml")
public class FlowClientNamespaceTest {
	 
	@Autowired 
	@Qualifier("another-input")
	MessageChannel gatewayInput;
	
	@Autowired 
	@Qualifier("another-output")
	PollableChannel gatewayOutput;
	
	
  @Test
   public void testGateway(){
 	  Message<String> msg = new GenericMessage<String>("hello"); 
 	  gatewayInput.send(msg);
 	  Message<?> reply = gatewayOutput.receive();
 	  assertNotNull(reply);
    }
  
	@Autowired 
	@Qualifier("another-input2")
	MessageChannel gatewayInput2;
	
	@Autowired 
	@Qualifier("another-output2")
	PollableChannel gatewayOutput2;
    
   
   @Test
   public void testOutboundGateway(){
 	  Message<String> msg1 = new GenericMessage<String>("hello"); 
 	  Message<String> msg2 = new GenericMessage<String>("world"); 
 	  Message<?> reply  = null;
 	 
 	 gatewayInput2.send(msg1);
 	 reply = gatewayOutput2.receive();
 	 assertNotNull(reply);
 	 assertEquals("gateway-output",reply.getHeaders().get("flow.output.port"));
 	 assertEquals("yeah!",reply.getHeaders().get("gateway"));
 	 
 	 gatewayInput2.send(msg2);
	 reply = gatewayOutput2.receive();
	 assertNotNull(reply);
	 assertEquals("gateway-discard",reply.getHeaders().get("flow.output.port"));
	 assertEquals("yeah!",reply.getHeaders().get("gateway"));
	 
	 gatewayInput2.send(msg1);
 	 reply = gatewayOutput2.receive();
 	 assertNotNull(reply);
 	 assertEquals("gateway-output",reply.getHeaders().get("flow.output.port"));
 	 assertEquals("yeah!",reply.getHeaders().get("gateway"));
	 
    }
   
	
  
}
