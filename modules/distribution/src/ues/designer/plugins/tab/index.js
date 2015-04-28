//csj
ues.plugin('tab', function (plugin, data) {
    plugin.html = function () {

    };
    plugin.js = function (el, data) {

    };
    plugin.css = function () {

    };
    el.html(ues.hbs('index.hbs', data));
    el.on('click', 'a', function (e) {
        e.preventDefault();
        $(this).tab('show');
    });
    $('a:first', el).tab('show');
});

//ssj or index.js
ues.plugin('tag', 'html', function (el, data, ues) {
    el.html(ues.render('index.hbs', data));
    el.html('<h1>' + data.name + '</h1>');
    el.on('click', 'a', function (e) {
        e.preventDefault();
        $(this).tab('show');
    });
    $('a:first', el).tab('show');
});

//ssj or index.js
ues.plugin('tag', 'js', function (el) {
    return ''
});

//ssj or index.css
ues.plugin('tag', 'css', function () {

});