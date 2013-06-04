// Copyright (c) 2012 Fraunhofer Gesellschaft
// Licensed under the EUPL V.1.1

goog.provide('carneades.policy_analysis.web.agb.argument');

AGB.argument_url = function(db, argid)
{
    return '/arguments/argument/' + IMPACT.project + '/' + db + '/' + argid;
};

AGB.set_argument_url = function(db, argid)
{
    $.address.value(AGB.argument_url(db, argid));
};

AGB.set_has_properties = function(argument_data) {
    argument_data.hascounterarguments = argument_data.undercutters.length > 0 || argument_data.rebuttals.length > 0
        ? true : false;
    argument_data.hasdependents = argument_data.dependents.length > 0 ? true : false;

    if(_.isNil(argument_data.header)) {
        return;
    }

    argument_data.hasdescription = argument_data.header.description &&
        argument_data.header.description[IMPACT.lang] ? true : false;
    argument_data.header_haskey = argument_data.header.key ? true : false;
    argument_data.header_hascontributor = argument_data.header.contributor ? true : false;
    argument_data.header_hascoverage = argument_data.header.coverage ? true : false;
    argument_data.header_hascreator = argument_data.header.creator ? true : false;
    argument_data.header_hasdate = argument_data.header.date ? true : false;
    argument_data.header_hasformat = argument_data.header.format ? true : false;
    argument_data.header_hasidentifier = argument_data.header.identifier ? true : false;
    argument_data.header_haslanguage = argument_data.header.language ? true : false;
    argument_data.header_haspublisher = argument_data.header.publisher ? true : false;
    argument_data.header_hasrelation = argument_data.header.relation ? true : false;
    argument_data.header_hasrights = argument_data.header.rights ? true : false;
    argument_data.header_hassource = argument_data.header.source ? true : false;
    argument_data.header_hassubject = argument_data.header.subject ? true : false;
    argument_data.header_hastitle = argument_data.header.title ? true : false;
    argument_data.header_hastype = argument_data.header.type ? true : false;

    argument_data.hasheader = argument_data.hasdescription || argument_data.header_haskey
        || argument_data.header_hascontributor || argument_data.header_hascoverage
        || argument_data.header_hascreator || argument_data.header_hasdate
        || argument_data.header_hasformat || argument_data.header_hasidentifier
        || argument_data.header_haslanguage || argument_data.header_haspublisher
        || argument_data.header_hasrelation || argument_data.header_hasrights
        || argument_data.header_hassource || argument_data.header_hassubject
        || argument_data.header_hastitle || argument_data.header_hastype;

};

AGB.argument_html = function(db, argument_data)
{
    AGB.normalize(argument_data);
    AGB.set_has_properties(argument_data);
    argument_data.direction = argument_data.pro ? "pro" : "con";
    argument_data.db = db;
    argument_data.description_text = AGB.description_text(argument_data.header);
    argument_data.scheme_text = PM.scheme_text(argument_data.scheme);
    AGB.set_argument_title_text(argument_data);
    argument_data.direction_text = argument_data.pro ? "pro" : "con";
    argument_data.conclusion.statement_text = AGB.statement_raw_text(argument_data.conclusion);
    AGB.set_premises_text(argument_data);
    argument_data.haspremises = argument_data.premises.length > 0;
    AGB.set_undercutters_text(argument_data);
    AGB.set_rebuttals_text(argument_data);
    AGB.set_dependents_text(argument_data);
    argument_data.pmt_id = $.i18n.prop('pmt_id');
    argument_data.pmt_scheme = $.i18n.prop('pmt_scheme');
    argument_data.pmt_strict = $.i18n.prop('pmt_strict');
    argument_data.pmt_weight = $.i18n.prop('pmt_weight');
    argument_data.pmt_value = $.i18n.prop('pmt_value');
    argument_data.pmt_premises = $.i18n.prop('pmt_premises');
    argument_data.pmt_value = $.i18n.prop('pmt_value');
    argument_data.pmt_conclusion = $.i18n.prop('pmt_conclusion');
    argument_data.pmt_counterarguments = $.i18n.prop('pmt_counterarguments');
    argument_data.pmt_used_by = $.i18n.prop('pmt_used_by');

    argument_data.pmt_key = $.i18n.prop('pmt_key');
    argument_data.pmt_coverage = $.i18n.prop('pmt_coverage');
    argument_data.pmt_creator = $.i18n.prop('pmt_creator');
    argument_data.pmt_date = $.i18n.prop('pmt_date');
    argument_data.pmt_date = $.i18n.prop('pmt_date');
    argument_data.pmt_format = $.i18n.prop('pmt_format');
    argument_data.pmt_identifier = $.i18n.prop('pmt_identifier');
    argument_data.pmt_language = $.i18n.prop('pmt_language');
    argument_data.pmt_publisher = $.i18n.prop('pmt_publisher');
    argument_data.pmt_relation = $.i18n.prop('pmt_relation');
    argument_data.pmt_rights = $.i18n.prop('pmt_rights');
    argument_data.pmt_source = $.i18n.prop('pmt_source');
    argument_data.pmt_subject = $.i18n.prop('pmt_subject');
    argument_data.pmt_title = $.i18n.prop('pmt_title');
    argument_data.pmt_type = $.i18n.prop('pmt_type');

    argument_data = PM.merge_menu_props(argument_data);
    argument_data = PM.merge_ag_menu_props(argument_data);

    var argument_html = ich.argument(argument_data);
    return argument_html.filter('#argument');
};

