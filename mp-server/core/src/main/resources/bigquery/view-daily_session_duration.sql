CREATE OR REPLACE VIEW `[].[].daily_session_duration` AS
SELECT
    DATE(s_date) AS day,
    Group_Game_Name,
    Room_Type,
    Session_ID,
    SUM(TIMESTAMP_DIFF(Round_End_Time, Round_Start_Time, SECOND)) AS total_session_duration
  FROM
    `[].[].round_result`
  GROUP BY
    day,
    Group_Game_Name,
    Room_Type,
    Session_ID
  ORDER BY
    day DESC