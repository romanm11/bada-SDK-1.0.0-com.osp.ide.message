package com.osp.ide.message.view.output;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;

public class UtilityActionDelegate implements IViewActionDelegate {
	private static final String FILTER_UTILITY 			= "Filter";
	private static final String REPLACE_UTILITY 		= "Replace";
	private static final String FIND_UTILITY 			= "Find";

	Output output;
	
	@Override
	public void init(IViewPart view) {
		// TODO Auto-generated method stub
		this.output = (Output)view;
	}

	@Override
	public void run(IAction action) {
		// TODO Auto-generated method stub
		String id = action.getText();
		
		if(id.compareTo(FILTER_UTILITY)==0) {
			output.openFilterDialog();
		}
		else if(id.compareTo(REPLACE_UTILITY)==0) {
			output.openReplaceDialog();
		}
		else if(id.compareTo(FIND_UTILITY)==0) {
			output.openFindDialog();
		}
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		// TODO Auto-generated method stub

	}

}
