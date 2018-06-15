For 20 second interval test:

with temp as(
SELECT ROW_NUMBER() OVER(ORDER BY publishTime) as rownum, wt.* FROM `cloud-iot-testing-185623.iot_bigdata_dataset1.weather` wt
)

select max(tempcur.rownum) as cur_col, min(tempprev.rownum) as prev_col, MIN(UNIX_SECONDS(tempcur.publishTime) - UNIX_SECONDS(tempprev.publishTime)) as time_gap_in_sec from temp tempcur left join temp tempprev on tempcur.rownum = tempprev.rownum + 1 group by tempcur.rownum having abs(time_gap_in_sec/20) > 1.1 order by cur_col


Show table’s data

Standard sql:

SELECT ROW_NUMBER() OVER(ORDER BY wt.publishTime) as rownum, wt.publishTime, wtdata.ambient_pressure, wtdata.temperature, wtdata.motion FROM `gcp-io-demo.weather.readings` wt cross join UNNEST(wt.data) as wtdata where wtdata.ambient_pressure is not null or wtdata.motion is not null or wtdata.temperature is not null

