package com.fiverr.patreonscrapper.config;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Getter
@NoArgsConstructor
@ToString
@Configuration
public class PatreonConfig {
	@Value("${patreon.product.url}")
	private String patreonProductUrl;

	@Value("${patreon.alert.time-between-alerts}")
	private int timeBetweenAlerts;

	@Value("${patreon.alert.to-email}")
	private String toEmail;

	@Value("${patreon.alert.to-phone-number}")
	private String toPhoneNumber;

	@Value("${patreon.alert.phone-carrier}")
	private String phoneCarrier;

	@Value("${patreon.product.monitor-products}")
	private String productsToMonitor;
}
