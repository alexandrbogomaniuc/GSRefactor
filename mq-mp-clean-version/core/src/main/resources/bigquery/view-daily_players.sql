CREATE OR REPLACE VIEW `${google.cloud.project.id}.${google.cloud.bigquery.dataset.name}.daily_players` AS
SELECT 
    DATE(s_date) AS day,
    player_nickname
FROM 
    `${google.cloud.project.id}.${google.cloud.bigquery.dataset.name}.rooms_players`
GROUP BY
    day,
    player_nickname
ORDER BY day DESC