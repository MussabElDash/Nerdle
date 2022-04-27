package Play;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;
import java.util.Set;

import Simulation.Simulation;
import Statistics.CharacterStatistics;
import utils.Permutations;

public class Nerdle {
	private static Set<String> allPermutation;
	private static Scanner scan = new Scanner(System.in);
	public static Map<Integer, String> firstGuesses = Map.of(5, "3*2=6", 6, "3*7=56");
	private Set<String> all_perms;
	private Collection<String> uniquePerms = new HashSet<>();
	private CharacterStatistics charStats;
	private int size;

	public Nerdle(int size) throws IOException, URISyntaxException {
		this.size = size;
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
		Map<String, Set<String>> comparisons = Simulation.providePossibleAnswers(guess, all_perms);
		all_perms = comparisons.get(result);
	}

	private Optional<String> unique(Set<String> perms) {
//		Function<String, Long> countUnique = s -> s.chars().distinct().count();
//		Comparator<String> maxUnique = Comparator.comparing(countUnique);

		Collection<String> mostProbable = Permutations.eleminateDuplicates(perms);
		mostProbable = charStats.getMaxSums(mostProbable);

		uniquePerms = Permutations.eleminateDuplicates(all_perms);
		if (uniquePerms.size() == 1) {
			return uniquePerms.stream().findAny();
		}
//		return uniquePerms.stream().collect(Collectors.maxBy(maxUnique));
//		return mostProbable.stream().collect(Collectors.maxBy(maxUnique));
		return mostProbable.stream().findFirst();
	}

	public String guess() {
		if (all_perms.size() == 1) {
			uniquePerms = all_perms;
			return all_perms.stream().findAny().get();
		}
		return unique(Simulation.bestGuess(allPermutation, all_perms)).orElse("none");
	}

	public void play() {
		System.out.println("recommended Guess is " + firstGuesses.get(size));
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
		int size = scan.nextInt();
		Nerdle nerdle = new Nerdle(size);
		nerdle.play();
		scan.close();
	}
}