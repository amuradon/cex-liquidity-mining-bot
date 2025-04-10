package cz.amuradon.tralon.agent.strategies;

import java.math.BigDecimal;
import java.util.Map;

import cz.amuradon.tralon.agent.connector.OrderBookUpdate;

public interface Strategy {
	
	void start();

	// XXX These are not really required on interface!
	void onOrderBookUpdate(OrderBookUpdate update, Map<BigDecimal, BigDecimal> orderBookSide);
	
	void onBaseBalanceUpdate(BigDecimal balance);
	
	void onQuoteBalanceUpdate(BigDecimal balance);

	void stop();
	
	String getDescription();
}
