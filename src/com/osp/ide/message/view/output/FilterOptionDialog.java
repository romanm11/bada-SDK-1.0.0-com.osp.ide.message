package com.osp.ide.message.view.output;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

import org.eclipse.core.runtime.Platform;
import org.eclipse.osgi.service.datalocation.Location;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.osp.ide.message.Constants;
import com.osp.ide.message.socket.NetManager;

public class FilterOptionDialog extends Dialog {
	Text	text;
	List	list;
	
	Button 	checkBtn;
	
	Button 	checkInfo, checkDebug, checkException;
	boolean info, debug, exception;

	public FilterOptionDialog(Shell parent) {
		super(parent);
		// TODO Auto-generated constructor stub
		
		info 		= NetManager.getInstance().isFilter_info();
		debug 		= NetManager.getInstance().isFilter_debug();
		exception 	= NetManager.getInstance().isFilter_exception();
	}
	
	public boolean open() {
		Shell parent = getParent();
		Shell dialog = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		dialog.setSize(320, 340);
		dialog.setText("Filtering Options");
		
		Rectangle parentSize = parent.getBounds();
		Rectangle mySize = dialog.getBounds();
		
		int locX, locY;
		locX = (parentSize.width - mySize.width)/2+parentSize.x;
		locY = (parentSize.height - mySize.height)/2+parentSize.y;
		dialog.setLocation(locX, locY);
		
		createContents(dialog);
		
		dialog.open();
		LoadFilter();
		
		Display display = parent.getDisplay();
		while(!dialog.isDisposed()) {
			if(!display.readAndDispatch())
				display.sleep();
		}

		return true;
	}

	private void createContents(final Shell dialog) {
		GridLayout layout = new GridLayout();
		layout.numColumns = 4;
		layout.makeColumnsEqualWidth = true;
		dialog.setLayout(layout);
		dialog.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		Group groupType = new Group(dialog, SWT.NONE);
		groupType.setText("Log Types");
		layout = new GridLayout();
		layout.numColumns = 3;
		groupType.setLayout(layout);
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		//gridData.grabExcessVerticalSpace = true;
		gridData.horizontalSpan = 4;
		groupType.setLayoutData(gridData);
		
		checkInfo = new Button(groupType, SWT.CHECK);
		gridData = new GridData();
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.CENTER;
		checkInfo.setText("INFO");
		checkInfo.setLayoutData(gridData);
		checkInfo.setSelection(info);
		checkInfo.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				info = checkInfo.getSelection();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		
		checkDebug = new Button(groupType, SWT.CHECK);
		gridData = new GridData();
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.CENTER;
		checkDebug.setText("DEBUG");
		checkDebug.setLayoutData(gridData);
		checkDebug.setSelection(debug);
		checkDebug.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				debug = checkDebug.getSelection();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		
		checkException = new Button(groupType, SWT.CHECK);
		gridData = new GridData();
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.CENTER;
		checkException.setText("EXCEPTION");
		checkException.setLayoutData(gridData);
		checkException.setSelection(exception);
		checkException.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				exception = checkException.getSelection();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		
		Group group = new Group(dialog, SWT.NONE);
		group.setText("Set Filtering List");
		layout = new GridLayout();
		layout.numColumns = 4;
		group.setLayout(layout);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalSpan = 4;
		group.setLayoutData(gridData);
		
		Label label = new Label(group, SWT.NONE);
		label.setText("Text :");
		
		text = new Text(group, SWT.BORDER );
		gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.verticalAlignment = SWT.CENTER;
		gridData.horizontalSpan = 3;
		text.setLayoutData(gridData);
		
		Label label2 = new Label(group, SWT.NONE);
		
		Button addBtn = new Button(group, SWT.NONE);
		addBtn.setText("Add");
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.verticalAlignment = GridData.CENTER;
		addBtn.setLayoutData(gridData);
		addBtn.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected( SelectionEvent event ) {
				if(text.getText().length() > 0) {
					boolean bFind = false;
					for(String item : list.getItems()) {
						if(item.compareTo(text.getText()) == 0) {
							bFind = true;
							MessageBox message = new MessageBox(dialog, SWT.ICON_ERROR | SWT.OK);
							message.setText("Error");
							message.setMessage("It's already exist!!!");
							message.open();
							text.setFocus();
							break;
						}
					}
					
					if(!bFind) {
						list.add(text.getText());
						text.setText("");
						text.setFocus();
					}
				}
			}
		});
		
		Button editBtn = new Button(group, SWT.NONE);
		editBtn.setText("Edit");
		editBtn.setLayoutData(gridData);
		editBtn.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected( SelectionEvent event ) {
				if(list.getSelectionIndex() != -1 && text.getText().length() > 0) {
					boolean bFind = false;
					for(String item : list.getItems()) {
						if(item.compareTo(text.getText()) == 0) {
							bFind = true;
							MessageBox message = new MessageBox(dialog, SWT.ICON_ERROR | SWT.OK);
							message.setText("Error");
							message.setMessage("It's already exist!!!");
							message.open();
							text.setFocus();
							break;
						}
					}
					
					if(!bFind) {
						list.setItem(list.getSelectionIndex(), text.getText());
						text.setText("");
					}
				}
			}
		});
		
		Button deleteBtn = new Button(group, SWT.NONE);
		deleteBtn.setText("Delete");
		deleteBtn.setLayoutData(gridData);
		deleteBtn.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected( SelectionEvent event ) {
				if(list.getSelectionIndex() != -1) {
					MessageBox message = new MessageBox(dialog, SWT.ICON_QUESTION | SWT.OK | SWT.CANCEL);
					message.setText("Question");
					message.setMessage("Are you sure?");
					if( message.open() == SWT.OK ) {
						list.remove(list.getSelectionIndex());
						text.setText("");
					}
				}
			}
		});
		
		list = new List(group, SWT.SINGLE |SWT.BORDER | SWT.V_SCROLL);
		gridData = new GridData();
		gridData.horizontalSpan = 4;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
