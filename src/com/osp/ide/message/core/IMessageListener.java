package com.osp.ide.message.core;

/**
 * IMessageListener.java
 * <p>
 * Simulator/Target에서 IDE에 전달하는 메세지를 수신한다.
 * </p>
 *
 * @author 전수완
 * @version 1.0
 */
public interface IMessageListener {
    /**
     * 수신받은 메세지가 Listener에서 처리할 내용인지 판단한다.
     * @param message Simulator/Target에서 수신된 메세지 
     * @return Listener에서 처리할 내용이면 true, 아니면 false를 돌려준다.
     * true인 경우 {@link IMessageListener#execute(String)}를 실행한다.
     * @see IMessageListener#execute(String)
     */
    boolean isAccept(String message);
    
    /**
     * 수신받은 메세지를 처리한다.
     * {@link IMessageListener#isAccept(String)}에서 true된 메세지만을 수신한다.
     * @param message Simulator/Target에서 수신된 메세지
     * @see IMessageListener#isAccept(String)
     */
    void execute(String message);
}
