package com.abs.casino.common.configuration.resource.observable;

import com.abs.casino.common.configuration.resource.listener.IPropertyListener;

public interface IFileObservable {	
	void addListener(IPropertyListener listener);
	void removeListener(IPropertyListener listener);	
	void setUpdater(Runnable task);
	boolean isUpdated();
	void fileModified();
	void clear();
}
