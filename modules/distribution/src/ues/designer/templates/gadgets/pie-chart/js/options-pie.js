var options = {
    series: {
        pie: {
            show: true,
            radius: 1,
            label: {
                show: true,
                radius: 2/3,
                formatter: labelFormatter,
                threshold: 0.1
            }
        }
    },
    legend: {
        show: false
    }
};
function labelFormatter(label, series) {
		return "<div style='font-size:8pt; text-align:center; padding:2px; color:white;'>" + label + "<br/>" + Math.round(series.percent) + "%</div>";
	}



