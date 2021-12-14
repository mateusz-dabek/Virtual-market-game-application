package project.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import pro.xstore.api.message.codes.PERIOD_CODE;
import pro.xstore.api.message.command.APICommandFactory;
import pro.xstore.api.message.error.APICommandConstructionException;
import pro.xstore.api.message.error.APICommunicationException;
import pro.xstore.api.message.error.APIReplyParseException;
import pro.xstore.api.message.records.*;
import pro.xstore.api.message.response.*;
import pro.xstore.api.streaming.StreamingListener;
import pro.xstore.api.sync.Credentials;
import pro.xstore.api.sync.ServerData;
import pro.xstore.api.sync.SyncAPIConnector;
import project.model.MarketSymbol;
import project.model.OpenPosition;
import project.model.PendingPosition;
import project.repositories.OpenPositionRepository;
import project.repositories.PendingPositionRepository;
import reactor.core.publisher.Flux;

import java.io.*;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

@Service
public class PriceService {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final OpenPositionRepository openPositionRepository;
    private final PendingPositionRepository pendingPositionRepository;

    public PriceService(OpenPositionRepository openPositionRepository, PendingPositionRepository pendingPositionRepository) {
        this.openPositionRepository = openPositionRepository;
        this.pendingPositionRepository = pendingPositionRepository;
        forexSymbols.forEach(s -> forexSymbolsPrice.add(new MarketSymbol(s, 0.0, 0.0)));
    }

    //
    private List<String> forexSymbols = Arrays.asList(
            "EURJPY", "GBPCHF", "EURCHF", "GBPUSD", "AUDUSD", "GBPJPY", "USDCHF", "EURUSD", "USDCAD", "EURGBP", "USDJPY", "NZDUSD",
            "BITCOIN", "ETHEREUM", "RIPPLE", "LITECOIN", "EOS",
            "GOLD", "SILVER", "PLATINUM");
    private List<MarketSymbol> forexSymbolsPrice = new ArrayList<>();
    static public List<MarketSymbol> forexSymbolsPriceCopy;
    //
    final private Credentials credentials = new Credentials(10859916, "Pomidor1");
    private SyncAPIConnector connector;
    private LoginResponse loginResponse;


    @EventListener(ApplicationReadyEvent.class)
    private void loginToExternalAPI() throws APICommunicationException {
        try {
            connector = new SyncAPIConnector(ServerData.ServerEnum.DEMO);
            loginResponse = APICommandFactory.executeLoginCommand(
                    connector,
                    credentials
            );
            StreamingListener sl = new StreamingListener() {
                @Override
                public void receiveTickRecord(STickRecord tickRecord) {
                    if (tickRecord.getLevel() == 0) {
                        forexSymbolsPrice.stream()
                                .filter(s -> s.getSymbol().equals(tickRecord.getSymbol()))
                                .findFirst()
                                .ifPresent(s -> {
                                    s.setBid(tickRecord.getBid());
                                    s.setAsk(tickRecord.getAsk());
                                });
                        forexSymbolsPriceCopy = new ArrayList<>(forexSymbolsPrice);
                        checkLiquidationPriceOpenPositions();
                        checkExecutePricePendingPositions();
                    }
                }
            };
            connector.connectStream(sl);
            connector.subscribePrices(forexSymbols);
        } catch (IOException | APICommunicationException | APICommandConstructionException | APIReplyParseException | APIErrorResponse e) {

        }
    }

    private void checkLiquidationPriceOpenPositions() {
        List<OpenPosition> openPositions = openPositionRepository.findAll();

        Predicate<OpenPosition> predicateForShort = openPosition ->
                openPosition.getLiquidationPrice() <= forexSymbolsPriceCopy.stream() // 7800 <= 7000
                        .filter(symbol -> symbol.getSymbol().equals(openPosition.getSymbol())) // jeśli short to cena rynkowa = ASK
                        .mapToDouble(MarketSymbol::getAsk)
                        .sum();

        openPositions.stream()
                .filter(openPosition -> openPosition.getTypePosition().equals(OpenPosition.Position.SHORT))
                .filter(predicateForShort)
                .forEach(openPosition -> {
                    System.out.println("Usunieto SHORTA: " + openPosition);
                    openPositionRepository.delete(openPosition);
                });

        Predicate<OpenPosition> predicateForLong = openPosition ->
                forexSymbolsPriceCopy.stream()
                        .filter(symbol -> symbol.getSymbol().equals(openPosition.getSymbol())) // jeśli LONG to cena rynkowa = BID
                        .mapToDouble(MarketSymbol::getBid)
                        .sum() <= openPosition.getLiquidationPrice();

        openPositions.stream()
                .filter(openPosition -> openPosition.getTypePosition().equals(OpenPosition.Position.LONG))
                .filter(predicateForLong)
                .forEach(openPosition -> {
                    System.out.println("Usunieto LONGA: " + openPosition);
                    openPositionRepository.delete(openPosition);
                });
    }

