CREATE EXTENSION IF NOT EXISTS pg_trgm;
CREATE EXTENSION IF NOT EXISTS unaccent;
CREATE TABLE IF NOT EXISTS adresse (
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
                                       x DOUBLE PRECISION,
                                       y DOUBLE PRECISION,
                                       lon DOUBLE PRECISION,
                                       lat DOUBLE PRECISION,
                                       type_position TEXT,
                                       alias TEXT,
                                       nom_ld TEXT,
                                       libelle_acheminement TEXT,
                                       nom_afnor TEXT,
                                       source_position TEXT,
                                       source_nom_voie TEXT,
                                       certification_commune INTEGER,
                                       cad_parcelles TEXT,
                                       search_text text,
                                       CONSTRAINT uk_adresse UNIQUE (id, type_position, x, y)
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
                                               x DOUBLE PRECISION,
                                               y DOUBLE PRECISION,
                                               lon DOUBLE PRECISION,
                                               lat DOUBLE PRECISION,
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

CREATE TABLE IF NOT EXISTS BATCH_JOB_INSTANCE (
    JOB_INSTANCE_ID BIGINT NOT NULL PRIMARY KEY,
    VERSION BIGINT,
    JOB_NAME VARCHAR(100) NOT NULL,
    JOB_KEY VARCHAR(32) NOT NULL,
    CONSTRAINT JOB_INST_UN UNIQUE (JOB_NAME, JOB_KEY)
);

CREATE TABLE IF NOT EXISTS BATCH_JOB_EXECUTION (
    JOB_EXECUTION_ID BIGINT NOT NULL PRIMARY KEY,
    VERSION BIGINT,
    JOB_INSTANCE_ID BIGINT NOT NULL,
    CREATE_TIME TIMESTAMP NOT NULL,
    START_TIME TIMESTAMP DEFAULT NULL,
    END_TIME TIMESTAMP DEFAULT NULL,
    STATUS VARCHAR(10),
    EXIT_CODE VARCHAR(2500),
    EXIT_MESSAGE VARCHAR(2500),
    LAST_UPDATED TIMESTAMP,
    CONSTRAINT JOB_INST_EXEC_FK
        FOREIGN KEY (JOB_INSTANCE_ID)
        REFERENCES BATCH_JOB_INSTANCE(JOB_INSTANCE_ID)
);

CREATE TABLE IF NOT EXISTS BATCH_JOB_EXECUTION_PARAMS (
    JOB_EXECUTION_ID BIGINT NOT NULL,
    PARAMETER_NAME VARCHAR(100) NOT NULL,
    PARAMETER_TYPE VARCHAR(100) NOT NULL,
    PARAMETER_VALUE VARCHAR(2500),
    IDENTIFYING CHAR(1) NOT NULL,
    CONSTRAINT JOB_EXEC_PARAMS_FK
        FOREIGN KEY (JOB_EXECUTION_ID)
        REFERENCES BATCH_JOB_EXECUTION(JOB_EXECUTION_ID)
);

CREATE TABLE IF NOT EXISTS BATCH_STEP_EXECUTION (
    STEP_EXECUTION_ID BIGINT NOT NULL PRIMARY KEY,
    VERSION BIGINT NOT NULL,
    STEP_NAME VARCHAR(100) NOT NULL,
    JOB_EXECUTION_ID BIGINT NOT NULL,
    CREATE_TIME TIMESTAMP NOT NULL,
    START_TIME TIMESTAMP DEFAULT NULL,
    END_TIME TIMESTAMP DEFAULT NULL,
    STATUS VARCHAR(10),
    COMMIT_COUNT BIGINT,
    READ_COUNT BIGINT,
    FILTER_COUNT BIGINT,
    WRITE_COUNT BIGINT,
    READ_SKIP_COUNT BIGINT,
    WRITE_SKIP_COUNT BIGINT,
    PROCESS_SKIP_COUNT BIGINT,
    ROLLBACK_COUNT BIGINT,
    EXIT_CODE VARCHAR(2500),
    EXIT_MESSAGE VARCHAR(2500),
    LAST_UPDATED TIMESTAMP,
    CONSTRAINT JOB_EXEC_STEP_FK
        FOREIGN KEY (JOB_EXECUTION_ID)
        REFERENCES BATCH_JOB_EXECUTION(JOB_EXECUTION_ID)
);

CREATE TABLE IF NOT EXISTS BATCH_STEP_EXECUTION_CONTEXT (
    STEP_EXECUTION_ID BIGINT NOT NULL PRIMARY KEY,
    SHORT_CONTEXT VARCHAR(2500) NOT NULL,
    SERIALIZED_CONTEXT TEXT,
    CONSTRAINT STEP_EXEC_CTX_FK
        FOREIGN KEY (STEP_EXECUTION_ID)
        REFERENCES BATCH_STEP_EXECUTION(STEP_EXECUTION_ID)
);

CREATE TABLE IF NOT EXISTS BATCH_JOB_EXECUTION_CONTEXT (
    JOB_EXECUTION_ID BIGINT NOT NULL PRIMARY KEY,
    SHORT_CONTEXT VARCHAR(2500) NOT NULL,
    SERIALIZED_CONTEXT TEXT,
    CONSTRAINT JOB_EXEC_CTX_FK
        FOREIGN KEY (JOB_EXECUTION_ID)
        REFERENCES BATCH_JOB_EXECUTION(JOB_EXECUTION_ID)
);

CREATE SEQUENCE IF NOT EXISTS BATCH_STEP_EXECUTION_SEQ
    MAXVALUE 9223372036854775807
    NO CYCLE;

CREATE SEQUENCE IF NOT EXISTS BATCH_JOB_EXECUTION_SEQ
    MAXVALUE 9223372036854775807
    NO CYCLE;

CREATE SEQUENCE IF NOT EXISTS BATCH_JOB_INSTANCE_SEQ
    MAXVALUE 9223372036854775807
    NO CYCLE;
