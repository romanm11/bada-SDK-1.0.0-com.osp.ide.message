package com.osp.ide.message.wizards;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import com.osp.ide.IConstants;
import com.osp.ide.message.socket.NetManager;

public class OSPImportWizard extends Wizard implements IImportWizard {
	
	OSPImportWizardPage		wizardPage;
	private String			messageFile = "";
	
	public OSPImportWizard() {
		super();
		setWindowTitle("Import");
	}

	@Override
	public boolean performFinish() {
		// TODO Auto-generated method stub

		if(messageFile.isEmpty())
			return false;
		
		ImportEventLog(messageFile);
		
		return true;
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		// TODO Auto-generated method stub
		
	}
	
	public void addPages() {
		wizardPage = new OSPImportWizardPage(this);
		addPage(wizardPage);
	}

	public void setMessageFile(String messageFile) {
		this.messageFile = messageFile;
	}

	public String getMessageFile() {
		return messageFile;
	}
	
	private void ImportEventLog(String filename) {
		BufferedReader reader = null;
		NetManager netManager = NetManager.getInstance();
		try {
			netManager.clearOspMessageList();
			netManager.getApplicationList().clear();
			
//			reader = new BufferedReader(new FileReader(filename));
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(filename), Charset.defaultCharset()));
			String line="";
			while((line = reader.readLine()) != null) {
				if(line.length() > 0 ) {
					netManager.addMessage(line);
				}
			}
			reader.close();
			
			if(!netManager.getMessageList().isEmpty()) {
				if(netManager.getOutputViewer() == null) {
					IWorkbenchPage page = PlatformUI.getWorkbench()
							.getActiveWorkbenchWindow().getActivePage();
					try {
						page.showView(IConstants.VIEW_ID_OUTPUT);
					} catch (PartInitException e) {
						e.printStackTrace();
					}
				}
				netManager.getOutputViewer().refresh();
			}
		} catch(IOException e) {
			e.printStackTrace();
		}
		finally {
			try { if(reader != null) reader.close(); }
			catch (IOException e) { e.printStackTrace(); }
		}
	}
}
