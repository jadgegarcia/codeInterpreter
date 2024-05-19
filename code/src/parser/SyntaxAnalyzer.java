package parser;
import java.util.*;
import Arithmetic.ArithInterpreter;
import lexer.Token;
import lexer.TokenType;

public class SyntaxAnalyzer {
    private int flg = 0;
    private List<Token> tokens;
    private int currentTokenIndex;
    private boolean begin_code;
    private Map<String, Token> variables;
    Scanner scanner;
    public SyntaxAnalyzer(List<Token> tokens) {
        this.tokens = tokens;
        this.currentTokenIndex = 0;
        this.begin_code = false;
        this.variables = new HashMap<>();
        this.scanner = new Scanner(System.in);
;    }
    private Token peek() {
        if (currentTokenIndex < tokens.size()) {
            return tokens.get(currentTokenIndex + 1);
        } else {
            return null; // No more tokens left
        }
    }
    private Token currToken() {
        if (currentTokenIndex < tokens.size()) {
            return tokens.get(currentTokenIndex);
        } else {
            return null; // No more tokens left
        }
    }
    public void parse() {
        while (currentTokenIndex < tokens.size()) {
            statement();
        }
        if (currentTokenIndex < tokens.size()) {
            error("Unexpected tokens after end of program " + currentTokenIndex +":"+tokens.size());
        }
    }
    public int countNewline(int from, int to) {
        int count =0;
        for(int i = from; i < to+1;i++){
            if(tokens.get(i).getType() == TokenType.NEWLINE) count++;
        }
        return count;
    }

