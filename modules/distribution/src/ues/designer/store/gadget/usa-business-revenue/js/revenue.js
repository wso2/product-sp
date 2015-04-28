var cache = {};
var stateData = function (state) {
    if (cache[state]) {
        return cache[state];
    }
    return cache[state] = [
        {company: 'Google', revenue: Math.random()},
        {company: 'WSO2', revenue: Math.random()},
        {company: 'Facebook', revenue: Math.random()},
        {company: 'Twitter', revenue: Math.random()},
        {company: 'Microsoft', revenue: Math.random()},
        {company: 'Yahoo', revenue: Math.random()}
    ];
};

var draw = function (o) {
    var state = o.state.toLowerCase();
    document.getElementById("chart").innerHTML = "";
    var margin = {top: 20, right: 20, bottom: 30, left: 40},
        width = 400 - margin.left - margin.right,
        height = 280 - margin.top - margin.bottom;

    var x = d3.scale.ordinal()
        .rangeRoundBands([0, width], .1);

    var y = d3.scale.linear()
        .range([height, 0]);

    var xAxis = d3.svg.axis()
        .scale(x)
        .orient("bottom");

    var yAxis = d3.svg.axis()
        .scale(y)
        .orient("left")
        .ticks(10, "%");

    var svg = d3.select("svg")
        .attr("width", width + margin.left + margin.right)
        .attr("height", height + margin.top + margin.bottom)
        .append("g")
        .attr("transform", "translate(" + margin.left + "," + margin.top + ")");

    var data = stateData(state);

    x.domain(data.map(function (d) {
        return d.company;
    }));

    y.domain([0, d3.max(data, function (d) {
        return d.revenue;
    })]);

    svg.append("g")
        .attr("class", "x axis")
        .attr("transform", "translate(0," + height + ")")
        .call(xAxis);

    svg.append("g")
        .attr("class", "y axis")
        .call(yAxis)
        .append("text")
        .attr("transform", "rotate(-90)")
        .attr("y", 6)
        .attr("dy", ".71em")
        .style("text-anchor", "end")
        .text("Revenue");

    svg.selectAll(".bar")
        .data(data)
        .enter().append("rect")
        .attr("class", "bar")
        .attr("x", function (d) {
            return x(d.company);
        })
        .attr("width", x.rangeBand())
        .attr("y", function (d) {
            return y(d.revenue);
        })
        .attr("height", function (d) {
            return height - y(d.revenue);
        });
};

function type(d) {
    d.revenue = +d.revenue;
    return d;
}


gadgets.HubSettings.onConnect = function () {
    gadgets.Hub.subscribe('state', function (topic, data, subscriberData) {
        draw(data);
    });
};