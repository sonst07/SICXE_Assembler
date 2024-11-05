import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.stream.Collectors;

import instruction.InstructionTable;

public class Assembler {
	public static void main(String[] args) {
		try {
			// 명령어 테이블 초기화
			Assembler assembler = new Assembler("inst_table.txt");
			// 어셈블리어 코드 읽기
			ArrayList<String> input = assembler.readInputFromFile("input.txt");
			// section별로 나누어진 이중 array을 반환한다
			ArrayList<ArrayList<String>> dividedInput = assembler.divideInput(input);

			// dividedInput 각각 pass1을 진행한다
			ArrayList<ControlSection> controlSections = (ArrayList<ControlSection>) dividedInput.stream()
					.map(x -> assembler.pass1(x))
					.collect(Collectors.toList());

			// 심볼테이블, 리터럴테이블 출력을 위한 변수저장
			String symbolsString = controlSections.stream()
					.map(x -> x.getSymbolString())
					.collect(Collectors.joining("\n\n"));
			String literalsString = controlSections.stream()
					.map(x -> x.getLiteralString())
					.collect(Collectors.joining("\n\n"));

			assembler.writeStringToFile("output_symtab.txt", symbolsString);
			assembler.writeStringToFile("output_littab.txt", literalsString);

			// controlSection별로 pass2를 진행한다
			ArrayList<ObjectCode> objectCodes = (ArrayList<ObjectCode>) controlSections.stream()
					.map(x -> assembler.pass2(x))
					.collect(Collectors.toList());

			String objectCodesString = objectCodes.stream()
					.map(x -> x.toString())
					.collect(Collectors.joining("\n\n"));

			assembler.writeStringToFile("output_objectcode.txt", objectCodesString);
		} catch (Exception e) {
			System.out.println("Error : " + e.getMessage());
		}

	}

	public Assembler(String instFile) throws FileNotFoundException, IOException {
		_instTable = new InstructionTable(instFile);
	}

	private ArrayList<ArrayList<String>> divideInput(ArrayList<String> input) {
		ArrayList<ArrayList<String>> divided = new ArrayList<ArrayList<String>>();
		String lastStr = input.get(input.size() - 1);

		ArrayList<String> tmpInput = new ArrayList<String>();
		for (String str : input) {
			// CSECT 를 포함할시 만약 tmpInput 이 비어있으면 그대로 넣어주고, 아니면 새로운 arrayList 에 저장한다
			if (str.contains("CSECT")) {
				if (!tmpInput.isEmpty()) {
					tmpInput.add(lastStr);
					divided.add(tmpInput);
					tmpInput = new ArrayList<String>();
					tmpInput.add(str);
				}
			} else {
				tmpInput.add(str);
			}
		}
		// 마지막 section 을 더해준다
		if (!tmpInput.isEmpty()) {
			divided.add(tmpInput);
		}
		// 결과적으로 section 별로 나누어진 문자열 array 들을 반환한다
		return divided;
	}

	private ArrayList<String> readInputFromFile(String inputFileName) throws FileNotFoundException, IOException {
		ArrayList<String> input = new ArrayList<String>();

		File file = new File(inputFileName);
		BufferedReader bufReader = new BufferedReader(new FileReader(file));

		String line = "";
		while ((line = bufReader.readLine()) != null){
			input.add(line);
		}

		bufReader.close();

		return input;
	}

	private void writeStringToFile(String fileName, String content) throws IOException {
		File file = new File(fileName);

		BufferedWriter writer = new BufferedWriter(new FileWriter(file));
		writer.write(content);
		writer.close();
	}

	private ControlSection pass1(ArrayList<String> input) throws RuntimeException {
		return new ControlSection(_instTable, input);
	}

	private ObjectCode pass2(ControlSection controlSection) throws RuntimeException {
		return controlSection.buildObjectCode();
	}


	private static InstructionTable _instTable;
}
