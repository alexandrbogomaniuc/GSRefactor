CREATE OR REPLACE VIEW `${google.cloud.project.id}.${google.cloud.bigquery.dataset.name}.daily_rounds_played_per_games` AS
SELECT     
    day,
    Group_Game_Name, 
    Room_Type, 
    Currency, 
    Room_Value,
    COUNT(Round_ID) AS Rounds_count,
FROM `${google.cloud.project.id}.${google.cloud.bigquery.dataset.name}.daily_rounds_sums_total`
GROUP BY  
    day,
    Group_Game_Name, 
    Room_Type, 
    Currency, 
    Room_Value
ORDER BY day DESC