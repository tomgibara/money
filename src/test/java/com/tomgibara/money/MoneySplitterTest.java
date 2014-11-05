package com.tomgibara.money;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import junit.framework.TestCase;

public class MoneySplitterTest extends TestCase {

	private static List<Money> list(Money[] money) {
		return Arrays.asList(money);
	}
	
	public void testSplit() {
		Random r = new Random(0L);
		MoneyType type = new MoneyType(Locale.US);
		for (int i = 0; i < 100; i++) {
			Money money = type.money(r.nextInt(1000));
			for (int j = 0; j < 10; j++) { // parts
				int parts = 1 + r.nextInt(10);
				for (int k = 0; k < 3; k++) { // precision
					testSplit(money.calc(k, null), parts);
				}
			}
		}
	}
	
	private void testSplit(MoneyCalc calc, int parts) {
		MoneyCalc copy = calc.calc();// make arbitrary arbitrary precision copy of calc amount
		Money[] split = calc.splitter().setParts(parts).split(); // split it
		assertEquals(calc.clone().zero(), calc); // check no money remains in calc
		for (Money m : split) copy.subtract(m); // subtract away all parts
		assertTrue(copy.getAmount().signum() == 0); // check we have no remainder
	}

	public void testSplitProportions() {
		MoneyType type = new MoneyType(Locale.US);
		Money money = type.money(100);
		MoneyCalc calc = money.calc(2, null);
		Money[] ms;
		ms = calc.clone().splitter().setProportions(BigDecimal.valueOf(5)).split();
		assertEquals(money, ms[0]);
		ms = calc.clone().splitter().setProportions(BigDecimal.valueOf(1), BigDecimal.valueOf(1)).split();
		assertEquals(type.money(50), ms[0]);
		assertEquals(type.money(50), ms[1]);
		ms = calc.clone().splitter().setProportions(BigDecimal.valueOf(1), BigDecimal.valueOf(4)).split();
		assertEquals(type.money(20), ms[0]);
		assertEquals(type.money(80), ms[1]);
		ms = calc.clone().splitter().setProportions(BigDecimal.valueOf(4), BigDecimal.valueOf(1)).split();
		assertEquals(type.money(80), ms[0]);
		assertEquals(type.money(20), ms[1]);
		ms = calc.clone().splitter().setProportions(BigDecimal.valueOf(1), BigDecimal.valueOf(9), BigDecimal.valueOf(90)).split();
		assertEquals(type.money(1), ms[0]);
		assertEquals(type.money(9), ms[1]);
		assertEquals(type.money(90), ms[2]);
		ms = calc.clone().splitter().setProportions(BigDecimal.valueOf(1), BigDecimal.valueOf(1000)).split();
		assertEquals(type.money(0), ms[0]);
		assertEquals(type.money(100), ms[1]);
	}
	
	public void testSplitZeros() {
		MoneyType type = new MoneyType(Locale.US);
		Money zero = type.calc(2, null).money();
		Money one = type.money(1).calc(2, null).money();
		MoneyCalc calc = one.calc(2, null);
		assertEquals(list(new Money[] {zero, zero, one}), list(calc.clone().splitter().setProportions(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ONE).split()));
		assertEquals(list(new Money[] {zero, one, zero}), list(calc.clone().splitter().setProportions(BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.ZERO).split()));
		assertEquals(list(new Money[] {one, zero, zero}), list(calc.clone().splitter().setProportions(BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO).split()));
	}
	
	public void testNonSplit() {
		MoneyCalc calc = new MoneyType(Locale.US).money(100).calc(2, null);
		assertEquals(0, calc.splitter().split().length);
		assertEquals(BigDecimal.valueOf(100, 2), calc.money().getAmount());
	}
	
}
