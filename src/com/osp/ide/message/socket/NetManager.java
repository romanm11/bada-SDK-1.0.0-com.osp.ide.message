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
 * 시뮬레이터/타겟과 IDE의 인터페이스를 관리한다.
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
     * <tt>OutputView</tt>의 메세지를 관리한다.
     * 
     * @see com.osp.ide.message.view.output.Output
     */
    private List<OSPMessage> ospMessageList;

    /**
     * <tt>ResourceMonitor</tt>의 응용프로그램의 리스트를 관리한다.
     * 
     * @see com.osp.ide.message.view.resourcemonitor.ResourceMonitor
     */
    private List<BadaApplication> badaApplicationList;
    
    private Process broker = null; 


    /**
     * 생성자
     */
    private NetManager() {
        super();

        ospMessageList = new ArrayList<OSPMessage>(MAX_MESSAGE_SIZE);
        badaApplicationList = Collections.synchronizedList(new ArrayList<BadaApplication>());
    }

    /**
     * NetManager의 인스턴스를 돌려준다.
     * 
     * @return NetManager 인스턴스
     */
    public static NetManager getInstance() {
        return netManager;
    }

    /**
     * Message서비스를 시작한다.
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
     * 메세지 서비스를 종료한다.
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
     * 시뮬레이터/타겟에 메세지를 전달한다.
     * 
     * @param message
     *            전문 메세지
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
     * 시뮬레이터/타겟으로부터 받은 메세지를 IDE에 출력한다.
     * 
     * @param message
     *            전문 메세지
     */
    public synchronized void addMessage(String message) {
        parseMessage(message);

        // 화면을 갱신한다.
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
                                // ResourceMonitor의 TreeView에 값을 동적으로
                                // 구성(setInput()) 한다.
                                // 기존 N개의 Application에 대해서 처리하도록 작성되어 있던 부분이 그대로
                                // 남아 있는 상태
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
     * 전문 메세지를 파싱하여 사용자 메세지로 전환한다.
     * <p>
     * 시뮬레이터/타겟으로 받은 데이터를 분석하여 해당 데이터가 Output의 항목인지 ResourceMonitor의 항목인지를 판단후
     * 해당 View에 값을 할당한다.
     * </p>
     * 
     * @param message
     *            전문 메세지
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
     * 시뮬레이터/타겟의 응용프로그램을 추가한다.
     * 
     * @param nType
     *            리소스 유형
     *            <ul>
     *            <li>Constants.OSP_ID_APPLICATION - 새로운 응용프로그램.</li>
     *            <li>나머지 - 응용프로그램 메세지</li>
     *            </ul>
     * @param messages
     *            메세지
     *            <ul>
     *            <li>0 - 명령어</li>
     *            <li>1 - 응용프로그램 ID</li>
     *            <li>2 - 응용프로그램 명칭</li>
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
     * 시뮬레이터/타겟의 응용프로그램을 제거한다..
     * 
     * @param nType
     *            리소스 유형
     *            <ul>
     *            <li>Constants.OSP_ID_APPLICATION - 새로운 응용프로그램</li>
     *            <li>나머지 - 응용프로그램 메세지(해당 메세지를 제거한다)</li>
     *            </ul>
     * @param appID
     *            응용프로그램 ID
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
     * 시뮬레이터/타겟의 응용프로그램을 제거한다..
     * 
     * @param nType
     *            리소스 유형
     *            <ul>
     *            <li>Constants.OSP_ID_APPLICATION - 새로운 응용프로그램.</li>
     *            <li>나머지 - 응용프로그램 메세지</li>
     *            </ul>
     * @param messages
     *            메세지
     *            <ul>
     *            <li>0 - 명령어</li>
     *            <li>1 - 응용프로그램 ID</li>
     *            <li>2 - 응용프로그램 명칭</li>
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
     * ResourceMonitor의 메세지를 생성한다.
     * 
     * @param nType
     *            메세지 유형
     *            <ul>
     *            <li>Constants.OSP_ID_MEMORY - 메모리</li>
     *            <li>Constants.OSP_ID_THREAD - 쓰레드</li>
     *            <li>Constants.OSP_ID_FILE - 파일</li>
     *            <li>Constants.OSP_ID_DATABASE - 데이터베이스</li>
     *            <li>Constants.OSP_ID_REGISTERY - 레지스트리</li>
     *            <li>Constants.OSP_ID_TIMER - 타이머</li>
     *            <li>Constants.OSP_ID_FORM - 폼</li>
     *            <li>Constants.OSP_ID_SOCKET - 소켓</li>
     *            </ul>
     * 
     * @param messages
     *            메세지
     *            <ul>
     *            <li>0 - 명령어</li>
     *            <li>1 - 응용프로그램 ID</li>
     *            <li>2 - 응용프로그램 명칭</li>
     *            <li>3 - 스택사이즈 (참조:
     *            {@link com.osp.ide.message.view.resourcemonitor.ThreadMessage}
     *            )</li>
     *            </ul>
     * @return ResourceMonitor의 메세지
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
            /* MODIFIED 2010.02.25 - Suwan Jeon, 객체 생성 변경
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
     * 작업을 정리한다.
     */
    public void dispose() {
        stopTimer();
        ospMessageList.clear();
        badaApplicationList.clear();
    }

    /**
     * 주기적으로 시뮬레이터/타겟에 리소스 정보를 요청하는 타이머를 시작한다.
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
     * 주기적으로 시뮬레이터/타겟에 리소스 정보를 요청하는 타이머를 종료한다.
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
     * 타겟에 바이너리를 전송한다.
     * 
     * @param broker_message
     *            타겟파일전송 전문
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
     *  Output Viewer의 출력 내용을 초기화한다.
     */
    public void clearOspMessageList() {
        ospMessageList.clear();
    }
    
    // Accessors....................................................................................................
    /**
     * Output Viewer를 설정한다.
     * 
     * @param viewer Output Viewer
     */
    public void setOutputViewer(TableViewer viewer) {
        this.outputViewer = viewer;
    }

    /**
     * Output Viewer를 돌려준다.
     * 
     * @return Output Viewer
     */
    public TableViewer getOutputViewer() {
        return outputViewer;
    }

    /**
     * Resource Monitor의 TreeViewer를 설정한다.
     * 
     * @param viewer Resource Monitor의 TreeViewer (응용프로그램의 자원정보를 출력한다.)
     */
    public void setResourceTreeViewer(TreeViewer viewer) {
        resourceTreeViewer = viewer;
    }

    /**
     * Resource Monitor의 TreeViewer를 돌려준다.
     * 
     * @return ResourceMonitor의 TreeViewer
     */
    public TreeViewer getResourceTreeViewer() {
        return resourceTreeViewer;
    }

    /**
     * Resource Monitor의 TableViewer를 설정한다.
     * 
     * @param viewer  ResourceMonitor의 TableViewer(응용프로그램을 출력한다.)
     */
    public void setResourceViewer(TableViewer viewer) {
        resourceTableViewer = viewer;
    }

    /**
     * Resource Monitor의 TableViewer를 돌려준다.
     * 
     * @return ResourceMonitor의 TableViewer
     */
    public TableViewer getResourceViewer() {
        return resourceTableViewer;
    }

    /**
     * Output Viewer의 스크롤 잠금여부를 설정한다.
     * 
     * @param lock 스크롤 잠금 여부, true - 락설정, false - 락해제
     */
    public void setScrollLock(boolean lock) {
        scrollLock = lock;
    }

    /**
     * Output Viewer의 스크롤 잠금여부를 돌려준다.
     *   
     * @return 스크롤 잠금 여부
     */
    public boolean isScollLock() {
        return scrollLock;
    }

    /**
     * Output Viewer의 메세지 항목을 돌려준다.
     * 
     * @return Output Viewer 메세지 항목 
     */
    public List<OSPMessage> getMessageList() {
        return ospMessageList;
    }

    /**
     * Resource Monitor의 응용프로그램 항목을 돌려준다.
     * 
     * @return Resource Monitor의 응용프로그램 항목
     */
    public List<BadaApplication> getApplicationList() {
        return badaApplicationList;
    }

    /**
     * 시뮬레이터 통신 소켓을 돌려준다.
     * 
     * @return 시뮬레이터 소켓
     */
    public SimulatorSocket getSimulatorSocket() {
        if (simulatorServer != null)
            return simulatorServer.getSimulatorSocket();
        return null;
    }

    /**
     * 폰 상태를 설정한다.
     * 
     * @param phoneStatus 폰 상태
     */
    public void setPhoneStatus(String phoneStatus) {
        PhoneStatus = phoneStatus;
    }

    /**
     * 폰 상태를 돌려준다.
     * 
     * @return 폰 상태
     */
    public String getPhoneStatus() {
        return PhoneStatus;
    }
    
    /**
     * Simulator로 부터 받은 PHONESTATUS:04 메시지의 errType 값을 설정한다.
     * 
     * @param errType  PHONESTATUS:04 메시지의 errType 값
     */
    public void setPhoneStatus04ErrorType(int errType)
    {
        phoneStatus04ErrorType = errType;
    }

    /**
     * Simulator로 부터 받은 PHONESTATUS:04 메시지의 errType 값을 돌려준다.
     * 
     * @return PHONESTATUS:04 메시지를 받았다면 errType 값을 돌려준다.
     *         그렇지 않으면 -1 값을 돌려준다.
     */
    public int getPhoneStatus04ErrorType()
    {
        return phoneStatus04ErrorType;
    }

    /**
     * 시뮬레이터/타겟의 리소스 정보를 요청하는 타이머를 돌려준다.
     * 
     * @return 리소스 요청 타이머
     */
    public Timer getMemoryRequestTimer() {
        return this.timer;
    }

    /**
     * 필터 활성화를 설정한다.
     * 
     * @param filter_info 필터, true - 활성화 false - 비활성화
     */
    public void setFilter_info(boolean filter_info) {
        this.filter_info = filter_info;
    }

    /**
     * 필터 활성화 여부를 돌려준다.
     * 
     * @return 필터 활성화 여부
     */
    public boolean isFilter_info() {
        return filter_info;
    }

    /**
     * 필터 디버그 여부를 설정한다.
     * 
     * @param filter_debug 필터디버그, true - 활성화 false - 비활성화
     */
    public void setFilter_debug(boolean filter_debug) {
        this.filter_debug = filter_debug;
    }

    /**
     * 필터 디버그 여부를 돌려준다. 
     * 
     * @return 필터디버그
     */
    public boolean isFilter_debug() {
        return filter_debug;
    }

    /**
     * 필터 예외 여부를 설정한다.
     * 
     * @param filter_exception 필터예외여부, true - 활성화 false - 비활성화
     */
    public void setFilter_exception(boolean filter_exception) {
        this.filter_exception = filter_exception;
    }

    /**
     * 필터 예외 여부를 돌려준다.
     * 
     * @return 필터예외여부
     */
    public boolean isFilter_exception() {
        return filter_exception;
    }

    /**
     * 프로세서ID를 설정한다.
     * 
     * @param processID 프로세서 ID
     */
    public void setProcessID(int processID) {
        this.processID = processID;
    }

    /**
     * 프로세서ID를 돌려준다.
     * 
     * @return 프로세서ID
     */
    public int getProcessID() {
        return processID;
    }

    /**
     * 타겟 디버깅 활성화 여부를 설정한다.
     * 
     * @param remoteDebuggingEnabled 타겟 디버깅 활성화 여부, true - 활성화 false - 비활성화
     */
    public void setRemoteDebuggingEnabled(boolean remoteDebuggingEnabled) {
        this.remoteDebuggingEnabled = remoteDebuggingEnabled;
    }

    /**
     * 타겟 디버깅 활성화 여부를 돌려준다.
     * 
     * @return 타겟 디버깅 활성화 여부
     */
    public boolean isRemoteDebuggingEnabled() {
        return remoteDebuggingEnabled;
    }

    /**
     * 타겟  COM포트를 설정한다.
     * 
     * @param comPort COM포트
     */
    public void setComPort(int comPort) {
        this.comPort = comPort;
    }

    /**
     * 타겟 COM포트를 돌려준다.
     * 
     * @return COM포트
     */
    public int getComPort() {
        return comPort;
    }

    /**
     * Broker를 돌려준다.
     * 
     * @return broker
     */
    public Process getBroker() {
        return broker;
    }

    /**
     * Broker를 설정한다.
     * 
     * @param broker broker
     */
    public void setBroker(Process broker) {
        this.broker = broker;
    }
    
    /**
     * Simulator에게 종료하라는 메시지를 보낸다.
     */
    public void stopSimulator()
    {
    	sendMessage(Constants.BADA_EXIT_SIMULATOR);
    }
}
