package project.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pro.xstore.api.message.codes.PERIOD_CODE;
import project.services.PriceService;
import reactor.core.publisher.Flux;

import java.io.IOException;

@RestController
@RequestMapping(value = "/api/trade", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
public class TradeController {

    final private PriceService priceService;

    public TradeController(PriceService priceService) {
        this.priceService = priceService;
    }

    @GetMapping("/forex")
    public Flux<String> streamPricesForex() throws JsonProcessingException {
        try {
            return priceService.receiveStreamPriceSymbol();
        } catch (IOException e) {


        }
        return streamPricesForex();
    }

    @GetMapping("/chart")
    public String chartCandlesData(@RequestParam String symbol, @RequestParam PERIOD_CODE period) {
        if (period == null) {
            period = PERIOD_CODE.PERIOD_D1;
        }
        return priceService.receiveChartCandlesData(symbol, period);
    }
}
