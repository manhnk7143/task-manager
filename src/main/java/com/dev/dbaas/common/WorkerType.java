package com.dev.dbaas.common;

public enum WorkerType {
	
	NONE(0), APP_WORKER(1), SCHEDULE_WORKER(2), AUTH_APP_WORKER(3), CALLBACK_WORKER(4), CONTROL_WORKER(5), BASIC_ROUTE_WORKER(6), LOG_OUTBOUND_WORKER(7), LOG_POSTMAN_WORKER(8);
	
	private WorkerType(int mValue){
	}

}
