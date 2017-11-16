package com.smks.cyclictrading.commontypes;

import java.util.ArrayList;
import java.util.List;

import com.smks.cyclictrading.hitbtctypes.CurrencyPair;

@lombok.Getter
@lombok.Setter
@lombok.EqualsAndHashCode
@lombok.RequiredArgsConstructor
@lombok.ToString
public class Currency {

	private final String currencySymbol;
	private List<CurrencyPair> pairs = new ArrayList<>();
	
	public void addCurrencyPair(final CurrencyPair newPair) {
		pairs.add(newPair);
	}
}
