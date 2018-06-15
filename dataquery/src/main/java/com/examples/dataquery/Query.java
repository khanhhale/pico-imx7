package com.examples.dataquery;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.joda.time.Duration;

import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.BigQueryOptions;
import com.google.cloud.bigquery.FieldValue;
import com.google.cloud.bigquery.FieldValueList;
import com.google.cloud.bigquery.JobException;
import com.google.cloud.bigquery.QueryJobConfiguration;
import com.google.cloud.bigquery.QueryParameterValue;

public class Query {

	public static void main(String[] args) {
		BigQuery bigquery = BigQueryOptions.getDefaultInstance().getService();
		long startTime=0;
		long endTime=0;
		long averageRate=0;
		long freq=0;
		
		String query = "#standardSQL\n  with temp as(SELECT ROW_NUMBER() OVER(ORDER BY publishTime) as rownum, wt.* FROM `cloud-iot-testing-185623.iot_bigdata_dataset1.weather` wt), average as(SELECT AVG(UNIX_MILLIS(tempcur.publishTime) - UNIX_MILLIS(tempprev.publishTime)) as time_gap_in_millisec from temp tempcur left join temp tempprev on tempcur.rownum = tempprev.rownum + 1 group by tempcur.rownum),\n" + 
				"message_table as(select AVG(UNIX_MILLIS(tempcur.publishTime) - UNIX_MILLIS(tempprev.publishTime)) as time_gap_in_millisec, MIN((UNIX_MILLIS(tempcur.publishTime) - UNIX_MILLIS(tempprev.publishTime))/@averageRate) as messages from temp tempcur left join temp tempprev on tempcur.rownum = tempprev.rownum + 1 group by tempcur.rownum order by tempcur.rownum)\n" + 
				"select cast(round(time_gap_in_millisec) as int64) as time_gap_in_millisec, cast(round(messages) as int64) as messages from message_table where messages is not null and time_gap_in_millisec between @startTime and @endTime";
		
		//M=millisecond, s = seconds, m=minutes, h=hours, d=days
		try {
			Duration startT = Query.durationParse(args[0]);
			Duration endT = Query.durationParse(args[1]);
			Duration averageR = Query.durationParse(args[2]);

			startTime = startT.getMillis();
			endTime = endT.getMillis();
			averageRate=averageR.getMillis();
			
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
				
		QueryJobConfiguration queryconfig = QueryJobConfiguration.newBuilder(query)
				.addNamedParameter("averageRate", QueryParameterValue.int64(averageRate))
				.addNamedParameter("startTime", QueryParameterValue.int64(startTime))
				.addNamedParameter("endTime", QueryParameterValue.int64(endTime))
				.build();		
		try {
			try {
				System.out.printf("%s per message \t Messages from averages rates\n", Query.timeStringParse(args[0]));
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			for (FieldValueList row: bigquery.query(queryconfig).iterateAll()) {
				FieldValue timeGap = row.get(0);
				FieldValue messages = row.get(1);
				
			
				try {
					if(Query.timeStringParse(args[0]) == "Milliseconds")
					freq = Duration.millis(timeGap.getLongValue()).getMillis();
					else if(Query.timeStringParse(args[0]) == "Seconds") {
					freq = Duration.millis(timeGap.getLongValue()).getStandardSeconds();
					}
					else if(Query.timeStringParse(args[0]) == "Minutes")
					freq = Duration.millis(timeGap.getLongValue()).getStandardMinutes();
					else if(Query.timeStringParse(args[0]) == "Hours")
					freq = Duration.millis(timeGap.getLongValue()).getStandardHours();
					else if(Query.timeStringParse(args[0]) == "Days")
					freq = Duration.millis(timeGap.getLongValue()).getStandardDays();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				
		        System.out.printf("%d\t\t\t\t\t%d\n", freq, messages.getLongValue());
			}
			
		} catch (JobException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}


	 private static String timeStringParse(String durationStr) throws Exception{
		 
		  if(durationStr.indexOf('M') != -1) {	   
		     return "Milliseconds"; 
		      }	
		  else if(durationStr.indexOf('s') != -1) {	   
			 return "Seconds";
	      }
	      else if(durationStr.indexOf('m') != -1) {
	    	 return "Minutes";
	      }
	      else if(durationStr.indexOf('h') != -1) {
	     	 return "Hours";
	      }
	      else if(durationStr.indexOf('d') != -1) {
	      	 return "Days";
	      }
	      else {
	    	 throw new Exception("Duration symbol does not exist!");
		  }
	     
	  }
	 
	 private static Duration durationParse(String durationStr) throws Exception{
			
		  Pattern pat = Pattern.compile("[a-zA-Z]");
		  Matcher match = pat.matcher(durationStr); 
		  
		  String durationString = durationStr.replaceAll("\\D","");  
		if(match.find()) { 
		 
		  if(durationStr.indexOf('M') != -1) {	   
				 return Duration.millis(Integer.valueOf(durationString)); 
		      }	
		  else if(durationStr.indexOf('s') != -1) {	   
			 return Duration.standardSeconds(Integer.valueOf(durationString)); 
	      }
	      else if(durationStr.indexOf('m') != -1) {
	    	 return Duration.standardMinutes(Integer.valueOf(durationString));
	      }
	      else if(durationStr.indexOf('h') != -1) {
	     	 return Duration.standardHours(Integer.valueOf(durationString));
	      }
	      else if(durationStr.indexOf('d') != -1) {
	      	 return Duration.standardDays(Integer.valueOf(durationString));
	      }
	      else {
	    	  throw new Exception("Duration symbol does not exist!"); 
		  }
	     
		}
		  else
			  throw new Exception("Duration symbol does not exist!"); 
	  }
	  
}