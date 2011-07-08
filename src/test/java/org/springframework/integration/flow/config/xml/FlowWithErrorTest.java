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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.integration.Message;
import org.springframework.integration.MessageChannel;
import org.springframework.integration.MessagingException;
import org.springframework.integration.core.MessageHandler;
import org.springframework.integration.core.PollableChannel;
import org.springframework.integration.core.SubscribableChannel;
import org.springframework.integration.message.GenericMessage;

 
/**
 * 
 * @author David Turanski
 *
 */
 
public class FlowWithErrorTest { 
	 
  @Test
   public void testExceptionThrown(){
	  ApplicationContext applicationContext = new ClassPathXmlApplicationContext("classpath:/FlowWithErrorTest-context.xml");
	  MessageChannel input = applicationContext.getBean("inputC",MessageChannel.class);
	  PollableChannel output = applicationContext.getBean("outputC",PollableChannel.class);
 	  Message<String> msg = new GenericMessage<String>("hello"); 
 	  input.send(msg);
  
 	  Message<?> reply = output.receive(100);
 	  assertNotNull(reply);
 	  assertTrue(reply.getPayload() instanceof MessagingException);
   }
  
  @Test
  public void testDirectCallWithErrorChannel(){
	  ApplicationContext applicationContext = new ClassPathXmlApplicationContext("classpath:/META-INF/spring/integration/flows/subflow5/subflow5-context.xml");
	  MessageChannel input = applicationContext.getBean("subflow-input",MessageChannel.class);
	  SubscribableChannel error =  applicationContext.getBean("errorChannel",SubscribableChannel.class);
	   
	  error.subscribe(new MessageHandler() {
		
		@Override
		public void handleMessage(Message<?> message) throws MessagingException {
			assertTrue( message.getPayload() instanceof MessagingException );
			System.out.println("got error message");
		}
	});
	  
 	  Message<String> msg = new GenericMessage<String>("hello"); 
 	  assertTrue(input.send(msg));
   }
  
  
  @Test
  public void testWithErrorChannel(){
	  ApplicationContext applicationContext = new ClassPathXmlApplicationContext("classpath:/FlowWithErrorTest-context.xml");
	  MessageChannel input = applicationContext.getBean("inputC1",MessageChannel.class);
	  PollableChannel output = applicationContext.getBean("outputC1",PollableChannel.class);
	  Message<String> msg = new GenericMessage<String>("hello"); 
	  input.send(msg);
 
	  Message<?> reply = output.receive(100);
	  assertNotNull(reply);
	  assertTrue(reply.getPayload() instanceof MessagingException);
  }
  
  
  
}
