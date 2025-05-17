package project.luckybooky.domain.category.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import project.luckybooky.domain.category.entity.Category;
import project.luckybooky.domain.category.repository.CategoryRepository;
import project.luckybooky.global.apiPayload.error.dto.ErrorCode;
import project.luckybooky.global.apiPayload.error.exception.BusinessException;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;

    public Category findOne(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));
    }

    public Category findByName(String categoryName) {
        return categoryRepository.findByCategoryName(categoryName);
    }
}
