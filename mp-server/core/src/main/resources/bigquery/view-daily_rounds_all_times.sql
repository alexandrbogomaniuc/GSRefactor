CREATE OR REPLACE VIEW `[].[].daily_rounds_all_times` AS
SELECT timestamp_trunc(Round_Start_Time, SECOND) AS unique_timestamp FROM `[].[].daily_rounds_sums_total`
  UNION DISTINCT
SELECT timestamp_trunc(Round_End_Time, SECOND) AS unique_timestamp FROM `[].[].daily_rounds_sums_total`