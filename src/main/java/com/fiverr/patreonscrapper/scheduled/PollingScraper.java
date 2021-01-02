package com.fiverr.patreonscrapper.scheduled;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiverr.patreonscrapper.dto.ProductInfo;
import com.fiverr.patreonscrapper.dto.ProductMonitor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class PollingScraper {
	private List<ProductMonitor> productsToMonitor = new ArrayList<>();
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	private static final String PATREON_PRODUCT_REGEX = "\"attributes\": *(\\{[ \r\n]*\"[A-Za-z0-9?/>.<,\"':;+=_\\-)(*&^%$#@!~`\\\\ \r\n]*})";
	private static final Pattern PATREON_PRODUCT_REGEX_PATTERN = Pattern.compile(PATREON_PRODUCT_REGEX);

	public PollingScraper() {
		productsToMonitor.add(new ProductMonitor("Merchant", 35));
		productsToMonitor.add(new ProductMonitor("Adventurer - first 100", 9));
		productsToMonitor.add(new ProductMonitor("Adventurer", 10));
	}

	@Scheduled (fixedDelay = 6000)
	private void pollPatreonForProductAvailability() {
		String url = "https://www.patreon.com/ArtisanGuild";
		Document doc = null;

		try {
			doc = Jsoup.connect(url).userAgent("Mozilla").get();
		} catch (IOException e) {
			log.error("An IOException occurred when attempting to reach the url: " + url);
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
						match
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
