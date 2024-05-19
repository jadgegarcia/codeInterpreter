package Arithmetic;

import java.util.ArrayList;
import java.util.HashMap;

import lexer.Token;
import lexer.TokenType;


public class MathTokenizer {
    private static final HashMap<Character, TokenType> operationChars = new HashMap<>();
    private static final HashMap<String, TokenType> logicalOperationChars = new HashMap<>();

    static {
        operationChars.put('+', TokenType.PLUS);
        operationChars.put('-', TokenType.MINUS);
        operationChars.put('*', TokenType.MULTIPLY);
        operationChars.put('/', TokenType.DIVIDE);
        operationChars.put('%', TokenType.MODULO);
        operationChars.put('(', TokenType.PAREN_OPEN);
        operationChars.put(')', TokenType.PAREN_CLOSE);
        

        logicalOperationChars.put("==", TokenType.EQUAL);
        logicalOperationChars.put("<", TokenType.LESS_THAN);
        logicalOperationChars.put(">", TokenType.GREATER_THAN);
        logicalOperationChars.put("<=", TokenType.LESS_THAN_OR_EQUAL);
        logicalOperationChars.put(">=", TokenType.GREATER_THAN_OR_EQUAL);
        logicalOperationChars.put("&&", TokenType.AND);
        logicalOperationChars.put("||", TokenType.OR);
        logicalOperationChars.put("!", TokenType.NOT);
        logicalOperationChars.put("!=", TokenType.NOT_EQUAL);

    }
  
    /**
     * Generates a list containing all the valid mathematical tokens in the input string.
     *
     * @throws Exception if an invalid number of unexpected character is found
     */
    public static ArrayList<Token> generateTokens(String input) throws Exception {
        ArrayList<Token> tokenList = new ArrayList<>();
        boolean containsDecimalPoint = false;
  
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            // String s = String.valueOf(c);
            // if character is a digit, create and add new token

            if (Character.isDigit(c) || c == '.') {
                int j;
                // iterate until the end of the number
                for (j = i; j < input.length(); j++) {
                    char curr = input.charAt(j);
                    // cannot contain 2 decimal points in a number
                    if (curr == '.') {
                        if (containsDecimalPoint) throw new Exception();
                        else containsDecimalPoint = true;
                    }
                    // end of number token reached
                    else if (!Character.isDigit(curr)) break;
                }
                // create a new number token and add to list
                String substring = "0" + input.substring(i, j); //from start to current character
                Token token = new Token(TokenType.INT, Double.parseDouble(substring));
                tokenList.add(token);
                containsDecimalPoint = false; //update decimal tracker as end of number token reached
                // update the outer loop and start iterating from the end of the number token
                i = j - 1;
            }
            // if it is an operation, add to operation token
            else if (operationChars.containsKey(c)) {
                Token token = new Token(operationChars.get(c));
                tokenList.add(token);

               
            }

             // if it is Logical Operator, add to logicals token
            else if(logicalOperationChars.containsKey(String.valueOf(c))){
        
                Token logicToken = new Token(logicalOperationChars.get(String.valueOf(c)));
                tokenList.add(logicToken);
                
            }


            // else if it isn't a valid character, throw exception
            else if (!Character.isWhitespace(c)) throw new Exception();
        }
        return tokenList;
    }
}
