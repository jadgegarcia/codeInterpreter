package lexer;

import java.util.ArrayList;

public class Lexer1 {
    private final String input;
    private int position;

    public Lexer1(String input) {
        this.input = input;
        this.position = 0;
    }

    // Function to get the next token
    public Token getNextToken() {
        if (position >= input.length()) {
            return new Token(TokenType.EOF, "");
        }
    
        char currentChar = input.charAt(position);
    
        if (Character.isWhitespace(currentChar)) {
            position++;
            return getNextToken();
        }
    
        if (Character.isLetter(currentChar)) {
            return handleIdentifier();
        }
    
        if (Character.isDigit(currentChar) || currentChar == '.') {
            return handleNumber();
        }
    
        switch (currentChar) {
            case '-':
            case '+':
                if (position + 1 < input.length() && Character.isDigit(input.charAt(position + 1)) && TokenUtils.isOperator(input.charAt(position - 1))) {
                    return handleUnaryOperator();
                }
                return handleOperator();
    
            case '*':
            case '/':
            case '%':
                return handleOperator();
    
            case '=':
                return handleAssignmentOrComparison();
    
            case '>':
            case '<':
                return handleComparison();
    
            case '\'':
                return handleSingleQuote();
    
            case '"':
                return handleDoubleQuote();
    
            case '&':
                position++;
                return new Token(TokenType.CONCAT, String.valueOf(currentChar));
    
            case '$':
                position++;
                return new Token(TokenType.STRING, String.valueOf(currentChar));
    
            case '[':
                return handleSquareBracketString();
    
            case '#':
                return handleComment();
    
            case '@':
                position++;
                return new Token(TokenType.NEWLINE, String.valueOf(currentChar));
    
            default:
                if (TokenUtils.isDelimiter(currentChar)) {
                    position++;
                    return new Token(TokenType.DELIMITER, String.valueOf(currentChar));
                } else {
                    position++;
                    return new Token(TokenType.INVALID, String.valueOf(currentChar));
                }
        }
    }
    
    // Helper methods to handle different cases
    private Token handleIdentifier() {
        StringBuilder identifierBuilder = new StringBuilder();
        char currentChar = input.charAt(position);
        while (position < input.length() && (Character.isLetterOrDigit(currentChar) || currentChar == '_')) {
            identifierBuilder.append(currentChar);
            position++;
            if (position < input.length()) {
                currentChar = input.charAt(position);
            }
        }
        String identifier = identifierBuilder.toString();
        if (TokenUtils.isKeywordOrSpecialToken(identifier)) return new Token(TokenType.KEYWORD, identifier);
        if (TokenUtils.isValidIdentifier(identifier)) return new Token(TokenType.IDENTIFIER, identifier);
        return new Token(TokenType.INVALID, TokenUtils.isError("Invalid identifier = " + identifier));
    }
    
    private Token handleNumber() {
        StringBuilder numberBuilder = new StringBuilder();
        char currentChar = input.charAt(position);
        boolean isFloat = false;
    
        while (position < input.length() && (Character.isDigit(currentChar) || currentChar == '.')) {
            if (currentChar == '.') {
                if (isFloat) {
                    return new Token(TokenType.INVALID, TokenUtils.isError("Invalid number = " + numberBuilder.toString()));
                }
                isFloat = true;
            }
            numberBuilder.append(currentChar);
            position++;
            if (position < input.length()) {
                currentChar = input.charAt(position);
            }
        }
    
        return isFloat ? new Token(TokenType.FLOAT, numberBuilder.toString()) : new Token(TokenType.INT, numberBuilder.toString());
    }
    
    private Token handleUnaryOperator() {
        StringBuilder numberBuilder = new StringBuilder();
        char currentChar = input.charAt(position);
        numberBuilder.append(currentChar);
        position++;
        currentChar = input.charAt(position);
    
        while (position < input.length() && (Character.isDigit(currentChar) || currentChar == '.')) {
            numberBuilder.append(currentChar);
            position++;
            if (position < input.length()) {
                currentChar = input.charAt(position);
            }
        }
    
        String numberLiteral = numberBuilder.toString();
        if (numberLiteral.equals("-") || numberLiteral.equals("+")) {
            return new Token(TokenType.OPERATOR, numberLiteral);
        } else if (numberLiteral.contains(".")) {
            return new Token(TokenType.FLOAT, numberLiteral);
        } else {
            return new Token(TokenType.INT, numberLiteral);
        }
    }
    
    private Token handleOperator() {
        char currentChar = input.charAt(position);
        position++;
        return new Token(TokenType.OPERATOR, String.valueOf(currentChar));
    }
    
    private Token handleAssignmentOrComparison() {
        position++;
        if (position < input.length() && input.charAt(position) == '=') {
            position++;
            return new Token(TokenType.OPERATOR, "==");
        } else {
            return new Token(TokenType.ASSIGNMENT, "=");
        }
    }
    
