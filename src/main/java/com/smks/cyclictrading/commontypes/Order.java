package com.smks.cyclictrading.commontypes;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;

@Data
@lombok.RequiredArgsConstructor
public class Order {
	
	private final String symbol;
	private final String side; // Buy or Sell
	private final String type = "limit";
	private final String timeInForce = "GTC";
	private final double price;
	private double quantity;
	@JsonIgnore private double afterQuantity;
	
	// Due to rounding and other factors, there may be some amount of the currency that was held previous to the trade left
	//	after the trade is done.
	@JsonIgnore private double currencyRemainder; 
	@JsonIgnore private double quantityInc;
	
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

	public void decreaseQuantity() {
		this.quantity -= this.quantityInc;
	}
	
}
