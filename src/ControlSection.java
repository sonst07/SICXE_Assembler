import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import directive.Directive;
import instruction.*;
import literal.*;
import symbol.*;
import token.*;
import token.operand.*;
import numeric.Numeric;

public class ControlSection {
	/**
	 * pass1 작업을 수행한다. 기계어 목록 테이블을 통해 소스 코드를 토큰화하고, 심볼 테이블 및 리터럴 테이블을 초기화환다.
	 *
	 * @param instTable 기계어 목록 테이블
	 * @param input     하나의 control section에 속하는 소스 코드. 마지막 줄은 END directive를 강제로
	 *                  추가하였음.
	 * @throws RuntimeException 소스 코드 컴파일 오류.
	 */
	public ControlSection(InstructionTable instTable, ArrayList<String> input) throws RuntimeException {
		// control section별로 초기화를 해준다
		// StringToken()을 통해 토큰을 파싱해준다
		List<StringToken> stringTokens = input.stream()
				.map(x -> new StringToken(x))
				.collect(Collectors.toList());

		// pass1을 위한 변수들 초기화
		SymbolTable symTab = new SymbolTable();
		LiteralTable litTab = new LiteralTable();
		ArrayList<Token> tokens = new ArrayList<Token>();
		int locctr = 0;

		for (StringToken stringToken : stringTokens) {
			// 만약 operator가 없는데 operand나 label이 있으면 에러처리
			if (stringToken.getOperator().isEmpty()) {
				boolean isLabelEmpty = stringToken.getLabel().isEmpty();
				boolean isOperandEmpty = stringToken.getOperands().isEmpty();
				if (!isLabelEmpty || !isOperandEmpty)
					throw new RuntimeException("missing operator\n\n" + stringToken.toString());
				continue;
			}

			String operator = stringToken.getOperator().get();
			// operator가 명령어 테이블에 있는지 확인한다
			Optional<Instruction> optInst = instTable.search(operator);
			boolean isOperatorInstruction = optInst.isPresent();
			// 만약 명령어테이블에 일치항목이 있으면
			if (isOperatorInstruction) {
				Token token = handlePass1InstructionStep(optInst.get(), stringToken, locctr, symTab, litTab);
				locctr = token.getAddress();
				tokens.add(token);
				System.out.println(token.toString()); /** 디버깅 용도 */
			}
			// 그렇지 않으면
			else {
				Token token = handlePass1DirectiveStep(stringToken, locctr, symTab, litTab);
				locctr = token.getAddress();
				tokens.add(token);
				System.out.println(token.toString()); /** 디버깅 용도 */
				// 만약 추가한다면 LTORG, END는 여기서 추가한다
			}
		}
		// 결국 위에서는 심볼테이블, 리터럴테이블, LOCCTR, nixbpe을 할당시키는 것이군
		_tokens = tokens;
		_symbolTable = symTab;
		_literalTable = litTab;
	}

	/**
	 * pass2 작업을 수행한다. pass1에서 초기화한 토큰 테이블, 심볼 테이블 및 리터럴 테이블을 통해 오브젝트 코드를 생성한다.
	 *
	 * @return 해당 control section에 해당하는 오브젝트 코드 객체
	 * @throws RuntimeException 소스 코드 컴파일 오류.
	 */
	public ObjectCode buildObjectCode() throws RuntimeException {
		ObjectCode objCode = new ObjectCode();
		Optional<Symbol> optRepSymbol = _symbolTable.getRepSymbol();
		if (optRepSymbol.isEmpty())
			throw new RuntimeException("invalid operation");
		Symbol repSymbol = optRepSymbol.get();

		for (Token token : _tokens) {
			// 명령어 토큰일 경우
			if (token instanceof InstructionToken) {
				handlePass2InstructionStep(objCode, (InstructionToken) token, _symbolTable, _literalTable);
			} // 지시어 토큰일 경우
			else if (token instanceof DirectiveToken) {
				handlePass2DirectiveStep(objCode, (DirectiveToken) token, repSymbol, _symbolTable, _literalTable);
			} else
				throw new RuntimeException("invalid operation");
		}

		return objCode;
	}

