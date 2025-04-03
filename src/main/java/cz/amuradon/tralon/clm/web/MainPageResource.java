package cz.amuradon.tralon.clm.web;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentSkipListMap;

import org.jboss.resteasy.reactive.RestForm;

import cz.amuradon.tralon.clm.connector.RestClient;
import cz.amuradon.tralon.clm.connector.WebsocketClient;
import cz.amuradon.tralon.clm.connector.mexc.Mexc;
import cz.amuradon.tralon.clm.strategies.SpotHedgingStrategy;
import cz.amuradon.tralon.clm.strategies.Strategy;
import io.quarkus.arc.impl.AnnotationLiterals;
import io.quarkus.logging.Log;
import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.literal.NamedLiteral;
import jakarta.enterprise.inject.literal.QualifierLiteral;
import jakarta.enterprise.util.AnnotationLiteral;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/")
public class MainPageResource {

	private static final String WALL_BEFORE = "Wall Before";
	private static final String MARKET_MAKING = "Market Making";
	private static final String SPOT_HEDGE = "Spot Hedge";

	private final Map<UUID, Strategy> runningStrategies;
	
	private final List<String> supportedExchanges;
	
	private final Instance<RestClient> restClientFactory;
	
	private final Instance<WebsocketClient> websocketClientFactory;
	
	@Inject
	public MainPageResource(Instance<RestClient> restClientFactory,
			Instance<WebsocketClient> websocketClientFactory) {
		runningStrategies = new ConcurrentSkipListMap<>();
		supportedExchanges = Arrays.asList("Binance", "Kucoin", "MEXC");
		this.restClientFactory = restClientFactory;
		this.websocketClientFactory = websocketClientFactory;
	}
	
	@GET
	@Produces(MediaType.TEXT_HTML)
	public TemplateInstance index() {
		return Templates.index(Arrays.asList(SPOT_HEDGE, MARKET_MAKING, WALL_BEFORE));
	}

	@GET
	@Path("/get-running")
	@Produces(MediaType.TEXT_HTML)
	public TemplateInstance getRunning() {
		return Templates.runningStrategies(runningStrategies);
	}

	@POST
	@Path("/stop-strategy")
	@Produces(MediaType.TEXT_HTML)
	public TemplateInstance stop(@RestForm String stopUuid) {
		Log.infof("Stopping %s", stopUuid);
		
		Strategy strategy = runningStrategies.remove(UUID.fromString(stopUuid));
		
		strategy.stop();
		
		return Templates.runningStrategies(runningStrategies);
	}
	
	@POST
	@Path("/choose-strategy")
	@Produces(MediaType.TEXT_HTML)
	public TemplateInstance runSpotHedge(@RestForm String strategy) {
		Log.info(strategy);
		if (strategy.equalsIgnoreCase(SPOT_HEDGE)) {
			return Templates.spotHedge(supportedExchanges);
		} else if (strategy.equalsIgnoreCase(MARKET_MAKING)) {
			return Templates.marketMaking(supportedExchanges);
		} else if (strategy.equalsIgnoreCase(WALL_BEFORE)) {
			return Templates.wallBefore(supportedExchanges);
		} else {
			return Templates.noneStrategy();
		}
	}

	@POST
	@Path("/run-spot-hedge")
	@Produces(MediaType.TEXT_HTML)
	public TemplateInstance runSpotHedge(@RestForm String exchangeName, @RestForm LocalDateTime endDateTime,
			@RestForm String baseAsset, @RestForm String quoteAsset, @RestForm BigDecimal price,
			@RestForm BigDecimal baseQuantity) {
		RestClient restClient = restClientFactory.select(Mexc.MexcLiteral.INSTANCE).get();
		WebsocketClient websocketClient = websocketClientFactory.select(Mexc.MexcLiteral.INSTANCE).get();
		Strategy strategy = new SpotHedgingStrategy(restClient, websocketClient, baseAsset, quoteAsset, price, baseQuantity);
		strategy.start();
		runningStrategies.put(UUID.randomUUID(), strategy);
		return Templates.runningStrategies(runningStrategies);
	}

	@CheckedTemplate
	public static class Templates {
		public static native TemplateInstance index(List<String> strategies);
		public static native TemplateInstance runningStrategies(Map<UUID, Strategy> runningStrategies);
		public static native TemplateInstance spotHedge(List<String> exchanges);
		public static native TemplateInstance marketMaking(List<String> exchanges);
		public static native TemplateInstance wallBefore(List<String> exchanges);
		public static native TemplateInstance noneStrategy();
	}
}
