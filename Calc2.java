/*
Tyler Decker

<expr1>   -> 'let' identifier '=' <expr2> expr2.in           := expr1.in;
                                          expr1.value        := expr2.value
                                          expr1.out          := expr2.out.put(identifier=expr2.value)
           | <term> <term_tail>           term.in            := expr1.in;
                                          term_tail.in       := term.out;
                                          term_tail.subtotal := term.value;
                                          expr1.value        := term_tail.value;
                                          expr1.out          := term_tail.out

<term> -> <factor> <factor_tail>          factor_tail.subtotal:= factor.value;
                                          term.value          := factor_tail.value

<term_tail1> -> '+' <term> <term_tail2>   term_tail2.subtotal := term_tail1.subtotal+term.value;
                                          term_tail1.value    := term_tail2.value
                                          term.in             := term_tail1.in
					                                term_tail2.in       := term.out 
					                                term_tail1.out      := term_tail2.out 
                | '-' <term> <term_tail2> term_tail2.subtotal := term_tail1.subtotal-term.value;
                                          term_tail1.value    := term_tail2.value
                                          term.in             := term_tail1.in
					                                term_tail2.in       := term.out 
					                                term_tail1.out      := term_tail2.out 
                | empty                   term_tail1.value    := term_tail1.subtotal

<factor1> -> '(' <expr> ')'               expr.in            := factor1.in;
                                          factor1.value      := expr.value
                                          factor1.out        := expr.out
           | '-' <factor2>                factor2.in         := factor1.in;
                                          factor1.value      := -factor2.value;
                                          factor1.out        := factor2.out
           | identifier                   factor1.value      := factor1.in.get(identifier)
                                          factor1.out        := factor1.in  
           | number                       factor1.value      := number;
                                          factor1.out        := factor1.in

<factor_tail1> -> '*' <factor> <factor_tail2>  factor_tail2.subtotal := factor_tail1.subtotal*factor.value;
                                               factor_tail1.value    := factor_tail2.value
					                                     factor.in             := factor_tail1.in
					                                     factor_tail2.in       := factor.out
                                               factor_1.out          := factor2.out
                | '/' <factor> <factor_tail2>  factor_tail2.subtotal := factor_tail1.subtotal/factor.value;
                                               factor_tail1.value    := factor_tail2.value
					                                     factor.in             := factor_tail1.in
					                                     factor_tail2.in       := factor.out
                                               factor_1.out          := factor2.out
                | empty                        factor_tail1.value    := factor_tail1.subtotal


/*	Calc.java
	Implementes a calculator for simple expressions
	Uses java.io.StreamTokenizer and recursive descent parsing
	
	Compile:
	javac Calc.java

	Execute:
	java Calc
	or:
	java Calc <filename>
*/

import java.io.*;
import java.util.*;

public class Calc2
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
    
    Hashtable<String,Integer> exprin = new Hashtable<String,Integer>();
    Hashtable<String,Integer> exprout = exprin;


    // parse expression and get calculated value:
    int value = expr(exprin, exprout);

    // check if expression ends with ';' and print value
    if (token == (int)';')
      System.out.println("Value = " + value);
    else
      System.out.println("Syntax error");
  }

  // getToken - advance to the next token on the input
  private static void getToken() throws IOException
  {
    token = tokens.nextToken();
  }

  // expr - parse <expr> -> <term> <term_tail>
  // ***** expr new

  private static int expr
  (Hashtable<String,Integer> exprin, Hashtable<String,Integer> exprout) throws IOException
  {
    if (token == tokens.TT_WORD && tokens.sval.equals("let"))
    {
      getToken(); // advance to identifier
      String id = tokens.sval;
      getToken(); // advance to '='
      getToken(); // advance to <expr>
      int value = expr(exprin, exprout);
      exprout.put(id, new Integer(value));
      return value;
    }
    else
    {
      Hashtable<String,Integer> x = exprin; // Java likes references to be
                                            // initialized
      int subtotal = term(exprin, x);
      return term_tail(subtotal, x, exprout);
    }
  }

  // term - parse <term> -> <factor> <factor_tail>
  // *** new term
  private static int term(Hashtable<String,Integer> termin, Hashtable<String,Integer> termout) throws IOException
  {
    Hashtable<String, Integer> x = termin;
    int subtotal = factor(termin, x);
    return factor_tail(subtotal, x, termout);
  }


  // term_tail - parse <term_tail> -> <add_op> <term> <term_tail> | empty
  /// *** new term tail
  private static int term_tail(int subtotal, Hashtable<String,Integer> termin, Hashtable<String,Integer> termout) throws IOException
  {
    if (token == (int)'+')
    {
      getToken();
      Hashtable<String, Integer> x = termin;
      int termvalue = term(termin, x);
      return term_tail(subtotal + termvalue, x, termout );
    }
    else if (token == (int)'-')
    {
      getToken();
      Hashtable<String, Integer> x = termin;
      int termvalue = term(termin, x);
      return term_tail(subtotal - termvalue, x, termout);
    }
    else
      return subtotal;
  }


  // factor - parse <factor> -> '(' <expr> ')' | '-' <expr> | ID | NUM
  /// ***** new factor
  private static int factor(Hashtable<String,Integer> factorin, Hashtable<String,Integer> factorout) throws IOException
  {
    if (token == (int)'(')
    {
      getToken(); 
      int value = expr(factorin, factorout);
      if (token == (int)')')
        getToken();
      else
        System.out.println("closing ')' expected");
      return value;
    }
    else if (token == (int)'-')
    {
      getToken();
      return -factor(factorin, factorout);
    }
    else if (token == tokens.TT_WORD)
    {
      String id = tokens.sval;
      getToken();
      factorout = factorin;
      return ((Integer)factorin.get(id)).intValue();
    }
    else if (token == tokens.TT_NUMBER)
    {
      getToken();
      factorout = factorin;
      return (int)tokens.nval;
    }
    else
    {
      System.out.println("factor expected");
      return 0;
    }
  }

               
 //**** new  factor_tail - parse <factor_tail> -> <mult_op> <factor>
 //**** <factor_tail> | empty
  private static int factor_tail(int subtotal, Hashtable<String,Integer> factorin, Hashtable<String,Integer> factorout ) throws IOException
  {
    if (token == (int)'*')
    {
      getToken();
      Hashtable<String, Integer> x = factorin;
      int factorvalue = factor(factorin, x);
      return factor_tail(subtotal * factorvalue, x, factorout);
    }
    else if (token == (int)'/')
    {
      getToken();
      Hashtable<String, Integer> x = factorin;
      int factorvalue = factor(factorin, x);
      return factor_tail(subtotal / factorvalue, x, factorout );
    }
    else
      factorout = factorin;
      return subtotal;
  } 



}
