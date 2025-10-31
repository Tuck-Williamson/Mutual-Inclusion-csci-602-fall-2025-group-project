CREATE TABLE list
(
    list_id    BIGINT PRIMARY KEY AUTO_INCREMENT,
    title       VARCHAR(50) NOT NULL DEFAULT 'New List',
    created_on TIMESTAMP           NOT NULL DEFAULT now(),
    completed_on TIMESTAMP NULL
);

CREATE TABLE list_item
(
    list_item_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    list_id BIGINT NOT NULL,
    list_item_name VARCHAR(50) NOT NULL DEFAULT 'New List Item',
    list_item_desc VARCHAR(255) NULL,
    foreign key (list_id) REFERENCES list (list_id) ON DELETE CASCADE
);