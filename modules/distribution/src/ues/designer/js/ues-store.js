var ues = ues || {};
var store = {};

(function () {

    var assetsUrl = ues.utils.relativePrefix() + 'assets';

    var store = (ues.store = {});

    store.gadget = function (id, cb) {
        $.get(assetsUrl + '/' + id + '?type=gadget', function (data) {
            cb(false, data);
        }, 'json');
    };

    store.gadgets = function (paging, cb) {
        $.get(assetsUrl + '?start=' + paging.start + '&count=' + paging.count + '&type=gadget', function (data) {
            cb(false, data);
        }, 'json');
    };

    store.layout = function (id, cb) {
        $.get(assetsUrl + '/' + id + '?type=layout', function (data) {
            cb(false, data);
        }, 'json');
    };

    store.layouts = function (paging, cb) {
        $.get(assetsUrl + '?start=' + paging.start + '&count=' + paging.count + '&type=layout', function (data) {
            cb(false, data);
        }, 'json');
    };


}());