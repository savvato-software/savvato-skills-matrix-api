package com.savvato.skillsmatrix.services;

public interface SMSTextMessageService {

	public boolean sendSMS(String toPhoneNumber, String msg);
	
}
