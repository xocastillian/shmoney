package com.shmoney.transaction.feed;

import com.shmoney.common.dto.PageResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
@Transactional(readOnly = true)
public class TransactionFeedService {

    private final TransactionFeedRepository repository;

    public TransactionFeedService(TransactionFeedRepository repository) {
        this.repository = repository;
    }

    public PageResponse<TransactionFeedItem> getFeed(Long userId,
                                                     TransactionFeedType type,
                                                     Long walletId,
                                                     Long categoryId,
                                                     Long subcategoryId,
                                                     OffsetDateTime from,
                                                     OffsetDateTime to,
                                                     int page,
                                                     int size) {
        TransactionFeedRepository.PagedFeedResult result = repository.fetch(userId, type, walletId, categoryId,
                subcategoryId, from, to, page, size);
        return PageResponse.of(result.totalCount(), result.page(), result.size(), result.items());
    }
}
