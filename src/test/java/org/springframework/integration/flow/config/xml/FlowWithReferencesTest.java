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
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.integration.Message;
import org.springframework.integration.MessageChannel;
import org.springframework.integration.core.PollableChannel;
import org.springframework.integration.message.GenericMessage;
 
/**
 * 
 * @author David Turanski
 *
 */
 
public class FlowWithReferencesTest { 
	 
  @Test
   public void testReferencedBeanConfig(){
	  ApplicationContext applicationContext = new ClassPathXmlApplicationContext("classpath:/FlowWithReferencesTest-context.xml");
	  MessageChannel input = applicationContext.getBean("inputC",MessageChannel.class);
	  PollableChannel output = applicationContext.getBean("outputC",PollableChannel.class);
 	  Message<String> msg = new GenericMessage<String>("hello"); 
 	  input.send(msg);
 	  Message<?> reply = output.receive();
 	  assertNotNull(reply);
 	  assertEquals("it works!",reply.getHeaders().get("refbean.value"));
 	  assertEquals("val1",reply.getHeaders().get("property.value.1"));
 	  assertEquals("undefined",reply.getHeaders().get("property.value.2"));
   }
  
  
}
