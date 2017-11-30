package com.smks.cyclictrading.hitbtctypes;

import java.util.List;

import lombok.Data;

@Data
@lombok.ToString
@lombok.Getter
public class ActiveOrder
{
    private String updatedAt; 
    private String id; 
    private String price; 
    private String symbol;
    private String clientOrderId; 
    private String status; 
    private String side; 
    private String createdAt; 
    private String cumQuantity; 
    private String quantity; 
    private String timeInForce; 
    private String type; 
    private List<TradeReport> tradesReport;
    
    public boolean isFilled() {
    	return "filled".equals(this.status);
    }
}
