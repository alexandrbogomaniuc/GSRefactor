CREATE OR REPLACE VIEW  `${google.cloud.project.id}.${google.cloud.bigquery.dataset.name}.daily_players_per_games_per_buyin_count` AS
SELECT 
    daily_players_per_games.day, 
    daily_players_per_games.room_gameId, 
    daily_players_per_games.room_gameName, 
    daily_players_per_games.room_currency, 
    daily_players_per_games.room_buyInStake,
    COUNT(DISTINCT daily_players_per_games.player_nickname) AS player_count,
    ROUND((COUNT(DISTINCT daily_players_per_games.player_nickname) / daily_players_count.player_count) * 100, 0) AS percent
FROM 
    `${google.cloud.project.id}.${google.cloud.bigquery.dataset.name}.daily_players_per_games` daily_players_per_games
JOIN
    `${google.cloud.project.id}.${google.cloud.bigquery.dataset.name}.daily_players_count` daily_players_count
ON
    daily_players_per_games.day = daily_players_count.day
GROUP BY
    daily_players_per_games.day, 
    daily_players_per_games.room_gameId, 
    daily_players_per_games.room_gameName,
    daily_players_per_games.room_currency, 
    daily_players_per_games.room_buyInStake,
    daily_players_count.player_count
ORDER BY 
    daily_players_per_games.day DESC,
    daily_players_per_games.room_gameId, 
    daily_players_per_games.room_currency, 
    daily_players_per_games.room_buyInStake
