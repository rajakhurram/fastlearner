package com.vinncorp.fast_learner.controllers.coupon;

import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.models.coupon.Coupon;
import com.vinncorp.fast_learner.request.coupon.CouponRequest;
import com.vinncorp.fast_learner.services.coupon.ICouponService;
import com.vinncorp.fast_learner.util.Constants.APIUrls;
import com.vinncorp.fast_learner.util.Message;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping(APIUrls.COUPON)
@RequiredArgsConstructor
public class CouponController {

    private final ICouponService service;

    @GetMapping(APIUrls.COUPON_FETCH_ALL)
    public ResponseEntity<Message<List<Coupon>>> fetchAll(Principal principal) throws EntityNotFoundException {
        var m = service.fetchAll();
        return ResponseEntity.status(m.getStatus()).body(m);
    }

    @PostMapping(APIUrls.COUPON_CREATE)
    public ResponseEntity<Message<String>> create(@Valid @RequestBody CouponRequest request, Principal principal)
            throws InternalServerException, EntityNotFoundException {
        var m = service.create(request);
        return ResponseEntity.status(m.getStatus()).body(m);
    }

    @PutMapping(APIUrls.COUPON_UPDATE)
    public ResponseEntity<Message<String>> update(@Valid @RequestBody CouponRequest request, Principal principal)
            throws InternalServerException, EntityNotFoundException {
        var m = service.update(request);
        return ResponseEntity.status(m.getStatus()).body(m);
    }

    @DeleteMapping(APIUrls.COUPON_DELETE)
    public ResponseEntity<Message<String>> delete(@Valid @NotNull @RequestParam Long id, Principal principal) {
        var m = service.delete(id);
        return ResponseEntity.status(m.getStatus()).body(m);
    }

    @GetMapping(APIUrls.COUPON_VALIDATE)
    public  ResponseEntity<?> validate(
            @RequestParam String coupon, @RequestParam String couponType,
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) Long subscriptionId ,
            Principal principal) throws EntityNotFoundException, BadRequestException {
        var m = service.validateDiscount(coupon, couponType, courseId,subscriptionId ,principal.getName());
        return ResponseEntity.status(m.getStatus()).body(m);
    }
}
