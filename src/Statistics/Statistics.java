package Statistics;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.DecimalFormat;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import Play.Nerdle;
import Simulation.TotalSimulation;
import utils.Permutations;
import utils.Compare.Compare;

public class Statistics {
	private static final DecimalFormat dfSharp = new DecimalFormat("#.##");
	private int size;
	private Set<String> allPermutations;

	public Statistics(int size) throws URISyntaxException, IOException {
		this.size = size;
		allPermutations = Permutations.loadPerm(size);
	}

	public Map<String, List<Integer>> numberOfTrials() throws IOException, URISyntaxException {
		Map<String, List<Integer>> numberOfTrials = new ConcurrentHashMap<>();
		AtomicInteger num = new AtomicInteger();
		allPermutations.parallelStream().forEach(firstGuess -> {
			System.out.println("Checking guess number: " + num.incrementAndGet());
			allPermutations.parallelStream().forEach(solution -> {
				TotalSimulation sim = new TotalSimulation(solution);
				try {
					sim.simulate(Optional.of(firstGuess));
				} catch (IOException | URISyntaxException e) {
					e.printStackTrace();
					return;
				}
				numberOfTrials.computeIfAbsent(firstGuess, k -> new ArrayList<>()).add(sim.numberOfGuesses());
			});
		});
		return numberOfTrials;
	}

	public Map<String, Double> averageTrails(Map<String, List<Integer>> numberOfTrials) {
		return numberOfTrials.entrySet().stream()
				.map(entry -> new SimpleEntry<String, OptionalDouble>(entry.getKey(),
						entry.getValue().stream().mapToInt(Integer::valueOf).filter(i -> i != Integer.MAX_VALUE)
								.average()))
				.filter(entry -> entry.getValue().isPresent())
				.map(entry -> new SimpleEntry<>(entry.getKey(), entry.getValue().getAsDouble()))
				.collect(Collectors.toMap(Entry::getKey, Entry::getValue));

	}

	public Statistics.StatisticsDetails calcStatistics() throws URISyntaxException, IOException {
		Nerdle.setAllPermutation(allPermutations);
		StatisticsDetails details = new StatisticsDetails();
		details.setSize(size);
		details.setAllPermutations(allPermutations);

		Map<String, List<Integer>> numberOfTrials = numberOfTrials();
		details.setAllNumberOfTrials(numberOfTrials);

		Map<String, Double> averageTrails = averageTrails(numberOfTrials);
		details.setAllAverages(averageTrails);

		int max = numberOfTrials.values().stream().flatMap(List::stream).mapToInt(Integer::valueOf).max()
				.orElse(Integer.MIN_VALUE);
		details.setMax(max);

		int bestTrials = (int) Math.ceil(averageTrails.values().stream().min(Double::compare).orElse(Double.MAX_VALUE));
		details.setAverageTrials(bestTrials);

		Map<String, Double> bestAverages = averageTrails.entrySet().stream()
				.filter(entry -> entry.getValue() <= bestTrials)
				.collect(Collectors.toMap(Entry::getKey, Entry::getValue));

		for (String perm : bestAverages.keySet()) {
			double average = averageTrails.get(perm);
			StatisticsDetails.PermutationStatistics stats = new StatisticsDetails.PermutationStatistics();
			stats.setAverage(average);
			stats.setPermutation(perm);
			List<Integer> numbers = numberOfTrials.get(perm);
			stats.setMax(Collections.max(numbers));

			Map<Integer, Integer> countOfTrials = new HashMap<>();
			for (int i = 1; i <= stats.getMax(); i++) {
				int functionI = i;
				countOfTrials.put(i, (int) numbers.stream().filter(t -> functionI == t).count());
			}
			stats.setCountOfTrials(countOfTrials);

			Map<Integer, Double> percentageOfTrials = new HashMap<>(countOfTrials.entrySet().stream()
					.map(entry -> new SimpleEntry<Integer, Double>(entry.getKey(),
							(entry.getValue() * 100.0) / allPermutations.size()))
					.collect(Collectors.toMap(Entry::getKey, Entry::getValue)));
			stats.setPercentageOfTrials(percentageOfTrials);

			Map<Integer, Double> percentageLessEqualAvg = new HashMap<>();
			percentageLessEqualAvg.put(1, percentageOfTrials.get(1));
			for (int i = 2; i <= stats.getMax(); i++) {
				percentageLessEqualAvg.put(i,
						Math.min(100, percentageOfTrials.get(i) + percentageLessEqualAvg.get(i - 1)));
			}
			stats.setPercentageLessEqualAvg(percentageLessEqualAvg);

			details.addPermutationStatistics(stats);

			countOfTrials.remove(1);
			percentageOfTrials.remove(1);
			percentageLessEqualAvg.remove(1);
		}

		int bestMax = details.getAllStatistics().stream().mapToInt(stat -> stat.getMax()).min()
				.orElse(Integer.MAX_VALUE);

		details.getAllStatistics().removeIf(stat -> stat.getMax() > bestMax);

		Collections.sort(details.getAllStatistics());
		details.setBestAverage(Collections.min(details.getAllStatistics()));

		return details;
	}

