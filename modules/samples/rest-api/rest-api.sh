port="9443"
username="admin"
password="admin"

for c in $*
do
    case $c in
    (*=*) eval $c;;
    esac
done

echo "You need to have curl installed and available in your PATH to run this sample. Curl can be downloaded from http://curl.haxx.se/download.html"
echo ""
echo "Saving stream definition 1 with POST: "
echo ""
echo "curl -k --user $username:$password https://localhost:$port/datareceiver/1.0.0/streams/ --data @streamdefn1.json -H \"Accept: application/json\" -H \"Content-type: application/json\" -X POST"
curl -k --user $username:$password https://localhost:$port/datareceiver/1.0.0/streams/ --data @streamdefn1.json -H "Accept: application/json" -H "Content-type: application/json" -X POST
echo ""
echo "Posting events to stream definition 1: "
echo ""
echo "curl -k --user $username:$password https://localhost:$port/datareceiver/1.0.0/stream/stockquote.stream/1.0.2/ --data @events1.json -H \"Accept: application/json\" -H \"Content-type: application/json\" -X POST"
curl -k --user $username:$password https://localhost:$port/datareceiver/1.0.0/stream/stockquote.stream/1.0.2/ --data @events1.json -H "Accept: application/json" -H "Content-type: application/json" -X POST
echo ""
echo "Saving stream definition 2 with POST: "
echo ""
echo "curl -k --user $username:$password https://localhost:$port/datareceiver/1.0.0/streams/ --data @streamdefn2.json -H \"Accept: application/json\" -H \"Content-type: application/json\" -X POST"
curl -k --user $username:$password https://localhost:$port/datareceiver/1.0.0/streams/ --data @streamdefn2.json -H "Accept: application/json" -H "Content-type: application/json" -X POST
echo ""
echo "Posting events to stream definition 2: "
echo ""
echo "curl -k --user $username:$password https://localhost:$port/datareceiver/1.0.0/stream/stockquote.stream.2/2.0.0 --data @events2.json -H \"Accept: application/json\" -H \"Content-type: application/json\" -X POST"
curl -k --user $username:$password https://localhost:$port/datareceiver/1.0.0/stream/stockquote.stream.2/2.0.0 --data @events2.json -H "Accept: application/json" -H "Content-type: application/json" -X POST
