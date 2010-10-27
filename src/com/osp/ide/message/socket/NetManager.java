package com.osp.ide.message.socket;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Tree;
import com.osp.ide.message.Constants;
import com.osp.ide.message.OSPMessage;
import com.osp.ide.message.view.output.UploadToTargetProgress;
import com.osp.ide.message.view.resourcemonitor.DatabaseMessage;
import com.osp.ide.message.view.resourcemonitor.FileMessage;
import com.osp.ide.message.view.resourcemonitor.FormMessage;
import com.osp.ide.message.view.resourcemonitor.BadaApplication;
import com.osp.ide.message.view.resourcemonitor.MemoryMessage;
import com.osp.ide.message.view.resourcemonitor.MemoryRequest;
import com.osp.ide.message.view.resourcemonitor.RegistryMessage;
import com.osp.ide.message.view.resourcemonitor.ResourceMonitorMessage;
import com.osp.ide.message.view.resourcemonitor.SocketMessage;
import com.osp.ide.message.view.resourcemonitor.ThreadMessage;
import com.osp.ide.message.view.resourcemonitor.TimerMessage;

/**
 * NetManager
 * <p>
 * �ùķ�����/Ÿ�ٰ� IDE�� �������̽��� �����Ѵ�.
 * </p>
 * 
 */
// MODIFIED 2010.02.25 - Suwan Jeon, deleteResource(int nType, String appID) -> deleteResource(int nType, String[] messages)
public class NetManager {
    public static final int MAX_MESSAGE_SIZE = 20000;
    public static final int DELETE_MESSAGE_INDEX = MAX_MESSAGE_SIZE / 4;

    private static NetManager netManager = new NetManager();
    
    private static int nOldOutputViewItemCount = 0;

    private TableViewer outputViewer = null;
    private TreeViewer resourceTreeViewer = null;
    private TableViewer resourceTableViewer = null;

    private boolean scrollLock = false;

    private SimulatorServer simulatorServer = null;

    private String PhoneStatus = Constants.BADA_PHONE_STATUS_OFF;
    
    private int phoneStatus04ErrorType = -1;

    private Timer timer;

    private boolean filter_info = true;
    private boolean filter_debug = true;
    private boolean filter_exception = true;

    private static boolean bRefreshed = false;

    private int processID = -1;

    private boolean remoteDebuggingEnabled = false;
    private int comPort = -1;
    private UploadToTargetProgress progress = null;

    /**
     * <tt>OutputView</tt>�� �޼����� �����Ѵ�.
     * 
     * @see com.osp.ide.message.view.output.Output
     */
    private List<OSPMessage> ospMessageList;

    /**
     * <tt>ResourceMonitor</tt>�� �������α׷��� ����Ʈ�� �����Ѵ�.
     * 
     * @see com.osp.ide.message.view.resourcemonitor.ResourceMonitor
     */
    private List<BadaApplication> badaApplicationList;
    
    private Process broker = null; 


    /**
     * ������
     */
    private NetManager() {
        super();

        ospMessageList = new ArrayList<OSPMessage>(MAX_MESSAGE_SIZE);
        badaApplicationList = Collections.synchronizedList(new ArrayList<BadaApplication>());
    }

    /**
     * NetManager�� �ν��Ͻ��� �����ش�.
     * 
     * @return NetManager �ν��Ͻ�
     */
    public static NetManager getInstance() {
        return netManager;
    }

    /**
     * Message���񽺸� �����Ѵ�.
     */
    public void startServer() {
    	nOldOutputViewItemCount = 0;
    	
        ospMessageList.clear();
        badaApplicationList.clear();

        // Clear Output view
        addMessage("");
        bRefreshed = false;

        if (simulatorServer == null) {
            new Thread(simulatorServer = new SimulatorServer()).start();
            System.out.println("Start Server...");
        } else {
            stopServer();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            new Thread(simulatorServer = new SimulatorServer()).start();
            System.out.println("Restart Server...");
        }
    }

    /**
     * �޼��� ���񽺸� �����Ѵ�.
     */
    public void stopServer() {
        stopTimer();

        if (simulatorServer != null) {
            simulatorServer.stop();
            simulatorServer = null;
        }

        System.out.println("Stop Server...");
    }

