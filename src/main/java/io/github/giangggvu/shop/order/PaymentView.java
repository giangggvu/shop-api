package io.github.giangggvu.shop.order;

/** What the shopper needs to pay by bank transfer: the account, the exact amount, the note to type, and a QR code. */
public record PaymentView(
    String bankId,
    String accountNo,
    String accountName,
    long amount,
    String transferContent,
    String qrImageUrl) {
}
