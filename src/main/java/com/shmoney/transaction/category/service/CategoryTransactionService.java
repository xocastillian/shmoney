package com.shmoney.transaction.category.service;

import com.shmoney.auth.security.AuthenticatedUser;
import com.shmoney.category.entity.Category;
import com.shmoney.category.entity.Subcategory;
import com.shmoney.category.service.CategoryService;
import com.shmoney.category.service.SubcategoryService;
import com.shmoney.transaction.category.dto.CategoryTransactionCreateRequest;
import com.shmoney.transaction.category.dto.CategoryTransactionFilter;
import com.shmoney.transaction.category.dto.CategoryTransactionUpdateRequest;
import com.shmoney.transaction.category.entity.CategoryTransaction;
import com.shmoney.transaction.category.entity.CategoryTransactionType;
import com.shmoney.transaction.category.exception.CategoryTransactionNotFoundException;
import com.shmoney.transaction.category.exception.InvalidCategoryTransactionException;
import com.shmoney.transaction.category.repository.CategoryTransactionRepository;
import com.shmoney.transaction.category.repository.CategoryTransactionSpecifications;
import com.shmoney.wallet.entity.Wallet;
import com.shmoney.wallet.repository.WalletRepository;
import com.shmoney.wallet.service.WalletService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@Transactional
public class CategoryTransactionService {

    private static final Logger log = LoggerFactory.getLogger(CategoryTransactionService.class);

    private final CategoryTransactionRepository transactionRepository;
    private final WalletService walletService;
    private final WalletRepository walletRepository;
    private final CategoryService categoryService;
    private final SubcategoryService subcategoryService;

    public CategoryTransactionService(CategoryTransactionRepository transactionRepository,
                                      WalletService walletService,
                                      WalletRepository walletRepository,
                                      CategoryService categoryService,
                                      SubcategoryService subcategoryService) {
        this.transactionRepository = transactionRepository;
        this.walletService = walletService;
        this.walletRepository = walletRepository;
        this.categoryService = categoryService;
        this.subcategoryService = subcategoryService;
    }

    public CategoryTransaction create(AuthenticatedUser currentUser,
                                      CategoryTransactionCreateRequest request) {
        Wallet wallet = requireWalletOwner(currentUser.id(), request.walletId());
        Category category = categoryService.getOwnedCategory(request.categoryId(), currentUser.id());
        Subcategory subcategory = resolveSubcategory(currentUser.id(), request.subcategoryId(), category);

        CategoryTransaction transaction = new CategoryTransaction();
        transaction.setUser(wallet.getOwner());
        transaction.setWallet(wallet);
        transaction.setCategory(category);
        transaction.setSubcategory(subcategory);
        transaction.setType(request.type());
        transaction.setAmount(normalize(request.amount()));
        transaction.setCurrency(wallet.getCurrency());
        transaction.setDescription(request.description());
        transaction.setOccurredAt(request.occurredAt());
        validateCategorySelection(transaction.getCategory(), transaction.getSubcategory());

        CategoryTransaction saved = transactionRepository.save(transaction);
        applyBalanceDelta(wallet, request.type(), saved.getAmount());
        log.info("Category transaction created id={} user={} wallet={} type={} amount={}", saved.getId(),
                currentUser.id(), wallet.getId(), saved.getType(), saved.getAmount());
        return saved;
    }

    @Transactional(readOnly = true)
    public Page<CategoryTransaction> getPage(Long userId,
                                             CategoryTransactionFilter filter,
                                             Pageable pageable) {
        Specification<CategoryTransaction> specification = Specification
                .where(CategoryTransactionSpecifications.belongsToUser(userId))
                .and(CategoryTransactionSpecifications.hasWallet(filter.walletId()))
                .and(CategoryTransactionSpecifications.hasCategory(filter.categoryId()))
                .and(CategoryTransactionSpecifications.hasSubcategory(filter.subcategoryId()))
                .and(CategoryTransactionSpecifications.hasType(filter.type()))
                .and(CategoryTransactionSpecifications.occurredAfter(filter.from()))
                .and(CategoryTransactionSpecifications.occurredBefore(filter.to()));

        return transactionRepository.findAll(specification, pageable);
    }

