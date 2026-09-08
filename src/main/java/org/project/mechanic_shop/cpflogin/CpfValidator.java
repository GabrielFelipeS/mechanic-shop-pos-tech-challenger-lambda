package org.project.mechanic_shop.cpflogin;

public final class CpfValidator {

	private CpfValidator() {
	}

	public static boolean isValid(String rawDocument) {
		if (rawDocument == null) {
			return false;
		}

		String cpf = rawDocument.replaceAll("\\D", "");
		if (cpf.length() != 11 || cpf.chars().distinct().count() == 1) {
			return false;
		}

		int firstCheckDigit = calculateCheckDigit(cpf.substring(0, 9), 10);
		int secondCheckDigit = calculateCheckDigit(cpf.substring(0, 9) + firstCheckDigit, 11);

		return cpf.equals(cpf.substring(0, 9) + firstCheckDigit + secondCheckDigit);
	}

	private static int calculateCheckDigit(String base, int startWeight) {
		int sum = 0;
		int weight = startWeight;
		for (char c : base.toCharArray()) {
			sum += (c - '0') * weight;
			weight--;
		}

		int remainder = sum % 11;
		return remainder < 2 ? 0 : 11 - remainder;
	}
}
