package com.vikas.projs.ml.autonomousvehicle;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Generci Utilities
 * @author Vikas_Vijayakumar
 *
 */
public class Utilities {
	
	/**
	 * Used to validate if a value is a Integer, additionally if its between a certain inclusive range
	 * @param integerValue
	 * @param limitFrom
	 * @param limitTo
	 * @return
	 */
	public static Boolean validateInteger(String integerValue, int limitFrom, int limitTo){
		try{
			int value = Integer.valueOf(integerValue);
			if((value >= limitFrom) && (value <= limitTo)){
				return true;
			}else{
				return false;
			}
		} catch(NumberFormatException e){
			return false;
		}
	}
	
	/**
	 * Used to validate an IP Address
	 * @param ipAddress
	 * @return
	 */
	public static Boolean validateIPv4Address(String ipAddress){
		final String ipV4PATTERN = "^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";
	    Pattern pattern = Pattern.compile(ipV4PATTERN);
	    Matcher matcher = pattern.matcher(ipAddress);
	    return matcher.matches();
	}

}
