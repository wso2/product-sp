var log = new Log();

var carbon = require('carbon');

//TODO: what happen when the context is changed or mapped via reverse proxy
var registryPath = function (id) {
    var path = '/_system/config/ues/dashboards';
    return id ? path + '/' + id : path;
};

var findOne = function (id) {
    var server = new carbon.server.Server();
    var registry = new carbon.registry.Registry(server, {
        system: true
    });
    var content = registry.content(registryPath(id));
    return JSON.parse(content);
};

var find = function () {
    var server = new carbon.server.Server();
    var registry = new carbon.registry.Registry(server, {
        system: true
    });
    var dashboards = registry.content(registryPath());
    var dashboardz = [];
    dashboards.forEach(function (dashboard) {
        dashboardz.push(JSON.parse(registry.content(dashboard)));
    });
    return dashboardz;
};

var create = function (dashboard) {
    var server = new carbon.server.Server();
    var registry = new carbon.registry.Registry(server, {
        system: true
    });
    var path = registryPath(dashboard.id);
    if (registry.exists(path)) {
        throw 'a dashboard exists with the same id ' + dashboard.id;
    }
    registry.put(path, {
        content: JSON.stringify(dashboard),
        mediaType: 'application/json'
    });
};

var update = function (dashboard) {
    var server = new carbon.server.Server();
    var registry = new carbon.registry.Registry(server, {
        system: true
    });
    var path = registryPath(dashboard.id);
    if (!registry.exists(path)) {
        throw 'a dashboard cannot be found with the id ' + dashboard.id;
    }
    registry.put(path, {
        content: JSON.stringify(dashboard),
        mediaType: 'application/json'
    });
};

var remove = function (id) {
    var server = new carbon.server.Server();
    var registry = new carbon.registry.Registry(server, {
        system: true
    });
    var path = registryPath(id);
    if (registry.exists(path)) {
        registry.remove(path);
    }
};

var allowed = function (dashboard, permission) {
    var usr = require('/modules/user.js');
    var utils = require('/modules/utils.js');
    var user = usr.current();
    var permissions = dashboard.permissions;
    if (permission.edit) {
        return utils.allowed(user.roles, permissions.editors);
    }
    if (permission.view) {
        return utils.allowed(user.roles, permissions.viewers);
    }
};