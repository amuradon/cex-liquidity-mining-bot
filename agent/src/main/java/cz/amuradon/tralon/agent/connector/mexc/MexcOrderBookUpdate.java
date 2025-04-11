package cz.amuradon.tralon.agent.connector.mexc;

import java.math.BigDecimal;

import cz.amuradon.tralon.agent.OrderBook;
import cz.amuradon.tralon.agent.Side;
import cz.amuradon.tralon.agent.connector.OrderBookUpdate;

/**
 * Used for Websocket update message.
 */
public class MexcOrderBookUpdate implements OrderBookUpdate {

	private final long lastUpdateId;
	private final BigDecimal price;
	private final BigDecimal size;
	private final Side side;
	
	public MexcOrderBookUpdate(MexcOrderBookChangeRecord data, Side side, long lastUpdateId) {
		this.lastUpdateId = lastUpdateId;
		price = data.price();
		size = data.volume();
		this.side = side;
	}

	@Override
	public long sequence() {
		return lastUpdateId;
	}

	@Override
	public BigDecimal price() {
		return price;
	}

	@Override
	public BigDecimal size() {
		return size;
	}

	@Override
	public Side side() {
		return side;
	}

	@Override
	public boolean setSequenceIfShouldBeProcessed(OrderBook orderBook) {
		// Process updates with the same last update ID
		if (lastUpdateId >= orderBook.sequence()) {
			orderBook.setSequence(lastUpdateId);
			return true;
		} else {
			return false;
		}
	}

}
