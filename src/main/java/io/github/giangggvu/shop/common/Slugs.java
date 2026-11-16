package io.github.giangggvu.shop.common;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

/** Turns Vietnamese names into URL slugs: "Trà Shan Tuyết Hà Giang" becomes "tra-shan-tuyet-ha-giang". */
public final class Slugs {

    private static final Pattern COMBINING_MARKS = Pattern.compile("\\p{M}+");
    private static final Pattern NOT_ALPHANUMERIC = Pattern.compile("[^a-z0-9]+");
    private static final Pattern EDGE_DASHES = Pattern.compile("^-+|-+$");

    private Slugs() {
    }

    public static String of(String text) {
        // "đ" is a separate letter in Unicode, not "d" plus a mark, so it needs its own rule.
        String withoutD = text.replace('đ', 'd').replace('Đ', 'D');
        String ascii = COMBINING_MARKS.matcher(Normalizer.normalize(withoutD, Normalizer.Form.NFD)).replaceAll("");
        String dashed = NOT_ALPHANUMERIC.matcher(ascii.toLowerCase(Locale.ROOT)).replaceAll("-");
        String slug = EDGE_DASHES.matcher(dashed).replaceAll("");
        return slug.isEmpty() ? "item" : slug;
    }
}
