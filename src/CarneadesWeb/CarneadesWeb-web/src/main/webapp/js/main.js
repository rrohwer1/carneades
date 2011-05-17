/**
 * This is the AJAX-Engine for the IMPACT web application.
 *
 * @author bbr
 * @version 0.50
 */

/** Settings */
var jsloadstarted = new Date();
var showhints = true;

// adding JSON parser when browser is too old to have a build-in one (pre-IE8, pre-FF3.5, ...)
if( typeof( window[ 'JSON' ] ) == "undefined" ) document.write('<script type="text/javascript" src="https://github.com/douglascrockford/JSON-js/raw/master/json2.js"/>');

/**
 * Fixes a javascript bug that makes copying of Arrays impossible
 * @returns returns a copy of the array
 * @type Array
 */
Array.prototype.copy = function () {
    return ((new Array()).concat(this));
};

/**
 * Initialisation
 * @constructor
 */
$(function(){ // Init

    // Progressbar
    $("#progressbar").progressbar({
            value: 0
    });

    // Tabs
    $('#tabs').tabs();

    $.datepicker.setDefaults({
            //regional: "de",
            inline: true,
            changeMonth: true,
            changeYear: true,
            yearRange: (''+(jsloadstarted.getFullYear()-100)+':'+(jsloadstarted.getFullYear()+10)),
            showAnim: 'slideDown'
    });

    //button
     $(".ui-button").button();

    // Land -> Datum
    $("#locate").change(function(){
        alert("? "+$(this).val() );
        doAJAX({"language" : $(this).val()});
        /*
        $("body").append("<script src=\"js/jquery/jquery.ui.datepicker-"+$(this).val()+".js\" type=\"text/javascript\"></script>");
        $.datepicker.setDefaults($.datepicker.regional[$(this).val()]);
        */
        /*$( ".datepicker" ).each(function(index) {
                $(this).datepicker( "option",
                "regional", $("#locale").val() );
        })*/
    });

    // Fragen-Liste
    $("li", $("#questionlist")).each(function(index){
            //this.style.backgroundColor="red";
            var li_i = index;
            $(this).click(function(){
                    $(document.getElementsByTagName("h3")[li_i].firstChild).click();
            });
    });

    /** AJAX request config */
    $.ajaxSetup({
       url: "/CarneadesWeb-web/PolicySimulation",
       async: true,
       beforeSend: function() {
           statusupdate(0,"Please be patient.");
       },
       complete: function(XMLHttpRequest, textStatus) {
           if (textStatus == "success")
               $("#status").fadeOut();
           else if(textStatus == "error")
               statusupdate(1,XMLHttpRequest.status+" "+textStatus);
           else // "notmodified", "timeout", or "parsererror"
               statusupdate(1,textStatus);
       },
       timeout : 60000,
       dataType: "json",
       type: "POST"
    });

    /** loads initial questions */
    //loadTopic("demo");

});

/**
 * loads questions for requested topic
 * @param {string} t name of the topic
 * @see doAJAX
 */
function loadTopic(t) {
    doAJAX( {"request" : t} );
}

/**
 * Sends a ajax request to the server and manage the output of the reply.
 * @param {JSON} jsondata expects a json object with the given answers
 * @see sendAnswers
 * @see statusupdate
 * @see radioCheckNewLine
 */
function doAJAX(jsondata) {
    if (typeof jsondata == "undefined") var jsondata = null;
    //else alert("sending = "+JSON.stringify(jsondata));
    $.ajax({
        dataType : "json",
        data : {
            json : JSON.stringify(jsondata)
        },
        success : function(data) {
            // alert("ajax success"+data);
            if (data == null || data == "") alert("Empty Server Answer!")
            // getting questions
            else if (data.questions && data.questions.length >= 1) {
                showQuestions(data.questions);
            }
            // getting solution
            else if (data.solution) {
                showSolution(data.solution, data.path);
            }
            else if (data.language) {
                // alert("Language set to: "+data.language);
            } else if(data.schemes) {
                showPolicyRules(data.schemes);
            } else if(data.evaluated) {
                showArgGraph(data.evaluated);
            } else if(data.graphpath) {
                showSVGGraph(data.graphpath)
            } else if (data.error) {
                showError(data.error);
            }
            else {
                alert("Can not resolve: "+JSON.stringify(data,null,"  "));
            }
        }
    });
}

