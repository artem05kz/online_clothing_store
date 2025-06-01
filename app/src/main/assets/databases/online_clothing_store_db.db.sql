BEGIN TRANSACTION;
CREATE TABLE IF NOT EXISTS "cart" (
	"id"	INTEGER NOT NULL,
	"user_id"	INTEGER NOT NULL,
	"product_id"	INTEGER NOT NULL,
	"quantity"	INTEGER DEFAULT 1,
	PRIMARY KEY("id" AUTOINCREMENT),
	FOREIGN KEY("product_id") REFERENCES "products"("id"),
	FOREIGN KEY("user_id") REFERENCES "users"("id")
);
CREATE TABLE IF NOT EXISTS "categories" (
	"id"	INTEGER NOT NULL,
	"name"	TEXT NOT NULL UNIQUE,
	PRIMARY KEY("id" AUTOINCREMENT)
);
CREATE TABLE IF NOT EXISTS "favorites" (
	"user_id"	INTEGER NOT NULL,
	"product_id"	INTEGER NOT NULL,
	PRIMARY KEY("user_id","product_id"),
	FOREIGN KEY("product_id") REFERENCES "products"("id") ON DELETE CASCADE,
	FOREIGN KEY("user_id") REFERENCES "users"("id") ON DELETE CASCADE
);
CREATE TABLE IF NOT EXISTS "order_items" (
	"order_id"	INTEGER NOT NULL,
	"product_id"	INTEGER NOT NULL,
	"quantity"	INTEGER NOT NULL,
	PRIMARY KEY("order_id","product_id"),
	FOREIGN KEY("order_id") REFERENCES "orders"("id"),
	FOREIGN KEY("product_id") REFERENCES "products"("id")
);
CREATE TABLE IF NOT EXISTS "orders" (
	"id"	INTEGER NOT NULL,
	"user_id"	INTEGER NOT NULL,
	"order_date"	TEXT DEFAULT CURRENT_TIMESTAMP,
	"status"	TEXT DEFAULT 'В обработке',
	PRIMARY KEY("id" AUTOINCREMENT),
	FOREIGN KEY("user_id") REFERENCES "users"("id") ON DELETE CASCADE
);
CREATE TABLE IF NOT EXISTS "product_images" (
	"id"	INTEGER NOT NULL,
	"product_id"	INTEGER NOT NULL,
	"image_url"	TEXT,
	PRIMARY KEY("id" AUTOINCREMENT),
	FOREIGN KEY("product_id") REFERENCES "products"("id") ON DELETE CASCADE
);
CREATE TABLE IF NOT EXISTS "products" (
	"id"	INTEGER NOT NULL,
	"name"	TEXT NOT NULL,
	"description"	TEXT,
	"price"	REAL NOT NULL,
	"category_id"	INTEGER,
	"size"	TEXT,
	"brand"	TEXT,
	"rating"	REAL NOT NULL DEFAULT 0,
	"image_url"	TEXT,
	"composition"	TEXT,
	PRIMARY KEY("id" AUTOINCREMENT),
	FOREIGN KEY("category_id") REFERENCES "categories"("id")
);
CREATE TABLE IF NOT EXISTS "users" (
	"id"	INTEGER NOT NULL,
	"name"	TEXT NOT NULL,
	"email"	TEXT NOT NULL UNIQUE,
	"password_hash"	TEXT NOT NULL,
	"address"	TEXT,
	PRIMARY KEY("id" AUTOINCREMENT)
);
CREATE TABLE IF NOT EXISTS "wardrobe" (
	"id"	INTEGER NOT NULL,
	"user_id"	INTEGER NOT NULL,
	"name"	TEXT NOT NULL,
	PRIMARY KEY("id" AUTOINCREMENT),
	FOREIGN KEY("user_id") REFERENCES "users"("id")
);
CREATE TABLE IF NOT EXISTS "wardrobe_items" (
	"wardrobe_id"	INTEGER NOT NULL,
	"product_id"	INTEGER NOT NULL,
	"type"	TEXT CHECK("type" IN ('HEAD', 'TOP', 'BOTTOM', 'SHOES')),
	PRIMARY KEY("wardrobe_id","product_id"),
	FOREIGN KEY("product_id") REFERENCES "products"("id"),
	FOREIGN KEY("wardrobe_id") REFERENCES "wardrobe"("id")
);
COMMIT;
