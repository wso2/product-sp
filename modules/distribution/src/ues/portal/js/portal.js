$(function () {

    var dashboardsListHbs = Handlebars.compile($("#dashboards-list-hbs").html());

    var initDashboards = function () {
        ues.store.dashboards({
            start: 0,
            count: 20
        }, function(err, data) {
            $('#wrapper').html(dashboardsListHbs(data));
        });
    };

    initDashboards();
});