    /**
     * �ùķ�����/Ÿ�ٿ� �޼����� �����Ѵ�.
     * 
     * @param message
     *            ���� �޼���
     */
    public synchronized void sendMessage(String message) {
        if (simulatorServer != null) {
            SimulatorSocket simulatorSocket = simulatorServer.getSimulatorSocket();
            if (simulatorSocket != null) {
                if (simulatorSocket.IsRunning())
                    simulatorSocket.sendMessage(message);
            }
        }
    }

    /**
     * �ùķ�����/Ÿ�����κ��� ���� �޼����� IDE�� ����Ѵ�.
     * 
     * @param message
     *            ���� �޼���
     */
    public synchronized void addMessage(String message) {
        parseMessage(message);

        // ȭ���� �����Ѵ�.
        Display display = Display.getDefault();

        if (display != null) {
            display.asyncExec(new Runnable() {
                @Override
                public void run() {
                    if (outputViewer != null && outputViewer.getInput() != null) {
                        Table table = outputViewer.getTable();
                        if (!table.isDisposed()) {
                            outputViewer.refresh(false);
                            if (!scrollLock) {
                            	int nOutputViewItemCount = table.getItemCount();
                                if (nOutputViewItemCount > 0 && nOutputViewItemCount != nOldOutputViewItemCount)
                                    table.showItem(table.getItem(table.getItemCount() - 1));
                                nOldOutputViewItemCount = nOutputViewItemCount;
                            }
                        }
                    }
                    if (resourceTableViewer != null && resourceTableViewer.getInput() != null) {
                        Table table = resourceTableViewer.getTable();
                        if (!table.isDisposed()) {
                            resourceTableViewer.refresh();

                            if (!bRefreshed && table.getItemCount() > 0) {
                                // ResourceMonitor�� TreeView�� ���� ��������
                                // ����(setInput()) �Ѵ�.
                                // ���� N���� Application�� ���ؼ� ó���ϵ��� �ۼ��Ǿ� �ִ� �κ��� �״��
                                // ���� �ִ� ����
                                table.setSelection(0);
                                BadaApplication bada = getApplicationList().get(0);
                                getResourceTreeViewer().setInput(bada.getResourceMonitorMessageList());
                                getResourceTreeViewer().expandAll();

                                bRefreshed = true;
                            }
                        }
                    }

                    if (resourceTreeViewer != null && resourceTreeViewer.getInput() != null) {
                        Tree tree = resourceTreeViewer.getTree();
                        if (!tree.isDisposed()) {
                            resourceTreeViewer.refresh();
                            resourceTreeViewer.expandAll();
                        }
                    }
                }
            });
        }
    }

