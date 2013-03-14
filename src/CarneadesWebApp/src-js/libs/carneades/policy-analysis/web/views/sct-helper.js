// Copyright (c) 2012 Fraunhofer Gesellschaft
// Licensed under the EUPL V.1.1

goog.provide('carneades.policy_analysis.web.views.sct_helper');

PM.display_sct_intro = function() {
    var sct_intro = new PM.SctIntro({'model': PM.sct});
    sct_intro.render();
    $('#pm').html(sct_intro.$el);  
};

goog.exportProperty(PM, 'display_sct_intro', PM.display_sct_intro);

PM.sct_issues_url = function() {
    return '/sct/issues';
};

PM.set_sct_issues_url = function() {
    $.address.value(PM.sct_issues_url());
};

PM.display_sct_issues = function() {
    var sct_issues = undefined;
    
    // TODO use deferred instead, like here
    // [[file:~/Documents/Projects/carneades/src/PolicyModellingTool/resources/policymodellingtool/public/js/app/arguments.js::deferreds.push(PM.arguments.fetch())%3B][deferreds]]
    
    if(PM.debate_info.get('main-issues') == undefined) {
        PM.debate_info.fetch({success: function() {
                                  sct_issues = new PM.SctIssues(
                                      {'model': PM.sct,
                                       'issues': PM.debate_info.get('main-issues'),
                                       'statements': PM.debate_statements, 
                                       'arguments': PM.debate_arguments});
                                  sct_issues.render();
                                  $('#pm').html(sct_issues.$el);
                              }
                             });
    } else {
        sct_issues = new PM.SctIssues(
            {model: PM.sct,
             issues: PM.debate_info.get('main-issues'),
             statements: PM.debate_statements, 
             arguments: PM.debate_arguments});
        sct_issues.render();
        $('#pm').html(sct_issues.$el);  
    } 
};

goog.exportProperty(PM, 'display_sct_issues', PM.display_sct_issues);

PM.sct_question_url = function() {
    return '/sct/question';
};

PM.set_sct_question_url = function() {
  $.address.value(PM.sct_question_url());
};

PM.display_sct_question = function() {
    var sct_question = new PM.SctQuestion({model: PM.sct,
                                           lang: IMPACT.lang});
    sct_question.render();
    
    $('#pm').html(sct_question.$el);
};

goog.exportProperty(PM, 'display_sct_question', PM.display_sct_question);

PM.sct_summary_url = function() {
    return '/sct/summary';
};

PM.set_sct_summary_url = function() {
  $.address.value(PM.sct_summary_url());
};

PM.display_sct_summary = function() {
    var sct_summary = new carneades.policy_analysis.web.views.sct.summary.Summary(
        {model: PM.sct});

    sct_summary.render();
    $('#pm').html(sct_summary.$el);
    
};

goog.exportProperty(PM, 'display_sct_summary', PM.display_sct_summary);

PM.sct_comparison_url = function() {
    return '/sct/comparison';
};

PM.set_sct_comparison_url = function() {
  $.address.value(PM.sct_comparison_url());
};

PM.display_sct_comparison = function() {
    var sct_comparison = new carneades.policy_analysis.web.views.sct.comparison.Comparison(
        {model: PM.sct}); 

    sct_comparison.render();
    $('#pm').html(sct_comparison.$el);
    
};

goog.exportProperty(PM, 'display_sct_comparison', PM.display_sct_comparison);

