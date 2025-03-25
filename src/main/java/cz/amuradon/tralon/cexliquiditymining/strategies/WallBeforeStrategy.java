package cz.amuradon.tralon.cexliquiditymining.strategies;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.kucoin.sdk.KucoinRestClient;

import cz.amuradon.tralon.cexliquiditymining.Order;
import cz.amuradon.tralon.cexliquiditymining.PriceProposal;
import cz.amuradon.tralon.cexliquiditymining.Side;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;

// @Singleton required due to abstract parent class
@Singleton
@Named("WallBeforeStrategy")
public class WallBeforeStrategy extends AbstractStrategy {

	private final BigDecimal sideVolumeThreshold;
	
    @Inject
    public WallBeforeStrategy(
    		@ConfigProperty(name = "orderBookQuoteVolumeBefore") final int sideVolumeThreshold, 
    		@ConfigProperty(name = "priceChangeDelayMs") final int priceChangeDelayMs,
    		final Map<Side, PriceProposal> priceProposals,
    		final KucoinRestClient restClient,
    		@ConfigProperty(name = "baseToken") final String baseToken,
    		@ConfigProperty(name = "quoteToken") final String quoteToken,
    		@ConfigProperty(name = "maxQuoteBalanceToUse") final int maxBalanceToUse,
    		final Map<String, Order> orders) {
    	super(priceChangeDelayMs, priceProposals, restClient, baseToken, quoteToken, maxBalanceToUse, orders);
    	this.sideVolumeThreshold = new BigDecimal(sideVolumeThreshold);
	}
	
    BigDecimal getTargetPriceLevel(Map<BigDecimal, BigDecimal> aggregatedOrders) {
		BigDecimal volume = BigDecimal.ZERO;
    	BigDecimal price = BigDecimal.ZERO;
    	for (Entry<BigDecimal, BigDecimal> entry : aggregatedOrders.entrySet()) {
    		price = entry.getKey();
			volume = volume.add(price.multiply(entry.getValue()));
			if (volume.compareTo(sideVolumeThreshold) >= 0) {
				break;
			}
		}
    	
    	return price;
    }
}
