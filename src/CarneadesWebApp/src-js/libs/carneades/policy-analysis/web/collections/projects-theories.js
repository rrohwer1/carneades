// Copyright (c) 2013 Fraunhofer Gesellschaft
// Licensed under the EUPL V.1.1

goog.provide('carneades.policy_analysis.web.collections.projects_theories');


goog.require('carneades.policy_analysis.web.models.project_theory');

PM.ProjectsTheories = Backbone.Collection.extend(
    {model: PM.ProjectTheory,

     url: function() {
         return IMPACT.wsurl + '/project/' + this.project + '/theories';
     },

     initialize: function(models, attrs) {
         this.project = attrs.project;
     }

    }

);
