CREATE OR REPLACE VIEW `${google.cloud.project.id}.${google.cloud.bigquery.dataset.name}.current_rooms_players` AS
SELECT
    MAX(s_date) as s_date,
    room_id,
    room_isBattleground,
    room_isPrivate,
    room_buyInStake,
    room_currency,
    room_gameId,
    room_gameName,
    player_nickname,
    player_isOwner,
    player_seatNr
FROM `${google.cloud.project.id}.${google.cloud.bigquery.dataset.name}.rooms_players`
WHERE s_date >= TIMESTAMP_SUB(CURRENT_TIMESTAMP(), INTERVAL 20 SECOND)
GROUP BY
    room_id,
    room_isBattleground,
    room_isPrivate,
    room_buyInStake,
    room_currency,
    room_gameId,
    room_gameName,
    player_nickname,
    player_isOwner,
    player_seatNr
ORDER BY s_date DESC;