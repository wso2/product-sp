# API Docs - v4.0.4-SNAPSHOT

## Throughput

### throughput *<a target="_blank" href="https://wso2.github.io/siddhi/documentation/siddhi-4.0/#stream-processor">(Stream Processor)</a>*

<p style="word-wrap: break-word">Measuring performance of stream processor with simple passthrough</p>

<span id="syntax" class="md-typeset" style="display: block; font-weight: bold;">Syntax</span>
```
throughput:throughput(<LONG> iijtimestamp)
```

<span id="query-parameters" class="md-typeset" style="display: block; color: rgba(0, 0, 0, 0.54); font-size: 12.8px; font-weight: bold;">QUERY PARAMETERS</span>
<table>
    <tr>
        <th>Name</th>
        <th style="min-width: 20em">Description</th>
        <th>Default Value</th>
        <th>Possible Data Types</th>
        <th>Optional</th>
        <th>Dynamic</th>
    </tr>
    <tr>
        <td style="vertical-align: top">iijtimestamp</td>
        <td style="vertical-align: top; word-wrap: break-word">This value used to find the sending timestamp from client</td>
        <td style="vertical-align: top"></td>
        <td style="vertical-align: top">LONG</td>
        <td style="vertical-align: top">No</td>
        <td style="vertical-align: top">No</td>
    </tr>
</table>

<span id="examples" class="md-typeset" style="display: block; font-weight: bold;">Examples</span>
<span id="example-1" class="md-typeset" style="display: block; color: rgba(0, 0, 0, 0.54); font-size: 12.8px; font-weight: bold;">EXAMPLE 1</span>
```
@App:name("TCP_Benchmark")
@source(type = 'tcp', context='inputStream',@map(type='binary'))
define stream outputStream (iijtimestamp long,value float);
from inputStream
select iijtimestamp,value
insert into tempStream;from tempStream#throughput:throughput(iijtimestamp,value)
select "aa" as tempTrrb
insert into tempStream1;
```
<p style="word-wrap: break-word">This is a simple passthrough query that inserts iijtimestamp (long) and random number(float) into the temp stream  </p>

<span id="example-2" class="md-typeset" style="display: block; color: rgba(0, 0, 0, 0.54); font-size: 12.8px; font-weight: bold;">EXAMPLE 2</span>
```
@App:name("TCP_Benchmark")
@source(type = 'tcp', context='inputStream',@map(type='binary'))
define stream inputStream (iijtimestamp long,value float);
define stream outputStream (iijtimestamp long,value float,mode String);
from inputStream[value<=0.25]
select iijtimestamp,value
insert into tempStream;
from tempStream#throughput:throughput(iijtimestamp,value,"both")
select "aa" as tempTrrb
insert into tempStream1;
```
<p style="word-wrap: break-word">This is a filter query</p>

