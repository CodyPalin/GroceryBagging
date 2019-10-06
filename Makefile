JFLAGS = -g
JC = javac
.SUFFIXES: .java .class
.java.class:
	$(JC) $(JFLAGS) $*.java

CLASSES = \
	Bag.java \
	Item.java \
	SearchState.java \
	Main.java 

default: classes

classes: $(CLASSES:.java=.class)

clean:
	$(RM) *.class
