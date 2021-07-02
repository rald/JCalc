import java.util.ArrayList;
import java.util.Scanner;



enum TokenType {
	NUMBER,
	OPER_ADD,
	OPER_SUB,
	OPER_MUL,
	OPER_DIV,
	OPER_POW,
	PAREN_OPEN,
	PAREN_CLOSE,
	EOF
}



class Token {
	TokenType type;
	String value;

	Token(TokenType type,String value) {
		this.type=type;
		this.value=value;
	}

	public String toString() {
		return "{ type: "+this.type+", value: \""+this.value+"\" }";
	}
}



enum LexerState {
	DEFAULT,
	NUMBER,
	OPERATOR,
	PAREN
}



class Lexer {

	static ArrayList<Token> lex(String input) {
		int i=0;
		String text="";
		LexerState lexerState=LexerState.DEFAULT;
		ArrayList<Token> tokens=new ArrayList<Token>();
		boolean isDotPresent=false;
		while(i<input.length()) {
			char ch=input.charAt(i);
			switch(lexerState) {
				case DEFAULT:
					if(ch=='+') {
						tokens.add(new Token(TokenType.OPER_ADD,Character.toString(ch)));
					} else if(ch=='-') {
						tokens.add(new Token(TokenType.OPER_SUB,Character.toString(ch)));
					} else if(ch=='*') {
						tokens.add(new Token(TokenType.OPER_MUL,Character.toString(ch)));
					} else if(ch=='/') {
						tokens.add(new Token(TokenType.OPER_DIV,Character.toString(ch)));
					} else if(ch=='^') {
						tokens.add(new Token(TokenType.OPER_POW,Character.toString(ch)));
					} else if(ch=='(') {
						tokens.add(new Token(TokenType.PAREN_OPEN,Character.toString(ch)));
					} else if(ch==')') {
						tokens.add(new Token(TokenType.PAREN_CLOSE,Character.toString(ch)));
					} else if(Character.isDigit(ch) || ch=='.') {
						lexerState=LexerState.NUMBER;
						i--;
					} else if(Character.isWhitespace(ch)) {

					} else {
						System.out.println("Error: Invalid input.");
						System.exit(1);
					}
					break;
				case NUMBER:
					if(Character.isDigit(ch) || (!isDotPresent && ch=='.')) {
						if(ch=='.') isDotPresent=true;
						text+=Character.toString(ch);
					} else {
						tokens.add(new Token(TokenType.NUMBER,text));
						text="";
						isDotPresent=false;
						lexerState=LexerState.DEFAULT;
						i--;
					}
				break;
			}
			i++;
		}
		switch(lexerState) {
			case NUMBER: tokens.add(new Token(TokenType.NUMBER,text)); break;
		}
		tokens.add(new Token(TokenType.EOF,null));
		return tokens;
	}
}



class Parser {

	ArrayList<Token> tokens;
	int currentTokenIndex=0;

	Parser(ArrayList<Token> tokens) {
		this.tokens=tokens;
	}

	Token getToken() {
		return tokens.get(currentTokenIndex);
	}

	boolean isAddOp() {
		return	getToken().type==TokenType.OPER_ADD ||
				getToken().type==TokenType.OPER_SUB;
	}

	boolean isMulOp() {
		return	getToken().type==TokenType.OPER_MUL ||
				getToken().type==TokenType.OPER_DIV;
	}

	boolean isPowOp() {
		return	getToken().type==TokenType.OPER_POW;
	}

	void expected(TokenType type) {
		System.out.println(type+" expected");
		System.exit(1);
	}

	void match(TokenType type) {
		if(getToken().type==type) {
			currentTokenIndex++;
		} else {
			expected(type);
		}
	}

	double getNum() {
		double value=0;
		if(getToken().type!=TokenType.NUMBER) {
			expected(TokenType.NUMBER);
		}
		value=Double.parseDouble(getToken().value);
		currentTokenIndex++;
		return value;
	}

	double expr() {
		double value=0;
		if(isAddOp()) {
			value=0;
		} else {
			value=term();
		}
		while(getToken().type!=TokenType.EOF && isAddOp()) {
			switch(getToken().type) {
				case OPER_ADD:
					match(TokenType.OPER_ADD);
					value+=term();
				break;
				case OPER_SUB:
					match(TokenType.OPER_SUB);
					value-=term();
				break;
			}
		}
		return value;
	}

	double term() {
		double value=pow();
		while(getToken().type!=TokenType.EOF && isMulOp()) {
			switch(getToken().type) {
				case OPER_MUL:
					match(TokenType.OPER_MUL);
					value*=pow();
				break;
				case OPER_DIV:
					match(TokenType.OPER_DIV);
					value/=pow();
				break;
			}
		}
		return value;
	}

	double pow() {
		double value=factor();
		while(getToken().type!=TokenType.EOF && isPowOp()) {
			switch(getToken().type) {
				case OPER_POW:
					match(TokenType.OPER_POW);
					value=Math.pow(value,factor());
				break;
			}
		}
		return value;
	}

	double factor() {
		double value=0;
		if(getToken().type==TokenType.PAREN_OPEN) {
			match(TokenType.PAREN_OPEN);
			value=expr();
			match(TokenType.PAREN_CLOSE);
		} else {
			value=getNum();
		}
		return value;
	}

	double parse() {
		return expr();
	}
}



class JCalc {

	static String prompt="JCalc> ";

	public static void main(String[] args) {

		boolean quit=false;
		Scanner scanner = new Scanner(System.in);
		String input="";

		while(!quit) {
			System.out.print(prompt);
			input=scanner.nextLine();
			ArrayList<Token> tokens=Lexer.lex(input);
			Parser parser=new Parser(tokens);
			System.out.println(parser.parse());
		}

	}

}

