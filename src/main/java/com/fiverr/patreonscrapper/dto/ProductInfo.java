package com.fiverr.patreonscrapper.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
@NoArgsConstructor
@ToString
@EqualsAndHashCode
public class ProductInfo {
	@JsonProperty("amount_cents")
	private int amountCents;

	private boolean published;

	private String remaining;

	private String title;

	private String url;
}
