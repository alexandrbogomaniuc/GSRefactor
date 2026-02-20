CREATE OR REPLACE VIEW `${google.cloud.project.id}.${google.cloud.bigquery.dataset.name}.daily_bets_wins_rakes_total` AS
SELECT 
    day,
    Currency,
    AVG(Sum_Total_Bets_value) AS avg_bet,
    AVG(Sum_Total_Win_value) AS avg_win,
    AVG(Sum_Total_Bets_value) - AVG(Sum_Total_Win_value) AS avg_rake,
    SUM(Sum_Total_Bets_value) AS sum_bet,
    SUM(Sum_Total_Win_value) AS sum_win,
    SUM(Sum_Total_Bets_value) - SUM(Sum_Total_Win_value) AS sum_rake
  FROM 
    `${google.cloud.project.id}.${google.cloud.bigquery.dataset.name}.daily_rounds_sums_total`
  GROUP BY
    day,
    Currency
  ORDER BY day DESC