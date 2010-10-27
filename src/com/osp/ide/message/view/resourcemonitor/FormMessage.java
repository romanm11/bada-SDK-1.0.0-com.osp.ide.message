package com.osp.ide.message.view.resourcemonitor;

import com.osp.ide.message.Constants;

//MODIFIED 2010.02.25 - Suwan Jeon, FormMessage(int type, String command, String appID, String name, String handle) 추가
//MODIFIED 2010.02.25 - Suwan Jeon, handle에 대한 Accessors, isEquals() 추가

public class FormMessage extends ResourceMonitorMessage {
    private String handle = null;

	public FormMessage(int type, String command, String appID, String name) {
		super(type, command, appID);
		// TODO Auto-generated constructor stub
		this.name = name;
		this.typeString = Constants.OSP_RESOURCE_FORM;
	}
	
	/**
	 * 생성자
	 * 
	 * @param type 메세지 유형
	 * @param command   명령어
	 * @param appID 응용프로그램 ID 
	 * @param name 응용프로그램 명칭
	 * @param handle 핸들러
	 */
	public FormMessage(int type, String command, String appID, String name, String handle) {
        this(type, command, appID, name);
        setHandle(handle);
    }

    /**
     * Handle을 돌려준다.
     * 
     * @return Handle
     */
    public String getHandle() {
        return handle;
    }

    /**
     * Handle을 설정한다.
     * 
     * @param handle Handle
     */
    public void setHandle(String handle) {
        this.handle = handle;
    }
    
    /* (non-Javadoc)
     * @see com.osp.ide.message.view.resourcemonitor.ResourceMonitorMessage#isEuqlas(com.osp.ide.message.view.resourcemonitor.ResourceMonitorMessage)
     */
    public boolean isEuqlas(ResourceMonitorMessage message) {
        //FormMessage가 아니면 비교 대상이 아니다.
        if (message instanceof FormMessage == false)
            return false;
        
        FormMessage targetFormMessage = (FormMessage)message;
        
        if (super.isEuqlas(targetFormMessage) == false)
            return false;
        
        //이름을 비교한다.
        if (getName().equals(targetFormMessage.getName()) == false)
            return false;
    
        //핸들을 비교한다.
        if (getHandle().equals(targetFormMessage.getHandle()) == false)
            return false;
        
        return true;
    }
}
