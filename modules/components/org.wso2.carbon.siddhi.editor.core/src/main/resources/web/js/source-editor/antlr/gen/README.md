1. Use "antlr4 -Dlanguage=JavaScript SiddhiQL.g4" to generate Lexers and Parsers
2. Copy generated content to "source-editor/antlr/gen/" dir
3. Change following methods in SiddhiQLLexer.js

    ```                            
        SiddhiQLLexer.prototype.ID_QUOTES_action = function (localctx, actionIndex) {
            switch (actionIndex) {
                case 0:
                    // setText(getText().substring(1, getText().length()-1));
                    this.text = this.text.substring(1, this.text.length - 1);
                    break;
                default:
                    throw "No registered action for:" + actionIndex;
            }
        };
        
        SiddhiQLLexer.prototype.STRING_LITERAL_action = function (localctx, actionIndex) {
            switch (actionIndex) {
                case 1:
                    // setText(getText().substring(1, getText().length()-1));
                    this.text = this.text.substring(1, this.text.length - 1);
                    break;
                default:
                    throw "No registered action for:" + actionIndex;
            }
        };
        
    ```

4. Change antlr4 path for require js
    ```   
       var antlr4 = require('../../antlr4-js-runtime/index');
    ```