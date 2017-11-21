package com.smks.cyclictrading.CyclicTrading;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.mashape.unirest.http.exceptions.UnirestException;
import com.smks.cyclictrading.commontypes.Currency;
import com.smks.cyclictrading.commontypes.Order;
import com.smks.cyclictrading.commontypes.TradeCycle;
import com.smks.cyclictrading.hitbtctypes.CurrencyPair;

import lombok.Data;

@Data
@lombok.RequiredArgsConstructor
public class Trader {

	private static boolean makeTrade = true;
	private static double bestGain = -2.0;
	public static long startingMillis = System.currentTimeMillis();
	public static int count = 0;
	
	public static synchronized void incrementCount() {
		count++;
		if(count % 100 == 0)
			System.out.println("Average over: " + count + ", " + ((System.currentTimeMillis() - startingMillis) / count));
	}
	public static synchronized void performTrade(final TradeCycle tradeCycle, final List<Order> orders, double percentGain) {
		

		if(percentGain < bestGain) return;
		
		System.out.println(percentGain + ", " + tradeCycle.toString());
		bestGain = percentGain;
		
		if(!makeTrade)
			return;
		makeTrade = false;
		
		for(final Order orderToMake : orders) {
			try {
				final String response = WebServices.postOrder(orderToMake);
			} catch (UnirestException e) {
				e.printStackTrace();
				break;
			}
		}
		
	}

	
	public static final Double PERCENT_GAIN_THRESHOLD = -0.005; // .4%
	
	private final Map<String, Currency> currencyMap;
	
	public void startTrading(final String startingSymbol, final Double startingAmount) throws UnirestException, InterruptedException {
		
		final Currency startingCurrency = currencyMap.get(startingSymbol);
		
		// First, find valid cyclic edges that can be traded from my starting currency
		final List<TradeCycle> validGraphCycles = determineValidGraphCycles(startingCurrency);
		
		// Determine which trade will give me profit

		System.out.println("Starting trades");
		while(true) {
			ExecutorService pool = Executors.newFixedThreadPool(50);
			for(final TradeCycle tradeCycle : validGraphCycles) {
				tradeCycle.setStartingVolume(startingAmount);
				pool.execute(tradeCycle);
			}
			pool.shutdown();
			pool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
		}
	}

	private List<TradeCycle> determineValidGraphCycles(final Currency startingCurrency) {
		final String startingSymbol = startingCurrency.getCurrencySymbol();
		
		final List<TradeCycle> validGraphCycles = new ArrayList<>();
		for(final CurrencyPair edge : startingCurrency.getPairs()) {
			final Currency secondCurrency = edge.getOppositeCurrency(startingSymbol);
					
			for(final CurrencyPair secondEdge : secondCurrency.getPairs()) {
				final Currency thirdCurrency = secondEdge.getOppositeCurrency(secondCurrency.getCurrencySymbol());
				
				for(final CurrencyPair thirdEdge : thirdCurrency.getPairs()) {
					if(thirdEdge.getOppositeCurrency(thirdCurrency.getCurrencySymbol()).equals(startingCurrency)) {
						validGraphCycles.add(new TradeCycle(startingSymbol, edge, secondEdge, thirdEdge));
					}
				}
			}
		}
		
		return validGraphCycles;
	}
}
