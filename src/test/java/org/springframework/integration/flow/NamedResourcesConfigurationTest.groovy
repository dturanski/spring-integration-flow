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
package org.springframework.integration.flow
import org.junit.Test
/**
 * 
 * @author David Turanski
 *
 */
class NamedResourcesConfigurationTest {
	def resourceMetadataList = [
		new NamedResourceMetadata('resource1','describes resource1',true),
		new NamedResourceMetadata('resource2','describes resource2',false),
		new NamedResourceMetadata('resource3','describes resource3',true)
	]

	@Test
	public void testGetRequired(){
		def namedResourceConfiguration = new NamedResourceConfiguration(resourceMetadataList);
		def required = namedResourceConfiguration.getRequiredResources()
		assert required.size() == 2
		required.each { assert it.required; assert namedResourceConfiguration.isRequired(it.name) }
	}

	@Test
	public void testGetAll(){
		def namedResourceConfiguration = new NamedResourceConfiguration(resourceMetadataList);
		assert namedResourceConfiguration.getConfiguredResources().size() == 3
	}
}
