CREATE OR REPLACE VIEW `${google.cloud.project.id}.${google.cloud.bigquery.dataset.name}.daily_rounds_played` AS
SELECT     
    day,
    COUNT(Round_ID) AS Rounds_count,
FROM `${google.cloud.project.id}.${google.cloud.bigquery.dataset.name}.daily_rounds_sums_total`
GROUP BY  
    day
ORDER BY day DESC
