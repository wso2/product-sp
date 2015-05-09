var opts = {
    from: 1234,
    to: 5678
};

module.exports.prepare = function (sandbox, hub) {

};

module.exports.create = function (sandbox, options, events, hub) {
    options = options || opts;
    var el = $(sandbox).datepicker(options);
    el.on('update', function (e) {
        hub.emit('country-selected', $(this).date());
    });
};

module.exports.update = function (sandbox, options, events, hub) {

};

module.exports.destroy = function (sandbox, hub) {
    $(sandbox).datepicker('destroy');
};