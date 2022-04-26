import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;
import java.util.Set;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Nerdle {
	private static Set<String> allPermutation;
	private static Scanner scan = new Scanner(System.in);
	private static int[] digits1 = { '1', '2', '3', '4', '5', '6', '7', '8', '9' }, operators = { '+', '-', '*', '/' },
			all_chars = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '-', '*', '/', '=' };
	public static Map<Integer, String> firstGuesses = Map.of(6, "3*7=56");
	private int size;
	private Map<Character, Set<Integer>> possibleLocation = new HashMap<>();
	private Map<Character, Set<Integer>> rightLocation = new HashMap<>();
	private Set<Character> must = new HashSet<Character>();
	private Set<String> all_perms;
	private Map<Character, Long> allExactCounts = new HashMap<>();
	IntFunction<Character> toChar = i -> (char) i;
	private Map<Character, Integer> atLeastCounts = new HashMap<>(
			Arrays.stream(all_chars).mapToObj(toChar).collect(Collectors.toMap(c -> c, c -> 0)));
	private Set<Character> tried = new HashSet<>();
	private Collection<String> uniquePerms = new HashSet<>();
	private CharacterStatistics charStats;

	public Nerdle(int size) throws IOException, URISyntaxException {
		this.size = size;

		for (int d : digits1) {
			for (int i = 0; i < size; i++) {
				possibleLocation.computeIfAbsent((char) d, k -> new HashSet<Integer>()).add(i);
			}
		}
		for (int i = size == 3 ? 0 : 1; i < size; i++) {
			possibleLocation.computeIfAbsent('0', k -> new HashSet<Integer>()).add(i);
		}
		for (int c : operators) {
			for (int i = 1; i < size - 3; i++) {
				possibleLocation.computeIfAbsent((char) c, k -> new HashSet<Integer>()).add(i);
			}
		}
		if (allPermutation == null) {
			allPermutation = Permutations.loadPerm(size);
		}
		all_perms = new HashSet<>(allPermutation);
		charStats = new CharacterStatistics(all_perms);
	}

	/**
	 * 0 Dead, 1 Wrong Place, 2 Right Place
	 * 
	 * @param guess
	 * @param result
	 */
	public void guessed(String guess, String result) {
		all_perms = Simulation.providePossibleAnswers(guess, all_perms).get(result);
	}

	/**
	 * 0 Dead, 1 Wrong Place, 2 Right Place
	 * 
	 * @param guess
	 * @param result
	 */
	public void guessed1(String guess, String result) {
		if (guess.length() != size || result.length() != size) {
			return;
		}
		// Mark these characters as tried
		tried.addAll(guess.chars().mapToObj(toChar).filter(c -> Character.isDigit(c)).collect(Collectors.toSet()));

		// Count the occurrence of each character in the guess
		Map<Character, Long> nonZeroCount = IntStream.range(0, size).boxed().filter(i -> result.charAt(i) != '0')
				.collect(Collectors.groupingBy(i -> guess.charAt(i), Collectors.counting()));

		// Getting the exact count of a character in case the guess exceeds the
		// solution's count
		Map<Character, Long> exactCounts = IntStream.range(0, size).boxed().filter(i -> result.charAt(i) == '0')
				.filter(i -> nonZeroCount.containsKey(guess.charAt(i))).map(i -> guess.charAt(i)).distinct()
				.collect(Collectors.toMap(c -> c, c -> nonZeroCount.get(c)));
		allExactCounts.putAll(exactCounts);

		// Update the at least occurrence for each character
		nonZeroCount.entrySet().stream().forEach(
				entry -> atLeastCounts.compute(entry.getKey(), (k, v) -> Math.max(v, entry.getValue().intValue())));

		for (int i = 0; i < size; i++) {
			char gc = guess.charAt(i);
			switch (result.charAt(i)) {
			case '0':
				if (allExactCounts.containsKey(gc)) {
					possibleLocation.getOrDefault(gc, new HashSet<Integer>()).remove(i);
				} else {
					allExactCounts.put(gc, 0l);
					possibleLocation.remove(gc);
				}
				break;
			case '1':
				possibleLocation.getOrDefault(gc, new HashSet<Integer>()).remove(i);
				must.add(gc);
				break;
			case '2':
				rightLocation.computeIfAbsent(gc, k -> new HashSet<Integer>()).add(i);
				must.add(gc);
				break;
			default:
				break;
			}
			if (allExactCounts.containsKey(gc)
					&& rightLocation.getOrDefault(gc, new HashSet<>()).size() == allExactCounts.get(gc)) {
				possibleLocation.remove(gc);
				if (allExactCounts.get(gc) > 0 && result.charAt(i) == '2') {
					int tempI = i;
					possibleLocation.values().stream().forEach(set -> set.remove(tempI));
				}
			}
		}
		if (rightLocation.containsKey('=')) {
			possibleLocation.remove('=');
		}
	}

	private Optional<String> unique(Set<String> perms) {
//		Function<String, Long> countUnique = s -> s.chars().distinct().count();
//		Comparator<String> maxUnique = Comparator.comparing(countUnique);

		Collection<String> mostProbable = Permutations.eleminateDuplicates(perms);
		mostProbable = charStats.getMaxSums(mostProbable);

		uniquePerms = mostProbable;
//		uniquePerms = Permutations.eleminateDuplicates(perms);
//		return uniquePerms.stream().collect(Collectors.maxBy(maxUnique));
//		return mostProbable.stream().collect(Collectors.maxBy(maxUnique));
		return mostProbable.stream().findFirst();
	}

	public String guess() {
		if (all_perms.size() == 1) {
			return all_perms.stream().findAny().get();
		}
		return unique(Simulation.bestGuess(allPermutation, all_perms)).orElse("none");
	}

	public String guess1() {
		if (tried.isEmpty()) {
			Optional<String> unique = Optional.empty();
			return unique.orElse(unique(all_perms).orElse("none"));
		}
//		Set<String> removed = new HashSet<>();

		eliminateNotPossilble();

//		Optional<String> unique = addSomeRemoved(removed);
		Optional<String> unique = Optional.empty();
		return unique.orElse(unique(all_perms).orElse("none"));
	}

	private void eliminateNotPossilble() {
		Iterator<String> iter = all_perms.iterator();
		allPerms: while (iter.hasNext()) {
			String perm = iter.next();

			// Checking if all the values that must be in the equation are in the equation
			if (must.stream().anyMatch(c -> !perm.contains(c.toString()))) {
				iter.remove();
//				removed.add(perm);
				continue;
			}
			// Checking if the values that their location are known are in their correct
			// locations
			for (char k : rightLocation.keySet()) {
				if (rightLocation.get(k).stream().anyMatch(i -> perm.charAt(i) != k)) {
					iter.remove();
//					removed.add(perm);
					continue allPerms;
				}
			}

			// Checking if the values should be occurring more than once
			Map<Character, Long> counters = perm.chars().mapToObj(toChar)
					.collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
			Predicate<Character> checkingCounts = k -> allExactCounts.containsKey(k)
					&& counters.get(k) != allExactCounts.get(k) || counters.get(k) < atLeastCounts.get(k);
			if (counters.keySet().stream().anyMatch(checkingCounts)) {
				iter.remove();
//				removed.add(perm);
				continue;
			}
			// Checking if the values are in their possible location
			for (int i = 0; i < perm.length(); i++) {
				char c = perm.charAt(i);
				if (c == '=') {
					continue;
				}
				if (rightLocation.getOrDefault(c, new HashSet<>()).contains(i)
						|| possibleLocation.getOrDefault(c, new HashSet<>()).contains(i)) {
					continue;
				}
				iter.remove();
				break;
			}
		}
	}

	public void play() {
		System.out.println("recommended Guess is " + guess());
		do {
			System.out.println("Enter your guess followed by the result");
			String guess = scan.next();
			String res = scan.next();
			guessed(guess, res);
			System.out.println("recommended Guess is " + guess());
			showGuesses();
		} while (all_perms.size() > 1);
		scan.close();
	}

	public void showGuesses() {
		System.out.println("all possible answers: " + uniquePerms);
	}

	public static void setAllPermutation(Set<String> allPermutation) {
		Nerdle.allPermutation = allPermutation;
	}

	public static void main(String[] args) throws IOException, URISyntaxException {
		System.out.println("Enter the size of the puzzle");
		Nerdle nerdle = new Nerdle(scan.nextInt());
		nerdle.play();
		scan.close();
	}
}