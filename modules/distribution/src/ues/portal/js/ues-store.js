var ues = ues || {};

(function () {

    var assetsUrl = ues.utils.relativePrefix() + 'assets';

    var dashboardsUrl = ues.utils.relativePrefix() + 'apis/dashboards';

    var store = (ues.store = {});

    store.asset = function (type, id, cb) {
        $.get(assetsUrl + '/' + id + '?type=' + type, function (data) {
            cb(false, data);
        }, 'json');
    };

    store.assets = function (type, paging, cb) {
        $.get(assetsUrl + '?start=' + paging.start + '&count=' + paging.count + '&type=' + type, function (data) {
            cb(false, data);
        }, 'json');
    };
}());