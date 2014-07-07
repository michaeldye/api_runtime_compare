package com.lulzotron.perf.seq;

import java.math.BigInteger;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * <p>
 * A really simplistic lazy sequence generator.
 * </p>
 * 
 * <p>
 * TODO: If performance becomes a problem, first consider extending a sequence
 * with multiple worker threads. Failing that, use the macho <a
 * href=https://code.google.com/p/totallylazy/">TotallyLazy lib</a> instead.
 * </p>
 * 
 * @author mdye
 *
 */
public class SequenceGenerator {

  private final Map<Sequences, List<BigInteger>> sequences;

  public SequenceGenerator() {
    Map<Sequences, List<BigInteger>> modSequences = new HashMap<>();
    for (Sequences seqs : EnumSet.allOf(Sequences.class)) {
      modSequences.put(seqs, new LinkedList<>()); // N.B. Calls to modify this
                                                  // LinkedList *must* be
                                                  // synchronized externally
    }

    sequences = Collections.unmodifiableMap(modSequences);
  }

  public String count(final int upTo, SequenceFormatter formatter) {
    // no benefit to memoizing this one
    return formatter.format(upTo, (Integer ix) -> ix + 1);
  }

  /**
   * <p>
   * Calculates fibonacci sequence up to given <code>upTo</code> num. Throws
   * {@link IllegalArgumentException} when upTo is too large.
   * </p>
   * 
   * 
   * @param upTo
   * @param writer
   * @return formatted result String
   * @throws IllegalArgumentException
   */
  public String fib(final int upTo, SequenceFormatter formatter)
      throws IllegalArgumentException {
    return formatter.format(upTo,
        memoCalc((Integer ix) -> calcFib(BigInteger.valueOf(ix), BigInteger.ONE, BigInteger.ZERO), Sequences.FIB));
  }

  /*
   * Writes sequence to string in "plain" format
   */
  public static SequenceFormatter plain = (final Integer value,
      final Function<Integer, ?> wrapper) -> {

    final StringBuffer sb = new StringBuffer();

    for (int ix = 1; ix <= value; ix++) {
      sb.append(wrapper.apply(ix - 1));

      if (ix < value) {
        sb.append(' ');
      }
    }

    return sb.toString();
  };

  /*
   * A tail-rec fib method calculation method
   */
  private BigInteger calcFib(BigInteger n, BigInteger next, BigInteger result) {
    if (n.equals(BigInteger.ZERO))
      return result;
    else
      return calcFib(n.subtract(BigInteger.ONE), next.add(result), next);
  }

  /*
   * Returns wrapped calc function suitable for use in output function. Memoizes
   * each calculated value.
   */
  private Function<Integer, BigInteger> memoCalc(
      final Function<Integer, BigInteger> calc, final Sequences sequence) {

    return (Integer index) -> {
      List<BigInteger> seq = sequences.get(sequence);

      BigInteger val;
      if (index < seq.size()) {
        return seq.get(index); // fine to sacrifice thread visibility here
      } else {
        val = calc.apply(index);
        synchronized (this) {
          seq.add(val);
        }
        return val;
      }
    };
  }

  @FunctionalInterface
  public interface SequenceFormatter {
    String format(Integer value, Function<Integer, ?> wrapper);
  }

  enum Sequences {
    FIB;
  }
}