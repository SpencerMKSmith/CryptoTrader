package com.smks.cyclictrading.hitbtctypes;

import com.smks.cyclictrading.commontypes.Currency;
import lombok.Data;

@Data
@lombok.NoArgsConstructor
@lombok.EqualsAndHashCode(exclude={"baseCurrencyObject", "quoteCurrencyObject"}) //Caused cyclic hashcode issues
@lombok.ToString(exclude={"baseCurrencyObject", "quoteCurrencyObject"})
public class CurrencyPair {
    private String id;
    private String baseCurrency;
    private String quoteCurrency;
    private String quantityIncrement;
    private String tickSize;
    private String takeLiquidityRate;
    private String provideLiquidityRate;
    private String feeCurrency;
    private Currency baseCurrencyObject;
    private Currency quoteCurrencyObject;
    
    public Currency getOppositeCurrency(final String currency) {
    	if(this.baseCurrency.equals(currency))
    		return quoteCurrencyObject;
    	else if(this.quoteCurrency.equals(currency))
    		return baseCurrencyObject;
    	else
    		return null;
    }
}
