package com.osp.ide.message.socket;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import com.osp.ide.IdePlugin;

public class SimulatorServer implements Runnable {
	private ServerSocket			serverSocket = null;
	private SimulatorSocket 		simulator = null;
	private boolean					waiting = false;
	
	public SimulatorServer() {
		if(serverSocket != null && !serverSocket.isClosed()) {
			try {
				serverSocket.close();
				serverSocket = null;
			} catch (IOException e) { e.printStackTrace(); }
		}
		
		if(simulator != null && simulator.IsRunning()) {
			simulator.stop();
		}
	}

	@Override
	public void run() {
		try {
			int port = IdePlugin.getDefault().getSimulatorConnectPort();
			serverSocket = new ServerSocket(port);
			waiting = true;
			{
				System.out.println("Waiting connection request...");
				Socket socket = serverSocket.accept();
				simulator = new SimulatorSocket(socket);
				new Thread(simulator).start();
				Thread.sleep(1);
				System.out.println("Connected...");
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			waiting = false;
		}
	}

	public void stop() {
		try {
			if(simulator != null) {
				simulator.stop();
			}
			
			if(serverSocket != null && !serverSocket.isClosed()) {
				serverSocket.close();
				waiting = false;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public boolean IsWaiting() {
		return waiting;
	}
	
	public SimulatorSocket getSimulatorSocket() {
		return simulator;
	}
}
