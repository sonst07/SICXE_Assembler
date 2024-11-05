package token;

import java.util.ArrayList;
import java.util.stream.Collectors;

import directive.Directive;
import token.operand.Operand;

public class DirectiveToken extends Token {
	public DirectiveToken(String tokenString, int address, int size, Directive directive, ArrayList<Operand> operands) {
		super(tokenString, address, size);
		_directive = directive;
		_operands = operands;
	}

	/**
	 * DirectiveToken 객체의 정보를 문자열로 반환한다. 디버그 용도로 사용된다.
	 */
	@Override
	public String toString() {
		String directiveName = _directive.getName();
		String operands = _operands.isEmpty() ? "(empty)"
				: (_operands.stream()
						.map(x -> x.toString())
						.collect(Collectors.joining("/")));
		return "DirectiveToken{name:" + directiveName + ", operands:" + operands + "}";
	}

	public Directive getDirective() {
		return _directive;
	}

	public ArrayList<Operand> getOperands() {
		return _operands;
	}

	private Directive _directive;

	private ArrayList<Operand> _operands;
}
