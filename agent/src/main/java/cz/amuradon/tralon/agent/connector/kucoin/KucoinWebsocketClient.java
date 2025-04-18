package cz.amuradon.tralon.agent.connector.kucoin;

import java.util.function.Consumer;

import com.kucoin.sdk.KucoinPrivateWSClient;
import com.kucoin.sdk.KucoinPublicWSClient;

import cz.amuradon.tralon.agent.connector.AccountBalance;
import cz.amuradon.tralon.agent.connector.OrderBookChange;
import cz.amuradon.tralon.agent.connector.OrderChange;
import cz.amuradon.tralon.agent.connector.Trade;
import cz.amuradon.tralon.agent.connector.WebsocketClient;
import cz.amuradon.tralon.agent.connector.WebsocketClientFactory;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
@Kucoin
@WebsocketClientFactory // Required for proper usage with Instance
public class KucoinWebsocketClient implements WebsocketClient {

	private final KucoinPublicWSClient wsClientPublic;
    
	private final KucoinPrivateWSClient wsClientPrivate;
	
	@Inject
	public KucoinWebsocketClient(final KucoinPublicWSClient wsClientPublic,
    		final KucoinPrivateWSClient wsClientPrivate) {
		this.wsClientPrivate = wsClientPrivate;
		this.wsClientPublic = wsClientPublic;
	}
	
	@Override
	public void onOrderChange(Consumer<OrderChange> callback) {
		wsClientPrivate.onOrderChange(e -> callback.accept(new KucoinOrderChange(e.getData())));
	}

	@Override
	public void onOrderBookChange(Consumer<OrderBookChange> callback, String symbol) {
		wsClientPublic.onLevel2Data(e -> callback.accept(new KucoinOrderBookChange(e.getData())), symbol);
	}

	@Override
	public void onAccountBalance(Consumer<AccountBalance> callback) {
		wsClientPrivate.onAccountBalance(e -> callback.accept(new KucoinAccountBalance(e.getData())));
	}

	@Override
	public void onTrade(Consumer<Trade> callback, String symbol) {
		wsClientPublic.onLevel3Data_V2(e -> callback.accept(new KucoinTrade(e.getData())), symbol);
		
	}

}