//		int listHeight = list.getItemHeight()*12;
//		Rectangle trim = list.computeTrim(0, 0, 0, listHeight);
//		gridData.heightHint = trim.height;
		list.setLayoutData(gridData);
		list.addSelectionListener(new SelectionAdapter () {
			public void widgetSelected( SelectionEvent event ) {
				if(list.getSelectionIndex() != -1) {
					text.setText(list.getItem(list.getSelectionIndex()));
				}
			}
		});

		
		Group group2 = new Group(dialog, SWT.NONE);
		group2.setText("Filtering Options");
		layout = new GridLayout();
		layout.numColumns = 1;
		group2.setLayout(layout);
		gridData = new GridData();
		gridData.horizontalSpan = 4;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.CENTER;
		group2.setLayoutData(gridData);
		
		checkBtn = new Button(group2, SWT.CHECK);
		gridData = new GridData();
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.CENTER;
		checkBtn.setText("Case sensitive");
		checkBtn.setLayoutData(gridData);
		
		Label label3 = new Label(dialog, SWT.NONE);
		Label label4 = new Label(dialog, SWT.NONE);
		
		Button ok = new Button(dialog, SWT.NONE);
		ok.setText("OK");
		gridData = new GridData(GridData.FILL);
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.CENTER;
		ok.setLayoutData(gridData);
		ok.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected( SelectionEvent event ) {
				NetManager netManager = NetManager.getInstance();
				netManager.setFilter_info(info);
				netManager.setFilter_debug(debug);
				netManager.setFilter_exception(exception);
				
				SaveFilter();
				
				dialog.close();
			}
		});

		Button cancel = new Button(dialog, SWT.NONE);
		cancel.setText("Cancel");
		gridData = new GridData(GridData.FILL);
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.CENTER;
		cancel.setLayoutData(gridData);
		cancel.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected( SelectionEvent event ) {
				dialog.close();
			}
		});

		dialog.setDefaultButton(ok);
	}
	
	public void SaveFilter() {
		Location location = Platform.getConfigurationLocation();
        if (location == null || location.isReadOnly()) {
			return;
		}
        
        BufferedWriter writer = null;
        try {
        	String filename = location.getURL().getPath() + Constants.FILTER_FILE_NAME;
//        	writer = new BufferedWriter(new FileWriter(filename));
        	writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filename), Charset.defaultCharset()));
        	writer.write("Case sensitive="+String.valueOf(checkBtn.getSelection())+"\n");
        	for(int i=0; i<list.getItemCount(); ++i) {
        		writer.write(list.getItem(i) + "\n");
        	}
		} catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			try { if(writer != null) writer.close(); }
			catch (IOException e) { e.printStackTrace(); }
		}
	}
	
	public void LoadFilter() {
		Location location = Platform.getConfigurationLocation();
		if (location == null || location.isReadOnly()) {
			return;
		}
		
		BufferedReader reader = null;
		try {
			String filename = location.getURL().getPath() + Constants.FILTER_FILE_NAME;
			File fi = new File(filename);
        	if(!fi.exists()) return;
			
//			reader = new BufferedReader(new FileReader(filename));
        	reader = new BufferedReader(new InputStreamReader(new FileInputStream(filename), Charset.defaultCharset()));
			String line = reader.readLine();
			if(line != null) {
				String[] l = line.split("=");
				if(l[0].equals("Case sensitive")) {
					if(l[1].equals("true"))
						checkBtn.setSelection(true);
					else
						checkBtn.setSelection(false);
				}
			}
			while((line = reader.readLine()) != null) {
				if(line.length() > 0) {
					list.add(line);
				}
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