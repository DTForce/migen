DROP SCHEMA public CASCADE;
CREATE SCHEMA public;

CREATE EXTENSION IF NOT EXISTS ltree;

CREATE TABLE "client"
(
  "id" VARCHAR(255) NOT NULL,
  "path" LTREE NOT NULL,
  "description" TEXT,
  PRIMARY KEY ("id")
);

CREATE TABLE "client_authorities"
(
  "client_id" VARCHAR(255) NOT NULL,
  "authorities" VARCHAR(255)
);

CREATE UNIQUE INDEX "client_path_unq" ON "client"("path");

CREATE INDEX "fk9gbpak3a5886rp59ly8efavha_idx" ON "client_authorities" ("client_id");

ALTER TABLE "client_authorities"
  ADD CONSTRAINT "fk9gbpak3a5886rp59ly8efavha" FOREIGN KEY ("client_id") REFERENCES "client" ("id");

