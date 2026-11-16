package io.github.giangggvu.shop.catalog;

import io.github.giangggvu.shop.common.BadRequestException;
import io.github.giangggvu.shop.common.NotFoundException;
import io.github.giangggvu.shop.common.PageView;
import io.github.giangggvu.shop.common.Paging;
import io.github.giangggvu.shop.common.Slugs;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CatalogService {

    private final CategoryRepository categories;
    private final ProductRepository products;

    public CatalogService(CategoryRepository categories, ProductRepository products) {
        this.categories = categories;
        this.products = products;
    }

    @Transactional(readOnly = true)
    public List<CategoryView> listCategories() {
        return categories.findAllByOrderByIdAsc().stream().map(CategoryView::from).toList();
    }

    @Transactional(readOnly = true)
    public PageView<ProductView> searchProducts(ProductQuery query) {
        validatePriceRange(query.minPrice(), query.maxPrice());

        Specification<Product> spec = Specification.where(ProductSpecs.active());
        if (hasText(query.category())) {
            spec = spec.and(ProductSpecs.inCategory(query.category().trim()));
        }
        if (hasText(query.q())) {
            spec = spec.and(ProductSpecs.nameContains(query.q()));
        }
        if (query.minPrice() != null) {
            spec = spec.and(ProductSpecs.priceAtLeast(query.minPrice()));
        }
        if (query.maxPrice() != null) {
            spec = spec.and(ProductSpecs.priceAtMost(query.maxPrice()));
        }

        Pageable pageable = Paging.of(query.page(), query.size(), ProductSort.parse(query.sort()).sort());
        return PageView.of(products.findAll(spec, pageable).map(ProductView::from));
    }

    @Transactional(readOnly = true)
    public ProductView getProduct(String slug) {
        return products.findBySlugAndActiveTrue(slug)
            .map(ProductView::from)
            .orElseThrow(() -> new NotFoundException("Product '" + slug + "' not found"));
    }

    @Transactional
    public ProductView createProduct(ProductRequest request) {
        Category category = findCategory(request.categoryId());
        Product product = new Product(
            category,
            request.name().trim(),
            uniqueSlug(request.name()),
            request.description(),
            request.price(),
            request.stock(),
            request.imageUrl());
        if (!request.activeOrDefault()) {
            product.deactivate();
        }
        return ProductView.from(products.save(product));
    }

    @Transactional
    public ProductView updateProduct(Long id, ProductRequest request) {
        Product product = findProduct(id);
        product.update(
            findCategory(request.categoryId()),
            request.name().trim(),
            request.description(),
            request.price(),
            request.stock(),
            request.imageUrl(),
            request.activeOrDefault());
        return ProductView.from(product);
    }

    @Transactional
    public void deactivateProduct(Long id) {
        findProduct(id).deactivate();
    }

    private Product findProduct(Long id) {
        return products.findById(id).orElseThrow(() -> new NotFoundException("Product " + id + " not found"));
    }

    private Category findCategory(Long id) {
        return categories.findById(id).orElseThrow(() -> new NotFoundException("Category " + id + " not found"));
    }

    private String uniqueSlug(String name) {
        String base = Slugs.of(name);
        String slug = base;
        int suffix = 2;
        while (products.existsBySlug(slug)) {
            slug = base + "-" + suffix++;
        }
        return slug;
    }

    private static void validatePriceRange(Long min, Long max) {
        if ((min != null && min < 0) || (max != null && max < 0)) {
            throw new BadRequestException("Prices cannot be negative");
        }
        if (min != null && max != null && min > max) {
            throw new BadRequestException("minPrice cannot be greater than maxPrice");
        }
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
