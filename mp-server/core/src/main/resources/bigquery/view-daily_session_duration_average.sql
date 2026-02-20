CREATE OR REPLACE VIEW `${google.cloud.project.id}.${google.cloud.bigquery.dataset.name}.daily_session_duration_average` AS
SELECT
    day,
    AVG(total_session_duration) AS avg_session_duration
  FROM
    `${google.cloud.project.id}.${google.cloud.bigquery.dataset.name}.daily_session_duration`
  GROUP BY
    day
  ORDER BY day DESC