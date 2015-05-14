$(function () {

    var overridden = false;

    var generateUrl = function (title) {
        return title.replace(/[^\w]/g, '-').toLowerCase();
    };

    var updateUrl = function () {
        if (overridden) {
            return;
        }
        var title = $('#dashboard-title').val();
        $('#dashboard-id').val(generateUrl(title));
    };

    $('#dashboard-title').on('keyup', function () {
        updateUrl();
    }).on('change', function () {
        updateUrl();
    });

    $('#dashboard-id').on('keyup', function () {
        overridden = overridden || true;
    });

    $('#dashboard-create').on('click', function () {
        var form = $('#dashboard-form');
        var action = form.attr('action');
        form.attr('action', action + '/' + $('#dashboard-id').val());
    });
});