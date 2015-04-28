var log = new Log();

var carbon = require('carbon');

//TODO: what happen when the context is changed or mapped via reverse proxy
var registryPath = function (id) {
    var path = '/_system/config/ues/dashboards';
    return id ? path + '/' + id : path;
};

var findOne = function (id) {
    var usr = require('/modules/user.js');
    var user = usr.current();
    var server = new carbon.server.Server();
    var registry = new carbon.registry.Registry(server, {
        username: user.username,
        tenantId: user.tenantId
    });
    var content = registry.content(registryPath(id));
    return JSON.parse(content);
};

var find = function () {
    var usr = require('/modules/user.js');
    var user = usr.current();
    var server = new carbon.server.Server();
    var registry = new carbon.registry.Registry(server, {
        username: user.username,
        tenantId: user.tenantId
    });
    var dashboards = registry.content(registryPath());
    var dashboardz = [];
    dashboards.forEach(function (dashboard) {
        dashboardz.push(JSON.parse(registry.content(dashboard)));
    });
    return dashboardz;
};

var create = function (dashboard) {
    var usr = require('/modules/user.js');
    var user = usr.current();
    var server = new carbon.server.Server();
    var registry = new carbon.registry.Registry(server, {
        username: user.username,
        tenantId: user.tenantId
    });
    registry.put(registryPath(dashboard.id), {
        content: JSON.stringify(dashboard),
        mediaType: 'application/json'
    });
};