package org.springframework.integration.flow;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.support.BeanDefinitionValidationException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
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
 * Encapsulates a Spring Integration message flow with inputs and outputs
 * abstracted by a {@link FlowConfiguration} Creates a Spring Integration flow
 * in a child application context. This facilitates reuse of the message flow
 * within complex messaging flows. Each flow instance may be configured by
 * injecting property values or referenced bean locations to allow for different
 * bean definitions used by the flow. In addition, beans defined in the parent
 * application context may be referenced or overridden in the flow application
 * context.
 * 
 * By convention the flow configuration resource locations are
 * classpath:META-INF/spring/integration/flows/[flow-id]/*.xml
 * 
 * The flow-id defaults to the bean name if not set
 * 
 * @author David Turanski
 * 
 */
public class Flow implements InitializingBean, BeanNameAware, ChannelResolver, ApplicationContextAware {

	private static Log logger = LogFactory.getLog(Flow.class);

	private volatile ClassPathXmlApplicationContext flowContext;

	private ApplicationContext applicationContext;

	private volatile FlowConfiguration flowConfiguration;

	private volatile String[] configLocations;

	private volatile String[] referencedBeanLocations;

	private volatile Properties flowProperties;

	private volatile String beanName;

	private volatile String flowId;

	private volatile ChannelResolver flowChannelResolver;

	private volatile PublishSubscribeChannel flowOutputChannel;

	private volatile boolean help;

	/**
	 * Default constructor
	 */
	public Flow() {

	}

	/**
	 * 
	 * @param flowProperties properties for this flow instance
	 * @param configLocations Spring configuration resource locations containing
	 * bean definitions included in the flow application context
	 */
	public Flow(Properties flowProperties, String[] configLocations) {
		this.flowProperties = flowProperties;
		this.configLocations = configLocations;
	}

	/**
	 * 
	 * @param configLocations Spring configuration resource locations containing
	 * bean definitions included in the flow application context
	 */
	public Flow(String[] configLocations) {
		this.configLocations = configLocations;
	}

	@Override
	public void afterPropertiesSet() {

		if (this.flowId == null) {
			this.flowId = this.beanName;
		}

		if (this.help) {
			System.out.println(FlowUtils.getDocumentation(this.flowId));
		}

		if (configLocations == null) {
			configLocations = new String[] { String.format("classpath:META-INF/spring/integration/flows/%s/*.xml",
					this.flowId) };
		}

		if (referencedBeanLocations != null) {
			configLocations = (String[]) ArrayUtils.addAll(configLocations, referencedBeanLocations);
		}

		Assert.notEmpty(configLocations, "configLocations cannot be empty");

		/*
		 * create a child application context
		 */
		flowContext = new ClassPathXmlApplicationContext(applicationContext);

		addReferencedProperties();

		if (logger.isDebugEnabled()) {
			logger.debug("instantiating flow context from configLocations ["
					+ StringUtils.arrayToCommaDelimitedString(configLocations) + "]");
		}

		this.flowContext.setConfigLocations(configLocations);

		this.flowContext.refresh();

		this.flowConfiguration = flowContext.getBean(FlowConfiguration.class);
		Assert.notNull(flowConfiguration, "flow context does not contain a flow configuration");

		validatePortMapping();

		this.flowChannelResolver = new BeanFactoryChannelResolver(flowContext);

		bridgeMessagingPorts();

	}

	public FlowConfiguration getFlowConfiguration() {
		return this.flowConfiguration;
	}

	@Override
	public void setBeanName(String name) {
		this.beanName = name;

	}

	public String getBeanName() {
		return this.beanName;
	}

	/**
	 * @param flowId The flow identifier used to locate the flow configuration
	 */
	public void setFlowId(String flowId) {
		this.flowId = flowId;
	}

	/**
	 * 
	 * @param referencedBeanLocations Additional resource locations containing
	 * referenced bean definitions
	 */
	public void setReferencedBeanLocations(String[] referencedBeanLocations) {
		this.referencedBeanLocations = referencedBeanLocations;
	}

	/**
	 * 
	 * @param flowProperties properties referenced in the flow definition
	 * property placeholders
	 */
	public void setProperties(Properties flowProperties) {
		this.flowProperties = flowProperties;
	}

