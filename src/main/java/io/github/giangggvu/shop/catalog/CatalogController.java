package io.github.giangggvu.shop.catalog;

import io.github.giangggvu.shop.common.PageView;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Public, read-only catalogue. No login needed. */
@RestController
@RequestMapping("/api")
public class CatalogController {

    private final CatalogService catalog;

    public CatalogController(CatalogService catalog) {
        this.catalog = catalog;
    }

    @GetMapping("/categories")
    public List<CategoryView> categories() {
        return catalog.listCategories();
    }

    @GetMapping("/products")
    public PageView<ProductView> products(
            @RequestParam(name = "category", required = false) String category,
            @RequestParam(name = "q", required = false) String q,
            @RequestParam(name = "minPrice", required = false) Long minPrice,
            @RequestParam(name = "maxPrice", required = false) Long maxPrice,
            @RequestParam(name = "sort", required = false) String sort,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "12") int size) {
        return catalog.searchProducts(new ProductQuery(category, q, minPrice, maxPrice, sort, page, size));
    }

    @GetMapping("/products/{slug}")
    public ProductView product(@PathVariable("slug") String slug) {
        return catalog.getProduct(slug);
    }
}