    /**
     * ���� �޼����� �Ľ��Ͽ� ����� �޼����� ��ȯ�Ѵ�.
     * <p>
     * �ùķ�����/Ÿ������ ���� �����͸� �м��Ͽ� �ش� �����Ͱ� Output�� �׸����� ResourceMonitor�� �׸������� �Ǵ���
     * �ش� View�� ���� �Ҵ��Ѵ�.
     * </p>
     * 
     * @param message
     *            ���� �޼���
     */
    private void parseMessage(String message) {
        int dest = -1;
        int type = -1;
        int id = -1;
        int choice = -1;
        String date = null;
        String hour = null;

        String outputMessage;

        String messages[] = message.split(",", 7);

        if (messages.length == 7) {
            try {
                dest = Integer.parseInt(messages[0].toString().trim());
                type = Integer.parseInt(messages[1].toString().trim());
                id = Integer.parseInt(messages[2].toString().trim());
                choice = Integer.parseInt(messages[3].toString().trim());
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println(message);
                return;
            }

            date = messages[4].toString().trim();
            hour = messages[5].toString().trim();

            outputMessage = messages[6].trim();

            // System.out.println(outputMessage);

            if (dest == Constants.OSP_MSG_IN_HOST_DEBUG || dest == Constants.OSP_MSG_IN_TARGET_DEBUG) {
                OSPMessage osp = new OSPMessage(dest, type, id, choice, date, hour, outputMessage);
                osp.setOutputMessage(outputMessage);
                ospMessageList.add(osp);
            } else if (dest == Constants.OSP_MSG_IN_HOST_TRACE || dest == Constants.OSP_MSG_IN_TARGET_TRACE) {
                int bracketL_Pos = outputMessage.indexOf("[");
                int bracketR_Pos = outputMessage.indexOf("]");
                if (bracketL_Pos != -1 && bracketR_Pos != -1) {
                    String text = null;
                    try {
                        text = outputMessage.substring(bracketL_Pos + 1, bracketR_Pos);
                    } catch (IndexOutOfBoundsException e) {
                        System.out.println(outputMessage + ":" + e.getMessage());
                        return;
                    }

                    String[] rmIDs = text.split(":", 2);
                    String[] rmMessages = outputMessage.split(",");
                    if (rmIDs.length != 2)
                        return;

                    int rmType = -4845;
                    int rmCommand = -4845;
                    try {
                        rmType = Integer.parseInt(rmIDs[0].trim());
                        rmCommand = Integer.parseInt(rmIDs[1].trim());
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                        return;
                    }

                    if (rmType == -4845 && rmCommand == -4845)
                        return;

                    int nType = rmCommand - rmType;
                    switch (nType) {
                    case 0: // Create
                        addResource(rmType, rmMessages);
                        break;
                    case 1: // Delete
                        /* MODIFIED 2010.02.25
                        deleteResource(rmType, rmMessages[1].trim());
                        */
                        deleteResource(rmType, rmMessages);
                        break;
                    }
                } else if (outputMessage.indexOf(Constants.BADA_RM_FILTER_HEAP) != -1) {
                    String[] rmMems = outputMessage.split(">", 2);
                    if (rmMems.length != 2)
                        return;
                    rmMems[1] = rmMems[1].trim();
                    String[] rmMemSizes = rmMems[1].split(",", 3);
                    if (rmMemSizes.length != 3)
                        return;

                    try {
                        if (!badaApplicationList.isEmpty()) {
                            BadaApplication bada = badaApplicationList.get(0);
                            List<ResourceMonitorMessage> rm = bada.getResourceMonitorMessageList();
                            for (int i = 0; i < rm.size(); i++) {
                                ResourceMonitorMessage rmm = rm.get(i);
                                if (rmm.getType() == Constants.OSP_ID_MEMORY) {
                                    bada.removeResourceMessage(i);
                                    break;
                                }
                            }

                            rmMemSizes[0] = bada.getAppId();
                            ResourceMonitorMessage memMessage = getConvertMessage(Constants.OSP_ID_MEMORY, rmMemSizes);
                            bada.addResourceMessage(memMessage);
                        }
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                    }
                } else if (outputMessage.indexOf(Constants.OSP_RM_FILTER_PROCESS) != -1) {
                    String[] rmPro = outputMessage.split(">", 2);
                    if (rmPro.length != 2)
                        return;
                    rmPro[1] = rmPro[1].trim();
                    String[] rmProcess = rmPro[1].split(",", 2);
                    if (rmProcess.length != 2)
                        return;

                    String[] apps = rmProcess[1].split("/");
                    if (apps.length == 1)
                        return;
                    String appName = apps[apps.length - 1];

                    setProcessID(Integer.parseInt(rmProcess[0].toString().trim()));
                }
            } else if (dest == Constants.BADA_MSG_IN_BROKER) {
                communicationWithBroker(outputMessage);
            }
        }
    }

