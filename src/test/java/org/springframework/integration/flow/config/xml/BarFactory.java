package org.springframework.integration.flow.config.xml;

import org.springframework.beans.factory.FactoryBean;

public class BarFactory implements FactoryBean<Bar> {

	@Override
	public Bar getObject() throws Exception {
		// TODO Auto-generated method stub
		return new Bar();
	}

	@Override
	public Class getObjectType() {
		// TODO Auto-generated method stub
		return Bar.class;
	}

	@Override
	public boolean isSingleton() {
		// TODO Auto-generated method stub
		return true;
	}

}