/**
 * Displays a list of questions
 * @param {object} qList json object representing the questions
 * @see doAJAX
 */
function showQuestions(qList) {
    $("#tabs a[href='#tabs-2']").click();
    var qbox = $("#questions");
    var newline = false;
    qbox.empty();
    qbox.append("<div id=\"hints\"><h4>hints</h4></div>");
    qbox.append("<div id=\""+qList[0].category.replace(/\s/,"_")+"\"><h3>"+qList[0].category+"</h3><div id=\"qcontent\"></div></div>");
    qbox = $("#qcontent", qbox);
    $.each(qList, function(i,item){
        // pre append formatting
        var output = "<p><label for=\"qID"+item.id+"\">"+item.question+"</label>";
        if (item.type == "select") {
            output += "<select id=\"qID"+item.id+"\" name=\"qID"+item.id+"\">";
            output += "<option value=\"\" selected=\"selected\">-- please choose --</option>";
            $.each(item.answers, function(answindex, answer) {
                output += "<option value=\""+answer+"\">"+answer+"</option>";
            });
            output += "</select>";
        }
        else if (item.type == "radio" || item.type == "checkbox") {
            newline = radioCheckNewLine(item.answers);
            $.each(item.answers, function(answindex, answer) {
                if (newline) output += "<br/>";
                output += "<input name=\"qID"+item.id+"\" type=\""+item.type+"\" value=\""+answer+"\">";
                output += "<span onclick=\"$(this).prev().click()\">"+answer+"</span>";
            });
        }
        else if (item.type == "date") {
            output += "<input type=\"text\" class=\"datefield\" id=\"qID"+item.id+"\" name=\"qID"+item.id+"\""+((item.answers && item.answers[0]!="") ? " value=\""+item.answers[0]+"\"" : "")+"/>";
        }
        else if (item.type == "int") {
            output += "<input type=\"text\" class=\"integer\" id=\"qID"+item.id+"\" name=\"qID"+item.id+"\""+((item.answers && item.answers[0]!="") ? " value=\""+item.answers[0]+"\"" : "")+"/>";
        }
        else if (item.type == "float") {
            output += "<input type=\"text\" class=\"float\" id=\"qID"+item.id+"\" name=\"qID"+item.id+"\""+((item.answers && item.answers[0]!="") ? " value=\""+item.answers[0]+"\"" : "")+"/>";
        }
        else output += "<input type=\""+item.type+"\" id=\"qID"+item.id+"\" name=\"qID"+item.id+"\""+((item.answers && item.answers[0]!="") ? " value=\""+item.answers[0]+"\"" : "")+"/>";
        output += "</p>";
        qbox.append(output);
        if (item.hint) $("#hints").append("<p id=\"qHINT"+item.id+"\" class=\"hint\">"+item.hint+"</p>");
        // post append formatting
        if (item.optional) $("p:last :input:first", qbox).addClass("optional");
        // focus and blur does not work on radio/checkbox
        if (item.type != "radio" && item.type != "checkbox") {
            $(":input:last", qbox).focus(function(){
                if (showhints) {
                    $("#hints p").css('display','none');
                    $("#qHINT"+this.id.substring(3)).show();
                }
            });
            $(":input:last", qbox).blur(function(){
                $("#qHINT"+this.id.substring(3)).css('display','none');
            });
            // validation
            $(":input:last", qbox).change(function(){
                validateField(this);
            });
        }
        else { // radios & checkboxes
            $("input:last", qbox).parent().mouseover(function(){
                if (showhints) {
                    var hinton=$("#hints > p:not(:hidden)");
                    statusupdate(1,"Verstecke: "+((hinton.length > 0)?"#qID"+hinton.attr("id").substring(5):"-")+" | Zeige: "+"#qHINT"+$(this).children("input:first").attr("name").substring(3));
                    if (hinton.length > 0) $("#qID"+hinton.attr("id").substring(5)).blur();
                    $("#qHINT"+$(this).children("input:first").attr("name").substring(3)).show();
                }
            });
            $("input:last", qbox).parent().mouseout(function(){
                $("#qHINT"+$(this).children("input:first").attr("name").substring(3)).css('display','none');
            });
            // validation
            $("input[name='qID"+item.id+"']", qbox).change(function(){
                validateField($("input:first", this.parentNoded)[0]);
            });
        }
    });
    qbox.append('<input type="button" class="ui-button next" value="next" onclick="sendAnswers(this.parentNode)"/>');
    $('.datefield', qbox).datepicker();
    updateTopicList(qList[0].category);
}

