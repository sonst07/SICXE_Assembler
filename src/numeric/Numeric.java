package numeric;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Optional;

import symbol.Symbol;
import symbol.SymbolTable;

public class Numeric {
	// refer 을 담기위한 numeric
	public Numeric(String constant, int n) throws RuntimeException {
		_memoryName = constant;
        _value = new BigInteger("0");
        _relativeMap = new HashMap<>();
    }

	/**
	 * 상수 문자열을 파싱하여, 해당 상수 값으로 수치값 객체를 초기화한다.
	 *
	 * @param constant 상수 문자열
	 * @throws RuntimeException 잘못된 상수 문자열 포맷
	 */
	public Numeric(String constant) throws RuntimeException {
		Numeric numeric = evaluateConstant(constant);

		_value = numeric._value;
		_relativeMap = numeric._relativeMap;
	}

	/**
	 * 수치값 객체를 초기화한다. 수치는 <code>symbol.address + value</code>를 가진다.
	 *
	 * @param value  절대값
	 * @param symbol 상대값에서의 주소 심볼
	 *               BUFFER - 100 같은거
	 */
	public Numeric(int value, Symbol symbol) {
		HashMap<Symbol, Integer> map = new HashMap<Symbol, Integer>();
		map.put(symbol, 1);

		_value = new BigInteger(Integer.toString(value));
		_relativeMap = map;
	}

	/**
	 * 수식을 계산하여, 해당 값으로 수치값 객체를 초기화한다.
	 *
	 * @param formula     수식 문자열
	 * @param symbolTable 심볼 테이블
	 * @param locctr      location counter 값
	 * @throws RuntimeException 잘못된 수식 포맷
	 */
	public Numeric(String formula, SymbolTable symbolTable, int locctr) throws RuntimeException {
		Numeric numeric = evaluateFormula(formula, symbolTable, locctr);

		_value = numeric._value;
		_relativeMap = numeric._relativeMap;
	}

	private Numeric(BigInteger value, HashMap<Symbol, Integer> relativeMap) {
		_value = value;
		_relativeMap = relativeMap;
	}

	/**
	 * 수치값에 단일 심볼이 포함된 경우, 해당 심볼의 명칭을 반환한다.
	 *
	 * @return 심볼의 명칭. 수치값에 심볼이 없거나 하나보다 많은 경우 empty <code>Optional</end>.
	 */
	public Optional<String> getName() {
		if (_relativeMap.size() != 1)
			return Optional.empty();
		String name = _relativeMap.keySet().stream()
				.map(x -> x.getName())
				.collect(Collectors.joining(""));
		return Optional.of(name);
	}

	/**
	 * 절대값을 반환한다. 절대값이 integer 범위를 초과하는 경우 하위 32-bit 값만 반환한다.
	 *
	 * @return 절대값
	 */
	public int getInteger() {
		return _value.intValue();
	}


	/**
	 * 절대값을 저장하기 위해 필요한 크기를 반환한다.
	 *
	 * @return 절대값 크기		// 여기서 나는 literalTable경우만 사용한다
	 */
	public int getSize() {
		if(_memoryName == null){
			return (_value.toString(16).length() + 1) / 2;
		}else if(_memoryName.contains("=C")){
			String convertedResult = String.format("%X", _value);
			return (convertedResult.length() + 1) / 2;
		}
		return 0;
	}

	/**
	 * 수치값이 절대값으로만 이루어졌는지 여부를 반환한다.
	 *
	 * @return 절대값으로만 이루어졌는지 여부
	 */
	public boolean isAbsolute() {
		return _relativeMap.size() == 0;
	}

	/**
	 * 수치값이 단일 심볼의 상대값으로 이루어졌는지 여부를 반환한다.
	 *
	 * @return 단일 심볼의 상대값으로 이루어졌는지 여부
	 */
	public boolean isRelative() {
		if (_relativeMap.size() != 1)
			return false;

		boolean isOnlyOneMultiplier = _relativeMap.values().stream()
				.map(x -> x == 1 ? 1 : 2)
				.reduce(0, (acc, x) -> acc + x) == 1;
		return isOnlyOneMultiplier == true;
	}

	/**
	 * 수치값 객체의 정보를 문자열로 반환한다. 디버그 용도로 사용한다.
	 */
	@Override
	public String toString() {
		String value = String.format("0x%s", _value.toString(16).toUpperCase());
		String relative = _relativeMap.entrySet().stream()
				.map(x -> {
					String symbolName = x.getKey().getName();
					int multiplier = x.getValue();

					String sign = multiplier > 0 ? "+" : "-";
					String mulStr = Math.abs(multiplier) == 1 ? "" : "*" + multiplier;

					return sign + symbolName + mulStr;
				})
				.collect(Collectors.joining(""));
		return value.equals("") ? relative.substring(1) : value + relative;
	}

