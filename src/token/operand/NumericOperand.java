package token.operand;

import numeric.Numeric;

public class NumericOperand extends Operand {
	public NumericOperand(Numeric numeric) {
		_numeric = numeric;
	}

	/**
	 * 수치값 operand 객체의 정보를 문자열로 반환한다. 디버그 용도로 사용된다.
	 */
	@Override
	public String toString() {
		return "Numeric(" + _numeric.toString() + ")";
	}

	public Numeric getNumeric() {
		return _numeric;
	}

	private final Numeric _numeric;
}
