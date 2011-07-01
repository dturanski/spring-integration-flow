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

import org.springframework.util.Assert;

/**
 * 
 * @author David Turanski
 * 
 */
public class NamedResourceMetadata {
	private final String name;

	private final String description;

	private final boolean required;

	public NamedResourceMetadata(String name, String description, boolean required) {
		Assert.hasText(name, "name is required");
		this.name = name;
		this.description = description;
		this.required = required;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public boolean isRequired() {
		return required;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(getName()).append("\t")
		.append(required? "required" : "optional")
		.append("\t")
		.append(getDescription());
		return sb.toString();
	}

}
