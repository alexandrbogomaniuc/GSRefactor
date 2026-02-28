package com.abs.casino.common.exception;

import com.dgphoenix.casino.common.exception.CommonException;

public class XmlWriterException extends CommonException{
	
	private static final long serialVersionUID = 7007092398221411644L;

	public XmlWriterException(String message){
        super(message);
    }
    
    public XmlWriterException(Throwable tread){
        super(tread);
    }
    
    public XmlWriterException(String message,Throwable tread){
        super(message,tread);
    }

    public XmlWriterException() {
    }
}
