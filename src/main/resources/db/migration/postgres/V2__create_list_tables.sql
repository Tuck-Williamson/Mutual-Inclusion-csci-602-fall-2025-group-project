CREATE TABLE "list"
(
    list_id    BIGSERIAL PRIMARY KEY,
    title       VARCHAR(50) NOT NULL DEFAULT 'New List',
    created_on TIMESTAMP           NOT NULL DEFAULT now(),
    completed_on TIMESTAMP NULL
);

CREATE TABLE list_item
(
    list_item_id BIGSERIAL PRIMARY KEY,
    list_id BIGINT NOT NULL REFERENCES "list" (list_id) ON DELETE CASCADE,
    list_item_name VARCHAR(50) NOT NULL DEFAULT 'New List Item',
    list_item_desc VARCHAR(255) NULL
);