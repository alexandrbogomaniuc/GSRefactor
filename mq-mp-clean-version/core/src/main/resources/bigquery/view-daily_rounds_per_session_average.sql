CREATE OR REPLACE VIEW `${google.cloud.project.id}.${google.cloud.bigquery.dataset.name}.daily_rounds_per_session_average` AS
SELECT
    day,
    AVG(rounds_per_session) AS avg_rounds_per_session
  FROM
    `${google.cloud.project.id}.${google.cloud.bigquery.dataset.name}.daily_rounds_per_session`
  GROUP BY
    day
  ORDER BY
    day DESC