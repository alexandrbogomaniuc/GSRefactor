CREATE OR REPLACE VIEW `${google.cloud.project.id}.${google.cloud.bigquery.dataset.name}.current_rooms_one_player` AS
SELECT *
FROM `${google.cloud.project.id}.${google.cloud.bigquery.dataset.name}.current_rooms_players`
WHERE room_id IN (
    SELECT room_id
    FROM `${google.cloud.project.id}.${google.cloud.bigquery.dataset.name}.current_rooms_players`
    WHERE NOT room_isPrivate
    GROUP BY room_id
    HAVING count(player_nickname) = 1
)