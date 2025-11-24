package com.shmoney.category.repository;

import com.shmoney.category.entity.Category;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    @EntityGraph(attributePaths = "subcategories")
    List<Category> findAllByOwnerIdOrderByNameAsc(Long ownerId);

    @EntityGraph(attributePaths = "subcategories")
    Optional<Category> findByIdAndOwnerId(Long id, Long ownerId);
}
