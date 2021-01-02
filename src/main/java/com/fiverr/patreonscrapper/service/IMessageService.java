package com.fiverr.patreonscrapper.service;

public interface IMessageService {
	boolean sendEmail(String recipient, String body);
	boolean sendText(String phoneNumber, String carrier, String body);
}
