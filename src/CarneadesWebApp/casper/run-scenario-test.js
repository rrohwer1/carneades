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

    casper.then(function() {
        this.click('#schemes-item');
    });

    casper.waitForSelector('.theory-view', test_scenario_schemes);

};

var test_scenario_schemes = function() {
    this.test.assertEval(function() {
        return $('#outline li:first a').text().match('Argument from') != null;
    }, 'There is a least one scheme on the schemes page named Argument from..');

    casper.then(function() {
        this.click('#policies-item');
    });

    casper.waitForSelector('.policies-filtering', test_scenario_policies);
};

var test_scenario_policies = function() {
    casper.then(function() {
        this.click('#in');
        this.waitWhileSelector('#UrhG', test_scenario_policies_aktionbundnis);
    });

};

var test_scenario_policies_aktionbundnis = function() {
    this.test.assertEval(function() {
        return $('input[type=submit]').length == 1;
    }, 'Only one policy makes the issue acceptable');

    casper.then(function() {
        this.click('#inputQ12-Aktionsbundnis');
    });

    casper.waitForSelector('#argumentgraph', 
                           test_scenario_outline_after_eval,
                           function() {
                               this.fail('Selecting a policy does not work');
                           },
                           10000);
};

var test_scenario_outline_after_eval = function() {
    this.test.assertEval(function() {
        return $('p:first').text() == "☑ The person may publish the work.";
    }, 'The outline page shows that the main issue is now acceptable');
};

// TODO test changing answer to license
// TODO test validating UhRG
// TODO test issue is in
// TODO test creating a statement
// TODO test creating an argument
// TODO test reading the general map
// TODO test copy / export / vote / evaluate


casper.start(casper.cli.get('url'), function() {
    casper.waitForSelector('#mainmenu', function() {
        test_scenario_entry.call(this);
    });
});

casper.run(function() {
               this.test.renderResults(true);
           });
