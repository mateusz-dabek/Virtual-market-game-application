package project.rest;


import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import project.dto.OpenPositionDTO;
import project.model.MarketSymbol;
import project.model.OpenPosition;
import project.security.JwtProvider;
import project.services.OpenPositionService;
import project.services.PriceService;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

@RestController
@RequestMapping(value = "/api/openPositions", produces = MediaType.APPLICATION_JSON_VALUE)
public class OpenPositionController {

    private final OpenPositionService openPositionService;
    private final JwtProvider jwtProvider;

    public OpenPositionController(OpenPositionService openPositionService, JwtProvider jwtProvider) {
        this.openPositionService = openPositionService;
        this.jwtProvider = jwtProvider;
    }

    @GetMapping
    public List<OpenPositionDTO> getUserOpenPositionsById(HttpServletRequest request) {
        Long id = jwtProvider.getIdFromToken(jwtProvider.getToken(request));
        return openPositionService.getUserOpenPositionsById(id);
    }

    @PostMapping
    public void createOpenPosition(@RequestBody OpenPositionDTO openPositionDTO, HttpServletRequest request) {
        Long id = jwtProvider.getIdFromToken(jwtProvider.getToken(request));
        openPositionService.createPosition(openPositionDTO.parsePosition(), id);
    }

    @DeleteMapping("/{id}")
    public void deleteOpenPostion(HttpServletRequest request, @PathVariable("id") Long id) {
        openPositionService.deletePosition(request, id);
    }

}
