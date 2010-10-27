package com.osp.ide.message.view.resourcemonitor;

import java.util.List;

import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import com.osp.ide.message.socket.NetManager;

public class ResourceMonitor extends ViewPart {
	public static final String RESOURCE_MONITOR_VIEW_ID = "com.osp.ide.message.view.resourcemonitor.Resourcemonitor";

	private TableViewer		tableViewer;
	private TreeViewer		treeViewer;

	@Override
	public void createPartControl(Composite parent) {
		// TODO Auto-generated method stub
		NetManager netManager = NetManager.getInstance();

		SashForm sashForm = new SashForm(parent, SWT.VERTICAL);

		tableViewer = new TableViewer(sashForm, SWT.BORDER|SWT.SINGLE|SWT.FULL_SELECTION);
		tableViewer.setContentProvider(new ResourceTableContentProvider());
		tableViewer.setLabelProvider(new ResourceTableLabelProvider());

		tableViewer.setInput(netManager.getApplicationList());
		netManager.setResourceViewer(tableViewer);

		treeViewer = new TreeViewer(sashForm, SWT.BORDER|SWT.H_SCROLL|SWT.V_SCROLL|SWT.VIRTUAL|SWT.SINGLE|SWT.FULL_SELECTION);
		treeViewer.setContentProvider(new ResourceContentProvider());
		treeViewer.setLabelProvider(new ResourceLabelProvider());
		
		netManager.setResourceTreeViewer(treeViewer);

		tableViewer.addDoubleClickListener(new IDoubleClickListener(){
			public void doubleClick(DoubleClickEvent event) {
				try {
					Object[] elements = treeViewer.getExpandedElements();
					int length = elements.length;
					if( length > 0){
						for(int i=0;i<length;i++)
							treeViewer.setExpandedState(elements[i], false);
					}
					else{
						treeViewer.expandAll();
					}
				}
				catch(Exception exp) {
					System.out.println(exp.getMessage());
				}
			}
		});

		tableViewer.addSelectionChangedListener(new ISelectionChangedListener()
	    {
			public void selectionChanged(SelectionChangedEvent event)
			{
				try {
					IStructuredSelection selection = (IStructuredSelection) event.getSelection();
	
					BadaApplication selected_app = (BadaApplication)selection.getFirstElement();
			        if(selected_app == null)
			        {
			        	treeViewer.setInput(null);
			        	return ;
			        }
			        
			        if(selected_app.getResourceMonitorMessageList().size() > 0) {
				        if( ((List<ResourceMonitorMessage>)treeViewer.getInput()) != selected_app.getResourceMonitorMessageList()) {
				        	treeViewer.setInput(selected_app.getResourceMonitorMessageList());
				        	treeViewer.expandAll();
				        }
			        }
				}
				catch(Exception e) {
					System.out.println(e.getMessage());
				}
			}
	    });

		treeViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent e) {
				try {
					IStructuredSelection selection = (IStructuredSelection) e.getSelection();
					if (selection.size() < 1) return;
					Object element = selection.getFirstElement();
					if (treeViewer.getExpandedState(element))
						treeViewer.collapseToLevel(element, 1);
				}
				catch(Exception exp) {
					System.out.println(exp.getMessage());
				}
			}
		});
		
		treeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				// TODO Auto-generated method stub
				try {
					IStructuredSelection selection = (IStructuredSelection) event.getSelection();
					if (selection.size() < 1) return;
					Object element = selection.getFirstElement();
					if (!treeViewer.getExpandedState(element))
						treeViewer.expandToLevel(element, 1);
				}
				catch(Exception exp) {
					System.out.println(exp.getMessage());
				}
			}
		});

		sashForm.setWeights(new int[] {25, 75});

