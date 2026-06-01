package com.pairflow.couple;

import com.pairflow.config.CurrentUser;
import com.pairflow.couple.dto.BreakupRequest;
import com.pairflow.couple.dto.CoupleResponse;
import com.pairflow.couple.dto.CreateInviteResponse;
import com.pairflow.couple.dto.JoinRequest;
import com.pairflow.couple.dto.UpdateCoupleRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/couples")
public class CoupleController {

    private final CoupleService coupleService;

    public CoupleController(CoupleService coupleService) {
        this.coupleService = coupleService;
    }

    @PostMapping("/invite")
    public CreateInviteResponse invite() {
        return coupleService.createInvite(CurrentUser.id());
    }

    @PostMapping("/join")
    public CoupleResponse join(@Valid @RequestBody JoinRequest req) {
        return coupleService.join(CurrentUser.id(), req.code());
    }

    @GetMapping("/me")
    public CoupleResponse me() {
        return coupleService.getMyCouple(CurrentUser.id());
    }

    @PatchMapping("/{coupleId}")
    public CoupleResponse update(@PathVariable String coupleId, @Valid @RequestBody UpdateCoupleRequest req) {
        return coupleService.update(coupleId, CurrentUser.id(), req);
    }

    @PostMapping("/{coupleId}/breakup")
    public Map<String, Object> breakup(@PathVariable String coupleId, @Valid @RequestBody BreakupRequest req) {
        return coupleService.breakup(coupleId, CurrentUser.id(), req);
    }
}