    private void checkExecutePricePendingPositions() {
        List<PendingPosition> pendingPositions = pendingPositionRepository.findAll();
        pendingPositions.forEach(pendingPosition -> {
            Optional<Double> currentPrice = PriceService.receiveCurrentPrice(pendingPosition.getSymbol(), pendingPosition.getTypePosition());
            if (pendingPosition.getSmallerThanCurrentPrice() && currentPrice.get() >= pendingPosition.getPriceExecute()) {
                convertPendingPositionToOpenPosition(pendingPosition);
            } else if (!pendingPosition.getSmallerThanCurrentPrice() && currentPrice.get() <= pendingPosition.getPriceExecute()) {
                convertPendingPositionToOpenPosition(pendingPosition);
            }
        });
    }

    private void convertPendingPositionToOpenPosition(PendingPosition pendingPosition) {
        OpenPosition openPosition = new OpenPosition();
        openPosition.setSymbol(pendingPosition.getSymbol());
        openPosition.setOpenPrice(pendingPosition.getPriceExecute());
        openPosition.setLeverage(pendingPosition.getLeverage());
        openPosition.setTypePosition(pendingPosition.getTypePosition());
        openPosition.setValue(pendingPosition.getValue());
        openPosition.setUserId(pendingPosition.getUserId());
        BigDecimal liquidationPrice = PrecisionCalculate.calculateLiquidationPrice(pendingPosition.getPriceExecute(), pendingPosition.getLeverage(), pendingPosition.getTypePosition());
        openPosition.setLiquidationPrice(liquidationPrice.doubleValue());
        openPosition.setOpenTime(LocalDateTime.now());

        openPositionRepository.save(openPosition);
        pendingPositionRepository.delete(pendingPosition);
    }

    //    --
    public Flux<String> receiveStreamPriceSymbol() throws JsonProcessingException {
        Flux<Long> interval = Flux.interval(Duration.ofSeconds(5));
        Flux<String> events = Flux.fromStream(Stream.generate(this::convertMapToJSON));
        return Flux.zip(events, interval, (key, value) -> key);
    }

    private String convertMapToJSON() {
        String json = null;
        try {
            json = objectMapper.writeValueAsString(forexSymbolsPrice);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return json;
    }

    public String receiveChartCandlesData(String symbol, PERIOD_CODE period) {
        ChartLastInfoRecord config;
        long amountOfMonths = getTailoredAmountOfMonths(period);
        config = new ChartLastInfoRecord(symbol, period, LocalDateTime.now().minusMonths(amountOfMonths).atZone(ZoneOffset.UTC).toInstant().toEpochMilli());
        System.out.println(LocalDateTime.now().minusMonths(amountOfMonths).atZone(ZoneOffset.UTC).toInstant().toEpochMilli());
        try {
            ChartResponse chartResponse = APICommandFactory.executeChartLastCommand(connector, config);
            chartResponse.getRateInfos().forEach(rateInfoRecord -> {
                BigDecimal open = new BigDecimal(rateInfoRecord.getOpen());
                BigDecimal dividedBy = new BigDecimal(Math.pow(10, chartResponse.getDigits()));
                open = open.divide(dividedBy);
                rateInfoRecord.setOpen(open.doubleValue());

                BigDecimal high = new BigDecimal(rateInfoRecord.getHigh());
                high = high.divide(dividedBy);
                high = high.add(open);
                rateInfoRecord.setHigh(high.doubleValue());

                BigDecimal low = new BigDecimal(rateInfoRecord.getLow());
                low = low.divide(dividedBy);
                low = low.add(open);
                rateInfoRecord.setLow(low.doubleValue());

                BigDecimal close = new BigDecimal(rateInfoRecord.getClose());
                close = close.divide(dividedBy);
                close = close.add(open);
                rateInfoRecord.setClose(close.doubleValue());
            });
            try {
                return objectMapper.writeValueAsString(chartResponse.getRateInfos());
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        } catch (APICommandConstructionException | APIErrorResponse | APIReplyParseException | APICommunicationException e) {
            e.printStackTrace();
        }
        return "no data";
    }

    private Long getTailoredAmountOfMonths(PERIOD_CODE period) {
        if (period.getCode() < 30)
            return 1L;
        else if (period.getCode() < 240)
            return 6L;
        else if (period.getCode() <= 1440)
            return 12L;
        else return 24L;
    }

    // ---
    static Optional<Double> receiveCurrentPrice(String symbol, OpenPosition.Position type) {
        return forexSymbolsPriceCopy.stream()
                .filter(marketSymbol -> marketSymbol.getSymbol().equals(symbol))
                .findFirst()
                .map(marketSymbol -> {
                            if (type.equals(OpenPosition.Position.SHORT))
                                return marketSymbol.getBid(); // po cenie bid zawiera się transakcję sprzedaży
                            else return marketSymbol.getAsk(); // po cenie ask zawiera się transakcję kupna
                        }
                );
    }
}
