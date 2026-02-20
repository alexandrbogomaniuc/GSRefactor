CREATE OR REPLACE VIEW `${google.cloud.project.id}.${google.cloud.bigquery.dataset.name}.daily_concurrent_players_average` AS
SELECT
      DATE(unique_timestamp) AS day,
      AVG(concurrent_players) AS avg_concurrent_players
    FROM
      `${google.cloud.project.id}.${google.cloud.bigquery.dataset.name}.concurrent_players_counts`
    GROUP BY
      day
    ORDER BY day DESC