# Lá & Hạt - Shop API

Backend for a small online tea and coffee shop: a public catalogue, accounts, a cart, checkout with VietQR bank transfer, and an admin area for products and orders.

> **Tiếng Việt:** Máy chủ cho một cửa hàng trà và cà phê trực tuyến, viết bằng Java Spring Boot. Có danh mục sản phẩm, đăng nhập, giỏ hàng, đặt hàng, thanh toán chuyển khoản bằng mã VietQR và trang quản trị. Kiểm thử tự động chạy trên mỗi lần đẩy code.

**Stack:** Java 21 · Spring Boot 3 · Spring Security (JSON Web Token) · Spring Data JPA · PostgreSQL · Flyway · springdoc OpenAPI · JUnit 5 · Docker · GitHub Actions

---

## What it does

| Area | Features |
|---|---|
| Catalogue | Categories; product search by name, category and price range; sort by newest or price; pagination |
| Accounts | Register, log in, stateless token authentication, customer and admin roles |
| Cart | One persistent cart per user; add, change quantity, remove; stock checked on every change |
| Checkout | Turns the cart into an order in one transaction; locks stock rows so the last item cannot be sold twice |
| Payment | Every unpaid order comes with VietQR transfer details and a QR image any Vietnamese banking app can scan |
| Admin | Create, edit and hide products; list orders by status; move orders through `PENDING_PAYMENT → PAID → SHIPPING → COMPLETED`, or cancel |

## Run it

### With Docker (nothing else to install)

```bash
cp .env.example .env           # then set JWT_SECRET and ADMIN_PASSWORD
docker compose up --build
```

Open **http://localhost:8080/swagger-ui.html** to try every endpoint in the browser.

### With Java and Maven

Needs Java 21 and a PostgreSQL database named `shop` (user `shop`, password `shop`, or set `DB_URL`, `DB_USER`, `DB_PASSWORD`).

```bash
docker compose up -d db        # or use your own PostgreSQL
mvn spring-boot:run
```

### Run the tests

```bash
mvn verify
```

Tests run against an in-memory H2 database in PostgreSQL mode, using the same Flyway migrations as production. No database to set up.

## API overview

| Method | Path | Who |
|---|---|---|
| `POST` | `/api/auth/register` | Anyone |
| `POST` | `/api/auth/login` | Anyone |
| `GET` | `/api/me` | Logged in |
| `GET` | `/api/categories` | Anyone |
| `GET` | `/api/products?category=&q=&minPrice=&maxPrice=&sort=&page=&size=` | Anyone |
| `GET` | `/api/products/{slug}` | Anyone |
| `GET` | `/api/cart` | Logged in |
| `POST` | `/api/cart/items` | Logged in |
| `PUT` | `/api/cart/items/{productId}` | Logged in |
| `DELETE` | `/api/cart/items/{productId}` | Logged in |
| `POST` | `/api/orders` | Logged in |
| `GET` | `/api/orders` | Logged in |
| `GET` | `/api/orders/{code}` | Owner |
| `POST` | `/api/orders/{code}/cancel` | Owner, before payment |
| `POST` `PUT` `DELETE` | `/api/admin/products...` | Admin |
| `GET` | `/api/admin/orders?status=` | Admin |
| `PATCH` | `/api/admin/orders/{code}/status` | Admin |

Every error has the same shape ([RFC 9457 problem details](https://www.rfc-editor.org/rfc/rfc9457)), with an `errors` map for invalid fields:

```json
{
  "title": "Validation failed",
  "status": 400,
  "detail": "Some fields are invalid",
  "errors": { "phone": "must be a Vietnamese phone number, such as 0912345678" }
}
```

`requests.http` has a full walkthrough you can run from IntelliJ IDEA or VS Code.

## Design decisions

**Stock is checked twice, and the second check holds a lock.** Adding to the cart gives a friendly early warning. Checkout then locks the product rows (`SELECT … FOR UPDATE`) before checking again, so two people buying the last bag at the same moment cannot both succeed. Rows are always locked in id order, so two checkouts never wait on each other in a circle.

**Checkout never reads products before locking them.** The cart lines are read as plain ids and quantities. Loading the products first would leave stale copies in memory that the locked read would not refresh. `CheckoutFlowTest` covers the case where stock drops between adding to the cart and paying.

**Orders copy the name and price of what was bought.** Changing a product's price later never rewrites an old order or its total.

**Products are hidden, not deleted.** Old orders still point to them.

**Money is a whole number of đồng.** VND has no minor unit, so `long` is exact and avoids decimal rounding.

**The order code is the transfer note.** Codes such as `LH7K2M9QXA` skip look-alike characters (0/O, 1/I), so a shopper can type them into a banking app without mistakes, and the shop can match each incoming transfer to its order.

**Order status changes follow fixed rules.** `OrderStatus` lists which status may follow which, so an order cannot be shipped before it is paid, or reopened after it is cancelled. Cancelling returns the stock.

**No secrets in the code.** The token signing key and the first admin account come from environment variables; the app refuses to start with a signing key shorter than 32 bytes.

## Project layout

```
src/main/java/io/github/giangggvu/shop
├── auth       register, login, token issuing
├── user       the User entity and roles
├── catalog    categories, products, search, admin product management
├── cart       the persistent cart
├── order      checkout, order status rules, VietQR payment details, admin order management
├── config     security, API documentation, first admin account
└── common     errors, paging, slugs
src/main/resources/db/migration   Flyway schema and sample catalogue
```

Each feature keeps its controller, service, repository and response shapes together, so one folder tells the whole story of that feature.

## Next steps

- React storefront
- Confirm payments automatically from the bank's transaction webhook instead of by hand
- Search that ignores Vietnamese accents (`unaccent` in PostgreSQL)
- Product image upload

## License

MIT
