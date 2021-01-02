package com.fiverr.patreonscrapper.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;

public class MessageService implements IMessageService {

	@Autowired
	private JavaMailSender javaMailSender;

	@Override
	public boolean sendEmail(String recipient, String body) {
		return false;
	}

	@Override
	public boolean sendText(String phoneNumber, String carrier, String body) {
		String recipient = phoneNumber + carrier;
		return sendEmail(recipient, body);
	}
}
