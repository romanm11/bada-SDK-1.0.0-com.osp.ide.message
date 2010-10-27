package com.osp.ide.message.view.resourcemonitor;

import com.osp.ide.message.Constants;

public class ThreadMessage extends ResourceMonitorMessage {
	String 			stackSize;

	public ThreadMessage(int type, String command, String appID, String name, String stackSize) {
		super(type, command, appID);
		// TODO Auto-generated constructor stub
		this.name = name;
		this.stackSize = stackSize;
		this.typeString = Constants.OSP_RESOURCE_THREAD;
	}
	
	public String getStackSize() {
		return this.stackSize;
	}
}
