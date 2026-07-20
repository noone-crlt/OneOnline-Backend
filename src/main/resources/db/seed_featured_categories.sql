-- Thêm các thể loại nổi bật vào database
-- Bỏ qua nếu thể loại đã tồn tại (dựa vào cơ chế của NOT EXISTS)

INSERT INTO categories (name)
SELECT 'Văn học' WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Văn học')
UNION ALL
SELECT 'Kinh tế' WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Kinh tế')
UNION ALL
SELECT 'Kỹ năng sống' WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Kỹ năng sống')
UNION ALL
SELECT 'Công nghệ' WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Công nghệ')
UNION ALL
SELECT 'Thiếu nhi' WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Thiếu nhi')
UNION ALL
SELECT 'Trinh thám' WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Trinh thám')
UNION ALL
SELECT 'Tâm lý' WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Tâm lý')
UNION ALL
SELECT 'Ngoại ngữ' WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Ngoại ngữ');
