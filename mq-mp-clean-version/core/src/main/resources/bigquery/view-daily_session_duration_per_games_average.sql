CREATE OR REPLACE VIEW `${google.cloud.project.id}.${google.cloud.bigquery.dataset.name}.daily_session_duration_per_games_average` AS
SELECT
    day,
    Group_Game_Name,
    Room_Type,
    AVG(total_session_duration) AS avg_session_duration
  FROM
    `${google.cloud.project.id}.${google.cloud.bigquery.dataset.name}.daily_session_duration`
  GROUP BY
    day,
    Group_Game_Name,
    Room_Type
  ORDER BY day DESC