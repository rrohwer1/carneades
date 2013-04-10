var casper = require('casper').create();

console.log('url = ' + casper.cli.get('url'));

var test_scenario_entry = function() {
    this.test.assertEval(function() {
        return $('body p:first').text().match("is the policy") != null;
    }, 'Introduction page contains some text');


    casper.then(function() {
        this.click('#start');
    });

    casper.waitForSelector('#issues', test_scenario_issues);
};

var test_scenario_issues = function() {
    // console.log('New location is ' + this.getCurrentUrl());

    this.test.assertEval(function() {
        return $('body p:first').text().match('the following issues') != null;
    }, 'Issues page contains some text');

    casper.then(function() {
        this.click('#submit');
    });

    casper.waitForSelector('#facts', test_scenario_facts);

};

var test_scenario_facts = function() {
    // console.log('New location is ' + this.getCurrentUrl());

    this.test.assertEval(function() {
        return $('body p:first').text().match('be used for commercial') != null;
    }, 'The fact page contains a question about the use of the work');

    casper.then(function() {
        this.click('input[type=button]');
    });

    casper.waitForSelector('h3:nth-of-type(1)', test_scenario_facts_license);
    
};

var test_scenario_facts_license = function() {
    this.test.assertEval(function() {
        return $('p:nth(1)').text().match('existing license') != null;
    }, 'The fact page contains a question about the license');

    // selects 'no' for the license question
    casper.thenEvaluate(function(term) {
        document.querySelector('input[name="input-q2-2"][value="no"]').setAttribute('checked', true);
    });

    casper.then(function() {
        this.click('input[type=button]');
    });

    casper.waitForSelector('h3:nth-of-type(2)', test_scenario_facts_search);
};

var test_scenario_facts_search = function() {
    this.test.assertEval(function() {
        return $('p:nth(2)').text().match('copyright') != null;
    }, 'The fact page contains a question about the copyright owner');

    this.test.assertEval(function() {
        return $('p:nth(3)').text().match('announcement') != null;
    }, 'The fact page contains a question about an announcement');

    // selects 'yes' for the announcement question
    casper.thenEvaluate(function(term) {
        document.querySelector('input[name="input-q4-5"][value="yes"]').setAttribute('checked', true);
    });

    casper.then(function() {
        this.click('input[type=button]');
    });

    casper.waitForSelector('#argumentgraph', test_scenario_outline);
};

var test_scenario_outline = function() {
    this.test.assertEval(function() {
        return $('p:first').text() == "☐ The person may publish the work.";
    }, 'The outline page shows that the main issue is not acceptable');
};

casper.start(casper.cli.get('url'), function() {
    casper.waitForSelector('#mainmenu', function() {
        test_scenario_entry.call(this);
    });
});

casper.run(function() {
               this.test.renderResults(true);
           });
