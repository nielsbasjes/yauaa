var pkg = require('./package.json');

module.exports = {
    // Documentation for Yauaa is stored under "src/main/docs"
    root: './main/docs',
    title: 'Yauaa: Yet Another UserAgent Analyzer',

    // Enforce use of GitBook v3
    gitbook: '3.2.3',

    // Use the "official" theme
    // plugins: ['theme-official@2.1.1', '-sharing', '-fontsettings', 'sitemap'],
    plugins: ['sitemap'],

    variables: {
        version: pkg.version,
        YauaaVersion: "5.7"
    },

    pluginsConfig: {
        sitemap: {
            hostname: 'https://yauaa.basjes.nl'
        }
    }
};
