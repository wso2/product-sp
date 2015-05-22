$(function () {

    var dashboardsApi = ues.utils.relativePrefix() + 'apis/dashboards';

    var dashboardsListHbs = Handlebars.compile($("#dashboards-list-hbs").html());

    var initDashboardList = function () {
        ues.store.assets('dashboard', {
            start: 0,
            count: 20
        }, function (err, data) {
            $('#wrapper').html(dashboardsListHbs(data))
                .find('.ues-dashboards .ues-delete').on('click', function () {
                    var button = Ladda.create(this);
                    button.start();
                    var id = $(this).closest('.ues-dashboard').data('id');
                    $.ajax({
                        url: dashboardsApi + '/' + id,
                        method: 'DELETE',
                        success: function () {
                            button.stop();
                            location.reload();
                        },
                        error: function () {
                            button.stop();
                        }
                    })
                });
        });
    };

    initDashboardList();
});