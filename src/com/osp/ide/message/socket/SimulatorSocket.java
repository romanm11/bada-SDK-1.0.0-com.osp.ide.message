package com.osp.ide.message.socket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.Queue;

import com.osp.ide.message.Constants;
import com.osp.ide.message.core.MessageService;

public class SimulatorSocket implements Runnable {
	private Socket 			socket = null;
	private PrintWriter 	writer = null;
	private BufferedReader 	reader = null;
	
	private boolean 		run = false;
	
	private Queue<String>	sendMessageList;
	
	public SimulatorSocket(Socket socket) {
		this.socket = socket;

		sendMessageList = new LinkedList<String>();
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
			InputStream is = socket.getInputStream();
			OutputStream os = socket.getOutputStream();
//			writer = new PrintWriter(os);
			writer = new PrintWriter(new OutputStreamWriter(os, Charset.defaultCharset()));
			reader = new BufferedReader(new InputStreamReader(is, Charset.defaultCharset()));
			
			String line = null;
			run = true;
			
			NetManager netManager = NetManager.getInstance();
			netManager.setPhoneStatus(Constants.BADA_PHONE_STATUS_OFF);

			while(run) {
				if(!socket.isConnected() || socket.isClosed())
					break;
				
				if(reader != null && reader.ready() && (line = reader.readLine()) != null) {
//					System.out.println(line);
					checkCommand(line);
					netManager.addMessage(line);
					MessageService.instance().write(line);
				}
				
				if(writer != null && sendMessageList.size() > 0) {
					String message = sendMessageList.poll();
					if(message != null) {
//						message += "\n";
						writer.write(message);
						writer.flush();
					}
				}
				Thread.sleep(1);
			}

			os.close();
			is.close();
		}
		catch (IOException e) { e.printStackTrace(); }
		catch (InterruptedException e) { e.printStackTrace(); }
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			run = false;
			
			NetManager netManager = NetManager.getInstance();
			netManager.setPhoneStatus(Constants.BADA_PHONE_STATUS_OFF);
			netManager.stopTimer();
			
			System.out.println("Simulator Disconnected...");
			
			if(sendMessageList != null) {
				sendMessageList.clear();
				sendMessageList = null;
			}
			
			if (writer != null) {
				try { writer.close(); writer = null; } catch (Exception ex) { ex.printStackTrace(); }
			}

			if (reader != null) {
				try { reader.close(); reader = null; } catch (IOException ex) { ex.printStackTrace(); }
			}

			if (socket != null) {
				try { socket.close(); socket = null; } catch (IOException ex) { ex.printStackTrace(); }
			}

			netManager.stopServer();
		}
	}
	
	public void stop() {		
		run = false;
		
		NetManager.getInstance().setPhoneStatus(Constants.BADA_PHONE_STATUS_OFF);
//		System.out.println("Simulator Disconnected(stop)...");
		
		if (writer != null) {
			try { writer.close(); writer = null; } catch (Exception ex) { ex.printStackTrace(); }
		}

		if (reader != null) {
			try { reader.close(); reader = null; } catch (IOException ex) { ex.printStackTrace(); }
		}

		if (socket != null) {
			try { socket.close(); socket = null; } catch (IOException ex) { ex.printStackTrace(); }
		}
		
		if(sendMessageList != null) {
			sendMessageList.clear();
			sendMessageList = null;
		}
	}
	
	public boolean IsRunning() {
		return run;
	}
	
	public synchronized void sendMessage(String message) {
		sendMessageList.add(message);
	}
	
	private void checkCommand(String message) {
		NetManager manager = NetManager.getInstance();
		
//		System.out.println(message);
		
		if(message.indexOf(Constants.BADA_PHONE_STATUS_00) >= 0) {
//          Packet ex.) "1,0,0,1,2010-03-29,17:13:50.078,00001|03:15:39|PHONESTATUS:00> Idle Entered"
//			System.out.println("Received Phone Status 00 Message...");
			manager.setPhoneStatus(Constants.BADA_PHONE_STATUS_00);
		}
		else if(message.indexOf(Constants.BADA_PHONE_STATUS_04) >= 0) {
//          Packet ex.) "1,0,0,1,2010-03-29,17:13:51.546,00002|03:38:70|PHONESTATUS:04> app installed, errType=0"
//			System.out.println("Received Phone Status 04 Message...");
            manager.setPhoneStatus(Constants.BADA_PHONE_STATUS_04);
            String strErrTypeEqual = "errType=";
            int nIndex = message.indexOf(strErrTypeEqual);
            nIndex += strErrTypeEqual.length();
            try {
                String strErrType = message.substring(nIndex);
                int nErrType = Integer.parseInt(strErrType);
                manager.setPhoneStatus04ErrorType(nErrType);
            } catch (IndexOutOfBoundsException e) {
                e.printStackTrace();
            }
        }
		else if(message.indexOf(Constants.BADA_PHONE_SHUTDOWN) >= 0) {
			System.out.println("Received Shutdown Message...");
			run = false;
			manager.stopServer();
		}
	}
}