package com.smks.cyclictrading.CyclicTrading;

import java.util.Arrays;
import java.util.List;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.smks.cyclictrading.hitbtctypes.*;

public class WebServices {

	public static final String HITBTC_SYMBOL_URL = "https://api.hitbtc.com/api/2/public/symbol";
	public static final String HITBTC_ORDER_BOOK_PREFIX = "https://api.hitbtc.com/api/2/public/orderbook/";
	public static final Integer ORDER_BOOK_LIMIT = 10;
	
	public static String test() throws UnirestException {
		HttpResponse<String> jsonResponse = Unirest.post("http://httpbin.org/post")
				  .header("accept", "application/json")
				  .queryString("apiKey", "123")
				  .field("parameter", "value")
				  .field("foo", "bar")
				  .asString();
		
		return "";
	}
	
	/*
	 * Will return all of the currently traded currency pairs
	 */
	public static List<CurrencyPair> getCurrencyPairs() throws UnirestException {
		HttpResponse<CurrencyPair[]> response = Unirest.get(HITBTC_SYMBOL_URL)
				  .header("accept", "application/json")
				  .header("Connection", "Keep-Alive")
				  .asObject(CurrencyPair[].class);

		return Arrays.asList(response.getBody());
	}
	
	/*
	 * Will return the order book for a given symbol
	 */
	public static OrderBook getOrderBookForSymbol(final String symbol) throws UnirestException {
		final String orderBookUrl = HITBTC_ORDER_BOOK_PREFIX + symbol + "?limit=" + ORDER_BOOK_LIMIT;
		HttpResponse<OrderBook> response = Unirest.get(orderBookUrl)
				  .header("accept", "application/json")
				  .asObject(OrderBook.class);
		
		return response.getBody();
	}
	
	/*
	 * 
	 */
}
