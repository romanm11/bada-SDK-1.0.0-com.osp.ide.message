package com.osp.ide.message.view.resourcemonitor;

import com.osp.ide.message.Constants;

//MODIFIED 2010.02.25 - Suwan Jeon, FormMessage(int type, String command, String appID, String name, String handle) �߰�
//MODIFIED 2010.02.25 - Suwan Jeon, handle�� ���� Accessors, isEquals() �߰�

public class FormMessage extends ResourceMonitorMessage {
    private String handle = null;

	public FormMessage(int type, String command, String appID, String name) {
		super(type, command, appID);
		// TODO Auto-generated constructor stub
		this.name = name;
		this.typeString = Constants.OSP_RESOURCE_FORM;
	}
	
	/**
	 * ������
	 * 
	 * @param type �޼��� ����
	 * @param command   ��ɾ�
	 * @param appID �������α׷� ID 
	 * @param name �������α׷� ��Ī
	 * @param handle �ڵ鷯
	 */
	public FormMessage(int type, String command, String appID, String name, String handle) {
        this(type, command, appID, name);
        setHandle(handle);
    }

    /**
     * Handle�� �����ش�.
     * 
     * @return Handle
     */
    public String getHandle() {
        return handle;
    }

    /**
     * Handle�� �����Ѵ�.
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
        //FormMessage�� �ƴϸ� �� ����� �ƴϴ�.
        if (message instanceof FormMessage == false)
            return false;
        
        FormMessage targetFormMessage = (FormMessage)message;
        
        if (super.isEuqlas(targetFormMessage) == false)
            return false;
        
        //�̸��� ���Ѵ�.
        if (getName().equals(targetFormMessage.getName()) == false)
            return false;
    
        //�ڵ��� ���Ѵ�.
        if (getHandle().equals(targetFormMessage.getHandle()) == false)
            return false;
        
        return true;
    }
}
