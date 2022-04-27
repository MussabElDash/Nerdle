package Statistics;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import utils.Permutations;

public class CharacterStatistics {

	private Set<String> allPermutations;
	private Map<Character, Long> occur;
	private Map<String, CharacterStatistics.Details> permSum;

	public CharacterStatistics(Set<String> allPermutations) {
		this.allPermutations = allPermutations;
		permSum = permSum();
	}

	private Map<Character, Long> calcCharOccur() {
		Map<Character, Long> occur = allPermutations.stream().flatMap(p -> p.chars().boxed())
				.map(c -> (char) c.intValue())
				.collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

		return occur;
	}

	private Map<String, CharacterStatistics.Details> permSum() {
		occur = calcCharOccur();
		Map<String, CharacterStatistics.Details> sums = allPermutations.stream()
				.collect(Collectors.toMap(Function.identity(), Details::new));
		return sums;
	}

	public List<String> getMaxSums() {
		return getMaxSums(allPermutations);
	}

	public List<String> getMaxSums(Collection<String> availablePerms) {
		return permSum.entrySet().stream().filter(entry -> availablePerms.contains(entry.getKey()))
				.sorted(Entry.comparingByValue()).map(Entry::getKey).collect(Collectors.toList());
	}

	public class Details implements Comparable<Details> {
		private String perm;
		private long distinctChars;
		private long sum;

		public Details(String perm) {
			this.perm = perm;
			distinctChars = perm.chars().distinct().count();
			sum = perm.chars().mapToObj(i -> (char) i).map(occur::get).reduce(0l, Long::sum);
		}

		public String getPerm() {
			return perm;
		}

		public long getDistinctChars() {
			return distinctChars;
		}

		public long getSum() {
			return sum;
		}

		@Override
		public int compareTo(CharacterStatistics.Details o) {
			double distinctDistance = Math.pow(distinctChars - o.distinctChars, 2);
			double sumDistance = Math.pow(sum - o.sum, 2);
			return (int) Math.ceil(Math.sqrt(distinctDistance + sumDistance));
		}
	}

	public static void main(String[] args) throws URISyntaxException, IOException {
		Set<String> allPermutations = Permutations.loadPerm(8);
		CharacterStatistics stats = new CharacterStatistics(allPermutations);
		System.out.println(stats.getMaxSums());
	}
}
