package com.smks.cyclictrading.commontypes;

import com.mashape.unirest.http.exceptions.UnirestException;
import com.smks.cyclictrading.CyclicTrading.Trader;
import com.smks.cyclictrading.CyclicTrading.WebServices;
import com.smks.cyclictrading.hitbtctypes.CurrencyPair;
import com.smks.cyclictrading.hitbtctypes.OrderBook;

public class TradeCycle implements Runnable {

	private final CurrencyPair firstTrade;
	private final CurrencyPair secondTrade;
	private final CurrencyPair thirdTrade;
	private final String startingSymbol;
	
	private Boolean firstTradeIsAsk;
	private String secondCurrency;
	private Boolean secondTradeIsAsk;
	private String thirdCurrency;
	private Boolean thirdTradeIsAsk;
	
	private double startingVolume;
	/*
	 * ASK = Selling base
	 * BID = Buying base
	 */
	public TradeCycle(final String startingSymbol, CurrencyPair firstTrade, CurrencyPair secondTrade, CurrencyPair thirdTrade) {
		this.firstTrade = firstTrade;
		this.secondTrade = secondTrade;
		this.thirdTrade = thirdTrade;
		this.startingSymbol = startingSymbol;

		// Determine some facts about the trade to make computation easier
		this.firstTradeIsAsk = this.firstTrade.getBaseCurrency().equals(this.startingSymbol);
		this.secondCurrency = this.firstTrade.getOppositeCurrency(this.startingSymbol).getCurrencySymbol();
		this.secondTradeIsAsk = this.secondTrade.getBaseCurrency().equals(this.secondCurrency);
		this.thirdCurrency = this.secondTrade.getOppositeCurrency(this.secondCurrency).getCurrencySymbol();
		this.thirdTradeIsAsk = this.thirdTrade.getBaseCurrency().equals(this.thirdCurrency);
	}
	
	public void setStartingVolume(final double startingVolume) {
		this.startingVolume = startingVolume;
	}

	@Override
	public void run() {
		
		// First get the percent gains that we can achieve from this cycle
		try {
			final double percentGain = computePercentGain(this.startingVolume);
			Trader.incrementCount(percentGain);
		} catch (UnirestException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public Double computePercentGain(final Double startingVolume) throws UnirestException {

		try {
			// TODO: Create faster lookup that will keep these values for some determined amount of time
			// Note after each call we will return quickly if there are no valid trades
			
			final OrderBook firstTradeOrders = WebServices.getOrderBookForSymbol(firstTrade.getId());
			final OrderBook secondTradeOrders = WebServices.getOrderBookForSymbol(secondTrade.getId());
			final OrderBook thirdTradeOrders = WebServices.getOrderBookForSymbol(thirdTrade.getId());
			
			double volumeAfterFirstTrade = firstTradeOrders.getVolumeAfterBestOrder(startingVolume, firstTradeIsAsk, firstTrade);
			double volumeAfterSecondTrade = secondTradeOrders.getVolumeAfterBestOrder(volumeAfterFirstTrade, secondTradeIsAsk, secondTrade);
			double volumeAfterThirdTrade = thirdTradeOrders.getVolumeAfterBestOrder(volumeAfterSecondTrade, thirdTradeIsAsk, thirdTrade);
			
			return (volumeAfterThirdTrade - startingVolume) / startingVolume;
		} catch (Exception e) {
			e.printStackTrace();
			return 0.0;
		}
	}

	@Override
	public String toString() {
		return "TradeCycle [ " + startingSymbol + " --> " + secondCurrency
				+ " --> " + thirdCurrency + " --> " + startingSymbol + " ]";
	}	
}