    /**
     * �ùķ�����/Ÿ���� �������α׷��� �߰��Ѵ�.
     * 
     * @param nType
     *            ���ҽ� ����
     *            <ul>
     *            <li>Constants.OSP_ID_APPLICATION - ���ο� �������α׷�.</li>
     *            <li>������ - �������α׷� �޼���</li>
     *            </ul>
     * @param messages
     *            �޼���
     *            <ul>
     *            <li>0 - ��ɾ�</li>
     *            <li>1 - �������α׷� ID</li>
     *            <li>2 - �������α׷� ��Ī</li>
     *            </ul>
     */
    private synchronized void addResource(int nType, String[] messages) {
        // System.out.println("Insert RM Message : " + Integer.toString(nType) +
        // " " + messages[1]);
        try {
            if (nType == Constants.OSP_ID_APPLICATION) {
                for (Iterator iter = badaApplicationList.iterator(); iter.hasNext();) {
                    BadaApplication bada = (BadaApplication) iter.next();
                    if (messages[1].indexOf(bada.getAppId()) >= 0 && messages[2].indexOf(bada.getAppName()) >= 0)
                        return;
                }
                
                BadaApplication app = new BadaApplication(
                    messages[0]/* Command */, 
                    messages[1]/* App ID */, 
                    messages[2]/* App Name*/
                );
                
                badaApplicationList.add(app);

                startTimer();
                return;
            } else {
                String appID = messages[1];
                for (Iterator iter = badaApplicationList.iterator(); iter.hasNext();) {
                    BadaApplication bada = (BadaApplication) iter.next();
                    if (appID.indexOf(bada.getAppId()) >= 0) {
                        ResourceMonitorMessage message = getConvertMessage(nType, messages);
                        if (message != null) {
                            bada.addResourceMessage(message);
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    /*
     * �ùķ�����/Ÿ���� �������α׷��� �����Ѵ�..
     * 
     * @param nType
     *            ���ҽ� ����
     *            <ul>
     *            <li>Constants.OSP_ID_APPLICATION - ���ο� �������α׷�</li>
     *            <li>������ - �������α׷� �޼���(�ش� �޼����� �����Ѵ�)</li>
     *            </ul>
     * @param appID
     *            �������α׷� ID
     *
    private synchronized void deleteResource(int nType, String appID) {
        // System.out.println("Delete Resource Monitor Message : " +
        // Integer.toString(nType) + " " + appID);
        try {
            if (nType == Constants.OSP_ID_APPLICATION) {
                for (Iterator iter = badaApplicationList.iterator(); iter.hasNext();) {
                    BadaApplication app = (BadaApplication) iter.next();
                    if (appID.indexOf(app.getAppId()) >= 0) {
                        iter.remove();
                        if (badaApplicationList.isEmpty()) {
                            stopTimer();
                        }
                        break;
                    }
                }
            } else {
                for (int i = 0; i < badaApplicationList.size(); i++) {
                    BadaApplication bada = badaApplicationList.get(i);
                    if (appID.indexOf(bada.getAppId()) >= 0) {
                        List<ResourceMonitorMessage> messages = bada.getResourceMonitorMessageList();
                        for (int j = 0; j < messages.size(); j++) {
                            ResourceMonitorMessage rmm = messages.get(j);
                            if (nType == rmm.getType() && appID.indexOf(rmm.getAppID()) >= 0) {
                                // messages.remove(j);
                                bada.removeResourceMessage(j);
                                break;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }*/
    
    /**
     * �ùķ�����/Ÿ���� �������α׷��� �����Ѵ�..
     * 
     * @param nType
     *            ���ҽ� ����
     *            <ul>
     *            <li>Constants.OSP_ID_APPLICATION - ���ο� �������α׷�.</li>
     *            <li>������ - �������α׷� �޼���</li>
     *            </ul>
     * @param messages
     *            �޼���
     *            <ul>
     *            <li>0 - ��ɾ�</li>
     *            <li>1 - �������α׷� ID</li>
     *            <li>2 - �������α׷� ��Ī</li>
     *            </ul>
     */
    private synchronized void deleteResource(int nType, String[] messages) {
        String appID = messages[1].trim();
        
        // System.out.println("Delete Resource Monitor Message : " +
        // Integer.toString(nType) + " " + appID);
        try {
            if (nType == Constants.OSP_ID_APPLICATION) {
                for (Iterator iter = badaApplicationList.iterator(); iter.hasNext();) {
                    BadaApplication app = (BadaApplication) iter.next();
                    if (appID.indexOf(app.getAppId()) >= 0) {
                        iter.remove();
                        if (badaApplicationList.isEmpty()) {
                            stopTimer();
                        }
                        break;
                    }
                }
            } else {
                System.out.println("request the elimination: " + nType + "/" + messages[0] + "/" + messages[1]);
                ResourceMonitorMessage destinationMessage = getConvertMessage(nType, messages);
                
                for (int i = 0; i < badaApplicationList.size(); i++) {
                    BadaApplication bada = badaApplicationList.get(i);
                    
                    if (appID.indexOf(bada.getAppId()) >= 0) {
                        List<ResourceMonitorMessage> message = bada.getResourceMonitorMessageList();
                        
                        for (int j = 0; j < message.size(); j++) {
                            ResourceMonitorMessage rmm = message.get(j);
                            
                            if (rmm.isEuqlas(destinationMessage) == true) {
                                bada.removeResourceMessage(j);
                                System.out.println("complete the elimination: " + rmm.getClass().getName());
                                break;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * ResourceMonitor�� �޼����� �����Ѵ�.
     * 
     * @param nType
     *            �޼��� ����
     *            <ul>
     *            <li>Constants.OSP_ID_MEMORY - �޸�</li>
     *            <li>Constants.OSP_ID_THREAD - ������</li>
     *            <li>Constants.OSP_ID_FILE - ����</li>
     *            <li>Constants.OSP_ID_DATABASE - �����ͺ��̽�</li>
     *            <li>Constants.OSP_ID_REGISTERY - ������Ʈ��</li>
     *            <li>Constants.OSP_ID_TIMER - Ÿ�̸�</li>
     *            <li>Constants.OSP_ID_FORM - ��</li>
     *            <li>Constants.OSP_ID_SOCKET - ����</li>
     *            </ul>
     * 
     * @param messages
     *            �޼���
     *            <ul>
     *            <li>0 - ��ɾ�</li>
     *            <li>1 - �������α׷� ID</li>
     *            <li>2 - �������α׷� ��Ī</li>
     *            <li>3 - ���û����� (����:
     *            {@link com.osp.ide.message.view.resourcemonitor.ThreadMessage}
     *            )</li>
     *            </ul>
     * @return ResourceMonitor�� �޼���
     */
    private ResourceMonitorMessage getConvertMessage(int nType, String[] messages) {
        ResourceMonitorMessage message = null;

        switch (nType) {
        case Constants.OSP_ID_MEMORY:
            try {
                message = new MemoryMessage(nType, messages[0], messages[0], messages[0], Long.parseLong(messages[1].trim()), Long.parseLong(messages[2].trim()));
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
            break;
        case Constants.OSP_ID_THREAD:
            if (messages.length == 2)
                message = new ThreadMessage(nType, messages[0], messages[1], "", "0");
            else if (messages.length == 3)
                message = new ThreadMessage(nType, messages[0], messages[1], messages[2], "0");
            else
                message = new ThreadMessage(nType, messages[0], messages[1], messages[2], messages[3]);
            break;
        case Constants.OSP_ID_FILE:
            if (messages.length == 2)
                message = new FileMessage(nType, messages[0], messages[1], "");
            else
                message = new FileMessage(nType, messages[0], messages[1], messages[2]);
            break;
        case Constants.OSP_ID_DATABASE:
            if (messages.length == 2)
                message = new DatabaseMessage(nType, messages[0], messages[1], "");
            else
                message = new DatabaseMessage(nType, messages[0], messages[1], messages[2]);
            break;
        case Constants.OSP_ID_REGISTERY:
            if (messages.length == 2)
                message = new RegistryMessage(nType, messages[0], messages[1], "");
            else
                message = new RegistryMessage(nType, messages[0], messages[1], messages[2]);
            break;
        case Constants.OSP_ID_TIMER:
            message = new TimerMessage(nType, messages[0], messages[1], "");
            break;
        case Constants.OSP_ID_FORM:
            /* MODIFIED 2010.02.25 - Suwan Jeon, ��ü ���� ����
            if (messages.length == 2)
                message = new FormMessage(nType, messages[0], messages[1], "");
            else
                message = new FormMessage(nType, messages[0], messages[1], messages[2]);
            */
            
            message = new FormMessage(
                nType,
                messages[0],
                messages[1],
                (messages.length > 2 ? messages[2] : ""),
                (messages.length > 3 ? messages[3] : "")
            );
                
            break;
        case Constants.OSP_ID_SOCKET:
            message = new SocketMessage(nType, messages[0], messages[1], "");
            break;
        }

        return message;
    }

    /**
     * �۾��� �����Ѵ�.
     */
    public void dispose() {
        stopTimer();
        ospMessageList.clear();
        badaApplicationList.clear();
    }

    /**
     * �ֱ������� �ùķ�����/Ÿ�ٿ� ���ҽ� ������ ��û�ϴ� Ÿ�̸Ӹ� �����Ѵ�.
     * 
     * @see com.osp.ide.message.view.resourcemonitor.ResourceMonitor
     */
    public void startTimer() {
        if (this.timer != null)
            stopTimer();
        MemoryRequest memoryRequest = new MemoryRequest();
        this.timer = new Timer();
        this.timer.scheduleAtFixedRate(memoryRequest, new Date(), Constants.BADA_MEMORY_REQUEST_DELAY);
        System.out.println("Start timer");
    }

    /**
     * �ֱ������� �ùķ�����/Ÿ�ٿ� ���ҽ� ������ ��û�ϴ� Ÿ�̸Ӹ� �����Ѵ�.
     */
    public void stopTimer() {
        if (this.timer != null) {
            try {
                this.timer.cancel();
                this.timer.purge();
            } catch (Exception e) {
                //e.printStackTrace();
            } finally {
                this.timer = null;
            }

            System.out.println("Stop timer");
        }
    }

    /**
     * Ÿ�ٿ� ���̳ʸ��� �����Ѵ�.
     * 
     * @param broker_message
     *            Ÿ���������� ����
     */
    private void communicationWithBroker(String broker_message) {
        String[] broker = broker_message.split("\\|", 3);
        // if(broker.length != 3) return;
        int broker_type = Integer.parseInt(broker[0].trim());
        int trans_type = Integer.parseInt(broker[1].trim());
        if (broker_type == Constants.BADA_MSG_IN_BROKER_TRANSFER) {
            switch (trans_type) {
            case Constants.BADA_MSG_TRANS_START_APPLICATION:
                final String[] application = broker[2].trim().split("\\|", 2);
                if (application.length != 2)
                    return;

                final ProgressMonitorDialog dialog = new ProgressMonitorDialog(null);
                progress = new UploadToTargetProgress();

                progress.setApplicationInfo(Integer.parseInt(application[0].trim()), Integer.parseInt(application[1].trim()));

                Display display = Display.getDefault();
                if (display != null) {
                    display.asyncExec(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                dialog.run(true, true, progress);
                            } catch (InvocationTargetException e) {
                                e.printStackTrace();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
                break;
            case Constants.BADA_MSG_TRANS_START_FILE:
                try {
                    String[] start_file = broker[2].trim().split("\\|", 4);
                    if (start_file.length != 4)
                        return;
                    if (progress == null || progress.getApplicationInfo() == null)
                        return;

                    progress.startFileTransfer(Integer.parseInt(start_file[0]), Long.parseLong(start_file[1]), start_file[2], start_file[3]);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case Constants.BADA_MSG_TRANS_FILE:
                progress.setCurrentTransferedSize(Long.parseLong(broker[2].trim()));
                // progress.getApplicationInfo().setCurrentTranferedSize(Long.parseLong(broker[2].trim()));
                break;
            case Constants.BADA_MSG_TRANS_STOP_FILE:
                progress.stopTransferFile();
                // progress.getApplicationInfo().stopFileTransfer();
                break;
            case Constants.BADA_MSG_TRANS_STOP_APPLICATION:
                progress.stopTransferApplication();
                break;
            }
        } else if (broker_type == Constants.BADA_MSG_IN_BROKER_DEBUGGING) {
            try {
                int comPort = Integer.parseInt(broker[2].trim());
                setComPort(comPort);
                setRemoteDebuggingEnabled(true);
            } catch (Exception e) {
                setComPort(-1);
                setRemoteDebuggingEnabled(false);
                e.printStackTrace();
            }
        } else if (broker_type == Constants.BADA_MSG_IN_BROKER_INFORMATION) {
            switch (trans_type) {
            case Constants.BADA_MSG_BROKER_ERROR:
                if (progress != null)
                    progress.stopTransferFileByError(broker[2].trim());
                break;
            }
        }
    }
    
    /**
     *  Output Viewer�� ��� ������ �ʱ�ȭ�Ѵ�.
     */
    public void clearOspMessageList() {
        ospMessageList.clear();
    }
    
    // Accessors....................................................................................................
    /**
     * Output Viewer�� �����Ѵ�.
     * 
     * @param viewer Output Viewer
     */
    public void setOutputViewer(TableViewer viewer) {
        this.outputViewer = viewer;
    }

    /**
     * Output Viewer�� �����ش�.
     * 
     * @return Output Viewer
     */
    public TableViewer getOutputViewer() {
        return outputViewer;
    }

    /**
     * Resource Monitor�� TreeViewer�� �����Ѵ�.
     * 
     * @param viewer Resource Monitor�� TreeViewer (�������α׷��� �ڿ������� ����Ѵ�.)
     */
    public void setResourceTreeViewer(TreeViewer viewer) {
        resourceTreeViewer = viewer;
    }

    /**
     * Resource Monitor�� TreeViewer�� �����ش�.
     * 
     * @return ResourceMonitor�� TreeViewer
     */
    public TreeViewer getResourceTreeViewer() {
        return resourceTreeViewer;
    }

    /**
     * Resource Monitor�� TableViewer�� �����Ѵ�.
     * 
     * @param viewer  ResourceMonitor�� TableViewer(�������α׷��� ����Ѵ�.)
     */
    public void setResourceViewer(TableViewer viewer) {
        resourceTableViewer = viewer;
    }

    /**
     * Resource Monitor�� TableViewer�� �����ش�.
     * 
     * @return ResourceMonitor�� TableViewer
     */
    public TableViewer getResourceViewer() {
        return resourceTableViewer;
    }

    /**
     * Output Viewer�� ��ũ�� ��ݿ��θ� �����Ѵ�.
     * 
     * @param lock ��ũ�� ��� ����, true - ������, false - ������
     */
    public void setScrollLock(boolean lock) {
        scrollLock = lock;
    }

    /**
     * Output Viewer�� ��ũ�� ��ݿ��θ� �����ش�.
     *   
     * @return ��ũ�� ��� ����
     */
    public boolean isScollLock() {
        return scrollLock;
    }

    /**
     * Output Viewer�� �޼��� �׸��� �����ش�.
     * 
     * @return Output Viewer �޼��� �׸� 
     */
    public List<OSPMessage> getMessageList() {
        return ospMessageList;
    }

    /**
     * Resource Monitor�� �������α׷� �׸��� �����ش�.
     * 
     * @return Resource Monitor�� �������α׷� �׸�
     */
    public List<BadaApplication> getApplicationList() {
        return badaApplicationList;
    }

    /**
     * �ùķ����� ��� ������ �����ش�.
     * 
     * @return �ùķ����� ����
     */
    public SimulatorSocket getSimulatorSocket() {
        if (simulatorServer != null)
            return simulatorServer.getSimulatorSocket();
        return null;
    }

    /**
     * �� ���¸� �����Ѵ�.
     * 
     * @param phoneStatus �� ����
     */
    public void setPhoneStatus(String phoneStatus) {
        PhoneStatus = phoneStatus;
    }

    /**
     * �� ���¸� �����ش�.
     * 
     * @return �� ����
     */
    public String getPhoneStatus() {
        return PhoneStatus;
    }
    
    /**
     * Simulator�� ���� ���� PHONESTATUS:04 �޽����� errType ���� �����Ѵ�.
     * 
     * @param errType  PHONESTATUS:04 �޽����� errType ��
     */
    public void setPhoneStatus04ErrorType(int errType)
    {
        phoneStatus04ErrorType = errType;
    }

    /**
     * Simulator�� ���� ���� PHONESTATUS:04 �޽����� errType ���� �����ش�.
     * 
     * @return PHONESTATUS:04 �޽����� �޾Ҵٸ� errType ���� �����ش�.
     *         �׷��� ������ -1 ���� �����ش�.
     */
    public int getPhoneStatus04ErrorType()
    {
        return phoneStatus04ErrorType;
    }

    /**
     * �ùķ�����/Ÿ���� ���ҽ� ������ ��û�ϴ� Ÿ�̸Ӹ� �����ش�.
     * 
     * @return ���ҽ� ��û Ÿ�̸�
     */
    public Timer getMemoryRequestTimer() {
        return this.timer;
    }

    /**
     * ���� Ȱ��ȭ�� �����Ѵ�.
     * 
     * @param filter_info ����, true - Ȱ��ȭ false - ��Ȱ��ȭ
     */
    public void setFilter_info(boolean filter_info) {
        this.filter_info = filter_info;
    }

    /**
     * ���� Ȱ��ȭ ���θ� �����ش�.
     * 
     * @return ���� Ȱ��ȭ ����
     */
    public boolean isFilter_info() {
        return filter_info;
    }

    /**
     * ���� ����� ���θ� �����Ѵ�.
     * 
     * @param filter_debug ���͵����, true - Ȱ��ȭ false - ��Ȱ��ȭ
     */
    public void setFilter_debug(boolean filter_debug) {
        this.filter_debug = filter_debug;
    }

    /**
     * ���� ����� ���θ� �����ش�. 
     * 
     * @return ���͵����
     */
    public boolean isFilter_debug() {
        return filter_debug;
    }

    /**
     * ���� ���� ���θ� �����Ѵ�.
     * 
     * @param filter_exception ���Ϳ��ܿ���, true - Ȱ��ȭ false - ��Ȱ��ȭ
     */
    public void setFilter_exception(boolean filter_exception) {
        this.filter_exception = filter_exception;
    }

    /**
     * ���� ���� ���θ� �����ش�.
     * 
     * @return ���Ϳ��ܿ���
     */
    public boolean isFilter_exception() {
        return filter_exception;
    }

    /**
     * ���μ���ID�� �����Ѵ�.
     * 
     * @param processID ���μ��� ID
     */
    public void setProcessID(int processID) {
        this.processID = processID;
    }

    /**
     * ���μ���ID�� �����ش�.
     * 
     * @return ���μ���ID
     */
    public int getProcessID() {
        return processID;
    }

    /**
     * Ÿ�� ����� Ȱ��ȭ ���θ� �����Ѵ�.
     * 
     * @param remoteDebuggingEnabled Ÿ�� ����� Ȱ��ȭ ����, true - Ȱ��ȭ false - ��Ȱ��ȭ
     */
    public void setRemoteDebuggingEnabled(boolean remoteDebuggingEnabled) {
        this.remoteDebuggingEnabled = remoteDebuggingEnabled;
    }

    /**
     * Ÿ�� ����� Ȱ��ȭ ���θ� �����ش�.
     * 
     * @return Ÿ�� ����� Ȱ��ȭ ����
     */
    public boolean isRemoteDebuggingEnabled() {
        return remoteDebuggingEnabled;
    }

    /**
     * Ÿ��  COM��Ʈ�� �����Ѵ�.
     * 
     * @param comPort COM��Ʈ
     */
    public void setComPort(int comPort) {
        this.comPort = comPort;
    }

    /**
     * Ÿ�� COM��Ʈ�� �����ش�.
     * 
     * @return COM��Ʈ
     */
    public int getComPort() {
        return comPort;
    }

    /**
     * Broker�� �����ش�.
     * 
     * @return broker
     */
    public Process getBroker() {
        return broker;
    }

    /**
     * Broker�� �����Ѵ�.
     * 
     * @param broker broker
     */
    public void setBroker(Process broker) {
        this.broker = broker;
    }
    
    /**
     * Simulator���� �����϶�� �޽����� ������.
     */
    public void stopSimulator()
    {
    	sendMessage(Constants.BADA_EXIT_SIMULATOR);
    }
}
