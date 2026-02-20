CREATE OR REPLACE VIEW  `[].[].daily_rounds_duration` AS
SELECT
    DATE(Round_Start_Time) AS day,
    Group_Game_Name,
    Room_Type,
    TIMESTAMP_DIFF(Round_End_Time, Round_Start_Time, SECOND) AS round_duration_seconds
  FROM
    `[].[].round_result`
  WHERE
    Round_Start_Time IS NOT NULL AND Round_End_Time IS NOT NULL
  ORDER BY day DESC;