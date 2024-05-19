package Arithmetic;

import java.util.ArrayList;
import java.util.ListIterator;

import lexer.Token;
import lexer.TokenType;

public class ArithParser {
        // to iterate over all tokens in the token list passed from the lexer
        private static ListIterator<Token> tokenIter;

        /**
         * Parse the list of tokens passed in and build a tree to evaluate results
         *
         * @param tokenList ArrayList of all the valid tokens to be parsed
         */
        public static ArithTokenNode parseTokens(ArrayList<Token> tokenList) throws Exception {
            tokenIter = tokenList.listIterator();
            ArithTokenNode result = expression(tokenIter.next());
    
            if (tokenIter.hasNext()) // unreachable / unexpected tokens found
                throw new Exception();
    
            return result;
        }
    
        /**
         * Create an expression from the generated tokens of the input
         */
        static ArithTokenNode expression(Token current) throws Exception {
            ArithTokenNode currentExpr = term(current);
    
            while (tokenIter.hasNext()) {
                current = tokenIter.next();
                // new terms encountered
                if (current.getType() == TokenType.PLUS) {
                    currentExpr = new ArithTokenNode(TokenType.PLUS, currentExpr, term(tokenIter.next()));
                } else if (current.getType() == TokenType.MINUS) {
                    currentExpr = new ArithTokenNode(TokenType.MINUS, currentExpr, term(tokenIter.next()));
                } 


                else if(current.getType() == TokenType.EQUAL){
                    currentExpr = new ArithTokenNode(TokenType.EQUAL, currentExpr, term(tokenIter.next()));
                }

                // unexpected tokens found
                else {
                    tokenIter.previous();
                    break;
                }
            }
            return currentExpr;
        }
    
        /**
         * Create terms from the factors in the expression
         */
        static ArithTokenNode term(Token current) throws Exception {
            ArithTokenNode currentTerm = factor(current);
    
            while (tokenIter.hasNext()) {
                current = tokenIter.next();
                // new factors / expressions encountered
                if (current.getType() == TokenType.MULTIPLY) {
                    currentTerm = new ArithTokenNode(TokenType.MULTIPLY, currentTerm, factor(tokenIter.next()));
                } else if (current.getType() == TokenType.DIVIDE) {
                    currentTerm = new ArithTokenNode(TokenType.DIVIDE, currentTerm, factor(tokenIter.next()));
                } else if (current.getType() == TokenType.PAREN_OPEN) {
                    currentTerm = new ArithTokenNode(TokenType.MULTIPLY, currentTerm, expression(current));
                } else if (current.getType() == TokenType.MODULO) {
                    currentTerm = new ArithTokenNode(TokenType.MODULO, currentTerm, factor(tokenIter.next()));
                }
                // unexpected token found
                else {
                    tokenIter.previous();
                    break;
                }
            }
            return currentTerm;
        }
    
        /**
         * Create a tokenNode for the factors in the terms of the expression
         */
        static ArithTokenNode factor(Token current) throws Exception {
            switch (current.getType()) {
                // an bracket enclosed expression found.
                case PAREN_OPEN: {
                    ArithTokenNode expr = expression(tokenIter.next());
    
                    if (tokenIter.hasNext()) // iterate once more as PAREN_CLOSE encountered
                        tokenIter.next();
    
                    return expr;
                }

                // a +ve expression/number found
                case PLUS: {
                    return factor(tokenIter.next());
                }
                // a -ve expression/number found
                case MINUS: {
                    ArithTokenNode next = factor(tokenIter.next());
                    next.negateNode();
                    return next;
                }
                // a number found
                case INT: {
                    return new ArithTokenNode(current);
                }

                // unexpected token at start
                default: {
                    throw new Exception();
                }
            }
        }
}
