# 17630-A6

## Build and Run

1. Make sure you have `Java 1.8`
2. Make DataServer is up and running
3. If you have run this program before, make sure all the summary data and data bucket files in your output directory is deleted.
3. `javac *.java`
4. `java Main $PATH_TO_CONFIG_FILE`

## Config file

An example configuration file looks like this:

```
port=6789
output_dir=/Users/davidiamyou/Downloads/A6/
schema=hour:integer;minute:integer;second:integer;humidity:float;temperature:float;pressure:float
```

## TODO

1. A Command line UI that calls `Api.defaultApi()` for functions
2. Sanitize user input field names against the schema
3. More testing...

## Meeting Notes Monday

1. Hashing Function (Hashes relative time to the index of the data chunks)
2. Parsing the config file (port, data schema, output folder) to some JSON Map. The JSON Map should be used as a context through out the app.
3. Write summary to summary.dat
4. Search Occurance: read data.0 to data.9 one by one, do line by line search upon user input (of continue)
5. Time range -> Hash -> Data chunk indexes -> Filter through all selected chunks by start and end time

```
{
  "port": 6789,
  "output_dir": "/Users/david/Documents/a6",
  "schema": [
    {name: "hours", type: "integer"},
    ...
    {name: "pressure", type: "float"}
  ]
}
```

Q1 (ASK Mel what is a buffer, point 2 last point)
Q2. We can make assumption the user can provide any input as long as all of the physical units in float and time in int.
Q3. Ask Mel to clarify what he has in mind as an API.
