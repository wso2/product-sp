(function () {

    var gadgetPrefix = (osapi.container.GadgetHolder.IFRAME_ID_PREFIX_ = 'sandbox-');

    var containerPrefix = 'gadget-';

    var gadgets = {};

    var server = ues.global.server;

    var resolveURI = ues.dashboards.resolveURI;

    var context = ues.global.context;

    var resolveGadgetURL = function (uri) {
        var index = uri.indexOf('local://');
        if (index === -1) {
            return resolveURI(uri);
        }
        uri = uri.substring(index + 8);
        if (window.location.protocol === 'https:') {
            return 'https://localhost:' + server.httpsPort + context + '/' + uri;
        }
        return 'http://localhost:' + server.httpPort + context + '/' + uri;
    };

    var subscribeForClient = ues.hub.subscribeForClient;

    var containerId = function (id) {
        return containerPrefix + id;
    };

    var gadgetId = function (id) {
        return gadgetPrefix + containerPrefix + id;
    };

    ues.hub.subscribeForClient = function (container, topic, conSubId) {
        var clientId = container.getClientID();
        var data = gadgets[clientId];
        if (!data) {
            return subscribeForClient.apply(ues.hub, [container, topic, conSubId]);
        }
        var component = data.component;
        var channel = component.id + '.' + topic;
        console.log('subscribing container:%s topic:%s, channel:%s by %s', clientId, topic, channel);
        return subscribeForClient.apply(ues.hub, [container, channel, conSubId]);
    };

    var component = (ues.plugins.components['gadget'] = {});

    var createPanel = function (styles) {
        var html = '<div class="panel panel-default';
        if (!styles.borders) {
            html += ' ues-borderless';
        }
        html += '">';
        if (styles.title) {
            html += '<div class="panel-heading">';
            html += '<h3 class="panel-title ues-title-' + (styles.titlePosition) + '">' + styles.title + '</h3>';
            html += '</div>';
        }
        html += '<div class="panel-body"></div>';
        html += '</div>';
        return $(html);
    };

    component.create = function (sandbox, component, hub, done) {
        var content = component.content;
        var url = resolveGadgetURL(content.data.url);
        var settings = content.settings || {};
        var styles = content.styles || {};
        ues.gadgets.preload(url, function (err, metadata) {
            var pref;
            var opts = content.options || (content.options = {});
            var prefs = metadata.userPrefs;
            for (pref in prefs) {
                if (prefs.hasOwnProperty(pref)) {
                    pref = prefs[pref];
                    opts[pref.name] = {
                        type: pref.dataType,
                        title: pref.displayName,
                        value: pref.defaultValue,
                        options: pref.orderedEnumValues,
                        required: pref.required
                    };
                }
            }
            var cid = containerId(component.id);
            var gid = gadgetId(component.id);
            var panel = createPanel(styles);
            var container = $('<div id="' + cid + '" class="ues-component-box-gadget"></div>');
            container.appendTo(panel.find('.panel-body'));
            panel.appendTo(sandbox);
            var site = ues.gadgets.render(container, url);
            gadgets[gid] = {
                component: component,
                site: site
            };
            done(false, component);
        });
    };

    component.update = function (sandbox, component, hub, done) {

    };

    component.destroy = function (sandbox, component, hub, done) {
        var gid = gadgetId(component.id);
        var data = gadgets[gid];
        var site = data.site;
        ues.gadgets.remove(site.getId());
        done(false);
    };

}());