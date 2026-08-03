-- ============================================================================
-- Eco-Guía Dolores Hidalgo: Sistema Multiplataforma (Móvil, Wear OS & Smart TV)
-- Esquema Oficial de Base de Datos PostgreSQL + PostGIS (Neon DB)
-- Versión del Esquema: 2.0 (Producción Limpia)
-- ============================================================================

BEGIN;

-- Extensiones Requeridas
CREATE EXTENSION IF NOT EXISTS postgis;
CREATE EXTENSION IF NOT EXISTS pgcrypto;
CREATE EXTENSION IF NOT EXISTS citext;

-- ============================================================================
-- TIPOS ENUMERADOS (ENUMS)
-- ============================================================================

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'user_role') THEN
        CREATE TYPE user_role AS ENUM (
            'visitor',
            'local_user',
            'admin',
            'moderator',
            'museum_hotel'
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
END $$;

-- ============================================================================
-- FUNCIÓN Y TRIGGER COMÚN (UPDATED_AT)
-- ============================================================================

CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = now();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- ============================================================================
-- 1. USERS (USUARIOS Y PERFILES DE ACCESO)
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
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_users_email ON users (email);
CREATE INDEX IF NOT EXISTS idx_users_role ON users (role);

DROP TRIGGER IF EXISTS trg_users_updated_at ON users;
CREATE TRIGGER trg_users_updated_at
BEFORE UPDATE ON users
FOR EACH ROW
EXECUTE FUNCTION set_updated_at();

-- ============================================================================
-- 2. SITE_CATEGORIES (CATEGORÍAS DE SITIOS HISTÓRICOS Y CULTURALES)
-- ============================================================================

CREATE TABLE IF NOT EXISTS site_categories (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(80) UNIQUE NOT NULL,
    icon VARCHAR(60) NOT NULL DEFAULT 'museum',
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

INSERT INTO site_categories (name, icon) VALUES
    ('Museo', 'museum'),
    ('Monumento Histórico', 'monument'),
    ('Plaza Principal', 'plaza'),
    ('Templo / Iglesia', 'church'),
    ('Sitio Arqueológico', 'archaeological'),
    ('Cultura y Artes', 'culture'),
    ('Gastronomía y Tradición', 'gastronomy'),
    ('Otro', 'other')
ON CONFLICT (name) DO NOTHING;

-- ============================================================================
-- 3. HISTORICAL_SITES (SITIOS HISTÓRICOS Y PATRIMONIALES)
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
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_by UUID REFERENCES users(id) ON DELETE SET NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_historical_sites_location ON historical_sites USING GIST (location);
CREATE INDEX IF NOT EXISTS idx_historical_sites_active ON historical_sites (is_active);
CREATE INDEX IF NOT EXISTS idx_historical_sites_created_by ON historical_sites (created_by);

DROP TRIGGER IF EXISTS trg_historical_sites_updated_at ON historical_sites;
CREATE TRIGGER trg_historical_sites_updated_at
BEFORE UPDATE ON historical_sites
FOR EACH ROW
EXECUTE FUNCTION set_updated_at();

-- ============================================================================
-- 4. GEO_DROPS (CÁPSULAS DE CONTENIDO GEOLOCALIZADO Y AR)
-- ============================================================================

CREATE TABLE IF NOT EXISTS geo_drops (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    site_id UUID REFERENCES historical_sites(id) ON DELETE CASCADE,
    author_id UUID REFERENCES users(id) ON DELETE SET NULL,
    drop_type geo_drop_type NOT NULL DEFAULT 'photo',
    title VARCHAR(180) NOT NULL,
    description TEXT,
    media_url TEXT NOT NULL,
    location GEOGRAPHY(Point, 4326) NOT NULL,
    detection_radius_m INTEGER NOT NULL DEFAULT 20 CHECK (detection_radius_m > 0),
    status content_status NOT NULL DEFAULT 'pending',
    visible_on_tv BOOLEAN NOT NULL DEFAULT TRUE,
    likes_count INTEGER NOT NULL DEFAULT 0,
    reports_count INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_geo_drops_location ON geo_drops USING GIST (location);
CREATE INDEX IF NOT EXISTS idx_geo_drops_site_id ON geo_drops (site_id);
CREATE INDEX IF NOT EXISTS idx_geo_drops_author_id ON geo_drops (author_id);
CREATE INDEX IF NOT EXISTS idx_geo_drops_status ON geo_drops (status);

DROP TRIGGER IF EXISTS trg_geo_drops_updated_at ON geo_drops;
CREATE TRIGGER trg_geo_drops_updated_at
BEFORE UPDATE ON geo_drops
FOR EACH ROW
EXECUTE FUNCTION set_updated_at();

-- ============================================================================
-- 5. USER_SAVED_ITEMS (MI COLECCIÓN / FAVORITOS DEL USUARIO)
-- ============================================================================

CREATE TABLE IF NOT EXISTS user_saved_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    site_id UUID REFERENCES historical_sites(id) ON DELETE CASCADE,
    geo_drop_id UUID REFERENCES geo_drops(id) ON DELETE CASCADE,
    route_id UUID,
    saved_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT chk_user_saved_item_type CHECK (
        (site_id IS NOT NULL AND geo_drop_id IS NULL AND route_id IS NULL) OR
        (site_id IS NULL AND geo_drop_id IS NOT NULL AND route_id IS NULL) OR
        (site_id IS NULL AND geo_drop_id IS NULL AND route_id IS NOT NULL)
    ),
    CONSTRAINT uq_user_site_saved UNIQUE (user_id, site_id),
    CONSTRAINT uq_user_geodrop_saved UNIQUE (user_id, geo_drop_id),
    CONSTRAINT uq_user_route_saved UNIQUE (user_id, route_id)
);

CREATE INDEX IF NOT EXISTS idx_user_saved_items_user ON user_saved_items (user_id);

-- ============================================================================
-- 6. ROUTES & ROUTE_STOPS (RUTAS TURÍSTICAS Y PARADAS)
-- ============================================================================

CREATE TABLE IF NOT EXISTS routes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title VARCHAR(180) NOT NULL,
    description TEXT,
    created_by UUID REFERENCES users(id) ON DELETE SET NULL,
    estimated_duration_min INTEGER DEFAULT 60,
    distance_km NUMERIC(6, 2) DEFAULT 2.50,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS route_stops (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    route_id UUID NOT NULL REFERENCES routes(id) ON DELETE CASCADE,
    site_id UUID NOT NULL REFERENCES historical_sites(id) ON DELETE CASCADE,
    stop_order INTEGER NOT NULL CHECK (stop_order >= 1),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_route_stop_order UNIQUE (route_id, stop_order)
);

CREATE INDEX IF NOT EXISTS idx_route_stops_route_id ON route_stops (route_id);

-- ============================================================================
-- 7. DEVICES & DEVICE_PAIRINGS (DISPOSITIVOS Y VINCULACIÓN SMART TV / WEAR)
-- ============================================================================

CREATE TABLE IF NOT EXISTS devices (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    device_type device_type NOT NULL,
    device_name VARCHAR(120) NOT NULL,
    last_seen_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_devices_user_id ON devices (user_id);

CREATE TABLE IF NOT EXISTS device_pairings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    pairing_code VARCHAR(20) UNIQUE NOT NULL,
    initiator_device_id UUID REFERENCES devices(id) ON DELETE CASCADE,
    target_device_id UUID REFERENCES devices(id) ON DELETE CASCADE,
    status VARCHAR(30) NOT NULL DEFAULT 'pending',
    expires_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_device_pairings_code ON device_pairings (pairing_code);

-- ============================================================================
-- 8. VISTA DE CÁPSULAS APROBADAS (APPROVED_GEO_DROPS)
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
    gd.drop_type AS type,
    gd.media_url,
    gd.location,
    ST_Y(gd.location::geometry)::double precision AS latitude,
    ST_X(gd.location::geometry)::double precision AS longitude,
    gd.detection_radius_m,
    gd.visible_on_tv,
    gd.likes_count,
    gd.reports_count,
    gd.created_at
FROM geo_drops gd
LEFT JOIN historical_sites hs ON hs.id = gd.site_id
LEFT JOIN users u ON u.id = gd.author_id
WHERE gd.status = 'approved';

COMMIT;
