# Design Patterns

-----


## Creational
Used to construct objects such that they can be decoupled from their implementing system.

### Abstract Factory
This pattern provides an interface for creating families of simialr/depenedent objects without specifying their concrete classes, rather it delegates the creation to the concrete class.

``` Java
class A {
    private Factory factory;

    public A(Factory factory) {
        this.factory = factory;
    }

    public void doSomething() {
        //The concrete class of "f" depends on the concrete class
        //of the factory passed into the constructor. If you provide a
        //different factory, you get a different Foo object.
        Foo f = factory.makeFoo();
        f.whatever();
    }
}

interface Factory {
    Foo makeFoo();
    Bar makeBar();
    Dummy makeDummy();
}

//need to make concrete factories that implement the "Factory" interface here
```

### Builder
Used to encapsulate the construction of an object and allow it to be constructed in steps. Allows for flexible design.



### Factory
This pattern exposes methods for creating objects but lets subclasses decide on which class to instantiate.

``` Java
class A {
    public void doSomething() {
        Foo f = makeFoo();
        f.whatever();   
    }

    protected Foo makeFoo() {
        return new RegularFoo();
    }
}

class B extends A {
    protected Foo makeFoo() {
        //subclass is overriding the factory method
        //to return something different
        return new SpecialFoo();
    }
}

```


### Prototype
Used when creating an instance of a class is complex or expensive

### Singleton
Used to create a unique object which is not duplicated
