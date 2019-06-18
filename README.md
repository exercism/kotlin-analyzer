#KOTLIN ANALYZER


This is an auto-analyzer for Kotlin exercises on the exercism.io platform.

##Build and make final executable
_Two way to build the project :_ 
###Using gradle wrapper included
```
./gradlew test installDist
```
###Using docker
```
docker build -t exercism/kotlin-analyzer .
```

It will : build classes and run test classes, then make the distribution (https://docs.gradle.org/current/userguide/distribution_plugin.html)

##Run

In the `./build/install/kotlinanalyzer` directory, you will find a `bin/kotlinanalyzer` executable file and a `lib` folder with all dependencies. 
Simply execute `./build/install/kotlinanalyzer/bin/kotlinanalyzer` to run the application

(A `bin/analyze.sh` is include to match the Interface of auto-mentoring in exercism.io)

##Add an exercise
In the package `io.exercism.analyzer.kotlin.exercice`, you'll find an abstract class `Exercise`. Just create a new package for the new exercise and create a new Kotlin class extended the `Exercise` abstract class. 

You only have to implement the rules overriding this function :  
`abstract fun applyRules(file: Node.File): Either<ExerciseError, Analysis>`

If you're not aware with functional programming, I use [Arrow](https://arrow-kt.io/).

Don't forget to add unit tests