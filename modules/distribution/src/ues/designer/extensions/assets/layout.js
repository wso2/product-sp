var update = function (asset, options) {
    asset.url = options.resolveURI(options.url);
    asset.thumbnail = options.resolveURI(options.thumbnail);
};