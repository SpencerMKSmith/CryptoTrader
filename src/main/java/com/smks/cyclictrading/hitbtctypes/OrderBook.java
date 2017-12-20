package com.smks.cyclictrading.hitbtctypes;

import java.util.List;

import com.smks.cyclictrading.commontypes.Order;

import lombok.Data;

@Data
@lombok.Getter
@lombok.Setter
public class OrderBook
{
    private List<Ask> ask;
    private List<Bid> bid;
    
	public OrderBook() {
	}

	public OrderBook(List<Ask> ask, List<Bid> bid) {
		this.ask = ask;
		this.bid = bid;
	}

	public Order getBestOrder(double startingVolume, final boolean lookForBestBid, final CurrencyPair tradePair) {
		
		final String side = lookForBestBid? "sell" : "buy";
		Order order;
		if(lookForBestBid)
			order = getPredictedSellOrder(startingVolume, tradePair);
		else
			order = getPredictedBuyOrder(startingVolume, tradePair);
		
		return order;
	}
	
	public Order getPredictedBuyOrder(double startingVolume, final CurrencyPair tradePair) {
		Order predictedOrder = new Order(tradePair.getId(), "buy", this.ask.get(0).getPrice());
		double quantityInc = tradePair.getQuantityIncrement();
		predictedOrder.setQuantityInc(quantityInc);
		
		double volumeLeftToBuyWith = startingVolume;
		double takeRate = tradePair.getTakeLiquidityRate();
		
		double quantityOfNewCurrency = 0.0;
		
		// For each ask until all of our input currency is used, we will simulate buying currency at the
		//	best price
		double currentAskPrice = this.ask.get(0).getPrice();
		for(final Ask currentAsk : this.ask) {
			double currentAskSize = Double.MAX_VALUE;
			//double currentAskPrice = currentAsk.getPrice();
			double minVolumeNeeded = (quantityInc * currentAskPrice) * (1 + takeRate); // This is the value needed to buy the smallest increment
			
			// If we have used all of our input currency that we can, return the Order predictions
			if(volumeLeftToBuyWith < minVolumeNeeded) {
				quantityOfNewCurrency -= quantityOfNewCurrency % quantityInc;
				predictedOrder.setQuantity(quantityOfNewCurrency);
				predictedOrder.setAfterQuantity(quantityOfNewCurrency);
				predictedOrder.setCurrencyRemainder(volumeLeftToBuyWith);
				return predictedOrder;
			}
			
			double volumeToFulfillAsk = currentAskSize * currentAskPrice; // What would be needed to buy all of current ask?
			double quantityToBuy = 0.0;
			if(volumeToFulfillAsk > volumeLeftToBuyWith) { // Spend the rest of our volume here
				quantityToBuy = volumeLeftToBuyWith / currentAskPrice;	// This will be the most quantity we can buy
				quantityToBuy -= quantityToBuy % quantityInc;			// Remove the training decimals
			} else { // Fulfill the ask 
				quantityToBuy = currentAskSize;
			}
			
			// Now we have the max volume that we can buy with the current ask, simulate the trade
			double baseTradeCostAmount = quantityToBuy * currentAskPrice;
			double feeCost = baseTradeCostAmount * takeRate;
			double totalTradeCostAmount = baseTradeCostAmount + feeCost;
			
			// If we don't have sufficient funds, decrease the buy quantity
			// TODO: Fix this, it has been seen to loop many times until it finds a good amount
			//		 
			while(totalTradeCostAmount > volumeLeftToBuyWith) {
				quantityToBuy -= quantityInc;
				baseTradeCostAmount = quantityToBuy * currentAskPrice;
				feeCost = baseTradeCostAmount * takeRate;
				totalTradeCostAmount = baseTradeCostAmount + feeCost;
			}
			
			if(totalTradeCostAmount > volumeLeftToBuyWith)
				System.out.println("Check this!");

			// Decrease the amount we have left to trade
			volumeLeftToBuyWith -= totalTradeCostAmount;
			
			// Increase the quantity after trading
			quantityOfNewCurrency += quantityToBuy;
		}
		return predictedOrder;
	}
	
