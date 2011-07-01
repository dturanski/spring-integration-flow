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

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.flow.FlowConfiguration;
import org.springframework.integration.flow.FlowProviderPortConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * 
 * @author David Turanski
 * 
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:/FlowConfigNamespaceTest-context.xml")
public class FlowConfigNamespaceTest {
	@Autowired
	FlowConfiguration flowConfiguration;

	@Test
	public void test() {
		assertNotNull(flowConfiguration.getPortConfigurations());
		assertEquals(2, flowConfiguration.getPortConfigurations().size());
		FlowProviderPortConfiguration pc0 = flowConfiguration.getPortConfigurations().get(0);
		assertEquals("input", pc0.getInputPortName());
		assertEquals("subflow-input", pc0.getInputChannel());
		assertEquals("", pc0.getInputPortDescription());
		assertEquals("subflow-output", pc0.getOutputChannel("output"));
		assertEquals(1, pc0.getOutputPortNames().size());
	}

}
