#!/bin/bash
for i in {1..50}; do
  curl -X POST \
        http://localhost:9090/simulation/single \
        -u admin:admin \
        -H 'content-type: text/plain' \
        -d '{
        "siddhiAppName": "timeSeriesExtensionSample",
        "streamName": "InputStream",
        "timestamp": null,
        "data": [
          1000,
          2000
        ]
      }'
done