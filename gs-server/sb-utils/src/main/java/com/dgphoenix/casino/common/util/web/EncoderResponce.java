package com.abs.casino.common.util.web;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import com.dgphoenix.casino.common.exception.CommonException;

public class EncoderResponce {

	private final int base64lenght;
	private final int desLength;
	private final String encString;

	public EncoderResponce(int base64lenght, int desLength, String encString) {
		this.base64lenght = base64lenght;
		this.desLength = desLength;
		this.encString = encString;
	}

	public int getBase64lenght() {
		return base64lenght;
	}

	public int getDesLength() {
		return desLength;
	}

	public String getEncString() {
		return encString;
	}
	
	public String getEncStringURLFormatted() throws CommonException{
		try {
			return URLEncoder.encode(encString, "UTF8");
		} catch (UnsupportedEncodingException e) {
			throw new CommonException(e);
		}
	}

	/**
	 * Constructs a <code>String</code> with all attributes
	 * in name = value format.
	 *
	 * @return a <code>String</code> representation 
	 * of this object.
	 */
	public String toString()
	{
	    final String TAB = "    ";
	    
	    String retValue = "";
	    
	    retValue = "EncoderResponce ( "
	        + super.toString() + TAB
	        + "base64lenght = " + this.base64lenght + TAB
	        + "desLength = " + this.desLength + TAB
	        + "encString = " + this.encString + TAB
	        + " )";
	
	    return retValue;
	}
}
