package com.smks.cyclictrading.commontypes;

import java.util.Arrays;
import java.util.Objects;

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
			virtuallyPerformTrade(this.startingVolume);
		} catch (UnirestException e) {
			e.printStackTrace();
		}
		
	}
	
	/*
	 * This function will use the current order books for each of the currency pairs in this cycle
	 * and analyze the gain that could be made from 
	 */
	public void virtuallyPerformTrade(final Double startingVolume) throws UnirestException {

		try {
			// TODO: Create faster lookup that will keep these values for some determined amount of time
			// Note after each call we will return quickly if there are no valid trades
			
			final OrderBook firstTradeOrders = WebServices.getOrderBookForSymbol(firstTrade.getId());
			final OrderBook secondTradeOrders = WebServices.getOrderBookForSymbol(secondTrade.getId());
			final OrderBook thirdTradeOrders = WebServices.getOrderBookForSymbol(thirdTrade.getId());
			
			if(Objects.isNull(firstTradeOrders) || Objects.isNull(secondTradeOrders) || Objects.isNull(thirdTradeOrders))
				return;
			
			final Order firstOrder = firstTradeOrders.getBestOrder(startingVolume, firstTradeIsAsk, firstTrade);
			if(firstOrder.getQuantity() <= 0) return;
			final Order secondOrder = secondTradeOrders.getBestOrder(firstOrder.getQuantity(), secondTradeIsAsk, secondTrade);
			if(firstOrder.getQuantity() <= 0) return;
			final Order thirdOrder = thirdTradeOrders.getBestOrder(secondOrder.getQuantity(), thirdTradeIsAsk, thirdTrade);
			if(firstOrder.getQuantity() <= 0) return;

			// Compute potential percent gain from performing this cycle
			double percentGain = (thirdOrder.getQuantity() - startingVolume) / startingVolume;
			
			// If the gain is better than the threshold given by the trader, give the orders to the trader
			//	to perform the trade (method is sychronized across all threads)
			if(percentGain > Trader.PERCENT_GAIN_THRESHOLD)
				Trader.performTrade(this, Arrays.asList(firstOrder, secondOrder, thirdOrder ), percentGain);
			
		} catch (Exception e) {
		}
	}

	@Override
	public String toString() {
		return "TradeCycle [ " + startingSymbol + " --> " + secondCurrency
				+ " --> " + thirdCurrency + " --> " + startingSymbol + " ]";
	}	
}
