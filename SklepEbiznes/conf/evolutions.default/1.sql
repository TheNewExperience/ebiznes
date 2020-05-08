

CREATE TABLE "category" (
                            "id_category" INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                            "category_name" VARCHAR NOT NULL
);

CREATE TABLE "manufacturer" (
                            "id_manufacturer" INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                            "name_manufacturer" VARCHAR NOT NULL
);


DROP TABLE "category";
DROP TABLE "manufacturer"