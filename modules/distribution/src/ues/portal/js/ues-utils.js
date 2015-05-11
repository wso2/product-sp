var ues = ues || {};

(function () {
    var relativePrefix = function () {
        var path = window.location.pathname;
        //.match(/.*(\/dashboards\/).+/ig) ? '../dashboards' : 'dashboards'
        var parts = path.split('/');
        var prefix = '';
        var i;
        var count = parts.length - 3;
        for (i = 0; i < count; i++) {
            prefix += '../';
        }
        return prefix;
    };

    ues.utils = {
        relativePrefix: relativePrefix
    };
}());