	public Properties getProperties() {
		return this.flowProperties;
	}

	/**
	 * 
	 * @param help if true write the flow documentation to stdout The default
	 * document location is
	 * "classpath:META-INF/spring/integration/flows/[flow-id]/flow.doc"
	 */
	public void setHelp(boolean help) {
		this.help = help;
	}

	/**
	 * All flow outputs defined in the {@link PortConfiguration} are bridged to
	 * a single PublishSubscribeChannel
	 * @return the publish-subscribe channel
	 */
	public PublishSubscribeChannel getFlowOutputChannel() {
		return flowOutputChannel;
	}

	/**
	 * All flow outputs defined in the {@link PortConfiguration} are bridged to
	 * a single PublishSubscribeChannel
	 * @param the publish-subscribe channel
	 */
	public void setFlowOutputChannel(PublishSubscribeChannel flowOutputChannel) {
		this.flowOutputChannel = flowOutputChannel;
	}

	@Override
	public MessageChannel resolveChannelName(String channelName) {
		return flowChannelResolver.resolveChannelName(channelName);
	}

	private void addReferencedProperties() {
		if (flowProperties != null) {
			PropertySource<?> propertySource = new PropertiesPropertySource("flowProperties", flowProperties);

			MutablePropertySources propertySources = flowContext.getEnvironment().getPropertySources();
			propertySources.addLast(propertySource);
		}

	}

	private void validatePortMapping() {
		Assert.notEmpty(this.flowConfiguration.getPortConfigurations(),
				"flow configuration contains no port configurations");

		List<String> errors = new ArrayList<String>();
		for (PortConfiguration portConfiguration : this.flowConfiguration.getPortConfigurations()) {
			String inputChannelName = (String) portConfiguration.getInputChannel();
			validateFlowChannelDefinition(inputChannelName, errors, false);

			for (String outputPortName : portConfiguration.getOutputPortNames()) {
				String outputChannelName = (String) portConfiguration.getOutputChannel(outputPortName);
				validateFlowChannelDefinition(outputChannelName, errors, true);
			}
		}
		if (errors.size() > 0 ) {
			 
			throw new BeanDefinitionValidationException("\n"+StringUtils.arrayToDelimitedString(errors.toArray(),"\n"));
		}
	}

	/*
	 * If flow context does not contain the bean definition then the definition
	 * comes from the parent context. The flow should should still work with a
	 * 'global' PublishSubscribeChannel output channel
	 */
	private void validateFlowChannelDefinition(String channelName, List<String> errors, boolean allowPubSub) {

		MessageChannel channel = this.flowContext.getBean(channelName, MessageChannel.class);

		if (!this.flowContext.containsBeanDefinition(channelName)) {
			if (channel instanceof PublishSubscribeChannel && allowPubSub) {
				 if (logger.isDebugEnabled()) {
					 logger.warn("Flow '" +  this.flowId +"'" +
						" is sharing the publish-subscribe channel '" + channelName +"'" + 
						" with the parent context.");
				 }
			} else {
				errors.add("The flow channel '"
						+ channelName
						+ "' in flow '"
						+ this.flowId
						+ "' conflicts with a bean definition in the parent context. It must be explicitly declared in the flow'");
			}
		}
	}

	private void bridgeMessagingPorts() {
		/*
		 * create a bridge for each target output port to the flow outputChannel
		 */
		for (PortConfiguration targetPortConfiguration : this.getFlowConfiguration().getPortConfigurations()) {
			for (String outputPort : targetPortConfiguration.getOutputPortNames()) {
				String targetOutputChannelName = (String) targetPortConfiguration.getOutputChannel(outputPort);
				SubscribableChannel inputChannel = (SubscribableChannel) resolveChannelName(targetOutputChannelName);

				((AbstractMessageChannel) inputChannel).addInterceptor(new FlowInterceptor(outputPort));

				logger.debug("creating output bridge on [" + outputPort + "] inputChannelName = ["
						+ targetOutputChannelName + "] outputChannel = [" + this.flowOutputChannel + "]");
				FlowUtils.bridgeChannels(inputChannel, this.flowOutputChannel);
			}
		}
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}
}
