package com.osp.ide.message.view.output;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.List;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.osgi.service.datalocation.Location;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;

import com.osp.ide.message.OSPMessage;
import com.osp.ide.message.socket.NetManager;

public class CommonActionDelegate implements IViewActionDelegate {
	private final static String TOOL_ACTIONS_IMPORT = "Import";
	private final static String TOOL_ACTIONS_EXPORT = "Export";
	private final static String TOOL_ACTIONS_SCROLL = "Scroll";
	private final static String TOOL_ACTIONS_CLEAR = "Clear";

	Output output;
	
	@Override
	public void init(IViewPart view) {
		// TODO Auto-generated method stub
		this.output = (Output) view;
	}

	@Override
	public void run(IAction action) {
		// TODO Auto-generated method stub
		String id = action.getText();
		NetManager netManager = NetManager.getInstance();
		if(id.compareTo(TOOL_ACTIONS_IMPORT)==0) {
			Import();
		}
		else if(id.compareTo(TOOL_ACTIONS_EXPORT)==0) {
			Export();
		}
		else if(id.compareTo(TOOL_ACTIONS_SCROLL)==0) {
			netManager.setScrollLock(!NetManager.getInstance().isScollLock());
		}
		else if(id.compareTo(TOOL_ACTIONS_CLEAR)==0) {
			if( MessageDialog.openConfirm(null, "Confirm", "Are you sure?") ) {
				netManager.clearOspMessageList();
				netManager.getOutputViewer().refresh();
			}
		}
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		// TODO Auto-generated method stub

	}

	private void Export() {
		Shell shell = new Shell();
		
		Location location = Platform.getConfigurationLocation();
		if (location == null || location.isReadOnly()) {
			return;
		}
		
		FileDialog fd = new FileDialog(shell, SWT.SAVE);
		fd.setText("Save");
		fd.setFilterPath(location.getURL().getPath());//"C:/");
		String[] filterExt = { "*.log", "*.*" };
		fd.setFilterExtensions(filterExt);
		String selected = fd.open();
		if(selected.length() == 0)
			return;
	        
	    ExportEventLog(selected);
	}
	
	private void ExportEventLog(String filename) {
		BufferedWriter writer = null;
		try {
			final List<OSPMessage> list = NetManager.getInstance().getMessageList();
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
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally {
			try { if(writer != null) writer.close(); }
			catch (IOException e) { e.printStackTrace(); }
		}
	}
	
	private void Import() {
		Shell shell = new Shell();
		
		Location location = Platform.getConfigurationLocation();
		if (location == null || location.isReadOnly()) {
			return;
		}
		
		FileDialog fd = new FileDialog(shell, SWT.OPEN);
		fd.setText("Open");
		fd.setFilterPath(location.getURL().getPath());//"C:/");
		String[] filterExt = { "*.log", "*.*" };
		fd.setFilterExtensions(filterExt);
		String selected = fd.open();
		if(selected.length() == 0)
			return;
	        
	    ImportEventLog(selected);
	}
	
	private void ImportEventLog(String filename) {
		BufferedReader reader = null;
		try {
//			reader = new BufferedReader(new FileReader(filename));
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(filename), Charset.defaultCharset()));
			String line="";
			while((line = reader.readLine()) != null) {
				if(line.length() > 0 ) {
					NetManager.getInstance().addMessage(line);
				}
			}
			output.getTableViewer().refresh();
		} catch(IOException e) {
			e.printStackTrace();
		}
		finally {
			try { if(reader != null) reader.close(); }
			catch (IOException e) {	e.printStackTrace(); }
		}
	}
}
