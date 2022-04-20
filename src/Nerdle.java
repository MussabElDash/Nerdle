import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Nerdle {
	private static Scanner scan = new Scanner(System.in);
	private static int[] digits1 = { '1', '2', '3', '4', '5', '6', '7', '8', '9' },
			digits0 = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' }, operators = { '+', '-', '*', '/' },
			all_chars = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '-', '*', '/', '=' };
	private int size;
	private Map<Character, Set<Integer>> possibleLocation = new HashMap<>();
	private Map<Character, Set<Integer>> rightLocation = new HashMap<>();
	private Set<Character> must = new HashSet<Character>();
	private Set<String> all_perms = new HashSet<String>();
	private Map<Character, Long> allExactCounts = new HashMap<>();
	private Map<Character, Integer> atLeastCounts = new HashMap<>(
			Arrays.stream(all_chars).mapToObj(c -> (char) c).collect(Collectors.toMap(c -> c, c -> 0)));
	private Set<Character> tried = new HashSet<>();
	int maxNumO, maxNumD;

	public Nerdle(int size) throws IOException {
		this.size = size;
		// The equals sign, an operator and the rest digits
		maxNumD = size - 2;
		// The equals sign and a number at the end and from start to end digit followed
		// by operator and in case of odd number it's followed by another digit
		maxNumO = (int) Math.floor((size - 1) / 2.0) - 1;
		for (int d : digits1) {
			for (int i = 0; i < size; i++) {
				possibleLocation.computeIfAbsent((char) d, k -> new HashSet<Integer>()).add(i);
			}
		}
		for (int i = 1; i < size; i++) {
			possibleLocation.computeIfAbsent('0', k -> new HashSet<Integer>()).add(i);
		}
		for (int c : operators) {
			for (int i = 1; i < size - 3; i++) {
				possibleLocation.computeIfAbsent((char) c, k -> new HashSet<Integer>()).add(i);
			}
		}
		InputStream file = Nerdle.class.getResourceAsStream("Permutations" + size + ".txt");
		BufferedReader bf = new BufferedReader(new InputStreamReader(file));
		String line;
		while ((line = bf.readLine()) != null) {
			all_perms.add(line);
		}
		bf.close();
	}

	/**
	 * 0 Dead, 1 Wrong Place, 2 Right Place
	 * 
	 * @param guess
	 * @param result
	 */
	public void guessed(String guess, String result) {
		if (guess.length() != size || result.length() != size) {
			return;
		}
		// Mark these characters as tried
		tried.addAll(
				guess.chars().mapToObj(c -> (char) c).filter(c -> Character.isDigit(c)).collect(Collectors.toSet()));

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
			switch (result.charAt(i)) {
			case '0':
				if (allExactCounts.containsKey(guess.charAt(i))) {
					possibleLocation.getOrDefault(guess.charAt(i), new HashSet<Integer>()).remove(i);
				} else {
					allExactCounts.put(guess.charAt(i), 0l);
					possibleLocation.remove(guess.charAt(i));
				}
				break;
			case '1':
				possibleLocation.getOrDefault(guess.charAt(i), new HashSet<Integer>()).remove(i);
				must.add(guess.charAt(i));
				break;
			case '2':
				rightLocation.computeIfAbsent(guess.charAt(i), k -> new HashSet<Integer>()).add(i);
				possibleLocation.getOrDefault(guess.charAt(i), new HashSet<Integer>()).remove(i);
				must.add(guess.charAt(i));
				break;
			default:
				break;
			}
			if (allExactCounts.containsKey(guess.charAt(i)) && rightLocation
					.getOrDefault(guess.charAt(i), new HashSet<>()).size() == allExactCounts.get(guess.charAt(i))) {
				possibleLocation.remove(guess.charAt(i));
			}
		}
		if (rightLocation.containsKey('=')) {
			possibleLocation.remove('=');
		}
	}

	private Optional<String> unique(Set<String> perms) {
		// unique_perms = Permutations.eleminateDuplicates(all_perms);
		Function<String, Long> countUnique = s -> s.chars().distinct().count();
		Comparator<String> maxUnique = Comparator.comparing(countUnique);
		return Permutations.eleminateDuplicates(perms).stream().collect(Collectors.maxBy(maxUnique));
	}

	public String guess() {
//		Set<String> removed = new HashSet<>();

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
			Map<Character, Long> counters = perm.chars().mapToObj(c -> (char) c)
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
//		Optional<String> unique = addSomeRemoved(removed);
		Optional<String> unique = Optional.empty();
		return unique.orElse(unique(all_perms).orElse("none"));
	}

	@SuppressWarnings("unused")
	private Optional<String> addSomeRemoved(Set<String> removed) {
		// count the known characters
		int totalKnown = atLeastCounts.values().stream().reduce(0, Integer::sum);
		if (totalKnown == size) {
			return Optional.empty();
		}
		int countOp = atLeastCounts.entrySet().stream()
				.filter(entry -> Arrays.stream(operators).anyMatch(c -> c == entry.getKey()))
				.map(entry -> entry.getValue()).reduce(0, Integer::sum);
		int countDig = atLeastCounts.entrySet().stream()
				.filter(entry -> Arrays.stream(digits0).anyMatch(c -> c == entry.getKey()))
				.map(entry -> entry.getValue()).reduce(0, Integer::sum);

		int possibleCountOp = Math.min(maxNumO, size - countDig - 1);
		if (rightLocation.containsKey('=')) {
			possibleCountOp = Math.min(possibleCountOp,
					(int) Math.floor((rightLocation.get('=').iterator().next() - 1) / 2.0));
		}

		int possibleCountDig = Math.min(maxNumD, size - countOp - 1);
		if (countOp == possibleCountOp || countDig == possibleCountDig) {
			return Optional.empty();
		}

//		Set<Character> notTried = possibleLocation.keySet().stream().filter(c -> !tried.contains(c))
//				.collect(Collectors.toSet());
		Set<Character> charNotRight = possibleLocation.keySet().stream().filter(c -> tried.contains(c))
				.filter(c -> !rightLocation.containsKey(c)).collect(Collectors.toSet());

		Set<String> toBeAdded = removed.stream()
				.filter(perm -> perm.chars().mapToObj(c -> (char) c).filter(c -> charNotRight.contains(c)).distinct()
						.collect(Collectors.counting()) == Math.min(2, charNotRight.size()))
//				.filter(perm -> perm.chars().mapToObj(c -> (char) c).filter(c -> notTried.contains(c))
//						.collect(Collectors.counting()) == Math.min(possibleLocation.size(), size)
//								- Math.min(2, charNotRight.size()))
				.filter(perm -> perm.chars().mapToObj(c -> (char) c).filter(Character::isDigit).distinct()
						.collect(Collectors.counting()) == possibleCountDig)
				.collect(Collectors.toSet());
		Optional<String> unique = unique(toBeAdded);
		return unique;
	}

	public void play() {
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
		System.out.println("all possible answers: " + all_perms);
	}

	public static void main(String[] args) throws IOException {
		System.out.println("Enter the size of the puzzle");
		Nerdle nerdle = new Nerdle(scan.nextInt());
		nerdle.play();
		scan.close();
	}
}