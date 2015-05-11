(function () {
    ues.plugins.uris['store'] = function (uri) {
        //TODO dynamically calculate the uri
        return 'https://localhost:9443/' + uri;
    };
}());