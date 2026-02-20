CREATE OR REPLACE VIEW  `${google.cloud.project.id}.${google.cloud.bigquery.dataset.name}.daily_rounds_duration_average` AS
  SELECT
    day,
    Group_Game_Name,
    Room_Type,
    AVG(round_duration_seconds) AS avg_round_duration_seconds
  FROM
    `${google.cloud.project.id}.${google.cloud.bigquery.dataset.name}.daily_rounds_duration`
  GROUP BY
    day,
    Group_Game_Name,
    Room_Type
  ORDER BY
    day DESC,
    Group_Game_Name;