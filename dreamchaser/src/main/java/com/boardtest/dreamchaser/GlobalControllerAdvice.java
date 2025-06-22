package com.boardtest.dreamchaser;

import com.boardtest.dreamchaser.category.Category;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalControllerAdvice {


    @ModelAttribute("groupedCategories")
    public Map<String, List<Category>> addGroupedCategories() {

        return Arrays.stream(Category.values())
                .collect(Collectors.groupingBy(Category::getMainCategory));
    }
}