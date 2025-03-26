package cz.amuradon.tralon.clm.connector.kucoin;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.kucoin.sdk.rest.request.OrderCreateApiRequest;
import com.kucoin.sdk.rest.request.OrderCreateApiRequest.OrderCreateApiRequestBuilder;

import cz.amuradon.tralon.clm.Order;
import cz.amuradon.tralon.clm.Side;
import cz.amuradon.tralon.clm.connector.AccountBalance;
import cz.amuradon.tralon.clm.connector.RestClient;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@ApplicationScoped
@Named("KucoinRestClient")
public class KucoinRestClient implements RestClient {

	private final com.kucoin.sdk.KucoinRestClient restClient;
	
	@Inject
	public KucoinRestClient(final com.kucoin.sdk.KucoinRestClient restClient) {
		this.restClient = restClient;
	}
	
	@Override
	public NewOrderBuilder newOrder() {
		return new KucoinNewOrderBuilder();
	}
	
	@Override
	public void cancelOrder(String orderId) {
		try {
			restClient.orderAPI().cancelOrder(orderId);
		} catch (IOException e) {
			throw new IllegalStateException("Could not send cancel order request.", e);
		}
	}
	
	@Override
	public Map<String, Order> listOrders(String symbol) {
		try {
			return restClient.orderAPI().listOrders(symbol, null, null, null, "active", null, null, 20, 1).getItems().stream()
			.collect(Collectors.toMap(r -> r.getId(), r ->
					new Order(r.getId(), Side.getValue(r.getSide()), r.getSize(), r.getPrice())));
		} catch (IOException e) {
			throw new IllegalStateException("Could not list orders.", e);
		}
	}
	
	@Override
	public List<AccountBalance> listBalances() {
		try {
			return restClient.accountAPI().listAccounts(null, "trade").stream()
					.map(b -> new KucoinAccountBalance(b)).collect(Collectors.toList());
		} catch (IOException e) {
			throw new IllegalStateException("Could not account balances.", e);
		}
	}

	public final class KucoinNewOrderBuilder implements NewOrderBuilder {
		
		OrderCreateApiRequestBuilder builder = OrderCreateApiRequest.builder();

		@Override
		public NewOrderBuilder clientOrderId(String clientOrderId) {
			builder.clientOid(clientOrderId);
			return this;
		}

		@Override
		public NewOrderBuilder side(Side side) {
			builder.side(side.name().toLowerCase());
			return this;
		}

		@Override
		public NewOrderBuilder symbol(String symbol) {
			builder.symbol(symbol);
			return this;
		}

		@Override
		public NewOrderBuilder price(BigDecimal price) {
			builder.price(price);
			return this;
		}

		@Override
		public NewOrderBuilder size(BigDecimal size) {
			builder.size(size);
			return this;
		}

		@Override
		public NewOrderBuilder type(String type) {
			builder.type(type);
			return this;
		}

		@Override
		public String send() {
			try {
				return restClient.orderAPI().createOrder(builder.build()).getOrderId();
			} catch (IOException e) {
				throw new IllegalStateException("Could not send new order request.", e);
			}
		}
		
	}

}
