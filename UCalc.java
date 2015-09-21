/*
Tyler Decker 
td09


<expr>         -> <term> <term_tail>           term_tail.subtotal := term.value;
                                               expr.value := term_tail.value

<factor1>      -> '(' <expr> ')'               factor1.value := expr.value
                | '-' <factor2>                factor1.value := -factor2.value
                | number                       factor1.value := number
///////////
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
		                          term_tail1.out      := term_tail1.in

<factor> -> <power> <factor>              power_tail.subtoatl := power.value
                                          factor.value        := power_tail.value
           | '-' <factor2>                factor2.in          := factor1.in;
                                          factor1.value       := -factor2.value;
                                          factor1.out         := factor2.out
           | '(' <expr> ')'               expr.in             := power.in;
                                          factor.value        := expr.value
				                                  factor.out          := expr.out
           | identifier                   factor.value        := factor.in.get(identifier)
                                          factor.out          := factor.in  
           | number                       factor.value        := number;
                                          factor.out          := factor.in
					  

<factor_tail1> -> '*' <factor> <factor_tail2>  factor_tail2.subtotal := factor_tail1.subtotal*factor.value;
                                               factor_tail1.value    := factor_tail2.value
					                                     factor.in             := factor_tail1.in
					                                     factor_tail2.in       := factor.out
                                               factor_tail1.out      := factor_tail2.out
                | '/' <factor> <factor_tail2>  factor_tail2.subtotal := factor_tail1.subtotal/factor.value;
                                               factor_tail1.value    := factor_tail2.value
					                                     factor.in             := factor_tail1.in
					                                     factor_tail2.in       := factor.out
                                               factor_tail1.out      := factor_tail2.out
                | empty                        factor_tail1.value    := factor_tail1.subtotal
		                                           factor_tail1.out      := factor_tail1.in

					  
<power> -> '^' <factor>                       power.out              := (int)Math.pow(power.base, factor.value)
                                              factor.in              := power.in
                                            


---------------------------------------
---- Exception that can be thrown -----
---------------------------------------

-- syntax errors --
 Missing 
  ')' expected
  '=' expected
  operator expected
  factor expected  

-- runtime error --

  undefined identifier / variable


*/

import java.io.*;
import java.util.*;
import java.lang.Math;

public class UCalc
{


  static class SyntaxError extends Exception 
  {
    public SyntaxError()
    {
      System.out.println("syntax error: ");
      System.exit(0);
    }
  
    public SyntaxError(String message)
    {
      System.out.println("syntax error: " + message);
      System.exit(0);
    }

  }

  static class RuntimeError extends Exception
  {
    public RuntimeError()
    {
      System.out.println("runtime error: ");
      System.exit(0);
    }

    public RuntimeError(String message)
    {

      System.out.println("runtime error: " + message);
      System.exit(0);
    }


  }







  private static StreamTokenizer tokens;
  private static int token;


  public static void main(String argv[]) throws IOException, SyntaxError, RuntimeError
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
      throw new SyntaxError("operator expected");
  }

  // getToken - advance to the next token on the input
  private static void getToken() throws IOException, SyntaxError, RuntimeError
  {
    token = tokens.nextToken();
  }

  // expr - parse <expr> -> <term> <term_tail>
  // ***** expr new

  private static int expr
  (Hashtable<String,Integer> exprin, Hashtable<String,Integer> exprout) throws IOException, SyntaxError, RuntimeError
  {
    if (token == tokens.TT_WORD && tokens.sval.equals("let"))
    {
      getToken(); // advance to identifier
      String id = tokens.sval;
      getToken(); // advance to '='
      if (token != (int)'=')
            throw new SyntaxError(" '=' expected");
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
  private static int term(Hashtable<String,Integer> termin, Hashtable<String,Integer> termout) throws IOException, SyntaxError, RuntimeError
  {
    Hashtable<String, Integer> x = termin;
    int subtotal = factor(termin, x);
    return factor_tail(subtotal, x, termout);
  }
  



  // term_tail - parse <term_tail> -> <add_op> <term> <term_tail> | empty
  /// *** new term tail
  private static int term_tail(int subtotal, Hashtable<String,Integer> termin, Hashtable<String,Integer> termout) throws IOException, SyntaxError, RuntimeError
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
  private static int factor(Hashtable<String,Integer> factorin, Hashtable<String,Integer> factorout) throws IOException, SyntaxError, RuntimeError
  {
      
   Hashtable<String,Integer> x = factorin;
   int value;
 
    if (token == (int)'-')
    {
      getToken();
      return -factor(factorin, factorout);
    }    

    else if (token == (int)'(')
    {
      getToken(); 
      value = expr(factorin, factorout);
      if (token == (int)')')
        getToken();
      else
	  throw new SyntaxError("closing ')' expected");
      
      if (token == (int)'^')
	return power(value, x, factorout);
    
      return value;
    }
    
    else if (token == tokens.TT_WORD)
    {
      String id = tokens.sval;
      getToken();
      try
      {
        value = ((Integer)factorin.get(id)).intValue();
      }
      catch(Exception e)
      {
        throw new RuntimeError(" '" + id + "' undefined");
      }

      if (token == (int)'^')
	  return power(value, x, factorout);

      factorout = factorin;
      return value;
    }
    else if (token == tokens.TT_NUMBER)
    {
      getToken();
      value = (int)tokens.nval;
      if (token == (int)'^')
	return power(value, x, factorout);

      factorout = factorin;
      return value;
    }
    else
    {
      throw new SyntaxError("factor expected");

    }
  }

  

  //**** new  factor_tail - parse <factor_tail> -> <mult_op> <factor>
 //**** <factor_tail> | empty
  private static int factor_tail(int subtotal, Hashtable<String,Integer> factorin, Hashtable<String,Integer> factorout ) throws IOException, SyntaxError, RuntimeError
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

    private static int power(int base, Hashtable<String,Integer> powerin, Hashtable<String,Integer> powerout ) throws IOException, SyntaxError, RuntimeError
    {
	getToken();
	return (int)Math.pow(base, factor(powerin, powerout));
    }
 


}

