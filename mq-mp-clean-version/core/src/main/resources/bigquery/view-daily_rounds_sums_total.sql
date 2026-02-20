CREATE OR REPLACE VIEW `${google.cloud.project.id}.${google.cloud.bigquery.dataset.name}.daily_rounds_sums_total` AS
SELECT     
    DATE(s_date) AS day,
    Round_ID,
    Round_Start_Time,
    Round_End_Time,
    Group_Game_Name, 
    Room_Type, 
    Currency, 
    Room_Value,
    Rake_value,
    COUNT(Player_ID) AS Players,
    SUM(Total_Bets_value) AS Sum_Total_Bets_value,
    SUM(Total_Win_value) AS Sum_Total_Win_value,
FROM `${google.cloud.project.id}.${google.cloud.bigquery.dataset.name}.round_result`
GROUP BY  
    day,
    Round_ID,
    Round_Start_Time,
    Round_End_Time,
    Group_Game_Name, 
    Room_Type, 
    Currency, 
    Room_Value,
    Rake_value
HAVING SUM(Total_Bets_value) <> 0 OR SUM(Total_Win_value) <> 0
ORDER BY day DESC