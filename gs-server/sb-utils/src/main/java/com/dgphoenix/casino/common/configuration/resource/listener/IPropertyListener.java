package com.dgphoenix.casino.common.configuration.resource.listener;

import com.abs.casino.common.configuration.resource.event.PropertyChangedEvent;

public interface IPropertyListener {
	void registerListener(String bundleName);
	void propertyChanged(PropertyChangedEvent event);
}
