import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

public class TotalSimulation {
	private int size;
	private String solution;
	private Simulation simulation;
	private Optional<Map<Integer, AnsweredGuess>> simulationResult = Optional.empty();

	public TotalSimulation(String solution) {
		this.solution = solution;
		size = solution.length();
		simulation = new Simulation(solution);
	}

	public Map<Integer, AnsweredGuess> simulate(Optional<String> firstGuess) throws IOException, URISyntaxException {
		simulationResult = Optional.of(new HashMap<>());

		Nerdle nerdle = new Nerdle(size);

		String ans = "";
		int guessNum = 0;
		String expectedAns = StringUtils.repeat('2', size);

		while (!ans.equals(expectedAns)) {
			guessNum++;
			String guess;
			if (guessNum == 1 && firstGuess.isPresent()) {
				guess = firstGuess.get();
			} else if (guessNum == 1 && Nerdle.firstGuesses.containsKey(size)) {
				guess = Nerdle.firstGuesses.get(size);
			} else {
				guess = nerdle.guess();
			}
			ans = simulation.exactSimulation(guess);
//			ans = simulation.simulate(guess);
			nerdle.guessed(guess, ans);
			simulationResult.get().put(guessNum, new AnsweredGuess(guess, ans));
			if (Permutations.eleminateDuplicates(List.of(guess, solution)).size() == 1) {
				break;
			}
		}

		return simulationResult.get();
	}

	public String getSolution() {
		return solution;
	}

	public static class AnsweredGuess {
		private String guess, answer;

		public AnsweredGuess(String guess, String answer) {
			super();
			this.guess = guess;
			this.answer = answer;
		}

		public String getGuess() {
			return guess;
		}

		public String getAnswer() {
			return answer;
		}

		@Override
		public String toString() {
			return "AnsweredGuess [guess=" + guess + ", answer=" + answer + "]";
		}

	}

	public Optional<Map<Integer, TotalSimulation.AnsweredGuess>> getSimulationResult() {
		return simulationResult;
	}

	public Integer numberOfGuesses() {
		return getSimulationResult().map(Map::keySet).map(Collections::max).orElse(Integer.MAX_VALUE);
	}

	public static void main(String[] args) throws IOException, URISyntaxException {
//		TotalSimulation sim = new TotalSimulation("10+1-8=3");
//		TotalSimulation sim = new TotalSimulation("58-46=12");
//		TotalSimulation sim = new TotalSimulation("10/2=5");
//		System.out.println(sim.simulate(Optional.of("58-46=12")));
		TotalSimulation sim = new TotalSimulation("6+5+6=17");
		System.out.println(sim.simulate(Optional.of("11-6-5=0")));
		System.out.println(sim.numberOfGuesses());
	}
}
