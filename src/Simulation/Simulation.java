package Simulation;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Simulation {

	private String solution;

	public Simulation(String solution) {
		if (solution == null || solution.isBlank()) {
			throw new IllegalArgumentException();
		}
		this.solution = solution;
	}

	public String exactSimulation(String guess) {
		return exactCompare(solution, guess);
	}

	public String simulate(String guess) {
		return compare(solution, guess);
	}

	public static String exactCompare(String solution, String guess) {
		if (guess == null || solution == null || guess.isBlank() || solution.isBlank()
				|| guess.length() != solution.length()) {
			throw new IllegalArgumentException();
		}
		int size = solution.length();
		char[] solutionChar = solution.toCharArray();
		char[] guessChar = guess.toCharArray();
		char[] chars = "0".repeat(size).toCharArray();

		Map<Character, Long> counts = IntStream.range(0, size).mapToObj(i -> solutionChar[i])
				.collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

		IntStream.range(0, size).filter(i -> solutionChar[i] == guessChar[i]).forEach(i -> {
			counts.compute(guessChar[i], (k, v) -> v - 1);
			chars[i] = '2';
		});
		IntStream.range(0, size).filter(i -> chars[i] != '2').filter(i -> counts.containsKey(guessChar[i]))
				.forEach(i -> {
					if (counts.get(guessChar[i]) != 0) {
						counts.compute(guessChar[i], (k, v) -> v - 1);
						chars[i] = '1';
					}
				});
		return new String(chars);
	}

	public static String compare(String solution, String guess) {
		if (guess == null || solution == null || guess.isBlank() || solution.isBlank()
				|| guess.length() != solution.length()) {
			throw new IllegalArgumentException();
		}
		int size = solution.length();

		char[] solutionChar = solution.toCharArray();
		char[] guessChar = guess.toCharArray();
		char[] chars = "0".repeat(size).toCharArray();

		IntStream.range(0, size).filter(i -> solutionChar[i] == guessChar[i]).forEach(i -> chars[i] = '2');
		// Iterate over the solution
		IntStream.range(0, size).filter(i -> solutionChar[i] != guessChar[i])
				// Iterate over the guess
				.forEach(i -> IntStream.range(0, size).filter(j -> i != j).filter(j -> solutionChar[i] == guessChar[j])
						.filter(j -> chars[j] == '0').forEach(j -> chars[j] = '1'));
		return new String(chars);
	}

	public static Map<String, Set<String>> providePossibleAnswers(String guess, Set<String> permutations) {
//		return permutations.stream().collect(Collectors.groupingBy(perm -> {
//			return compare(perm, guess);
//		}, Collectors.toSet()));
		return permutations.stream().collect(Collectors.groupingBy(perm -> {
			return exactCompare(perm, guess);
		}, Collectors.toSet()));
	}

	public static Set<String> bestGuess(Set<String> allPerms, Set<String> possiblePerms) {
		AtomicLong minMax = new AtomicLong(Long.MAX_VALUE);
		AtomicReference<Set<String>> bestGuess = new AtomicReference<>();

		allPerms.parallelStream().forEach(guess -> {
			int max = providePossibleAnswers(guess, possiblePerms).values().stream().mapToInt(Set::size).max()
					.orElse(Integer.MAX_VALUE);
			synchronized (minMax) {
				if (max < minMax.get()) {
					minMax.set(max);
					bestGuess.set(new HashSet<>(List.of(guess)));
				} else if (max == minMax.get()) {
					bestGuess.get().add(guess);
				}
			}
		});

		return bestGuess.get();

//		Map<Integer, Set<String>> maxs = allPerms.parallelStream()
//				.collect(
//						Collectors
//								.groupingBy(
//										perm -> providePossibleAnswers(perm, possiblePerms).values().stream()
//												.mapToInt(Set::size).max().orElse(Integer.MAX_VALUE),
//										Collectors.toSet()));
//
//		int min = maxs.keySet().stream().min(Integer::compareTo).orElse(Integer.MAX_VALUE);
//
//		return maxs.entrySet().stream().filter(entry -> entry.getKey() == min).map(Entry::getValue).findAny()
//				.orElse(new HashSet<>());
	}

	public static void main(String[] args) {
		Scanner scan = new Scanner(System.in);
//		Simulation sim = new Simulation("34+37=71");
//		Simulation sim = new Simulation("75-20=55");
//		Simulation sim = new Simulation("35-6*5=5");
//		Simulation sim = new Simulation("10+1-8=3");
		Simulation sim = new Simulation("6+5+6=17");
		String res = "";
		while (!res.equals("2".repeat(sim.solution.length()))) {
			System.out.println(res = sim.exactSimulation(scan.next()));
		}
		scan.close();
	}

}
