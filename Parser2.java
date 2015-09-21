/*  
Tyler Decker

       Parser.java
        Implements a parser for a calculator language
        Uses java.io.StreamTokenizer and recursive descent parsing

        Compile:
        javac Parser.java
*/

import java.io.*;

/*      Calculator language grammar:

        <expr>          -> <term> <term_tail>
        <term>          -> <factor> <factor_tail>
        <term_tail>     -> <add_op> <term> <term_tail>
                         | empty
        <factor>        -> '(' <expr> ')'
                         | '-' <factor>
        <factor_tail>   -> <mult_op> <factor> <factor_tail>
                         | empty
        <add_op>        -> '+' | '-'
        <mult_op>       -> '*' | '/'

        <power>         -> <power_op> <factor>
                         | identifier
                         | number
        <power_op>      -> '^'

*/


public class Parser2
{ 
  private static StreamTokenizer tokens;
  private static int token;
  public static void main(String argv[]) throws IOException
  {
    InputStreamReader reader;
    if (argv.length > 0)
      reader = new InputStreamReader(new FileInputStream(argv[0]));
    else
      reader = new InputStreamReader(System.in);

    // create the tokenizer:
    tokens = new StreamTokenizer(reader);
    tokens.ordinaryChar('.');
    tokens.ordinaryChar('-');
    tokens.ordinaryChar('/');

    // advance to the first token on the input:
    getToken();

    // check if expression:
    expr();

    // check if expression ends with ';'
    if (token == (int)';')
      System.out.println("Syntax ok");
    else
      System.out.println("Syntax error");
  }

  // getToken - advance to the next token on the input
  private static void getToken() throws IOException
  {
     token = tokens.nextToken();
  }

  // expr - parse <expr> -> <term> <term_tail>
  private static void expr() throws IOException
  { 

    term();
    term_tail();

  }

  // term - parse <term> -> <factor> <factor_tail>
  private static void term() throws IOException
  {

    factor();
    factor_tail();

  }

  // term_tail - parse <term_tail> -> <add_op> <term> <term_tail> | empty
  private static void term_tail() throws IOException
  { 

    if (token == (int)'+' || token == (int)'-')
    { 
      add_op();
      term();
      term_tail();
    }

  }

  // factor - parse <factor> -> '(' <expr> ')' | '-' <factor> | ID '(' <expr>
  // ')' | number '(' <expr> ')' |   ID | NUM


  // factor - parse <factor> -> '-' <power>
  //                          | <power>

  private static void factor() throws IOException
  {

    if (token == (int)'-')
      neg_op();
     
    power();

 
  }


  // factor_tail - parse <factor_tail> -> <mult_op> <factor> <factor_tail> | empty
  private static void factor_tail() throws IOException
  {

    if (token == (int)'*' || token == (int)'/')
    {
      mult_op();
      factor();
      factor_tail();
    }

  }

  // power - parse <power> -> identifer <power_op> <factor> 
  //                        | <power> <power_op>  <factor>
  //                        | '(' <expr> ')'
  //                        | <power> '(' <expr> ')'

  //                        | identifer
  //                        | number
  
  private static void power() throws IOException
  {

    if (token == (int)'(')
    {
      parenthesis_expr();
       if (token == (int)'^')
       {
         power_op();
         factor();
       }

    }
    else if (token == tokens.TT_WORD)
    {  
      getToken(); 
      if (token == (int)'(')
        parenthesis_expr();
      if (token == (int)'^')
      {
        power_op();
        factor();
      }
    }
    else if (token == tokens.TT_NUMBER)
    {
      getToken();
       if (token == (int)'(')
        parenthesis_expr();


       if (token == (int)'^')
       {
         power_op();
         factor();
       }
    }
    else System.out.println("factor expected");
    


  }



  // Function to check for '(' <expr> ')' 
  // used to check 
  // <power> -> identifer '(' <expr> ')'
  //           | number '(' <expr> ')'
  //
  private static void parenthesis_expr() throws IOException
  {

    getToken();
    expr();
    
    if (token == (int)')')
      getToken();
    else System.out.println("closing ')' expected");


  }


  // neg_op 
  private static void neg_op() throws IOException
  {
    if (token == (int)'-')
      getToken();
  }

  // power_op - parse <power_op> -> '^'
  private static void power_op() throws IOException
  {
    if (token == (int)'^')
      getToken();
  }


  // add_op - parse <add_op> -> '+' | '-'
  private static void add_op() throws IOException
  {
    if (token == (int)'+' || token == (int)'-')
      getToken();

  }


  // mult_op - parse <mult_op> -> '*' | '/'
  private static void mult_op() throws IOException
  {
    if (token == (int)'*' || token == (int)'/')
      getToken();


  }
}
