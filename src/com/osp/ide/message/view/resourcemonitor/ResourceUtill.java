package com.osp.ide.message.view.resourcemonitor;

import org.eclipse.jface.resource.*;

public class ResourceUtill {
	private static ImageRegistry image_registry = null;

	public static ImageRegistry getImageRegistry()
	{
		if (image_registry == null){
			image_registry = new ImageRegistry();
			image_registry.put("folder", ImageDescriptor.createFromFile(ResourceTableLabelProvider.class,"../../../../../../icons/folder.gif"));
			image_registry.put("file", ImageDescriptor.createFromFile(ResourceTableLabelProvider.class,"../../../../../../icons/file.gif"));
			image_registry.put("app", ImageDescriptor.createFromFile(ResourceTableLabelProvider.class,"../../../../../../icons/View.png"));
		}
		return image_registry;
	}
}