/**
 * Displays the solution
 * @param {object} solution json object representing the solution
 * @see doAJAX
 */
function showSolution(solution, path) {
    $("#tabs a[href='#tabs-3']").click();
 /*   $("#tabs-3").html("<h2>Solution</h2>"+
        "<h3>"+solution["main-issue"][1]+" "+solution["main-issue"][0]+" "+solution["main-issue"][2]+"</h3>"+
        "<p>Below you ind the full JSON solution output.</p><pre id=\"solution-xml\"><\/pre>");  
    var solutionNew = JSON.stringify(solution, null, "\t");
    $("#solution-xml").html(solutionNew); */
  /*  $("#tabs-3").html("<h2>Solution</h2>"+
        "<h3>"+solution+"</h3><div id=\"policyrules\"></div>");  */
    $("#solutionstatement").append(solution);
    $.ajaxSetup({url: "/CarneadesWeb-web/PolicyEvaluation"});
    var json = {"policyrules" : path}
    doAJAX(json);
    showArgGraph(path);
    //$("#solution-xml").html(solution);
}

/**
 * Vaildates the formfield content.
 * @param {object} obj a question formfield or a array or DIV-element that contains this HTML nodes
 * @returns returns if a form-field or a set of fields has passed validation
 * @type Boolean
 * @see qwarn
 * @see qunwarn
 * @see sendAnswers
 */
function validateField(obj) {
    var result = true;
    // Array
    if ($.type(obj) == "array") {
        for (var i=0;i < obj.length;i++) {
            if (validateForm(obj[i])=== false) {
                result = false;
                return false;
            }
        }
        return result;
    }
    // DIV (qcontent)
    else if ($.type(obj) == "object" && obj.nodeName == "DIV") {
        $(":input", obj).each( function(i, elem) {
            if ( !validateField(elem) ) {
                result = false;
                //return false;
            }
        });
        return result;
    }
    // validation:
    else if ($.type(obj) == "object" && (obj.nodeName == "INPUT"
             || obj.nodeName == "SELECT" || obj.nodeName == "TEXTFIELD") ) {
        var o = $(obj);
        // ignore buttons:
        if (obj.type && (obj.type == "button" || obj.type == "submit" || obj.type == "reset") ) return true;
        if (o.hasClass("integer")) {
            if (o.val().search(/\D/) != -1) {
                qwarn(obj,"Please insert a integer. No characters or whitespaces allowed.");
                return false;
            }
        }
        if (o.hasClass("float")) {
            if (o.val() != "" && o.val().search(/[\sa-zA-Z]/i) != -1 ||
               (null == /(^-?\d*\.?\d*$)/.exec(o.val()) ? true : /(^-?\d*\.?\d*$)/.exec(o.val())[0] !== o.val() ) ) {
                qwarn(obj,"No valid number found.");
                return false;
            }
        }
        if (o.hasClass("datefield")) {
            if (o.val() != "" && o.val().search(/[\sa-zA-Z]/i) != -1 || o.val().search(/\d\d[\.\/]\d\d[\.\/]\d\d\d\d/i) != -1
                && (null == /(\d\d[\.\/]\d\d[\.\/]\d\d\d\d)/i.exec(o.val()) ? true : /(\d\d[\.\/]\d\d[\.\/]\d\d\d\d)/i.exec(o.val())[0] !== o.val() ) ) {
                qwarn(obj,"Invalid date.");
                return false;
            }
        }
        // empty radio/box
        if ( obj.type == "radio" || obj.type == "checkbox") {
            if (obj.parentNode.getElementsByTagName("input")[0] == obj && !o.hasClass("optional")) {
                if ($("input:checked[name='"+obj.name+"']").length == 0) {
                    qwarn(obj,"This field is required.");
                    return false;
                }
                // something is selected
            }
            else { // abort validation because it already has been validated or is optional
                return true;
            }
        }
        // empty field
        else if (o.val() == "" && !o.hasClass("optional")) {
            qwarn(obj,"This field is required.");
            return false;
        }
        // valid content
        qunwarn(obj);
        return true;
    }
    else {
        alert("nothing to validate on field: "+obj.nodeName+" of type "+$.type(obj));
        return false;
    }
}