	/**
	 * 상수 문자열을 파싱하고, 해당 상수를 절대값으로 가지는 수치값 객체를 반환한다. 파싱할 수 있는 상수는 캐릭터
	 * 문자열(ex. <code>C'EOF'</code>), 16진수 숫자(ex. <code>X'F1'</code>), 10진수 숫자(ex.
	 * <code>3</code>)이다.
	 *
	 * @param constant 상수 문자열
	 * @return 수치값 객체
	 * @throws RuntimeException 잘못된 상수 문자열 포맷
	 */
	private Numeric evaluateConstant(String constant) throws RuntimeException {
		// TODO: 상수 문자열을 파싱하여 수치값 객체를 생성 및 반환하기.

		// ex. 상수 문자열이 "3"인 경우 Numeric{_value:3, _relativeMap:[]}을 반환하기.
		// ex. 상수 문자열이 "C'EOF'"인 경우 Numeric{_value:0x454F46, _relativeMap:[]}을 반환하기.
		// ex. 상수 문자열이 "X'F1'"인 경우 Numeric{_value:0xF1, _relativeMap:[]}을 반환하기.

		BigInteger value;
		HashMap<Symbol, Integer> hashMap = new HashMap<>();

		// =C'EOF'케이스 처리
		if(constant.charAt(0) == '=' && constant.charAt(1) == 'C'){
			// 음 BigInteger이 숫자만 받고 문자열은 안받는구나. 일단 그래서 십진수로만 저장했다
			String[] split = constant.split("'");

			byte[] getBytesFromString = split[1].getBytes(StandardCharsets.UTF_8);
			value = new BigInteger(getBytesFromString);

			_memoryName = constant;

		}
		else if(constant.contains("'")){	// =X'F1'케이스
			String[] split = constant.split("'");

			value = BigInteger.valueOf(Integer.parseInt(split[1],16));

			_memoryName = constant;
		}
		else{	// 그외
			value = new BigInteger(constant);
		}

		return new Numeric(value, hashMap);
	}

	private static int isPlusMinus(String s) throws RuntimeException {
		if(s.contains("+"))
			return 1;
		else if(s.contains("-"))
			return 2;

		return 0;
	}

	/**
	 * 수식 문자열을 파싱하고, 해당 수식을 계산하여 수치값 객체로 반환한다. 수식에는 피연산자로 괄호, 특수 기호(<code>*</code>),
	 * 심볼, 상수가 포함될 수 있으며, 이항연산자로 +, -가 포함될 수 있으며, 단항연산자로 -가 포함될 수 있다.
	 *
	 * @param formula     수식 문자열
	 * @param symbolTable 심볼 테이블
	 * @param locctr      location counter 값
	 * @return 수치값 객체
	 * @throws RuntimeException 잘못된 수식 포맷
	 */
	private Numeric evaluateFormula(String formula, SymbolTable symbolTable, int locctr)
			throws RuntimeException {
		// TODO: 수식을 계산하여 수치값 객체를 생성 및 반환하기.
		// Q. evaluateOperand 함수와 차이점은?		여기서 나는 단지 +, -연산만 해주었다. 나머지는 ( ), -단항은 제외

		// ex. 피연산자 문자열이 "BUFEND"인 경우 Numeric{_value:0, _relativeMap:[(BUFEND),+1)]}을
		// 반환하기.
		// Q. 피연산자 문자열이 "*"인 경우는??	ok

		// 위에거와 합쳤다

		Numeric n = null;

		int whichNum = isPlusMinus(formula);
		if(whichNum != 0){
			try {
				String[] split = formula.split("[-+]");
				int a = symbolTable.search(split[0]).get().getAddress().get().getInteger();
				int b = symbolTable.search(split[1]).get().getAddress().get().getInteger();
				if (whichNum == 1) {
					n = new Numeric(String.valueOf(a + b));
				} else if (whichNum == 2) {
					n = new Numeric(String.valueOf(a - b));
				}
				_memoryName = formula;
			}catch (RuntimeException e){
				_memoryName = formula;
				n = new Numeric("0");
			}
		}
		else if(formula.contains("*")){
			n = new Numeric(String.valueOf(locctr));
		}else{
			// BUFEND같은 케이스 해주기
			_memoryName = formula;
			n = new Numeric(String.valueOf(locctr));
		}
		return n;
	}



	/**
	 * 피연산자로 시작하는 수식 문자열에서 피연산자의 길이를 반환한다.
	 *
	 * @param formula 피연산자로 시작하는 수식 문자열
	 * @return 피연산자의 길이
	 * @throws RuntimeException 잘못된 수식 포맷
	 */
	private static int getOperandLength(String formula) throws RuntimeException {
		// TODO: 함수를 구현하고, evaluateOperand 혹은 evaluateFormula 함수에서 사용하기.

		return formula.length();
	}

	// String _memoryName 반환
	public String getMemoryName() {
		return _memoryName;
	}

	public BigInteger getValue(){
		return _value;
	}


	/** C'EOFFFFFFFFFFFFFFFFFFFFF'와 같은 입력에서도 동작하도록 하기 위하여 BigInteger를 사용함 */
	private String _memoryName;
	private final BigInteger _value;

	private final HashMap<Symbol, Integer> _relativeMap;
}
