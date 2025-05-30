package cz.amuradon.tralon.agent.connector.binance;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record BinanceOrderBookResponse(@JsonProperty("lastUpdateId") long lastUpdateId,
		@JsonProperty("bids") List<List<String>> bids,
		@JsonProperty("asks") List<List<String>> asks) {

}
