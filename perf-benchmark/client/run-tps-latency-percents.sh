#!/usr/bin/env bash

baseDir=$(dirname "$0")
concLevels="1 10 25 50 100 200 400 800 1600 3200"
mSizes="1024 10240 51200 102400"
mode="IServer"
service="http://localhost:9090/stockquote/stocks"
be_service="http://localhost:8080/EchoService/echo"
perTestTime=30
testLoops=1000000
warmUpConc=200
warmUpLoop=50000

tmpDir="$baseDir/tmpData"
resultsDir="$baseDir/results"
default_payload="$baseDir/1kb_rand_data.txt"
timeStmp=$(date +%s)

types=()
declare -A MAP

function printResultStructures(){
    echo "Printing results map.."
    for key in ${!MAP[@]}
    do
      echo "$key -> ${MAP[$key]}"
    done
}

function hash(){
    echo -n $1 | md5sum | awk '{print $1}'
}

function processResults(){
    local metric=$1
    local resultsFile=$2
    local mSize=""
    local modeI=$3
    local conc=""

    rm -f "$resultsFile"

    for mSize in $mSizes
    do
        echo "Test MSize(Bytes): $mSize," >> "$resultsFile"
        local isPrintH=true
        for conc in $concLevels
        do
            local header=""
            if "$isPrintH"
            then
                header="Concurrency"
            fi
            local line="$conc"
            
                
                if "$isPrintH"
                then
                    header+=", $modeI"
                    header+=", BE"
                fi
                local tps=${MAP["$mSize-$modeI-$conc-$metric"]}
                local tps_be=${MAP["$mSize-$modeI-$conc-$metric""_be"]}
                line+=", $tps"
                line+=", $tps_be"
                 
            
            if "$isPrintH"
            then
                echo "$header" >> "$resultsFile"
                isPrintH=false
            fi
            echo "$line" >> "$resultsFile"
        done
        echo "" >> "$resultsFile"
    done
    echo "==========================================="
    echo "            Results ($metric)              "
    echo "==========================================="
    cat "$resultsFile"
}

function processPercentiles(){
    local resultsFile=$1
    local mSize=""
    local  modeI=$2
    local conc=""

    rm -f "$resultsFile"

    local header="Mode, Concurrency"
    for hVal in $(seq 0 100)
    do
        header+=", $hVal"
    done

    for mSize in $mSizes
    do
        echo "Test: $mSize," >> "$resultsFile"
        for conc in $concLevels
        do
            local isPrintH=true
            
                local percents=${MAP["$mSize-$modeI-$conc-percents"]}
                local percents_be=${MAP["$mSize-$modeI-$conc-percents_be"]}
                if "$isPrintH"
                then
                    echo "$header" >> "$resultsFile"
                    isPrintH=false
                fi
                echo "$percents" >> "$resultsFile"
                echo "$percents_be" >> "$resultsFile"
           
            echo "" >> "$resultsFile"
        done
    done
    echo "==========================================="
    echo "            Results (Percentiles)              "
    echo "==========================================="
    cat "$resultsFile"
}

function warmUp(){
    local service=$1
    echo "Warm up service $service"
    ab -k -p "$default_payload" -c $warmUpConc -n $warmUpLoop -H "Accept:text/plain" "$service" > /dev/null
}


