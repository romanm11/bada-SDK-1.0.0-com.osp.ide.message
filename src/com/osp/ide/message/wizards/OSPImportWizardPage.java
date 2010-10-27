package com.osp.ide.message.wizards;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.osgi.service.datalocation.Location;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;


public class OSPImportWizardPage extends WizardPage {
	private Text	text;
	
	private OSPImportWizard				ospWizard;
	
	public OSPImportWizardPage(OSPImportWizard wizard) {
		super("BadaImportPage");
		// TODO Auto-generated constructor stub

		setTitle("Imports Output Messages");
		setDescription("Select a log file for output messages.");
		setPageComplete(false);
		
		ospWizard = wizard;
	}

	@Override
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
        composite.setFont(parent.getFont());
		
        initializeDialogUnits(parent);
        
        GridLayout layout = new GridLayout(1, false);
        composite.setLayout(layout);
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));
        
        Composite comp = new Composite(composite, SWT.NULL);
        comp.setFont(composite.getFont());
        layout = new GridLayout(3, false);
        comp.setLayout(layout);
        comp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Label label = new Label(comp, SWT.NONE);
        label.setText("Input File:");
        label.setLayoutData(new GridData());
        
        text = new Text(comp, SWT.BORDER);
        text.setLayoutData(new GridData());
        text.setLayoutData(new GridData(GridData.FILL_BOTH));
        text.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
//				valueChanged();
			}
		});  
        
		Button browse = new Button(comp, SWT.PUSH);
		browse.setText("Browse...");
		browse.setFont(composite.getFont());
		browse.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent evt) {
            	Shell shell = new Shell();
        		
        		Location location = Platform.getConfigurationLocation();
        		if (location == null || location.isReadOnly()) {
        			return;
        		}
        		
        		FileDialog fd = new FileDialog(shell, SWT.OPEN | SWT.SINGLE);
        		fd.setText("Open");
        		fd.setFilterPath(location.getURL().getPath());//"C:/");
        		String[] filterExt = { "*.log", "*.*" };
        		fd.setFilterExtensions(filterExt);
        		String filename = fd.open();
        		text.setText(filename);
        		ospWizard.setMessageFile(filename);
        		
        		setPageComplete(true);
            }
        });
		
		setControl(composite);
		text.setFocus();
	}
}