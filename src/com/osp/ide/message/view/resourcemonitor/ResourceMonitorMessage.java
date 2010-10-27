package com.osp.ide.message.view.resourcemonitor;

//MODIFIED 2010.02.25 - isEquals() �߰�
public class ResourceMonitorMessage {
	int 			type;
	String			command;
	String			appID;
	String			name;
	
	String			typeString;
	
	public ResourceMonitorMessage(int type, String command, String appID) {
		this.type = type;
		this.command = command;
		this.appID = appID;	
		
		this.name = "";
		this.typeString = "";
	}
	
	public int getType() {
		return this.type;
	}
	
	public String getCommand() {
		return this.command;
	}
	
	public String getAppID() {
		return this.appID;
	}
	
	public String getTypeString() {
		return this.typeString;
	}
	
	public String getName() {
		return this.name.isEmpty() ? "Unnamed" : this.name;
	}
	
	/**
	 * �� ��ü�� ������ ��ü���� �Ǵ��Ѵ�. 
	 * 
	 * <pre>
	 * {@link NetManager#deleteResource}���� Resource�� �Ǵ��ϴ� �κ��� ResourceMonitorMesage�� �̵��Ͽ���.
	 *    
	 * for (int j = 0; j < messages.size(); j++) {
	 *     ResourceMonitorMessage rmm = messages.get(j);
	 *     
	 *     <b>if (nType == rmm.getType() && appID.indexOf(rmm.getAppID()) >= 0)</b> {
	 *         // messages.remove(j);
	 *         bada.removeResourceMessage(j);
	 *         break;
	 *     }
	 *}
	 * </pre> 
	 * 
	 * @param message ��� ResourceMonitorMessage
	 * @return ��� ResourceMonitorMessage�� �������� 
	 */
	public boolean isEuqlas(ResourceMonitorMessage message) {
	    //Type�� ���Ѵ�.
	    if (getType() != message.getType())
	        return false;
	    
	    //Application ID�� ���Ѵ�.
	    if (getAppID().equals(message.getAppID()) == false)
	        return false;
	    
	    return true;
	}
}
