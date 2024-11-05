package token;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.Optional;

import instruction.Instruction;
import literal.Literal;
import literal.LiteralTable;
import numeric.Numeric;
import symbol.Symbol;
import symbol.SymbolTable;
import token.operand.*;

public class InstructionToken extends Token {
	public InstructionToken(String tokenString, int address, int size, Instruction inst, ArrayList<Operand> operands,
			boolean nBit, boolean iBit, boolean xBit, boolean pBit, boolean eBit) {
		super(tokenString, address, size);
		_inst = inst;
		_operands = operands;
		_nBit = nBit;
		_iBit = iBit;
		_xBit = xBit;
		_pBit = pBit;
		_eBit = eBit;
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
	 * 토큰의 Base relative bit가 1인지 여부를 반환한다.
	 * 
	 * @return B bit가 1인지 여부
	 */
	public boolean isB() {
		return false;
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
	 * InstructionToken 객체의 정보를 문자열로 반환한다. 디버그 용도로 사용한다.
	 */
	@Override
	public String toString() {
		String instName = _inst.getName();
		String operands = _operands.isEmpty() ? "(empty)"
				: (_operands.stream()
						.map(x -> x.toString())
						.collect(Collectors.joining("/")));
		String nixbpe = String.format("0b%d%d%d%d%d%d", _nBit ? 1 : 0, _iBit ? 1 : 0, _xBit ? 1 : 0, 0, _pBit ? 1 : 0,
				_eBit ? 1 : 0);
		return "InstructionToken{name:" + instName + ", operands:" + operands + ", nixbpe:" + nixbpe + "}";
	}

	/**
	 * object code에 관한 정보를 반환한다.
	 * 
	 * @return 텍스트 레코드 정보가 담긴 객체
	 * @throws RuntimeException 잘못된 심볼 객체 변환 시도.
	 */
	public TextInfo getTextInfo(SymbolTable symbolTable, LiteralTable literalTable) throws RuntimeException {
		int address;
		int code = 0;
		int size;
		Optional<ModificationInfo> modInfo = Optional.empty();

		// TODO: pass2 과정 중, 오브젝트 코드 생성을 위한 정보를 TextInfo 객체에 담아서 반환하기.
		//		tokenString, address, size);
		//		_inst = inst;
		//		_operands = operands;
		// 		주소있고, inst있고, Operand있고, nixbpe있으니까 합칠 수 있다 / 여기서 합치는 거군

		address = getAddress();
		size = getSize();

		// code 구현부
		if(_inst.getFormat() == Instruction.Format.TWO){
			Instruction.OperandType index = _inst.getOperandType();
			if(index == Instruction.OperandType.REG){
				RegisterOperand r = (RegisterOperand) _operands.getFirst();
				code = _inst.getOpcode() << 8 | r.getValue() << 4;
			}else if(index == Instruction.OperandType.REG1_REG2){
				RegisterOperand r1 = (RegisterOperand) _operands.getFirst();
				RegisterOperand r2 = (RegisterOperand) _operands.get(1);
				code = _inst.getOpcode() << 8 | r1.getValue() << 4 | r2.getValue();
			}
		} else if (_inst.getFormat() == Instruction.Format.THREE_OR_FOUR) {
			int nixbpe = 0;
			if(_eBit){
				try{	// 에러처리부문
					Numeric n = null;
					int oAddress;

					NumericOperand num = (NumericOperand) _operands.getFirst();
					n = num.getNumeric();
					String s = n.getMemoryName();
//					System.out.println(s);
					Optional<Symbol> o = symbolTable.search(s);
					if(o.isEmpty()){
						throw new Exception(s);
					}
				}catch (Exception e) {
					System.out.println("Error : " + e.getMessage());
					System.exit(-1);
				}

				nixbpe = nixbpe | (isE()? 0b1 << 20 : 0) | (isP()? 0b1 << 21 : 0) | (isX()? 0b1 << 23 : 0)
						| (isI()? 0b1 << 24 : 0) | (isN()? 0b1 << 25 : 0);
				code = nixbpe | _inst.getOpcode() << 24;
			}else if(!_nBit && _iBit){
				int calculated = 0;
				Numeric n = null;

				NumericOperand num = (NumericOperand) _operands.getFirst();
				n = num.getNumeric();

				calculated = n.getInteger();
				nixbpe = nixbpe | (isE()? 0b1 << 12 : 0) | (isP()? 0b1 << 13 : 0) | (isX()? 0b1 << 15 : 0)
						| (isI()? 0b1 << 16 : 0) | (isN()? 0b1 << 17 : 0) | calculated;
				code = nixbpe | _inst.getOpcode() << 16;
			}
			else{
				int calculated = 0;
				if(!_operands.isEmpty()){
					Numeric n = null;
					if(_operands.getFirst() instanceof LiteralOperand){
						LiteralOperand l = (LiteralOperand) _operands.getFirst();
						n = l.getLiteral().getValue();
					}else if(_operands.getFirst() instanceof NumericOperand){
						NumericOperand num = (NumericOperand) _operands.getFirst();
						n = num.getNumeric();
					}

                    String s = n.getMemoryName();
					int oAddress = 0;
					if(s == null){ // 이미 계산된 값
						calculated = n.getValue().intValue();
					}else if(s.contains("=")){
						// 주소값
						Optional<Literal> o = literalTable.search(s);
						oAddress = o.get().getAddress().get();
					}else{
						// 값 계산해주기
						Optional<Symbol> o = symbolTable.search(s);
						oAddress = o.get().getAddress().get().getValue().intValue();
					}

					if(oAddress >= address){
						calculated = oAddress - address;
					}else{
						calculated = 0x1000 - (address - oAddress);
					}
				}

				nixbpe = nixbpe | (isE()? 0b1 << 12 : 0) | (isP()? 0b1 << 13 : 0) | (isX()? 0b1 << 15 : 0)
						| (isI()? 0b1 << 16 : 0) | (isN()? 0b1 << 17 : 0) | calculated;
				code = nixbpe | _inst.getOpcode() << 16;

			}
		}

		if(_eBit){
			modInfo = Optional.of(new ModificationInfo(getTokenString(), address, 5));
		}


		TextInfo textInfo = new TextInfo(address, code, size, modInfo);

		return textInfo;
	}

	private Instruction _inst;

	private ArrayList<Operand> _operands;

	private boolean _nBit;
	private boolean _iBit;
	private boolean _xBit;
	// private boolean _bBit; /** base relative는 구현하지 않음 */
	private boolean _pBit;
	private boolean _eBit;
}
