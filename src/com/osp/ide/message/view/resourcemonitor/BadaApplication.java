package com.osp.ide.message.view.resourcemonitor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

public class BadaApplication {
	private String				appId;
	private String				appName;
	private String				appCommand;
	
	private List<ResourceMonitorMessage>	resourceMessageList;

	public BadaApplication(String appCommand, String appId, String appName) {
		this.appId = appId;
		this.appName = appName;
		this.appCommand = appCommand;
		
		resourceMessageList = Collections.synchronizedList(new ArrayList<ResourceMonitorMessage>());
	}

	public String getAppId(){
		return appId;
	}
	
	public void setAppId(String appId){
		this.appId = appId;
	}

	public String getAppName() {
		return this.appName;
	}
	
	public void setAppName(String appName) {
		this.appName = appName;
	}
	
	public String getAppCommand() {
		return this.appCommand;
	}
	
	public void setAppCommand(String appName) {
		this.appCommand = appName;
	}
	
	public synchronized List<ResourceMonitorMessage> getResourceMonitorMessageList() {
		return resourceMessageList;
	}
	
	public synchronized void addResourceMessage(ResourceMonitorMessage message) {
		if(message == null) return;
		try { resourceMessageList.add(message); }
		catch (Exception ex) { ex.printStackTrace(); }
	}
	
	public synchronized void removeResourceMessage(int idx) {
		if(idx < 0 || idx > resourceMessageList.size())	return;
		try { resourceMessageList.remove(idx); }
		catch (Exception ex) { ex.printStackTrace(); }
	}
}