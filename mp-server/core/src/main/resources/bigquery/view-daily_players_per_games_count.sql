CREATE OR REPLACE VIEW `${google.cloud.project.id}.${google.cloud.bigquery.dataset.name}.daily_players_per_games_count` AS
SELECT 
    day, 
    room_gameId, 
    room_gameName, 
    COUNT(DISTINCT player_nickname) AS player_count
FROM 
    `${google.cloud.project.id}.${google.cloud.bigquery.dataset.name}.daily_players_per_games`
GROUP BY
    day, 
    room_gameId, 
    room_gameName
ORDER BY day DESC