	/**
	 * 심볼 테이블 객체의 정보를 문자열로 반환한다. Assembler.java에서 심볼 테이블 출력 용도로 사용한다.
	 *
	 * @return 심볼 테이블의 정보를 담은 문자열
	 */
	public String getSymbolString() {
		return _symbolTable.toString();
	}

	/**
	 * 리터럴 테이블 객체의 정보를 문자열로 반환한다. Assembler.java에서 리터럴 테이블 출력 용도로 사용한다.
	 *
	 * @return 리터럴 테이블의 정보를 담은 문자열
	 */
	public String getLiteralString() {
		return _literalTable.toString();
	}

	/**
	 * pass1에서 operator가 instruction에 해당하는 경우에 대해서 처리한다. label 및 operand에 출현한 심볼 및
	 * 리터럴을 심볼 테이블 및 리터럴 테이블에 추가하고, 문자열 형태로 파싱된 토큰을 InstructionToken으로 가공하여 반환한다.
	 *
	 * @param inst   기계어 정보
	 * @param token  문자열로 파싱된 토큰
	 * @param locctr location counter 값
	 * @param symTab 심볼 테이블
	 * @param litTab 리터럴 테이블
	 * @return 가공된 InstructionToken 객체
	 * @throws RuntimeException 잘못된 명령어 사용 방식.
	 */
	private static InstructionToken handlePass1InstructionStep(Instruction inst, StringToken token,
				   int locctr, SymbolTable symTab, LiteralTable litTab) throws RuntimeException {
		Instruction.Format format = inst.getFormat();
		Instruction.OperandType operandType = inst.getOperandType();

		int size;
		ArrayList<Operand> operands = new ArrayList<>();
		boolean isN = token.isN();
		boolean isI = token.isI();
		boolean isX = token.isX();
		boolean isP = token.isP();
		boolean isE = token.isE();

		// TODO: label을 심볼 테이블에 추가하기.
		if(token.getLabel().isPresent()){
			String s = token.getLabel().get();
			symTab.put(s, locctr);
		}

		System.out.printf("%X	", locctr);

		switch (operandType) {
			case NO_OPERAND:
				// TODO: operand가 없어야 하는 경우에 대해서 처리하기.
                break;

			case MEMORY:
				// TODO: operand로 MEMORY 하나만 주어져야 하는 경우에 대해서 처리하기.
				String opd0 = token.getOperands().getFirst();
				Operand.MemoryType memoryType = Operand.MemoryType.distinguish(opd0);

				switch (memoryType) {
					case NUMERIC:
						// TODO: operand로 상수 혹은 심볼이 주어지는 경우에 대해서 처리하기.
						Numeric n;
						if(isNotFormula(opd0)){
							n = new Numeric(opd0);
						}else{
							n = new Numeric(opd0, symTab, locctr);	// 아니지 타입에 따라서 다르지
						}
						NumericOperand no = new NumericOperand(n);
						operands.add(no);
						break;

					case LITERAL:
						// TODO: operand로 리터럴이 주어지는 경우에 대해서 처리하기.
						Literal l = litTab.putLiteral(opd0);
						LiteralOperand lo = new LiteralOperand(l);
						operands.add(lo);

						break;

					default:
						throw new UnsupportedOperationException("not fully support Operand.MemoryType");
				}
				break;

			case REG:
				// TODO: operand로 REGISTER 하나만 주어져야 하는 경우에 대해서 처리하기.
				String s = token.getOperands().getFirst();
				Operand.Register r = Operand.Register.stringToRegister(s);
				RegisterOperand ro = new RegisterOperand(r);

				operands.add(ro);
				break;

			case REG1_REG2:
				// TODO: operand로 REGISTER 두개가 주어져야 하는 경우에 대해서 처리하기.
				String s1 = token.getOperands().get(0);
				String s2 = token.getOperands().get(1);
				Operand.Register r1 = Operand.Register.stringToRegister(s1);
				Operand.Register r2 = Operand.Register.stringToRegister(s2);
				RegisterOperand ro1 = new RegisterOperand(r1);
				RegisterOperand ro2 = new RegisterOperand(r2);

				operands.add(ro1);
				operands.add(ro2);
				break;

			default:
				throw new UnsupportedOperationException("not fully support InstructionInfo.OperandType");
		}

		switch (format) {
			case TWO:
				// TODO: size = 2?;
				size = 2;
				locctr += 2;
				break;

			case THREE_OR_FOUR:
				// TODO: size = 3 or 4?;
				String thisOperator = token.getOperator().get();

				if(isE){
					size = 4;
					locctr += size;
				}else{
					size = 3;
					locctr += size;
				}

				break;

			default:
				throw new UnsupportedOperationException("not fully support InstructionInfo.Format");
		}

		String para = "";

		if(!token.getOperands().isEmpty()){
			para = token.getOperands().getFirst();
		}

		return new InstructionToken(para, locctr, size, inst, operands, isN, isI, isX, isP, isE);
	}

