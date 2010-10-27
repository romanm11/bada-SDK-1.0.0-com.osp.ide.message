package com.osp.ide.message.view.resourcemonitor;

//MODIFIED 2010.02.25 - isEquals() 추가
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
	 * 두 객체가 동일한 객체인지 판단한다. 
	 * 
	 * <pre>
	 * {@link NetManager#deleteResource}에서 Resource를 판단하던 부분을 ResourceMonitorMesage로 이동하였음.
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
	 * @param message 대상 ResourceMonitorMessage
	 * @return 대상 ResourceMonitorMessage와 동일한지 
	 */
	public boolean isEuqlas(ResourceMonitorMessage message) {
	    //Type을 비교한다.
	    if (getType() != message.getType())
	        return false;
	    
	    //Application ID를 비교한다.
	    if (getAppID().equals(message.getAppID()) == false)
	        return false;
	    
	    return true;
	}
}
