// Copyright (c) 2012 Fraunhofer Gesellschaft
// Licensed under the EUPL V.1.1

goog.provide('carneades.policy_analysis.web.agb.statement');

AGB.statement_url = function(db, stmtid)
{
    return '/arguments/statement/' + IMPACT.project + '/' + db + '/' + stmtid;
};

AGB.set_statement_url = function(db, stmtid)
{
    $.address.value(AGB.statement_url(db, stmtid));
};

AGB.set_statement_has_properties = function(info) {
  if(_.isNil(info.header)) {
        return;
    }

    info.hasdescription = info.header.description &&
        info.header.description[IMPACT.lang] ? true : false;
    info.header_haskey = info.header.key ? true : false;
    info.header_hascontributor = info.header.contributor ? true : false;
    info.header_hascoverage = info.header.coverage ? true : false;
    info.header_hascreator = info.header.creator ? true : false;
    info.header_hasdate = info.header.date ? true : false;
    info.header_hasformat = info.header.format ? true : false;
    info.header_hasidentifier = info.header.identifier ? true : false;
    info.header_haslanguage = info.header.language ? true : false;
    info.header_haspublisher = info.header.publisher ? true : false;
    info.header_hasrelation = info.header.relation ? true : false;
    info.header_hasrights = info.header.rights ? true : false;
    info.header_hassource = info.header.source ? true : false;
    info.header_hassubject = info.header.subject ? true : false;
    info.header_hastitle = info.header.title ? true : false;
    info.header_hastype = info.header.type ? true : false;

    info.hasheader = info.hasdescription || info.header_haskey
        || info.header_hascontributor || info.header_hascoverage
        || info.header_hascreator || info.header_hasdate
        || info.header_hasformat || info.header_hasidentifier
        || info.header_haslanguage || info.header_haspublisher
        || info.header_hasrelation || info.header_hasrights
        || info.header_hassource || info.header_hassubject
        || info.header_hastitle || info.header_hastype;

};

AGB.statement_html = function(db, info, lang)
{
    AGB.normalize(info);
    AGB.set_statement_has_properties(info);
    info.db = db;
    AGB.set_statement_title_text(info);
    info.description_text = AGB.description_text(info.header);
    AGB.set_procon_texts(info);
    AGB.set_procon_premises_text(info);
    AGB.set_premise_of_texts(info);
    if(info.text) {
        info.statement_text = AGB.markdown_to_html(info.text[lang]);
        if(info.statement_text != "") {
            info.hastext = true;
        }
    }
    info.haspro = info.pro && info.pro.length > 0;
    info.hascon = info.con && info.con.length > 0;
    info.haspremiseof = info.premise_of && info.premise_of.length > 0;

    info.pmt_key = $.i18n.prop('pmt_key');
    info.pmt_coverage = $.i18n.prop('pmt_coverage');
    info.pmt_creator = $.i18n.prop('pmt_creator');
    info.pmt_date = $.i18n.prop('pmt_date');
    info.pmt_date = $.i18n.prop('pmt_date');
    info.pmt_format = $.i18n.prop('pmt_format');
    info.pmt_identifier = $.i18n.prop('pmt_identifier');
    info.pmt_language = $.i18n.prop('pmt_language');
    info.pmt_publisher = $.i18n.prop('pmt_publisher');
    info.pmt_relation = $.i18n.prop('pmt_relation');
    info.pmt_rights = $.i18n.prop('pmt_rights');
    info.pmt_source = $.i18n.prop('pmt_source');
    info.pmt_subject = $.i18n.prop('pmt_subject');
    info.pmt_title = $.i18n.prop('pmt_title');
    info.pmt_type = $.i18n.prop('pmt_type');

    info.pmt_id = $.i18n.prop('pmt_id');
    info.pmt_header = $.i18n.prop('pmt_header');
    info.pmt_atom = $.i18n.prop('pmt_atom');
    info.pmt_main_issue = $.i18n.prop('pmt_main_issue');
    info.pmt_header = $.i18n.prop('pmt_header');
    info.pmt_standard = $.i18n.prop('pmt_standard');
    info.pmt_weight = $.i18n.prop('pmt_weight');
    info.pmt_value = $.i18n.prop('pmt_value');
    info.pmt_text = $.i18n.prop('pmt_text');
    info.pmt_pro_arguments = $.i18n.prop('pmt_pro_arguments');
    info.pmt_con_arguments = $.i18n.prop('pmt_con_arguments');
    info.pmt_premise_of = $.i18n.prop('pmt_premise_of');

    info = PM.merge_menu_props(info);
    info = PM.merge_ag_menu_props(info);

    var statement_html = ich.statement(info);
    return statement_html.filter('#statement');
};

PM.agb_statement_menu = function (db, stmtid) {
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
             ,link: "#/arguments/statement/" + PM.project.id + '/' + db + '/' + stmtid + '?edit=true&entity=statement'},
            {text: 'pmt_menu_delete'
             ,link: "#/arguments/statement/" + PM.project.id + '/' + db + '/' + stmtid + '?delete=true&entity=statement'},
            {text: 'pmt_new_argument'
             ,link: "#/arguments/statement/" + PM.project.id + '/' + db + '/' + stmtid + '?edit=true&entity=argument'}
           ];
};

