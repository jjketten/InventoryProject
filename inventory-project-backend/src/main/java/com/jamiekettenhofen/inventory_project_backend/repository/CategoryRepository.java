package com.jamiekettenhofen.inventory_project_backend.repository;

import com.jamiekettenhofen.inventory_project_backend.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {}
