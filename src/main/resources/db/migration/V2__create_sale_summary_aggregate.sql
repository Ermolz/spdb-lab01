CREATE TABLE sale_summary (
    id BIGSERIAL PRIMARY KEY,
    product_name VARCHAR(255) NOT NULL,
    total_quantity BIGINT NOT NULL
);

INSERT INTO sale_summary (product_name, total_quantity)
SELECT product_name, SUM(quantity)::BIGINT
FROM sale_item
GROUP BY product_name;
