package com.shmoney.category.repository;

import com.shmoney.category.entity.Subcategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SubcategoryRepository extends JpaRepository<Subcategory, Long> {

    List<Subcategory> findAllByCategoryIdAndCategoryOwnerIdOrderByNameAsc(Long categoryId, Long ownerId);

    Optional<Subcategory> findByIdAndCategoryOwnerId(Long id, Long ownerId);
}
