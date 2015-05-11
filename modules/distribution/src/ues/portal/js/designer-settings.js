$(function () {

    var rolesApi = ues.utils.relativePrefix() + 'apis/roles';

    var dashboard = ues.global.dashboard;

    var permissions = dashboard.permissions;

    var viewers = permissions.viewers;

    var editors = permissions.editors;

    var saveDashboard = ues.dashboards.save;

    var sharedRoleHbs = Handlebars.compile($("#shared-role-hbs").html());

    var viewer = function (el, role) {
        var permissions = dashboard.permissions;
        var viewers = permissions.viewers;
        viewers.push(role);
        saveDashboard();
        $('.ues-settings .ues-shared-view').append(sharedRoleHbs(role));
        el.typeahead('val', '');
    };

    var editor = function (el, role) {
        var permissions = dashboard.permissions;
        var editors = permissions.editors;
        editors.push(role);
        saveDashboard();
        $('.ues-settings .ues-shared-edit').append(sharedRoleHbs(role));
        el.typeahead('val', '');
    };

    var initExistingRoles = function () {
        var i;
        var role;

        var html = '';
        var length = viewers.length;
        for (i = 0; i < length; i++) {
            role = viewers[i];
            html += sharedRoleHbs(role);
        }
        $('.ues-settings .ues-shared-view').append(html);

        html = '';
        length = editors.length;
        for (i = 0; i < length; i++) {
            role = editors[i];
            html += sharedRoleHbs(role);
        }
        $('.ues-settings .ues-shared-edit').append(html);
    };

    var initTypeahead = function () {
        var engine = new Bloodhound({
            name: 'roles',
            limit: 10,
            prefetch: {
                url: rolesApi + '?q=%QUERY',
                filter: function (roles) {
                    console.log(roles);
                    return $.map(roles, function (role) {
                        return {name: role};
                    });
                },
                ttl: 60
            },
            datumTokenizer: function (d) {
                return d.name.split(/[\s\/]+/) || [];
            },
            queryTokenizer: Bloodhound.tokenizers.whitespace
        });

        engine.initialize();

        //TODO: handle autocompletion and check clearing
        $('#ues-share-view').typeahead(null, {
            name: 'roles',
            displayKey: 'name',
            source: engine.ttAdapter()
        }).on('typeahead:selected', function (e, role, roles) {
            viewer($(this), role.name);
        }).on('typeahead:autocomplete', function (e, role) {
            viewer($(this), role.name);
        });

        $('#ues-share-edit').typeahead(null, {
            name: 'roles',
            displayKey: 'name',
            source: engine.ttAdapter()
        }).on('typeahead:selected', function (e, role, roles) {
            editor($(this), role.name);
        }).on('typeahead:autocomplete', function (e, role) {
            editor($(this), role.name);
        });

        $('#settings').find('.ues-shared-edit').on('click', '.remove-button', function () {
            var el = $(this).closest('.ues-shared-role');
            var role = el.data('role');
            editors.splice(editors.indexOf(role), 1);
            saveDashboard();
            el.remove();
        }).end().find('.ues-shared-view').on('click', '.remove-button', function () {
            var el = $(this).closest('.ues-shared-role');
            var role = el.data('role');
            viewers.splice(viewers.indexOf(role), 1);
            saveDashboard();
            el.remove();
        });
    };

    initTypeahead();
    initExistingRoles();
});