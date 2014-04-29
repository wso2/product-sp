@echo off

SET port=9443
SET username=admin
SET password=admin

:loop
IF NOT "%1"=="" (
    IF "%1"=="-port" (
        SET port=%2
        SHIFT
    )
    IF "%1"=="-username" (
        SET username=%2
        SHIFT
    )
    IF "%1"=="-password" (
        SET password=%2
        SHIFT
    )
    SHIFT
    GOTO :loop
)

echo "You need to have curl installed and available in your PATH to run this sample. Curl can be downloaded from http://curl.haxx.se/download.html"
echo ""
echo "Saving stream definition 1 with POST: "
echo ""
echo "curl -k --user %username%:%password% https://localhost:%port%/datareceiver/1.0.0/streams/ --data @streamdefn1.json -H \"Accept: application/json\" -H \"Content-type: application/json\" -X POST"
curl -k --user %username%:%password% https://localhost:%port%/datareceiver/1.0.0/streams/ --data @streamdefn1.json -H "Accept: application/json" -H "Content-type: application/json" -X POST
echo ""
echo "Posting events to stream definition 1: "
echo ""
echo "curl -k --user %username%:%password% https://localhost:%port%/datareceiver/1.0.0/stream/stockquote.stream/1.0.2/ --data @events1.json -H \"Accept: application/json\" -H \"Content-type: application/json\" -X POST"
curl -k --user %username%:%password% https://localhost:%port%/datareceiver/1.0.0/stream/stockquote.stream/1.0.2/ --data @events1.json -H "Accept: application/json" -H "Content-type: application/json" -X POST
echo ""
echo "Saving stream definition 2 with POST: "
echo ""
echo "curl -k --user %username%:%password% https://localhost:%port%/datareceiver/1.0.0/streams/ --data @streamdefn2.json -H \"Accept: application/json\" -H \"Content-type: application/json\" -X POST"
curl -k --user %username%:%password% https://localhost:%port%/datareceiver/1.0.0/streams/ --data @streamdefn2.json -H "Accept: application/json" -H "Content-type: application/json" -X POST
echo ""
echo "Posting events to stream definition 2: "
echo ""
echo "curl -k --user %username%:%password% https://localhost:%port%/datareceiver/1.0.0/stream/stockquote.stream.2/2.0.0 --data @events2.json -H \"Accept: application/json\" -H \"Content-type: application/json\" -X POST"
curl -k --user %username%:%password% https://localhost:%port%/datareceiver/1.0.0/stream/stockquote.stream.2/2.0.0 --data @events2.json -H "Accept: application/json" -H "Content-type: application/json" -X POST
