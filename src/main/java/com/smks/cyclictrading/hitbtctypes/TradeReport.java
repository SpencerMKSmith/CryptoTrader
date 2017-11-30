package com.smks.cyclictrading.hitbtctypes;

import lombok.Data;

@Data
public class TradeReport {
	private final String id;
	private final double quantity;
	private final double price;
	private final double fee;
	private final String timestamp;
}
