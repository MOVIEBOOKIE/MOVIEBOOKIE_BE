package project.luckybooky.domain.category.repository.init;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import project.luckybooky.domain.category.entity.Category;
import project.luckybooky.domain.category.repository.CategoryRepository;
import project.luckybooky.global.util.DummyDataInit;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@DummyDataInit
public class CategoryInitializer implements ApplicationRunner {
    private final CategoryRepository categoryRepository;

    @Override
    public void run(ApplicationArguments args) {
        if (categoryRepository.count() > 0) {
            log.info("[Category] 더미 데이터 존재");
        } else {
            List<Category> categoryList = new ArrayList<>();

            Category 영화 = Category.builder()
                    .categoryName("영화")
                    .build();

            Category 드라마 = Category.builder()
                    .categoryName("드라마")
                    .build();

            Category 스포츠 = Category.builder()
                    .categoryName("스포츠")
                    .build();

            Category 예능 = Category.builder()
                    .categoryName("예능")
                    .build();

            Category 콘서트 = Category.builder()
                    .categoryName("콘서트")
                    .build();

            Category 그외_이벤트 = Category.builder()
                    .categoryName("기타")
                    .build();

            categoryList.add(영화);
            categoryList.add(드라마);
            categoryList.add(스포츠);
            categoryList.add(예능);
            categoryList.add(콘서트);
            categoryList.add(그외_이벤트);

            categoryRepository.saveAll(categoryList);
        }
    }
}
