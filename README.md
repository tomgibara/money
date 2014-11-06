Money
=====

Money is a tiny library for conveniently handling monetary amounts in Java.

Licencing
---------

Money is released under the Apache 2.0 licence. This gives it an extremely wide
scope for use in closed source, commercially licenced software and other open
source projects. Please read the licence for full details.

Dependency
----------

The project is built using Maven and it is available as a regular dependency
from the Central Repository:

> Group ID:    `com.tomgibara.money`
> Artifact ID: `money`
> Version:     `1.2.0`

    <dependency>
      <groupId>com.tomgibara.money</groupId>
      <artifactId>money</artifactId>
      <version>1.2.0</version>
    </dependency>

Downloading
-----------

The money project is packaged as a single jar with no dependencies.
The project is hosted on Github from which the following latest files may be
downloaded:

 * Library:  [money-1.1.jar](https://github.com/tomgibara/money/releases/download/money-1.2.0/money-1.2.0.jar)
 * Javadocs: [money-1.1-javadoc.jar](https://github.com/tomgibara/money/releases/download/money-1.2.0/money-1.2.0-javadoc.jar)
 * Sources:  [money-1.1-sources.jar](https://github.com/tomgibara/money/releases/download/money-1.2.0/money-1.2.0-sources.jar)

Documentation
-------------

Complete javadocs are available.

Overview
--------

The package provides four simple abstractions:

 * `MoneyType`     – combines the notion of currency and locale to track and
                     display monetary values.
 * `Money`         – records a fixed monetary amount of a given type.
 * `MoneyCalc`     – a variable monetary value used to calculate monetary
                     amounts.
 * `MoneySplitter` – reliably distributes a calculated monetary amount into a
                     number of proportioned parts.

With the exception of `MoneySplitter`, all of the classes implement the
`MoneyCalcOrigin` interface to provide a standard way of accessing their
monetary value in a mutable form.

The API is extremely straightforward; indications of how it can be used can be
gleaned from the test cases. There are two noteworthy elements of the API. The
first is that instances of `MoneyType` have a notion of consistency that is not
directly exposed:

Calculations are always performed in the context of a `MoneyType`.
The `MoneyType` may however be entirely non-specific (ie. not specifying a
currency or locale). When a calculation is performed with a number of different
`Money` values, its type may change (by becoming more specific) to ensure that
the final value it generates is consistent with all the types of money in the
calculation. If this is not possible an `IllegalArgumentException` will be
thrown. For example, adding a euro denominated money amount to another that is
localized to Greece will result in a final amount that is euro denominated in
the Greek locale. On the other hand, attempting to US dollars to British pounds
will raise an exception.

The other noteworthy aspect of the API is how it controls the precision of
monetary calculations. Simply calling `money.calc()` will return an instance
that will perform calculations at arbitrary precision, this is most often what
one wants. But in some situations, where specific accounting rules are being
transcribed, or where the precision of division needs to be controlled, the
scale and rounding needs to be specified (see `BigDecimal` for an explanation
of scale and rounding modes) This is done by supplying additional parameters on
the creation of a MoneyCalc object like so: calc(scale, roundingMode). All
numbers involved in a calculation obtained in this way are adjusted (possibly
with a loss of precession) to match the specified scale prior to all arithmetic
or comparison operations.

Changes
-------

**November 2014** *Release 1.2.0* introduced a new method to derive calculations
                  specifying only a scale. A new class was added to provide more
                  control over the splitting of monetary amounts.

**June 2011**     *Release 1.1* added support for specifying the scale of
                  calculations (their precision) and for partitioning the values
                  of monetary calculations in such a way that they sum to the
                  original value.

**March 2010**    *Release 1.0* was the initial release.