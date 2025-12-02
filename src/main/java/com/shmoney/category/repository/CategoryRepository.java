package com.shmoney.category.repository;

import com.shmoney.category.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findAllByOwnerIdOrderByNameAsc(Long ownerId);

    Optional<Category> findByIdAndOwnerId(Long id, Long ownerId);

    List<Category> findAllByOwnerIdAndIdIn(Long ownerId, Iterable<Long> ids);
}
