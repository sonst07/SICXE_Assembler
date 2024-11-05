package token.operand;

public class RegisterOperand extends Operand {
	public RegisterOperand(Register register) {
		_register = register;
	}

	/**
	 * 레지스터 operand 객체의 정보를 문자열로 반환한다. 디버그 용도로 사용된다.
	 */
	@Override
	public String toString() {
		String registerName = _register.getName();

		return "Register(" + registerName + ")";
	}

	public int getValue() {
		return _register.value;
	}

	private final Register _register;
}
