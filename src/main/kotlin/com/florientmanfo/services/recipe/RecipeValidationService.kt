package com.florientmanfo.com.florientmanfo.services.recipe


import com.florientmanfo.com.florientmanfo.models.recipe.RecipeCommentDTO
import com.florientmanfo.com.florientmanfo.models.recipe.RecipeDTO
import com.florientmanfo.com.florientmanfo.utils.ValidationResult

class RecipeValidationService {
    fun validateRecipe(recipe: RecipeDTO): ValidationResult {
        if (recipe.name.isBlank() || recipe.ingredients.isEmpty() || recipe.instructions.isEmpty()) {
            return ValidationResult(false, "All recipe fields (title, ingredients, steps) must be filled.")
        }
        return ValidationResult(true)
    }

    fun validateComment(commentDTO: RecipeCommentDTO): ValidationResult {
        if (commentDTO.content.isBlank()) {
            return ValidationResult(false, "Comment cannot be empty.")
        }
        return ValidationResult(true)
    }
}