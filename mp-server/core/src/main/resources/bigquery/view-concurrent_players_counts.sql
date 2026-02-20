CREATE OR REPLACE VIEW `${google.cloud.project.id}.${google.cloud.bigquery.dataset.name}.concurrent_players_counts` AS
SELECT
      all_times.unique_timestamp,
      SUM(
        CASE
          WHEN rounds_sums_total.Round_Start_Time <= all_times.unique_timestamp AND all_times.unique_timestamp <= rounds_sums_total.Round_End_Time
          THEN rounds_sums_total.Players
          ELSE 0
        END
      ) AS concurrent_players
    FROM
      `${google.cloud.project.id}.${google.cloud.bigquery.dataset.name}.daily_rounds_all_times` all_times
    JOIN
      `${google.cloud.project.id}.${google.cloud.bigquery.dataset.name}.daily_rounds_sums_total` rounds_sums_total
    ON
      all_times.unique_timestamp BETWEEN rounds_sums_total.Round_Start_Time AND rounds_sums_total.Round_End_Time
    GROUP BY
      all_times.unique_timestamp
    ORDER BY all_times.unique_timestamp DESC