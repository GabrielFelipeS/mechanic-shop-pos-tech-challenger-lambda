package org.project.mechanic_shop.cpflogin;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.Test;

class CpfValidatorTest {

	@ParameterizedTest
	@ValueSource(strings = {"26644152040", "266.441.520-40", "52998224725"})
	void isValid_acceptsCpfWithCorrectChecksum(String document) {
		assertTrue(CpfValidator.isValid(document));
	}

	@ParameterizedTest
	@ValueSource(strings = {"11111111111", "26644152041", "123", "", "abc.def.ghi-jk"})
	void isValid_rejectsInvalidCpf(String document) {
		assertFalse(CpfValidator.isValid(document));
	}

	@Test
	void isValid_rejectsNull() {
		assertFalse(CpfValidator.isValid(null));
	}
}
