CREATE OR REPLACE VIEW `${google.cloud.project.id}.${google.cloud.bigquery.dataset.name}.daily_players_count` AS
SELECT 
    day, 
    COUNT(DISTINCT player_nickname) AS player_count
FROM 
    `${google.cloud.project.id}.${google.cloud.bigquery.dataset.name}.daily_players`
GROUP BY
    day
ORDER BY day DESC