var casper = require('casper').create();

console.log('url = ' + casper.cli.get('url'));

var test_scenario_entry = function() {
    this.test.assertEval(function() {
        return $('body p:first').text().match("is the policy") != null;
    }, 'Introduction page contains some text');


    casper.then(function() {
        this.click('#start');
    });

    casper.waitForSelector('#issues', function() {
        test_scenario_issues.call(this);
    });
};

var test_scenario_issues = function() {
    // console.log('New location is ' + this.getCurrentUrl());

    this.test.assertEval(function() {
        return $('body p:first').text().match('the following issues') != null;
    }, 'Issues page contains some text');

    casper.then(function() {
        this.click('#submit');
    });

    casper.waitForSelector('#facts', function() {
        test_scenario_facts.call(this);
    });
};

var test_scenario_facts = function() {
    // console.log('New location is ' + this.getCurrentUrl());

    this.test.assertEval(function() {
        return $('body p:first').text().match('be used for commercial') != null;
    }, 'The fact page contains a question and it is about the use of the work');

    // casper.then(function() {
    //     this.click('#submit');
    // });

    // casper.waitForSelector('#facts', function() {
    //     test_scenario_facts.call(this);
    // });
};

casper.start(casper.cli.get('url'), function() {
    casper.waitForSelector('#mainmenu', function() {
        test_scenario_entry.call(this);
    });
});

casper.run(function() {
               this.test.renderResults(true);
           });
