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
import java.util.HashMap;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.osgi.service.datalocation.Location;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import com.osp.ide.message.view.output.OutputFilter;


public class ReplaceOptionDialog extends Dialog {
	private static final String USE_PROPERTY = "use";
	private static final String ORIGINAL_PROPERTY = "original";
	private static final String REPLACEMENT_PROPERTY = "replacement";
	  
	private static final String NEW_KEY_STRING = "New Key";
	private static final String NEY_VALUE_STRING = "New Value";
	
//	private static final String CURRENT_FILE_NAME = "replace_filename";
//	private IMemento	fMemento;
	
	private TableViewer viewer;
	private Table table;

	public ReplaceOptionDialog(Shell parent) {
		super(parent);
		// TODO Auto-generated constructor stub
	}
	
	private class NewRowAction extends Action {
		public NewRowAction() {
			super("Insert New Row");
		}
		
		public void run() {
			int bRet = isExist(NEW_KEY_STRING);
			if(bRet > 0)
				return;
			
			EditableTableItem newItem = new EditableTableItem(USE_PROPERTY, NEW_KEY_STRING, NEY_VALUE_STRING);
			viewer.add(newItem);
		}
	}
	
	public boolean open() {
		Shell parent = getParent();
		Shell dialog = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		dialog.setSize(500, 400);
		dialog.setText("Replace Options");
		
		Rectangle parentSize = parent.getBounds();
		Rectangle mySize = dialog.getBounds();
		
		int locX, locY;
		locX = (parentSize.width - mySize.width)/2+parentSize.x;
		locY = (parentSize.height - mySize.height)/2+parentSize.y;
		dialog.setLocation(locX, locY);
		
        createContents(dialog);

        dialog.open();
		
		Display display = parent.getDisplay();
		while(!dialog.isDisposed()) {
			if(!display.readAndDispatch())
				display.sleep();
		}
		return false;
	}
	
