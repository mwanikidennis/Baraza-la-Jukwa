db = db.getSiblingDB('jukwaa_telemetry');

db.createCollection("traffic_telemetry", {
  timeseries: {
    timeField: "timestamp",
    metaField: "sensor_id",
    granularity: "minutes"
  }
});

print("Created time-series collection: traffic_telemetry");
