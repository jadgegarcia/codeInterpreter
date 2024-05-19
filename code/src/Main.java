import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import lexer.Lexer1;
import lexer.Token;
import lexer.TokenType;
import parser.SyntaxAnalyzer;

public class Main {

    //static Scanner scanner = new Scanner(System.in);

    // Main method
    public static void main(String[] args) {
        // Define the filename containing the CODE program
        String filename = "D:\\CODE\\FINAL\\code\\code\\code\\src\\sourceCode.txt";
        
        try {
            // Create a FileReader to read the file
            FileReader fileReader = new FileReader(filename);
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            // Create a StringBuilder to store the CODE program
            StringBuilder codeBuilder = new StringBuilder();
            String line;
            // Read each line from the file and append it to the StringBuilder
            while ((line = bufferedReader.readLine()) != null) {
                codeBuilder.append(line).append("@\n");
            }

            // Close the BufferedReader
            bufferedReader.close();
            // Create a Lexer instance
            //System.out.println(codeBuilder.toString());
            Lexer1 lexer = new Lexer1(codeBuilder.toString());
            //System.out.println(codeBuilder.toString());
            List<Token> tokens = new ArrayList<>();
//            System.out.println("Tokens:-----------------");
            Token token;
             // Initialize index counter

            do {
                token = lexer.getNextToken();
                tokens.add(token);
                System.out.println(token);
            } while (token.getType() != TokenType.EOF);


            SyntaxAnalyzer syntaxAnalyzer = new SyntaxAnalyzer(tokens);
            syntaxAnalyzer.parse();

        } catch (FileNotFoundException e) {
            System.err.println("File not found: " + filename);
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
        
    }

}
