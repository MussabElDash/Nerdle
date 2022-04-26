import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.lang3.StringUtils;

public class Simulation {

	private int size;
	private String solution;
	private Map<Character, Long> counts;

	public Simulation(String solution) {
		if (solution == null || solution.isBlank()) {
			throw new IllegalArgumentException();
		}
		size = solution.length();
		this.solution = solution;
		counts = solution.chars().mapToObj(c -> (char) c)
				.collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
	}

	public String simulate1(String guess) {
		if (guess == null || guess.isBlank() || guess.length() != size) {
			throw new IllegalArgumentException();
		}
		char[] chars = new char[size];
		for (int i = 0; i < size; i++) {
			char gc = guess.charAt(i), sc = solution.charAt(i);
			if (gc == sc) {
				chars[i] = '2';
			} else if (counts.getOrDefault(gc, 0l) == 0) {
				chars[i] = '0';
			} else {
				chars[i] = '1';
			}
		}
		IntStream.range(0, size).boxed().collect(Collectors.groupingBy(i -> guess.charAt(i), Collectors.toSet()))
				.entrySet().stream().filter(entry -> counts.getOrDefault(entry.getKey(), 0l) != 0)
				.filter(entry -> entry.getValue().size() > counts.get(entry.getKey()))
				.flatMap(entry -> entry.getValue().stream().filter(i -> chars[i] != '2')
						.limit(entry.getValue().size() - counts.get(entry.getKey())))
				.forEach(i -> chars[i] = '0');
		return new String(chars);
	}

	public String simulate(String guess) {
		return compare(solution, guess);
	}

	public static String compare(String solution, String guess) {
		if (guess == null || solution == null || guess.isBlank() || solution.isBlank()
				|| guess.length() != solution.length()) {
			throw new IllegalArgumentException();
		}
		int size = solution.length();

		char[] solutionChar = solution.toCharArray();
		char[] guessChar = guess.toCharArray();
		char[] chars = StringUtils.repeat('0', size).toCharArray();

		IntStream.range(0, size).filter(i -> solutionChar[i] == guessChar[i]).forEach(i -> chars[i] = '2');
		// Iterate over the solution
		IntStream.range(0, size).filter(i -> solutionChar[i] != guessChar[i])
				// Iterate over the guess
				.forEach(i -> IntStream.range(0, size).filter(j -> i != j).filter(j -> solutionChar[i] == guessChar[j])
						.filter(j -> chars[j] == '0').forEach(j -> chars[j] = '1'));
		return new String(chars);
	}

	public static Map<String, Set<String>> providePossibleAnswers(String guess, Set<String> permutations) {
		return permutations.stream().collect(Collectors.groupingBy(perm -> compare(perm, guess), Collectors.toSet()));
	}

	public static Set<String> bestGuess(Set<String> allPerms, Set<String> possiblePerms) {
//		AtomicLong minMax = new AtomicLong(Long.MAX_VALUE);
//		AtomicReference<String> bestGuess = new AtomicReference<>("");
//
//		allPerms.stream().forEach(guess -> {
//			int max = providePossibleAnswers(guess, possiblePerms).values().stream().mapToInt(Set::size).max()
//					.orElse(Integer.MAX_VALUE);
//			if (max < minMax.get()) {
//				minMax.set(max);
//				bestGuess.set(guess);
//			}
//		});

		Map<Integer, Set<String>> maxs = allPerms.parallelStream()
				.collect(
						Collectors
								.groupingBy(
										perm -> providePossibleAnswers(perm, possiblePerms).values().stream()
												.mapToInt(Set::size).max().orElse(Integer.MAX_VALUE),
										Collectors.toSet()));

		int min = maxs.keySet().stream().min(Integer::compareTo).orElse(Integer.MAX_VALUE);

		return maxs.entrySet().stream().filter(entry -> entry.getKey() == min).map(Entry::getValue).findAny()
				.orElse(new HashSet<>());

//		return bestGuess.get();
	}

	public static void main(String[] args) {
		Scanner scan = new Scanner(System.in);
//		Simulation sim = new Simulation("34+37=71");
//		Simulation sim = new Simulation("75-20=55");
//		Simulation sim = new Simulation("35-6*5=5");
//		Simulation sim = new Simulation("10+1-8=3");
		Simulation sim = new Simulation("1+1=2");
		String res = "";
		while (!res.equals("22222222")) {
			System.out.println(res = sim.simulate(scan.next()));
		}
		scan.close();
	}

}

