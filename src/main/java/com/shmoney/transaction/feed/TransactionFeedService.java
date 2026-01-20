package com.shmoney.transaction.feed;

import com.shmoney.common.dto.PageResponse;
import com.shmoney.debt.entity.DebtTransactionDirection;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class TransactionFeedService {
    
    private final TransactionFeedRepository repository;
    
    public TransactionFeedService(TransactionFeedRepository repository) {
        this.repository = repository;
    }
    
    public PageResponse<TransactionFeedItem> getFeed(Long userId,
                                                     TransactionFeedType type,
                                                     List<Long> walletIds,
                                                     List<Long> categoryIds,
                                                     List<Long> debtCounterpartyIds,
                                                     DebtTransactionDirection debtDirection,
                                                     OffsetDateTime from,
                                                     OffsetDateTime to,
                                                     TransactionFeedPeriod period,
                                                     int page,
                                                     int size) {
        OffsetDateTime fromDate = from;
        OffsetDateTime toDate = to;
        
        if (period != null) {
            TransactionFeedPeriod.DateRange range = period.resolve(OffsetDateTime.now());
            fromDate = range.from();
            toDate = range.to();
        }
        
        TransactionFeedRepository.PagedFeedResult result = repository.fetch(userId, type, walletIds, categoryIds,
                debtCounterpartyIds, debtDirection, fromDate, toDate, page, size);
        return PageResponse.of(result.totalCount(), result.page(), result.size(), result.items());
    }
}
