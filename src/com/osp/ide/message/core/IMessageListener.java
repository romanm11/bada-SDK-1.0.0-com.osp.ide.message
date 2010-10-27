package com.osp.ide.message.core;

/**
 * IMessageListener.java
 * <p>
 * Simulator/Target���� IDE�� �����ϴ� �޼����� �����Ѵ�.
 * </p>
 *
 * @author ������
 * @version 1.0
 */
public interface IMessageListener {
    /**
     * ���Ź��� �޼����� Listener���� ó���� �������� �Ǵ��Ѵ�.
     * @param message Simulator/Target���� ���ŵ� �޼��� 
     * @return Listener���� ó���� �����̸� true, �ƴϸ� false�� �����ش�.
     * true�� ��� {@link IMessageListener#execute(String)}�� �����Ѵ�.
     * @see IMessageListener#execute(String)
     */
    boolean isAccept(String message);
    
    /**
     * ���Ź��� �޼����� ó���Ѵ�.
     * {@link IMessageListener#isAccept(String)}���� true�� �޼������� �����Ѵ�.
     * @param message Simulator/Target���� ���ŵ� �޼���
     * @see IMessageListener#isAccept(String)
     */
    void execute(String message);
}
