package com.shmoney.transaction.feed;

import com.shmoney.auth.security.AuthenticatedUser;
import com.shmoney.auth.security.CurrentUserProvider;
import com.shmoney.common.dto.PageResponse;
import com.shmoney.debt.entity.DebtTransactionDirection;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.List;

@Tag(name = "Transactions Feed")
@SecurityRequirement(name = "bearer-jwt")
@RestController
@RequestMapping("/api/transactions")
public class TransactionFeedController {
    
    private final TransactionFeedService feedService;
    private final CurrentUserProvider currentUserProvider;
    
    public TransactionFeedController(TransactionFeedService feedService,
                                     CurrentUserProvider currentUserProvider) {
        this.feedService = feedService;
        this.currentUserProvider = currentUserProvider;
    }
    
    @Operation(summary = "Лента всех транзакций пользователя")
    @GetMapping
    public PageResponse<TransactionFeedItem> getFeed(@RequestParam(defaultValue = "0") int page,
                                                     @RequestParam(defaultValue = "50") int size,
                                                     @RequestParam(required = false) TransactionFeedType type,
                                                     @RequestParam(name = "walletId", required = false) List<Long> walletIds,
                                                     @RequestParam(name = "categoryId", required = false) List<Long> categoryIds,
                                                     @RequestParam(name = "debtCounterpartyId", required = false)
                                                     List<Long> debtCounterpartyIds,
                                                     @RequestParam(required = false) DebtTransactionDirection debtDirection,
                                                     @RequestParam(required = false) TransactionFeedPeriod period,
                                                     @RequestParam(required = false)
                                                     @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                                                     OffsetDateTime from,
                                                     @RequestParam(required = false)
                                                     @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                                                     OffsetDateTime to) {
        AuthenticatedUser current = currentUserProvider.requireCurrentUser();
        
        return feedService.getFeed(current.id(), type, walletIds, categoryIds, debtCounterpartyIds, debtDirection,
                from, to, period, page, size);
    }
}
