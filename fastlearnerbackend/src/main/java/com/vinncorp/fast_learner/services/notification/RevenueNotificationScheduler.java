package com.vinncorp.fast_learner.services.notification;

import com.vinncorp.fast_learner.models.payout.InstructorSales;
import com.vinncorp.fast_learner.rabbitmq.RabbitMQProducer;
import com.vinncorp.fast_learner.repositories.payout.InstructorSalesRepository;
import com.vinncorp.fast_learner.util.enums.NotificationContentType;
import com.vinncorp.fast_learner.util.enums.NotificationType;
import com.vinncorp.fast_learner.util.enums.PayoutStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RevenueNotificationScheduler {
    private final RabbitMQProducer rabbitMQProducer;
    private final InstructorSalesRepository instructorSalesRepository;

//    @Scheduled(cron = "0/30 * * * * ?")
    @Scheduled(cron = "0 0 0 1 * ?")
    public void sendRevenueNotificationOnMonthStart() {
        List<InstructorSales> sales = instructorSalesRepository
                .findAll()
                .stream()
                .filter(sale -> sale.getTotalSales() > 0 && sale.getStatus().equals(PayoutStatus.PENDING))
                .toList();

        for (InstructorSales sale : sales) {
            log.info("total sales: {} by instructor id: {}", sale.getTotalSales(), sale.getInstructorId());
            rabbitMQProducer.sendRevenueMessage(
                    sale.getInstructorId(),
                    sale.getTotalSales(),
                    NotificationContentType.TEXT,
                    NotificationType.MONTHLY_REVENUE
            );
        }
    }
}
