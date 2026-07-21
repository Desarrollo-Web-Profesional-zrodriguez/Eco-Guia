-- Eco-Guia Dolores: Ruta & Capsulas
-- PostgreSQL + PostGIS initial schema
-- Recommended PostgreSQL version: 15+

BEGIN;

-- Required extensions
CREATE EXTENSION IF NOT EXISTS postgis;
CREATE EXTENSION IF NOT EXISTS pgcrypto;
CREATE EXTENSION IF NOT EXISTS citext;

-- Optional extension for future semantic search with AI embeddings.
-- Enable only if your PostgreSQL provider supports pgvector.
-- CREATE EXTENSION IF NOT EXISTS vector;

-- ============================================================================
-- ENUM TYPES
-- ============================================================================

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'user_role') THEN
        CREATE TYPE user_role AS ENUM (
            'visitor',
            'local_user',
            'admin',
            'moderator',
            'business'
        );
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'device_type') THEN
        CREATE TYPE device_type AS ENUM (
            'phone',
            'wear',
            'tv'
        );
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'content_status') THEN
        CREATE TYPE content_status AS ENUM (
            'pending',
            'approved',
            'rejected',
            'hidden'
        );
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'geo_drop_type') THEN
        CREATE TYPE geo_drop_type AS ENUM (
            'photo',
            'text',
            'audio',
            'video'
        );
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'report_type') THEN
        CREATE TYPE report_type AS ENUM (
            'sensitive_content',
            'wrong_location',
            'wrong_ai_answer',
            'spam',
            'other'
        );
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'moderation_action_type') THEN
        CREATE TYPE moderation_action_type AS ENUM (
            'approved',
            'rejected',
            'hidden',
            'edited',
            'anchor_moved',
            'escalated'
        );
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'proximity_event_type') THEN
        CREATE TYPE proximity_event_type AS ENUM (
            'site_nearby',
            'geo_drop_nearby',
            'arrived',
            'route_started',
            'route_paused',
            'route_finished'
        );
    END IF;
END $$;

-- ============================================================================
-- COMMON TRIGGER
-- ============================================================================

CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = now();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- ============================================================================
-- USERS AND AUTH PROFILE
-- ============================================================================

CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email CITEXT UNIQUE NOT NULL,
    password_hash TEXT,
    display_name VARCHAR(120) NOT NULL,
    role user_role NOT NULL DEFAULT 'visitor',
    avatar_url TEXT,
    bio TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    last_login_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

DROP TRIGGER IF EXISTS trg_users_updated_at ON users;
CREATE TRIGGER trg_users_updated_at
BEFORE UPDATE ON users
FOR EACH ROW
EXECUTE FUNCTION set_updated_at();

-- ============================================================================
-- HISTORICAL SITES / MUSEUMS
-- ============================================================================

