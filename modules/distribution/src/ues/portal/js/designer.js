$(function () {
    //TODO: cleanup this

    var dashboardsApi = ues.utils.relativePrefix() + 'apis/dashboards';

    var dashboardsUrl = ues.utils.relativePrefix() + 'dashboards';

    var dashboard;

    var page;

    var freshDashboard = true;

    var storeCache = {};

    var resolveURI = ues.dashboards.resolveURI;

    Handlebars.registerHelper('has', function () {
        var has = function (o) {
            if (!o) {
                return false;
            }
            if (o instanceof Array && !o.length) {
                return false;
            }
            var key;
            for (key in o) {
                if (o.hasOwnProperty(key)) {
                    return true;
                }
            }
            return false;
        };
        var args = Array.prototype.slice.call(arguments);
        var options = args.pop();
        var length = args.length;
        if (!length) {
            return new Handlebars.SafeString(options.inverse(this));
        }
        var i;
        for (i = 0; i < length; i++) {
            if (has(args[i])) {
                return new Handlebars.SafeString(options.fn(this));
            }
        }
        return new Handlebars.SafeString(options.inverse(this));
    });

    Handlebars.registerHelper('equals', function (left, right, options) {
        if (left === right) {
            return options.fn(this);
        }
        return options.inverse(this);
    });

    Handlebars.registerHelper('dump', function (o) {
        return JSON.stringify(o);
    });

    Handlebars.registerHelper('resolveURI', function (path) {
        return ues.dashboards.resolveURI(path);
    });

    var layoutsListHbs = Handlebars.compile($("#layouts-list-hbs").html());

    var layoutHbs = Handlebars.compile($("#layout-hbs").html());

    var componentsListHbs = Handlebars.compile($("#components-list-hbs").html());

    var componentToolbarHbs = Handlebars.compile($("#component-toolbar-hbs").html());

    var pageOptionsHbs = Handlebars.compile($("#page-options-hbs").html());

    var componentPropertiesHbs = Handlebars.compile($("#component-properties-hbs").html());

    var randomId = function () {
        return Math.random().toString(36).slice(2);
    };

    var findStoreCache = function (type, id) {
        var i;
        var item;
        var items = storeCache[type];
        var length = items.length;
        for (i = 0; i < length; i++) {
            item = items[i];
            if (item.id === id) {
                return item;
            }
        }
    };

    var findComponent = function (id) {
        var i;
        var length;
        var area;
        var component;
        var components;
        var content = page.content;
        for (area in content) {
            if (content.hasOwnProperty(area)) {
                components = content[area];
                length = components.length;
                for (i = 0; i < length; i++) {
                    component = components[i];
                    if (component.id === id) {
                        return component;
                    }
                }
            }
        }
    };

    var saveComponentProperties = function (id, data) {
        var o;
        var opt;
        var block = findComponent(id);
        var content = block.content;

        //save options
        var options = content.options;
        var opts = data.options;
        for (opt in opts) {
            if (opts.hasOwnProperty(opt)) {
                o = options[opt] || (options[opt] = {});
                o.value = opts[opt];
            }
        }

        //save settings
        content.settings = data.settings;

        //save styles
        content.styles = data.styles;

        //save wiring
        var event;
        var listener;
        var notifiers = data.notifiers;
        var listen = content.listen;
        for (event in notifiers) {
            if (notifiers.hasOwnProperty(event)) {
                listener = listen[event];
                listener.on = notifiers[event];
            }
        }

        ues.dashboards.rewire(page);
        updateComponent(id);
        saveDashboard();
    };

    var removeComponent = function (component, done) {
        destroyComponent(component, function (err) {
            if (err) {
                return done(err);
            }
            var container = $('#' + component.id);
            var area = container.closest('.ues-component-box').attr('id');
            var content = page.content;
            area = content[area];
            var index = area.indexOf(component);
            area.splice(index, 1);
            container.remove();

            var el = $('#middle').find('.ues-designer .ues-properties');
            var oid = el.find('.ues-sandbox').data('id');
            if (oid !== component.id) {
                return done();
            }
            el.empty();
            done();
        });
    };

    var destroyComponent = function (component, done) {
        ues.components.destroy(component, function (err) {
            if (err) {
                return err;
            }
            done(err);
        });
    };

    var destroyArea = function (components, done) {
        var i;
        var length = components.length;
        var tasks = [];
        for (i = 0; i < length; i++) {
            tasks.push((function (component) {
                return function (done) {
                    destroyComponent(component, function (err) {
                        done(err);
                    });
                };
            }(components[i])));
        }
        async.parallel(tasks, function (err, results) {
            done(err);
        });
    };

    var destroyPage = function (page, done) {
        var area;
        var content = page.content;
        var tasks = [];
        for (area in content) {
            if (content.hasOwnProperty(area)) {
                tasks.push((function (area) {
                    return function (done) {
                        destroyArea(area, function (err) {
                            done(err);
                        });
                    };
                }(content[area])));
            }
        }
        async.parallel(tasks, function (err, results) {
            $('#middle').find('.ues-designer .ues-layout').empty();
            done(err);
        });
    };

    var removePage = function (page, done) {
        var pages = dashboard.pages;
        var index = pages.indexOf(page);
        pages.splice(index, 1);
        destroyPage(page, done);
    };

    var previewDashboard = function (page) {
        window.open(dashboardsUrl + '/' + dashboard.id + '/' + page.id, '_blank');
    };

    var exportDashboard = function (page) {
        window.open(dashboardsApi + '/' + dashboard.id, '_blank');
    };

    var saveDashboard = function () {
        var method;
        var url;
        if (freshDashboard) {
            freshDashboard = false;
            method = 'POST';
            url = dashboardsApi;
        } else {
            method = 'PUT';
            url = dashboardsApi + '/' + dashboard.id;
        }
        $.ajax({
            url: url,
            method: method,
            data: JSON.stringify(dashboard),
            contentType: 'application/json'
        }).success(function (data) {
            console.log('dashboard saved successfully');
        }).error(function () {
            console.log('error saving dashboard');
        });
    };

    var initComponentToolbar = function () {
        var designer = $('#middle').find('.ues-designer');
        designer.on('click', '.ues-component .ues-toolbar .ues-properties-handle', function () {
            var id = $(this).closest('.ues-component').attr('id');
            renderComponentProperties(findComponent(id));
        });
        designer.on('click', '.ues-component .ues-toolbar .ues-trash-handle', function () {
            var id = $(this).closest('.ues-component').attr('id');
            removeComponent(findComponent(id), function (err) {
                if (err) {
                    console.error(err);
                }
                saveDashboard();
            });
        });
        designer.on('mouseenter', '.ues-component .ues-toolbar .ues-move-handle', function () {
            $(this).draggable({
                cancel: false,
                appendTo: 'body',
                helper: 'clone',
                start: function (event, ui) {
                    console.log('dragging');
                },
                stop: function () {
                    //$('#left a[href="#components"]').tab('show');
                }
            });
        }).on('mouseleave', '.ues-component .ues-toolbar .ues-move-handle', function () {
            $(this).draggable('destroy');
        });
    };

    var renderComponentToolbar = function (component) {
        var el = $('#' + component.id).prepend($(componentToolbarHbs(component)));
        $('[data-toggle="tooltip"]', el).tooltip();
    };

    var updateStyles = function (asset) {
        var styles = asset.styles || (asset.styles = {
                title: true,
                borders: true
            });
        if (styles.title && typeof styles.title === 'boolean') {
            styles.title = asset.title;
        }
    };

    var createComponent = function (container, asset) {
        var id = randomId();
        //TODO: remove hardcoded gadget
        var area = container.attr('id');
        var content = page.content;
        content = content[area] || (content[area] = []);
        updateStyles(asset);
        var component = {
            id: id,
            content: asset
        };
        content.push(component);
        ues.components.create(container, component, function (err, block) {
            if (err) {
                throw err;
            }
            renderComponentToolbar(component);
            renderComponentProperties(component);
            saveDashboard();
        });
    };

    var moveComponent = function (container, id) {
        var component = findComponent(id);
        var area = container.attr('id');
        var content = page.content;
        content = content[area] || (content[area] = []);
        content.push(component);
        removeComponent(component, function (err) {
            if (err) {
                throw err;
            }
            ues.components.create(container, component, function (err, block) {
                if (err) {
                    throw err;
                }
                renderComponentToolbar(component);
                renderComponentProperties(component);
                saveDashboard();
            });
        });
    };

    var updateComponent = function (id) {
        ues.components.update(findComponent(id), function (err) {
            if (err) {
                throw err;
            }
        });
    };

    var componentNotifiers = function (notifiers, current, component) {
        if (current.id === component.id) {
            return;
        }
        var notify = component.content.notify;
        if (!notify) {
            return;
        }
        var event;
        var events;
        var data;
        for (event in notify) {
            if (notify.hasOwnProperty(event)) {
                data = notify[event];
                events = notifiers[data.type] || (notifiers[data.type] = []);
                events.push({
                    from: component.id,
                    event: event,
                    type: data.type,
                    content: component.content,
                    description: data.description
                });
            }
        }
    };

    var areaNotifiers = function (notifiers, component, components) {
        var i;
        var length = components.length;
        for (i = 0; i < length; i++) {
            componentNotifiers(notifiers, component, components[i]);
        }
    };

    var pageNotifiers = function (component, page) {
        var area;
        var notifiers = {};
        var content = page.content;
        for (area in content) {
            if (content.hasOwnProperty(area)) {
                areaNotifiers(notifiers, component, content[area]);
            }
        }
        return notifiers;
    };

    var findNotifiers = function (component, page) {
        var event, listener, notifiers;
        var listeners = [];
        var content = component.content;
        var listen = content.listen;
        if (!listen) {
            return listeners;
        }
        notifiers = pageNotifiers(component, page);
        for (event in listen) {
            if (listen.hasOwnProperty(event)) {
                listener = listen[event];
                listeners.push({
                    event: event,
                    title: listener.title,
                    description: listener.description,
                    notifiers: notifiers[listener.type] || []
                });
            }
        }
        console.log(listeners);
        return listeners;
    };

    var wiredNotifier = function (from, event, notifiers) {
        var i, notifier;
        var length = notifiers.length;
        for (i = 0; i < length; i++) {
            notifier = notifiers[i];
            if (notifier.from === from && notifier.event === event) {
                return notifier;
            }
        }
    };

    var wireEvent = function (on, notifiers) {
        var i, notifier;
        var length = on.length;
        for (i = 0; i < length; i++) {
            notifier = on[i];
            notifier = wiredNotifier(notifier.from, notifier.event, notifiers);
            if (!notifier) {
                continue;
            }
            notifier.wired = true;
        }
    };

    var eventNotifiers = function (event, notifiers) {
        var i, events;
        var length = notifiers.length;
        for (i = 0; i < length; i++) {
            events = notifiers[i];
            if (events.event === event) {
                return events.notifiers;
            }
        }
    };

    var wireEvents = function (component, notifiers) {
        var listen = component.content.listen;
        if (!listen) {
            return notifiers;
        }
        var event, on;
        for (event in listen) {
            if (listen.hasOwnProperty(event)) {
                on = listen[event].on;
                if (!on) {
                    continue;
                }
                wireEvent(on, eventNotifiers(event, notifiers));
            }
        }
        return notifiers;
    };

    var buildPropertiesContext = function (component, page) {
        var notifiers = findNotifiers(component, page);
        var content = component.content;
        return {
            id: component.id,
            title: content.title,
            options: content.options,
            styles: content.styles,
            settings: content.settings,
            listeners: wireEvents(component, notifiers)
        };
    };

    var updatePageOptions = function (sandbox) {
        var id = $('.id', sandbox).val();
        var title = $('.title', sandbox).val();
        var landing = $('.landing', sandbox);
        var link = $('#middle').find('.ues-designer').children('.ues-toolbar')
            .find('.ues-pages a[data-id="' + page.id + '"]');

        link.data('id', id);
        link.text(title);
        page.id = id;
        page.title = title;
        if (landing.is(':checked')) {
            dashboard.landing = id;
        }

        saveDashboard();
    };

    var renderPageOptions = function (page) {
        $('#middle').find('.ues-designer .ues-properties').html(pageOptionsHbs({
            id: page.id,
            title: page.title
        })).find('.ues-sandbox').on('change', 'input', function () {
            updatePageOptions($(this).closest('.ues-sandbox'));
        });
    };

    var saveOptions = function (sandbox, options) {
        $('.ues-options input', sandbox).each(function () {
            var el = $(this);
            options[el.attr('name')] = el.val();
        });
        $('.ues-options textarea', sandbox).each(function () {
            var el = $(this);
            options[el.attr('name')] = el.val();
        });
    };

    var saveSettings = function (sandbox, settings) {
        $('.ues-settings input', sandbox).each(function () {
            var el = $(this);
            var type = el.attr('type');
            var name = el.attr('name');
            if (type === 'text') {
                settings[name] = el.val();
                return;
            }
            if (type === 'checkbox') {
                settings[name] = el.is(':checked');
            }
        });
    };

    var saveStyles = function (sandbox, styles) {
        $('.ues-styles input', sandbox).each(function () {
            var el = $(this);
            var type = el.attr('type');
            var name = el.attr('name');
            if (type === 'text') {
                styles[name] = el.val();
                return;
            }
            if (type === 'checkbox') {
                styles[name] = el.is(':checked');
            }
        });

        styles.titlePosition = $('.ues-styles .ues-title-position', sandbox).val();
    };

    var saveNotifiers = function (sandbox, notifiers) {
        $('.ues-notifiers .notifier', sandbox).each(function () {
            var el = $(this);
            var from = el.data('from');
            var event = el.data('event');
            var listener = el.closest('.listener').data('event');
            var events = notifiers[listener] || (notifiers[listener] = []);
            if (!el.is(':checked')) {
                return;
            }
            events.push({
                from: from,
                event: event
            });
        });
    };

    var updateComponentProperties = function (sandbox) {
        var notifiers = {};
        var options = {};
        var settings = {};
        var styles = {};
        var id = sandbox.data('id');

        saveOptions(sandbox, options);
        saveSettings(sandbox, settings);
        saveStyles(sandbox, styles);
        saveNotifiers(sandbox, notifiers);

        saveComponentProperties(id, {
            options: options,
            settings: settings,
            styles: styles,
            notifiers: notifiers
        });
    };

    var renderComponentProperties = function (component) {
        var ctx = buildPropertiesContext(component, page);
        var el = $('#middle').find('.ues-designer .ues-properties').html(componentPropertiesHbs(ctx))
            .find('.ues-sandbox').on('change', 'input, select, textarea', function () {
                updateComponentProperties($(this).closest('.ues-sandbox'));
            });
        $('[data-toggle="tooltip"]', el).tooltip();
    };

    var loadAssets = function (type, start, count) {
        ues.store.assets(type, {
            start: start,
            count: count
        }, function (err, data) {
            storeCache.gadget = data;
            $('#middle').find('.ues-components .ues-content').html(componentsListHbs(data));
        });
    };

    var initComponents = function () {
        $('.ues-components').on('mouseenter', '.thumbnail .ues-drag-handle', function () {
            $(this).draggable({
                cancel: false,
                appendTo: 'body',
                helper: 'clone',
                start: function (event, ui) {
                    console.log('dragging');
                    $('#left').find('a[href="#designer"]').tab('show');
                },
                stop: function () {
                    //$('#left a[href="#components"]').tab('show');
                }
            });
        }).on('mouseleave', '.thumbnail .ues-drag-handle', function () {
            $(this).draggable('destroy');
        });
    };

    var initLeftMenu = function () {
        $('#left')
            .find('.nav-tabs a')
            .click(function (e) {
                e.preventDefault();
                var el = $(this);
                el.tab('show');
            });
    };

    var initComponentsStore = function () {
        var toolbar = $('#middle').find('.ues-components .ues-toolbar');
        $('.ues-gadgets', toolbar).on('click', function () {
            loadAssets('gadget', 0, 20);
        });
        $('.ues-widgets', toolbar).on('click', function () {
            loadAssets('widget', 0, 20);
        });
        loadAssets('gadget', 0, 20);
    };

    var initUI = function () {
        initLeftMenu();
        initComponentToolbar();
        initComponentsStore();
        initComponents();
    };

    var listenLayout = function () {
        $('#middle').find('.ues-designer')
            .children('.ues-toolbar')
            .find('.ues-page').on('click', function () {
                destroyPage(page, function (err) {
                    if (err) {
                        return console.error(err);
                    }
                    initPage();
                });
            }).end()
            .find('.ues-preview').on('click', function () {
                previewDashboard(page);
            }).end()
            .find('.ues-export').on('click', function () {
                exportDashboard(page);
            }).end()
            .find('.ues-settings').on('click', function () {
                renderPageOptions(page);
            }).end()
            .find('.ues-remove').on('click', function () {
                removePage(page, function (err) {
                    if (err) {
                        return console.error(err);
                    }
                    var landing = dashboard.landing;
                    if (landing !== page.id) {
                        return renderPage(landing);
                    }
                    var pages = dashboard.pages;
                    var p = pages[0];
                    if (p) {
                        landing = (dashboard.landing = p.id);
                        return renderPage(landing);
                    }
                    dashboard.landing = null;
                    initPage('landing');
                });
            }).end()
            .find('.ues-pages').on('click', 'a', function () {
                var next = $(this).data('id');
                switchPage(next);
            }).end()
            .end()
            .find('.ues-component-box').droppable({
                //activeClass: 'ui-state-default',
                hoverClass: 'ui-state-hover',
                //accept: ':not(.ui-sortable-helper)',
                drop: function (event, ui) {
                    //$(this).find('.placeholder').remove();
                    var id = ui.helper.data('id');
                    var action = ui.helper.data('action');
                    var el = $(this);
                    switch (action) {
                        case 'move':
                            moveComponent(el, id);
                            break;
                        default:
                            createComponent(el, findStoreCache('gadget', id));
                    }
                }
            });
    };

    var layoutContainer = function () {
        return $('#middle').find('.ues-designer').html(layoutHbs({
            pages: dashboard.pages,
            current: page
        })).find('.ues-layout');
    };

    var createPage = function (options, lid) {
        var layout = findStoreCache('layout', lid);
        $.get(resolveURI(layout.url), function (data) {
            var id = options.id;
            layout.content = data;
            currentPage({
                id: id,
                title: options.title,
                layout: layout,
                content: {}
            });
            dashboard.landing = dashboard.landing || id;
            dashboard.pages.push(page);
            saveDashboard();
            var container = layoutContainer();
            ues.dashboards.render(container, dashboard, id, function () {
                listenLayout();
            });
        }, 'html');
    };

    var currentPage = function (p) {
        return page = (ues.global.page = p);
    };

    var switchPage = function (pid) {
        if (!page) {
            return renderPage(pid);
        }
        destroyPage(page, function (err) {
            if (err) {
                throw err;
            }
            renderPage(pid);
        });
    };

    var renderPage = function (pid) {
        currentPage(ues.dashboards.findPage(dashboard, pid));
        if (!page) {
            throw 'specified page : ' + pid + ' cannot be found';
        }
        var container = layoutContainer();
        ues.dashboards.render(container, dashboard, pid, function () {
            $('#middle').find('.ues-designer .ues-component').each(function () {
                var id = $(this).attr('id');
                renderComponentToolbar(findComponent(id));
            });
            listenLayout();
        });
    };

    var pageOptions = function (type) {
        switch (type) {
            case 'landing':
                return {
                    id: 'landing',
                    title: 'My Dashboard'
                };
            case 'login':
                return {
                    id: 'login',
                    title: 'Login'
                };
            default:
                var i;
                var pid = 0;
                var prefix = 'page';
                var titlePrefix = 'Page ';
                var page = prefix + pid;
                var pages = dashboard.pages;
                var length = pages.length;
                for (i = 0; i < length; i++) {
                    if (pages[i].id === page) {
                        pid++;
                        page = prefix + pid;
                    }
                }
                return {
                    id: page,
                    title: titlePrefix + pid
                };
        }
    };

    var initPage = function (type) {
        ues.store.assets('layout', {
            start: 0,
            count: 20
        }, function (err, data) {
            storeCache.layout = data;
            $('#middle')
                .find('.ues-designer .ues-content').html(layoutsListHbs(data))
                .on('click', '.thumbnails .ues-add', function () {
                    createPage(pageOptions(type), $(this).data('id'));
                });
        });
    };

    var initDashboard = function (db, page, fresh) {
        freshDashboard = fresh;
        dashboard = (ues.global.dashboard = db);
        fresh ? initPage('landing') : renderPage(page || db.landing);
    };

    initUI();
    initDashboard(ues.global.dashboard, ues.global.page, ues.global.fresh);

    ues.dashboards.save = saveDashboard;

});