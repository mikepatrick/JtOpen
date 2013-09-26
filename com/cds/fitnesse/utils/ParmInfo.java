package com.cds.fitnesse.utils;

public class ParmInfo{
	private String dataName;
	private String dataType;
	private int dataLength;
	private String dataValue;
	private int decimalLength;
	private int byteLength;
	private String returnMessage;
	private byte[] storedParm;
	
	public ParmInfo(String dataName, String dataType, String dataLength, String dataValue){
		this.setDataName(dataName);
		this.setDataType(dataType);
		this.setReturnMessage("");
		this.setByteLength(0);
		if ((dataLength.contains(","))){
			String[] splitLength = dataLength.split(",");
			String totalLength = splitLength[0];
			String decimalLength = splitLength[1];
			this.setDataLength(Integer.parseInt(totalLength));
			this.setDecimalLength(Integer.parseInt(decimalLength));
		}else{
			this.setDataLength(Integer.parseInt(dataLength));
			this.setDecimalLength(0);
		}

		this.setDataValue(dataValue);
	}

	public void setDataName(String dataName) {
		this.dataName = dataName;
	}

	public String getDataName() {
		return dataName;
	}

	public void setDataType(String dataType) {
		this.dataType = dataType;
	}

	public String getDataType() {
		return dataType;
	}

	public void setDataLength(int dataLength) {
		this.dataLength = dataLength;
	}

	public int getDataLength() {
		return dataLength;
	}

	public void setDataValue(String dataValue) {
		this.dataValue = dataValue;
	}

	public String getDataValue() {
		return dataValue;
	}

	public void setDecimalLength(int decimalLength) {
		this.decimalLength = decimalLength;
	}

	public int getDecimalLength() {
		return decimalLength;
	}

	public void setByteLength(int byteLength) {
		this.byteLength = byteLength;
	}

	public int getByteLength() {
		return byteLength;
	}

	public void setReturnMessage(String returnMessage) {
		this.returnMessage = returnMessage;
	}

	public String getReturnMessage() {
		return returnMessage;
	}

	public void setStoredParm(byte[] storedParm) {
		this.storedParm = storedParm;
	}

	public byte[] getStoredParm() {
		return storedParm;
	}
	
}

