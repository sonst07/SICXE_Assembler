package token;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

public class StringToken {
	/**
	 * 소스 코드 한 줄에 해당하는 토큰을 초기화한다.
	 * 
	 * @param input 소스 코드 한 줄에 해당하는 문자열
	 * @throws RuntimeException 잘못된 형식의 소스 코드 파싱 시도.
	 */
	public StringToken(String input) throws RuntimeException {
		// TODO: 소스 코드를 파싱하여 토큰을 초기화하기.
		_nBit = _iBit = _pBit = true;
		_xBit = _eBit = false;

		String[] split = input.split("\t");

		_label = Optional.empty();
		_operator = Optional.empty();
		_operands = new ArrayList<>();
		_comment = Optional.empty();

		if(split[0].equals(".")) return;

		if(split.length >= 2){
			if(split[0].isEmpty()){
				_label = Optional.empty();
			}else{
				_label = Optional.of(split[0]);
			}

			if(split[1].charAt(0) == '+'){
				_operator = Optional.of(split[1].substring(1));
				_eBit = true;
				_pBit = false;
			}else{
				_operator = Optional.of(split[1]);
			}

		}

		if(split.length >= 3){
			_operands =	getCommaArray(split[2]);
		}

		if(split.length >= 4){
			if(split[2].isEmpty())
				_pBit = false;

			_comment = Optional.of(split[3]);

		}

		System.out.println(this.toString()); /** 디버깅 용도 */
	}

	public ArrayList<String> getCommaArray(String s){
		if(s == null || s.isEmpty()){
			return new ArrayList<>();
		}
		String[] split = s.split(",");

		if(split[0].charAt(0) == '@'){
			_iBit = false;
			split[0] = split[0].substring(1);
		}else if(split[0].charAt(0) == '#'){
			_nBit = false;
			_pBit = false;
			split[0] = split[0].substring(1);
		}

		if(split.length != 1 && split[1] != null){
			if(split[1].equals("X")){
				_xBit = true;
			}
		}

        return new ArrayList<>(Arrays.asList(split));
	}

	/**
	 * label 문자열을 반환한다.
	 * 
	 * @return label 문자열. 없으면 empty <code>Optional</code>.
	 */
	public Optional<String> getLabel() {
		return _label;
	}

	/**
	 * operator 문자열을 반환한다.
	 * 
	 * @return operator 문자열. 없으면 empty <code>Optional</code>.
	 */
	public Optional<String> getOperator() {
		return _operator;
	}

	/**
	 * operand 문자열 배열을 반환한다.
	 * 
	 * @return operand 문자열 배열
	 */
	public ArrayList<String> getOperands() {
		return _operands;
	}

	/**
	 * comment 문자열을 반환한다.
	 * 
	 * @return comment 문자열. 없으면 empty <code>Optional</code>.
	 */
	public Optional<String> getComment() {
		return _comment;
	}

	/**
	 * 토큰의 iNdirect bit가 1인지 여부를 반환한다.
	 * 
	 * @return N bit가 1인지 여부
	 */
	public boolean isN() {
		return _nBit;
	}

	/**
	 * 토큰의 Immediate bit가 1인지 여부를 반환한다.
	 * 
	 * @return I bit가 1인지 여부
	 */
	public boolean isI() {
		return _iBit;
	}

	/**
	 * 토큰의 indeX bit가 1인지 여부를 반환한다.
	 * 
	 * @return X bit가 1인지 여부
	 */
	public boolean isX() {
		return _xBit;
	}

	/**
	 * 토큰의 Pc relative bit가 1인지 여부를 반환한다.
	 * 
	 * @return P bit가 1인지 여부
	 */
	public boolean isP() {
		return _pBit;
	}

	/**
	 * 토큰의 Extra bit가 1인지 여부를 반환한다.
	 * 
	 * @return E bit가 1인지 여부
	 */
	public boolean isE() {
		return _eBit;
	}

	/**
	 * StringToken 객체의 정보를 문자열로 반환한다. 디버그 용도로 사용한다.
	 */
	@Override
	public String toString() {
		String label = _label.map(x -> "<" + x + ">").orElse("(no label)");
		String operator = (isE() ? "+" : "") + _operator.map(x -> "<" + x + ">").orElse("(no operator)");
		String operand = (isN() && !isI() ? "@" : "") + (isI() && !isN() ? "#" : "")
				+ (_operands.isEmpty() ? "(no operand)"
						: "<" + _operands.stream().collect(Collectors.joining("/")) + ">")
				+ (isX() ? (_operands.isEmpty() ? "X" : "/X") : "");
		String comment = _comment.map(x -> "<" + x + ">").orElse("(no comment)");

		String formatted = String.format("%-12s\t%-12s\t%-18s\t%s", label, operator, operand, comment);
		return formatted;
	}

	private Optional<String> _label;
	private Optional<String> _operator;
	private ArrayList<String> _operands;
	private Optional<String> _comment;

	private boolean _nBit;
	private boolean _iBit;
	private boolean _xBit;
	// private boolean _bBit; /** base relative는 구현하지 않음 */
	private boolean _pBit;
	private boolean _eBit;
}
