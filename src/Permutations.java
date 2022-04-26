import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.script.ScriptException;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

public class Permutations {
	private int size;
	private int[] digits0 = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 }, digits1 = { 1, 2, 3, 4, 5, 6, 7, 8, 9 };
	private char[] opers = { '+', '-', '*', '/' },
			digits_opers = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '-', '*', '/' };
	private Set<String> all_perms = new HashSet<>();

	public Permutations(int size) {
		this.size = size;
	}

	public void generate_perms(int level, int max_level, String prev_perm) throws ScriptException {
		if (max_level < 0) {
			return;
		}
		if (level == max_level) {
			// End With a digit
			if (new String(opers).chars().mapToObj(o -> (char) o)
					.anyMatch(o -> !prev_perm.isBlank() && prev_perm.charAt(prev_perm.length() - 1) == o)) {
				// No zero digit after an operator
				for (int i : digits1) {
					verify_perm(prev_perm + i);
				}
			} else {
				// Any digit after digit
				for (int i : digits0) {
					verify_perm(prev_perm + i);
				}
			}
		} else if (level == 0) {
			// Start with a non-zero digit
			for (int i : digits1) {
				generate_perms(1, max_level, prev_perm + i);
			}
		} else {
			// In the middle
			if (new String(opers).chars().mapToObj(o -> (char) o)
					.anyMatch(o -> prev_perm.charAt(prev_perm.length() - 1) == o)) {
				for (int i : digits1) {
					generate_perms(level + 1, max_level, prev_perm + i);
				}
			} else {
				for (char c : digits_opers) {
					generate_perms(level + 1, max_level, prev_perm + c);
				}
			}
		}
	}

	private void verify_perm(String perm) throws ScriptException {
		Expression expression = new ExpressionBuilder(perm).build();
		double val = expression.evaluate();
		int intVal = (int) val;
		String fullPerm = perm + "=" + intVal;
		if (val >= 0 && val == intVal && fullPerm.length() == size) {
			all_perms.add(fullPerm);
		}
	}

	public Set<String> getPerms() throws ScriptException {
		if (all_perms.isEmpty()) {
			for (int i = 0; i < size - 2; i++) {
				generate_perms(0, i, "");
			}
		}
		return all_perms;
	}

	public static Set<String> eleminateDuplicates(Collection<String> all_perms) {
		String operators = "[\\+\\*\\-/=]";
		Map<Object, List<String>> map = all_perms.stream().collect(Collectors.groupingBy(s -> {
			String split[] = s.split(operators);
			split[split.length - 1] = "=" + split[split.length - 1];
			Set<String> res = new HashSet<>(Arrays.asList("+", "-", "*", "/"));
			res.removeIf(o -> !s.contains(o));
			res.addAll(Arrays.asList(split));
			return res;
		}, Collectors.toList()));
		return map.values().stream().map(l -> l.stream().findAny()).filter(e -> e.isPresent()).map(e -> e.get())
				.collect(Collectors.toSet());
	}

	public static Set<String> loadPerm(int size) throws URISyntaxException, IOException {
		Set<String> all_perm = new HashSet<String>();

		Path filePath = Constants.getZstdFilePath(size, true);
		filePath = Paths.get(Compression.class.getResource(filePath.toString()).toURI());

		BufferedReader bf = Compression.getBufferedReader(filePath.toFile());
		String line;
		while ((line = bf.readLine()) != null) {
			all_perm.add(line);
		}
		bf.close();

		return all_perm;
	}

	public static void main(String[] args) throws IOException, ScriptException {
		for (int i = 3; i < 10; i++) {
			System.out.println("Generating for size " + i);
			Permutations perms = new Permutations(i);
			Set<String> all_perms = perms.getPerms();
			System.out.println("Generated " + all_perms.size() + " permutations");
			PrintWriter pw = new PrintWriter(new FileWriter("Permutations" + i + ".txt"));
			System.out.println("Writing the permutations into Permutations" + i + ".txt");
			for (String perm : all_perms) {
				pw.println(perm);
			}
			System.out.println("Done writing to the file");
			System.out.println("==============================");
			pw.close();
		}
	}
};