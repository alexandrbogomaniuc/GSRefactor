package com.abs.casino.common.exception;

import com.abs.casino.common.exception.CommonException;



public class ParserException extends CommonException {
	private static final long serialVersionUID = -1183070539875219174L;
    private int position = Integer.MIN_VALUE;

	public ParserException(String message){
        super(message);
    }

    public ParserException(String message, int position){
        super(message);
        this.position = position;
    }

    public ParserException(String message,Throwable thread, int position){
        super(message, thread);
        this.position = position;
    }

    public ParserException(Throwable tread){
        super(tread);
    }
    
    public ParserException(String message,Throwable tread){
        super(message,tread);
    }

    @Override
    public String toString() {
        return super.toString() + (position == Integer.MIN_VALUE ? "" : ", position=" + position);
    }
}
