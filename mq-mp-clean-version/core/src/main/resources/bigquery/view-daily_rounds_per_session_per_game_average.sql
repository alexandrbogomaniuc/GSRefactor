CREATE OR REPLACE VIEW `${google.cloud.project.id}.${google.cloud.bigquery.dataset.name}.daily_rounds_per_session_per_game_average` AS
SELECT
    day,
    Group_Game_Name,
    Room_Type,
    AVG(rounds_per_session) AS avg_rounds_per_session
  FROM
    `${google.cloud.project.id}.${google.cloud.bigquery.dataset.name}.daily_rounds_per_session`
  GROUP BY
    day,
    Group_Game_Name,
    Room_Type
  ORDER BY
    day DESC,
    Group_Game_Name