    @Transactional(readOnly = true)
    public CategoryTransaction getOwnedById(Long userId, Long id) {
        return transactionRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new CategoryTransactionNotFoundException(id));
    }

    public CategoryTransaction update(AuthenticatedUser currentUser,
                                      Long id,
                                      CategoryTransactionUpdateRequest request) {
        CategoryTransaction existing = getOwnedById(currentUser.id(), id);
        Wallet originalWallet = existing.getWallet();
        BigDecimal originalAmount = existing.getAmount();
        CategoryTransactionType originalType = existing.getType();

        Wallet targetWallet = resolveWalletUpdate(currentUser, request.walletId(), existing);
        Category category = resolveCategoryUpdate(currentUser, request.categoryId(), existing);
        Subcategory subcategory = resolveSubcategoryUpdate(currentUser, request.subcategoryId(), category, existing);

        if (request.type() != null) {
            existing.setType(request.type());
        }
        if (request.amount() != null) {
            existing.setAmount(normalize(request.amount()));
        }
        if (request.occurredAt() != null) {
            existing.setOccurredAt(request.occurredAt());
        }
        if (request.description() != null) {
            existing.setDescription(request.description());
        }
        existing.setWallet(targetWallet);
        existing.setUser(targetWallet.getOwner());
        existing.setCategory(category);
        existing.setSubcategory(subcategory);
        existing.setCurrency(targetWallet.getCurrency());

        validateCategorySelection(existing.getCategory(), existing.getSubcategory());

        CategoryTransaction saved = transactionRepository.save(existing);
        revertBalanceDelta(originalWallet, originalType, originalAmount);
        applyBalanceDelta(saved.getWallet(), saved.getType(), saved.getAmount());
        log.info("Category transaction updated id={} user={}", saved.getId(), currentUser.id());
        return saved;
    }

    public void delete(AuthenticatedUser currentUser, Long id) {
        CategoryTransaction existing = getOwnedById(currentUser.id(), id);
        revertBalanceDelta(existing.getWallet(), existing.getType(), existing.getAmount());
        transactionRepository.delete(existing);
        log.info("Category transaction deleted id={} user={}", id, currentUser.id());
    }

    private Wallet requireWalletOwner(Long userId, Long walletId) {
        Wallet wallet = walletService.getById(walletId);
        if (!wallet.getOwner().getId().equals(userId)) {
            throw new InvalidCategoryTransactionException("Wallet does not belong to current user");
        }
        return wallet;
    }

    private Subcategory resolveSubcategory(Long userId, Long subcategoryId, Category category) {
        if (subcategoryId == null) {
            return null;
        }
        Subcategory subcategory = subcategoryService.getOwnedSubcategory(userId, subcategoryId);
        if (!subcategory.getCategory().getId().equals(category.getId())) {
            throw new InvalidCategoryTransactionException("Subcategory does not belong to specified category");
        }
        return subcategory;
    }

    private Wallet resolveWalletUpdate(AuthenticatedUser currentUser, Long walletId,
                                       CategoryTransaction existing) {
        if (walletId == null) {
            return existing.getWallet();
        }
        return requireWalletOwner(currentUser.id(), walletId);
    }

    private Category resolveCategoryUpdate(AuthenticatedUser currentUser, Long categoryId,
                                           CategoryTransaction existing) {
        if (categoryId == null) {
            return existing.getCategory();
        }
        return categoryService.getOwnedCategory(categoryId, currentUser.id());
    }

    private Subcategory resolveSubcategoryUpdate(AuthenticatedUser currentUser,
                                                 Long subcategoryId,
                                                 Category category,
                                                 CategoryTransaction existing) {
        if (subcategoryId == null) {
            return category.equals(existing.getCategory()) ? existing.getSubcategory() : null;
        }
        return resolveSubcategory(currentUser.id(), subcategoryId, category);
    }

    private void applyBalanceDelta(Wallet wallet, CategoryTransactionType type, BigDecimal amount) {
        BigDecimal delta = amount;
        if (type == CategoryTransactionType.EXPENSE) {
            delta = delta.negate();
        }
        updateWalletBalance(wallet, delta);
    }

    private void revertBalanceDelta(Wallet wallet, CategoryTransactionType type, BigDecimal amount) {
        BigDecimal delta = amount;
        if (type == CategoryTransactionType.EXPENSE) {
            delta = delta;
        } else {
            delta = delta.negate();
        }
        updateWalletBalance(wallet, delta);
    }

    private void updateWalletBalance(Wallet wallet, BigDecimal delta) {
        BigDecimal currentBalance = wallet.getBalance() == null
                ? BigDecimal.ZERO
                : wallet.getBalance();
        BigDecimal updated = currentBalance.add(delta);
        wallet.setBalance(updated);
        walletRepository.save(wallet);
    }

    private void validateCategorySelection(Category category, Subcategory subcategory) {
        if (category == null) {
            throw new InvalidCategoryTransactionException("Category must be provided");
        }
        if (subcategory != null && !subcategory.getCategory().getId().equals(category.getId())) {
            throw new InvalidCategoryTransactionException("Subcategory does not belong to category");
        }
    }

    private BigDecimal normalize(BigDecimal value) {
        return value == null ? null : value.setScale(2, RoundingMode.HALF_UP);
    }

    public Pageable buildPageable(int page, int size) {
        int resolvedSize = size <= 0 ? 50 : Math.min(size, 100);
        int resolvedPage = Math.max(page, 0);
        Sort sort = Sort.by(Sort.Order.desc("occurredAt"), Sort.Order.desc("id"));
        return PageRequest.of(resolvedPage, resolvedSize, sort);
    }
}
