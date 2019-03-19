# capnproto-java: Cap'n Proto for Java

[![Build Status](https://travis-ci.org/paxel/capnproto-java.svg?branch=master)](https://travis-ci.org/capnproto/capnproto-java)

[Cap'n Proto](http://capnproto.org) is an extremely efficient protocol for sharing data
and capabilities, and capnproto-java is a pure Java implementation.


Improvements that have been introduced here:

## Dropped support for JDK 7
The generated code now uses lambdas and streams.

## toString()
The generated source code now pimplements toString() so you can log what you receive or send.
```java
      @Override
      public String toString() {
         StringBuilder s = new StringBuilder("Lion {");
           s.append(" Id={").append(getId()).append("}");
           doName(v->s.append(" Name={").append(v).append("}"));
         return s.append("}").toString();
      }
```

## doMe(consumer)
All access methods that require isX() and/or hasX() to be called before getX() can now be accessed with doX(Consumer x). 
The consumer will only receive the X in case it is and/or has X.

```java
      public void doName(java.util.function.Consumer<org.capnproto.Text.Builder>consume) {
        if(hasName())
           consume.accept( getName() );
      }

```

## Collections
All ListReader implement Collection<> now. Yes including stream(). Yes you can instantiate list entries via stream().

```java
new MessageBuilder().initRoot(MyStyle.factory).initList(5).stream().forEach(e->e.setStart(System.currentTimeMillis()));
```

* interface annotation - you can now annotate structs with an interface to let reader and builder of similar objects implement similar interfaces to simplify usage of generated code. you're welcome.
```
struct Lion
$Java.readerInterface("org.capnproto.examples.Cat, org.capnproto.examples.Named<org.capnproto.Text.Reader>")
$Java.builderInterface("org.capnproto.examples.Cat, org.capnproto.examples.Named<org.capnproto.Text.Builder>")
{
  id @0 :UInt32;
  name @1 :Text;
}
```

turns into

```java
    public static final class Reader extends org.capnproto.StructReader  implements org.capnproto.examples.Cat, org.capnproto.examples.Named<org.capnproto.Text.Reader> {
       ....

    public static final class Builder extends org.capnproto.StructBuilder  implements org.capnproto.examples.Cat, org.capnproto.examples.Named<org.capnproto.Text.Builder> {
       ....

```

## mavenized
checkout the project
install a current maven version
install make and gcc
and the whole project can be built by calling (in root dir)

mvn clean install

to do the benchmarks call

mvn -P benchmark -pl benchmark

NOTE: I will update this part with more info about requirements soonish

# FUTURE

* allow chainbuilding (setX(return this;))
* Release process via maven.
* more tests.
* cleanup the runtime code.
* cleanup genearator code.
* create in between pojo objects for projects that want to detach from the binary data.
* remove the zero out of previous data and throw an exception instead.
* force push upstream ;) [Say Hi do David.](https://github.com/dwrensha), the maintainer of the [original capnproto-java](https://dwrensha.github.io/capnproto-java/index.html)


