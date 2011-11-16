# Decorators

I spent a year hacking Python recently, and in the process I fell in love with decorators.  They are a wonderful tool for handling indirect concerns, as they let you centralize many aspects like logging or authentication in an application.  They are also very lispy, involving all sorts of fun closure factories, and are strongly related to higher order functions.  Most importantly, though, they allow your code to be simpler (but not easier, at least at first).  The real benefit becomes apparent when testing your application.

This is an independent decorator library I've created in order to demonstrate the power of decorators.  It is made up of the following parts

* decorators.core - This is where the core decorator forms are stored
* decorators.invokable - This is where definvoke is stored.  Contributed by Meikel Brandmeyer
* decorators.same - The same protocol
* decorators.common - Includes common decorators, such as logging, validating, coercing.  Also includes the word-like protocol.
* decorators.number-protocols - A collection of protocols for converting to a numeric type.

## Usage

So what is a decorator, anyway?  For the sake of discussion, a decorator is a function that can modify a function.  Let's take a look at a small example.

Logging is the type of behavior that is commonly needed, but is unrelated to the task at hand.  Let's create a decorator to handle it.

	user=> (defn logging-dec [f]
		(fn logged-fn [& args] (apply println f args) (apply f args)))

This logging decorator takes a function in, and returns a logged version of the function.  Let's see it in action

	user=> (+ 1 2);Normal addition
	3
	user=> ((logging-dec +) 1 2)
	#<core$_PLUS_ clojure.core$_PLUS_@6c0ec436> 1 2
	3

You can see that the logging decorator added a side effect (printing), but didn't alter the original function.  Decorators are very good for managing side effects, because you can seperate out the concern. 

So, how does this work with this library?  Let's start with the decorators.core namespace.

	user=> (use 'decorators.core)

The first thing we'll want to look at is the decorate macro.  It applies a decorator to a function, and then stores it in the same symbol.  Let's see it in action, by writing our own plus function. 

	user=> (defn my+ [x y] (+ x y))
	#'user/my+
	user=> (my+ 1 2)
	3

Suppose we want to log every call to my+.  We would use the decorate macro like so

	user=> (decorate my+ logging-dec)
	#'user/my+
	user=> (my+ 1 2)
	#<user$my_PLUS_ user$my_PLUS_@30b48b11> 1 2
	3

Now *every* call to my+ will be logged, and we don't have to think about it.  This is useful, and it gets even better when you consider other decorators like validators or coercing decorators.  Let's take a look at some of the decorators in decorators.common, to see what other common patterns can be pulled out.

	user=> (use 'decorators.common)

We'll start with what I call coercing decorators.  They're useful for forcing the data into a common format.  This in turn broadens the set of input that is available to a function, making it more general.  Let's take a look at a simple example, the args-to-int decorator.

	user=> ((args-to-int +) "1" 2 3.0)
	6

As you can see, the args-to-int decorator converts all of the arguments to integers.  It is based on the ToInt protocol in decorators.number-protocols.  There are corresponding protocols & decorators for longs, doubles, etc.  Let's decorate my+ again with the args-to-int decorator

	user=> (decorate my+ args-to-int)
	#'user/my+
	user=> (my+ "1" 2.0)
	#<user$my_PLUS_ user$my_PLUS_@30b48b11> 1 2
	3

## Testing decorators

The best way to test a decorator is to decorate identity, or a special function pass (pass is a variadic version of identity).  Please see the tests for examples on how to test the decorator fns.

## JIT decorators

This is good, and it lets us

## License

Copyright (C) 2011 Sean Devlin

Distributed under the Eclipse Public License, the same as Clojure.
