package com.ems.services;

import com.ems.models.Category;
import com.ems.repositories.CategoryRepository;

import java.util.List;

public class CategoryService {
    private CategoryRepository categoryRepository;

    public CategoryService() {
        this.categoryRepository = new CategoryRepository();
    }

    public List<Category> getAllCategories() {
        return categoryRepository.getAllCategories();
    }

    public Category getCategoryById(int id) {
        return categoryRepository.getCategoryById(id);
    }

    public boolean createCategory(Category category) {
        if (category.getCategoryName() == null || category.getCategoryName().isEmpty()) return false;
        return categoryRepository.createCategory(category);
    }

    public boolean deleteCategory(int id) {
        return categoryRepository.deleteCategory(id);
    }
}
