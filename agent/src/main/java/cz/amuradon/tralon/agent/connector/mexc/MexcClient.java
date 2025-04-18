package cz.amuradon.tralon.agent.connector.mexc;

import java.util.List;
import java.util.Map;

import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.jboss.resteasy.reactive.RestQuery;

import com.fasterxml.jackson.core.JsonFactoryBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;

import cz.amuradon.tralon.agent.connector.ListenKey;
import cz.amuradon.tralon.agent.connector.MyInputDecorator;
import io.quarkus.rest.client.reactive.ClientQueryParam;
import io.quarkus.rest.client.reactive.jackson.ClientObjectMapper;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

@ClientHeaderParam(name = "Content-Type", value = "application/json")
@RegisterRestClient(configKey = "mexc-api")
@Retry(maxRetries = 10)
public interface MexcClient {

	@Path("/exchangeInfo")
	@GET
	ExchangeInfo exchangeInfo(@RestQuery String symbol);

	@Path("/depth")
	@GET
	@ClientQueryParam(name = "limit", value = "5000")
	MexcOrderBookResponse orderBook(@RestQuery String symbol);
	
	@Path("/account")
	@GET
	@ClientHeaderParam(name = "X-MEXC-APIKEY", value = "${mexc.apiKey}")
	List<MexcAccountBalance> listBalances(@RestQuery Map<String, Object> queryParams);
	
	@Path("/openOrders")
	@GET
	@ClientHeaderParam(name = "X-MEXC-APIKEY", value = "${mexc.apiKey}")
	List<MexcOrder> openOrders(@RestQuery Map<String, Object> queryParams);
	
	@Path("/order")
	@POST
	@ClientHeaderParam(name = "X-MEXC-APIKEY", value = "${mexc.apiKey}")
	OrderResponse newOrder(@RestQuery Map<String, Object> queryParams);
	
	@Path("/order")
	@DELETE
	@ClientHeaderParam(name = "X-MEXC-APIKEY", value = "${mexc.apiKey}")
	OrderResponse cancelOrder(@RestQuery Map<String, Object> queryParams);

	@Path("/userDataStream")
	@POST
	@ClientHeaderParam(name = "X-MEXC-APIKEY", value = "${mexc.apiKey}")
	ListenKey userDataStream(@RestQuery Map<String, Object> queryParams);
	
	@ClientObjectMapper
	static ObjectMapper objectMapper(ObjectMapper defaultObjectMapper) {
		System.out.println("*** Custom Object Mapper ***");
		return defaultObjectMapper.copyWith(new JsonFactoryBuilder().inputDecorator(new MyInputDecorator()).build());
	}
}
