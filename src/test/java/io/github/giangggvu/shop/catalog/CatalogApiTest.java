package io.github.giangggvu.shop.catalog;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import io.github.giangggvu.shop.ApiTestSupport;
import org.junit.jupiter.api.Test;

class CatalogApiTest extends ApiTestSupport {

    @Test
    void listsProductsWithoutLogin() throws Exception {
        mvc.perform(get("/api/products"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items").isArray())
            .andExpect(jsonPath("$.page").value(0))
            .andExpect(jsonPath("$.size").value(12))
            .andExpect(jsonPath("$.totalItems", greaterThanOrEqualTo(10)));
    }

    @Test
    void listsCategoriesInOrder() throws Exception {
        mvc.perform(get("/api/categories"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].slug").value("ca-phe"))
            .andExpect(jsonPath("$[1].slug").value("tra"));
    }

    @Test
    void filtersByCategory() throws Exception {
        mvc.perform(get("/api/products").param("category", "tra").param("size", "50"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items", hasSize(4)))
            .andExpect(jsonPath("$.items[*].category.slug", everyItem(is("tra"))));
    }

    @Test
    void searchesByNameIgnoringCase() throws Exception {
        mvc.perform(get("/api/products").param("q", "ROBUSTA"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items", hasSize(1)))
            .andExpect(jsonPath("$.items[0].name", containsString("Robusta")));
    }

    @Test
    void treatsPercentSignAsText() throws Exception {
        mvc.perform(get("/api/products").param("q", "%"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items", hasSize(0)));
    }

    @Test
    void filtersByPriceRange() throws Exception {
        JsonNode items = body(mvc.perform(get("/api/products")
                .param("minPrice", "100000").param("maxPrice", "200000").param("size", "50"))
            .andExpect(status().isOk())).get("items");
        assertThat(items.size()).isPositive();
        items.forEach(item -> assertThat(item.get("price").asLong()).isBetween(100_000L, 200_000L));
    }

    @Test
    void sortsByPriceAscending() throws Exception {
        JsonNode items = body(mvc.perform(get("/api/products").param("sort", "price_asc").param("size", "50"))
            .andExpect(status().isOk())).get("items");
        long previous = Long.MIN_VALUE;
        for (JsonNode item : items) {
            long price = item.get("price").asLong();
            assertThat(price).isGreaterThanOrEqualTo(previous);
            previous = price;
        }
    }

    @Test
    void rejectsUnknownSort() throws Exception {
        mvc.perform(get("/api/products").param("sort", "cheapest"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.detail", containsString("price_asc")));
    }

    @Test
    void rejectsInvertedPriceRange() throws Exception {
        mvc.perform(get("/api/products").param("minPrice", "200000").param("maxPrice", "100000"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void showsOneProductBySlug() throws Exception {
        mvc.perform(get("/api/products/ca-phe-robusta-dak-lak-500g"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Cà phê Robusta Đắk Lắk 500g"))
            .andExpect(jsonPath("$.category.slug").value("ca-phe"));
    }

    @Test
    void unknownProductIsNotFound() throws Exception {
        mvc.perform(get("/api/products/khong-co-san-pham-nay"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void publishesApiDocumentation() throws Exception {
        mvc.perform(get("/v3/api-docs"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.info.title").value("Lá & Hạt Shop API"));
    }
}
