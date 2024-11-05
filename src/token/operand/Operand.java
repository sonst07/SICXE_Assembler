package token.operand;

public abstract class Operand {
	/**
	 * 메모리 접근에 해당하는 피연산자 타입.
	 */
	public enum MemoryType {
		/**
		 * 심볼, 숫자 혹은 수식 (ex. RDREC, 0, X'F1', BUFEND-BUFFER) 즉 리터럴 빼고 나머지
		 */
		NUMERIC,

		/**
		 * 리터럴 (ex. =C'EOF', =X'F1')
		 */
		LITERAL;

		public static MemoryType distinguish(String str) {
			if (str.charAt(0) == '=')
				return LITERAL;
			return NUMERIC;
		}
	}

	/**
	 * 레지스터 종류.
	 * </ul>
	 */
	public enum Register {
		A(0),

		X(1),

		L(2),

		B(3),

		S(4),

		T(5),

		F(6),

		PC(8),

		SW(9);

		public String getName() {
			return this.name();
		}

		public final int value;

		public static Register stringToRegister(String str) throws RuntimeException {
			if ("A".equals(str))
				return A;
			if ("X".equals(str))
				return X;
			if ("L".equals(str))
				return L;
			if ("B".equals(str))
				return B;
			if ("S".equals(str))
				return S;
			if ("T".equals(str))
				return T;
			if ("F".equals(str))
				return F;
			if ("PC".equals(str))
				return PC;
			if ("SW".equals(str))
				return SW;
			throw new RuntimeException("illegal register name (" + str + ")");
		}

		private Register(int value) {
			this.value = value;
		}
	}
}
