CREATE OR REPLACE VIEW `${google.cloud.project.id}.${google.cloud.bigquery.dataset.name}.daily_rounds_per_session` AS
SELECT
    DATE(s_date) AS day,
    Group_Game_Name,
    Room_Type,
    Session_ID,
    COUNT(DISTINCT Round_ID) AS rounds_per_session
  FROM
    `${google.cloud.project.id}.${google.cloud.bigquery.dataset.name}.round_result`
  GROUP BY
    day,
    Group_Game_Name,
    Room_Type,
    Session_ID
  ORDER BY day DESC
