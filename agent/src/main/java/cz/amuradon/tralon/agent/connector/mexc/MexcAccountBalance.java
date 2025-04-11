package cz.amuradon.tralon.agent.connector.mexc;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import cz.amuradon.tralon.agent.connector.AccountBalance;

/**
 * Used for REST API response.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record MexcAccountBalance(@JsonProperty("asset") String asset,
		@JsonProperty("free") BigDecimal available) implements AccountBalance {

}
