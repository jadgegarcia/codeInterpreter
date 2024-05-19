package lexer;
import java.util.List;

// Lexer class to tokenize the source code
public class Lexer {
    private final String input;
    private int position;

    public Lexer(String input) {
        this.input = input;
        this.position = 0;
    }

    // Function to get the next token
    public Token getNextToken() {
        if (position >= input.length()) {
            return new Token(TokenType.EOF, "");
        }

        char currentChar = input.charAt(position);
        // Handle unary operators for numeric literals
        if (currentChar == '-' || currentChar == '+') {
            if (position + 1 < input.length() && Character.isDigit(input.charAt(position + 1)) && isOperator(input.charAt(position-1))) {
                StringBuilder numberBuilder = new StringBuilder();
                numberBuilder.append(currentChar);
                position++;
                currentChar = input.charAt(position);

                while (position < input.length() && (Character.isDigit(currentChar) || currentChar == '.')) {
                    numberBuilder.append(currentChar);
                    position++;
                    if (position < input.length())
                        currentChar = input.charAt(position);
                }

                // If it's a float, return as FLOAT type, else return as NUMBER type
                String numberLiteral = numberBuilder.toString();
                if (numberLiteral.equals("-") || numberLiteral.equals("+")) {
                    // Only the unary operator without the numeric literal
                    return new Token(TokenType.OPERATOR, numberLiteral);
                } else if (numberLiteral.contains(".")) {
                    // It's a float
                    return new Token(TokenType.FLOAT, numberLiteral);
                } else {
                    // It's an integer
                    return new Token(TokenType.INT, numberLiteral);
                }
            }
        }
        // Handle identifiers and keywords
        if (Character.isLetter(currentChar)) {
            StringBuilder identifierBuilder = new StringBuilder();
            while (position < input.length() && (Character.isLetterOrDigit(currentChar) || currentChar == '_')) {
                identifierBuilder.append(currentChar);
                position++;
                if (position < input.length())
                    currentChar = input.charAt(position);
            }
            String identifier = identifierBuilder.toString();
            //if(isBooleanLiteral(identifier)) return new Token(TokenType.BOOL, identifier);
            // Check if it's a keyword or special token
            if (isKeywordOrSpecialToken(identifier)) {
                return new Token(TokenType.KEYWORD, identifier);
            } else {
                if (isValidIdentifier(identifier)) {
                    return new Token(TokenType.IDENTIFIER, identifier);
                } else {
                    return new Token(TokenType.INVALID, "(The set identifier is invalid = " + identifier + ")");
                }
            }
        }
        // Handle numeric literals (integers and floats)
        if (Character.isDigit(currentChar) || currentChar == '.') {
            StringBuilder numberBuilder = new StringBuilder();
            boolean isFloat = false;

            while (position < input.length() && (Character.isDigit(currentChar) || currentChar == '.')) {
                if (currentChar == '.') {
                    if (isFloat) {
                        // Second dot found, invalid token
                        return new Token(TokenType.INVALID, "(Token is invalid = " + numberBuilder.toString() + ")");
                    }
                    isFloat = true;
                }
                numberBuilder.append(currentChar);
                position++;
                if (position < input.length())
                    currentChar = input.charAt(position);
            }

            // If it's a float, return as FLOAT type, else return as NUMBER type
            if (isFloat) {
                return new Token(TokenType.FLOAT, numberBuilder.toString());
            } else {
                return new Token(TokenType.INT, numberBuilder.toString());
            }
        }

        // Handle delimiters
        if (isDelimiter(currentChar)) {
            position++;
            return new Token(TokenType.DELIMITER, String.valueOf(currentChar));
        }
        // Handle assignment or comparison operator
        if (currentChar == '=') {
            position++;
            if (position < input.length() && input.charAt(position) == '=') {
                // Handle ==
                position++;
                return new Token(TokenType.OPERATOR, "==");
            } else {
                // Handle =
                return new Token(TokenType.ASSIGNMENT, "=");
            }
        }
        // Handle comparison operators
        if (currentChar == '>' || currentChar == '<') {
            position++;
            if (position < input.length() && input.charAt(position) == '=') {
                // Handle >= and <=
                position++;
                return new Token(TokenType.OPERATOR, String.valueOf(currentChar) + "=");
            } else if (position < input.length() && input.charAt(position) == '>') {
                // Handle <>
                position++;
                return new Token(TokenType.OPERATOR, "<>");
            } else {
                return new Token(TokenType.OPERATOR, String.valueOf(currentChar));
            }
        }
        // Handle unary operators
        if (currentChar == '+' || currentChar == '-' || currentChar == '*' || currentChar == '/' ||currentChar == '%') {
            position++;
            return new Token(TokenType.OPERATOR, String.valueOf(currentChar));
        }
        // Handle Single Quote
        if (input.charAt(position) == '\'') {
            if (position + 2 < input.length() && input.charAt(position + 2) == '\'') {
                char charLiteral = input.charAt(position + 1);
                position += 3;  // Move to the character after the closing single quote
                return new Token(TokenType.CHAR, String.valueOf(charLiteral));
            } else {
                // Invalid character literal (not properly enclosed)
                position++;
                System.err.println("(A character is not properly enclosed or is invalid)");
                return new Token(TokenType.INVALID, "(A character is not properly enclosed or is invalid)");
            }
        }


        //Handle Double Quote
        if (currentChar == '"') {
            position++;
            StringBuilder stringLiteralBuilder = new StringBuilder();
            while (position < input.length() && input.charAt(position) != '"') {
                char charLiteral = input.charAt(position);
                // Handle characters enclosed within square brackets []
                if (charLiteral == '[') {
                    // Extract the characters enclosed within square brackets
                    StringBuilder bracketContent = new StringBuilder();
                    position++;
                    while (position < input.length() && input.charAt(position) != ']') {
                        bracketContent.append(input.charAt(position));
                        position++;
                    }
                    if (position < input.length() && input.charAt(position) == ']') {
                        // Append the bracket content to the string literal builder
                        stringLiteralBuilder.append(bracketContent.toString());
                        position++;
                    } else {
                        // Unterminated bracket content
                        return new Token(TokenType.INVALID, "(Bracket content is not properly enclosed)");
                    }
                } else {
                    // Handle escape sequences if needed
                    if (charLiteral == '\\') {
                        position++;
                        if (position < input.length()) {
                            char escapeChar = input.charAt(position);
                            // Handle escape characters like \n, \t, etc.
                            // You may need to extend this logic based on your language's escape sequence rules
                            switch (escapeChar) {
                                case 'n':
                                    charLiteral = '\n';
                                    break;
                                case 't':
                                    charLiteral = '\t';
                                    break;
                                // Handle other escape characters as needed
                                default:
                                    // Default behavior: treat the escape sequence as is
                                    charLiteral = escapeChar;
                                    break;
                            }
                        }
                    }
                    stringLiteralBuilder.append(charLiteral);
                    position++;
                }
            }
            // Check if closing double quote was found
            if (position < input.length() && input.charAt(position) == '"') {
                position++;
                String stringLiteral = stringLiteralBuilder.toString();
                if (isBooleanLiteral(stringLiteral)) {
                    
                    return new Token(TokenType.BOOL, stringLiteral);
                }
                return new Token(TokenType.STRING, stringLiteral);
            } else {
                // No closing double quote found
                return new Token(TokenType.INVALID, "(String is not properly enclosed)");
            }
        }
        // Handle concat
        if (currentChar == '&') {
            position++;
            return new Token(TokenType.CONCAT, String.valueOf(currentChar));
        }
        // Handle new line
        if (currentChar == '$') {
            position++;
            return new Token(TokenType.STRING, String.valueOf(currentChar));
        }
        // Skip whitespaces
        if (Character.isWhitespace(currentChar)) {
            position++;
            return getNextToken();
        }
        // Handle string literals enclosed within square brackets
        if (currentChar == '[') {
            StringBuilder stringLiteralBuilder = new StringBuilder();
            position++;
            while (position < input.length() && input.charAt(position) != ']') {
                stringLiteralBuilder.append(input.charAt(position));
                position++;
            }
            if (position < input.length() && input.charAt(position) == ']') {
                // Move past the closing bracket
                position++;
                return new Token(TokenType.STRING, stringLiteralBuilder.toString());
            } else {
                // Unterminated string literal (no closing bracket)
                return new Token(TokenType.INVALID, "(String is not properly enclosed)");
            }
        }
        // Handle comments
        if (currentChar == '#') {
            // Skip until the end of line
            while (position < input.length() && currentChar != '\n' && currentChar != '\r') {
                position++;
                if (position < input.length())
                    currentChar = input.charAt(position);
            }
            // Skip newline characters
            while (position < input.length() && (currentChar == '\n' || currentChar == '\r')) {
                position++;
                if (position < input.length())
                    currentChar = input.charAt(position);
            }
            // Recursively call getNextToken to get the next token after the comment
            return getNextToken();
        }
        // Handle newline characters
        if (currentChar == '@' ){
            position++;
            return new Token(TokenType.NEWLINE, String.valueOf(currentChar));
        }
        // If none of the above, it's an invalid token
        position++;
        return new Token(TokenType.EOF, String.valueOf(currentChar));
    }

