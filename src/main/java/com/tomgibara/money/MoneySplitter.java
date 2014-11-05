package com.tomgibara.money;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Allows a calculated monetary amount to be split in by specified proportions.
 * 
 * @author tomgibara
 */

public class MoneySplitter {

	// statics

	private static final Money[] NO_MONIES = new Money[0];
	private static final Partition FREE = new Partition();

	public static BigDecimal[] toBigDecimalArray(Long... longs) {
		if (longs == null) throw new IllegalArgumentException("null longs");
		BigDecimal[] decimals = new BigDecimal[longs.length];
		for (int i = 0; i < longs.length; i++) {
			Long l = longs[i];
			decimals[i] = l == null ? null : BigDecimal.valueOf(l);
		}
		return decimals;
	}
	
	public static BigDecimal[] toBigDecimalArray(Double... doubles) {
		if (doubles == null) throw new IllegalArgumentException("null doubles");
		BigDecimal[] decimals = new BigDecimal[doubles.length];
		for (int i = 0; i < doubles.length; i++) {
			Double d = doubles[i];
			decimals[i] = d == null ? null : BigDecimal.valueOf(d);
		}
		return decimals;
	}
	
	// fields
	
	private final MoneyCalc calc;
	private List<Partition> partitions = null;
	private int parts = 0;

	// constructors
	
	MoneySplitter(MoneyCalc calc) {
		this.calc = calc;
	}
	
	// accessors

	/**
	 * The number of parts into which this splitter will distribute the currency
	 * amount
	 * 
	 * @return a number of parts.
	 */
	
	public int getParts() {
		return parts;
	}

	/**
	 * Specify the number of parts into which this splitter will divide the
	 * calculation amount.
	 * 
	 * @param parts
	 *            a non-negative number of parts
	 * @return the splitter
	 * @throws IllegalArgumentException
	 *             if the number of parts is negative
	 */
	
	public MoneySplitter setParts(int parts) {
		if (parts < 0) throw new IllegalArgumentException("negative parts");
		this.parts = parts;
		return this;
	}
	
	/**
	 * Sets the relative proportions into which the splitter will distribute the
	 * calculated monetary amount. If the number of proportions is greater than
	 * the currently specified number of parts, the number of parts will be
	 * implicitly increased.
	 * 
	 * @param proportions
	 *            the proportions into which the amount will be split.
	 * @return the splitter
	 * @throws IllegalArgumentException
	 *             if the proportions are null or negative
	 */
	
	public MoneySplitter setProportions(BigDecimal... proportions) {
		if (proportions == null) throw new IllegalArgumentException("null proportions");
		for (int i = 0; i < proportions.length; i++) {
			BigDecimal proportion = proportions[i];
			if (proportion == null) throw new IllegalArgumentException("null proportion");
			if (proportion.signum() < 0) throw new IllegalArgumentException("negative proportion");
			getPartition(i).proportion = proportion;
		}
		return growParts(proportions.length);
	}

	/**
	 * Sets the proportion of an individual part of the split. Specifying a null
	 * proportion leaves the proportion unspecified.
	 * 
	 * @param index
	 *            the index of the proportion to change
	 * @param proportions
	 *            the specified proportion into which the amount will be split.
	 * @return the splitter
	 * @throws IllegalArgumentException
	 *             if index or the proportion is negative
	 */
	
	public MoneySplitter setProportion(int index, BigDecimal proportion) {
		if (index < 0) throw new IllegalArgumentException("negative index");
		if (proportion != null && proportion.signum() < 0) throw new IllegalArgumentException("negative proportion");
		getPartition(index).proportion = proportion;
		return growParts(index + 1);
	}
	
	// methods

	/**
	 * Splits the amount of money stored by the calculation from which this
	 * splitter was obtained. If the number of parts is zero no change will be
	 * made to the calculation amount and an empty array will be returned,
	 * otherwise the amount recorded in that calculation will be zeroed and the
	 * amounts distributed as per the specified proportions.
	 * 
	 * @return an array containing the monetary amounts arising from the split.
	 */
	
	public Money[] split() {
		if (parts == 0) return NO_MONIES;
		return isProportioned() ? splitProportioned() : splitFree();
	}
	
	// private utility methods
	
	private Money[] splitFree() {
		Money[] monies = new Money[parts];
		BigDecimal remainder = calc.getAmount();
		for (int i = parts - 1; i >= 0; i--) {
			if (i == 0) {
				monies[i] = calc.money(remainder);
			} else {
				BigDecimal share = remainder.divide(BigDecimal.valueOf(i + 1), calc.scale, calc.roundingMode);
				monies[i] = calc.money(share);
				remainder = remainder.subtract(share);
			}
		}
		calc.zero();
		return monies;
	}
	
	private Money[] splitProportioned() {
		// trivial case
		if (parts == 1) {
			if (getEffectiveProportion(0).signum() == 0) throw new IllegalStateException("all proportions zero");
			return new Money[] { calc.money() };
		}
		
		BigDecimal[] denominators = new BigDecimal[parts];
		{
			BigDecimal denominator = null;
			for (int i = 0; i < parts; i++) {
				BigDecimal proportion = getEffectiveProportion(i);
				denominator = i == 0 ? proportion : denominator.add(proportion);
				denominators[i] = denominator;
			}
			if (denominator.signum() == 0) throw new IllegalArgumentException("all proportions zero");
		}
		Money[] monies = new Money[parts];
		BigDecimal remainder = calc.getAmount();
		Money none = null; // lazily instantiated
		for (int i = parts - 1; i >= 0; i--) {
			if (i == 0) {
				monies[i] = calc.money(remainder);
			} else {
				BigDecimal proportion = getEffectiveProportion(i);
				if (proportion.signum() == 0) {
					if (none == null) none = calc.money(BigDecimal.ZERO);
					monies[i] = none;
				} else {
					BigDecimal share = remainder.multiply(proportion).divide(denominators[i], calc.scale, calc.roundingMode);
					monies[i] = calc.money(share);
					remainder = remainder.subtract(share);
				}
			}
		}
		calc.zero();
		return monies;
	}
	
	private Partition getPartition(int index) {
		if (index < 0) throw new IllegalArgumentException("negative index");
		if (partitions == null) partitions = new ArrayList<Partition>();
		Partition partition = null;
		while (partitions.size() <= index) {
			partition = new Partition();
			partitions.add(partition);
		}
		return partition == null ? partitions.get(index) : partition;
	}

	private MoneySplitter growParts(int size) {
		return size <= parts ? this : setParts(size);
	}
	
	private boolean isProportioned() {
		if (partitions != null) {
			int limit = Math.min(partitions.size(), parts);
			for (int i = 0; i < limit; i++) {
				if (getEffectivePartition(i).isProportioned()) return true;
			}
		}
		return false;
	}
	
	private BigDecimal getEffectiveProportion(int index) {
		Partition partition = getEffectivePartition(index);
		BigDecimal proportion = partition.proportion;
		return proportion == null ? BigDecimal.ZERO : proportion;
	}
	
	private Partition getEffectivePartition(int index) {
		if (partitions == null) return FREE;
		Partition partition = partitions.get(index);
		return partition == null ? FREE : partition;
	}
	
	// inner classes
	
	private static class Partition {
		
		BigDecimal proportion;
		
		boolean isProportioned() {
			return proportion != null;
		}
		
	}
	
}
