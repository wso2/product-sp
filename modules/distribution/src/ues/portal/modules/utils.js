var log = new Log();

var relativePrefix = function (path) {
    var parts = path.split('/');
    var prefix = '';
    var i;
    var count = parts.length - 3;
    for (i = 0; i < count; i++) {
        prefix += '../';
    }
    return prefix;
};

var sandbox = function (options, fn) {
    var carbon = require('carbon');
    options.tenantId = carbon.server.tenantId(options);
    options.tenantId = options.tenantId || carbon.server.tenantId();
    carbon.server.sandbox(options, fn);
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

var context = function (user, domain) {
    if (user) {
        user.secured = true;
        return user;
    }
    return {
        domain: domain
    };
};