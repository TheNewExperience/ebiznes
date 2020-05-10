

CREATE TABLE "category" (
                            "id_category" INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                            "category_name" VARCHAR NOT NULL
);

CREATE TABLE "manufacturer" (
                            "id_manufacturer" INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                            "name_manufacturer" VARCHAR NOT NULL
);


create table "product"
(
    id_product INTEGER not null
        constraint product_pk
            primary key,
    product_name TEXT not null,
    product_description TEXT not null,
    price NUMERIC not null,
    id_category int not null
        constraint category_fk
            references category,
    id_manufacturer int not null
        constraint product_manufacturer_id_fk
            references manufacturer
);

create table "line_items"
(
    id INTEGER not null
        constraint line_items_pk
            primary key autoincrement,
    is_billed INTEGER not null,
    unit_price NUMERIC not null,
    quantity INTEGER not null,
    item_name TEXT not null,
    price NUMERIC not null,
    product_id INTEGER not null
        constraint product_id_fk
            references product,
    bill_id INTEGER not null
        constraint bill_id_fk
            references bills
);



create table "bills"
(
    id_bill INTEGER not null
        constraint bills_pk
            primary key autoincrement,
    name TEXT not null,
    sum_of NUMERIC not null,
    user_id INTEGER
        constraint user_id_fk
            references users,
    created_at TEXT not null,
    is_open INTEGER not null,
    payment_method_id INTEGER not null
        constraint payment_id_fk
            references payment_methods
);

create table "payment_methods"
(
    id_payment_method INTEGER not null
        constraint payment_methods_pk
            primary key,
    name TEXT not null
);

create table "users"
(
    id_user INTEGER not null
        constraint users_pk
            primary key autoincrement,
    email TEXT not null,
    encrypted_password TEXT not null,
    reset_question TEXT not null,
    reset_answer TEXT not null,
    created_at TEXT not null,
    updated_at TEXT not null,
    is_admin INTEGER default 0 not null
);

create table "favorites"
(
    id_favorites INTEGER not null
        constraint favorites_pk
            primary key autoincrement,
    user_id INTEGER not null
        constraint user_id_fk
            references users
);


create table "Box"
(
    box_id INTEGER not null
        constraint Box_pk
            primary key autoincrement
        references payment_methods,
    sumOf NUMERIC not null,
    user_id INTEGER not null
        references users,
    payment_id INTEGER not null
);


create table box_line
(
    id Integer not null
        constraint box_line_pk
            primary key autoincrement,
    unit_price NUMERIC not null,
    quantity INTEGER not null,
    SumOfLine NUMERIC not null,
    product_name TEXT not null,
    product_id INTEGER not null
        constraint product_id_fk
            references product,
    box_id INTEGER not null
        constraint box_id_fk
            references Box
);

create table "favorite_line"
(
    id INTEGER not null
        constraint table_name_pk
            primary key autoincrement,
    product_name TEXT not null,
    product_id INTEGER not null
        constraint product_id_fk
            references product,
    favorite_id INTEGER not null
        constraint favorite_fk
            references favorites
);


DROP TABLE "category";
DROP TABLE "manufacturer";
DROP TABLE "product";
DROP TABLE "line_items";
DROP TABLE "bills";
DROP TABLE "users";
DROP TABLE "payment_methods";
DROP TABLE "favorites";
DROP TABLE "line_items";
DROP TABLE "Box";
DROP TABLE "box_line";