package com.osp.ide.message.view.resourcemonitor;

import java.util.List;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

public class ResourceTableContentProvider implements IStructuredContentProvider{

	@Override
	public Object[] getElements(Object inputElement) {
		// TODO Auto-generated method stub
		List<BadaApplication> bada = (List<BadaApplication>)inputElement;
		return bada == null ? new Object[0] : bada.toArray();
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		// TODO Auto-generated method stub
		if(viewer != null)
			viewer.refresh();
	}
}