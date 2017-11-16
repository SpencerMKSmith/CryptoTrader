package com.smks.cyclictrading.hitbtctypes;

import java.util.List;

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

	public Double getVolumeAfterBestOrder(double volume, final boolean lookForBestBid) {
		
		double startingVolume = volume;
		
		if(lookForBestBid && bid.size() == 0) return 0.0;
		if(!lookForBestBid && ask.size() == 0) return 0.0;
		if(volume <= 0) return 0.0;
		
		// So basically we want to look for the best price that we can get for our volume
		if(lookForBestBid) { // Buying base currency
			double currentOutputVolume = 0.0;
			for(final Bid currentBid : this.bid) {
				
				// We will keep decreasing the volume, once it hits 0 return what we have found
				if(volume <= 0)
					return currentOutputVolume;

				// If the bid we are looking at will fill all that we want, simply add the volume that we get from
				//	this trade
				if(currentBid.getSize() >= volume)
					currentOutputVolume += volume * currentBid.getPrice();
				else
					//Else get the most volume that we can from the bid we are looking at
					currentOutputVolume += currentBid.getSize() * currentBid.getPrice();
					
				// Decrease the volume that we still need to find
				volume -= currentBid.getSize();
				
				if( currentOutputVolume > (startingVolume * this.bid.get(0).getPrice()) )
						System.out.println("Stop");
			}
		} else { // Selling base currency
			double currentOutputVolume = 0.0;
			for(final Ask currentAsk : this.ask) {
				// We will keep decreasing the volume, once it hits 0 return what we have found
				if(volume <= 0)
					return currentOutputVolume;

				if( ( currentAsk.getSize() * currentAsk.getPrice() ) >= volume)
					currentOutputVolume += volume / currentAsk.getPrice();
				else
					currentOutputVolume += currentAsk.getSize();

				volume -= currentAsk.getSize() * currentAsk.getPrice();
				
				if( currentOutputVolume > (startingVolume / this.ask.get(0).getPrice()) )
						System.out.println("Stop");
			}
		}
		return 0.0;
	}
}