/**
 * Checks if radio or checkbox input fields needs a new line to seperate them
 * @param {Array} answers Array that contains all possible answers
 * @see doAJAX
 * @returns true when a new line is required
 * @type Boolean
 */
function radioCheckNewLine(answers) {
    var newline=false;
    for (var i=0; i < answers.length; i++) {
        if (answers[i].length > 12 || i > 4) {
            newline=true;
            break;
        }
    }
    return newline;
}

/*
 *  liste bearbeiten.
              var qlist = $("#question");
              qlist.empty();
              $.each(data.questions, function(i,item){
                  qlist.append("<li onclick=\"loadQuestions('"+item.id+"')\">"+item.name+" ("+item.len+")</li>");
              });
 *
 **/

/**
 * Updates the statusfield of the page.
 * @param {number} type Expect a integer with the value of the status. 0 means loading 1 an error and -1 that there is everthing allright so loaded.
 * @param {string} text Here goes the Text that will be displayed in the status.
 */
function statusupdate(type, text) {
    var icon="";
    if (type == 0) { // Loading
            $("#status").removeClass("ui-state-error");
            $("#status").addClass("ui-state-highlight");
            icon='<p><span class="ui-icon ui-icon-info" style="float: left; margin-right: 0.3em;"></span> <strong>Loading:</strong> ';
    }
    else if (type == 1) { // Alert
            $("#status").removeClass("ui-state-highlight");
            $("#status").addClass("ui-state-error");
            icon='<p><span class="ui-icon ui-icon-alert" style="float: left; margin-right: .3em;"></span> <strong>Alert:</strong> ';
    }
    else if (type == -1) {$("#status").hide();return false;}
    else {$("#status").hide();return false;}
    $("#status").html(icon+text+"</p>");
    $("#status").show();
}

/**
 * Collects the given answers and parse them as JSON before sending them to {@link doAJAX}.
 * @param {object} obj expects a HTML object that includes the input fields for the given answers.
 * @see doAJAX
 * @see validateField
 */
