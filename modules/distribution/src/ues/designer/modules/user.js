var current = function () {
    login('admin');
    return session.get('user');
};

var login = function (username, password) {
    session.put('user', {
        username: username,
        tenantId: -1234
    });
    return true;
};

var logout = function () {
    session.remove('user');
};

var authorized = function (perm, action) {
    return true;
};