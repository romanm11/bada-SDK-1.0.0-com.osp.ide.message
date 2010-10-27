package com.osp.ide.message.view.output;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;

public class CopyOutputViewAction extends Action {
	private Output 	outputView;
	
	public CopyOutputViewAction(Output output, String text) {
		super(text);
		outputView = output;
	}
	
	public void run() {
		String data = outputView.getSelectedOutput();
		
		if(data.length() == 0)
			return;
		try {
			outputView.getClipboard().setContents(
									new Object[] {data.toString()},
									new Transfer[] {TextTransfer.getInstance()});
		}
		catch(SWTError e) {
			e.printStackTrace();
		}
	}
}