    private void statement() {
        if(!begin_code && !tokens.get(currentTokenIndex).getValue().equals("BEGIN") && !tokens.get(currentTokenIndex).getValue().equals("CODE")) {
            if(tokens.get(currentTokenIndex).getValue().equals("begin")) {
                error("Invalid statement:" + tokens.get(currentTokenIndex).toString());
            }
            if(tokens.get(currentTokenIndex).getValue().equals("code")) {
                error("Invalid statement:" + tokens.get(currentTokenIndex).toString());
            }
            consume();
            return;
        }
        if (currentTokenIndex < tokens.size()) {
            Token currentToken = tokens.get(currentTokenIndex);
            if (currentToken.getType() == TokenType.IDENTIFIER) {
                assignmentStatement();
            } else if(currentToken.getType() == TokenType.NEWLINE){
                consume();
            } else if (currentToken.getType() == TokenType.KEYWORD && currentToken.getValue().equals("BEGIN")) {
                consume();
                if(tokens.get(currentTokenIndex).getValue().equals("CODE")){
                    consume();
//                    System.out.println("Code is now Running");
                    begin_code = true;
                    if(currToken().getType() == TokenType.NEWLINE) consume();

                    else error("Expected a NEWLINE");
                }
                else if(tokens.get(currentTokenIndex).getValue().equals("IF")){ // BEGIN IF

                }
            } else if (currentToken.getType() == TokenType.KEYWORD && currentToken.getValue().equals("END")) {
                consume();
                if(tokens.get(currentTokenIndex).getValue().equals("CODE")){
                    
                    consume();
                    checkAfterEndCode(currentTokenIndex);
//                    System.out.println("\nFinished Coding");
                    if(currToken().getType() == TokenType.NEWLINE) {
                        consume();
                    } else {
                        error("Expected a NEWLINE");
                    }
                    if(flg == 0) {
                        consume();
                    } else {
                        error("Expected a EOF");
                    }
                }
                else if(tokens.get(currentTokenIndex).getValue().equals("IF")){ // END IF
                }
                else if(tokens.get(currentTokenIndex).getValue().equals("WHILE")){ // END WHILE
                consume();
                }
            } else if (currentToken.getType() == TokenType.KEYWORD && (currentToken.getValue().equals("INT") || currentToken.getValue().equals("CHAR") || currentToken.getValue().equals("BOOL") || currentToken.getValue().equals("FLOAT"))) {
                declareStatement();
            }else if (currentToken.getType() == TokenType.KEYWORD && currentToken.getValue().equals("DISPLAY")) {
                displayStatement();
            }else if (currentToken.getType() == TokenType.KEYWORD && currentToken.getValue().equals("SCAN")) {
                scanStatement();
            }else if (currentToken.getType() == TokenType.KEYWORD && currentToken.getValue().equals("IF")){
                ifStatement();
            }else if (currentToken.getType() == TokenType.KEYWORD && currentToken.getValue().equals("WHILE")){
                whileStatement();
            } else if(currToken().getType() == TokenType.EOF) {
                consume();
            }
            // TODO: make all the different handlers
            else {
                error("Invalid statement:" + currentToken);
            }
        }
    }
    private void whileStatement(){
        match(TokenType.KEYWORD,"WHILE");
        List<Token> expressionTokens = new LinkedList<>();
        while(currToken().getType()!= TokenType.NEWLINE){
//            System.out.println("Current: "+currToken());
            expressionTokens.add(currToken());
            consume();
        }
        match(TokenType.NEWLINE);
//         for (Token token: expressionTokens) {
// //            System.out.println(token+" Expression tokens");
//         }
        match(TokenType.KEYWORD,"BEGIN");
        match(TokenType.KEYWORD,"WHILE");
        match(TokenType.NEWLINE);
        int startwhileIndex = currentTokenIndex;
        int nestedCount = 0;

        while(true){
            if(currToken().getValue().equals("BEGIN") && peek().getValue().equals("WHILE"))
                nestedCount++;
            if(currToken().getValue().equals("END") && peek().getValue().equals("WHILE"))
                nestedCount--;
            if(nestedCount == -1){
                break;
            }
            consume();
        }

        int endwhileIndex = currentTokenIndex;

        while (Boolean.parseBoolean(expression(expressionTokens).toString())){
            currentTokenIndex = startwhileIndex;
            for(int i=0;i< countNewline(startwhileIndex,endwhileIndex);i++){
                statement();
            }
        }
        currentTokenIndex = endwhileIndex;

    }
    private void scanStatement() {
        match(TokenType.KEYWORD,"SCAN");
        match(TokenType.DELIMITER,":");
        while(currToken().getType() == TokenType.IDENTIFIER || currToken().getType() == TokenType.DELIMITER){
            if(currToken().getType() == TokenType.IDENTIFIER){
                try{
                    //System.out.println(currToken().getValue());
                    if(variables.containsKey(currToken().getValue())){
                        String newValue = scanner.nextLine();
                        variables.get(currToken().getValue()).setValue(newValue);
                    }else throw new RuntimeException("Variable: "+ currToken().getValue() + " is not yet declared");
                }catch (NullPointerException e){
                }
                consume();
            }else if(currToken().getType() == TokenType.DELIMITER){
                consume();
            }else error("Unexpected Token in Scan:" + currToken());
        }
        match(TokenType.NEWLINE);
    }
    private void displayStatement() {
//        System.out.println("Display");
        consume();
        consume();
        while(currToken().getType() == TokenType.IDENTIFIER || currToken().getType() == TokenType.STRING|| currToken().getType() == TokenType.CONCAT){
            if(currToken().getType() == TokenType.IDENTIFIER){
                try{
                    Token variableToken = variables.get(currToken().getValue());
                    if (variableToken != null) {
                        if (variableToken.getType() == TokenType.BOOL) {
                            System.out.print(variableToken.getValue().toUpperCase());
                        } else {
                            System.out.print(variableToken.getValue());
                        }
                    } else {
                        throw new RuntimeException("Variable: " + currToken().getValue() + " is not yet declared");
                    }
                }catch (NullPointerException e){
                    if(variables.containsKey(currToken().getValue()))
                        System.out.print("null");
                    else throw new RuntimeException("Variable: "+ currToken().getValue() + " is not yet declared");
                }
                consume();
            }else if(currToken().getType() == TokenType.CONCAT){
                consume();
            }else if(currToken().getType() == TokenType.STRING){
                if(currToken().getValue().equals("$")) System.out.println("");
                else System.out.print(currToken().getValue());
                consume();
            }
        }
        match(TokenType.NEWLINE);
    }
    private void declareStatement() {
        String datatype = currToken().getValue();
        TokenType expectedDataType = null;
        switch (datatype){
            case "INT":
                expectedDataType = TokenType.INT;
                break;
            case "FLOAT":
                expectedDataType = TokenType.FLOAT;
                break;
            case "BOOL":
                expectedDataType = TokenType.BOOL;
                break;
            case "CHAR":
                expectedDataType = TokenType.CHAR;
                break;
        }
//        System.out.println("expected data type: " + datatype);
        consume(); //Consume INT,FLOAT,CHAR,BOOL token
        while(currToken().getType() != TokenType.NEWLINE){
            if(currToken().getValue().equals(","))match(TokenType.DELIMITER);
            String varname = currToken().getValue();
//            System.out.println(varname+ "Varname");
            match(TokenType.IDENTIFIER);
            if(currToken().getValue() == "="){
                consume(); // Consume ASSIGNMENT token
                if(variables.containsKey(varname))throw new IllegalArgumentException("Variable name: " + varname + " is already declared");
                if(expectedDataType == TokenType.FLOAT && currToken().getType() == TokenType.INT) currToken().setType(TokenType.FLOAT);
                if(currToken().getType() != expectedDataType ){
                    if(currToken().getType() == TokenType.IDENTIFIER){
                        TokenType varDataType = variables.get(currToken().getValue()).getType();
                        switch (varDataType){
                            case CHAR:
                                if(expectedDataType != TokenType.CHAR)
                                    throw new IllegalArgumentException("Unmatched datatype Expected datatype: "+expectedDataType + " Defined datatype: "+varDataType);
                                break;
                            case INT:
                            case FLOAT:
                                if(expectedDataType == TokenType.CHAR || expectedDataType == TokenType.BOOL)
                                    throw new IllegalArgumentException("Unmatched datatype Expected datatype: "+expectedDataType + " Defined datatype: "+varDataType);
                                break;
                            case BOOL:
                                if(expectedDataType != TokenType.BOOL)
                                    throw new IllegalArgumentException("Unmatched datatype Expected datatype: "+expectedDataType + " Defined datatype: "+varDataType);
                                break;
                            default:
                            break;
                        }
                    }
                    if((expectedDataType == TokenType.CHAR || expectedDataType == TokenType.BOOL) && (currToken().getType() == TokenType.INT || currToken().getType() == TokenType.FLOAT))
                        throw new IllegalArgumentException("Unmatched datatype Expected datatype: "+expectedDataType + " Defined datatype: "+currToken().getType());
                    if((currToken().getType() == TokenType.CHAR || currToken().getType() == TokenType.BOOL) && (expectedDataType == TokenType.INT || expectedDataType == TokenType.FLOAT))
                        throw new IllegalArgumentException("Unmatched datatype Expected datatype: "+expectedDataType + " Defined datatype: "+currToken().getType());
                }
//                System.out.println(currToken());
                if(peek().getType() != TokenType.OPERATOR && currToken().getType() != TokenType.IDENTIFIER) {
                    if(expectedDataType == TokenType.INT){
                        currToken().setValue(Integer.toString((int)currToken().getDataType()));
                    }
                    variables.put(varname, currToken());
//                System.out.println("Declared "+varname+": " +initializedVariables.get(varname));
                    consume();
                }else {
                    StringBuilder tokenValuesBuilder = new StringBuilder();
                    while (currToken().getType() != TokenType.NEWLINE && currToken().getType() != TokenType.DELIMITER) {

                        if(currToken().getType() == TokenType.IDENTIFIER) {
                            tokenValuesBuilder.append(variables.get(currToken().getValue()).getDataType());
                        }
                        else
                            tokenValuesBuilder.append(currToken().getValue());
                        consume();
                    }
//                    System.out.println(tokenValuesBuilder.toString());
                    try {
                        if(expectedDataType == TokenType.INT)
                            variables.put(varname, new Token(expectedDataType,Integer.toString((int)ArithInterpreter.getResult(tokenValuesBuilder.toString()))));
                        else
                            variables.put(varname, new Token(expectedDataType,Double.toString(ArithInterpreter.getResult(tokenValuesBuilder.toString()))));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }else if(currToken().getValue().equals(",")){
                consume(); // Consume DELIMITER token
                variables.put(varname,new Token(expectedDataType,null));
//                System.out.println(initializedVariables.containsKey(varname)+varname);
            }else if(currToken().getType() == TokenType.NEWLINE){
                variables.put(varname,new Token(expectedDataType,null));
//                System.out.println(initializedVariables.containsKey(varname)+varname);
            }else error("Unexpected token type: " + currToken());
        }
        match(TokenType.NEWLINE);
    }
    private void assignmentStatement() {
        ArrayList<Token> identifiers = new ArrayList<>();
        identifiers.add(currToken());
        match(TokenType.IDENTIFIER);
        match(TokenType.ASSIGNMENT, "=");

        List<Token> tokens = new LinkedList<>();

        while (currToken().getType() != TokenType.NEWLINE) {
            if (currToken().getType() == TokenType.ASSIGNMENT) {
                consume(); // Consume the ASSIGNMENT token
            }else if (currToken().getType() == TokenType.IDENTIFIER && peek().getType() != TokenType.OPERATOR&& peek().getType() != TokenType.NEWLINE&& peek().getType() != TokenType.DELIMITER) {
                identifiers.add(currToken());
                consume(); // Consume the ASSIGNMENT token
            }else if (currToken().getType() == TokenType.INT || currToken().getType() == TokenType.FLOAT || currToken().getType() == TokenType.DELIMITER || currToken().getType() == TokenType.BOOL|| currToken().getType() == TokenType.IDENTIFIER){
                while(currToken().getType() != TokenType.NEWLINE){
                    if(peek().getValue().equals(">") ||peek().getValue().equals("<") ||peek().getValue().equals("<>") ||peek().getValue().equals("==") ||peek().getValue().equals(">=") ||peek().getValue().equals("<=")||peek().getValue().equals("AND")||peek().getValue().equals("OR")){
                        tokens.add(new Token(TokenType.DELIMITER,"("));
                        if(currToken().getType() == TokenType.IDENTIFIER) {
                            tokens.add(variables.get(currToken().getValue()));
                        }else tokens.add(currToken());
                        consume();
                        tokens.add(currToken());
                        consume();
                        if(currToken().getType() == TokenType.IDENTIFIER) {
                            tokens.add(variables.get(currToken().getValue()));
                        }else tokens.add(currToken());
                        consume();
                        tokens.add(new Token(TokenType.DELIMITER,")"));
                    }
                    if(currToken().getType() == TokenType.IDENTIFIER){
//                        System.out.println(variables.get(currToken().getValue()));
                        tokens.add(variables.get(currToken().getValue()));
                        consume();
                    }else{
                        tokens.add(currToken());
                        consume();
                    }
                }
            }
        }
//        for(Token token: tokens) System.out.println(token+"TOKEN EXPRESSION");
        for(Token var: identifiers){
            if(variables.containsKey(var.getValue())){
                StringBuilder tokenValuesBuilder = new StringBuilder();
                for (Token token : tokens) {
                    // Append the value of each token to the StringBuilder
                    if(token.getType() == TokenType.IDENTIFIER) tokenValuesBuilder.append(variables.get(token.getValue()));
                    else tokenValuesBuilder.append(token.getValue());
                }
                if(isLogicalStatement(tokens)){
                    LogicalCalculator logicalCalculator = new LogicalCalculator();
                    variables.get(var.getValue()).setValue(Boolean.toString(logicalCalculator.evaluate(tokens)));
                }else if(containsFloat(tokens))
                    try{
                        variables.get(var.getValue()).setValue(Double.toString(ArithInterpreter.getResult(tokenValuesBuilder.toString())));
                    } catch (Exception ignored) {
                        System.out.println("Invalid Input");
                    }
                else
                    try{
                        variables.get(var.getValue()).setValue(Integer.toString((int)ArithInterpreter.getResult(tokenValuesBuilder.toString())));
                    } catch (Exception ignored) {
                        System.out.println("Invalid Input");
                    }
            }
            else error("Variable: " + var +" must be declared first");
        }
        match(TokenType.NEWLINE);
    }

    private void ifStatement() {
        //Issues: IF(True), ELSE IF(T). Both will execute
        match(TokenType.KEYWORD, "IF");
        match(TokenType.DELIMITER, "(");
        Boolean parseStatement = ifExpression();
        match(TokenType.DELIMITER, ")");
        consume(); //the consume function calls just consume the newlines (it throws an error if you dont consume the newline)
        match(TokenType.KEYWORD, "BEGIN");
        match(TokenType.KEYWORD, "IF");
        consume();
        if(parseStatement){
            while (currToken().getType() != TokenType.KEYWORD || !currToken().getValue().equals("END")) {
                statement();
            }
        } else{
            int nestedCount = 0;
            while(true){
                if(currToken().getValue().equals("BEGIN") && peek().getValue().equals("IF"))
                    nestedCount++;
                if(currToken().getValue().equals("END") && peek().getValue().equals("IF"))
                    nestedCount--;
                if(nestedCount == -1){
                    break;
                }
                consume();
            }
        }
        match(TokenType.KEYWORD, "END");
        match(TokenType.KEYWORD, "IF"); //first if statement finished
        consume();
        List<Boolean> ifelseResult = new LinkedList<>();
        ifelseResult.add(parseStatement);
//        check for multiple alternatives

        while (true) {
            if(currToken().getType() == TokenType.KEYWORD && currToken().getValue().equals("ELSE")){ //if else keyword encountered
                consume();
                if(currToken().getType() == TokenType.KEYWORD && currToken().getValue().equals("IF")){ //should keep checking for else ifs
                    consume();
                    match(TokenType.DELIMITER, "(");
                    Boolean parseStatement2 = ifExpression();
                    match(TokenType.DELIMITER, ")");
                    consume();
                    match(TokenType.KEYWORD, "BEGIN");
                    match(TokenType.KEYWORD, "IF");
                    consume();
                    if(parseStatement2 && !ifelseResult.contains(true)){
                        while (currToken().getType() != TokenType.KEYWORD || !currToken().getValue().equals("END")) {
                            statement();
                        }
                    } else {
                        int nestedCount = 0;
                        while(true){
                            if(currToken().getValue().equals("BEGIN") && peek().getValue().equals("IF"))
                                nestedCount++;
                            if(currToken().getValue().equals("END") && peek().getValue().equals("IF"))
                                nestedCount--;
                            if(nestedCount == -1){
                                break;
                            }
                            consume();
                        }
                    }
                    ifelseResult.add(parseStatement2);
                    match(TokenType.KEYWORD, "END");
                    match(TokenType.KEYWORD, "IF");
                    consume();
                } else {
                    consume();
                    match(TokenType.KEYWORD, "BEGIN");
                    match(TokenType.KEYWORD, "IF");
                    consume();
                    if(ifelseResult.contains(true)){
                        int nestedCount = 0;
                        while(true){
                            if(currToken().getValue().equals("BEGIN") && peek().getValue().equals("IF"))
                                nestedCount++;
                            if(currToken().getValue().equals("END") && peek().getValue().equals("IF"))
                                nestedCount--;
                            if(nestedCount == -1){
                                break;
                            }
                            consume();
                        }
                    } else {
                        while (currToken().getType() != TokenType.KEYWORD || !currToken().getValue().equals("END")) {
                            statement();
                        }
                    }
                    match(TokenType.KEYWORD, "END");
                    match(TokenType.KEYWORD, "IF");
                    consume();
                    break;
                }
            }else{
                break;
            }
        }

    }

    private Object expression(List<Token> expressionTokens) {
        List<Token> tokens = new LinkedList<>();
        for (Token token: expressionTokens){
            if(token.getType() == TokenType.IDENTIFIER)
                tokens.add(variables.get(token.getValue()));
            else tokens.add(token);
        }
        ListIterator<Token> iterator = tokens.listIterator();

        while (iterator.hasNext()) {
            Token token = iterator.next();
            if (token.getType() == TokenType.OPERATOR) {
                // Insert a new token two steps after the current position
                for (int i = 0; i < 1 && iterator.hasNext(); i++) {
                    iterator.next();
                }
                iterator.add(new Token(TokenType.DELIMITER, ")")); // Adjust the type and value accordingly
                // Move the iterator back to the original position
                for (int i = 0; i < 2 && iterator.hasPrevious(); i++) {
                    iterator.previous();
                }
                // Insert a new token two steps before the current position
                for (int i = 0; i < 2 && iterator.hasPrevious(); i++) {
                    iterator.previous();
                }
                iterator.add(new Token(TokenType.DELIMITER, "(")); // Adjust the type and value accordingly
                // Move the iterator back to the original position
                for (int i = 0; i < 2 && iterator.hasNext(); i++) {
                    iterator.next();
                }
            }
        }

//        for (Token token: tokens){
//            System.out.println("Enclosed Tokens: "+token);
//        }
        Object result = null;
        if(isLogicalStatement(tokens)){
            LogicalCalculator logicalCalculator = new LogicalCalculator();
            result = Boolean.toString(logicalCalculator.evaluate(tokens));
        }else if(containsFloat(tokens))
            result = Double.toString(Calculator.evaluateArithmeticExpression(tokens,TokenType.FLOAT));
        else
            result = Integer.toString(Calculator.evaluateArithmeticExpression(tokens));
        return result;
    }

    private boolean ifExpression(){
        List<Token> tokensForIf = new LinkedList<>();
        while(currToken().getType()!=TokenType.DELIMITER && currToken().getValue()!=")"){
             tokensForIf.add(currToken());
            consume();
        }
        return Boolean.parseBoolean(expression(tokensForIf).toString());
    }

    private void match(TokenType expectedType) {
        if (currentTokenIndex < tokens.size()) {
            Token currentToken = tokens.get(currentTokenIndex);
            if (currentToken.getType() == expectedType) {
                consume();
            } else {
                error("Unexpected token type, expected " + expectedType + " Token: " + currentToken);
            }
        }
    }

    private void match(TokenType expectedType, String expectedValue) {
        if (currentTokenIndex < tokens.size()) {
            Token currentToken = tokens.get(currentTokenIndex);
            if (currentToken.getType() == expectedType && currentToken.getValue().equals(expectedValue)) {
                consume();
            } else {
                error("Unexpected token type or value, expected " + expectedType + " '" + expectedValue + "'"
                + " Current token: " + currToken().getType() + ", " + currToken().getValue());
            }
        }
    }

    private void consume() {
        currentTokenIndex++;
    }

    private void error(String message) {
        throw new RuntimeException("Syntax error: " + message);
    }
    public static boolean containsFloat(List<Token> tokens) {
        for (Token token : tokens) {
            if (token.getType() == TokenType.FLOAT) {
                return true;
            }
        }
        return false;
    }
    public static boolean isLogicalStatement(List<Token> tokens) {
        for (Token token : tokens) {
            if (token.getValue().equals("==")||token.getValue().equals("<>")||token.getValue().equals(">=")||token.getValue().equals("<=")||token.getValue().equals(">")||token.getValue().equals("<")||token.getValue().equals("AND")||token.getValue().equals("OR")||token.getValue().equals("NOT")) {
                return true;
            }
        }
        return false;
    }

    public void checkAfterEndCode(int ind) {
       // System.out.println("TOKEN SIZE: " + tokens.size());
        for(int i = ind; i < tokens.size(); i++) {
            //System.out.println(tokens.get(i).getValue());
            if(!tokens.get(i).getValue().equals("@") && (i != tokens.size()-1)) {
                //System.out.println("POSITIVE");
                flg = 1;
                break;
            }
        }
    }
}