CREATE TABLE IF NOT EXISTS historical_sites (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(180) NOT NULL,
    slug VARCHAR(200) UNIQUE NOT NULL,
    site_type VARCHAR(80) NOT NULL,
    short_description TEXT,
    historical_description TEXT,
    address TEXT,
    location GEOGRAPHY(Point, 4326) NOT NULL,
    detection_radius_m INTEGER NOT NULL DEFAULT 50 CHECK (detection_radius_m > 0),
    opening_hours JSONB,
    cost_info TEXT,
    accessibility JSONB,
    phone VARCHAR(40),
    website TEXT,
    chatbot_context TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_by UUID REFERENCES users(id) ON DELETE SET NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_historical_sites_location
ON historical_sites
USING GIST (location);

CREATE INDEX IF NOT EXISTS idx_historical_sites_active
ON historical_sites (is_active);

CREATE INDEX IF NOT EXISTS idx_historical_sites_type
ON historical_sites (site_type);

DROP TRIGGER IF EXISTS trg_historical_sites_updated_at ON historical_sites;
CREATE TRIGGER trg_historical_sites_updated_at
BEFORE UPDATE ON historical_sites
FOR EACH ROW
EXECUTE FUNCTION set_updated_at();

CREATE TABLE IF NOT EXISTS site_media (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    site_id UUID NOT NULL REFERENCES historical_sites(id) ON DELETE CASCADE,
    media_url TEXT NOT NULL,
    media_type VARCHAR(40) NOT NULL,
    title VARCHAR(160),
    alt_text TEXT,
    sort_order INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_site_media_site_id
ON site_media (site_id);

-- ============================================================================
-- TOURIST ROUTES
-- ============================================================================

CREATE TABLE IF NOT EXISTS routes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title VARCHAR(180) NOT NULL,
    slug VARCHAR(200) UNIQUE NOT NULL,
    description TEXT,
    estimated_minutes INTEGER CHECK (estimated_minutes IS NULL OR estimated_minutes >= 0),
    distance_m INTEGER CHECK (distance_m IS NULL OR distance_m >= 0),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_by UUID REFERENCES users(id) ON DELETE SET NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_routes_active
ON routes (is_active);

DROP TRIGGER IF EXISTS trg_routes_updated_at ON routes;
CREATE TRIGGER trg_routes_updated_at
BEFORE UPDATE ON routes
FOR EACH ROW
EXECUTE FUNCTION set_updated_at();

CREATE TABLE IF NOT EXISTS route_stops (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    route_id UUID NOT NULL REFERENCES routes(id) ON DELETE CASCADE,
    site_id UUID NOT NULL REFERENCES historical_sites(id) ON DELETE RESTRICT,
    stop_order INTEGER NOT NULL CHECK (stop_order > 0),
    instruction TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (route_id, stop_order),
    UNIQUE (route_id, site_id)
);

CREATE INDEX IF NOT EXISTS idx_route_stops_route_id
ON route_stops (route_id);

CREATE INDEX IF NOT EXISTS idx_route_stops_site_id
ON route_stops (site_id);

-- ============================================================================
-- GEO-DROPS
-- ============================================================================

CREATE TABLE IF NOT EXISTS geo_drops (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    site_id UUID REFERENCES historical_sites(id) ON DELETE SET NULL,
    author_id UUID REFERENCES users(id) ON DELETE SET NULL,
    title VARCHAR(180) NOT NULL,
    description TEXT,
    type geo_drop_type NOT NULL DEFAULT 'photo',
    media_url TEXT,
    location GEOGRAPHY(Point, 4326) NOT NULL,
    detection_radius_m INTEGER NOT NULL DEFAULT 20 CHECK (detection_radius_m > 0),
    status content_status NOT NULL DEFAULT 'pending',
    visible_on_tv BOOLEAN NOT NULL DEFAULT FALSE,
    likes_count INTEGER NOT NULL DEFAULT 0 CHECK (likes_count >= 0),
    reports_count INTEGER NOT NULL DEFAULT 0 CHECK (reports_count >= 0),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_geo_drops_location
ON geo_drops
USING GIST (location);

CREATE INDEX IF NOT EXISTS idx_geo_drops_status
ON geo_drops (status);

CREATE INDEX IF NOT EXISTS idx_geo_drops_site_id
ON geo_drops (site_id);

CREATE INDEX IF NOT EXISTS idx_geo_drops_author_id
ON geo_drops (author_id);

CREATE INDEX IF NOT EXISTS idx_geo_drops_visible_on_tv
ON geo_drops (visible_on_tv)
WHERE visible_on_tv = TRUE;

DROP TRIGGER IF EXISTS trg_geo_drops_updated_at ON geo_drops;
CREATE TRIGGER trg_geo_drops_updated_at
BEFORE UPDATE ON geo_drops
FOR EACH ROW
EXECUTE FUNCTION set_updated_at();

-- ============================================================================
-- USER COLLECTION AND LIKES
-- ============================================================================

CREATE TABLE IF NOT EXISTS user_saved_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    geo_drop_id UUID REFERENCES geo_drops(id) ON DELETE CASCADE,
    site_id UUID REFERENCES historical_sites(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CHECK (geo_drop_id IS NOT NULL OR site_id IS NOT NULL),
    CHECK (NOT (geo_drop_id IS NOT NULL AND site_id IS NOT NULL))
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_user_saved_geo_drop_unique
ON user_saved_items (user_id, geo_drop_id)
WHERE geo_drop_id IS NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS idx_user_saved_site_unique
ON user_saved_items (user_id, site_id)
WHERE site_id IS NOT NULL;

CREATE TABLE IF NOT EXISTS geo_drop_likes (
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    geo_drop_id UUID NOT NULL REFERENCES geo_drops(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (user_id, geo_drop_id)
);

CREATE INDEX IF NOT EXISTS idx_geo_drop_likes_geo_drop_id
ON geo_drop_likes (geo_drop_id);

-- ============================================================================
-- MODERATION AND REPORTS
-- ============================================================================

CREATE TABLE IF NOT EXISTS reports (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    reporter_id UUID REFERENCES users(id) ON DELETE SET NULL,
    geo_drop_id UUID NOT NULL REFERENCES geo_drops(id) ON DELETE CASCADE,
    report_type report_type NOT NULL,
    description TEXT,
    status VARCHAR(40) NOT NULL DEFAULT 'open',
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    resolved_at TIMESTAMPTZ
);

CREATE INDEX IF NOT EXISTS idx_reports_geo_drop_id
ON reports (geo_drop_id);

CREATE INDEX IF NOT EXISTS idx_reports_status
ON reports (status);

CREATE TABLE IF NOT EXISTS moderation_actions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    moderator_id UUID REFERENCES users(id) ON DELETE SET NULL,
    geo_drop_id UUID REFERENCES geo_drops(id) ON DELETE CASCADE,
    report_id UUID REFERENCES reports(id) ON DELETE SET NULL,
    action moderation_action_type NOT NULL,
    notes TEXT,
    old_location GEOGRAPHY(Point, 4326),
    new_location GEOGRAPHY(Point, 4326),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_moderation_actions_geo_drop_id
ON moderation_actions (geo_drop_id);

CREATE INDEX IF NOT EXISTS idx_moderation_actions_moderator_id
ON moderation_actions (moderator_id);

-- ============================================================================
-- DEVICES: PHONE / WEAR / SMART TV
-- ============================================================================

CREATE TABLE IF NOT EXISTS devices (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    type device_type NOT NULL,
    name VARCHAR(120),
    device_identifier TEXT UNIQUE,
    push_token TEXT,
    app_version VARCHAR(40),
    os_version VARCHAR(80),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    last_seen_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_devices_user_id
ON devices (user_id);

CREATE INDEX IF NOT EXISTS idx_devices_type
ON devices (type);

DROP TRIGGER IF EXISTS trg_devices_updated_at ON devices;
CREATE TRIGGER trg_devices_updated_at
BEFORE UPDATE ON devices
FOR EACH ROW
EXECUTE FUNCTION set_updated_at();

CREATE TABLE IF NOT EXISTS device_pairings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    phone_device_id UUID REFERENCES devices(id) ON DELETE CASCADE,
    paired_device_id UUID REFERENCES devices(id) ON DELETE CASCADE,
    pairing_code VARCHAR(20),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CHECK (phone_device_id IS DISTINCT FROM paired_device_id)
);

CREATE INDEX IF NOT EXISTS idx_device_pairings_user_id
ON device_pairings (user_id);

CREATE INDEX IF NOT EXISTS idx_device_pairings_active
ON device_pairings (is_active);

DROP TRIGGER IF EXISTS trg_device_pairings_updated_at ON device_pairings;
CREATE TRIGGER trg_device_pairings_updated_at
BEFORE UPDATE ON device_pairings
FOR EACH ROW
EXECUTE FUNCTION set_updated_at();

-- ============================================================================
-- WEAR / LOCATION EVENTS
-- ============================================================================

CREATE TABLE IF NOT EXISTS proximity_events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    device_id UUID REFERENCES devices(id) ON DELETE SET NULL,
    site_id UUID REFERENCES historical_sites(id) ON DELETE SET NULL,
    geo_drop_id UUID REFERENCES geo_drops(id) ON DELETE SET NULL,
    distance_m INTEGER CHECK (distance_m IS NULL OR distance_m >= 0),
    event_type proximity_event_type NOT NULL,
    metadata JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_proximity_events_user_created
ON proximity_events (user_id, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_proximity_events_device_created
ON proximity_events (device_id, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_proximity_events_type
ON proximity_events (event_type);

-- ============================================================================
-- CHATBOT KNOWLEDGE BASE
-- ============================================================================

CREATE TABLE IF NOT EXISTS knowledge_articles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    site_id UUID REFERENCES historical_sites(id) ON DELETE SET NULL,
    title VARCHAR(180) NOT NULL,
    content TEXT NOT NULL,
    source_url TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_by UUID REFERENCES users(id) ON DELETE SET NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_knowledge_articles_site_id
ON knowledge_articles (site_id);

CREATE INDEX IF NOT EXISTS idx_knowledge_articles_active
ON knowledge_articles (is_active);

DROP TRIGGER IF EXISTS trg_knowledge_articles_updated_at ON knowledge_articles;
CREATE TRIGGER trg_knowledge_articles_updated_at
BEFORE UPDATE ON knowledge_articles
FOR EACH ROW
EXECUTE FUNCTION set_updated_at();

CREATE TABLE IF NOT EXISTS chatbot_conversations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES users(id) ON DELETE SET NULL,
    site_id UUID REFERENCES historical_sites(id) ON DELETE SET NULL,
    title VARCHAR(180),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_chatbot_conversations_user_id
ON chatbot_conversations (user_id);

CREATE INDEX IF NOT EXISTS idx_chatbot_conversations_site_id
ON chatbot_conversations (site_id);

CREATE TABLE IF NOT EXISTS chatbot_messages (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    conversation_id UUID NOT NULL REFERENCES chatbot_conversations(id) ON DELETE CASCADE,
    sender VARCHAR(20) NOT NULL CHECK (sender IN ('user', 'assistant', 'system')),
    message TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_chatbot_messages_conversation_created
ON chatbot_messages (conversation_id, created_at ASC);

-- Future semantic search table if pgvector is enabled.
-- CREATE TABLE IF NOT EXISTS knowledge_embeddings (
--     id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
--     article_id UUID NOT NULL REFERENCES knowledge_articles(id) ON DELETE CASCADE,
--     chunk_text TEXT NOT NULL,
--     embedding vector(1536),
--     created_at TIMESTAMPTZ NOT NULL DEFAULT now()
-- );

-- ============================================================================
-- SMART TV
-- ============================================================================

CREATE TABLE IF NOT EXISTS tv_displays (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    device_id UUID REFERENCES devices(id) ON DELETE CASCADE,
    name VARCHAR(160) NOT NULL,
    location_name VARCHAR(180),
    mode VARCHAR(60) NOT NULL DEFAULT 'lobby',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_tv_displays_device_id
ON tv_displays (device_id);

CREATE INDEX IF NOT EXISTS idx_tv_displays_active
ON tv_displays (is_active);

DROP TRIGGER IF EXISTS trg_tv_displays_updated_at ON tv_displays;
CREATE TRIGGER trg_tv_displays_updated_at
BEFORE UPDATE ON tv_displays
FOR EACH ROW
EXECUTE FUNCTION set_updated_at();

CREATE TABLE IF NOT EXISTS tv_featured_geo_drops (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tv_display_id UUID REFERENCES tv_displays(id) ON DELETE CASCADE,
    geo_drop_id UUID REFERENCES geo_drops(id) ON DELETE CASCADE,
    sort_order INTEGER NOT NULL DEFAULT 0,
    active_from TIMESTAMPTZ,
    active_until TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_tv_featured_geo_drops_display_id
ON tv_featured_geo_drops (tv_display_id);

CREATE INDEX IF NOT EXISTS idx_tv_featured_geo_drops_active_window
ON tv_featured_geo_drops (active_from, active_until);

-- ============================================================================
-- ANALYTICS / HEATMAP EVENTS
-- ============================================================================

CREATE TABLE IF NOT EXISTS exploration_events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES users(id) ON DELETE SET NULL,
    device_id UUID REFERENCES devices(id) ON DELETE SET NULL,
    event_name VARCHAR(80) NOT NULL,
    location GEOGRAPHY(Point, 4326),
    site_id UUID REFERENCES historical_sites(id) ON DELETE SET NULL,
    geo_drop_id UUID REFERENCES geo_drops(id) ON DELETE SET NULL,
    metadata JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_exploration_events_location
ON exploration_events
USING GIST (location);

CREATE INDEX IF NOT EXISTS idx_exploration_events_created_at
ON exploration_events (created_at DESC);

CREATE INDEX IF NOT EXISTS idx_exploration_events_name
ON exploration_events (event_name);

-- ============================================================================
-- HELPER FUNCTIONS
-- ============================================================================

CREATE OR REPLACE FUNCTION increment_geo_drop_like_count()
RETURNS TRIGGER AS $$
BEGIN
    UPDATE geo_drops
    SET likes_count = likes_count + 1
    WHERE id = NEW.geo_drop_id;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION decrement_geo_drop_like_count()
RETURNS TRIGGER AS $$
BEGIN
    UPDATE geo_drops
    SET likes_count = GREATEST(likes_count - 1, 0)
    WHERE id = OLD.geo_drop_id;
    RETURN OLD;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_geo_drop_likes_insert ON geo_drop_likes;
CREATE TRIGGER trg_geo_drop_likes_insert
AFTER INSERT ON geo_drop_likes
FOR EACH ROW
EXECUTE FUNCTION increment_geo_drop_like_count();

DROP TRIGGER IF EXISTS trg_geo_drop_likes_delete ON geo_drop_likes;
CREATE TRIGGER trg_geo_drop_likes_delete
AFTER DELETE ON geo_drop_likes
FOR EACH ROW
EXECUTE FUNCTION decrement_geo_drop_like_count();

CREATE OR REPLACE FUNCTION increment_geo_drop_report_count()
RETURNS TRIGGER AS $$
BEGIN
    UPDATE geo_drops
    SET reports_count = reports_count + 1
    WHERE id = NEW.geo_drop_id;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_reports_insert ON reports;
CREATE TRIGGER trg_reports_insert
AFTER INSERT ON reports
FOR EACH ROW
EXECUTE FUNCTION increment_geo_drop_report_count();

-- ============================================================================
-- USEFUL VIEWS
-- ============================================================================

CREATE OR REPLACE VIEW approved_geo_drops AS
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
WHERE gd.status = 'approved';

-- ============================================================================
-- OPTIONAL SEED DATA
-- ============================================================================

INSERT INTO historical_sites (
    name,
    slug,
    site_type,
    short_description,
    historical_description,
    address,
    location,
    detection_radius_m,
    opening_hours,
    cost_info,
    accessibility,
    chatbot_context
)
VALUES (
    'Museo de la Independencia Nacional',
    'museo-de-la-independencia-nacional',
    'museum',
    'Sitio historico clave para entender el inicio de la Independencia de Mexico.',
    'Museo ubicado en Dolores Hidalgo, Guanajuato, relacionado con la memoria historica del movimiento independentista.',
    'Dolores Hidalgo Cuna de la Independencia Nacional, Guanajuato',
    ST_SetSRID(ST_MakePoint(-100.9350, 21.1561), 4326)::geography,
    50,
    '{"mon_sun": "Consultar horarios oficiales"}'::jsonb,
    'Consultar costo oficial vigente',
    '{"wheelchair_access": "unknown", "notes": "Validar informacion actualizada"}'::jsonb,
    'Responder preguntas sobre el museo, Dolores Hidalgo y la Independencia de Mexico con tono cultural e institucional.'
)
ON CONFLICT (slug) DO NOTHING;

-- ============================================================================
-- EXAMPLE QUERIES
-- ============================================================================

-- Nearby approved Geo-Drops within 100 meters:
--
-- SELECT
--     id,
--     title,
--     description,
--     ST_Distance(
--         location,
--         ST_SetSRID(ST_MakePoint(:lng, :lat), 4326)::geography
--     ) AS distance_m
-- FROM geo_drops
-- WHERE status = 'approved'
-- AND ST_DWithin(
--     location,
--     ST_SetSRID(ST_MakePoint(:lng, :lat), 4326)::geography,
--     100
-- )
-- ORDER BY distance_m ASC;

-- Nearby historical sites within 250 meters:
--
-- SELECT
--     id,
--     name,
--     short_description,
--     ST_Distance(
--         location,
--         ST_SetSRID(ST_MakePoint(:lng, :lat), 4326)::geography
--     ) AS distance_m
-- FROM historical_sites
-- WHERE is_active = TRUE
-- AND ST_DWithin(
--     location,
--     ST_SetSRID(ST_MakePoint(:lng, :lat), 4326)::geography,
--     250
-- )
-- ORDER BY distance_m ASC;

-- Wear payload suggestion:
--
-- The phone app should query nearby sites and Geo-Drops, then send the watch:
-- {
--   "target_id": "...",
--   "target_type": "geo_drop",
--   "title": "Geo-Drop Museo",
--   "subtitle": "Museo de la Independencia Nacional",
--   "distance_m": 18,
--   "bearing_degrees": 34,
--   "event": "geo_drop_nearby"
-- }

COMMIT;
