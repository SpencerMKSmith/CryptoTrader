package com.smks.cyclictrading.commontypes;

import lombok.Data;

@Data
@lombok.RequiredArgsConstructor
public class Order {
	
	private final String symbol;
	private final String side; // Buy or Sell
	private final Double quantity;
	//private final Double price;
	private final String type = "market";
	private final String timeInForce = "FOK";
	
	public String getUrlString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("symbol=")
			.append(this.symbol)
			.append("&side=")
			.append(this.side)
			.append("&quantity=")
			.append(this.quantity)
			.append("&type=")
			.append(this.type)
			.append("&timeInForce=")
			.append("FOK");

		return builder.toString();
	}
	
}