PM.agb_argument_menu = function (db, argid) {
    return [{text: 'pmt_ag_menu_map'
             ,link: '#/arguments/map/' + PM.project.id + '/' + db},
            {text: 'pmt_ag_menu_vote'
             ,link: "#/arguments/vote/" + PM.project.id},
            {text: 'pmt_ag_menu_copy'
             ,link: "#/arguments/copy-case/" + PM.project.id},
            {text: 'pmt_ag_menu_export'
             ,link: "#/arguments/export/" + PM.project.id + '/' + db},
            {text: 'pmt_ag_menu_evaluate'
             ,link: "#/arguments/evaluate/" + PM.project.id + '/' + db},
            {text: 'edit'
             ,link: "#/arguments/argument/" + PM.project.id + '/' + db + '/' + argid + '?edit=true&entity=argument'},
            {text: 'pmt_menu_delete'
             ,link: "#/arguments/argument/" + PM.project.id + '/' + db + '/' + argid + '?delete=true&entity=argument'},
            {text: 'pmt_new_statement'
             ,link: "#/arguments/argument/" + PM.project.id + '/' + db + '/' + argid + '?edit=true&entity=statement'}
           ];
};

AGB.display_argument = function(db, argid)
{
    console.log('display argument');

    var info = PM.get_arg_info(db, argid);

    PM.show_menu({text: PM.project.get('title'),
                  link: "#/project/" + PM.project.id},
                 PM.agb_argument_menu(db, argid));

    $('#browser').html(AGB.argument_html(db, info));

    if(PM.on_argument_edit()) {
        AGB.edit_argument(db, info);
    }

    if(PM.on_argument_delete()) {
        AGB.delete_argument(db, info);
    }

    if(PM.on_statement_edit()) {
        AGB.show_statement_editor({save_callback: function() {
            $.address.queryString('');
            return false;
        }});
    }

};

AGB.set_premises_text = function(argument_data)
{
    $.each(argument_data.premises,
           function(index, premise) {
               premise.statement.statement_text = AGB.statement_raw_text(premise.statement, index + 1);
               premise.positive_text = premise.positive ? "" : "neg.";
           });
};

AGB.set_argument_title_text = function(info)
{
    var default_text = $.i18n.prop('pmt_argument');
    if(info.header) {
        info.argument_title_text = info.header.title ? info.header.title['en'] : default_text;
    } else {
        info.argument_title_text = default_text;
    }
};

AGB.set_undercutters_text = function(info)
{
    $.each(info.undercutters_data,
           function(index, data) {
               data.argument_text = AGB.argument_text(data, index + 1);
               data.id = info.undercutters[index];
               AGB.set_premises_text(data);
           });
};

AGB.set_rebuttals_text = function(info)
{
    $.each(info.rebuttals_data,
          function(index, data) {
              data.argument_text = AGB.argument_text(data, index + info.undercutters_data.length);
              data.id = info.rebuttals[index];
              AGB.set_premises_text(data);
          });
};

AGB.set_dependents_text = function(info)
{
    $.each(info.dependents_data,
          function(index, data) {
              data.statement_text = AGB.statement_text(data, index + 1);
              data.id = info.dependents[index];
          });
};

// Returns a text representing the argument, ie., its title
// or then its scheme, or a default text if none of them is defined
AGB.argument_text = function(data, index)
{
    var text;
    if(data.header && data.header.title) {
        text = data.header.title;
    } else if (data.scheme && data.scheme.length > 0) {
        text = PM.scheme_text(data.scheme);
    } else if(index == undefined) {
        text = 'Argument';
    } else {
        text = 'Argument #' + index;
    }

    return text;
};

AGB.argument_link = function(db, id, text)
{
    return '<a href="/carneades/#/arguments/argument/{0}/{1}/{2}" rel="address:/arguments/argument/{0}/{1}/{2}" class="argument" id="argument{2}">{3}</a>'.format(IMPACT.project, db, id, text);
};


// AGB.enable_argument_edition = function(db, argid) {
//     $('#menus').append(ich.argumenteditormenu({
//         pmt_menu_edit: $.i18n.prop('pmt_menu_edit'),
//         pmt_menu_delete: $.i18n.prop('pmt_menu_delete')
//     }));
//     $('#delete-argument').click(_.bind(AGB.delete_argument, AGB, db, argid));
//     $('#edit-argument').click(_.bind(AGB.edit_argument, AGB, db, argid));
//     $('.evaluate').click(_.bind(AGB.evaluate, AGB, _.bind(AGB.display_argument, AGB, db, argid)));

//     return false;
// };

AGB.delete_argument = function(db, argid) {
    if(confirm('Delete this argument?')) {
        PM.ajax_delete(IMPACT.wsurl + '/argument/' + IMPACT.project + '/' + db + '/' + argid,
                       function(e) {
                           console.log('argument deleted');
                           console.log(e);

                           AGB.set_argumentgraph_url(db);
                       },
                       IMPACT.user,
                       IMPACT.password,
                       PM.on_error);
    }

    return false;
};

AGB.edit_argument = function(db, argid) {
    // PM.arguments_info.fetch({async: false});
    var argument = PM.get_arg(db, argid);
    var argumentcandidate = new PM.ArgumentCandidate({argument: argument,
                                                      statements: PM.get_stmts(),
                                                      schemes: PM.schemes,
                                                      current_lang: IMPACT.lang});
    var argumenteditorview = new PM.ArgumentEditorView({model: argumentcandidate,
                                                        title: $.i18n.prop('pmt_edit_argument')});

    argumenteditorview.render();
    $('#argumenteditor').html(argumenteditorview.$el);

    return false;
};
