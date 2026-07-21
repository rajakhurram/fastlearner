process.env.CHROME_BIN = require('puppeteer').executablePath();

module.exports = function (config) {
    config.set({
        basePath: '',
        frameworks: ['jasmine', '@angular-devkit/build-angular'],
        plugins: [
            require('karma-jasmine'),
            require('karma-coverage'),
            require('karma-chrome-launcher'),
            require('karma-jasmine-html-reporter'),
            require('@angular-devkit/build-angular/plugins/karma')
        ],
        client: {
            clearContext: false, // leave Jasmine Spec Runner output visible in browser
            jasmine: {
                // Full suite has heavy component specs; avoid false timeouts.
                timeoutInterval: 30000,
            },
        },
        coverageReporter: {
            dir: require('path').join(__dirname, 'coverage'),
            subdir: '.',
            reporters: [
                { type: 'html' },
                { type: 'lcov' },
                { type: 'text-summary' }
            ]
        },
        reporters: ['progress', 'kjhtml'],
        port: 9876,
        colors: true,
        logLevel: config.LOG_INFO,
        autoWatch: true,
        browsers: ['ChromeHeadlessNoSandbox'],
        concurrency: 1,
        // Defaults are too aggressive for ~2500 tests on Windows (ping timeout ~2s).
        pingTimeout: 120000,
        browserDisconnectTimeout: 120000,
        browserDisconnectTolerance: 3,
        browserNoActivityTimeout: 300000,
        captureTimeout: 180000,
        processKillTimeout: 20000,
        singleRun: true,
        restartOnFileChange: true,
        customLaunchers: {
            ChromeHeadlessNoSandbox: {
                base: 'ChromeHeadless',
                flags: [
                    '--no-sandbox',
                    '--disable-setuid-sandbox',
                    '--disable-dev-shm-usage',
                    '--disable-gpu',
                    '--disable-background-timer-throttling',
                    '--disable-backgrounding-occluded-windows',
                    '--disable-renderer-backgrounding',
                ]
            }
        }
    });
};