	/**
	 * pass1에서 operator가 directive에 해당하는 경우에 대해서 처리한다. label 및 operand에 출현한 심볼을 심볼
	 * 테이블에 추가하고, 주소가 지정되지 않은 리터럴을 리터럴 테이블에서 찾아 주소를 할당하고, 문자열 형태로 파싱된 토큰을
	 * DirectiveToken으로 가공하여 반환한다.
	 *
	 * @param token  문자열로 파싱된 토큰
	 * @param locctr location counter 값
	 * @param symTab 심볼 테이블
	 * @param litTab 리터럴 테이블
	 * @return 가공된 DirectiveToken 객체
	 * @throws RuntimeException 잘못된 지시어 사용 방식.
	 */
	private static DirectiveToken handlePass1DirectiveStep(StringToken token,
				   int locctr, SymbolTable symTab, LiteralTable litTab) throws RuntimeException {
		String operator = token.getOperator().get();

		Directive directive;
		try {
			directive = Directive.stringToDirective(operator);
		} catch (RuntimeException e) {
			throw new RuntimeException(e.getMessage() + "\n\n" + token.toString());
		}

		int size = 0;
		ArrayList<Operand> operands = new ArrayList<>();
		Numeric numeric;
		NumericOperand numericOperand;

		System.out.printf("%X	", locctr);

		switch (directive) {
			case START:
				// TODO: START인 경우에 대해서 pass1 처리하기.
				locctr = Integer.parseInt(token.getOperands().getFirst());
				symTab.putRep(token.getLabel().get(), locctr);
				numeric = new Numeric(String.valueOf(locctr));
				numericOperand = new NumericOperand(numeric);
				operands.add(numericOperand);
				break;

			case CSECT:
				// TODO: CSECT인 경우에 대해서 pass1 처리하기.
				locctr = 0;
				symTab.putRep(token.getLabel().get(), locctr);
				break;

			case EXTDEF:
				// TODO: EXTDEF인 경우에 대해서 pass1 처리하기.
				for(int i = 0; i < token.getOperands().size(); i++){
					numeric = new Numeric(token.getOperands().get(i), symTab, locctr);
					numericOperand = new NumericOperand(numeric);
					operands.add(numericOperand);
				}
				break;

			case EXTREF:
				// TODO: EXTREF인 경우에 대해서 pass1 처리하기.
				for(String s : token.getOperands()){
//					System.out.println("ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ" + s + "ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ");
					symTab.putRefer(s);
					operands.add(new NumericOperand(new Numeric(s, 7)));
				}
				break;

			case BYTE:
				// TODO: BYTE인 경우에 대해서 pass1 처리하기.
				size = 1;
				symTab.put(token.getLabel().get(), locctr);
				locctr += 1;
				numeric = new Numeric(token.getOperands().getFirst());
				numericOperand = new NumericOperand(numeric);
				operands.add(numericOperand);
				break;

			case WORD:
				// TODO: WORD인 경우에 대해서 pass1 처리하기.
				size = 3;
				symTab.put(token.getLabel().get(), locctr);
				locctr += 3;
				if(isNotFormula(token.getOperands().getFirst())){
//					System.out.println("KKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKK");
					numeric = new Numeric(token.getOperands().getFirst());
				}else{
//					System.out.println("PPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPP");
					numeric = new Numeric(token.getOperands().getFirst(), symTab, locctr);
				}
				numericOperand = new NumericOperand(numeric);
				operands.add(numericOperand);
				break;

			case RESB:
				// TODO: RESB인 경우에 대해서 pass1 처리하기.
				size = Integer.parseInt(token.getOperands().getFirst());
				symTab.put(token.getLabel().get(), locctr);
				locctr += size;
				numeric = new Numeric(String.valueOf(size));
				numericOperand = new NumericOperand(numeric);
				operands.add(numericOperand);
				break;

			case RESW:
				// TODO: RESW인 경우에 대해서 pass1 처리하기.
				size = 3;
				symTab.put(token.getLabel().get(), locctr);
				locctr += size;
				numeric = new Numeric(String.valueOf(size));
				numericOperand = new NumericOperand(numeric);
				operands.add(numericOperand);
				break;

			case LTORG:
				// TODO: LTORG인 경우에 대해서 pass1 처리하기.
				for(int i = 0; i < litTab.size(); i++){
					size += litTab.assignAddress(locctr, operands);
					locctr += size;
				}
				break;

			case EQU:
				// TODO: EQU인 경우에 대해서 pass1 처리하기.
				// 만약 *인지 BUFEND-BUFFER인지 케이스는 Numeric에서 알아서 해준다
				// 아니다. 여기서 해줘야한다
				symTab.put(token.getLabel().get(), token.getOperands().getFirst(), locctr);
				if(isNotFormula(token.getOperands().getFirst())){
					numeric = new Numeric(token.getOperands().getFirst());
				}else{
					// *일 경우?
					numeric = new Numeric(token.getOperands().getFirst(), symTab, locctr);
				}
				numericOperand = new NumericOperand(numeric);
				operands.add(numericOperand);
				break;

			case END:
				// TODO: END인 경우에 대해서 pass1 처리하기.
				for(int i = 0; i < litTab.size(); i++){
					locctr += litTab.assignAddress(locctr, operands);
				}
				break;

			default:
				throw new UnsupportedOperationException("not fully support Directive");
		}

		String para = "";

		if(!token.getOperands().isEmpty()){
			para = token.getOperands().getFirst();
		}

		return new DirectiveToken(para, locctr, size, directive, operands);
	}

