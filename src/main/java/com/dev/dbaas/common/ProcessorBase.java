package com.dev.dbaas.common;

public interface ProcessorBase<T> {

	public boolean process(T job)  throws Exception;
}
