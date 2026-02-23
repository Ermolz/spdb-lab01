CREATE TABLE IF NOT EXISTS product (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255),
    price DECIMAL(19, 2)
);

CREATE TABLE IF NOT EXISTS publication (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255),
    dtype VARCHAR(31)
);

CREATE TABLE IF NOT EXISTS book (
    id BIGINT PRIMARY KEY REFERENCES publication(id) ON DELETE CASCADE,
    isbn VARCHAR(255),
    page_count INT
);

CREATE TABLE IF NOT EXISTS article (
    id BIGINT PRIMARY KEY REFERENCES publication(id) ON DELETE CASCADE,
    journal VARCHAR(255),
    volume INT
);

CREATE TABLE sale_item (
    id BIGSERIAL PRIMARY KEY,
    product_name VARCHAR(255) NOT NULL,
    quantity INT NOT NULL
);

INSERT INTO sale_item (product_name, quantity) VALUES ('Widget', 10);
INSERT INTO sale_item (product_name, quantity) VALUES ('Gadget', 5);
INSERT INTO sale_item (product_name, quantity) VALUES ('Widget', 3);