/*		// app(process) 추가
		NetManager.getInstance().addMessage("1,1,1,0,2009-05-22,04:07:30.200,12345|01:01:01|PROCESSMGR:00> 1,HelloOspWorld");	// Process
*/
//		NetManager.getInstance().addMessage("1,1,1,0,2009-05-22,04:07:30.200,[11100:11100]0039:683,93bt1p123e,HelloWorld");
//		NetManager.getInstance().addMessage("1,1,1,0,2009-05-22,04:07:30.200,[11100:11100]0039:683,95bt1p123e,Hello_BKs");
//		NetManager.getInstance().addMessage("1,1,1,0,2009-05-22,04:07:30.200,[10100:10100]0039:683,93bt1p123e,MyThread,128");	// Thread
//		NetManager.getInstance().addMessage("1,1,1,0,2009-05-22,04:07:30.200,[10400:10400]0039:683,93bt1p123e");				// Timer
//		NetManager.getInstance().addMessage("1,1,1,0,2009-05-22,04:07:30.200,[10400:10400]0039:683,93bt1p123e");				// Timer
//		NetManager.getInstance().addMessage("1,1,1,0,2009-05-22,04:07:30.200,[10400:10400]0039:683,93bt1p123e");				// Timer
//		NetManager.getInstance().addMessage("1,1,1,0,2009-05-22,04:07:30.200,[10500:10500]0039:683,93bt1p123e,MyFile.txt");		// File
//		NetManager.getInstance().addMessage("1,1,1,0,2009-05-22,04:07:30.200,[10700:10700]0039:683,93bt1p123e,MyDB");			// Database
//		NetManager.getInstance().addMessage("1,1,1,0,2009-05-22,04:07:30.200,[10800:10800]0039:683,93bt1p123e,MyRegistry");		// Registry
//		NetManager.getInstance().addMessage("1,1,1,0,2009-05-22,04:07:30.200,[11000:11000]0039:683,93bt1p123e,MyForm");			// Form
//		NetManager.getInstance().addMessage("1,1,1,0,2009-05-22,04:07:30.200,[11000:11000]0039:683,93bt1p123e,YouForm");		// Form
//		NetManager.getInstance().addMessage("1,1,1,0,2009-05-22,04:07:30.200,[10900:10900]0039:683,93bt1p123e");				// Socket
/*
		NetManager.getInstance().addMessage("1,1,1,0,2009-05-22,04:07:32.200,12346|01:01:01|MEMMGR:05>111,1,1,1024,12,HelloOspWorld.cpp");	// Memory leak
		NetManager.getInstance().addMessage("1,1,1,0,2009-05-22,04:07:30.200,1,STATUS,5,10");	// Memory status

		// 삭제/추가 Test
		 * 
		 */
//		NetManager.getInstance().addMessage("1,1,1,0,2009-05-22,04:07:30.200,[10100:10100]0039:683,93bt1p123e,YourThread,512");	// Thread
//		NetManager.getInstance().addMessage("1,1,1,0,2009-05-22,04:07:30.200,[11000:11001]0039:683,93bt1p123e,YouForm");		// Form
//		NetManager.getInstance().addMessage("1,1,1,0,2009-05-22,04:07:30.200,[10400:10401]0039:683,93bt1p123e");				// Timer
//		NetManager.getInstance().addMessage("1,1,1,0,2009-05-22,04:07:30.200,[10900:10900]0039:683,93bt1p123e");				// Socket
/*		// app(process) 추가
		NetManager.getInstance().addMessage("1,1,1,0,2009-05-22,04:07:30.200,12345|01:01:01|PROCESSMGR:00> 2,Hello");	// Process

		NetManager.getInstance().addMessage("[10100:10100]SSSS:mmmm,2,MyThread,128");	// Thread
*/
//		NetManager.getInstance().addMessage("[10400:10400]0039:683,93bt1p123e");				// Timer
//		NetManager.getInstance().addMessage("[10400:10400]0039:683,93bt1p123e");				// Timer
//		NetManager.getInstance().addMessage("[10400:10400]0039:683,93bt1p123e");				// Timer
//		NetManager.getInstance().addMessage("[10500:10500]0039:683,93bt1p123e,MyFile.txt");		// File
//		NetManager.getInstance().addMessage("[10700:10700]0039:683,93bt1p123e,MyDB");			// Database
//		NetManager.getInstance().addMessage("[10800:10800]0039:683,93bt1p123e,MyRegistry");		// Registry
//		NetManager.getInstance().addMessage("[11000:11000]0039:683,93bt1p123e,MyForm");			// Form
//		NetManager.getInstance().addMessage("[11000:11000]0039:683,93bt1p123e,YouForm");		// Form
//		NetManager.getInstance().addMessage("[10900:10900]0039:683,93bt1p123e");				// Socket
//
//		NetManager.getInstance().addMessage("[10500:10500]0039:683,93bt1p123e,1File.txt");		// File
//		NetManager.getInstance().addMessage("[10500:10500]0039:683,93bt1p123e,2File.txt");		// File
//		NetManager.getInstance().addMessage("[10500:10500]0039:683,93bt1p123e,22File.txt");		// File
//		NetManager.getInstance().addMessage("[10500:10500]0039:683,93bt1p123e,11File.txt");		// File

/*		// app(process) 삭제
		NetManager.getInstance().addMessage("1,1,1,0,2009-05-22,04:07:30.200,12345|01:01:01|PROCESSMGR:01> 1");	// Process
*/
		// resource 추가
//		NetManager.getInstance().addMessage("[10400:10400]0039:683,93bt1p123e");				// Timer
//		NetManager.getInstance().addMessage("[10400:10400]0039:683,93bt1p123e");				// Timer
/*
		NetManager.getInstance().addMessage("[10500:10500]SSSS:mmmm,1,MyFile.txt");		// File
		NetManager.getInstance().addMessage("1,1,1,0,2009-05-22,04:07:32.200,12346|01:01:01|MEMMGR:05>111,1,1,1024,12,HelloOspWorld.cpp");	// Memory leak
		NetManager.getInstance().addMessage("1,1,1,0,2009-05-22,04:07:32.200,12346|01:01:01|MEMMGR:05>111,1,1,128,5,HelloOspWorld.h");	// Memory leak
*/	
	}

	@Override
	public void setFocus() {
	}

	public void dispose() {
		//NetManager.getInstance().getApplicationList().clear();
	}
}
