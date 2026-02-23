CREATE TABLE sale_record (
    id BIGSERIAL PRIMARY KEY,
    product_name VARCHAR(255) NOT NULL,
    quantity INT NOT NULL,
    total_quantity BIGINT NOT NULL
);

INSERT INTO sale_record (product_name, quantity, total_quantity)
SELECT s.product_name, s.quantity, COALESCE(agg.total_quantity, 0)
FROM sale_item s
LEFT JOIN sale_summary agg ON s.product_name = agg.product_name;

DROP TABLE sale_summary;
DROP TABLE sale_item;
