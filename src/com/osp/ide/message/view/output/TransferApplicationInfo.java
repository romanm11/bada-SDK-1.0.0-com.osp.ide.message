package com.osp.ide.message.view.output;

public class TransferApplicationInfo {
	private int			current_FileIndex;
	private long		current_FileSize;
	
	private String		sourcePath;
	private String		destPath;
	
	public TransferApplicationInfo() {
		this.current_FileIndex = 0;
		this.current_FileSize = 0;
		
		this.sourcePath = "";
		this.destPath = "";
	}
	
	public synchronized int getCurrent_FileIndex() {
		return current_FileIndex;
	}
	
	public synchronized long getCurrent_FileSize() {
		return current_FileSize;
	}
	
	public synchronized String getSourcePath() {
		return sourcePath;
	}

	public synchronized String getDestPath() {
		return destPath;
	}

	public synchronized void startFileTransfer(int index, long size, String src, String dest) {
		this.current_FileIndex = index;
		this.current_FileSize = size;
		this.sourcePath = src;
		this.destPath = dest;
	}
	
	public synchronized void stopFileTransfer() {
		this.current_FileIndex = 0;
		this.current_FileSize = 0;
		this.sourcePath = "";
		this.destPath = "";
	}
}
