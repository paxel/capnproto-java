# capnproto-java: Cap'n Proto for Java

[![Build Status](https://travis-ci.org/paxel/capnproto-java.svg?branch=master)](https://travis-ci.org/capnproto/capnproto-java)

[Cap'n Proto](http://capnproto.org) is an extremely efficient protocol for sharing data
and capabilities, and capnproto-java is a pure Java implementation.


Improvements that have been introduced here:

## TAG 2.0.0-rc002

### MessageReader and MessageBuilder now have a getSerializedSize() method that gives the exact size when will be written or was read.
This replaces Serialize.computeSerializedSizeInWords().

### MessageBuilder now has a write(ByteBuffer) method
This replaces Serialize.write(bb)

### AllocatedArenaBuilder builds an Arena from ByteBuffer or ReadableChannel
This replaces Serialize.read(..)


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


## chain building
The generated builder code now allows to chain build. This can make more compact, but also unreadable code. 
Handle with care!

```java
        org.capnproto.MessageBuilder message = new org.capnproto.MessageBuilder();
        AddressBook.Builder addressbook = message.initRoot(AddressBook.factory);
        StructList.Builder<Person.Builder> people = addressbook.initPeople(2);

        final Person.Builder alice = people.get(0).setId(123).setName("Alice").setEmail("alice@example.com");
        alice.initPhones(1).get(0).setNumber("555-1212").setType(Person.PhoneNumber.Type.MOBILE);
        alice.getEmployment().setSchool("MIT");

        final Person.Builder bob = people.get(1).setId(456).setName("Bob").setEmail("bob@example.com");
        bob.initPhones(2).get(0).setNumber("555-4567").setType(Person.PhoneNumber.Type.HOME);
        bob.getPhones().get(1).setNumber("555-7654").setType(Person.PhoneNumber.Type.WORK);
        bob.getEmployment().setUnemployed(org.capnproto.Void.VOID);

```

## Support for external memory management
The MessageBuilder and MessageReader have been opened to support custom Arena and SegmentReader and SegmentBuilder implementations.

For full memory control building a capnproto object extend

```java

/**
 * The Arena used for allocating new Segments.
 */
public interface AllocatingArena extends Arena {

    /**
     * Allocate a new Segment in case the previous Segment is not big enough for
     * the requested data.
     *
     * @param amountPlusRef the number of words needed.
     * @return The result of the allocation.
     */
    BuilderArena.AllocateResult allocate(int amountPlusRef);

    /**
     * Provides the {@link GenericSegmentBuilder} for the given segment ID.
     *
     * @param segmentId the segment ID
     * @return the segment.
     */
    @Override
    GenericSegmentBuilder tryGetSegment(int segmentId);

    /**
     * Retrieve the ByteBuffers for Serialization.
     *
     * @return the buffers.
     */
    ByteBuffer[] getSegmentsForOutput();

    /**
     * Access all currently existing segments.
     *
     * @return the segments.
     */
    List<? extends GenericSegmentBuilder> getSegments();
}


/**
 * Representation of the SegmentBuilder. This Builder is responsible to manage
 * one Segment for building a new Message.
 */
public interface GenericSegmentBuilder extends SegmentDataContainer {

    static final int FAILED_ALLOCATION = -1;

    /**
     * Puts the long value into the buffer at word index.
     *
     * @param index The word index.
     * @param value The value to add.
     */
    void put(int index, long value);

    /**
     * The current size of the Segment.
     *
     * @return size
     */
    int currentSize();

    /**
     * allocate more memory in this segment.
     *
     * @param words
     * @return the start position of the allocated words. -1 means there was not
     * enough space in the segment.
     */
    int allocate(int words);

    /**
     * Retrieve the AllocatingArena.
     *
     * @return the arena.
     */
    @Override
    AllocatingArena getArena();

    /**
     * Checks if the Segment is writable.
     *
     * @return {@code true}
     */
    boolean isWritable();

    /**
     * Retrieve the ID of this segment.
     *
     * @return the ID
     */
    int getId();

    /**
     * Sets the ID of the Segment.
     *
     * @param id the new ID.
     */
    void setId(int id);

    /**
     * Prepares the underlying ByteBuffer to be written.
     *
     * @return
     */
    ByteBuffer getSegmentForOutput();

}

```

For preallocated memory control in reading objects implement

```java

/**
 * Represents an Arena with previously allocated Segments.
 */
public interface AllocatedArena extends Arena {

    /**
     * Provides the {@link GenericSegmentReader} for the given segment ID.
     *
     * @param segmentId the segment ID.
     * @return the segment.
     */
    @Override
    public GenericSegmentReader tryGetSegment(int segmentId);

    /**
     * Access all existing segments
     *
     * @return the segments.
     */
    List<? extends GenericSegmentReader> getSegments();

}


/**
 * Representation of the SegmentBuilder. This Builder is responsible to manage
 * one Segment of an existing message.
 */
public interface GenericSegmentReader extends SegmentDataContainer {

    GenericSegmentReader EMPTY = new SegmentReader(ByteBuffer.allocate(8), null);

    /**
     * Retrieve the Arena containing all Segments.
     *
     * @return the arena.
     */
    @Override
    public AllocatedArena getArena();

}

```


## mavenized
checkout the project
install a current maven version
install make and gcc
and the whole project can be built by calling (in root dir)

mvn clean install

to do the benchmarks call

mvn install -P benchmark -pl benchmark

NOTE: I will update this part with more info about requirements soonish

# FUTURE

* Release process via maven.
* more tests.
* cleanup the runtime code.
* cleanup genearator code.
* create in between pojo objects for projects that want to detach from the binary data.
* remove the zero out of previous data and throw an exception instead.
* force push upstream ;) [Say Hi do David.](https://github.com/dwrensha), the maintainer of the [original capnproto-java](https://dwrensha.github.io/capnproto-java/index.html)


