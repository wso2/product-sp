var findOne, find, create, update, remove;
(function () {
    var log = new Log();

    var dir = '/store/';

    var assetsDir = function (type) {
        return dir + type + '/';
    };

    var allowed = function (roles, allowed) {
        var hasRole = function (role, roles) {
            var i;
            var length = roles.length;
            for (i = 0; i < length; i++) {
                if (roles[i] == role) {
                    return true;
                }
            }
            return false;
        };
        var i;
        var length = allowed.length;
        for (i = 0; i < length; i++) {
            if (hasRole(allowed[i], roles)) {
                return true;
            }
        }
        return false;
    };

    var registryPath = function (id) {
        var path = '/_system/config/ues/dashboards';
        return id ? path + '/' + id : path;
    };

    var currentContext = function () {
        var PrivilegedCarbonContext = Packages.org.wso2.carbon.context.PrivilegedCarbonContext;
        var context = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        var username = context.getUsername();

        return {
            username: username,
            domain: context.getTenantDomain(),
            tenantId: context.getTenantId()
        };
    };

    var findDashboards = function (type, query, start, count) {
        var user = currentContext();
        if (!user.username) {
            return [];
        }

        var carbon = require('carbon');
        var server = new carbon.server.Server();
        var registry = new carbon.registry.Registry(server, {
            system: true
        });
        var um = new carbon.user.UserManager(server);
        var userRoles = um.getRoleListOfUser(user.username);

        var dashboards = registry.content(registryPath(), {
            start: 0,
            count: 20
        });
        if (!dashboards) {
            return [];
        }
        var allDashboards = [];
        dashboards.forEach(function (dashboard) {
            allDashboards.push(JSON.parse(registry.content(dashboard)));
        });

        var userDashboards = [];
        allDashboards.forEach(function (dashboard) {
            var permissions = dashboard.permissions;
            if (allowed(userRoles, permissions.editors)) {
                userDashboards.push({
                    id: dashboard.id,
                    title: dashboard.title,
                    description: dashboard.description,
                    editable: true
                });
                return;
            }
            if (allowed(userRoles, permissions.viewers)) {
                userDashboards.push({
                    id: dashboard.id,
                    title: dashboard.title,
                    description: dashboard.description,
                    editable: false
                });
            }
        });
        return userDashboards;
    };

    findOne = function (type, id) {
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

    find = function (type, query, start, count) {
        if (type === 'dashboard') {
            return findDashboards(type, query, start, count);
        }
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

    create = function (type, asset) {
        var parent = new File(assetsDir(type));
        var file = new File(asset.id, parent);
        file.mkdir();
        file = new File(type + '.json', file);
        file.open('w');
        file.write(JSON.stringify(asset));
        file.close();
    };

    update = function (asset) {

    };

    remove = function (id) {

    };
}());