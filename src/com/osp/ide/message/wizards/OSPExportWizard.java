package com.osp.ide.message.wizards;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.List;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IExportWizard;
import org.eclipse.ui.IWorkbench;

import com.osp.ide.message.OSPMessage;
import com.osp.ide.message.socket.NetManager;

public class OSPExportWizard extends Wizard implements IExportWizard {
	private String						messageFile="";
	private OSPExportWizardPage			wizardPage;
	
	public OSPExportWizard() {
		super();
		setWindowTitle("Export");
	}
	
	@Override
	public boolean performFinish() {
		// TODO Auto-generated method stub
		if(messageFile.isEmpty())
			return false;
		
		ExportEventLog(messageFile);
		
		return true;
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		// TODO Auto-generated method stub

	}
	
	public void addPages() {
		wizardPage = new OSPExportWizardPage(this);
		addPage(wizardPage);
	}

	public void setExportFilename(String messageFile) {
		this.messageFile = messageFile;
	}

	public String getExportFilename() {
		return messageFile;
	}

	private void ExportEventLog(String filename) {
		BufferedWriter writer = null;
		try {
			final List<OSPMessage> list = NetManager.getInstance().getMessageList();
			if(list.isEmpty())
				return;
//        	writer = new BufferedWriter(new FileWriter(filename));
			writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filename), Charset.defaultCharset()));
        	for(OSPMessage message : list) {
        		if(message.getDest() != -1) {
	        		writer.write(message.getDest() + "," +
	        					 message.getType() + "," +
	        					 message.getID() + "," +
	        					 message.getChoice()  + "," +
	        					 message.getDate() + "," +
	        					 message.getHour() + "," +
	        					 message.getOriginalMessage()
	        					 + "\n"
	        				);
        		}
        	}
        	writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			try { if(writer != null) writer.close(); }
			catch (IOException e) { e.printStackTrace(); }
		}
	}
}
