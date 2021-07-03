import java.util.ArrayList;
import java.util.Scanner;
import java.util.Stack;



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

	static final int DEBUG=0;

	ArrayList<Token> tokens;
	int currentTokenIndex=0;
	Stack<Double> stack=new Stack<Double>();
	double d0=0,d1=0,value=0;

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
		if(DEBUG==1) System.out.println("match: "+type+" == "+getToken().type);
		if(getToken().type!=type) {
			expected(type);
		}
		currentTokenIndex++;
	}

	double getNum() {
		if(DEBUG==1) System.out.print("getNum: ");
		double value=0;
		if(getToken().type!=TokenType.NUMBER) {
			expected(TokenType.NUMBER);
		}
		value=Double.parseDouble(getToken().value);
		if(DEBUG==1) System.out.println(value);
		currentTokenIndex++;
		return value;
	}

	void add() {
		if(DEBUG==1) System.out.println("add");
		match(TokenType.OPER_ADD);
		term();
		d0+=stack.pop();
	}

	void sub() {
		if(DEBUG==1) System.out.println("sub");
		match(TokenType.OPER_SUB);
		term();
		d0=d0-stack.pop();
		d0=-d0;
	}

	void mul() {
		if(DEBUG==1) System.out.println("mul");
		match(TokenType.OPER_MUL);
		power();
		d0*=stack.pop();
	}

	void div() {
		if(DEBUG==1) System.out.println("div");
		match(TokenType.OPER_DIV);
		power();
		d0=stack.pop()/d0;
	}

	void pow() {
		if(DEBUG==1) System.out.println("pow");
		match(TokenType.OPER_POW);
		factor();
		d0=Math.pow(stack.pop(),d0);
	}

	void term() {
		if(DEBUG==1) System.out.println("term");
		factor();
		term1();
	}

	void firstTerm() {
		if(DEBUG==1) System.out.println("firstTerm");
		signedFactor();
		term1();
	}

	void term1() {
		if(DEBUG==1) System.out.println("term1");
		power();
		while(isMulOp()) {
			stack.push(d0);
			switch(getToken().type) {
				case OPER_MUL: mul(); break;
				case OPER_DIV: div(); break;
			}
		}
	}

	void power() {
		if(DEBUG==1) System.out.println("power");
		while(isPowOp()) {
			stack.push(d0);
			switch(getToken().type) {
				case OPER_POW: pow(); break;
			}
		}
	}

	void expr() {
		if(DEBUG==1) System.out.println("expr");
		firstTerm();
		while(isAddOp()) {
			stack.push(d0);
			switch(getToken().type) {
				case OPER_ADD: add(); break;
				case OPER_SUB: sub(); break;
			}
		}
	}

	void factor() {
		if(DEBUG==1) System.out.println("factor");
		if(getToken().type==TokenType.PAREN_OPEN) {
			match(TokenType.PAREN_OPEN);
			expr();
			match(TokenType.PAREN_CLOSE);
		} else {
			d0=getNum();
		}
	}

	void signedFactor() {
		if(DEBUG==1) System.out.println("signedFactor");
		int sign=1;
		while(isAddOp()) {
			switch(getToken().type) {
				case OPER_ADD: match(TokenType.OPER_ADD); break;
				case OPER_SUB: match(TokenType.OPER_SUB); sign*=-1; break;
			}
		}
		factor();
		d0*=sign;
	}

	double parse() {
		if(DEBUG==1) System.out.println("parse");
		expr();
		match(TokenType.EOF);
		return d0;
	}

}



class JCalc {

	static String prompt="JCalc> ";

	public static void main(String[] args) {

		Scanner scanner = new Scanner(System.in);
		String input="";

		for(;;) {
			System.out.print(prompt);
			input=scanner.nextLine().trim();
			if(input.isEmpty()) break;
			ArrayList<Token> tokens=Lexer.lex(input);

			for(int i=0;i<tokens.size();i++) {
				System.out.println(i+" "+tokens.get(i));
			}

			Parser parser=new Parser(tokens);
			System.out.println(parser.parse());
		}

		System.out.println("Bye!");

	}

}

