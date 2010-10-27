package com.osp.ide.message;

public class OSPMessage {
	private int 	dest;
	private int 	type;
	private int 	id;
	private int 	choice;
	private String 	date;
	private String 	hour;
	private String 	originalMessage;
	private String 	outputMessage;
	
	public OSPMessage(int dest, int type, int id, int choice, String date, String hour, String ori_message) {
		this.dest = dest;
		this.type = type;
		this.id = id;
		this.choice = choice;
		this.date = date;
		this.hour = hour;
		this.originalMessage = ori_message;
	}
	
	public int getDest() {
		return this.dest;
	}
	
	public int getType() {
		return this.type;
	}
	
	public int getID() {
		return this.id;
	}
	
	public int getChoice() {
		return this.choice;
	}
	
	public String getDate() {
		return this.date;
	}
	
	public String getHour() {
		return this.hour;
	}
	
	public String getOriginalMessage() {
		return this.originalMessage;
	}
	
	public String getOutputMessage() {
		return outputMessage;
	}
	
	public void setOutputMessage(String outputMessage) {
		this.outputMessage = outputMessage;
	}
}