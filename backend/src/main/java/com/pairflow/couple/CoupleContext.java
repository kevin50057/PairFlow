package com.pairflow.couple;

import com.pairflow.config.CurrentUser;
import org.springframework.stereotype.Component;

/**
 * The one dependency every couple-scoped module injects. It resolves the
 * authenticated user's active couple and enforces membership in a single place,
 * so feature services never have to re-implement the "is this user allowed to
 * touch this couple's data?" check (spec 9.4).
 */
@Component
public class CoupleContext {

    private final CoupleService coupleService;

    public CoupleContext(CoupleService coupleService) {
        this.coupleService = coupleService;
    }

    public String currentUserId() {
        return CurrentUser.id();
    }

    public Couple requireCouple() {
        return coupleService.requireActiveCouple(CurrentUser.id());
    }

    public String requireCoupleId() {
        return requireCouple().getId();
    }

    public String partnerId() {
        Couple couple = requireCouple();
        return couple.partnerOf(CurrentUser.id());
    }
}
