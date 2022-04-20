# Simple nerdle solver
Yet another simple solver for [Nerdle](https://nerdlegame.com/). Written in Java.

Doesn't Provide optimal paths, but suggest a solution that provides most eliminations possible.
All possible permutaions will be listed.
You can use `Permutation.java` to generate files with all possible permutations for puzzle size (3,5-9) which are already generated, You can just start playing now.
```
Permutation of the size 3 puzzles are
0=0
1=1
2=2
3=3
4=4
5=5
6=6
7=7
8=8
9=9
```

You can use `Simulation.java` to simulate the nerdle's reponses (may mark a different cell as black in case of guess contains more of the same number than the solution has). for example the following might happen:
```
You Guess:
679/7=97
Nerdle's reposonse might be 
01020202
while the simulation response might be
00021202
both hints while they're in different location helps you know the exact number of occurrences of the numer `7`
```

## Playing
Start with defing the size of the puzzle.
You'll always have to enter your guess followed by Nerdle's response.

- black - **0** 
- purple - **1**
- green - **2**

```
$ java -jar Nerdle.jar

Enter your guess followed by the result
Enter the size of the puzzle
8
Enter your guess followed by the result
58-46=12
10201100
recommended Guess is 93-37=56
all possible answers: [93-37=56, 30-5*6=0, 96-39=57, 33-5*6=3, 30-6*5=0, 65-30=35, 95-30=65, 33-6*5=3, 95-65=30, 65-35=30, 96-37=59, 39-5*6=9, 95-39=56, 96-57=39, 37-6*5=7, 35-6*5=5, 93-57=36, 39-6*5=9, 37-5*6=7, 96-59=37, 36-5*6=6, 35-5*6=5, 75-39=36, 95-35=60, 95-60=35, 95-59=36, 36-6*5=6]
Enter your guess followed by the result
67*9=603
10101001
recommended Guess is 35-6*5=5
all possible answers: [35-6*5=5]
```
## Credits
Permutations implementaion from [starypatyk](https://github.com/starypatyk/nerdle-solver) with my own solver to add the option to enter my own guess and change the size of the puzzle to be able to use it on Classic/Mini/Instant/Speed versions of Nerdle
