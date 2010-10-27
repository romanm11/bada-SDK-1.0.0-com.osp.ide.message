package com.osp.ide.message.view.output;

import java.lang.reflect.InvocationTargetException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Display;

import com.osp.ide.message.socket.NetManager;

public class UploadToTargetProgress implements IRunnableWithProgress {
	boolean	run;
	private TransferApplicationInfo				applicationInfo = null;
	
	int											totalFileCount;
	long										totalFileSize;
	
	int											currentFileIndex;
	long										totalTransferedSize = 0;
	
	DecimalFormat								decimalFormat;

	public UploadToTargetProgress() {
		applicationInfo = null;
		
		currentFileIndex = 0;
		totalTransferedSize = 0;
		
		decimalFormat = new DecimalFormat();
		DecimalFormatSymbols dfs = new DecimalFormatSymbols();
		dfs.setGroupingSeparator(',');
		decimalFormat.setDecimalFormatSymbols(dfs);
	}
	
	@Override
	public void run(IProgressMonitor monitor) throws InvocationTargetException,
			InterruptedException {
		// TODO Auto-generated method stub
		
		if(applicationInfo == null) return;
		
		monitor.beginTask("File Transfering", 100);
		int rate = 0;
		while( run &&
			   currentFileIndex < totalFileCount &&
			   totalTransferedSize < totalFileSize)
		{
			if(applicationInfo == null)	continue;
			
			monitor.setTaskName(decimalFormat.format(totalTransferedSize)
					+ " / " + decimalFormat.format(totalFileSize) + " byte(s)");
			
			int new_rate = (int) (((double)totalTransferedSize/(double)totalFileSize)*100);
			if(new_rate > rate) {
				monitor.worked(new_rate - rate);
				
				rate = new_rate;
//				System.out.println(Integer.toString(rate));
			}
			
//			monitor.worked(currentFileIndex);
//			monitor.worked(rate);
			
			
			monitor.subTask(applicationInfo.getSourcePath());
			
			if(monitor.isCanceled()) {
			    NetManager.getInstance().getBroker().destroy();
				break;
			}
			
			if(totalTransferedSize >= totalFileSize)
				monitor.done();
			
			Thread.sleep(100);
		}
		
		monitor.setTaskName(decimalFormat.format(totalTransferedSize)
				+ " / " + decimalFormat.format(totalFileSize) + " byte(s)");
		
		monitor.done();
	}
	
	public void setCurrentTransferedSize(long size) {
		totalTransferedSize += size;
	}
	
	public void setApplicationInfo(int counts, int size) {
		totalFileCount = counts;
		totalFileSize = size;
		
		totalTransferedSize = 0;
		
		applicationInfo = new TransferApplicationInfo();

		run = true;
	}

	public synchronized TransferApplicationInfo getApplicationInfo() {
		return applicationInfo;
	}
	
	public synchronized void startFileTransfer(int index, long size, String src, String dest) {
//		System.out.println(Integer.toString(index));
		currentFileIndex = index;
		applicationInfo.startFileTransfer(index, size, src, dest);
	}
	
	public void stopTransferFile() {
	    applicationInfo.stopFileTransfer();
	}
	
	public void stopTransferFileByError(String message) {
		applicationInfo.stopFileTransfer();

		run = false;
		
		if(!message.isEmpty()) {
		    final String strMsg = message;
		    Display.getDefault().asyncExec(new Runnable() {
                @Override
                public synchronized void run() {
                    MessageDialog.openError(null, "File Transfer Error", strMsg);
                }
            });
		}
	}
	
	 
	
	public void stopTransferApplication() {
		run = false;
	}
}
