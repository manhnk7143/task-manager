package com.dev.dbaas.common;

public enum ProcessorType {

	NORMAL(1), BACKUP(2);
	private int value;
	
	private ProcessorType(int mValue){
		value = mValue;
	}
	
	public int getValue(){
		return value;
	}
}
