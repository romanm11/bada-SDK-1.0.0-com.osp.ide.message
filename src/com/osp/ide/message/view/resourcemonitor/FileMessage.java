package com.osp.ide.message.view.resourcemonitor;

import com.osp.ide.message.Constants;

public class FileMessage extends ResourceMonitorMessage {

	public FileMessage(int type, String command, String appID, String name) {
		super(type, command, appID);
		// TODO Auto-generated constructor stub
		this.name = name;
		this.typeString = Constants.OSP_RESOURCE_FILE;
	}
}
