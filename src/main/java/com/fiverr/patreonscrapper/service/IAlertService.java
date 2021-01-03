package com.fiverr.patreonscrapper.service;

public interface IAlertService {
	boolean sendEmail(String recipient, String subject, String body);
	boolean sendText(String phoneNumber, String carrier, String body);
	boolean playAlertSound();
}