	public static class StatisticsDetails {
		private int size;
		private double averageTrials;
		private PermutationStatistics bestAverage;
		private Map<String, List<Integer>> allNumberOfTrials;
		private Map<String, Double> allAverages;
		private int max;
		private Set<String> allPermutations;
		private List<PermutationStatistics> allStatistics;

		public Map<String, List<Integer>> getAllNumberOfTrials() {
			return allNumberOfTrials;
		}

		public void setAllNumberOfTrials(Map<String, List<Integer>> allNumberOfTrials) {
			this.allNumberOfTrials = allNumberOfTrials;
		}

		public double getAverageTrials() {
			return averageTrials;
		}

		public void setAverageTrials(double averageTrials) {
			this.averageTrials = averageTrials;
		}

		public Map<String, Double> getAllAverages() {
			return allAverages;
		}

		public void setAllAverages(Map<String, Double> allAverages) {
			this.allAverages = allAverages;
		}

		public int getMax() {
			return max;
		}

		public void setMax(int allMax) {
			this.max = allMax;
		}

		public Set<String> getAllPermutations() {
			return allPermutations;
		}

		public void setAllPermutations(Set<String> allPermutations) {
			this.allPermutations = allPermutations;
		}

		public int getSize() {
			return size;
		}

		public void setSize(int size) {
			this.size = size;
		}

		public PermutationStatistics getBestAverage() {
			return bestAverage;
		}

		public void setBestAverage(PermutationStatistics bestAverage) {
			this.bestAverage = bestAverage;
		}

		public List<PermutationStatistics> getAllStatistics() {
			return allStatistics;
		}

		public void setAllStatistics(List<PermutationStatistics> allStatisrics) {
			this.allStatistics = allStatisrics;
		}

		public void addPermutationStatistics(Statistics.StatisticsDetails.PermutationStatistics stats) {
			if (allStatistics == null) {
				allStatistics = new ArrayList<>();
			}
			allStatistics.add(stats);
		}

		@Override
		public String toString() {
			return "StatisticsDetails [size=" + size + ", max=" + max + ", averageTrials=" + averageTrials
					+ ", bestAverage=" + bestAverage + ", allStatistics=" + allStatistics + "]";
		}

		public static class PermutationStatistics implements Comparable<PermutationStatistics> {
			private String permutation;
			private int max;
			private Map<Integer, Integer> countOfTrials;
			private Map<Integer, Double> percentageOfTrials;
			private double average;
			private Map<Integer, Double> percentageLessEqualAvg;

			public String getPermutation() {
				return permutation;
			}

			public void setPermutation(String permutation) {
				this.permutation = permutation;
			}

			public int getMax() {
				return max;
			}

			public void setMax(int max) {
				this.max = max;
			}

			public Map<Integer, Integer> getCountOfTrials() {
				return countOfTrials;
			}

			public void setCountOfTrials(Map<Integer, Integer> countOfTrials) {
				this.countOfTrials = countOfTrials;
			}

			public Map<Integer, Double> getPercentageOfTrials() {
				return percentageOfTrials;
			}

			public void setPercentageOfTrials(Map<Integer, Double> percentageOfTrials) {
				this.percentageOfTrials = percentageOfTrials;
			}

			public double getAverage() {
				return average;
			}

			public void setAverage(double average) {
				this.average = average;
			}

			public Map<Integer, Double> getPercentageLessEqualAvg() {
				return percentageLessEqualAvg;
			}

			public void setPercentageLessEqualAvg(Map<Integer, Double> percentageLessEqualAvg2) {
				this.percentageLessEqualAvg = percentageLessEqualAvg2;
			}

			@Override
			public String toString() {
				return "PermutationStatistics [permutation=" + permutation + ", max=" + max + ", average="
						+ dfSharp.format(average) + ", percentageLessEqualAvg="
						+ Statistics.toString(percentageLessEqualAvg, "%") + "]";
			}

			@Override
			public int compareTo(Statistics.StatisticsDetails.PermutationStatistics o) {
				int compare = Double.compare(max, o.getMax());
				int avg = (int) Math.ceil(average);
				while (compare == 0 && avg > 2) {
					Math.round(avg);
					double sum1 = percentageLessEqualAvg.get(avg);
					double sum2 = o.getPercentageLessEqualAvg().get(avg);
					compare = Compare.compareTo(sum2, sum1);
					avg--;
				}
				if (compare == 0) {
					compare = Double.compare(average, o.getAverage());
				}
				return compare;
			}

		}
	}

	public static String toString(Map<Integer, Double> map, String nextToValue) {
		String mapAsString = map.keySet().stream().map(key -> key + "=" + dfSharp.format(map.get(key)) + nextToValue)
				.collect(Collectors.joining(", ", "{", "}"));
		return mapAsString;

	}

	public static void main(String[] args) throws URISyntaxException, IOException {
		Statistics stat = new Statistics(8);
		System.out.println(stat.calcStatistics());
	}
}
