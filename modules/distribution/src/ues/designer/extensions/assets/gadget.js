(function () {
    ues.plugins.assets['gadget'] = function (asset, options) {
        asset.data.url = options.resolveURI(asset.data.url);
        asset.data.thumbnail = options.resolveURI(asset.data.thumbnail);
    };
}());