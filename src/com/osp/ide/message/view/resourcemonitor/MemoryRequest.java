package com.osp.ide.message.view.resourcemonitor;

import java.util.TimerTask;

import com.osp.ide.message.Constants;
import com.osp.ide.message.socket.NetManager;

/**
 * MemoryRequest
 * <p>
 * 주기적으로 Application의 메모리 정보를 요청한다.
 * </p>
 * 
 * @see java.util.Timer
 */
public class MemoryRequest extends TimerTask {
    /*
     * (non-Javadoc)
     * 
     * @see java.util.TimerTask#run()
     */
    public void run() {
        // TODO Auto-generated method stub
        // System.out.println("Send a heap request...");
        NetManager netManager = NetManager.getInstance();

        if (netManager.getApplicationList().size() > 0 && netManager.getProcessID() >= 0) {
            String request = Constants.BADA_RM_HEAP_REQUEST + Integer.toString(netManager.getProcessID());

            // Application의 메모리 정보를 요청한다.
            netManager.sendMessage(Constants.BADA_RM_HEAP_REQUEST_PRE);
            netManager.sendMessage(request);
        }
    }
}