    // Function to check if a string is a keyword or special token
    private boolean isKeywordOrSpecialToken(String identifier) {
        // For simplicity, let's assume some keywords and special tokens
        List<String> keywordsAndSpecialTokens = List.of("IF", "ELSE", "WHILE", "FOR","CHAR","INT", "FLOAT","BOOL", "RETURN", "BEGIN", "END","CODE","SCAN","DISPLAY","AND","OR","NOT");
        return keywordsAndSpecialTokens.contains(identifier);
    }
    // Function to check if a string is a Boolean Literal
    private boolean isBooleanLiteral(String identifier) {
        // For simplicity, let's assume some keywords and special tokens
        List<String> keywordsAndSpecialTokens = List.of("TRUE","FALSE");
        return keywordsAndSpecialTokens.contains(identifier);
    }

    // Function to check if a character is a delimiter
    private boolean isDelimiter(char c) {
        // For simplicity, let's assume some delimiters
        return "(){},;:".indexOf(c) != -1;
    }
    private boolean isValidIdentifier(String identifier) {
        // Check if the identifier matches the naming convention
        // Case sensitive, starts with a letter or underscore, followed by letters, underscores, or digits
        return identifier.matches("[a-zA-Z_][a-zA-Z0-9_]*");
    }
    private boolean isOperator(char c) {
        // Define your operator characters here
        return "+-*/() =".indexOf(c) != -1;
    }
}