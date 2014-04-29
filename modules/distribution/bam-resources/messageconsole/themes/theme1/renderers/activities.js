var render = function (theme, data, meta, require) {
    theme('index', {
        body: [
            {
                partial: 'activities',
                context: data.activities
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