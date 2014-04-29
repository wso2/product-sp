    /*
     * AutoSnippet JavaScript Library v1.0
     * http://spacebug.com
     *
     * Copyright (c) 2009 Amir Shevat
     * Dual licensed under the MIT and GPL licenses.
     *
     * Dependencies – jquery http://jquery.com/ Optionally SyntaxHighlighter
     */

            $(document).ready(function(){
               
                 $("div[name^='autosnippet']").each(function () {
                     $this = $(this);
                     $this.snippet();;
                 });
              
             });


            $.fn.snippet = function(options) {
                $this = $(this);
                           
                    var params = $this.attr('name');
                    params = params.toString();
                    params= params.substr( params.indexOf(':')+1);
                    var target;
                    var SyntaxHighlighter;
                    if(params.indexOf(':')>-1){
                         target = params.substr(0, params.indexOf(':'));
                         params= params.substr( params.indexOf(':')+1);
                         SyntaxHighlighter =params;
                    }else{
                         target = params;
                    }
                

                var cleanHTML = '<pre name="code_'+target+'" class="html">';
                    cleanHTML += $this.html().replace(/</g, "&lt;");
                    cleanHTML += "</pre>";
                $("div[name='"+target+"']").html(cleanHTML);

                if(SyntaxHighlighter && SyntaxHighlighter=='yes' ){
                    dp.SyntaxHighlighter.HighlightAll('code_'+target);
                }
            };