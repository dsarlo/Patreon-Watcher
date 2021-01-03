package com.fiverr.patreonscrapper.tasks;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiverr.patreonscrapper.config.PatreonConfig;
import com.fiverr.patreonscrapper.dto.ProductInfo;
import com.fiverr.patreonscrapper.dto.ProductMonitor;
import com.fiverr.patreonscrapper.service.IAlertService;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class PollingScraper {
	private final List<ProductMonitor> productsToMonitor;
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	private Instant nextAlert = null;

	private static final String PATREON_PRODUCT_REGEX = "\"attributes\": *(\\{[ \r\n]*\"[A-Za-z0-9?/>.<,\"':;+=_\\-)(*&^%$#@!~`\\\\ \r\n]*})";
	private static final Pattern PATREON_PRODUCT_REGEX_PATTERN = Pattern.compile(PATREON_PRODUCT_REGEX);

	private final IAlertService alertService;
	private final PatreonConfig patreonConfig;

	public PollingScraper(IAlertService alertService, PatreonConfig patreonConfig) {
		this.alertService = alertService;
		this.patreonConfig = patreonConfig;

		productsToMonitor = new ArrayList<>();

		String[] patreonProduct = patreonConfig.getProductsToMonitor().split(",");
		for (String productAndPrice : patreonProduct) {
			String[] productValue = productAndPrice.split(":");
			productsToMonitor.add(new ProductMonitor(productValue[0], Integer.parseInt(productValue[1])));
		}
	}

	@Scheduled (fixedDelayString = "${patreon.product.polling-frequency}")
	private void pollPatreonForProductAvailability() {
		String productUrl = patreonConfig.getPatreonProductUrl();
		Document doc = null;

		try {
			doc = Jsoup.connect(productUrl).userAgent("Mozilla").get();
		} catch (IOException e) {
			log.error("An IOException occurred when attempting to reach the url: {}", productUrl, e);
			return;
		}

		Matcher matcher = PATREON_PRODUCT_REGEX_PATTERN.matcher(doc.outerHtml());

		while (matcher.find()) {
			String match = matcher.group(1);
			ProductInfo productInfo = null;

			try {
				productInfo = OBJECT_MAPPER.readValue(match, ProductInfo.class);
			} catch (JsonProcessingException e) {
				log.error(
						"A JsonProcessingException occurred when attempting to map the value {} to an instance of ProductInfo!",
						match,
						e
				);
				return;
			}

			if (productInfo.getTitle() != null) {
				for (ProductMonitor productBeingMonitored : productsToMonitor) {
					if (productBeingMonitored.getTitle().equalsIgnoreCase(productInfo.getTitle()) &&
							productInfo.isPublished() &&
							productInfo.getAmountCents() / 100 == productBeingMonitored.getCost()) {
						if (productInfo.isPublished() && productInfo.getRemaining() == null ||
								!productInfo.getRemaining().equals("0")) {
							log.info(productBeingMonitored.getTitle() + ": $" + productBeingMonitored.getCost() +
									" - Product Available!");

							if (nextAlert == null || nextAlert.isBefore(Instant.now())) {
								String alertMessage =
										productBeingMonitored.getTitle() + " for $" + productBeingMonitored.getCost() +
												" is Available Now!\n\nGet the product now at the following url: " +
												productUrl;

								alertService.sendEmail(
										patreonConfig.getToEmail(),
										productBeingMonitored.getTitle() + " is Available Now!",
										alertMessage

								);
								alertService.sendText(
										patreonConfig.getToPhoneNumber(),
										patreonConfig.getPhoneCarrier(),
										alertMessage
								);
								alertService.playAlertSound();

								nextAlert = Instant.now()
										.plus((long) patreonConfig.getTimeBetweenAlerts(), ChronoUnit.MINUTES);
							} else {
								log.debug("Alert successfully suppressed until: {}", nextAlert);
							}
							break;
						} else {
							log.info(productBeingMonitored.getTitle() + ": $" + productBeingMonitored.getCost() +
									" - Product Unavailable!");
							break;
						}
					}
				}
			}
		}
	}
}