    private Token handleComparison() {
        char currentChar = input.charAt(position);
        position++;
        if (position < input.length() && input.charAt(position) == '=') {
            position++;
            return new Token(TokenType.OPERATOR, String.valueOf(currentChar) + "=");
        } else if (currentChar == '<' && position < input.length() && input.charAt(position) == '>') {
            position++;
            return new Token(TokenType.OPERATOR, "<>");
        } else {
            return new Token(TokenType.OPERATOR, String.valueOf(currentChar));
        }
    }
    
    private Token handleSingleQuote() {
        if (position + 2 < input.length() && input.charAt(position + 2) == '\'') {
            char charLiteral = input.charAt(position + 1);
            position += 3;
            return new Token(TokenType.CHAR, String.valueOf(charLiteral));
        } else {
            position++;
            return new Token(TokenType.INVALID, TokenUtils.isError("Invalid character or not enclosed"));
        }
    }
    
    private Token handleDoubleQuote() {
        position++;
        StringBuilder stringLiteralBuilder = new StringBuilder();
        char currentChar = input.charAt(position);
    
        while (position < input.length() && currentChar != '"') {
            if (currentChar == '[') {
                position++;
                StringBuilder bracketContent = new StringBuilder();
                while (position < input.length() && input.charAt(position) != ']') {
                    bracketContent.append(input.charAt(position));
                    position++;
                }
                if (position < input.length() && input.charAt(position) == ']') {
                    stringLiteralBuilder.append(bracketContent.toString());
                    position++;
                } else {
                    return new Token(TokenType.INVALID, TokenUtils.isError("Invalid bracket content or not enclosed"));
                }
            } else {
                if (currentChar == '\\') {
                    position++;
                    if (position < input.length()) {
                        char escapeChar = input.charAt(position);
                        switch (escapeChar) {
                            case 'n':
                                currentChar = '\n';
                                break;
                            case 't':
                                currentChar = '\t';
                                break;
                            default:
                                currentChar = escapeChar;
                                break;
                        }
                    }
                }
                stringLiteralBuilder.append(currentChar);
                position++;
            }
            if (position < input.length()) {
                currentChar = input.charAt(position);
            }
        }
    
        if (position < input.length() && input.charAt(position) == '"') {
            position++;
            if (TokenUtils.isBooleanLiteral(stringLiteralBuilder.toString())) {
                return new Token(TokenType.BOOL, stringLiteralBuilder.toString());
            }
            return new Token(TokenType.STRING, stringLiteralBuilder.toString());
        } else {
            return new Token(TokenType.INVALID, TokenUtils.isError("Invalid String content or not enclosed"));
        }
    }
    
    private Token handleSquareBracketString() {
        StringBuilder stringLiteralBuilder = new StringBuilder();
        position++;
        while (position < input.length() && input.charAt(position) != ']') {
            stringLiteralBuilder.append(input.charAt(position));
            position++;
        }
        if (position < input.length() && input.charAt(position) == ']') {
            position++;
            return new Token(TokenType.STRING, stringLiteralBuilder.toString());
        } else {
            return new Token(TokenType.INVALID, TokenUtils.isError("Invalid String not enclosed"));
        }
    }
    
    private Token handleComment() {
        char currentChar = input.charAt(position);
        while (position < input.length() && currentChar != '\n' && currentChar != '\r') {
            position++;
            if (position < input.length()) {
                currentChar = input.charAt(position);
            }
        }
        while (position < input.length() && (currentChar == '\n' || currentChar == '\r')) {
            position++;
            if (position < input.length()) {
                currentChar = input.charAt(position);
            }
        }
        return getNextToken();
    }

    private ArrayList<Token> getMathTokens(String input) throws Exception {
        ArrayList<Token> tokenList = new ArrayList<>();
        boolean containsDecimalPoint = false;

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);

            if (Character.isDigit(c) || c == '.') {
                int j;
                for (j = i; j < input.length(); j++) {
                    char curr = input.charAt(j);
                    if (curr == '.') {
                        if (containsDecimalPoint) throw new Exception();
                        else containsDecimalPoint = true;
                    } else if (!Character.isDigit(curr)) break;
                }
                String substring = "0" + input.substring(i, j);
                Token token = new Token(TokenType.INT, Double.parseDouble(substring));
                tokenList.add(token);
                containsDecimalPoint = false;
                i = j - 1;
            } else if (TokenUtils.isOperator(c)) {
                TokenType tokenType = TokenUtils.getOperationToken(c);
                if (tokenType != null) {
                    tokenList.add(new Token(tokenType));
                }
            } else if (!Character.isWhitespace(c)) {
                throw new Exception();
            }
        }
        return tokenList;
    }
    
}