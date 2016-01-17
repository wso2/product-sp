(function () {

    var DEFAULT_PADDING = 35;

    var gadgetPrefix = (osapi.container.GadgetHolder.IFRAME_ID_PREFIX_ = 'sandbox-');

    var containerPrefix = 'gadget-';

    var gadgets = {};

    var server = ues.global.server;

    var resolveURI = ues.dashboards.resolveURI;

    var context = ues.global.context;

    var resolveGadgetURL = function (uri) {
        uri = resolveURI(uri);
        if (uri.match(/^https?:\/\//i)) {
            return uri;
        }
        uri = uri.replace(/^(..\/)*/i, '');
        if (window.location.protocol === 'https:') {
            return 'https://' + window.location.hostname + ":" + server.httpsPort + context + '/' + uri;
        }
        return 'http://' + window.location.hostname + ":" + server.httpPort + context + '/' + uri;
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
        var html = '<div class="panel panel-default ues-component-box-gadget';
        if (!styles.borders) {
            html += ' ues-borderless';
        }
        html += '"';
        if (styles.height) {
            html += ' style="height:' + styles.height + 'px"';
        }
        html += '>';
        if (styles.title) {
            html += '<div class="panel-heading">';
            html += '<h3 class="panel-title ues-title-' + (styles.titlePosition) + '">' + styles.title + '</h3>';
            html += '</div>';
        }
        html += '<div class="panel-body"></div>';
        html += '</div>';
        return $(html);
    };
    
    var hasCustomUserPrefView = function (metadata, comp) {
        if(metadata.views.hasOwnProperty('settings')){
            comp.hasCustomUserPrefView= true;
        }
    };

    var hasCustomFullView = function (metadata, comp) {
        if(metadata.views.hasOwnProperty('full')){
            comp.hasCustomFullView= true;
        }
    };

    var loadLocalizedTitle = function (styles, comp) {
        var userLang = navigator.languages ? navigator.languages[0] : (navigator.language || navigator.userLanguage || navigator.browserLanguage);
        var locale_titles = comp.content.locale_titles || {};
        styles.title = locale_titles[userLang] || comp.content.title;
        comp.content.locale_titles = locale_titles || {};
    };

    component.create = function (sandbox, comp, hub, done) {
        var content = comp.content;
        var url = resolveGadgetURL(content.data.url);
        var settings = content.settings || {};
        var styles = content.styles || {};
        var options = content.options || (content.options = {});
        ues.gadgets.preload(url, function (err, metadata) {
            var pref;
            var name;
            var option;
            var params = {};
            var prefs = metadata.userPrefs;
            for (pref in prefs) {
                if (prefs.hasOwnProperty(pref)) {
                    pref = prefs[pref];
                    name = pref.name;
                    option = options[name] || {};
                    options[name] = {
                        type: option.type || pref.dataType,
                        title: option.title || pref.displayName,
                        value: option.value || pref.defaultValue,
                        options: option.options || pref.orderedEnumValues,
                        required: option.required || pref.required
                    };
                    params[name] = option.value;
                }
            }
            loadLocalizedTitle(styles, comp);
            var cid = containerId(comp.id);
            var gid = gadgetId(comp.id);
            var panel = createPanel(styles);
            
            var compHeight = $('#ues-designer').height() - 220;
            var height = (comp.viewOption && comp.viewOption == 'full' ? compHeight : '');

            if (ues.global.dbType === 'default'){
                hasCustomUserPrefView(metadata, comp);
                hasCustomFullView(metadata, comp);
            }
            
            var container = $('<div />').attr('id', cid);
            if (height) {
                container.css('height', height + 'px');
            }
            container.appendTo(panel.find('.panel-body'));
            panel.appendTo(sandbox);
            var renderParams = {};
            if (styles.height) {
                renderParams[osapi.container.RenderParam.HEIGHT] = parseInt(styles.height, 10) - DEFAULT_PADDING;
            }
            renderParams[osapi.container.RenderParam.VIEW] = comp.viewOption || 'home';
            var site = ues.gadgets.render(container, url, params, renderParams);
            gadgets[gid] = {
                component: comp,
                site: site
            };
            done(false, comp);
        });
    };

    component.update = function (sandbox, comp, hub, done) {
        component.destroy(sandbox, comp, hub, function (err) {
            if (err) {
                throw err;
            }
            component.create(sandbox, comp, hub, done);
        });
    };

    component.destroy = function (sandbox, comp, hub, done) {
        var gid = gadgetId(comp.id);
        var data = gadgets[gid];
        var site = data.site;
        ues.gadgets.remove(site.getId());
        $('.ues-component-box-gadget', sandbox).remove();
        done(false);
    };

}());