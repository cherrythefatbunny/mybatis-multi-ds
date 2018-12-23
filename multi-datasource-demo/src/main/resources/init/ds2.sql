CREATE TABLE "public"."person" (
                                       "id" varchar(10) NOT NULL COLLATE "default",
                                       "name" varchar(10) COLLATE "default",
                                       PRIMARY KEY ("id") NOT DEFERRABLE INITIALLY IMMEDIATE
)
  WITH (OIDS=FALSE);
ALTER TABLE "public"."person" OWNER TO "postgres";

insert into person values ('111','name2')