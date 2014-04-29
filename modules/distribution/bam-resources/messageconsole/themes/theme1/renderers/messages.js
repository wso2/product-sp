var render = function (theme, data, meta, require) {
    theme('index', {
        body: [
            {
                partial: 'messages',
                context: data.messages
            }
        ],
        header: [
            {
                partial: 'header',
                 context : require('/helpers/header.js').getNavigation(data.header.current)
            }
        ]
    });
};