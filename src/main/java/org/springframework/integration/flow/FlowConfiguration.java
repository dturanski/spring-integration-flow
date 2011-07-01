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
package org.springframework.integration.flow;

import java.util.List;

/**
 * 
 * @author David Turanski
 * 
 */
public class FlowConfiguration {

	private final List<FlowProviderPortConfiguration> portConfigurations;

	private volatile NamedResourceConfiguration propertiesConfiguration;

	private volatile NamedResourceConfiguration referencedBeansConfiguration;

	public FlowConfiguration(List<FlowProviderPortConfiguration> portConfigurations) {
		this.portConfigurations = portConfigurations;
	}

	public FlowProviderPortConfiguration getConfigurationForInputPort(String inputPortName) {
		for (FlowProviderPortConfiguration pc : portConfigurations) {
			if (pc.getInputPortName().equals(inputPortName)) {
				return pc;
			}
		}
		return null;
	}

	public void setPropertiesConfiguration(NamedResourceConfiguration propertiesConfiguration) {
		this.propertiesConfiguration = propertiesConfiguration;
	}

	public void setReferenceedBeansConfiguration(NamedResourceConfiguration referencedBeansConfiguration) {
		this.setReferencedBeansConfiguration(referencedBeansConfiguration);
	}

	public List<FlowProviderPortConfiguration> getPortConfigurations() {
		return portConfigurations;
	}

	public NamedResourceConfiguration getPropertiesConfiguration() {
		return propertiesConfiguration;
	}

	public void setReferencedBeansConfiguration(NamedResourceConfiguration referencedBeansConfiguration) {
		this.referencedBeansConfiguration = referencedBeansConfiguration;
	}

	public NamedResourceConfiguration getReferencedBeansConfiguration() {
		return referencedBeansConfiguration;
	}

}
