package com.smks.cyclictrading.hitbtctypes;

import java.util.List;

import com.smks.cyclictrading.commontypes.Order;
import com.smks.cyclictrading.commontypes.TradeCycle;

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
		
		double volumeToOrder = getVolumeToOrder(startingVolume, lookForBestBid, tradePair.getQuantityIncrement());
		final String side = lookForBestBid? "sell" : "buy";
		return new Order(tradePair.getId(), side, volumeToOrder);
	}
	
	/*
	 * This method will look at the current volume of the currency prior to trading and look at the best orders
	 * available for the trade and determine how much of the next currency I can obtain.
	 * 
	 * Fee note, the Fee currency will always be the same as the quote currency.  This means that each SELL
	 * order (lookForBestBid = TRUE), will take .1% of the new balance (quote currency)
	 * Each BUY order will remove .1% of the value before the trade
	 */
	public Double getVolumeToOrder(double startingVolume, final boolean lookForBestBid, double quantityInc) {
				
		double volumeLeft = startingVolume;
		
		if(lookForBestBid && bid.size() == 0) return 0.0;
		if(!lookForBestBid && ask.size() == 0) return 0.0;
		if(volumeLeft <= 0) return 0.0;
				
		// So basically we want to look for the best price that we can get for our volume
		if(lookForBestBid) { // Buying base currency
			double currentOutputVolume = 0.0;
			for(final Bid currentBid : this.bid) {
				
				// If it is a bid, then our volume is in the quote currency and we can reduce to check that it
				//	is in the min increment right here.  If there is leftover from volume mod Increment
				//	then subtract it from the volume.
				volumeLeft -= (volumeLeft % quantityInc);
				
				// We will keep decreasing the volume, once it hits 0 return what we have found
				if(volumeLeft <= 0)
					return currentOutputVolume; // Return the volume and remember to remove the fee
				
				// If the bid we are looking at will fill all that we want, simply add the volume that we get from
				//	this trade
				if(currentBid.getSize() >= volumeLeft)
					currentOutputVolume += volumeLeft * currentBid.getPrice();
				else
					//Else get the most volume that we can from the bid we are looking at
					currentOutputVolume += currentBid.getSize() * currentBid.getPrice();
					
				// Decrease the volume that we still need to find
				volumeLeft -= currentBid.getSize();
			}
		} else { // Selling base currency
			double currentOutputVolume = 0.0;
			//volumeLeft -= (volumeLeft * .001); // Remove the fee before we start TODO: Check this
			for(final Ask currentAsk : this.ask) {
				
				if(volumeLeft <= 0)
					return currentOutputVolume;

				// How much of the new currency can we buy with the currency we have?
				// There is a min increment on the new currency so ensure that the volume we ask for is divisible by the increment
				if( ( currentAsk.getSize() * currentAsk.getPrice() ) >= volumeLeft) {
					
					double volumeToAskFor = volumeLeft / currentAsk.getPrice();
					volumeToAskFor -= volumeToAskFor % quantityInc;

					// If we can't even buy the min increment, just return what we have so far.
					if(volumeToAskFor <= 0)
						return currentOutputVolume;
					
					// Else increment how much to ask for
					currentOutputVolume += volumeToAskFor;
				}
				else
					currentOutputVolume += currentAsk.getSize();

				volumeLeft -= currentAsk.getSize() * currentAsk.getPrice();
			}
		}
		return 0.0;
	}
}
