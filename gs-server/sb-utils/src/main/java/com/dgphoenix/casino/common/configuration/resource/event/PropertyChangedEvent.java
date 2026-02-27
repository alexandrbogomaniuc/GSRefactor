package com.abs.casino.common.configuration.resource.event;

public class PropertyChangedEvent implements IChangeEvent{
	private final String propertyName;
	private final String oldValue;
	private final String newValue;
	
	public PropertyChangedEvent(String propertyName, String oldValue, String newValue) {
		this.propertyName = propertyName;
		this.oldValue = oldValue;
		this.newValue = newValue;
	}
	
	public String getPropertyName() {
		return propertyName;
	}
	
	public String getOldValue() {
		return oldValue;
	}
	
	public String getNewValue() {
		return newValue;
	}

	public String toString(){
		return "PropertyChangedEvent [" +
	        super.toString() + 
	        ", propertyName=" + this.propertyName +
	        ", oldValue=" + this.oldValue +
	        ", newValue=" + this.newValue +
	        "]";
	}
}
