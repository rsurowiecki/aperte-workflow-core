package org.aperteworkflow.editor.actioneditor;

import java.util.HashMap;
import java.util.Map;

public class ActionDef {
	private String buttonType;

	private Map<String, Object> items = new HashMap<String, Object>();
	private Map<String, Object> attributes = new HashMap<String, Object>();

	public String getButtonType() {
		return buttonType;
	}

	public void setButtonType(String buttonType) {
		this.buttonType = buttonType;
	}

	public Map<String, Object> getItems() {
		return items;
	}

	public void setItems(Map<String, Object> items) {
		this.items = items;
	}
	
	public void putItem(String key, Object value) {
		items.put(key, value);
	}

	public Map<String, Object> getAttributes() {
		return attributes;
	}

	public void setAttributes(Map<String, Object> attributes) {
		this.attributes = attributes;
	}

	public void setAttribute(String name, Object value) {
		attributes.put(name,  value);
	}
}
