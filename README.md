# Simple nerdle solver
Yet another simple solver for [Nerdle](https://nerdlegame.com/). Written in Java.

Doesn't Provide optimal paths, but suggest a solution that provides most eliminations possible.
All possible permutaions will be listed.
You can use `Permutation.java` to generate files with all possible permutations for puzzle size (3,5-10) which are already generated, You can just start playing now.

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
[93-37=56, 30-5*6=0, 96-39=57, 33-5*6=3, 30-6*5=0, 65-30=35, 95-30=65, 33-6*5=3, 95-65=30, 65-35=30, 96-37=59, 39-5*6=9, 95-39=56, 96-57=39, 37-6*5=7, 35-6*5=5, 93-57=36, 39-6*5=9, 37-5*6=7, 96-59=37, 36-5*6=6, 35-5*6=5, 75-39=36, 95-35=60, 95-60=35, 95-59=36, 36-6*5=6]
Enter your guess followed by the result
67*9=603
10101001

recommended Guess is 35-6*5=5
[35-6*5=5]
```
