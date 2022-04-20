import java.util.Map;
import java.util.Scanner;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Simulation {

	private int size;
	private String solution;
	private Map<Character, Long> counts;

	public Simulation(int size, String solution) {
		if (solution == null || solution.isBlank() || solution.length() != size) {
			throw new IllegalArgumentException();
		}
		this.size = size;
		this.solution = solution;
		counts = solution.chars().mapToObj(c -> (char) c)
				.collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
	}

	public String simulate(String guess) {
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

	public static void main(String[] args) {
		Scanner scan = new Scanner(System.in);
//		Simulation sim = new Simulation(8, "34+37=71");
//		Simulation sim = new Simulation(8, "75-20=55");
		Simulation sim = new Simulation(8, "35-6*5=5");
		String res = "";
		while (!res.equals("22222222")) {
			System.out.println(res = sim.simulate(scan.next()));
		}
		scan.close();
	}

}
