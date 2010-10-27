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
    
    //TODO 이것은 샘플 프로그램이므로 실제 환경에 맞게 변경하도록 한다.
    private void updateListener(String message) {
        final String receivedMessage = message;
        
        //메세지를 Listener에게 전달한다.
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
                            
                            //현재 처리할 내용인지 확인한다.
                            if (messageListener.isAccept(receivedMessage) == true) {
                                //처리를 수행한다.
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
