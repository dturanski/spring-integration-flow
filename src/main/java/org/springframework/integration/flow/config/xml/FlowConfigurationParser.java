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

import java.util.List;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.integration.flow.ChannelNamePortConfiguration;
import org.springframework.integration.flow.FlowConfiguration;
import org.springframework.integration.flow.NamedResourceConfiguration;
import org.springframework.integration.flow.NamedResourceMetadata;
import org.springframework.integration.flow.PortMetadata;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

/**
 * 
 * @author David Turanski
 * 
 */
public class FlowConfigurationParser implements BeanDefinitionParser {

	@Override
	public BeanDefinition parse(Element element, ParserContext parserContext) {

		List<Element> portMappings = DomUtils.getChildElementsByTagName(element, "port-mapping");
		List<Element> portMappingRefs = DomUtils.getChildElementsByTagName(element, "port-mapping-ref");

		BeanDefinitionBuilder flowConfigurationBuilder = BeanDefinitionBuilder
				.genericBeanDefinition(FlowConfiguration.class);

		ManagedList<Object> portConfigList = new ManagedList<Object>();

		for (Element el : portMappings) {

			BeanDefinition portConfiguration = buildFlowProviderPortConfiguration(el, parserContext);
			portConfigList.add(portConfiguration);
		}

		flowConfigurationBuilder.addConstructorArgValue(portConfigList);


		 BeanDefinition referencedProperties = this.buildNamedResourceConfiguration(element, "referenced-property");
		 
		 flowConfigurationBuilder.addPropertyValue("propertiesConfiguration", referencedProperties);
		 
		 BeanDefinition referencedBeans = this.buildNamedResourceConfiguration(element, "referenced-bean");
		 
		 flowConfigurationBuilder.addPropertyValue("referencedBeansConfiguration", referencedBeans);
		
		BeanDefinitionReaderUtils.registerWithGeneratedName(flowConfigurationBuilder.getBeanDefinition(),
				parserContext.getRegistry());
		
		 
		
		
		return null;
	}

	private BeanDefinition buildFlowProviderPortConfiguration(Element el, ParserContext parserContext) {
		Element inputPortEl = DomUtils.getChildElementByTagName(el, "input-port");
		 
		BeanDefinitionBuilder portConfigurationBuilder = BeanDefinitionBuilder
				.genericBeanDefinition(ChannelNamePortConfiguration.class);

		BeanDefinition portMetadata = this.buildPortMetadata(el, inputPortEl);
		
		portConfigurationBuilder.addConstructorArgValue(portMetadata);

		List<Element> outputPortElements = DomUtils.getChildElementsByTagName(el, "output-port");
		ManagedList<Object> outputList = null;

		if (outputPortElements != null) {

			outputList = new ManagedList<Object>();
			for (Element outputPortEl : outputPortElements) {
				portMetadata = this.buildPortMetadata(el, outputPortEl);
				outputList.add(portMetadata);
			}
		}

		portConfigurationBuilder.addConstructorArgValue(outputList);

		return portConfigurationBuilder.getBeanDefinition();
	}
	
	private BeanDefinition buildNamedResourceConfiguration(Element parent, String elementName) {
		ManagedList<Object> namedResourceList = new ManagedList<Object>();
		List<Element> namedResources = DomUtils.getChildElementsByTagName(parent, elementName);
		for (Element el : namedResources ) {
			BeanDefinitionBuilder namedResourceBuilder = BeanDefinitionBuilder.genericBeanDefinition(NamedResourceMetadata.class);
			boolean required = ("true".equals(el.getAttribute("required")));
			String name = el.getAttribute("id");
			String description =  getChildElementText(el, "description", "");
			namedResourceBuilder.addConstructorArgValue(name);
			namedResourceBuilder.addConstructorArgValue(description);
			namedResourceBuilder.addConstructorArgValue(required);
			namedResourceList.add(namedResourceBuilder.getBeanDefinition());
		}
		BeanDefinitionBuilder namedResourceConfigurationBuilder = BeanDefinitionBuilder.genericBeanDefinition(NamedResourceConfiguration.class);
		namedResourceConfigurationBuilder.addConstructorArgValue(namedResourceList);
		return namedResourceConfigurationBuilder.getBeanDefinition();
	}
	
	private String getChildElementText(Element parent, String elementName, String defaultValue ){
		String value = defaultValue;
		Element child = DomUtils.getChildElementByTagName(parent, elementName);
		if (child != null ) {
			value = child.getTextContent();
		}
		return value;
	}
	
	private BeanDefinition buildPortMetadata(Element element, Element portElement) {
		BeanDefinitionBuilder portMetadataBuilder = BeanDefinitionBuilder.genericBeanDefinition(PortMetadata.class);
		portMetadataBuilder.addConstructorArgValue(portElement.getAttribute("name")); 
		String description =  getChildElementText(portElement, "description", "");
		portMetadataBuilder.addConstructorArgValue(description); 
		portMetadataBuilder.addConstructorArgValue(portElement.getAttribute("channel"));
		return portMetadataBuilder.getBeanDefinition();
	}

}
