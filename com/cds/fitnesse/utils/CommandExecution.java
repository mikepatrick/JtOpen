package com.cds.fitnesse.utils;

public class CommandExecution {


	public String cmdToRun;
	public String returnMsg;
	
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
