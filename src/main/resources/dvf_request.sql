WITH parametres AS (
                           /*
                            * La date de fin technique correspond au lendemain
                            * de la dernière date valide disponible dans DVF.
                            *
                            * La borne de fin est donc exclusive.
                            * La date maximale est recherchée uniquement parmi
                            * les appartements et les maisons valides.
                            * les mutations dont la nature commence par "Vente".
                            */
                           SELECT
                               MAX(date_mutation::date) + 1 AS date_fin_n
                           FROM dvf
                           WHERE date_mutation IS NOT NULL
                             AND valeur_fonciere IS NOT NULL
                             AND valeur_fonciere > 0.00
                             AND surface_reelle_bati IS NOT NULL
                             AND surface_reelle_bati > 0.00
                             AND type_local IN ('Appartement', 'Maison')
                             AND nature_mutation LIKE 'Vente%'
                       ),

                       periodes AS (
                           /*
                            * Période N :
                            * les 12 derniers mois disponibles.
                            *
                            * Période N-1 :
                            * les 12 mois précédant la période N.
                            */
                           SELECT
                               date_fin_n,

                               (date_fin_n - INTERVAL '12 months')::date
                                   AS date_debut_n,

                               (date_fin_n - INTERVAL '12 months')::date
                                   AS date_fin_n_1,

                               (date_fin_n - INTERVAL '24 months')::date
                                   AS date_debut_n_1

                           FROM parametres
                           WHERE date_fin_n IS NOT NULL
                       ),

                       lignes_valides AS (
                           /*
                            * Une ligne DVF participe aux calculs uniquement si :
                            *
                            * - le code commune est renseigné ;
                            * - l'identifiant de mutation est renseigné ;
                            * - la date de mutation est renseignée ;
                            * - la valeur foncière est renseignée et supérieure à zéro ;
                            * - la surface bâtie est renseignée et supérieure à zéro.
                            * - les mutations dont la nature commence par "Vente".
                            * - la division entre la valeur_fonciere et la surface_reelle_bati doit être entre 200 et 50000
                            */
                           SELECT
                               d.code_commune AS code_insee,
                               d.id_mutation,
                               d.date_mutation::date AS date_mutation,
                               d.valeur_fonciere::numeric AS valeur_fonciere,
                               d.surface_reelle_bati::numeric AS surface_reelle_bati

                           FROM dvf d
                           CROSS JOIN periodes p

                           WHERE d.code_commune IS NOT NULL
                             AND d.id_mutation IS NOT NULL
                             AND d.date_mutation IS NOT NULL

                             AND d.valeur_fonciere IS NOT NULL
                             AND d.valeur_fonciere > 0.00

                             AND d.surface_reelle_bati IS NOT NULL
                             AND d.surface_reelle_bati > 0.00
                             AND d.nature_mutation LIKE 'Vente%'


                         /*
                          * Seuls les appartements et les maisons
                          * participent aux calculs.
                          */
                          AND d.type_local IN ('Appartement', 'Maison')
                             /*
                              * Conservation des lignes appartenant aux deux
                              * périodes étudiées, soit 24 mois au total.
                              */
                             AND d.date_mutation::date >= p.date_debut_n_1
                             AND d.date_mutation::date < p.date_fin_n
                       ),
                       mutations_filtrees AS (
                        SELECT
                            d.code_commune AS code_insee,
                            d.id_mutation,
                            MIN(d.date_mutation)::date AS date_mutation,

                            MAX(d.valeur_fonciere)::numeric AS valeur_fonciere,

                            SUM(
                                COALESCE(d.surface_reelle_bati,0)
                            ) AS surface_totale,

                            SUM(
                                CASE
                                    WHEN d.type_local IN ('Maison','Appartement')
                                    THEN COALESCE(d.surface_reelle_bati,0)
                                    ELSE 0
                                END
                            ) AS surface_residentielle

                        FROM dvf d

                        GROUP BY
                            d.code_commune,
                            d.id_mutation

                        HAVING
                            SUM(COALESCE(d.surface_reelle_bati,0))
                            =
                            SUM(
                                CASE
                                    WHEN d.type_local IN ('Maison','Appartement')
                                    THEN COALESCE(d.surface_reelle_bati,0)
                                    ELSE 0
                                END
                            )
                    ),
                        mutations AS (
                        SELECT
                            code_insee,
                            id_mutation,
                            date_mutation,
                            valeur_fonciere,
                            surface_residentielle AS surface_reelle_bati
                        FROM mutations_filtrees

                        WHERE valeur_fonciere
                              / NULLIF(surface_residentielle,0)
                              BETWEEN 200 AND 50000
                    ),
                       calcul AS (
                           SELECT
                               d.code_insee,

                               /*
                                * Prix moyen de la période N.
                                */
                               ROUND(
                                   (
                                       AVG(d.valeur_fonciere)
                                       FILTER (
                                           WHERE d.date_mutation >= p.date_debut_n
                                             AND d.date_mutation < p.date_fin_n
                                       )
                                   )::numeric,
                                   2
                               ) AS prix_moyen,

                               /*
                                * Prix médian de la période N.
                                */
                               ROUND(
                                   (
                                       PERCENTILE_CONT(0.5)
                                       WITHIN GROUP (
                                           ORDER BY d.valeur_fonciere
                                       )
                                       FILTER (
                                           WHERE d.date_mutation >= p.date_debut_n
                                             AND d.date_mutation < p.date_fin_n
                                       )
                                   )::numeric,
                                   2
                               ) AS prix_median,

                               /*
                                * Prix au m² de la période N :
                                *
                                * SUM(valeur_fonciere)
                                * ---------------------
                                * SUM(surface_reelle_bati)
                                */
                               ROUND(
                                   (
                                       SUM(d.valeur_fonciere)
                                       FILTER (
                                           WHERE d.date_mutation >= p.date_debut_n
                                             AND d.date_mutation < p.date_fin_n
                                       )
                                       /
                                       NULLIF(
                                           SUM(d.surface_reelle_bati)
                                           FILTER (
                                               WHERE d.date_mutation >= p.date_debut_n
                                                 AND d.date_mutation < p.date_fin_n
                                           ),
                                           0
                                       )
                                   )::numeric,
                                   2
                               ) AS prix_m2,

                               /*
                                * Nombre de mutations distinctes de la période N.
                                */
                               COUNT(DISTINCT d.id_mutation)
                               FILTER (
                                   WHERE d.date_mutation >= p.date_debut_n
                                     AND d.date_mutation < p.date_fin_n
                               ) AS nombre_transactions,

                               /*
                                * Prix moyen de la période N-1.
                                */
                               ROUND(
                                   (
                                       AVG(d.valeur_fonciere)
                                       FILTER (
                                           WHERE d.date_mutation >= p.date_debut_n_1
                                             AND d.date_mutation < p.date_fin_n_1
                                       )
                                   )::numeric,
                                   2
                               ) AS prix_moyen_n_1,

                               /*
                                * Prix médian de la période N-1.
                                */
                               ROUND(
                                   (
                                       PERCENTILE_CONT(0.5)
                                       WITHIN GROUP (
                                           ORDER BY d.valeur_fonciere
                                       )
                                       FILTER (
                                           WHERE d.date_mutation >= p.date_debut_n_1
                                             AND d.date_mutation < p.date_fin_n_1
                                       )
                                   )::numeric,
                                   2
                               ) AS prix_median_n_1,

                               /*
                                * Prix au m² de la période N-1 :
                                *
                                * SUM(valeur_fonciere)
                                * ---------------------
                                * SUM(surface_reelle_bati)
                                */
                               ROUND(
                                   (
                                       SUM(d.valeur_fonciere)
                                       FILTER (
                                           WHERE d.date_mutation >= p.date_debut_n_1
                                             AND d.date_mutation < p.date_fin_n_1
                                       )
                                       /
                                       NULLIF(
                                           SUM(d.surface_reelle_bati)
                                           FILTER (
                                               WHERE d.date_mutation >= p.date_debut_n_1
                                                 AND d.date_mutation < p.date_fin_n_1
                                           ),
                                           0
                                       )
                                   )::numeric,
                                   2
                               ) AS prix_m2_n_1,

                               /*
                                * Nombre de mutations distinctes de la période N-1.
                                */
                               COUNT(DISTINCT d.id_mutation)
                               FILTER (
                                   WHERE d.date_mutation >= p.date_debut_n_1
                                     AND d.date_mutation < p.date_fin_n_1
                               ) AS nombre_transactions_n_1,

                               /*
                                * Dates enregistrées dans tarif_commune.
                                *
                                * Les bornes techniques de fin étant exclusives,
                                * un jour est retiré pour enregistrer une fin inclusive.
                                */
                               p.date_debut_n
                                   AS date_debut_periode_n,

                               (p.date_fin_n - 1)
                                   AS date_fin_periode_n,

                               p.date_debut_n_1
                                   AS date_debut_periode_n_1,

                               (p.date_fin_n_1 - 1)
                                   AS date_fin_periode_n_1

                           FROM mutations d
                           CROSS JOIN periodes p

                           GROUP BY
                               d.code_insee,
                               p.date_debut_n,
                               p.date_fin_n,
                               p.date_debut_n_1,
                               p.date_fin_n_1
                       ),

                       variations AS (
                           SELECT
                               code_insee,

                               prix_moyen,
                               prix_median,
                               prix_m2,
                               nombre_transactions,

                               prix_moyen_n_1,
                               prix_median_n_1,
                               prix_m2_n_1,
                               nombre_transactions_n_1,

                               /*
                                * Variation du prix moyen.
                                */
                               ROUND(
                                   (
                                       (
                                           prix_moyen
                                           - prix_moyen_n_1
                                       )
                                       / NULLIF(prix_moyen_n_1, 0)
                                       * 100
                                   )::numeric,
                                   2
                               ) AS variation_prix_moyen_pct,

                               /*
                                * Variation du prix médian.
                                */
                               ROUND(
                                   (
                                       (
                                           prix_median
                                           - prix_median_n_1
                                       )
                                       / NULLIF(prix_median_n_1, 0)
                                       * 100
                                   )::numeric,
                                   2
                               ) AS variation_prix_median_pct,

                               /*
                                * Variation du prix au m².
                                */
                               ROUND(
                                   (
                                       (
                                           prix_m2
                                           - prix_m2_n_1
                                       )
                                       / NULLIF(prix_m2_n_1, 0)
                                       * 100
                                   )::numeric,
                                   2
                               ) AS variation_prix_m2_pct,

                               /*
                                * Variation du nombre de transactions.
                                */
                               ROUND(
                                   (
                                       (
                                           nombre_transactions::numeric
                                           - nombre_transactions_n_1::numeric
                                       )
                                       /
                                       NULLIF(
                                           nombre_transactions_n_1::numeric,
                                           0
                                       )
                                       * 100
                                   )::numeric,
                                   2
                               ) AS variation_nombre_transactions_pct,

                               date_debut_periode_n,
                               date_fin_periode_n,
                               date_debut_periode_n_1,
                               date_fin_periode_n_1

                           FROM calcul
                       )

                       INSERT INTO tarif_commune (
                           code_insee,
                           prix_moyen,
                           prix_median,
                           prix_m2,
                           nombre_transactions,
                           prix_moyen_n_1,
                           prix_median_n_1,
                           prix_m2_n_1,
                           nombre_transactions_n_1,
                           variation_prix_moyen_pct,
                           variation_prix_median_pct,
                           variation_prix_m2_pct,
                           variation_nombre_transactions_pct,
                           date_debut_periode_n,
                           date_fin_periode_n,
                           date_debut_periode_n_1,
                           date_fin_periode_n_1,
                           date_calcul
                       )
                       SELECT
                           code_insee,
                           prix_moyen,
                           prix_median,
                           prix_m2,
                           nombre_transactions,
                           prix_moyen_n_1,
                           prix_median_n_1,
                           prix_m2_n_1,
                           nombre_transactions_n_1,
                           variation_prix_moyen_pct,
                           variation_prix_median_pct,
                           variation_prix_m2_pct,
                           variation_nombre_transactions_pct,
                           date_debut_periode_n,
                           date_fin_periode_n,
                           date_debut_periode_n_1,
                           date_fin_periode_n_1,
                           CURRENT_TIMESTAMP

                       FROM variations
                       ON CONFLICT (code_insee)
                       DO UPDATE SET
                           prix_moyen =EXCLUDED.prix_moyen,
                           prix_median =EXCLUDED.prix_median,
                           prix_m2 =EXCLUDED.prix_m2,
                           nombre_transactions =EXCLUDED.nombre_transactions,
                           prix_moyen_n_1 =EXCLUDED.prix_moyen_n_1,
                           prix_median_n_1 =EXCLUDED.prix_median_n_1,
                           prix_m2_n_1 =EXCLUDED.prix_m2_n_1,
                           nombre_transactions_n_1 =EXCLUDED.nombre_transactions_n_1,
                           variation_prix_moyen_pct =EXCLUDED.variation_prix_moyen_pct,
                           variation_prix_median_pct =EXCLUDED.variation_prix_median_pct,
                           variation_prix_m2_pct =EXCLUDED.variation_prix_m2_pct,
                           variation_nombre_transactions_pct =EXCLUDED.variation_nombre_transactions_pct,
                           date_debut_periode_n =EXCLUDED.date_debut_periode_n,
                           date_fin_periode_n =EXCLUDED.date_fin_periode_n,
                           date_debut_periode_n_1 =EXCLUDED.date_debut_periode_n_1,
                           date_fin_periode_n_1 =EXCLUDED.date_fin_periode_n_1,
                           date_calcul =EXCLUDED.date_calcul;