function sendAnswers(obj) {
    var doRequest = true;
    var jsonA = new Array();
    $(":input", obj).each(function(i, itemobj){
        var item = $(itemobj);
        // skip buttons
        if (item.hasClass("ui-button") || obj.type && (itemobj.type == "button" || itemobj.type == "submit" || itemobj.type == "reset") ) return true;
        var jsonitem;
        if (itemobj.type == "radio" || itemobj.type == "checkbox") {
            if (itemobj.parentNode.getElementsByTagName("input")[0] == obj) {
                if (validateField(itemobj) == false) {
                    doRequest = false;
                    return true;
                }
                var valArray = new Array();
                $(":input:checked[name='"+itemobj.name+"']", itemobj.parentNode).each(function(i, valobj) {
                    valArray.push(valobj.value);
                });
                jsonitem = {
                    "id" : item.attr("name").substring(3),
                    "value" : valArray.copy()
                }
                jsonA.push(jsonitem);
            }
            else return true;
        }
        else {
            if (validateField(itemobj) === false) {
                doRequest = false;
                return true;
            }
            jsonitem = {
                "id" : item.attr("name").substring(3),
                "value" : item.val()
            }
            jsonA.push(jsonitem);
        }
    });
    var jsonZ = {"answers" : jsonA.copy()}
    if (doRequest) doAJAX(jsonZ);
}

/**
 * Displays a warning besides a form field when invalid data is used. To hide this use {@link qunwarn}
 * @param {object} obj triggering form field HTML object
 * @param {string} warning text that appears right besides the field
 * @see qunwarn
 */
function qwarn(obj,warning) {
    var o = $(obj);
    if ($(".qwarn", obj.parentNode).length > 0) {
        $(".qwarn", obj.parentNode).html("<i></i>"+warning+"<b></b>");
    }
    else if (obj.type == "radio" || obj.type == "checkbox") {
        $("span", obj.parentNode).css("backgroundColor","#F78181");
        if ($("br", obj.parentNode).length > 0) { // newline checkbox/radio
            o=$("br:first", obj.parentNode);
            o.before("<span class=\"qwarn\"><i></i>"+warning+"<b></b></span>");
            o.prev().fadeIn(500);
        }
        else {
            o=$(":last-child", obj.parentNode);
            o.after("<span class=\"qwarn\"><i></i>"+warning+"<b></b></span>");
            o.next().fadeIn(500);
        }
    }
    else {
        o.css("backgroundColor","#F78181");
        o.after("<span class=\"qwarn\"><i></i>"+warning+"<b></b></span>");
        o.next().fadeIn(500);
    }
}

/**
 * Hides a {@link qwarn}-warning.
 * @param {object} obj triggering form field HTML object
 * @see qwarn
 */
function qunwarn(obj) {
    var o = $(obj);
    if (obj.type == "radio" || obj.type == "checkbox") {
        $("span", obj.parentNode).css("backgroundColor","");
    }
    else {
        o.css("backgroundColor","");
    }
    $(".qwarn", obj.parentNode).fadeOut(500, function() {$(this).remove();});
}

/**
 * adding a new topic to the question-topic-list
 * @param {string} topic name of the topic (must equal the id of the DIV)
 */
function updateTopicList(topic) {
    // first run:
    topic = topic.replace(/_/, " ");
    if ($("#questionslist li:first").text() == "not loaded yet") $("#questionslist").empty();
    $("#questionslist").add("<li>"+topic+"</li>").click(function() {
        var t = this.innerHTML; // topic
        if (t.indexOf(" ") != -1) t = t.replace(/\s/, "_");
        $("#questions div").hide();
        $("#"+t).show();
    });
}

function showPolicyRules(rules) {
    var policyList = $("#policyrules");
    // policyList.remove();
    policyList.append("<ul>")
    $.each(rules, function(ruleindex, r) {
       policyList.append("<li>"+ruleindex+" - "+r+"</li>"); 
    });
    policyList.append("</ul>");    
}

/**
 * creating svg representation of an argument graph
 * @param {string} path path to lkif with argument graph
 */
function showArgGraph(path) {
    //alert("converting argument graph: "+path);
    var json = {"showgraph" : path};
    doAJAX(json);
}

function showSVGGraph(path) {
    // alert("showing svg file : "+path);
    $("#graph").append("<object data=" + path + " width=\"900\" height=\"900\" type=\"image/svg+xml\" />");
}

function showError(error) {
    // TODO : handle errors
    alert(error);
}