	private void createContents(final Shell dialog) {
		GridLayout layout = new GridLayout();
		layout.numColumns = 5;
		layout.makeColumnsEqualWidth = true;
		dialog.setLayout(layout);
		dialog.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		Group group = new Group(dialog, SWT.NONE);
		layout = new GridLayout();
		layout.numColumns = 1;
		group.setLayout(layout);
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalSpan = 4;
		gridData.verticalSpan = 10;
		group.setLayoutData(gridData);

		table = new Table(group, SWT.FILL | SWT.FULL_SELECTION | SWT.CHECK);
		viewer = createTable(table);
		
		attachContentProvider(viewer);
	    attachLabelProvider(viewer);
	    attachCellEditors(viewer, table);
		
		createGroup1(dialog);
		createGroup2(dialog);
		
		MenuManager popupMenu = new MenuManager();
		IAction newRowAction = new NewRowAction();
		popupMenu.add(newRowAction);
		Menu menu = popupMenu.createContextMenu(table);
		table.setMenu(menu);
		
		Label label1 = new Label(dialog, SWT.NONE);
		Label label2 = new Label(dialog, SWT.NONE);
		Label label3 = new Label(dialog, SWT.NONE);
		Label label4 = new Label(dialog, SWT.NONE);
		Label label5 = new Label(dialog, SWT.NONE);
		Label label6 = new Label(dialog, SWT.NONE);
		Label label7 = new Label(dialog, SWT.NONE);
		
		Group group3 = new Group(dialog, SWT.NONE);
		layout = new GridLayout();
		layout.makeColumnsEqualWidth = false;
		group3.setLayout(layout);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.END;
		group3.setLayoutData(gridData);
		
		Button ok = new Button(group3, SWT.NONE);
		ok.setText("OK");
		gridData = new GridData(GridData.FILL);
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.END;
		ok.setLayoutData(gridData);
		ok.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected( SelectionEvent event ) {
				HashMap<String, String> hm = new HashMap<String, String>();
				for(TableItem item : table.getItems()) {
					if(item.getChecked())
						hm.put(item.getText(1), item.getText(2));
				}
				OutputFilter.setReplaceList(hm);
				dialog.close();
			}
		});

		Button cancel = new Button(group3, SWT.NONE);
		cancel.setText("Cancel");
		gridData = new GridData(GridData.FILL);
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.END;
		cancel.setLayoutData(gridData);
		cancel.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected( SelectionEvent event ) {
				dialog.close();
			}
		});

		dialog.setDefaultButton(ok);
	}
	
	private TableViewer createTable(final Table tbl) {
		TableViewer tableViewer = new TableViewer(tbl);
		
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalSpan = 4;
		tbl.setLayoutData(gridData);
		
		TableLayout layout = new TableLayout();
		layout.addColumnData(new ColumnWeightData(6, 10, true));
		layout.addColumnData(new ColumnWeightData(47, 55, true));
		layout.addColumnData(new ColumnWeightData(47, 55, true));
		tbl.setLayout(layout);
		
		TableColumn use = new TableColumn(tbl, SWT.CENTER);
		use.setText("U");
		TableColumn original = new TableColumn(tbl, SWT.CENTER);
		original.setText("Original");
		TableColumn replacement = new TableColumn(tbl, SWT.CENTER);
		replacement.setText("Replacement");
		tbl.setHeaderVisible(true);
		tbl.setLinesVisible(true);
		return tableViewer;
	}
	
	private void createGroup1(final Shell shell) {
		Group group1 = new Group(shell, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.makeColumnsEqualWidth = false;
		group1.setLayout(layout);
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.BEGINNING;
		group1.setLayoutData(gridData);
		
		Button clear = new Button(group1, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = false;
		gridData.verticalAlignment = GridData.CENTER;
		clear.setLayoutData(gridData);
		clear.setText("Clear");
		clear.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected( SelectionEvent event ) {
				if(table.getSelectionIndex() != -1) {
					MessageBox message = new MessageBox(shell, SWT.ICON_QUESTION | SWT.OK | SWT.CANCEL);
					message.setText("Question");
					message.setMessage("Are you sure?");
					if( message.open() == SWT.OK ) {
						table.remove(table.getSelectionIndex());
						//table.setText("");
					}
				}
			}
		});
		
		Button clearAll = new Button(group1, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.verticalAlignment = GridData.CENTER;
		clearAll.setLayoutData(gridData);
		clearAll.setText("Clear All");
		clearAll.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected( SelectionEvent event ) {
				if(table.getItemCount() > 0) {
					MessageBox message = new MessageBox(shell, SWT.ICON_QUESTION | SWT.OK | SWT.CANCEL);
					message.setText("Question");
					message.setMessage("Are you sure?");
					if( message.open() == SWT.OK ) {
						table.removeAll();
						//table.setText("");
					}
				}
			}
		});
	}
	
	private void createGroup2(final Shell shell) {
		class Open implements SelectionListener {
			public void widgetSelected(SelectionEvent event) {
				Location location = Platform.getConfigurationLocation();
				if (location == null || location.isReadOnly()) {
					return;
				}
				
				FileDialog fd = new FileDialog(shell, SWT.OPEN);
				fd.setText("Open");
		        fd.setFilterPath(location.getURL().getPath());//"C:/");
		        String[] filterExt = { "*.rpl", "*.*" };
		        fd.setFilterExtensions(filterExt);
		        String selected = fd.open();
		        if(selected.length() == 0)
		        	return;
		        LoadReplaceItems(selected);
			}
			public void widgetDefaultSelected(SelectionEvent event) {
			}
		}
		class Save implements SelectionListener {
			public void widgetSelected(SelectionEvent event) {
				Location location = Platform.getConfigurationLocation();

				FileDialog fd = new FileDialog(shell, SWT.SAVE);
		        fd.setText("Save");
		        fd.setFilterPath(location.getURL().getPath());//"C:/");
		        String[] filterExt = { "*.rpl", "*.*" };
		        fd.setFilterExtensions(filterExt);
		        String selected = fd.open();
		        if(selected.length()==0)
		        	return;
		        SaveRaplceItems(selected);
			}
			public void widgetDefaultSelected(SelectionEvent event) {
			}
		}
		
		Group group2 = new Group(shell, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.makeColumnsEqualWidth = false;
		group2.setLayout(layout);
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.BEGINNING;
		group2.setLayoutData(gridData);
		
		Button load = new Button(group2, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.CENTER;
		gridData.grabExcessHorizontalSpace = true;
		load.setLayoutData(gridData);
		load.setText("Load");
		load.addSelectionListener(new Open());
		
		Button save = new Button(group2, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.CENTER;
		save.setLayoutData(gridData);
		save.setText("Save");
		save.addSelectionListener(new Save());
	}
	
	private void attachContentProvider(TableViewer tableViewer) {
		tableViewer.setContentProvider(new IStructuredContentProvider() {

			public Object[] getElements(Object inputElement) {
				return (Object[])inputElement;
			}

			public void dispose() {
			}

			public void inputChanged(Viewer viewer, Object oldInput,
					Object newInput) {
			}
		});
	}
	
	private void attachLabelProvider(TableViewer tableViewer) {
		tableViewer.setLabelProvider(new ITableLabelProvider() {

			public Image getColumnImage(Object element, int columnIndex) {
				return null;
			}

			public String getColumnText(Object element, int columnIndex) {
				switch(columnIndex) {
				case 0:
					return null;//Boolean.toString(((EditableTableItem) element).use);
				case 1:
					return ((EditableTableItem) element).original;
				case 2:
					return ((EditableTableItem) element).replacement;
				}
				return null;
			}

			public void addListener(ILabelProviderListener listener) {
			}

			public void dispose() {
			}

			public boolean isLabelProperty(Object element, String property) {
				return false;
			}

			public void removeListener(ILabelProviderListener listener) {
			}
			
		});
	}
	
	private void attachCellEditors(final TableViewer tableViewer, Composite parent) {
		tableViewer.setCellModifier(new ICellModifier() {
			public boolean canModify(Object element, String property) {
				return true;
			}

			public Object getValue(Object element, String property) {
				if(USE_PROPERTY.equals(property))
					return Boolean.toString(((EditableTableItem)element).use);
				else if(ORIGINAL_PROPERTY.equals(property))
					return((EditableTableItem)element).original;
				else
					return((EditableTableItem)element).replacement;
			}

			public void modify(Object element, String property, Object value) {
				TableItem tableItem = (TableItem)element;
				EditableTableItem data = (EditableTableItem)tableItem.getData();
				int bRet = isExist((String)value);
				if(ORIGINAL_PROPERTY.equals(property) && bRet > 0)
					return;
				
				if(USE_PROPERTY.equals(property))
					data.use = Boolean.valueOf(value.toString());
				if(ORIGINAL_PROPERTY.equals(property))
					data.original = value.toString();
				else
					data.replacement = value.toString();
				
				tableViewer.refresh(data);
			}
		});
		
		tableViewer.setCellEditors(new CellEditor[] { new CheckboxCellEditor(parent), new TextCellEditor(parent), new TextCellEditor(parent) });
		tableViewer.setColumnProperties(new String[] {USE_PROPERTY, ORIGINAL_PROPERTY, REPLACEMENT_PROPERTY});
	}
	
	public void SaveRaplceItems(String filename) {
		BufferedWriter writer = null;
		try {
        	HashMap<String, String> hm = new HashMap<String, String>();
//        	writer = new BufferedWriter(new FileWriter(filename));
        	writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filename), Charset.defaultCharset()));
        	for(TableItem item : table.getItems()) {
        		writer.write(Boolean.toString(item.getChecked()) + "\t" + item.getText(1) + "\t" + item.getText(2) + "\n");
        		if(item.getChecked()) 
        			hm.put(item.getText(1), item.getText(2));
        	}
        	OutputFilter.setReplaceList(hm);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally {
			try { if(writer != null) writer.close(); }
			catch (IOException e) { e.printStackTrace(); }
		}
	}
	
	public void LoadReplaceItems(String filename) {
		BufferedReader reader = null;
		try {
			File fi = new File(filename);
        	if(!fi.exists()) return;
        	
			table.removeAll();
			HashMap<String, String> hm = new HashMap<String, String>();
//			reader = new BufferedReader(new FileReader(filename));
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(filename), Charset.defaultCharset()));
			String line="";
			while((line = reader.readLine()) != null) {
				if(line.length() > 0) {
					String[] item = line.split("\t");
					TableItem newItem = new TableItem(table, SWT.NONE);
					boolean bChecked = Boolean.valueOf(item[0]);
					newItem.setChecked(bChecked);
					newItem.setText(1, item[1]);
					newItem.setText(2, item[2]);
					if(bChecked)
						hm.put(item[1], item[2]);
				}
			}
			OutputFilter.setReplaceList(hm);
		} catch(IOException e) {
			e.printStackTrace();
		}
		finally {
			try { if(reader != null) reader.close(); }
			catch (IOException e) { e.printStackTrace(); }
		}
	}
	
	public int isExist(String key) {
		int bRet = 0;
		TableItem[] items = table.getItems();
		for(TableItem i : items) {
			if(key.compareTo(i.getText(1)) == 0) {
				bRet++;
			}
		}
		return bRet;
	}
}

class EditableTableItem {
	public boolean use;
	public String original;
	public String replacement;

	public EditableTableItem(String u, String o, String r) {
		use = true;
		original = o;
		replacement = r;
	}
}
