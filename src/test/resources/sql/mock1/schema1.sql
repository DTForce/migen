DROP SCHEMA public CASCADE;
CREATE SCHEMA public;

DROP TABLE IF EXISTS  "contract" CASCADE;
CREATE TABLE "contract"
(
  id uuid not null primary key,
  "name" VARCHAR(255),
  "total" NUMERIC(19,2),
  "note" TEXT,
  "type_name" VARCHAR(255)
);

DROP TABLE IF EXISTS  "contract_property" CASCADE;
CREATE TABLE "contract_property"
(
  "contract_id" uuid NOT NULL,
  "bool_val" BOOLEAN,
  "date_val" TIMESTAMP,
  "nbr_val" NUMERIC(19,2),
  "str_val" VARCHAR(255),
  "id_index_str" VARCHAR(255) NOT NULL,
  "property_name" VARCHAR(255) NOT NULL,
  "type_name" VARCHAR(255) NOT NULL,
  PRIMARY KEY ("contract_id", "id_index_str", "property_name", "type_name")
);

DROP TABLE IF EXISTS "contract_type" CASCADE;
CREATE TABLE "contract_type"
(
  "name" varchar(255) not null primary key
);

DROP TABLE IF EXISTS "contract_type_property" CASCADE;
CREATE TABLE "contract_type_property"
(
  "property_name" varchar(255) not null,
  "type_name" varchar(255) not null,
  "property_type" varchar(255),
  "required" boolean,
  PRIMARY KEY ("property_name", "type_name")
);

alter table "contract_property" add constraint "fk18gjrs5xnd3ldx3ws1vsp9xpq" foreign key ("property_name", "type_name") references "contract_type_property";

CREATE INDEX "fk18gjrs5xnd3ldx3ws1vsp9xpq_idx" ON "contract_property" ("property_name", "type_name");

alter table "contract_property" add constraint "fk2pvf6e16hyiajsagm7x1ntda0" foreign key ("contract_id") references "contract";

CREATE INDEX "fk2pvf6e16hyiajsagm7x1ntda0_idx" ON "contract_property" ("contract_id");

alter table "contract_type_property" add constraint "fk394nt19estdds64pjiu658hh2" foreign key ("type_name") references "contract_type";

CREATE INDEX "fk394nt19estdds64pjiu658hh2_idx" ON "contract_type_property" ("type_name");
