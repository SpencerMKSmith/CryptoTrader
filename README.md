# CryptoTrader

## Ideas:
* Overall idea: Make lots of trades that make good money.  With small quantities of ETH (.05 ~ $35) I have seen enough trades to provide a 10% gain daily.  These trade cycles are often exploited quickly by other people thus leaving
* Modularize with intention of supporting many different types of trades.
* Despite the above, begin with cyclical trading.
* Prelim UML diagrams in the works.  Thinking of using https://github.com/timmolter/XChange for the communication layer as it supports all major exchanges.
  * Will need to make some changes as it doesn't appear that getting all currencies/trading pairs from the exchange API is supported
  * Future: support web sockets https://github.com/bitrich-info/xchange-stream that should be able to detect good cycles more quickly
* Keep track of everything in trading accounts and show how much is being made
* Provide the possibility to execute more than 3 trades in a cycle, this has a low probabilty to provide good returns as each trade charges a fee, but it is worth taking a look at.
* Worker machines: One of the largest bottlenecks in finding profitable trade cycles is network call time.  It can take up to 30 seconds to check all 3 currency cycles from a given origin.  Within those 30 seconds many opportunites can be missed and exploited by others.  One option would be to have multiple worker nodes whos only purpose is to search for cycles quickly and then report back to a master node who will execute the trade when a profitable cycle is found.  The problem may also be remidied by a web socket implementation but I don't know if it's possible or feasible to listen on 300+ web streams simultaneously.

## Trading Strategy
* Cyclical Trading
 * Fixed price or Market price (if market can cause major inconsistencies between estimated and actual percent gain)
   * Provide option for a fail time/percent to bail out of a trade that may not be possible.  This could mean selling at market price or top of the order book to go back to the origin currency (whatever we start with).
 * AlwaysOnTop option.  If a trade doesn't execute at the fixed price, monitor the order and cancel/remake to ensure it remains on top of the order book.  This will cause the actual gain to be lower than percent gain but will most likely give better returns than executing at market price or waiting for the above.  If after a certain time of being on top of the book the trade still doesn't execute, abort the trade.
 * Potential cycles could be found by market making instead of exploiting as I have been doing.  This would mean executing all 3 trades as AlwaysOnTop agents, this can be time consuming and estimates will surely differ greatly from actual gain but it could be something interesting to explore.
 
