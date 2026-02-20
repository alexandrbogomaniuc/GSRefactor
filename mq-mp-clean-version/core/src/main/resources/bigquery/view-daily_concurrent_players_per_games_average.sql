CREATE OR REPLACE VIEW `${google.cloud.project.id}.${google.cloud.bigquery.dataset.name}.daily_concurrent_players_per_games_average` AS
SELECT
      DATE(unique_timestamp) AS day,
      Group_Game_Name,
      Room_Type,
      AVG(concurrent_players) AS avg_concurrent_players
    FROM
      `${google.cloud.project.id}.${google.cloud.bigquery.dataset.name}.concurrent_players_per_games_counts`
    GROUP BY
      day,
      Group_Game_Name,
      Room_Type
    ORDER BY day DESC