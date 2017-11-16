package com.smks.cyclictrading.CyclicTrading;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.mashape.unirest.http.exceptions.UnirestException;
import com.smks.cyclictrading.commontypes.Currency;
import com.smks.cyclictrading.commontypes.TradeCycle;
import com.smks.cyclictrading.hitbtctypes.CurrencyPair;

import lombok.Data;

@Data
@lombok.RequiredArgsConstructor
public class Trader {

	private static int finishedCount = 0;
	private static long startMillis;
	public static synchronized void incrementCount(double percentGain) {
		
		if(percentGain > PERCENT_GAIN_THRESHOLD) {
			System.out.println(percentGain);
		}
		finishedCount++;
		//if(finishedCount % 10 == 0 && finishedCount > 0)
			//System.out.println("Finished: " + finishedCount + ", average: " + (System.currentTimeMillis() - startMillis) / finishedCount);
	}
	
	private static final Double PERCENT_GAIN_THRESHOLD = .004; // .4%
	
	private final Map<String, Currency> currencyMap;
	
	public void startTrading(final String startingSymbol, final Double startingAmount) throws UnirestException, InterruptedException {
		
		final Currency startingCurrency = currencyMap.get(startingSymbol);
		
		// First, find valid cyclic edges that can be traded from my starting currency
		final List<TradeCycle> validGraphCycles = determineValidGraphCycles(startingCurrency);
		
		// Determine which trade will give me profit
		startMillis = System.currentTimeMillis();
		ExecutorService pool = Executors.newFixedThreadPool(10);

		while(true) {
			pool = Executors.newFixedThreadPool(10);
			for(final TradeCycle tradeCycle : validGraphCycles) {
				tradeCycle.setStartingVolume(startingAmount);
				pool.execute(tradeCycle);
				//if(percentGain >= PERCENT_GAIN_THRESHOLD)
				//	System.out.println(percentGain + tradeCycle.toString() + ", Date: " + System.currentTimeMillis());
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
