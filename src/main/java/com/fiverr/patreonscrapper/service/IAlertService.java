package com.fiverr.patreonscrapper.service;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;

public interface IAlertService {
	void sendEmail(String recipient, String subject, String body);
	void sendText(String phoneNumber, String carrier, String body);
	void playAlertSound() throws UnsupportedAudioFileException, IOException, LineUnavailableException;
}
