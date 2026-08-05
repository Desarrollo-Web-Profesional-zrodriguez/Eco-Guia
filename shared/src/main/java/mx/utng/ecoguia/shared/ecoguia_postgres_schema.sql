-- ============================================================================
-- Eco-Guía Dolores Hidalgo: Sistema Multiplataforma (Móvil, Wear OS & Smart TV)
-- Esquema Oficial Exportado de Base de Datos PostgreSQL + PostGIS (Neon DB)
-- Versión del Esquema: 2.1 (Producción Actualizada)
-- ============================================================================

CREATE SCHEMA IF NOT EXISTS "public";

-- ── TIPOS ENUMERADOS (ENUMS) ───────────────────────────────────────────────
CREATE TYPE "user_role" AS ENUM('visitor', 'local_user', 'admin', 'moderator', 'business', 'museum_hotel');
CREATE TYPE "device_type" AS ENUM('phone', 'wear', 'tv');
CREATE TYPE "content_status" AS ENUM('pending', 'approved', 'rejected', 'hidden');
CREATE TYPE "geo_drop_type" AS ENUM('photo', 'text', 'audio', 'video');
CREATE TYPE "report_type" AS ENUM('sensitive_content', 'wrong_location', 'wrong_ai_answer', 'spam', 'other');
CREATE TYPE "moderation_action_type" AS ENUM('approved', 'rejected', 'hidden', 'edited', 'anchor_moved', 'escalated');
CREATE TYPE "proximity_event_type" AS ENUM('site_nearby', 'geo_drop_nearby', 'arrived', 'route_started', 'route_paused', 'route_finished');

-- ── TABLAS ────────────────────────────────────────────────────────────────

CREATE TABLE "users" (
	"id" uuid PRIMARY KEY DEFAULT gen_random_uuid(),
	"email" citext NOT NULL CONSTRAINT "users_email_key" UNIQUE,
	"password_hash" text,
	"display_name" varchar(120) NOT NULL,
	"role" user_role DEFAULT 'visitor' NOT NULL,
	"avatar_url" text,
	"bio" text,
	"is_active" boolean DEFAULT true NOT NULL,
	"created_at" timestamp with time zone DEFAULT now() NOT NULL,
	"updated_at" timestamp with time zone DEFAULT now() NOT NULL
);

CREATE TABLE "site_categories" (
	"id" uuid PRIMARY KEY DEFAULT gen_random_uuid(),
	"name" varchar(50) NOT NULL CONSTRAINT "site_categories_name_key" UNIQUE,
	"icon" varchar(30),
	"is_active" boolean DEFAULT true
);

CREATE TABLE "historical_sites" (
	"id" uuid PRIMARY KEY DEFAULT gen_random_uuid(),
	"name" varchar(180) NOT NULL,
	"slug" varchar(200) NOT NULL CONSTRAINT "historical_sites_slug_key" UNIQUE,
	"site_type" varchar(80) NOT NULL,
	"short_description" text,
	"historical_description" text,
	"address" text,
	"location" geography(Point,4326) NOT NULL,
	"detection_radius_m" integer DEFAULT 50 NOT NULL,
	"opening_hours" jsonb,
	"cost_info" text,
	"accessibility" jsonb,
	"is_active" boolean DEFAULT true NOT NULL,
	"created_by" uuid REFERENCES "users"("id") ON DELETE SET NULL,
	"created_at" timestamp with time zone DEFAULT now() NOT NULL,
	"updated_at" timestamp with time zone DEFAULT now() NOT NULL,
	CONSTRAINT "historical_sites_detection_radius_m_check" CHECK ((detection_radius_m > 0))
);