	// 문자열에 +, -가 있는지 확인한다
	private static boolean isNotFormula(String s) throws RuntimeException {
		if(s.contains("'"))
			return true;
		else if(s.chars().allMatch(Character::isDigit))
			return true;

		return false;
	}

	/**
	 * pass2에서 operator가 instruction인 경우에 대해서 오브젝트 코드에 정보를 추가한다.
	 *
	 * @param objCode 오브젝트 코드 객체
	 * @param token   InstructionToken 객체
	 * @throws RuntimeException 잘못된 심볼 객체 변환 시도.
	 */
	private static void handlePass2InstructionStep(ObjectCode objCode, InstructionToken token,
												   SymbolTable symbolTable, LiteralTable literalTable) throws RuntimeException {
		Token.TextInfo textInfo = token.getTextInfo(symbolTable, literalTable);

		objCode.addText(textInfo.address - textInfo.size, textInfo.code, textInfo.size);

		if (textInfo.mod.isEmpty())
			return;

		Token.ModificationInfo modInfo = textInfo.mod.get();
		objCode.addModification("+" + modInfo.refers, modInfo.address, modInfo.sizeHalfByte);
	}

	/**
	 * pass2에서 operator가 directive인 경우에 대해서 오브젝트 코드에 정보를 추가한다.
	 *
	 * @param objCode      오브젝트 코드 객체
	 * @param token        DirectiveToken 객체
	 * @param repSymbol    control section 명칭 심볼
	 * @param literalTable 리터럴 테이블
	 * @throws RuntimeException 잘못된 지시어 사용 방식.
	 */
	private static void handlePass2DirectiveStep(ObjectCode objCode, DirectiveToken token, Symbol repSymbol,
												 SymbolTable symbolTable, LiteralTable literalTable) throws RuntimeException {
		Directive directive = token.getDirective();
		String sectionName = repSymbol.getName();

		ArrayList<Operand> operands;
		NumericOperand numOperand;
		Numeric num;
		int address;
		int size;

		switch (directive) {
			case START:
				numOperand = (NumericOperand) token.getOperands().get(0);
				objCode.setSectionName(sectionName);
				objCode.setStartAddress(numOperand.getNumeric().getInteger());
				isStartCsect = true;
				break;

			case CSECT:
				objCode.setSectionName(sectionName);
				objCode.setStartAddress(0);
				break;

			case EXTDEF:
				// TODO: EXTDEF인 경우에 대해서 pass2 처리하기.
				// objCode.addDefineSymbol(?, ?);
				for(int i = 0; i < token.getOperands().size(); i++){
					NumericOperand no = (NumericOperand) token.getOperands().get(i);
					String name = no.getNumeric().getMemoryName();
					int nameAddress = symbolTable.search(name).get().getAddress().get().getInteger();

					objCode.addDefineSymbol(name, nameAddress);
				}

				break;

			case EXTREF:
				operands = token.getOperands();
				for (Operand operand : operands) {
					numOperand = (NumericOperand) operand;
					num = numOperand.getNumeric();
					String symbolName = num.getMemoryName();
					objCode.addReferSymbol(symbolName);
				}
				break;

			case BYTE:
				// TODO: BYTE인 경우에 대해서 pass2 처리하기.
				// objCode.addText(?, ?, ?);
				NumericOperand n = (NumericOperand)token.getOperands().getFirst();
				byte k = (byte)n.getNumeric().getInteger();
				objCode.addText(token.getAddress(), k, 1);

				break;

			case WORD:
				// TODO: WORD인 경우에 대해서 pass2 처리하기.
				NumericOperand no = (NumericOperand) token.getOperands().getFirst();

				if(no.getNumeric().getMemoryName() == null){
					objCode.addText(token.getAddress(), no.getNumeric().getInteger(), token.getSize());
				}else{
					objCode.addText(token.getAddress(), 0, token.getSize());

					String []split = no.getNumeric().getMemoryName().split("[+-]");
					if(no.getNumeric().getMemoryName().contains("-")){
						objCode.addModification("+" + split[0], token.getAddress(), 6);
						objCode.addModification("-" + split[1], token.getAddress(), 6);
					}
				}




				break;

			case LTORG:
				// TODO: LTORG인 경우에 대해서 pass2 처리하기.
				for(int i = 0; i < token.getOperands().size(); i++){
					NumericOperand nn = (NumericOperand)token.getOperands().get(i);
					int bi = nn.getNumeric().getValue().intValue();

					objCode.addText(token.getAddress() - token.getSize(), Integer.toHexString(bi).toUpperCase(), token.getSize());
				}

				break;

			case END:
				// TODO: END인 경우에 대해서 pass2 처리하기.
				int extraLength = 0;
				for(int i = 0; i < token.getOperands().size(); i++){
					NumericOperand nn = (NumericOperand)token.getOperands().get(i);
					int bi = nn.getNumeric().getValue().intValue();

					String s = String.format("%02X", bi);
					extraLength += (s.length() + 1) / 2;
					objCode.addText(token.getAddress(), s, (s.length() + 1) / 2);
				}
				if(isStartCsect){
					objCode.setInitialPC(repSymbol.getAddress().get().getValue().intValue());
					isStartCsect = false;
				}else{
					objCode.setInitialPC(-1);
				}
				objCode.setProgramLength(token.getAddress() + extraLength);

				break;

			case RESW:
			case RESB:
				objCode.addText(0,0,0);	// 이 표시 있으면 한칸뛰어서 하면 되겠다!
				break;

			case EQU:
				// 처리할 동작이 없음.
				break;

			default:
				throw new UnsupportedOperationException("not fully support Directive");
		}
	}

	static boolean isStartCsect;
	private final List<Token> _tokens;
	private final SymbolTable _symbolTable;
	private final LiteralTable _literalTable;
}
