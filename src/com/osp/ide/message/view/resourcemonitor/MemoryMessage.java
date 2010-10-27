package com.osp.ide.message.view.resourcemonitor;

import com.osp.ide.message.Constants;

public class MemoryMessage extends ResourceMonitorMessage {
	long			peakSize;
	long			heapSize;

	public MemoryMessage(int type, String command, String appID, String name, long peakSize, long heapSize) {
		super(type, command, appID);
		// TODO Auto-generated constructor stub
		this.name = name;
		this.peakSize = peakSize;
		this.heapSize = heapSize;
		this.typeString = Constants.OSP_RESOURCE_MEMORY;
	}
	
	public long getPeakSize() {
		return this.peakSize;
	}
	
	public void setPeakSize(long size) {
		this.peakSize = size;
	}
	
	public long getHeapSize() {
		return this.heapSize;
	}
	
	public void setHeapSize(long size) {
		this.heapSize = size;
	}
}
