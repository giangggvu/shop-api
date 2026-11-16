package io.github.giangggvu.shop.catalog;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Only admins reach these routes; the rule lives in SecurityConfig. */
@RestController
@RequestMapping("/api/admin/products")
@SecurityRequirement(name = "bearer")
public class AdminProductController {

    private final CatalogService catalog;

    public AdminProductController(CatalogService catalog) {
        this.catalog = catalog;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductView create(@Valid @RequestBody ProductRequest request) {
        return catalog.createProduct(request);
    }

    @PutMapping("/{id}")
    public ProductView update(@PathVariable("id") Long id, @Valid @RequestBody ProductRequest request) {
        return catalog.updateProduct(id, request);
    }

    /** Hides the product from the shop. Past orders keep their line items. */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivate(@PathVariable("id") Long id) {
        catalog.deactivateProduct(id);
    }
}
