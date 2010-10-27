package com.osp.ide.message.view.output;

import java.nio.InvalidMarkException;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.content.ITextContentDescriber;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.osgi.framework.internal.core.Tokenizer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.dialogs.ISelectionValidator;
import org.eclipse.ui.help.IWorkbenchHelpSystem;
import org.eclipse.ui.part.ViewPart;

import com.osp.ide.message.OSPMessage;
import com.osp.ide.message.socket.NetManager;

public class Output extends ViewPart {
	public static final String OUTPUT_VIEW_ID = "com.osp.ide.message.view.Output";

	private TableViewer 			messageTable;
	private OutputFilter 			outputFilter;
	
	private Clipboard 				clipBoard;
	private CopyOutputViewAction	copyAction;

	// flag값을 false -> true / true -> false 로 변환
	public static boolean ChangeFlag(boolean flag){
		return !flag;
	}
	/////////////////////////////////////////////////////////////////////
	// 선택 된 문장이 에러코드인지 아닌지 검사
	public static boolean isErrorCode(String str) {
		StringTokenizer stk = new StringTokenizer(str, " ");
		boolean flag = false;
		while (stk.hasMoreElements()) {
			String str1 = null;
			str1 = stk.nextToken();
			if (str1.equals("Error")) {
				if (stk.hasMoreElements()) {
					str1 = stk.nextToken();
					if (str1.equals("Code:")) {
						return ChangeFlag(flag);
					}
				} else {
					return flag;
				}
			}
		}
		return flag;
	}

	/////////////////////////////////////////////////////////////////////
	// 선택 된 문장에서 에러코드 추출
	public String getErrorCode(String str){
		String errorcode = null;
		StringTokenizer stk = new StringTokenizer(str, " ");
		while(stk.hasMoreElements())
		{
			String str1 = null;
			str1 = stk.nextToken();
			if(str1.equals("Code:")){
				errorcode = stk.nextToken();
			}
		}
		return errorcode;
	}
	/////////////////////////////////////////////////////////////////////
	
	@Override
	public void createPartControl(Composite parent) {
		NetManager netManager = NetManager.getInstance();
		
		netManager.setScrollLock(false);

		messageTable = new TableViewer(parent, SWT.H_SCROLL | SWT.V_SCROLL
				| SWT.MULTI | SWT.FULL_SELECTION | SWT.VIRTUAL | SWT.BORDER);
		messageTable.setContentProvider(new OutputContentProvider());
		messageTable.setLabelProvider(new OutputLabelProvider());
		netManager.setOutputViewer(messageTable);
		messageTable.setInput(netManager.getMessageList());
		messageTable.setItemCount(netManager.MAX_MESSAGE_SIZE);
		outputFilter = new OutputFilter();
		messageTable.addFilter(outputFilter);
		getSite().setSelectionProvider(messageTable);
		
		//HelpSystem을 가져옴
		final IWorkbenchHelpSystem helpSystem = 
			getSite().getWorkbenchWindow().getWorkbench().getHelpSystem();
		
		// 더블 클릭 이벤트 리스너 추가
		messageTable.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				// TODO Auto-generated method stub
				// 선택 된 메시지 추출
				StructuredSelection sel = (StructuredSelection) event.getSelection();
				OSPMessage mess = (OSPMessage) sel.getFirstElement();
				String str = mess.getOriginalMessage();
				/////////////////////////////////////////////////////////////////////
				// 에러 메시지인지 검사해서 아닐 경우 리스너 중지
				boolean flag = false;
				if((flag = isErrorCode(str)) == false)
					return ;
				/////////////////////////////////////////////////////////////////////
				// 에러코드 출력 : 테스트용 (이 부분에서 에러코드 검사 후 원하는 페이지 출력
				System.out.println(getErrorCode(str));
				/////////////////////////////////////////////////////////////////////
				// 에러 메시지일 경우 원하는 도움말 페이지 출력
				if(flag)
					helpSystem.displayHelpResource(
					"/com.osp.devguide.help/html/app_dev_process/error_codes.htm");
				/////////////////////////////////////////////////////////////////////
			}
		});

		final Table table = messageTable.getTable();
//		AutoResizeTableLayout layout = new AutoResizeTableLayout(table);	// For autoresize table column width
//		table.setLayout(layout);											// For autoresize table column width
		
		table.setHeaderVisible(false);
		table.setLinesVisible(true);

		TableColumn column = new TableColumn(table, SWT.LEAD);
		column.setWidth(1500);
//		layout.addColumnData(new ColumnWeightData(200));					// For autoresize table column width

		messageTable.refresh();
		
		createActions();
		hookGlobalActions();
		hookDragAndDrop();
		
//		netManager.addMessage("6,0,0,1,2010-02-05,09:52:43.468,1|1|4|65093");
	}

	public TableViewer getTableViewer() {
		return messageTable;
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

	public void dispose() {
		if(clipBoard != null)
			clipBoard.dispose();
		super.dispose();
	}

	public void openFilterDialog() {
		Display display = Display.getDefault();
		new FilterOptionDialog(display.getActiveShell()).open();

		outputFilter.LoadFilter();
		messageTable.refresh();
	}

	public void openReplaceDialog() {
		Display display = Display.getDefault();
		new ReplaceOptionDialog(display.getActiveShell()).open();
	}

	public void openFindDialog() {
		Display display = Display.getDefault();
		new FindDialog(display.getActiveShell()).open();
	}
	
	public void createActions() {
		copyAction = new CopyOutputViewAction(this, "Copy");
		copyAction.setImageDescriptor(PlatformUI.getWorkbench()
				.getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_COPY));
		copyAction.setDisabledImageDescriptor(PlatformUI.getWorkbench()
				.getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_COPY_DISABLED));
	}
	
	private void hookGlobalActions() {
		getViewSite().getActionBars().setGlobalActionHandler(
							ActionFactory.COPY.getId(), copyAction);
	}
	
	private void hookDragAndDrop() {
		new OutputDragSource(this, messageTable);
	}
	
	public String getSelectedOutput() {
		StringBuilder data = new StringBuilder();
		
		Table table = messageTable.getTable();
		int[] indexes = table.getSelectionIndices();
		for (int i = 0; i < indexes.length; i++) {
			TableItem item = table.getItem(indexes[i]);

			data.append(item.getText()).append(System.getProperty("line.separator"));
		}
		
		return data.toString();
	}
	
	public Clipboard getClipboard() {
		if(clipBoard == null)
			clipBoard = new Clipboard(getSite().getShell().getDisplay());
		
		return clipBoard;
	}
}
