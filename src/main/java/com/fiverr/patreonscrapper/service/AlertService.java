package com.fiverr.patreonscrapper.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;

@Slf4j
@Service
public class AlertService implements IAlertService {

	@Autowired private JavaMailSender javaMailSender;

	/**
	 * @param recipient
	 * @param subject
	 * @param body
	 *
	 * @throws MailException
	 */
	@Async
	@Override
	public void sendEmail(String recipient, String subject, String body) {
		SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
		simpleMailMessage.setTo(recipient);
		simpleMailMessage.setText(body);
		simpleMailMessage.setSubject(subject);

		try {
			javaMailSender.send(simpleMailMessage);
		} catch (MailException e) {
			log.error("A MailException occurred when attempting to send an alert: {}", simpleMailMessage, e);
			throw e;
		}
	}

	/**
	 * @param phoneNumber
	 * @param carrier
	 * @param body
	 *
	 * @throws MailException
	 */
	@Async
	@Override
	public void sendText(String phoneNumber, String carrier, String body) {
		String carrierDomain = "@";
		switch (carrier) {
			case "Verizon":
				carrierDomain += "vzwpix.com";
				break;
			case "ATT":
				carrierDomain += "mms.att.net";
				break;
			case "TMobile":
				carrierDomain += "tmomail.net";
				break;
			case "Google":
				carrierDomain += "msg.fi.google.com";
				break;
		}
		String recipient = phoneNumber + carrierDomain;
		sendEmail(recipient, "", body);
	}

	@Async
	@Override
	public void playAlertSound() throws UnsupportedAudioFileException, IOException, LineUnavailableException {
		boolean audioPlayed = true;
		try {
			AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(getClass().getClassLoader()
					.getResource("alert.wav"));
			Clip clip = AudioSystem.getClip();
			clip.open(audioInputStream);
			clip.loop(1);
			clip.start();
		} catch (IOException | UnsupportedAudioFileException | LineUnavailableException e) {
			log.error("An IOException occurred when attempting to play the alert sound!");
			throw e;
		}
	}
}