	public Order getPredictedSellOrder(double startingVolume, final CurrencyPair tradePair) {
		Order predictedOrder = new Order(tradePair.getId(), "sell", this.bid.get(0).getPrice());
		double quantityInc = tradePair.getQuantityIncrement();
		predictedOrder.setQuantityInc(quantityInc);
		
		double volumeThatCantBeSold = startingVolume % quantityInc;
		double volumeLeftToSell = startingVolume - volumeThatCantBeSold;
		double takeRate = tradePair.getTakeLiquidityRate();
		
		double quantityOfNewCurrency = 0.0;
		double quantitySold = 0.0;
		final double currentBidPrice = this.bid.get(0).getPrice();
		for(final Bid currentBid : this.bid) {
			final double currentBidSize = Double.MAX_VALUE;
			//final double currentBidPrice = currentBid.getPrice();
			// If we have used all of our input currency that we can, return the Order predictions
			if(volumeLeftToSell < quantityInc) {
				predictedOrder.setQuantity(quantitySold);
				predictedOrder.setAfterQuantity(quantityOfNewCurrency);
				predictedOrder.setCurrencyRemainder(volumeLeftToSell + volumeThatCantBeSold);
				return predictedOrder;
			}
			
			double quantityToSellAtThisPrice = Math.min(volumeLeftToSell, currentBidSize);
			
			// Simulate selling this quantity
			double basePaymentAfterSelling = quantityToSellAtThisPrice * currentBidPrice;
			double feeCost = basePaymentAfterSelling * takeRate;
			double totalOutputQuantity = basePaymentAfterSelling - feeCost;
			totalOutputQuantity -= totalOutputQuantity % tradePair.getTickSize();
			// Decrease the amount we have left to trade
			volumeLeftToSell -= quantityToSellAtThisPrice;
			
			// Increase how much we have sold
			quantitySold += quantityToSellAtThisPrice;
			
			// Increase the quantity after trading
			quantityOfNewCurrency += totalOutputQuantity;
			
		}
		return predictedOrder;
	}
	
	/*
	 * This method will look at the current volume of the currency prior to trading and look at the best orders
	 * available for the trade and determine how much of the next currency I can obtain.
	 * 
	 * When buying, the quantity == afterQuantity, when selling, the quantity refers to the amount being sold
	 * and the after amount is the quantity of the quote currency being obtained
	 * 
	 * Fee note, the Fee currency will always be the same as the quote currency.  This means that each SELL
	 * order (lookForBestBid = TRUE), will take .1% of the new balance (quote currency)
	 * Each BUY order will remove .1% of the value before the trade
	 */
//	public Order getPredictedOrder(double startingVolume, final String side, final CurrencyPair tradePair) {
//				
//		double volumeLeft = startingVolume;
//		
//		if("sell".equals(side) && bid.size() == 0) return null;
//		if("buy".equals(side) && ask.size() == 0) return null;
//		if(volumeLeft <= 0) return null;
//				
//		double quantityInc = tradePair.getQuantityIncrement();
//		
//		// So basically we want to look for the best price that we can get for our volume
//		if("sell".equals(side)) { // Buying base currency
//			double currentOutputVolume = 0.0;
//			double amountToSell = 0.0;
//			for(final Bid currentBid : this.bid) {
//				
//				// If it is a bid, then our volume is in the quote currency and we can reduce to check that it
//				//	is in the min increment right here.  If there is leftover from volume mod Increment
//				//	then subtract it from the volume.
//				//volumeLeft -= (volumeLeft % quantityInc);
//				
//				// We will keep decreasing the volume, once it hits 0 return what we have found
//				if(volumeLeft <= 0) {
//					amountToSell -= (amountToSell % quantityInc); // Make sure it doesn't have outlieing values
//					currentOutputVolume -= currentOutputVolume * .001;
//					currentOutputVolume -= currentOutputVolume % quantityInc;
//					return new Order(tradePair.getId(), side, amountToSell, currentOutputVolume); // Return the volume and remember to remove the fee
//				}
//					
//				// If the bid we are looking at will fill all that we want, simply add the volume that we get from
//				//	this trade
//				if(currentBid.getSize() >= volumeLeft) {
//					currentOutputVolume += volumeLeft * currentBid.getPrice();
//					amountToSell += volumeLeft;
//				} else {
//					//Else get the most volume that we can from the bid we are looking at
//					currentOutputVolume += currentBid.getSize() * currentBid.getPrice();
//					amountToSell += currentBid.getSize();
//				}
//				
//				// Decrease the volume that we still need to find
//				volumeLeft -= currentBid.getSize();
//			}
//		} else if("buy".equals(side)) { // Selling base currency
//			double currentOutputVolume = 0.0;
//			volumeLeft -= (volumeLeft * .001); // Remove the fee before we start TODO: Check this
//			for(final Ask currentAsk : this.ask) {
//				
//				if(volumeLeft <= 0)
//					return new Order(tradePair.getId(), side, currentOutputVolume, currentOutputVolume);
//
//				// How much of the new currency can we buy with the currency we have?
//				// There is a min increment on the new currency so ensure that the volume we ask for is divisible by the increment
//				if( ( currentAsk.getSize() * currentAsk.getPrice() ) >= volumeLeft) {
//					
//					double volumeToAskFor = volumeLeft / currentAsk.getPrice();
//					volumeToAskFor -= volumeToAskFor % quantityInc;
//
//					// If we can't even buy the min increment, just return what we have so far.
//					if(volumeToAskFor <= 0)
//						return new Order(tradePair.getId(), side, currentOutputVolume, currentOutputVolume);
//					
//					// Else increment how much to ask for
//					currentOutputVolume += volumeToAskFor;
//				}
//				else
//					currentOutputVolume += currentAsk.getSize();
//
//				volumeLeft -= currentAsk.getSize() * currentAsk.getPrice();
//			}
//		}
//		return null;
//	}
}
