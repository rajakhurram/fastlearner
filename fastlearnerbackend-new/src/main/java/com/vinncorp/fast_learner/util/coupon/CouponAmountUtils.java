package com.vinncorp.fast_learner.util.coupon;

import com.vinncorp.fast_learner.models.coupon.Coupon;
import com.vinncorp.fast_learner.util.enums.DiscountUnit;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class CouponAmountUtils {

    private CouponAmountUtils() {
    }

    public static double calculateNetPaid(double subscriptionAmount, Coupon coupon) {
        if (coupon == null) {
            return BigDecimal.valueOf(subscriptionAmount).setScale(2, RoundingMode.HALF_UP).doubleValue();
        }

        BigDecimal listPrice = BigDecimal.valueOf(subscriptionAmount);
        BigDecimal discountValue = BigDecimal.valueOf(
                coupon.getDiscount() != null ? coupon.getDiscount() : 0.0);

        BigDecimal discount = coupon.getDiscountUnit() == DiscountUnit.FIXED_AMOUNT
                ? discountValue
                : listPrice.multiply(discountValue)
                        .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);

        return listPrice.subtract(discount).max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}
