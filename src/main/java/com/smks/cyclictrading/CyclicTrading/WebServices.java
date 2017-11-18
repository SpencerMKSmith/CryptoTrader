package com.smks.cyclictrading.CyclicTrading;

import java.util.Arrays;
import java.util.Base64;
import java.util.List;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.smks.cyclictrading.commontypes.Order;
import com.smks.cyclictrading.hitbtctypes.*;

public class WebServices {

	public static final String HITBTC_SYMBOL_URL = "https://api.hitbtc.com/api/2/public/symbol";
	public static final String HITBTC_ORDER_BOOK_PREFIX = "https://api.hitbtc.com/api/2/public/orderbook/";
	public static final Integer ORDER_BOOK_LIMIT = 10;
	
	private static final String apiKey = "119a651e55b28778575a3694dc4a2b9a";
	private static final String secret = "bbf6672d7debb0fe8ec47bf4a5b74332";
	
	/*
	 * Will return all of the currently traded currency pairs
	 */
	public static List<CurrencyPair> getCurrencyPairs() throws UnirestException {
		try{
			HttpResponse<CurrencyPair[]> response = Unirest.get(HITBTC_SYMBOL_URL)
					  .header("accept", "application/json")
					  .header("Connection", "Keep-Alive")
					  .asObject(CurrencyPair[].class);
	
			return Arrays.asList(response.getBody());
		} catch(Exception e) {
			return null;
		}
	}
	
	/*
	 * Will return the order book for a given symbol
	 */
	public static OrderBook getOrderBookForSymbol(final String symbol) throws UnirestException {
		
		try{
			final String orderBookUrl = HITBTC_ORDER_BOOK_PREFIX + symbol + "?limit=" + ORDER_BOOK_LIMIT;
			
			HttpResponse<OrderBook> response = Unirest.get(orderBookUrl)
					  .header("accept", "application/json")
					  .asObject(OrderBook.class);
			
			return response.getBody();
		} catch (Exception e) {
			return null;
		}

	}
	
	/*
	 * 
	 */
	public static String postOrder(final Order order) throws UnirestException {
		
		HttpResponse<String> jsonResponse = Unirest.post("https://api.hitbtc.com/api/2/order")
				.header("Authorization", getAuthHeader())
				.header("Content-Type", "application/json")
				.body(order)
				.asString();

		System.out.println(jsonResponse.getBody());
		return jsonResponse.getBody();
	}
	
	
	private static String getAuthHeader() {
		return "Basic " + Base64.getEncoder().encodeToString((apiKey + ":" + secret).getBytes());
	}
}
