package com.osp.ide.message.view.output;

import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.ITableFontProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import com.osp.ide.message.OSPMessage;

public class OutputLabelProvider implements ITableLabelProvider, IBaseLabelProvider, ITableColorProvider, ITableFontProvider {
	private static FontData fd = null;
	private static Font boldFont = null;
	@Override
	public Image getColumnImage(Object element, int columnIndex) {
		return null;
	}

	@Override
	public String getColumnText(Object element, int columnIndex) {
		OSPMessage osp = (OSPMessage) element;
		return osp.getOutputMessage();
//		switch(columnIndex) {
//		case 0:
//			return (osp.getDate() + " " + osp.getHour());
//		case 1:
//			return osp.getTag();
//		case 2:
//			return osp.getOutputMessage();
//		}
//		return "";
	}

	@Override
	public void addListener(ILabelProviderListener listener) {

	}

	@Override
	public void dispose() {

	}

	@Override
	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	@Override
	public void removeListener(ILabelProviderListener listener) {

	}

	@Override
	public Color getBackground(Object element, int columnIndex) {
		return null;
	}

	@Override
	public Color getForeground(Object element, int columnIndex) {
		OSPMessage ospMsg = (OSPMessage) element;
		String strMsg = ospMsg.getOutputMessage();
		if(Output.isErrorCode(strMsg))
			return Display.getDefault().getSystemColor(SWT.COLOR_BLUE);
		return null;
	}

	@Override
	public Font getFont(Object element, int columnIndex) {
//		OSPMessage ospMsg = (OSPMessage) element;
//		String strMsg = ospMsg.getOutputMessage();
//		if(Output.isErrorCode(strMsg)){
//			if(fd == null){
//				fd = Display.getDefault().getSystemFont().getFontData()[0];
//				fd.setStyle(SWT.BOLD);
//			}
//			if(boldFont == null)
//				boldFont = new Font(Display.getDefault(), fd);
//			return boldFont;
//		}
			
		return null;
	}

}
