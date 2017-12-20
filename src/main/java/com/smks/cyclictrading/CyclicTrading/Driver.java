package com.smks.cyclictrading.CyclicTrading;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mashape.unirest.http.ObjectMapper;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.smks.cyclictrading.commontypes.Currency;
import com.smks.cyclictrading.commontypes.Order;
import com.smks.cyclictrading.hitbtctypes.CurrencyPair;

public class Driver {

	public static final String STARTING_SYMBOL = "BTC";
	public static final Double STARTING_AMOUNT = 0.0014;
	
	final static Logger logger = Logger.getLogger(Driver.class);
	
    public static void main( String[] args ) throws UnirestException, InterruptedException
    {
    	// Setup unirest so that the mapping to custom objects works correctly
    	setupUnirest();
    	
    	// Get a graph of the currencies and how they can be traded
        final List<CurrencyPair> pairs = WebServices.getCurrencyPairs();
        final Map<String, Currency> currencyMap = getCurrencyMap(pairs);

        final Trader trader = new Trader(currencyMap);
        trader.startTrading(STARTING_SYMBOL, STARTING_AMOUNT);
    }
    
    public static Map<String, Currency> getCurrencyMap(final List<CurrencyPair> pairs) {
    	    	
        final Map<String, Currency> currencyMap = new HashMap<>();

    	for(CurrencyPair pair : pairs) {
    		createCurrencyIfDoesntExist(currencyMap, pair.getBaseCurrency());
    		createCurrencyIfDoesntExist(currencyMap, pair.getQuoteCurrency());

    		Currency baseCurrency = currencyMap.get(pair.getBaseCurrency());
    		Currency quoteCurrency = currencyMap.get(pair.getQuoteCurrency());
    		
    		baseCurrency.addCurrencyPair(pair);
    		quoteCurrency.addCurrencyPair(pair);
    		
    		pair.setBaseCurrencyObject(baseCurrency);
    		pair.setQuoteCurrencyObject(quoteCurrency);
    	}
    	return currencyMap;
    }
    
    public static void createCurrencyIfDoesntExist(final Map<String, Currency> currencyMap, final String currencyTicker) {
    	if(!currencyMap.containsKey(currencyTicker))
    		currencyMap.put(currencyTicker, new Currency(currencyTicker));
    }
    
    public static void setupUnirest() {
    	Unirest.setObjectMapper(new ObjectMapper() {
    	    private com.fasterxml.jackson.databind.ObjectMapper jacksonObjectMapper = new com.fasterxml.jackson.databind.ObjectMapper();

    	    public <T> T readValue(String value, Class<T> valueType) {
    	        try {
    	            return jacksonObjectMapper.readValue(value, valueType);
    	        } catch (IOException e) {
    	            throw new RuntimeException(e);
    	        }
    	    }

    	    public String writeValue(Object value) {
    	        try {
    	            return jacksonObjectMapper.writeValueAsString(value);
    	        } catch (JSONException e) {
    	            throw new RuntimeException(e);
    	        } catch (JsonProcessingException e) {
					e.printStackTrace();
				}
				return null;
    	    }
    	});
    }
    
}
