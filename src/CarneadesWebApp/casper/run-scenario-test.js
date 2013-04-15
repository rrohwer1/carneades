var casper = require('casper').create({waitTimeout: 6000});

console.log('url = ' + casper.cli.get('url'));

var test_timeout = function () {
    take_picture.call(this);
    this.fail('Timeout!');
};

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
    // casper.then(function() {
    //     this.click('#newstatement');
    // });
    // casper.waitForSelector('#save-statement', test_scenario_newstatement);
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

    casper.then(function() {
        this.click('#facts-item');
    });

    casper.waitForText('Modify the facts.', test_scenario_change_facts);

};

var test_scenario_change_facts = function() {
    casper.then(function() {
        this.click('a[href="#/facts/modify"]');
    });

    casper.waitForText('You can modify the answers below', test_scenario_change_facts_license);
};

var test_scenario_change_facts_license = function() {

    // selects 'yes' for the license question
    casper.thenEvaluate(function(term) {
        document.querySelector('input[name="input-q3-7"][value=yes]').setAttribute('checked', true);
    });

    casper.then(function() {
        this.click('input[type=button][value=Submit]');
    });

    casper.waitForSelector('#argumentgraph', 
                           test_scenario_select_urhg,
                           function() {
                               this.fail('Modifying the answer does not work');
                           },
                           10000);
};

var test_scenario_select_urhg = function() {
    casper.then(function() {
        this.click('#policies-item');
    });

    casper.waitForSelector('.policies-filtering', test_scenario_select_urhg2);
};


var test_scenario_select_urhg2 = function() {
    casper.then(function() {
        this.click('#inputUrhG');
    });

    casper.waitForSelector('#argumentgraph', 
                           test_scenario_copy,
                           function() {
                               this.fail('Applying UrhG policy does not work');
                           },
                           10000)
};

var test_scenario_copy = function () {
    casper.then(function() {
        this.click('#copy');
    });

    casper.setFilter("page.confirm", function(msg) {
        return msg === "Make a copy of the current case?" ? true : false;
    });

    casper.waitForText('You are now viewing a copy of the case.', 
                       function () {
                           casper.waitForText('may publish the work', test_scenario_vote);
                       });

};

var test_scenario_vote = function () {
    casper.then(function () {
        this.click('.vote');
    });

   casper.waitForText('Given the facts of this case', test_scenario_vote2);
};

var test_scenario_vote2 = function () {
    casper.then(function () {
        this.click('.vote-now');
    });

    casper.waitForText('Thank you for your vote!', test_scenario_vote3);

};

var test_scenario_vote3 = function () {
    casper.then(function () {
        this.click('.show-vote-results');
    });

    casper.waitForText('Claim', test_scenario_vote4);
};

var test_scenario_vote4 = function () {
    this.test.assertEval(function () {
       return $('tr td:nth-of-type(2)').text() == "100.00%";
    }, 'The vote has been registered');

    casper.then(function() {
        this.click('#arguments-item');
    });

    casper.waitForSelector('#argumentgraph', test_scenario_newstatement)
};

var test_scenario_evaluation = function () {
    casper.then(function () {
        this.click('.evaluate');
    });

    casper.waitForText('Evaluation finished', function() {
        casper.waitForSelector('#newstatement', test_scenario_newstatement);
    });
};

var test_scenario_newstatement = function () {
    casper.then(function () {
        this.click('#newstatement');
    });

    casper.waitForSelector('#save-statement', test_scenario_newstatement2);
};    

var test_scenario_newstatement2 = function () {
    this.test.assertExist('input[name=main][value=yes]');

    casper.thenEvaluate(function(term) {
        document.querySelector('input[name=main][value=yes]').setAttribute('checked', true);
    });

    // this.test.assertEval(function() {
    //     return document.querySelector('input[name=main][value=yes]').getAttribute('checked') == "true";
    // }, 'Main was set to true');

    casper.then(function () {
        this.click('#save-statement'); 
    });
    
    casper.waitWhileSelector('#save-statement', test_scenario_newstatement4)
};


// var text_en = "Here some text!";
// var metadata_en = "Here some metadata";

// document.querySelector('#statement-editor-text').value = text_en;  
// document.querySelector('.metadata-description-input').value = metadata_en;

// var test_scenario_newstatement3 = function () {

    
//     casper.waitForText('Here some text!', test_scenario_newstatement4);
    
// };

var test_scenario_newstatement4 = function () {
    take_picture.call(this);

    casper.then(function () {
        this.click('#newargument');
    });

    casper.waitForSelector('.save-argument', test_scenario_newargument);
};

var test_scenario_newargument = function () {
    casper.thenEvaluate(function (term) {
        $('#argument-editor-scheme').select2("val");
    });

    // take_picture.call(this);
};

// TODO test creating an argument
// TODO test reading the general map
// TODO export
// TODO test report

var take_picture = function () {

    this.capture('/tmp/carneades.png', {
        top: 0,
        left: 0,
        width: 1024,
        height: 1600
    });
};

casper.start(casper.cli.get('url'), function() {
    casper.waitForSelector('#mainmenu', function() {
        test_scenario_entry.call(this);
    });
});

casper.run(function() {
               this.test.renderResults(true);
           });