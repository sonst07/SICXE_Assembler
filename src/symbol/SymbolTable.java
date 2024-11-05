package symbol;

import java.util.LinkedHashMap;
import java.util.Optional;
import java.util.stream.Collectors;

import numeric.Numeric;

public class SymbolTable {
	/**
	 * 심볼 테이블 객체를 초기화한다.
	 */
	public SymbolTable() {
		_symbolMap = new LinkedHashMap<String, Symbol>();
		_repSymbol = Optional.empty();
	}

	/**
	 * 주소값이 정해지지 않은 심볼을 추가한다.
	 * 
	 * @param name 심볼 명칭
	 * @return 심볼 객체
	 * @throws RuntimeException 잘못된 심볼 생성 시도
	 */
	public Symbol put(String name) throws RuntimeException {
		// TODO: 예외 처리하기 (exception)

		Symbol symbol = Symbol.createAddressNotAssignedSymbol(name);
		_symbolMap.put(name, symbol);
		return symbol;
	}

	/**
	 * 주소값이 정해진 심볼을 추가한다.
	 * 
	 * @param name    심볼 명칭
	 * @param address 심볼 주소
	 * @return 심볼 객체
	 * @throws RuntimeException 잘못된 심볼 생성 시도
	 */
	public Symbol put(String name, int address) throws RuntimeException {
		// TODO: 심볼 추가하기. 만약 심볼이 이미 존재하고 해당 심볼이 주소가 지정되지 않은 심볼일 경우, 주소값 할당하기.

		Symbol symbol;
		Numeric addrNum = new Numeric(String.valueOf(address));

		Optional<Symbol> optSymbol = search(name);
		if (optSymbol.isPresent()) {
			// TODO: 해당 심볼이 주소가 지정되지 않은 심볼일 경우 주소값 할당하기.
			symbol = optSymbol.get();
			symbol.assign(addrNum);
		} else {
			// TODO: 심볼 추가하기.
			symbol = Symbol.createAddressAssignedSymbol(name, addrNum);
			_symbolMap.put(name, symbol);
		}

		return symbol;
	}

	/**
	 * EQU label에 해당하는 심볼을 추가한다.
	 * 
	 * @param name    심볼 명칭
	 * @param formula 수식 문자열
	 * @param locctr  location counter 값
	 * @return 심볼 객체
	 * @throws RuntimeException 잘못된 심볼 생성 시도 혹은 잘못된 수식 포맷
	 */
	public Symbol put(String name, String formula, int locctr) throws RuntimeException {
		// TODO: 심볼 추가하기. 만약 심볼이 이미 존재하고 해당 심볼이 주소가 지정되지 않은 심볼일 경우, 주소값 할당하기.

		Symbol symbol;
		Numeric addr = new Numeric(formula, this, locctr);
		Optional<Symbol> optSymbol = search(name);

		if (optSymbol.isPresent()) {
			// TODO: 해당 심볼이 주소가 지정되지 않은 심볼일 경우 주소값 할당하기.
			symbol = optSymbol.get();
			symbol.assign(addr);
		} else {
			// TODO: 심볼 추가하기.
			symbol = Symbol.createAddressAssignedSymbol(name, addr);
			_symbolMap.put(name, symbol);
		}

		return symbol;
	}

	/**
	 * control section 명칭에 해당하는 심볼을 추가한다.
	 * 
	 * @param name    심볼 명칭
	 * @param address 심볼 주소
	 * @return 심볼 객체
	 * @throws RuntimeException 잘못된 심볼 생성 시도
	 */
	public Symbol putRep(String name, int address) throws RuntimeException {
		Symbol symbol;

		// TODO: control section 명칭에 해당하는 심볼을 추가하기.
		symbol = Symbol.createRepSymbol(name, address);
		_symbolMap.put(name, symbol);
		_repSymbol = Optional.of(symbol);

		return symbol;
	}

	/**
	 * EXTERN operand에 주어지는 외부 심볼을 추가한다.
	 * 
	 * @param name 심볼 명칭
	 * @return 심볼 객체
	 * @throws RuntimeException 잘못된 심볼 생성 시도
	 */
	public Symbol putRefer(String name) throws RuntimeException {
		Symbol symbol;

		// TODO: EXTERN operand에 주어지는 외부 심볼을 추가하기.
		symbol = Symbol.createExternalSymbol(name);
		_symbolMap.put(name, symbol);

		return symbol;
	}

	/**
	 * 심볼 테이블에서 심볼을 찾는다.
	 * 
	 * @param name 찾을 심볼 명칭
	 * @return 심볼. 없을 경우 empty <code>Optional</code>
	 */
	public Optional<Symbol> search(String name) {
		return Optional.ofNullable(_symbolMap.get(name));
	}

	/**
	 * control section 명칭에 해당하는 심볼을 반환한다.
	 * 
	 * @return 심볼. 없을 경우 empty <code>Optional</code>
	 */
	public Optional<Symbol> getRepSymbol() {
		return _repSymbol;
	}

	/**
	 * 심볼 테이블 객체의 정보를 문자열로 반환한다. 심볼 테이블 출력 용도로 사용한다.
	 */
	public boolean checkTableFormat(Symbol s){
		if(s.isBaseSymbol() || (s.getName().equals("MAXLEN") && _repSymbol.get().getName().equals("COPY")))
			return true;
		else
			return false;
	}

	@Override
	public String toString() {
		String s;
		if(_repSymbol.isPresent()) {
			s = "\t+ " + _repSymbol.get().getName();
		} else {
            s = "";
        }

        String symbols = _symbolMap.entrySet().stream()
				.map(x -> x.getValue().toString() + (checkTableFormat(x.getValue()) ? "" : s))
				.collect(Collectors.joining("\n"));


        return symbols;
	}

	private final LinkedHashMap<String, Symbol> _symbolMap;
	private Optional<Symbol> _repSymbol;
}
