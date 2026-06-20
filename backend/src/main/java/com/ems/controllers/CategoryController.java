package com.ems.controllers;

import com.ems.models.Category;
import com.ems.services.CategoryService;
import io.javalin.http.Context;

public class CategoryController {
    private CategoryService categoryService;

    public CategoryController() {
        this.categoryService = new CategoryService();
    }

    public void getAllCategories(Context ctx) {
        ctx.json(categoryService.getAllCategories());
    }

    public void getCategoryById(Context ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        Category category = categoryService.getCategoryById(id);
        if (category != null) {
            ctx.json(category);
        } else {
            ctx.status(404).json("{\"message\": \"Category not found\"}");
        }
    }

    public void createCategory(Context ctx) {
        Category category = ctx.bodyAsClass(Category.class);
        if (categoryService.createCategory(category)) {
            ctx.status(201).json("{\"message\": \"Category created successfully\"}");
        } else {
            ctx.status(400).json("{\"message\": \"Invalid category data\"}");
        }
    }

    public void deleteCategory(Context ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        if (categoryService.deleteCategory(id)) {
            ctx.json("{\"message\": \"Category deleted successfully\"}");
        } else {
            ctx.status(400).json("{\"message\": \"Failed to delete category\"}");
        }
    }
}