CREATE TABLE "geo_drops" (
	"id" uuid PRIMARY KEY DEFAULT gen_random_uuid(),
	"site_id" uuid REFERENCES "historical_sites"("id") ON DELETE SET NULL,
	"author_id" uuid REFERENCES "users"("id") ON DELETE SET NULL,
	"title" varchar(180) NOT NULL,
	"description" text,
	"type" geo_drop_type DEFAULT 'photo' NOT NULL,
	"media_url" text,
	"location" geography(Point,4326) NOT NULL,
	"detection_radius_m" integer DEFAULT 20 NOT NULL,
	"status" content_status DEFAULT 'pending' NOT NULL,
	"visible_on_tv" boolean DEFAULT false NOT NULL,
	"likes_count" integer DEFAULT 0 NOT NULL,
	"reports_count" integer DEFAULT 0 NOT NULL,
	"created_at" timestamp with time zone DEFAULT now() NOT NULL,
	"updated_at" timestamp with time zone DEFAULT now() NOT NULL,
	CONSTRAINT "geo_drops_detection_radius_m_check" CHECK ((detection_radius_m > 0)),
	CONSTRAINT "geo_drops_likes_count_check" CHECK ((likes_count >= 0)),
	CONSTRAINT "geo_drops_reports_count_check" CHECK ((reports_count >= 0))
);

CREATE TABLE "devices" (
	"id" uuid PRIMARY KEY DEFAULT gen_random_uuid(),
	"user_id" uuid REFERENCES "users"("id") ON DELETE CASCADE,
	"type" device_type NOT NULL,
	"name" varchar(120),
	"device_identifier" text CONSTRAINT "devices_device_identifier_key" UNIQUE,
	"push_token" text,
	"app_version" varchar(40),
	"os_version" varchar(80),
	"is_active" boolean DEFAULT true NOT NULL,
	"last_seen_at" timestamp with time zone,
	"created_at" timestamp with time zone DEFAULT now() NOT NULL,
	"updated_at" timestamp with time zone DEFAULT now() NOT NULL
);

CREATE TABLE "device_pairings" (
	"id" uuid PRIMARY KEY DEFAULT gen_random_uuid(),
	"user_id" uuid REFERENCES "users"("id") ON DELETE CASCADE,
	"phone_device_id" uuid REFERENCES "devices"("id") ON DELETE CASCADE,
	"paired_device_id" uuid REFERENCES "devices"("id") ON DELETE CASCADE,
	"pairing_code" varchar(20),
	"is_active" boolean DEFAULT true NOT NULL,
	"created_at" timestamp with time zone DEFAULT now() NOT NULL,
	"updated_at" timestamp with time zone DEFAULT now() NOT NULL,
	CONSTRAINT "device_pairings_check" CHECK (((phone_device_id IS NULL) OR (paired_device_id IS NULL) OR (phone_device_id <> paired_device_id)))
);

CREATE TABLE "knowledge_articles" (
	"id" uuid PRIMARY KEY DEFAULT gen_random_uuid(),
	"title" varchar(255) NOT NULL,
	"content" text NOT NULL,
	"author_id" uuid REFERENCES "users"("id") ON DELETE SET NULL,
	"created_at" timestamp with time zone DEFAULT now() NOT NULL,
	"updated_at" timestamp with time zone DEFAULT now() NOT NULL
);

CREATE TABLE "routes" (
	"id" uuid PRIMARY KEY DEFAULT gen_random_uuid(),
	"title" varchar(180) NOT NULL,
	"slug" varchar(200) NOT NULL CONSTRAINT "routes_slug_key" UNIQUE,
	"description" text,
	"estimated_minutes" integer,
	"distance_m" integer,
	"is_active" boolean DEFAULT true NOT NULL,
	"created_by" uuid REFERENCES "users"("id") ON DELETE SET NULL,
	"created_at" timestamp with time zone DEFAULT now() NOT NULL,
	"updated_at" timestamp with time zone DEFAULT now() NOT NULL,
	CONSTRAINT "routes_distance_m_check" CHECK (((distance_m IS NULL) OR (distance_m >= 0))),
	CONSTRAINT "routes_estimated_minutes_check" CHECK (((estimated_minutes IS NULL) OR (estimated_minutes >= 0)))
);

