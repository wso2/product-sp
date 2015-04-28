var log = new Log();

var dir = '/store/';

var assetsDir = function (type) {
    return dir + type + '/';
};

var findOne = function (type, id) {
    var parent = assetsDir(type);
    var file = new File(parent + id);
    if (!file.isExists()) {
        return null;
    }
    file = new File(file.getPath() + '/' + type + '.json');
    if (!file.isExists()) {
        return null;
    }
    file.open('r');
    var asset = JSON.parse(file.readAll());
    file.close();
    return asset;
};

var find = function (type, query, start, count) {
    var parent = new File(assetsDir(type));
    var assetz = parent.listFiles();
    var assets = [];
    assetz.forEach(function (asset) {
        if (!asset.isDirectory()) {
            return;
        }
        asset = new File(asset.getPath() + '/' + type + '.json');
        asset.open('r');
        assets.push(JSON.parse(asset.readAll()));
        asset.close();
    });
    return assets;
};

var create = function (type, asset) {
    var parent = new File(assetsDir(type));
    var file = new File(asset.id, parent);
    file.mkdir();
    file = new File(type + '.json', file);
    file.open('w');
    file.write(JSON.stringify(asset));
    file.close();
};

var update = function (asset) {

};

var remove = function (id) {

};


/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

var createDir = function (path) {
    var file = new File(path);
    if (file.isExists()) {
        file.del();
    }
    file.mkdir();
  };

  var copyFile = function (src, dst) {
    var stream = src.getStream();
    dst = dst + '/' + src.getName();
    var dstf = new File(dst);
    dstf.open('w');
    dstf.write(stream);
    dstf.close();
};

var copyDir = function  (source,destination) {
    if(source.isDirectory()) {
        createDir(destination + "/" + source.getName());
        destination = destination + "/" + source.getName();
        var sourceFiles = source.listFiles();
        for (var i = 0; i < sourceFiles.length; i++) {
            var inFile = sourceFiles[i];
            log.info(inFile.getName());
            copyDir(inFile, destination);
        }
    } else {
        // log.info("** Copying file " + source.getName());
        copyFile(source,destination);
    }
};


