package token;

import java.util.ArrayList;
import java.util.Optional;

import numeric.Numeric;

public abstract class Token {
	/**
	 * object code 작성을 위한 text 영역 정보를 담은 클래스
	 */
	public class TextInfo {
		public TextInfo(int address, int code, int size, Optional<ModificationInfo> mod) {
			this.address = address;
			this.code = code;
			this.size = size;
			this.mod = mod;
		}

		public final int address;
		public final int code;
		public final int size;
		public final Optional<ModificationInfo> mod;		// 바로 아래에 클래스 정의되어 있음
	}

	/**
	 * object code 작성을 위한 modification 영역 정보를 담은 클래스
	 */
	public class ModificationInfo {
		public ModificationInfo(String refers, int address, int sizeHalfByte) {
			this.refers = refers;
			this.address = address;
			this.sizeHalfByte = sizeHalfByte;
		}

		public final String refers;
		public final int address;
		public final int sizeHalfByte;
	}

	/**
	 * Token 객체의 정보를 문자열로 반환한다. 디버그 용도로 사용한다.
	 */
	Token(String tokenString, int address, int size) {
		_tokenString = tokenString;
		_address = address;
		_size = size;
	}

	@Override
	public String toString() {
		return _tokenString;
	}

	public final int getAddress() {
		return _address;
	}

	public final int getSize() {
		return _size;
	}

	public final String getTokenString() {
		return _tokenString;
	}
	private final String _tokenString;
	private final int _address;
	private final int _size;
}
