# Achtung! Blinkenlights

## What does it do?

Displays a (square) grid of clickable nodes, with default size of 50. Clicking a node increments the value of that node, as well as the value of all the nodes in the same row and column. After each increment, a node briefly lights up in yellow. If two neighbouring nodes (up, down, left, right) contain a value of four and two, both these nodes light up briefly in green, and then reset their values to zero.

## Build/Run


```
$ git clone https://github.com/bramfoo/blinkenlights.git
$ cd blinkenlights/
```

Using Maven:
```
$ mvn clean verify
$ java -jar target/blinkenlights.jar [gridSize]
```

Using Java:
```
$ mkdir target/
$ javac src/main/java/blinkenlights/*.java -d target/
$ java -cp target/ blinkenlights.Blinkenlights [gridSize]
```


## To Do
* Optimisations
  * Keep active list of nodes with value 4 so not all nodes need to be checked
  * Use divide & conquer approach for checking for mouseclicks 

## Links

The following sources of information were used/helpful

* [Priority Queue](https://en.wikipedia.org/wiki/Priority_queue), used for scheduling events. Implementation used is from [Algorithms, 4th Edition booksite](http://algs4.cs.princeton.edu/24pq/MinPQ.java.html)
* [Achtung! Blinkenlights](https://en.wikipedia.org/wiki/Blinkenlights)

