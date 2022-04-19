import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Nerdle {
	private static Scanner scan = new Scanner(System.in);
	private static int[] digits1 = { 1, 2, 3, 4, 5, 6, 7, 8, 9 };
	private static char[] operands = { '+', '-', '*', '/' };
	private int size;
	private Map<Character, Set<Integer>> possibleLocation = new HashMap<>();
	private Map<Character, Set<Integer>> rightLocation = new HashMap<>();
	private Set<Character> must = new HashSet<Character>();
	private Set<String> all_perms = new HashSet<String>();
	private Map<Character, Boolean> hasMany = new HashMap<>();

	public Nerdle(int size) throws IOException {
		this.size = size;
		for (int d : digits1) {
			hasMany.put(Character.forDigit(d, 10), true);
			for (int i = 0; i < size; i++) {
				possibleLocation.computeIfAbsent(Character.forDigit(d, 10), k -> new HashSet<Integer>()).add(i);
			}
		}
		hasMany.put('0', true);
		for (int i = 1; i < size; i++) {
			possibleLocation.computeIfAbsent('0', k -> new HashSet<Integer>()).add(i);
		}
		for (char c : operands) {
			hasMany.put(c, true);
			for (int i = 1; i < size - 3; i++) {
				possibleLocation.computeIfAbsent(c, k -> new HashSet<Integer>()).add(i);
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
	 * @param s
	 * @param result
	 */
	public void guessed(String s, String result) {
		if (s.length() != size || result.length() != size) {
			return;
		}
		for (int i = 0; i < size; i++) {
			int tempi = i;
			switch (result.charAt(i)) {
			case '0':
				if (IntStream.range(0, size).anyMatch(
						ind -> ind != tempi && s.charAt(ind) == s.charAt(tempi) && result.charAt(ind) != '0')) {
					possibleLocation.getOrDefault(s.charAt(i), new HashSet<Integer>()).remove(i);
					hasMany.put(s.charAt(i), false);
				} else {
					possibleLocation.remove(s.charAt(i));
				}
				break;
			case '1':
				possibleLocation.getOrDefault(s.charAt(i), new HashSet<Integer>()).remove(i);
				must.add(s.charAt(i));
				break;
			case '2':
				rightLocation.computeIfAbsent(s.charAt(i), k -> new HashSet<Integer>()).add(i);
				possibleLocation.getOrDefault(s.charAt(i), new HashSet<Integer>()).remove(i);
				must.add(s.charAt(i));
				break;
			default:
				break;
			}
		}
		if (rightLocation.containsKey('=')) {
			possibleLocation.remove('=');
		}
	}

	private String unique() {
		int max = 0;
		String maxStr = "none";
		for (String s : all_perms) {
			Set<Character> chars = s.chars().mapToObj(c -> (char) c).collect(Collectors.toSet());
			if (chars.size() > max) {
				max = chars.size();
				maxStr = s;
			}
		}
		return maxStr;
	}

	public String guess() {
		Iterator<String> iter = all_perms.iterator();
		allPerms: while (iter.hasNext()) {
			String perm = iter.next();
			if (perm.equals("35-6*5=5")) {
				System.out.println();
			}
			// Checking if all the values that must be in the equation are in the equation
			if (must.stream().anyMatch(c -> !perm.contains(c.toString()))) {
				iter.remove();
				continue;
			}
			// Checking if the values that their location are known are in their correct
			// locations
			for (char k : rightLocation.keySet()) {
				if (rightLocation.get(k).stream().anyMatch(i -> perm.charAt(i) != k)) {
					iter.remove();
					continue allPerms;
				}
			}
			// Checking if the values should be occurring more than once
			Map<Character, Long> counters = perm.chars().mapToObj(c -> (char) c)
					.collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
			if (counters.keySet().stream().anyMatch(k -> counters.get(k) > 1 && !hasMany.get(k))) {
				iter.remove();
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
		return unique();
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
		System.out.println(all_perms);
	}

	public static void main(String[] args) throws IOException {
		System.out.println("Enter the size of the puzzle");
		Nerdle nerdle = new Nerdle(scan.nextInt());
		nerdle.play();
		scan.close();
	}
}