package token.operand;

import java.util.Optional;

import literal.Literal;

public class LiteralOperand extends Operand {
	public LiteralOperand(Literal literal) {
		_literal = literal;
	}

	/**
	 * 리터럴 operand 객체의 정보를 문자열로 반환한다. 디버그 용도로 사용된다.
	 */
	@Override
	public String toString() {
		String literalName = _literal.getLiteral();

		return "Literal(" + literalName + ")";
	}

	public Optional<Integer> getAddress() {
		return _literal.getAddress();
	}

	public Literal getLiteral(){
		return _literal;
	}

	private final Literal _literal;
}
