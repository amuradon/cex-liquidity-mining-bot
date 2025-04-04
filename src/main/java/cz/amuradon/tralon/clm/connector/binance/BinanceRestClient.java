package cz.amuradon.tralon.clm.connector.binance;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.binance.connector.client.SpotClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import cz.amuradon.tralon.clm.OrderType;
import cz.amuradon.tralon.clm.Side;
import cz.amuradon.tralon.clm.connector.AccountBalance;
import cz.amuradon.tralon.clm.connector.OrderBookResponse;
import cz.amuradon.tralon.clm.connector.OrderBookResponseImpl;
import cz.amuradon.tralon.clm.connector.RestClient;
import cz.amuradon.tralon.clm.connector.RestClientFactory;
import cz.amuradon.tralon.clm.model.Order;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
@Binance
@RestClientFactory // Required for proper usage with Instance
public class BinanceRestClient implements RestClient {

	private final SpotClient spotClient;
	
	private final ObjectMapper mapper;
	
	private int sizeScale = 4;
	
	@Inject
	public BinanceRestClient(SpotClient spotClient) {
		this.spotClient = spotClient;
		mapper = new ObjectMapper();
	}
	
	@Override
	public NewOrderBuilder newOrder() {
		return new BinanceNewOrderBuilder();
	}

	@Override
	public void cancelOrder(Order order) {
		spotClient.createTrade()
			.cancelOrder(param("symbol", order.symbol()).param("orderId", order.orderId()));
	}

	@Override
	public Map<String, Order> listOrders(String symbol) {
		final String response = spotClient.createTrade().getOpenOrders(param("symbol", symbol));
		try {
			return mapper.readValue(response, new TypeReference<List<BinanceOrder>>() { })
					.stream().collect(Collectors.toMap(o -> o.orderId(), o -> o));
		} catch (JsonProcessingException e) {
			throw new IllegalStateException("Could not read open orders.", e);
		}
	}

	@Override
	public List<? extends AccountBalance> listBalances() {
		final String response = spotClient.createTrade().account(new LinkedHashMap<>());
		try {
			return mapper.readValue(response, BinanceAccountInformation.class).balances();
		} catch (JsonProcessingException e) {
			throw new IllegalStateException("Could not read account information.", e);
		}
	}
	
	@Override
	public OrderBookResponse orderBook(String symbol) {
		try {
			String response = spotClient.createMarket().depth( param("symbol", symbol).param("limit", 5000));
			BinanceOrderBookResponse orderBookResponse = mapper.readValue(response, BinanceOrderBookResponse.class);
			return new OrderBookResponseImpl(orderBookResponse.sequence(),
					orderBookResponse.asks(), orderBookResponse.bids());
		} catch (JsonProcessingException e) {
			throw new IllegalStateException("Could not read order book.", e);
		}
	}
	
	@Override
	public void cacheSymbolDetails(String symbol) {
		try {
			String response = spotClient.createMarket().exchangeInfo(param("symbol", symbol));
			ExchangeInfo exchangeInfo = mapper.readValue(response, ExchangeInfo.class);
			for (SymbolInfo symbolInfo : exchangeInfo.symbols()) {
				if (symbol.equalsIgnoreCase(symbolInfo.symbol())) {
					for (Filter filter : symbolInfo.filters())
						if ("LOT_SIZE".equalsIgnoreCase(filter.filterType)) {
							String stepSize = filter.get("stepSize");
							sizeScale = new BigDecimal(stepSize).stripTrailingZeros().scale();
							break;
						}
					break;
				}
			}
		} catch (JsonProcessingException e) {
			throw new IllegalStateException("Could not read exchange info.", e);
		}
		
	}

	public final class BinanceNewOrderBuilder implements NewOrderBuilder {

		private final Map<String, Object> parameters = new HashMap<>();
		
		@Override
		public NewOrderBuilder clientOrderId(String clientOrderId) {
			parameters.put("newClientOrderId", clientOrderId);
			return this;
		}

		@Override
		public NewOrderBuilder side(Side side) {
			parameters.put("side", side.name());
			return this;
		}

		@Override
		public NewOrderBuilder symbol(String symbol) {
			parameters.put("symbol", symbol);
			return this;
		}

		@Override
		public NewOrderBuilder price(BigDecimal price) {
			parameters.put("price", price);
			return this;
		}

		@Override
		public NewOrderBuilder size(BigDecimal size) {
			parameters.put("quantity", size.setScale(sizeScale, RoundingMode.DOWN));
			return this;
		}

		@Override
		public NewOrderBuilder type(OrderType type) {
			parameters.put("type", type.name());
			if (type == OrderType.LIMIT) {
				parameters.put("timeInForce", "GTC");
			}
			return this;
		}

		@Override
		public String send() {
			return spotClient.createTrade().newOrder(parameters);
		}
	}
	
	private ParamsBuilder param(String key, Object value) {
		return new ParamsBuilder().param(key, value);
	}

	private static final class ParamsBuilder extends LinkedHashMap<String, Object> {
		
		private static final long serialVersionUID = 1L;

		ParamsBuilder param(String key, Object value) {
			put(key, value);
			return this;
		}

	}
}
