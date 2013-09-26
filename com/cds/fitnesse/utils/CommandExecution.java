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
	public CommandExecution(String cmd, String rtnMsg){
		this.cmdToRun = cmd;
		this.returnMsg = rtnMsg;
	}
	public String getCmdToRun() {
		return cmdToRun;
	}

	public void setCmdToRun(String cmd) {
		this.cmdToRun = cmd;
	}

	public String getReturnMsg() {
		return returnMsg;
	}

	public void setReturnMsg(String returnMsg) {
		this.returnMsg = returnMsg;
	}	
}