CREATE TABLE "route_stops" (
	"id" uuid PRIMARY KEY DEFAULT gen_random_uuid(),
	"route_id" uuid NOT NULL REFERENCES "routes"("id") ON DELETE CASCADE,
	"site_id" uuid NOT NULL REFERENCES "historical_sites"("id") ON DELETE RESTRICT,
	"stop_order" integer NOT NULL,
	"instruction" text,
	"created_at" timestamp with time zone DEFAULT now() NOT NULL,
	CONSTRAINT "route_stops_route_id_site_id_key" UNIQUE("route_id","site_id"),
	CONSTRAINT "route_stops_route_id_stop_order_key" UNIQUE("route_id","stop_order"),
	CONSTRAINT "route_stops_stop_order_check" CHECK ((stop_order > 0))
);

CREATE TABLE "user_saved_items" (
	"id" uuid PRIMARY KEY DEFAULT gen_random_uuid(),
	"user_id" uuid NOT NULL REFERENCES "users"("id") ON DELETE CASCADE,
	"geo_drop_id" uuid REFERENCES "geo_drops"("id") ON DELETE CASCADE,
	"site_id" uuid REFERENCES "historical_sites"("id") ON DELETE CASCADE,
	"route_id" uuid REFERENCES "routes"("id") ON DELETE CASCADE,
	"created_at" timestamp with time zone DEFAULT now() NOT NULL
);

-- ── ÍNDICES DE RENDIMIENTO Y GEOLOCALIZACIÓN ─────────────────────────────

CREATE INDEX IF NOT EXISTS "idx_device_pairings_active" ON "device_pairings" ("is_active");
CREATE INDEX IF NOT EXISTS "idx_device_pairings_user_id" ON "device_pairings" ("user_id");

CREATE INDEX IF NOT EXISTS "idx_devices_type" ON "devices" ("type");
CREATE INDEX IF NOT EXISTS "idx_devices_user_id" ON "devices" ("user_id");

CREATE INDEX IF NOT EXISTS "idx_geo_drops_author_id" ON "geo_drops" ("author_id");
CREATE INDEX IF NOT EXISTS "idx_geo_drops_location" ON "geo_drops" USING gist ("location");
CREATE INDEX IF NOT EXISTS "idx_geo_drops_site_id" ON "geo_drops" ("site_id");
CREATE INDEX IF NOT EXISTS "idx_geo_drops_status" ON "geo_drops" ("status");
CREATE INDEX IF NOT EXISTS "idx_geo_drops_visible_on_tv" ON "geo_drops" ("visible_on_tv");

CREATE INDEX IF NOT EXISTS "idx_historical_sites_active" ON "historical_sites" ("is_active");
CREATE INDEX IF NOT EXISTS "idx_historical_sites_location" ON "historical_sites" USING gist ("location");
CREATE INDEX IF NOT EXISTS "idx_historical_sites_type" ON "historical_sites" ("site_type");

CREATE INDEX IF NOT EXISTS "idx_knowledge_articles_created" ON "knowledge_articles" ("created_at");

CREATE INDEX IF NOT EXISTS "idx_route_stops_route_id" ON "route_stops" ("route_id");
CREATE INDEX IF NOT EXISTS "idx_route_stops_site_id" ON "route_stops" ("site_id");

CREATE INDEX IF NOT EXISTS "idx_routes_active" ON "routes" ("is_active");

CREATE UNIQUE INDEX IF NOT EXISTS "idx_user_saved_geo_drop_unique" ON "user_saved_items" ("user_id","geo_drop_id");
CREATE UNIQUE INDEX IF NOT EXISTS "idx_user_saved_site_unique" ON "user_saved_items" ("user_id","site_id");

-- ── VISTAS POSTGRESQL ──────────────────────────────────────────────────────

CREATE OR REPLACE VIEW "approved_geo_drops" AS 
(
    SELECT 
        gd.id, 
        gd.site_id, 
        hs.name AS site_name, 
        gd.author_id, 
        u.display_name AS author_name, 
        gd.title, 
        gd.description, 
        gd.type, 
        gd.media_url, 
        gd.location, 
        gd.detection_radius_m, 
        gd.visible_on_tv, 
        gd.likes_count, 
        gd.reports_count, 
        gd.created_at 
    FROM geo_drops gd 
    LEFT JOIN historical_sites hs ON hs.id = gd.site_id 
    LEFT JOIN users u ON u.id = gd.author_id 
    WHERE gd.status = 'approved'::content_status
);
