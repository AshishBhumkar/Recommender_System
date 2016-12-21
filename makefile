JFLAGS = -g
JC = javac
JVM = java
RM = rm

FILE=
.SUFFIXES: .java .class
.java.class:
	$(JC) -classpath $(JAR) $(JFLAGS) $*.java
CLASSES = \
	*.java
	    
MAIN = RecommenderSystem

default: classes

classes: $(CLASSES:.java=.class)

run: $(MAIN).class
	$(JVM) $(MAIN)

clean:
	$(RM) -rf *.class

