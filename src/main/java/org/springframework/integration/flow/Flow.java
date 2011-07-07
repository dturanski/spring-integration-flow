package org.springframework.integration.flow;

import java.util.Properties;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.integration.MessageChannel;
import org.springframework.integration.channel.AbstractMessageChannel;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.integration.core.SubscribableChannel;
import org.springframework.integration.flow.config.FlowUtils;
import org.springframework.integration.flow.interceptor.FlowInterceptor;
import org.springframework.integration.support.channel.BeanFactoryChannelResolver;
import org.springframework.integration.support.channel.ChannelResolver;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * Encapsulates a message flow with inputs and outputs exposed via a message
 * port
 * 
 * @author David Turanski
 * 
 */
public class Flow implements InitializingBean, BeanNameAware, ChannelResolver, ApplicationContextAware {

	private static Log logger = LogFactory.getLog(Flow.class);

	private volatile ConfigurableApplicationContext flowContext;
	
	private ConfigurableApplicationContext applicationContext;

	private volatile FlowConfiguration flowConfiguration;

	private volatile String[] configLocations;

	private volatile String[] referencedBeanLocations;

	private volatile Properties flowProperties;

	private volatile String name;

	private volatile ChannelResolver flowChannelResolver;

	private volatile PublishSubscribeChannel flowOutputChannel;

	private volatile boolean help;

	public Flow() {

	}

	public Flow(String[] configLocations) {
		this.configLocations = configLocations;
	}

	@Override
	public void afterPropertiesSet() {

		if (configLocations == null) {
			configLocations = new String[] { String.format(
					"classpath*:META-INF/spring/integration/flows/%s-context.xml", this.name) };
		}

		if (referencedBeanLocations != null) {
			configLocations = (String[]) ArrayUtils.addAll(configLocations, referencedBeanLocations);
		}

		logger.debug("instantiating flow context from configLocations ["
				+ StringUtils.arrayToCommaDelimitedString(configLocations) + "]");

		Assert.notEmpty(configLocations, "configLocations cannot be empty");

		flowContext = new ClassPathXmlApplicationContext(configLocations, applicationContext);

		this.flowConfiguration = flowContext.getBean(FlowConfiguration.class);
		Assert.notNull(flowConfiguration, "flow context does not contain a flow configuration");

		if (this.help) {
			System.out.println(displayFlowConfiguration());
		}

		validatePortMapping();

		this.flowChannelResolver = new BeanFactoryChannelResolver(flowContext);

		addReferencedProperties();
		
		bridgeMessagingPorts();
		
		

	}

	public FlowConfiguration getFlowConfiguration() {
		return this.flowConfiguration;
	}

	@Override
	public void setBeanName(String name) {
		this.name = name;

	}

	public String getBeanName() {
		return this.name;
	}

	public void setReferencedBeanLocations(String[] referencedBeanLocations) {
		this.referencedBeanLocations = referencedBeanLocations;
	}

	public void setProperties(Properties flowProperties) {
		this.flowProperties = flowProperties;
	}

	public void setHelp(boolean help) {
		this.help = help;
	}

	public PublishSubscribeChannel getFlowOutputChannel() {
		return flowOutputChannel;
	}

	public void setFlowOutputChannel(PublishSubscribeChannel flowOutputChannel) {
		this.flowOutputChannel = flowOutputChannel;
	}

	@Override
	public MessageChannel resolveChannelName(String channelName) {
		return flowChannelResolver.resolveChannelName(channelName);
	}

	 

	/**
	 * 
	 */
	private String displayFlowConfiguration() {
		StringBuilder sb = new StringBuilder();
		sb.append("\nFlow configuration for [").append(this.getBeanName()).append("]:\n");
		sb.append("Port configuration:\n");
		for (FlowProviderPortConfiguration portConfiguration : this.getFlowConfiguration().getPortConfigurations()) {
			sb.append("\tinput port:").append(portConfiguration.getInputPortName()).append("\n\n\t")
					.append(portConfiguration.getInputPortDescription()).append("\n\n").append("\toutput ports:\n");

			for (NamedResourceMetadata metadata : portConfiguration.getOutputPortMetadata()) {
				sb.append("\t\t").append(metadata.getName()).append("\t")
				.append(metadata.getDescription()).append("\n");
			}

			NamedResourceConfiguration referencedBeansConfig = this.getFlowConfiguration()
					.getReferencedBeansConfiguration();
			if (referencedBeansConfig != null && !referencedBeansConfig.getConfiguredResources().isEmpty()) {
				sb.append("\nReferenced beans:\n");
				for (NamedResourceMetadata metadata : referencedBeansConfig.getConfiguredResources()) {
					sb.append("\t").append(metadata).append("\n");
				}
			}

			NamedResourceConfiguration propertiesConfig = this.getFlowConfiguration().getPropertiesConfiguration();
			if (propertiesConfig != null && !propertiesConfig.getConfiguredResources().isEmpty()) {
				sb.append("\nFlow properties:\n");
				for (NamedResourceMetadata metadata : propertiesConfig.getConfiguredResources()) {
					sb.append("\t").append(metadata).append("\n");
				}
			}

		}

		return sb.toString();
	}

	private void addReferencedProperties() {
		if (flowProperties != null) {

			PropertySource<?> propertySource = new PropertiesPropertySource("flowProperties", flowProperties);
			NamedResourceConfiguration propertiesConfiguration = this.getFlowConfiguration()
					.getPropertiesConfiguration();
			if (propertiesConfiguration != null) {
				for (NamedResourceMetadata resource : propertiesConfiguration.getRequiredResources()) {
					Assert.isTrue(propertySource.containsProperty(resource.getName()), "Flow [" + this.name
							+ "] is missing required property [" + resource.getName() + "]");
				}
			}
			MutablePropertySources propertySources = flowContext.getEnvironment().getPropertySources();
			propertySources.addLast(propertySource);
			
			this.flowContext.refresh();	
		}

	}

	private void validatePortMapping() {
		Assert.notEmpty(this.flowConfiguration.getPortConfigurations(),
				"flow configuration contains no port configurations");
	}

	private void bridgeMessagingPorts() {

		/*
		 * create a bridge for each target output port to the flow outputChannel
		 */
		for (FlowProviderPortConfiguration targetPortConfiguration : this.getFlowConfiguration()
				.getPortConfigurations()) {
			for (String outputPort : targetPortConfiguration.getOutputPortNames()) {
				String targetOutputChannelName = (String) targetPortConfiguration.getOutputChannel(outputPort);
				SubscribableChannel inputChannel = (SubscribableChannel) resolveChannelName(targetOutputChannelName);

				((AbstractMessageChannel)inputChannel).addInterceptor(new FlowInterceptor(outputPort));

				logger.debug("creating output bridge on [" + outputPort + "] inputChannelName = ["
						+ targetOutputChannelName + "] outputChannel = [" + this.flowOutputChannel + "]");
				FlowUtils.bridgeChannels(inputChannel, this.flowOutputChannel);
			}
		}
	}

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
       this.applicationContext = (ConfigurableApplicationContext) applicationContext;
        
    }
}
