INSERT INTO categories (name, slug) VALUES
    ('Cà phê', 'ca-phe'),
    ('Trà', 'tra'),
    ('Dụng cụ pha chế', 'dung-cu-pha-che');

INSERT INTO products (category_id, name, slug, description, price, stock, image_url, active, version, created_at) VALUES
    ((SELECT id FROM categories WHERE slug = 'ca-phe'), 'Cà phê Robusta Đắk Lắk 500g', 'ca-phe-robusta-dak-lak-500g',
     'Rang vừa, đậm và hơi đắng, hợp pha phin.', 135000, 40, NULL, TRUE, 0, CURRENT_TIMESTAMP),
    ((SELECT id FROM categories WHERE slug = 'ca-phe'), 'Cà phê Arabica Cầu Đất 250g', 'ca-phe-arabica-cau-dat-250g',
     'Chua nhẹ, thơm mùi trái cây, hợp pha máy hoặc pha thủ công.', 165000, 25, NULL, TRUE, 0, CURRENT_TIMESTAMP),
    ((SELECT id FROM categories WHERE slug = 'ca-phe'), 'Cà phê Culi rang mộc 500g', 'ca-phe-culi-rang-moc-500g',
     'Hạt tròn, rang mộc không tẩm, vị mạnh.', 150000, 30, NULL, TRUE, 0, CURRENT_TIMESTAMP),
    ((SELECT id FROM categories WHERE slug = 'ca-phe'), 'Cà phê phin giấy hộp 10 gói', 'ca-phe-phin-giay-hop-10-goi',
     'Mỗi gói pha một ly, tiện mang đi làm.', 85000, 60, NULL, TRUE, 0, CURRENT_TIMESTAMP),
    ((SELECT id FROM categories WHERE slug = 'tra'), 'Trà Shan Tuyết Hà Giang 200g', 'tra-shan-tuyet-ha-giang-200g',
     'Búp trà cổ thụ, nước vàng sáng, hậu ngọt.', 220000, 20, NULL, TRUE, 0, CURRENT_TIMESTAMP),
    ((SELECT id FROM categories WHERE slug = 'tra'), 'Trà Thái Nguyên móc câu 200g', 'tra-thai-nguyen-moc-cau-200g',
     'Cánh nhỏ xoăn, vị chát dịu, hương cốm.', 120000, 35, NULL, TRUE, 0, CURRENT_TIMESTAMP),
    ((SELECT id FROM categories WHERE slug = 'tra'), 'Trà sen hộp 100g', 'tra-sen-hop-100g',
     'Trà xanh ướp hương sen, hợp pha ấm nhỏ.', 350000, 10, NULL, TRUE, 0, CURRENT_TIMESTAMP),
    ((SELECT id FROM categories WHERE slug = 'tra'), 'Trà gừng mật ong túi lọc', 'tra-gung-mat-ong-tui-loc',
     'Hộp 20 túi, ấm bụng những ngày lạnh.', 65000, 50, NULL, TRUE, 0, CURRENT_TIMESTAMP),
    ((SELECT id FROM categories WHERE slug = 'dung-cu-pha-che'), 'Phin nhôm cỡ vừa', 'phin-nhom-co-vua',
     'Pha được 1 đến 2 ly.', 45000, 80, NULL, TRUE, 0, CURRENT_TIMESTAMP),
    ((SELECT id FROM categories WHERE slug = 'dung-cu-pha-che'), 'Ấm pha trà gốm 300ml', 'am-pha-tra-gom-300ml',
     'Gốm men lam, giữ nhiệt tốt.', 280000, 12, NULL, TRUE, 0, CURRENT_TIMESTAMP);
