package io.github.giangggvu.shop.common;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class SlugsTest {

    @Test
    void removesVietnameseDiacritics() {
        assertThat(Slugs.of("Trà Shan Tuyết Hà Giang 200g")).isEqualTo("tra-shan-tuyet-ha-giang-200g");
        assertThat(Slugs.of("Ấm pha trà gốm")).isEqualTo("am-pha-tra-gom");
        assertThat(Slugs.of("Bưởi Phúc Trạch")).isEqualTo("buoi-phuc-trach");
    }

    @Test
    void handlesTheLetterD() {
        assertThat(Slugs.of("Cà phê Đắk Lắk")).isEqualTo("ca-phe-dak-lak");
        assertThat(Slugs.of("đường phèn")).isEqualTo("duong-phen");
    }

    @Test
    void collapsesPunctuationAndTrimsDashes() {
        assertThat(Slugs.of("  Trà -- sen (hộp)!  ")).isEqualTo("tra-sen-hop");
    }

    @Test
    void neverReturnsAnEmptySlug() {
        assertThat(Slugs.of("!!!")).isEqualTo("item");
    }
}
