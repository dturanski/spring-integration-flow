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
/**
 * @author David Turanski
 */
package org.springframework.integration.flow;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ChannelNamePortConfiguration extends NamedResourceConfiguration implements FlowProviderPortConfiguration {

	private PortMetadata inputPortMetadata;

	public ChannelNamePortConfiguration(PortMetadata inputPortMetadata, List<PortMetadata> outputPortMetadataList) {
		super(outputPortMetadataList);
		this.inputPortMetadata = inputPortMetadata;
	}

	@Override
	public String getInputPortName() {
		return this.inputPortMetadata.getName();
	}

	@Override
	public String getInputChannel() {
		return this.inputPortMetadata.getChannelName();
	}

	@Override
	public String getInputPortDescription() {
		return this.inputPortMetadata.getDescription();
	}
	
	 
	@Override
	public String getOutputChannel(String portName) {
		PortMetadata portMetadata = (PortMetadata) find(portName);
		if (portMetadata != null) {
			return portMetadata.getChannelName();
		}
		return null;
	}

	@Override
	public List<String> getOutputPortNames() {
		List<String> results = new ArrayList<String>();
		for (NamedResourceMetadata resourceMetadata : getConfiguredResources()) {
			PortMetadata portMetadata = (PortMetadata) resourceMetadata;
			results.add(portMetadata.getName());
		}
		/**
		 * consistent with ClientPortConfiguration impl.
		 */

		if (results.isEmpty()) {
			return null;
		}

		return results;
	}

	/* (non-Javadoc)
	 * @see org.springframework.integration.flow.FlowProviderPortConfiguration#getOutputPortMetadata()
	 */
	@Override
	public List<NamedResourceMetadata> getOutputPortMetadata() {
		return Collections.unmodifiableList(super.getConfiguredResources());
	}
}
