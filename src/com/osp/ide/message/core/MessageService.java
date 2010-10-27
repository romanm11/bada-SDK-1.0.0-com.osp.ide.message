/**
 * 
 */
package com.osp.ide.message.core;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SafeRunner;

/**
 * @author Suwan Jeon
 *
 */
public class MessageService {
    private static final String MESSAGE_LISTENER_ID = "com.osp.ide.message.MessageListener";
    private static MessageService messageService = null;
    
    private MessageService() {
        
    }
    
    public static MessageService instance() {
        return (messageService == null) ? messageService = new MessageService() : messageService; 
    }
    
    public boolean write(String message) {
        //System.out.println(this + "#write - " + message);
    	updateListener(message);
        return true;
    }

    public void echoForTest(String message) {
        System.out.println(this + "#echoForTest - " + message);
        updateListener(message);
    }
    
    //TODO �̰��� ���� ���α׷��̹Ƿ� ���� ȯ�濡 �°� �����ϵ��� �Ѵ�.
    private void updateListener(String message) {
        final String receivedMessage = message;
        
        //�޼����� Listener���� �����Ѵ�.
        IConfigurationElement[] configElements = Platform.getExtensionRegistry().getConfigurationElementsFor(MESSAGE_LISTENER_ID);
        
        for (IConfigurationElement selectedConfigElement: configElements) {
            try {
                final Object object = selectedConfigElement.createExecutableExtension("class");
            
                if (object instanceof IMessageListener == true) {
                    ISafeRunnable runnable = new ISafeRunnable() {
                        @Override
                        public void handleException(Throwable exception) {
                            exception.printStackTrace();
                        }

                        @Override
                        public void run() throws Exception {
                            IMessageListener messageListener = (IMessageListener)object;
                            
                            //���� ó���� �������� Ȯ���Ѵ�.
                            if (messageListener.isAccept(receivedMessage) == true) {
                                //ó���� �����Ѵ�.
                                messageListener.execute(receivedMessage);
                            }
                        }
                    };
                    
                    SafeRunner.run(runnable);
                }
            } catch (CoreException e) {
                e.printStackTrace();
            }
        }
    }
}
