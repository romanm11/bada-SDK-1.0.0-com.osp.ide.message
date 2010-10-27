package com.osp.ide.message.view.output;

import java.util.Locale;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import com.osp.ide.message.socket.NetManager;

public class FindDialog extends Dialog {
	private int		current = -1;
	private Button check;

	public FindDialog(Shell parent) {
		super(parent);
		// TODO Auto-generated constructor stub
		setText("Find Dialog");
	}
	
	public void open() {
		Shell parent = getParent();
		Shell dialog = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		dialog.setSize(300, 130);
		dialog.setText("Find");
		
		Rectangle parentSize = parent.getBounds();
		Rectangle mySize = dialog.getBounds();
		
		int locX, locY;
		locX = (parentSize.width - mySize.width)/2+parentSize.x;
		locY = (parentSize.height - mySize.height)/2+parentSize.y;
		dialog.setLocation(locX, locY);
		
		createContents(dialog);
		
		Table table = NetManager.getInstance().getOutputViewer().getTable();
		table.setSelection(table.getItemCount()-1);

        dialog.open();
		
		Display display = parent.getDisplay();
		while(!dialog.isDisposed()) {
			if(!display.readAndDispatch())
				display.sleep();
		}
		return;
	}
	
	private void createContents(final Shell shell) {
		shell.setLayout(new GridLayout(4, true));

		// Show the message
	    Label label = new Label(shell, SWT.NONE);
	    label.setText("Enter a string :");
	    GridData data = new GridData();
	    data.horizontalSpan = 4;
	    label.setLayoutData(data);

	    // Display the input box
	    final Text text = new Text(shell, SWT.BORDER);
	    data = new GridData(GridData.FILL_HORIZONTAL);
	    data.horizontalSpan = 4;
	    text.setLayoutData(data);
	    
	    check =new Button(shell, SWT.CHECK); 
	    check.setText("Case Sensitive");
	    data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
	    data.horizontalSpan = 4;
	    check.setLayoutData(data);


	    Button prev = new Button(shell, SWT.PUSH);
	    prev.setText("<<");
	    data = new GridData(GridData.FILL_HORIZONTAL);
	    prev.setLayoutData(data);
	    prev.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				Table table = NetManager.getInstance().getOutputViewer().getTable();
				if(current == -1)
					current = table.getItemCount()-1;
				Find(text.getText(), false);
			}
	    });
	    
	    Button next = new Button(shell, SWT.PUSH);
	    next.setText(">>");
	    data = new GridData(GridData.FILL_HORIZONTAL);
	    next.setLayoutData(data);
	    next.addSelectionListener(new SelectionAdapter() {
	    	@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
	    		Table table = NetManager.getInstance().getOutputViewer().getTable();
	    		if(current == -1)
	    			current = table.getItemCount()-1;
				Find(text.getText(), true);
			}
	    });
	    
	    Label fill = new Label(shell, SWT.NONE);

	    // Create the cancel button and add a handler
	    // so that pressing it will set input to null
	    Button close = new Button(shell, SWT.PUSH);
	    close.setText("Close");
	    data = new GridData(GridData.FILL_HORIZONTAL);
	    close.setLayoutData(data);
	    close.addSelectionListener(new SelectionAdapter() {
	      public void widgetSelected(SelectionEvent event) {
	        shell.close();
	      }
	    });

	    // Set the OK button as the default, so
	    // user can type input and press Enter
	    // to dismiss
	    shell.setDefaultButton(prev);
	}

	public void Find(String find, boolean direction) {
		if(find.length() == 0)
			return;
		
		boolean caseSensitive = check.getSelection();
		
		if(!caseSensitive)
			find = find.toLowerCase(Locale.getDefault());
		
		Table table = NetManager.getInstance().getOutputViewer().getTable();
		TableItem[] items = table.getItems();
		NetManager.getInstance().setScrollLock(true);
		if(direction) {
			for(int i=current+1; i<items.length; i++) {
				String message = caseSensitive ? items[i].getText() : items[i].getText().toLowerCase(Locale.getDefault());
				if(message.indexOf(find) >= 0) {
					table.setSelection(i);
					current = i;
					break;
				}
			}
		}
		else {
			for(int i=current-1; i>=0; i--) {
				String message = caseSensitive ? items[i].getText() : items[i].getText().toLowerCase(Locale.getDefault());
				if(message.indexOf(find) >= 0) {
					table.setSelection(i);
					current = i;
					break;
				}
			}
		}
	}
}