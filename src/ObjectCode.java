import numeric.Numeric;

import java.util.ArrayList;
import java.util.Optional;

public class ObjectCode {
	public ObjectCode() {
		_sectionName = Optional.empty();
		_startAddress = Optional.empty();
		_programLength = Optional.empty();
		_initialPC = 0;

		_defines = new ArrayList<Define>();
		_refers = new ArrayList<String>();
		_texts = new ArrayList<Text>();
		_mods = new ArrayList<Modification>();
	}

	/**
	 * ObjectCode 객체를 String으로 변환한다. Assembler.java에서 오브젝트 코드를 출력하는 데에 사용된다.
	 */
	@Override
	public String toString() {
		if (_sectionName.isEmpty() || _startAddress.isEmpty() || _programLength.isEmpty())
			throw new RuntimeException("illegal operation");

		String sectionName = _sectionName.get();
		int startAddress = _startAddress.get();
		int programLength = _programLength.get();

		String header = String.format("H%-6s%06X%06X\n", sectionName, startAddress, programLength);
		String define = "";
		String refer = "";
		String text = "";
		String modification = "";

		// TODO: 오브젝트 코드 문자열 생성하기.
		for(int i = 0; i < _defines.size(); i++) {
			if(i == 0)
				define += "D";

			define += _defines.get(i).symbolName + String.format("%06X", _defines.get(i).address);

			if(i == _defines.size() - 1)
				define += "\n";
		}

		for(int i = 0; i < _refers.size(); i++) {
			if(i == 0)
				define += "R";

			define += String.format("%-6s", _refers.get(i));

			if(i == _refers.size() - 1)
				define += "\n";
		}

		int textLength = 0;
		String tmpText = "";

		for(int i = 0 ; i < _texts.size() ; i++) {
			if(textLength == 0 && !(_texts.get(i).size == 0 && _texts.get(i).address == 0)){
				text += "T";
				text += String.format("%06X", _texts.get(i).address);
			}

			textLength += _texts.get(i).size;

			if(textLength > 30 || (_texts.get(i).size == 0 && _texts.get(i).address == 0)){
				if(textLength != 0){
					textLength -= _texts.get(i).size;
					text += String.format("%02X", textLength);
					text += tmpText;
					text += "\n";
				}

				textLength = 0;
				tmpText = "";

				if(!(_texts.get(i).size == 0 && _texts.get(i).address == 0))
					i--;

				continue;
			}

			tmpText += _texts.get(i).value;


			if(i == _texts.size() - 1){
				text += String.format("%02X", textLength);
				text += tmpText;
				text += "\n";
			}
		}

		for(int i = 0 ; i < _mods.size() ; i++) {
			modification += "M";
			modification += String.format("%06X", _mods.get(i).address);
			modification += String.format("%02X", _mods.get(i).sizeHalfByte);
			modification += _mods.get(i).symbolNameWithSign;
			modification += "\n";
		}

		String end = "E";
		if(_initialPC != -1){
			end += String.format("%06X", _initialPC);
		}

		return header + define + refer + text + modification + end;
	}

	public void setSectionName(String sectionName) {
		_sectionName = Optional.of(sectionName);
	}

	public void setStartAddress(int address) {
		_startAddress = Optional.of(address);
	}

	public void setProgramLength(int length) {
		_programLength = Optional.of(length);
	}

	public void addDefineSymbol(String symbolName, int address) {
		_defines.add(new Define(symbolName, address));
	}

	public void addReferSymbol(String symbolName) {
		_refers.add(symbolName);
	}

	public void addText(int address, int context, int size) {
		String value = String.format("%06X", context);

		if(size == 1){
			value = value.substring(6);
		}
		else if(size == 2){
			value = value.substring(4);
		}
		if(size == 3 && value.length() > 6)
			value = value.substring(2);

		_texts.add(new Text(address, value, size));
	}
	public void addText(int address, String context, int size) {
		_texts.add(new Text(address, context, size));
	}

	public void addModification(String symbolNameWithSign, int address, int sizeHalfByte) {
		_mods.add(new Modification(address - 3, sizeHalfByte, symbolNameWithSign));
	}

	public void setInitialPC(int address) {
		_initialPC = address;
	}

	class Define {
		Define(String symbolName, int address) {
			this.symbolName = symbolName;
			this.address = address;
		}

		String symbolName;
		int address;
	}

	class Text {
		Text(int address, String value, int size) {
			this.address = address;
			this.value = value;
			this.size = size;
		}

		int address;
		String value;
		int size;
	}

	class Modification {
		Modification(int address, int sizeHalfByte, String symbolNameWithSign) {
			this.address = address;
			this.sizeHalfByte = sizeHalfByte;
			this.symbolNameWithSign = symbolNameWithSign;
		}

		int address;
		int sizeHalfByte;
		String symbolNameWithSign;
	}

	private Optional<String> _sectionName;
	private Optional<Integer> _startAddress;
	private Optional<Integer> _programLength;
	private int _initialPC;

	private ArrayList<Define> _defines;
	private ArrayList<String> _refers;
	private ArrayList<Text> _texts;
	private ArrayList<Modification> _mods;
}
