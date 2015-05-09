(function () {
    var prefix = ues.utils.relativePrefix();

    ues.plugins.uris['local'] = function (uri) {
        return prefix + uri;
    };
}());