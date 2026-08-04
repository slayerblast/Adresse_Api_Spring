CREATE TABLE IF NOT EXISTS adresse (
    id TEXT,
    id_fantoir TEXT,
    numero TEXT,
    rep TEXT,
    nom_voie TEXT,
    code_postal TEXT,
    code_insee TEXT,
    nom_commune TEXT,    code_insee_ancienne_commune TEXT,
    nom_ancienne_commune TEXT,
    x REAL,
    y REAL,
    lon REAL,
    lat REAL,
    type_position TEXT,
    alias TEXT,
    nom_ld TEXT,
    libelle_acheminement TEXT,
    nom_afnor TEXT,
    source_position TEXT,
    source_nom_voie TEXT,
    certification_commune INTEGER,
    cad_parcelles TEXT,
    UNIQUE(id,type_position,x,y)

);

CREATE TABLE IF NOT EXISTS adresse_staging (

                                       id TEXT,
                                       id_fantoir TEXT,
                                       numero TEXT,
                                       rep TEXT,
                                       nom_voie TEXT,
                                       code_postal TEXT,
                                       code_insee TEXT,
                                       nom_commune TEXT,
                                       code_insee_ancienne_commune TEXT,
                                       nom_ancienne_commune TEXT,
                                       x REAL,
                                       y REAL,
                                       lon REAL,
                                       lat REAL,
                                       type_position TEXT,
                                       alias TEXT,
                                       nom_ld TEXT,
                                       libelle_acheminement TEXT,
                                       nom_afnor TEXT,
                                       source_position TEXT,
                                       source_nom_voie TEXT,
                                       certification_commune INTEGER,
                                       cad_parcelles TEXT

    );

drop table if exists adresse_fts;

CREATE VIRTUAL TABLE adresse_fts
USING fts5(
    id UNINDEXED,
    x UNINDEXED,
    y UNINDEXED,
    type_position UNINDEXED,
    search_text,
    tokenize = 'unicode61 remove_diacritics 2'
);