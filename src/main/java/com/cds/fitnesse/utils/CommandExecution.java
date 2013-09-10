package com.cds.fitnesse.utils;

public class CommandExecution {


	private String cmdToRun;
	private String returnMsg;
	
	public CommandExecution(){
		this.cmdToRun = "";
		this.returnMsg = "";
	}
	public CommandExecution(String cmd){
		this.cmdToRun = cmd;
	}
	public String getCmd() {
		return cmdToRun;
	}

	public void setCmd(String cmd) {
		this.cmdToRun = cmd;
	}

	public String getReturnMsg() {
		return returnMsg;
	}

	public void setReturnMsg(String returnMsg) {
		this.returnMsg = returnMsg;
	}	
}
