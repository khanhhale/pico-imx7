/**
 * Triggered from a message on a Cloud Pub/Sub topic.
 *
 * @param {!Object} event The Cloud Functions event.
 * @param {!Function} The callback function.
 */
const request = require('retry-request');
const fs = require('fs');
const pubSub = require('@google-cloud/pubsub');
const BigQuery = require('@google-cloud/bigquery');

const opts = {
        datasetId: "iot_bigdata_dataset1",
        tableId: "weather",
        projectId: "cloud-iot-testing-185623"
       }

function insertRowsIntoBigquery(datasetId, tableId, rows, projectId) {
  const bigquery = new BigQuery({
    projectId: projectId,
  });

  // Inserts data into a table
  bigquery
    .dataset(datasetId)
    .table(tableId)
    .insert(rows)
    .then(() => {
      console.log(`Inserted ${rows.length} rows`);
    })
    .catch(err => {
      if (err && err.name === 'PartialFailureError') {
        if (err.errors && err.errors.length > 0) {
          console.log('Insert errors:');
          err.errors.forEach(err => console.error(err));
        }
      } else {
        console.error('ERROR:', err);
      }
    });
  // [END bigquery_table_insert_rows]
}

exports.subscribe = (event, callback) => {
  	  const pubsubMessage = event.data;
      var data = null;
      var rows = null;
      var re = null
      var payload = [];
      var publishTime = Math.round(new Date().getTime());
      var messageType = "state"; 
      var pubsubMessageJason = null;
      try {
        data = Buffer.from(pubsubMessage.data, 'base64').toString();
        var jsonData = JSON.parse(data);
          
        for(key in jsonData){
          if(key == "data"){
            messageType = "event";
            break;
          }     
        }  
        var attributes = JSON.stringify(pubsubMessage.attributes);
              
        delete pubsubMessage.attributes;
        delete pubsubMessage["@type"];
                 
        data = JSON.stringify(jsonData);
             
        console.log("messageType: " + messageType);  
        
        if(messageType == "event")
        {         
        	re = /^\{"data":(\[.*\])\}$/i;
        	payload = data.match(re); 
            
            console.log("pubsubmessage: "+JSON.stringify(pubsubMessage));
            console.log("payload[0]: " + payload[0]); 
            data = payload[1].replace(/"/g, '\\"');
        	rows = `{"attributes":${attributes},"publishTime":${publishTime},"messageType":"${messageType}","messageContent":"${data}"}`;
        }
        else
        {     
            console.log("data: " + data);
            data = data.replace(/"/g, '\\"');
        	rows = `{"attributes":${attributes},"publishTime":${publishTime},"messageType":"${messageType}","messageContent":"${data}"}`;
        }
        
        console.log("rows: "+rows);
        
        rows = JSON.parse(rows);      
      } catch (err) {
        throw new Error(
          `"Invalid Data.`
        );
      }
 
      insertRowsIntoBigquery(
        opts.datasetId,
        opts.tableId,
        rows,
        opts.projectId
      );
      
};