function testConcLevel(){
    local service=$1
    local concLevel=$2
    local mSize=$3
    local mode=$4

    local resOut="$tmpDir/result-$mSize-$mode-conc$concLevel-rep$loopRep-loops$testLoops-time$timeStmp-$(uuidgen)"
    local resOutBE="$tmpDir/result-$mSize-$mode-conc$concLevel-rep$loopRep-loops$testLoops-time$timeStmp-$(uuidgen)-be"
    local percentOut="$tmpDir/percentile-$mSize-$mode-conc$concLevel-rep$loopRep-loops$testLoops-time$timeStmp-$(uuidgen)"
    local percentOutBE="$tmpDir/percentile-$mSize-$mode-conc$concLevel-rep$loopRep-loops$testLoops-time$timeStmp-$(uuidgen)-be"
    local payload="$mSize""B_rand_data.txt"
    echo "Testing service: $service"
    echo "Testing concurrency $concLevel at $resOut"
    ab -t "$perTestTime" -n "$testLoops" -c "$concLevel" -H "Accept:text/plain" -p "$payload" -k -e "$percentOut" "$service" > "$resOut"
    echo "Testing BE service: $be_service"
    ab -t "$perTestTime" -n "$testLoops" -c "$concLevel" -H "Accept:text/plain" -p "$payload" -k -e "$percentOutBE" "$be_service" > "$resOutBE"

    local tps=$(cat "$resOut" | grep -Eo "Requests per second.*" | grep -Eo "[0-9]+" | head -1)
   
    local tps_be=$(cat "$resOutBE" | grep -Eo "Requests per second.*" | grep -Eo "[0-9]+" | head -1)


    local meanLat=$(cat "$resOut" | grep -Eo "Time per request.*\(mean\)" | grep -Eo "[0-9]+(\.[0-9]+)?")
    local meanLat_be=$(cat "$resOutBE" | grep -Eo "Time per request.*\(mean\)" | grep -Eo "[0-9]+(\.[0-9]+)?")

    local percents=$(cat "$percentOut" | grep -Eo ",.*" | grep -Eo "[0-9]+(\.[0-9]+)?" | tr '\n' ',')
    local percents_be=$(cat "$percentOutBE" | grep -Eo ",.*" | grep -Eo "[0-9]+(\.[0-9]+)?" | tr '\n' ',')

    percents="$mode, $concLevel, $percents"
    percents_be="BE, $concLevel, $percents_be"

    echo "For $service at concurrency $concLevel"

    MAP["$mSize-$mode-$concLevel-tps"]=$tps
    echo -e "\tThroughput IS $tps"
    MAP["$mSize-$mode-$concLevel-tps_be"]=$tps_be
    echo -e "\tThroughput BE $tps_be"

    MAP["$mSize-$mode-$concLevel-meanLat"]=$meanLat
    echo -e "\tMean latency IS is $meanLat"
    MAP["$mSize-$mode-$concLevel-meanLat_be"]=$meanLat_be
    echo -e "\tMean latency BE is $meanLat_be"

    MAP["$mSize-$mode-$concLevel-percents"]=$percents
    echo -e "\tPercentiles IS are $percents"
    MAP["$mSize-$mode-$concLevel-percents_be"]=$percents_be
    echo -e "\tPercentiles BE are $percents_be"
}



function iterateConcLevels(){
    local mSize=$1
    local mode=$2
    local service=$3
    echo "Testing concurrency levels"
    warmUp "$service" # Warm up the service before getting results
    local concLevel=""
    for concLevel in $concLevels
    do
        testConcLevel "$service" "$concLevel" "$mSize" "$mode"
    done
}


function iteratemSizes(){
    local mode=$1
    local service=$2
    echo "Testing Message Sizes"
    warmUp "$service" # Warm up the service before getting results
    local mSize=""
    for mSize in $mSizes
    do
        iterateConcLevels "$mSize" "$mode" "$service"
    done

    processResults "tps" "$resultsDir/results-tps.csv"  "$mode"
    echo ""
    processResults "meanLat" "$resultsDir/results-latency.csv" "$mode"
    echo ""
    processPercentiles "$resultsDir/results-percentiles.csv" "$mode"
    echo ""
    #printResultStructures
}


function createFiles(){
    
    for mSize in $mSizes
    do
        base64 /dev/urandom | head -c $mSize > "$mSize""B_rand_data.txt"
    done

   
}

mkdir -p "$tmpDir"
mkdir "$resultsDir"

base64 /dev/urandom | head -c 1024 > "$default_payload"
createFiles
iteratemSizes "$mode" "$service"

