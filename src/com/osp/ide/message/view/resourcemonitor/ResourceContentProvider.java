package com.osp.ide.message.view.resourcemonitor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;

import com.osp.ide.message.Constants;
import com.osp.ide.message.socket.NetManager;

public class ResourceContentProvider implements ITreeContentProvider {
	List<String> messageRoot = null;
	List<String> messages = null;

	@Override
	public Object[] getChildren(Object parentElement) {
		if(messages == null)
			messages = new ArrayList();
		else
			messages.clear();

		String[] str = parentElement.toString().split(" ", 2);
		if(str.length != 2)	return new Object[0];
		
		if(str[0].trim().equals(Constants.OSP_RESOURCE_TIMER)
			|| str[0].trim().equals(Constants.OSP_RESOURCE_SOCKET))
			return new Object[0];
		
		try {
			TreeViewer tree = NetManager.getInstance().getResourceTreeViewer();
			if(tree.getInput() == null)	return new Object[0];
			List<ResourceMonitorMessage> rm = (List<ResourceMonitorMessage>) tree.getInput();
			for(Iterator iter = rm.iterator(); iter.hasNext();) {
				try {
					ResourceMonitorMessage message = (ResourceMonitorMessage) iter.next();
					if(message != null && str[0].equals(Constants.OSP_RESOURCE_MEMORY) 
							&& message.getType() == Constants.OSP_ID_MEMORY) {
						MemoryMessage mem = (MemoryMessage)message;
						String s = new StringBuilder().append("Peak Used Size : ")
									.append(Long.toString(mem.getPeakSize())).toString();
						messages.add( s );
					}
					else { 
						if(message != null && message.getTypeString().indexOf(str[0]) >= 0) {
							messages.add(message.getName());
						}
					}
				}
				catch (Exception e) {
					System.out.println(e.getMessage());
					break;
				}
			}
		}
		catch(Exception exp) {
			System.out.println(exp.getMessage());
		}
		return messages.size() > 0 ? messages.toArray() : new Object[0];
	}

	@Override
	public Object getParent(Object element) {
		if(element instanceof List) {
			
		}
		return new Object[0];
	}

	@Override
	public boolean hasChildren(Object element) {
		// TODO Auto-generated method stub
		String[] str = element.toString().split(" ", 2);
		if(str.length != 2)	return false;
		
		if(str[0].trim().equals(Constants.OSP_RESOURCE_TIMER)
		   || str[0].trim().equals(Constants.OSP_RESOURCE_SOCKET))
			return false;
		
		try {
			TreeViewer tree = NetManager.getInstance().getResourceTreeViewer();
			if(tree.getInput() == null)	return false;
			List<ResourceMonitorMessage> rm = (List<ResourceMonitorMessage>) tree.getInput();
			for(Iterator iter = rm.iterator(); iter.hasNext();) {
				try {
					ResourceMonitorMessage message = (ResourceMonitorMessage) iter.next();
					if(message.getTypeString().equals(str[0].trim()))
						return true;
				}
				catch(Exception e) {
					System.out.println(e.getMessage());
					break;
				}
			}
		}
		catch(Exception exp) {
			System.out.println(exp.getMessage());
		}

		return false;
	}

	@Override
	public Object[] getElements(Object inputElement) {
		// TODO Auto-generated method stub
		if(inputElement instanceof List) {
			if(messageRoot == null)
				messageRoot = new ArrayList();
			else
				messageRoot.clear();
			
			try {
				List<ResourceMonitorMessage> rm = (List<ResourceMonitorMessage>) inputElement;
				int nCount = 0;
				long heapSize = 0;
				for(int i=0; i<Constants.BADA_RM_MESSAGES_TYPE.length; ++i) {
					nCount = 0;
					for(Iterator iter = rm.iterator(); iter.hasNext();) {
						try {
							ResourceMonitorMessage message = (ResourceMonitorMessage) iter.next();
							
							if(message.getType() == Constants.OSP_ID_MEMORY) {
								heapSize = ((MemoryMessage)message).getHeapSize();
							}
							else {
								if(message.getTypeString().equals(Constants.BADA_RM_MESSAGES_TYPE[i])) {
									nCount++;
								}
							}
						}
						catch(Exception e) {
							System.out.println(e.getMessage());
							break;
						}
					}
					
					if (Constants.BADA_RM_MESSAGES_TYPE[i].equals(Constants.OSP_RESOURCE_MEMORY)) {
						String s = new StringBuilder().append(Constants.BADA_RM_MESSAGES_TYPE[i])
									.append(" : ")
									.append(Long.toString(heapSize)).toString();
						messageRoot.add( s );
					} 
					else if (nCount > 0) {
						String s = new StringBuilder().append(Constants.BADA_RM_MESSAGES_TYPE[i])
									.append(" (")
									.append(Long.toString(nCount))
									.append(")").toString();
						messageRoot.add( s );
					}
				}
			}
			catch(Exception e) {
				System.out.println(e.getMessage());
			}
			finally {
				return messageRoot.toArray();
			}
		}
		else {
			return new Object[0];
		}
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub
//		messages.clear();
//		messageRoot.clear();
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		// TODO Auto-generated method stub
	}
}
