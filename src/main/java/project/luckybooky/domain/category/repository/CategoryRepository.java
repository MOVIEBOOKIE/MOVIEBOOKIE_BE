package project.luckybooky.domain.category.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import project.luckybooky.domain.category.entity.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    Category findByCategoryName(String categoryName);
}
