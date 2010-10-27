package com.osp.ide.message.view.output;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;

public class OutputDragSource implements DragSourceListener {
	private Output		outputView;
	
	public OutputDragSource(Output output, TableViewer viewer) {
		this.outputView = output;
		DragSource source = new DragSource(viewer.getControl(), DND.DROP_COPY);
		source.setTransfer(
					new Transfer[] { TextTransfer.getInstance() });
		source.addDragListener(this);
	}

	@Override
	public void dragFinished(DragSourceEvent event) {
		// TODO Auto-generated method stub

	}

	@Override
	public void dragSetData(DragSourceEvent event) {
		// TODO Auto-generated method stub
		if(TextTransfer.getInstance().isSupportedType(event.dataType)) {
			event.data = outputView.getSelectedOutput();
		}
	}

	@Override
	public void dragStart(DragSourceEvent event) {
		// TODO Auto-generated method stub
		event.doit = outputView.getSelectedOutput().length() > 0;
	}

}
