package project.rest;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import project.dto.PendingPositionDTO;
import project.security.JwtProvider;
import project.services.PendingPositionService;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping(value = "/api/pendingPositions", produces = MediaType.APPLICATION_JSON_VALUE)
public class PendingPositionController {

    private final PendingPositionService pendingPositionService;
    private final JwtProvider jwtProvider;

    public PendingPositionController(PendingPositionService pendingPositionService, JwtProvider jwtProvider) {
        this.pendingPositionService = pendingPositionService;
        this.jwtProvider = jwtProvider;
    }

    @GetMapping
    public List<PendingPositionDTO> getUserPendingPositionsById(HttpServletRequest request) {
        Long id = jwtProvider.getIdFromToken(jwtProvider.getToken(request));
        return pendingPositionService.getUserOpenPositionsById(id);
    }

    @PostMapping
    public void createPendingPosition(@RequestBody PendingPositionDTO pendingPositionDTO,  HttpServletRequest request) {
        Long id = jwtProvider.getIdFromToken(jwtProvider.getToken(request));
        pendingPositionDTO.setUserId(id);
        pendingPositionService.createPosition(pendingPositionDTO.parsePosition());
    }

    @DeleteMapping("/{id}")
    public void deletePendingPostion(HttpServletRequest request, @PathVariable("id") Long id) {
        pendingPositionService.deletePosition(request, id);
    }
}