AGB.display_statement = function(db, stmtid)
{
    console.log('Display statement...');
    var info = PM.get_stmt_info(db, stmtid);

    PM.show_menu({text: PM.project.get('title'),
                  link: "#/project/" + PM.project.id},
                 PM.agb_statement_menu(db, stmtid));

    $('#browser').html(AGB.statement_html(db, info, IMPACT.lang));
    $('#export').click(function (event){
        window.open('/carneadesws/export/{0}/{1}'.format(db, IMPACT.project), 'CAF XML');
        return false;
    });
    // AGB.enable_statement_edition(db, info);

    if(PM.on_statement_edit()) {
        AGB.edit_statement(db, info);
    }

    if(PM.on_argument_edit()) {
        AGB.new_argument(PM.get_stmt(db, info.id).toJSON());
    }

    if(PM.on_statement_delete()) {
        AGB.delete_statement(db, stmtid);
    }
}

AGB.set_statement_title_text = function(info)
{
    var default_text = $.i18n.prop('pmt_statement');
    if(info.header) {
        info.statement_title_text = info.header.title ? info.header.title['en'] : default_text;
    } else {
        info.statement_title_text = default_text;
    };
};

AGB.set_arg_texts = function(info, direction)
{
    $.each(info[direction],
           function(index, data) {
               var text = AGB.argument_text(data, index + 1);
               info[direction][index].argument_text = text;
               info[direction][index].id = info.pro[index]; // used by the template to create the ahref
           });
};

AGB.set_procon_premises_text = function(statement_data)
{
    $.each(statement_data.pro_data,
           function(index, pro) {
               AGB.set_premises_text(pro);
           });
    $.each(statement_data.con_data,
           function(index, con) {
               AGB.set_premises_text(con);
           });
};

AGB.set_premise_of_texts = function(info)
{
    $.each(info.premise_of_data,
           function(index, data) {
               var text = AGB.argument_text(data, index + 1);
               data.argument_text = text;
               data.id = info.premise_of[index]; // used by the template to create the href
           }
          );
};

AGB.set_procon_texts = function(info)
{
    AGB.set_arg_texts(info, 'pro_data');
    AGB.set_arg_texts(info, 'con_data');
};

AGB.slice_statement = function(statement_text)
{
    var maxlen = 180;
    if(statement_text.length > maxlen) {
        return statement_text.slice(0, maxlen - 3) + "...";
    } else {
        return statement_text;
    }
};

AGB.statement_prefix = function(statement) {
    if(AGB.statement_in(statement)) {
        return "☑ ";
    } else if(AGB.statement_out(statement)) {
        return "☒ ";
    } else {
        return "☐ ";
    }
};

AGB.statement_standard = function(statement) {
    if(statement.standard == "pe") {
        return "Preponderance of Evidence";
    }

    if(statement.standard == "dv") {
        return "Dialectical Validity";
    }

    if(statement.standard == "cce") {
        return "Clear and Convincing Evidence";
    }

    if(statement.standard == "brd") {
        return "Beyond Reasonable Doubt";
    }

    return "";
};

AGB.sexpr_to_str = function(sexpr) {
    if(typeof sexpr == 'string') {
        return sexpr;
    }

    var str = "(";

    _.each(sexpr, function(s) {
              str += s + " ";
           });

    str = str.slice(0, -1);
    str += ")";

    return str;
};

AGB.statement_raw_text = function(statement) {
    if(statement.text && statement.text[IMPACT.lang]) {
        var text = statement.text[IMPACT.lang];
        return _.escape(text);
    }

    // TODO: if atom is UUID, then returns the string "statement" ?
    return _.escape(statement.atom);
};

AGB.statement_text = function(statement)
{
    var text = AGB.statement_raw_text(statement);
    return AGB.markdown_to_html(AGB.statement_prefix(statement) + text);
};

AGB.statement_link = function(db, id, text)
{
    return '<a href="/carneades/#/arguments/statement/{0}/{1}/{2}" rel="address:/arguments/statement/{0}/{1}/{2}" class="statement" id="statement{2}">{3}</a>'.format(IMPACT.project, db, id, text);
};

AGB.statement_in = function(statement)
{
    return (statement.value != null) && ((1.0 - statement.value) < 0.001);
};

AGB.statement_out = function(statement)
{
    return (statement.value != null) && (statement.value < 0.001);
};

AGB.delete_statement = function(db, stmtid) {
    if(confirm('Delete this statement?')) {
        PM.ajax_delete(IMPACT.wsurl + '/statement/' + IMPACT.project + '/' + db + '/' + stmtid,
                       function(e) {
                           PM.ag_info[db].fetch({async: false});
                           AGB.set_argumentgraph_url(db);
                       },
                       IMPACT.user,
                       IMPACT.password,
                       PM.on_error);
    }

    return false;
};

AGB.edit_statement = function(db, info) {
    AGB.show_statement_editor({update: true,
                               statement: PM.get_stmt(db, info.id).toJSON(),
                               save_callback: function() {
                                   $.address.queryString('');
                                   $.address.update();
                               }
                              });
    return false;
};
