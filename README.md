# 17630-A6

1. Hashing Function (Hashes relative time to the index of the data chunks)
2. Parsing the config file (port, data schema, output folder) to some JSON Map. The JSON Map should be used as a context through out the app.
3. Write summary to summary.dat
4. Search Occurance: read data.0 to data.9 one by one, do line by line search upon user input (of continue)
5. Time range -> Hash -> Data chunk indexes -> Filter through all selected chunks by start and end time


{
  "port": 6789,
  "output_dir": "/Users/david/Documents/a6",
  "schema": [
    {name: "hours", type: "integer"},
    ...
    {name: "pressure", type: "float"}
  ]
}

Q1 (ASK Mel what is a buffer, point 2 last point)
Q2. We can make assumption the user can provide any input as long as all of the physical units in float and time in int.
Q3. Ask Mel to clarify what he has in mind as an API.
