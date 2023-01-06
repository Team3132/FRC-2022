import { r as react } from './common/index-04edb6a1.js';
import './common/_commonjsHelpers-8c19dec8.js';

var PUBLISH = 0;
var SUBSCRIBE = 1;
var RESET = 2;
var VALUE = 4;

/**
 * Utils includes
 * - a handful of functional utilities inspired by or taken from the [Ramda library](https://ramdajs.com/);
 * - TypeScript crutches - the [[tup]] function.
 *
 * Use these for your convenience - they are here so that urx is zero-dependency package.
 *
 * @packageDocumentation
 */

/**
 * Performs left to right composition of two functions.
 */
function compose(a, b) {
  return function (arg) {
    return a(b(arg));
  };
}
/**
 * Takes a value and applies a function to it.
 */

function thrush(arg, proc) {
  return proc(arg);
}
/**
 * Takes a 2 argument function and partially applies the first argument.
 */

function curry2to1(proc, arg1) {
  return function (arg2) {
    return proc(arg1, arg2);
  };
}
/**
 * Takes a 1 argument function and returns a function which when called, executes it with the provided argument.
 */

function curry1to0(proc, arg) {
  return function () {
    return proc(arg);
  };
}
/**
 * Returns a function which extracts the property from from the passed object.
 */

function prop(property) {
  return function (object) {
    return object[property];
  };
}
/**
 * Calls callback with the first argument, and returns it.
 */

function tap(arg, proc) {
  proc(arg);
  return arg;
}
/**
 *  Utility function to help typescript figure out that what we pass is a tuple and not a generic array.
 *  Taken from (this StackOverflow tread)[https://stackoverflow.com/questions/49729550/implicitly-create-a-tuple-in-typescript/52445008#52445008]
 */

function tup() {
  for (var _len = arguments.length, args = new Array(_len), _key = 0; _key < _len; _key++) {
    args[_key] = arguments[_key];
  }

  return args;
}
/**
 * Calls the passed function.
 */

function call(proc) {
  proc();
}
/**
 * returns a function which when called always returns the passed value
 */

function always(value) {
  return function () {
    return value;
  };
}
/**
 * returns a function which calls all passed functions in the passed order.
 * joinProc does not pass arguments or collect return values.
 */

function joinProc() {
  for (var _len2 = arguments.length, procs = new Array(_len2), _key2 = 0; _key2 < _len2; _key2++) {
    procs[_key2] = arguments[_key2];
  }

  return function () {
    procs.map(call);
  };
}
function noop() {}

/**
 * urx Actions operate on streams - `publish` publishes data in a stream, and `subscribe` attaches a subscription to a stream.
 * @packageDocumentation
 */
/**
 * Subscribes the specified [[Subscription]] to the updates from the Emitter.
 * The emitter calls the subscription with the new data each time new data is published into it.
 *
 * ```ts
 * const foo = stream<number>();
 * subscribe(foo, (value) => console.log(value));
 * ```
 *
 * @returns an [[Unsubscribe]] handle  - calling it will unbind the subscription from the emitter.
 *```ts
 * const foo = stream<number>();
 * const unsub = subscribe(foo, (value) => console.log(value));
 * unsub();
 *```
 */

function subscribe(emitter, subscription) {
  return emitter(SUBSCRIBE, subscription);
}
/**
 * Publishes the value into the passed [[Publisher]].
 *
 * ```ts
 * const foo = stream<number>();
 * publish(foo, 42);
 * ```
 */

function publish(publisher, value) {
  publisher(PUBLISH, value);
}
/**
 * Clears all subscriptions from the [[Emitter]].
 * ```ts
 * const foo = stream<number>();
 * subscribe(foo, (value) => console.log(value));
 * reset(foo);
 * publish(foo, 42);
 * ```
 */

function reset(emitter) {
  emitter(RESET);
}
/**
 * Extracts the current value from a stateful stream. Use it only as an escape hatch, as it violates the concept of reactive programming.
 * ```ts
 * const foo = statefulStream(42);
 * console.log(getValue(foo));
 * ```
 */

function getValue(depot) {
  return depot(VALUE);
}
/**
 * Connects two streams - any value emitted from the emitter will be published in the publisher.
 * ```ts
 * const foo = stream<number>();
 * const bar = stream<number>();
 * subscribe(bar, (value) => console.log(`Bar emitted ${value}`));
 *
 * connect(foo, bar);
 * publish(foo);
 * ```
 * @returns an [[Unsubscribe]] handle which will disconnect the two streams.
 */

function connect(emitter, publisher) {
  return subscribe(emitter, curry2to1(publisher, PUBLISH));
}
/**
 * Executes the passed subscription at most once, for the next emit from the emitter.
 * ```ts
 * const foo = stream<number>()
 * handleNext(foo, value => console.log(value)) // called once, with 42
 * publish(foo, 42)
 * publish(foo, 43)
 * ```
 * @returns an [[Unsubscribe]] handle to unbind the subscription if necessary.
 */

function handleNext(emitter, subscription) {
  var unsub = emitter(SUBSCRIBE, function (value) {
    unsub();
    subscription(value);
  });
  return unsub;
}

/**
 * Streams are the basic building blocks of a reactive system. Think of them as the system permanent "data tubes".
 *
 * A stream acts as both an [[Emitter]] and [[Publisher]]. Each stream can have multiple {@link Subscription | Subscriptions}.
 *
 * urx streams are either **stateless** or **stateful**.
 * Stateless streams emit data to existing subscriptions when published, without keeping track of it.
 * Stateful streams remember the last published value and immediately publish it to new subscriptions.
 *
 * ```ts
 * import { stream, statefulStream, publish, subscribe } from "@virtuoso.dev/urx";
 *
 * // foo is a stateless stream
 * const foo = stream<number>();
 *
 * publish(foo, 42);
 * // this subsription will not be called...
 * subscribe(foo, (value) => console.log(value));
 * // it will only catch published values after it
 * publish(foo, 43);
 *
 * // stateful streams always start with an initial value
 * const bar = statefulStream(42);
 *
 * // subscribing to a stateful stream
 * // immediately calls the subscription with the current value
 * subscribe(bar, (value) => console.log(value));
 *
 * // subsequent publishing works just like stateless streams
 * publish(bar, 43);
 * ```
 * @packageDocumentation
 */
/**
 * Constructs a new stateless stream.
 * ```ts
 * const foo = stream<number>();
 * ```
 * @typeParam T the type of values to publish in the stream.
 * @returns a [[Stream]]
 */

function stream() {
  var subscriptions = [];
  return function (action, arg) {
    switch (action) {
      case RESET:
        subscriptions.splice(0, subscriptions.length);
        return;

      case SUBSCRIBE:
        subscriptions.push(arg);
        return function () {
          var indexOf = subscriptions.indexOf(arg);

          if (indexOf > -1) {
            subscriptions.splice(indexOf, 1);
          }
        };

      case PUBLISH:
        subscriptions.slice().forEach(function (subscription) {
          subscription(arg);
        });
        return;

      default:
        throw new Error("unrecognized action " + action);
    }
  };
}
/**
 * Constructs a new stateful stream.
 * ```ts
 * const foo = statefulStream(42);
 * ```
 * @param initial the initial value in the stream.
 * @typeParam T the type of values to publish in the stream. If omitted, the function infers it from the initial value.
 * @returns a [[StatefulStream]]
 */

function statefulStream(initial) {
  var value = initial;
  var innerSubject = stream();
  return function (action, arg) {
    switch (action) {
      case SUBSCRIBE:
        var subscription = arg;
        subscription(value);
        break;

      case PUBLISH:
        value = arg;
        break;

      case VALUE:
        return value;
    }

    return innerSubject(action, arg);
  };
}
/**
 * Event handlers are special emitters which can have **at most one active subscription**.
 * Subscribing to an event handler unsubscribes the previous subscription, if present.
 * ```ts
 * const foo = stream<number>();
 * const fooEvent = eventHandler(foo);
 *
 * // will be called once with 42
 * subscribe(fooEvent, (value) => console.log(`Sub 1 ${value}`));
 * publish(foo, 42);
 *
 * // unsubscribes sub 1
 * subscribe(fooEvent, (value) => console.log(`Sub 2 ${value}`));
 * publish(foo, 43);
 * ```
 * @param emitter the source emitter.
 * @returns the single-subscription emitter.
 */

function eventHandler(emitter) {
  var unsub;
  var currentSubscription;

  var cleanup = function cleanup() {
    return unsub && unsub();
  };

  return function (action, subscription) {
    switch (action) {
      case SUBSCRIBE:
        if (subscription) {
          if (currentSubscription === subscription) {
            return;
          }

          cleanup();
          currentSubscription = subscription;
          unsub = subscribe(emitter, subscription);
          return unsub;
        } else {
          cleanup();
          return noop;
        }

      case RESET:
        cleanup();
        currentSubscription = null;
        return;

      default:
        throw new Error("unrecognized action " + action);
    }
  };
}
/**
 * Creates and connects a "junction" stream to the specified emitter. Often used with [[pipe]], to avoid the multiple evaluation of operator sets.
 *
 * ```ts
 * const foo = stream<number>();
 *
 * const fooX2 = pipe(
 *   foo,
 *   map((value) => {
 *     console.log(`multiplying ${value}`);
 *     return value * 2;
 *   })
 * );
 *
 * subscribe(fooX2, (value) => console.log(value));
 * subscribe(fooX2, (value) => console.log(value));
 *
 * publish(foo, 42); // executes the map operator twice for each subscription.
 *
 * const sharedFooX2 = streamFromEmitter(pipe(
 *   foo,
 *   map((value) => {
 *     console.log(`shared multiplying ${value}`);
 *     return value * 2;
 *   })
 * ));
 *
 * subscribe(sharedFooX2, (value) => console.log(value));
 * subscribe(sharedFooX2, (value) => console.log(value));
 *
 * publish(foo, 42);
 *```
 * @returns the resulting stream.
 */

function streamFromEmitter(emitter) {
  return tap(stream(), function (stream) {
    return connect(emitter, stream);
  });
}
/**
 * Creates and connects a "junction" stateful stream to the specified emitter. Often used with [[pipe]], to avoid the multiple evaluation of operator sets.
 *
 * ```ts
 * const foo = stream<number>();
 *
 * const fooX2 = pipe(
 *   foo,
 *   map((value) => {
 *     console.log(`multiplying ${value}`);
 *     return value * 2;
 *   })
 * );
 *
 * subscribe(fooX2, (value) => console.log(value));
 * subscribe(fooX2, (value) => console.log(value));
 *
 * publish(foo, 42); // executes the map operator twice for each subscription.
 *
 * const sharedFooX2 = statefulStreamFromEmitter(pipe(
 *   foo,
 *   map((value) => {
 *     console.log(`shared multiplying ${value}`);
 *     return value * 2;
 *   })
 * ), 42);
 *
 * subscribe(sharedFooX2, (value) => console.log(value));
 * subscribe(sharedFooX2, (value) => console.log(value));
 *
 * publish(foo, 42);
 *```
 * @param initial the initial value in the stream.
 * @returns the resulting stateful stream.
 */

function statefulStreamFromEmitter(emitter, initial) {
  return tap(statefulStream(initial), function (stream) {
    return connect(emitter, stream);
  });
}

/**
 *
 * Stream values can be transformed and controlled by {@link pipe | **piping**} through **operators**.
 * urx includes several operators like [[map]], [[filter]], [[scan]], and [[throttleTime]].
 * The [[withLatestFrom]] operator allows the combination of values from other streams.
 *
 * ```ts
 * const foo = stream<number>()
 *
 * // create an emitter that first adds 2 to the passed value, then multiplies it by * 2
 * const bar = pipe(foo, map(value => value + 2), map(value => value * 2))
 * subscribe(bar, value => console.log(value))
 * publish(foo, 2) // outputs 8
 * ```
 *
 * ### Implementing Custom Operators
 * To implement your own operators, implement the [[Operator]] interface.
 * @packageDocumentation
 */
/** @internal */

function combineOperators() {
  for (var _len = arguments.length, operators = new Array(_len), _key = 0; _key < _len; _key++) {
    operators[_key] = arguments[_key];
  }

  return function (subscriber) {
    return operators.reduceRight(thrush, subscriber);
  };
}

function pipe(source) {
  for (var _len2 = arguments.length, operators = new Array(_len2 > 1 ? _len2 - 1 : 0), _key2 = 1; _key2 < _len2; _key2++) {
    operators[_key2 - 1] = arguments[_key2];
  }

  // prettier-ignore
  var project = combineOperators.apply(void 0, operators);
  return function (action, subscription) {
    switch (action) {
      case SUBSCRIBE:
        return subscribe(source, project(subscription));

      case RESET:
        reset(source);
        return;

      default:
        throw new Error("unrecognized action " + action);
    }
  };
}
/**
 * The default [[Comparator]] for [[distinctUntilChanged]] and [[duc]].
 */

function defaultComparator(previous, next) {
  return previous === next;
}
/**
 * Filters out identical values. Pass an optional [[Comparator]] if you need to filter non-primitive values.
 * ```ts
 * const foo = stream<number>()
 *
 * subscribe(
 *  pipe(foo, distinctUntilChanged()),
 *  console.log
 * ) // will be called only once
 *
 * publish(foo, 42)
 * publish(foo, 42)
 * ```
 */

function distinctUntilChanged(comparator) {
  if (comparator === void 0) {
    comparator = defaultComparator;
  }

  var current;
  return function (done) {
    return function (next) {
      if (!comparator(current, next)) {
        current = next;
        done(next);
      }
    };
  };
}
/**
 * Filters out values for which the predicator does not return `true`-ish.
 * ```ts
 * const foo = stream<number>()
 *
 * subscribe(
 *  pipe(foo, filter(value => value % 2 === 0)),
 *  console.log
 * ) // will be called only with even values
 *
 * publish(foo, 2)
 * publish(foo, 3)
 * publish(foo, 4)
 * publish(foo, 5)
 * ```
 */

function filter(predicate) {
  return function (done) {
    return function (value) {
      predicate(value) && done(value);
    };
  };
}
/**
 * Maps values using the provided project function.
 * ```ts
 * const foo = stream<number>()
 *
 * subscribe(
 *  pipe(foo, map(value => value * 2)),
 *  console.log
 * ) // 4, 6
 *
 * publish(foo, 2)
 * publish(foo, 3)
 * ```
 */

function map(project) {
  return function (done) {
    return compose(done, project);
  };
}
/**
 * Maps values to the hard-coded value.
 * ```ts
 * const foo = stream<number>()
 *
 * subscribe(
 *  pipe(foo, mapTo(3)),
 *  console.log
 * ) // 3, 3
 *
 * publish(foo, 1)
 * publish(foo, 2)
 * ```
 */

function mapTo(value) {
  return function (done) {
    return function () {
      return done(value);
    };
  };
}
/**
 * Works like Array#reduce.
 * Applies an accumulator function on the emitter, and outputs intermediate result. Starts with the initial value.
 * ```ts
 * const foo = stream<number>()
 *
 * subscribe(
 *  pipe(foo, scan((acc, value) => acc + value, 2),
 *  console.log
 * ) // 3, 5
 *
 * publish(foo, 1)
 * publish(foo, 2)
 * ```
 */

function scan(scanner, initial) {
  return function (done) {
    return function (value) {
      return done(initial = scanner(initial, value));
    };
  };
}
/**
 * Skips the specified amount of values from the emitter.
 * ```ts
 * const foo = stream<number>()
 *
 * subscribe(
 *  pipe(foo, skip(2)),
 *  console.log
 * ) // 3, 4
 *
 * publish(foo, 1) // skipped
 * publish(foo, 2) // skipped
 * publish(foo, 3)
 * publish(foo, 4)
 * ```
 */

function skip(times) {
  return function (done) {
    return function (value) {
      times > 0 ? times-- : done(value);
    };
  };
}
/**
 * Throttles flowing values at the provided interval in milliseconds.
 * [Throttle VS Debounce in SO](https://stackoverflow.com/questions/25991367/difference-between-throttling-and-debouncing-a-function).
 *
 * ```ts
 *  const foo = stream<number>()
 *  publish(foo, 1)
 *
 *  setTimeout(() => publish(foo, 2), 20)
 *  setTimeout(() => publish(foo, 3), 20)
 *
 *  subscribe(pipe(foo, throttleTime(50)), val => {
 *    console.log(value); // 3
 *  })
 * ```
 */

function throttleTime(interval) {
  var currentValue;
  var timeout;
  return function (done) {
    return function (value) {
      currentValue = value;

      if (timeout) {
        return;
      }

      timeout = setTimeout(function () {
        timeout = undefined;
        done(currentValue);
      }, interval);
    };
  };
}
/**
 * Debounces flowing values at the provided interval in milliseconds.
 * [Throttle VS Debounce in SO](https://stackoverflow.com/questions/25991367/difference-between-throttling-and-debouncing-a-function).
 *
 * ```ts
 *  const foo = stream<number>()
 *  publish(foo, 1)
 *
 *  setTimeout(() => publish(foo, 2), 20)
 *  setTimeout(() => publish(foo, 3), 20)
 *
 *  subscribe(pipe(foo, debounceTime(50)), val => {
 *    console.log(value); // 3
 *  })
 * ```
 */

function debounceTime(interval) {
  var currentValue;
  var timeout;
  return function (done) {
    return function (value) {
      currentValue = value;

      if (timeout) {
        clearTimeout(timeout);
      }

      timeout = setTimeout(function () {
        done(currentValue);
      }, interval);
    };
  };
}
function withLatestFrom() {
  for (var _len3 = arguments.length, sources = new Array(_len3), _key3 = 0; _key3 < _len3; _key3++) {
    sources[_key3] = arguments[_key3];
  }

  var values = new Array(sources.length);
  var called = 0;
  var pendingCall = null;
  var allCalled = Math.pow(2, sources.length) - 1;
  sources.forEach(function (source, index) {
    var bit = Math.pow(2, index);
    subscribe(source, function (value) {
      var prevCalled = called;
      called = called | bit;
      values[index] = value;

      if (prevCalled !== allCalled && called === allCalled && pendingCall) {
        pendingCall();
        pendingCall = null;
      }
    });
  });
  return function (done) {
    return function (value) {
      var call = function call() {
        return done([value].concat(values));
      };

      if (called === allCalled) {
        call();
      } else {
        pendingCall = call;
      }
    };
  };
}

/**
 * Transformers change and combine streams, similar to operators.
 * urx comes with two combinators - [[combineLatest]] and [[merge]], and one convenience filter - [[duc]].
 *
 * @packageDocumentation
 */
/**
 * Merges one or more emitters from the same type into a new Emitter which emits values from any of the source emitters.
 * ```ts
 * const foo = stream<number>()
 * const bar = stream<number>()
 *
 * subscribe(merge(foo, bar), (value) => console.log(value)) // 42, 43
 *
 * publish(foo, 42)
 * publish(bar, 43)
 * ```
 */

function merge() {
  for (var _len = arguments.length, sources = new Array(_len), _key = 0; _key < _len; _key++) {
    sources[_key] = arguments[_key];
  }

  return function (action, subscription) {
    switch (action) {
      case SUBSCRIBE:
        return joinProc.apply(void 0, sources.map(function (source) {
          return subscribe(source, subscription);
        }));

      case RESET:
        // do nothing, we are stateless
        return;

      default:
        throw new Error("unrecognized action " + action);
    }
  };
}
/**
 * A convenience wrapper that emits only the distinct values from the passed Emitter. Wraps [[pipe]] and [[distinctUntilChanged]].
 *
 * ```ts
 * const foo = stream<number>()
 *
 * // this line...
 * const a = duc(foo)
 *
 * // is equivalent to this
 * const b = pipe(distinctUntilChanged(foo))
 * ```
 *
 * @param source The source emitter.
 * @param comparator optional custom comparison function for the two values.
 *
 * @typeParam T the type of the value emitted by the source.
 *
 * @returns the resulting emitter.
 */

function duc(source, comparator) {
  if (comparator === void 0) {
    comparator = defaultComparator;
  }

  return pipe(source, distinctUntilChanged(comparator));
}
function combineLatest() {
  var innerSubject = stream();

  for (var _len2 = arguments.length, emitters = new Array(_len2), _key2 = 0; _key2 < _len2; _key2++) {
    emitters[_key2] = arguments[_key2];
  }

  var values = new Array(emitters.length);
  var called = 0;
  var allCalled = Math.pow(2, emitters.length) - 1;
  emitters.forEach(function (source, index) {
    var bit = Math.pow(2, index);
    subscribe(source, function (value) {
      values[index] = value;
      called = called | bit;

      if (called === allCalled) {
        publish(innerSubject, values);
      }
    });
  });
  return function (action, subscription) {
    switch (action) {
      case SUBSCRIBE:
        if (called === allCalled) {
          subscription(values);
        }

        return subscribe(innerSubject, subscription);

      case RESET:
        return reset(innerSubject);

      default:
        throw new Error("unrecognized action " + action);
    }
  };
}

/**
 * `system` defines a specification of a system - its constructor, dependencies and if it should act as a singleton in a system dependency tree.
 * When called, system returns a [[SystemSpec]], which is then initialized along with its dependencies by passing it to [[init]].
 *
 * ```ts
 * @import { subscribe, publish, system, init, tup, connect, map, pipe } from 'urx'
 *
 * // a simple system with two streams
 * const sys1 = system(() => {
 *  const a = stream<number>()
 *  const b = stream<number>()
 *
 *  connect(pipe(a, map(value => value * 2)), b)
 *  return { a, b }
 * })
 *
 * // a second system which depends on the streams from the first one
 * const sys2 = system(([ {a, b} ]) => {
 *  const c = stream<number>()
 *  connect(pipe(b, map(value => value * 2)), c)
 *  // re-export the `a` stream, keep `b` internal
 *  return { a, c }
 * }, tup(sys1))
 *
 * // init will recursively initialize sys2 dependencies, in this case sys1
 * const { a, c } = init(sys2)
 * subscribe(c, c => console.log(`Value multiplied by 4`, c))
 * publish(a, 2)
 * ```
 *
 * #### Singletons in Dependency Tree
 *
 * By default, systems will be initialized only once if encountered multiple times in the dependency tree.
 * In the below dependency system tree, systems `b` and `c` will receive the same stream instances from system `a` when system `d` is initialized.
 * ```txt
 *   a
 *  / \
 * b   c
 *  \ /
 *   d
 * ```
 * If `a` gets `{singleton: false}` as a last argument, `init` creates two separate instances - one for `b` and one for `c`.
 *
 * @param constructor the system constructor function. Initialize and connect the streams in its body.
 *
 * @param dependencies the system dependencies, which the constructor will receive as arguments.
 * Use the [[tup]] utility **For TypeScript type inference to work correctly**.
 * ```ts
 * const sys3 = system(() => { ... }, tup(sys2, sys1))
 * ```
 * @param __namedParameters Options
 * @param singleton determines if the system will act as a singleton in a system dependency tree. `true` by default.
 */
function system(constructor, dependencies, _temp) {
  if (dependencies === void 0) {
    dependencies = [];
  }

  var _ref = _temp === void 0 ? {
    singleton: true
  } : _temp,
      singleton = _ref.singleton;

  return {
    id: id(),
    constructor: constructor,
    dependencies: dependencies,
    singleton: singleton
  };
}
/** @internal */

var id = function id() {
  return Symbol();
};
/**
 * Initializes a [[SystemSpec]] by recursively initializing its dependencies.
 *
 * ```ts
 * // a simple system with two streams
 * const sys1 = system(() => {
 *  const a = stream<number>()
 *  const b = stream<number>()
 *
 *  connect(pipe(a, map(value => value * 2)), b)
 *  return { a, b }
 * })
 *
 * const { a, b } = init(sys1)
 * subscribe(b, b => console.log(b))
 * publish(a, 2)
 * ```
 *
 * @returns the [[System]] constructed by the spec constructor.
 * @param systemSpec the system spec to initialize.
 */


function init(systemSpec) {
  var singletons = new Map();

  var _init = function _init(_ref2) {
    var id = _ref2.id,
        constructor = _ref2.constructor,
        dependencies = _ref2.dependencies,
        singleton = _ref2.singleton;

    if (singleton && singletons.has(id)) {
      return singletons.get(id);
    }

    var system = constructor(dependencies.map(function (e) {
      return _init(e);
    }));

    if (singleton) {
      singletons.set(id, system);
    }

    return system;
  };

  return _init(systemSpec);
}

function _objectWithoutPropertiesLoose(source, excluded) {
  if (source == null) return {};
  var target = {};
  var sourceKeys = Object.keys(source);
  var key, i;

  for (i = 0; i < sourceKeys.length; i++) {
    key = sourceKeys[i];
    if (excluded.indexOf(key) >= 0) continue;
    target[key] = source[key];
  }

  return target;
}

function _unsupportedIterableToArray(o, minLen) {
  if (!o) return;
  if (typeof o === "string") return _arrayLikeToArray(o, minLen);
  var n = Object.prototype.toString.call(o).slice(8, -1);
  if (n === "Object" && o.constructor) n = o.constructor.name;
  if (n === "Map" || n === "Set") return Array.from(o);
  if (n === "Arguments" || /^(?:Ui|I)nt(?:8|16|32)(?:Clamped)?Array$/.test(n)) return _arrayLikeToArray(o, minLen);
}

function _arrayLikeToArray(arr, len) {
  if (len == null || len > arr.length) len = arr.length;

  for (var i = 0, arr2 = new Array(len); i < len; i++) arr2[i] = arr[i];

  return arr2;
}

function _createForOfIteratorHelperLoose(o, allowArrayLike) {
  var it = typeof Symbol !== "undefined" && o[Symbol.iterator] || o["@@iterator"];
  if (it) return (it = it.call(o)).next.bind(it);

  if (Array.isArray(o) || (it = _unsupportedIterableToArray(o)) || allowArrayLike && o && typeof o.length === "number") {
    if (it) o = it;
    var i = 0;
    return function () {
      if (i >= o.length) return {
        done: true
      };
      return {
        done: false,
        value: o[i++]
      };
    };
  }

  throw new TypeError("Invalid attempt to iterate non-iterable instance.\nIn order to be iterable, non-array objects must have a [Symbol.iterator]() method.");
}

var _excluded = ["children"];
/** @internal */

function omit(keys, obj) {
  var result = {};
  var index = {};
  var idx = 0;
  var len = keys.length;

  while (idx < len) {
    index[keys[idx]] = 1;
    idx += 1;
  }

  for (var prop in obj) {
    if (!index.hasOwnProperty(prop)) {
      result[prop] = obj[prop];
    }
  }

  return result;
}

var useIsomorphicLayoutEffect = typeof document !== 'undefined' ? react.useLayoutEffect : react.useEffect;
/**
 * Converts a system spec to React component by mapping the system streams to component properties, events and methods. Returns hooks for querying and modifying
 * the system streams from the component's child components.
 * @param systemSpec The return value from a [[system]] call.
 * @param map The streams to props / events / methods mapping Check [[SystemPropsMap]] for more details.
 * @param Root The optional React component to render. By default, the resulting component renders nothing, acting as a logical wrapper for its children.
 * @returns an object containing the following:
 *  - `Component`: the React component.
 *  - `useEmitterValue`: a hook that lets child components use values emitted from the specified output stream.
 *  - `useEmitter`: a hook that calls the provided callback whenever the specified stream emits a value.
 *  - `usePublisher`: a hook which lets child components publish values to the specified stream.
 *  <hr />
 */

function systemToComponent(systemSpec, map, Root) {
  var requiredPropNames = Object.keys(map.required || {});
  var optionalPropNames = Object.keys(map.optional || {});
  var methodNames = Object.keys(map.methods || {});
  var eventNames = Object.keys(map.events || {});
  var Context = react.createContext({});

  function applyPropsToSystem(system, props) {
    if (system['propsReady']) {
      publish(system['propsReady'], false);
    }

    for (var _iterator = _createForOfIteratorHelperLoose(requiredPropNames), _step; !(_step = _iterator()).done;) {
      var requiredPropName = _step.value;
      var stream = system[map.required[requiredPropName]];
      publish(stream, props[requiredPropName]);
    }

    for (var _iterator2 = _createForOfIteratorHelperLoose(optionalPropNames), _step2; !(_step2 = _iterator2()).done;) {
      var optionalPropName = _step2.value;

      if (optionalPropName in props) {
        var _stream = system[map.optional[optionalPropName]];
        publish(_stream, props[optionalPropName]);
      }
    }

    if (system['propsReady']) {
      publish(system['propsReady'], true);
    }
  }

  function buildMethods(system) {
    return methodNames.reduce(function (acc, methodName) {

      acc[methodName] = function (value) {
        var stream = system[map.methods[methodName]];
        publish(stream, value);
      };

      return acc;
    }, {});
  }

  function buildEventHandlers(system) {
    return eventNames.reduce(function (handlers, eventName) {
      handlers[eventName] = eventHandler(system[map.events[eventName]]);
      return handlers;
    }, {});
  }
  /**
   * A React component generated from an urx system
   */


  var Component = react.forwardRef(function (propsWithChildren, ref) {
    var children = propsWithChildren.children,
        props = _objectWithoutPropertiesLoose(propsWithChildren, _excluded);

    var _useState = react.useState(function () {
      return tap(init(systemSpec), function (system) {
        return applyPropsToSystem(system, props);
      });
    }),
        system = _useState[0];

    var _useState2 = react.useState(curry1to0(buildEventHandlers, system)),
        handlers = _useState2[0];

    useIsomorphicLayoutEffect(function () {
      for (var _iterator3 = _createForOfIteratorHelperLoose(eventNames), _step3; !(_step3 = _iterator3()).done;) {
        var eventName = _step3.value;

        if (eventName in props) {
          subscribe(handlers[eventName], props[eventName]);
        }
      }

      return function () {
        Object.values(handlers).map(reset);
      };
    }, [props, handlers, system]);
    useIsomorphicLayoutEffect(function () {
      applyPropsToSystem(system, props);
    });
    react.useImperativeHandle(ref, always(buildMethods(system)));
    return react.createElement(Context.Provider, {
      value: system
    }, Root ? react.createElement(Root, omit([].concat(requiredPropNames, optionalPropNames, eventNames), props), children) : children);
  });

  var usePublisher = function usePublisher(key) {
    return react.useCallback(curry2to1(publish, react.useContext(Context)[key]), [key]);
  };
  /**
   * Returns the value emitted from the stream.
   */


  var useEmitterValue = function useEmitterValue(key) {
    var context = react.useContext(Context);
    var source = context[key];

    var _useState3 = react.useState(curry1to0(getValue, source)),
        value = _useState3[0],
        setValue = _useState3[1];

    useIsomorphicLayoutEffect(function () {
      return subscribe(source, function (next) {
        if (next !== value) {
          setValue(always(next));
        }
      });
    }, [source, value]);
    return value;
  };

  var useEmitter = function useEmitter(key, callback) {
    var context = react.useContext(Context);
    var source = context[key];
    useIsomorphicLayoutEffect(function () {
      return subscribe(source, callback);
    }, [callback, source]);
  };

  return {
    Component: Component,
    usePublisher: usePublisher,
    useEmitterValue: useEmitterValue,
    useEmitter: useEmitter
  };
}

function P(){return (P=Object.assign||function(e){for(var t=1;t<arguments.length;t++){var n=arguments[t];for(var o in n)Object.prototype.hasOwnProperty.call(n,o)&&(e[o]=n[o]);}return e}).apply(this,arguments)}function A(e,t){if(null==e)return {};var n,o,r={},i=Object.keys(e);for(o=0;o<i.length;o++)t.indexOf(n=i[o])>=0||(r[n]=e[n]);return r}function V(e,t){(null==t||t>e.length)&&(t=e.length);for(var n=0,o=new Array(t);n<t;n++)o[n]=e[n];return o}function W(e,t){var n;if("undefined"==typeof Symbol||null==e[Symbol.iterator]){if(Array.isArray(e)||(n=function(e,t){if(e){if("string"==typeof e)return V(e,t);var n=Object.prototype.toString.call(e).slice(8,-1);return "Object"===n&&e.constructor&&(n=e.constructor.name),"Map"===n||"Set"===n?Array.from(e):"Arguments"===n||/^(?:Ui|I)nt(?:8|16|32)(?:Clamped)?Array$/.test(n)?V(e,t):void 0}}(e))||t&&e&&"number"==typeof e.length){n&&(e=n);var o=0;return function(){return o>=e.length?{done:!0}:{done:!1,value:e[o++]}}}throw new TypeError("Invalid attempt to iterate non-iterable instance.\nIn order to be iterable, non-array objects must have a [Symbol.iterator]() method.")}return (n=e[Symbol.iterator]()).next.bind(n)}function D(e,t){return !(!e||e[0]!==t[0]||e[1]!==t[1])}function F(e,t){return !(!e||e.startIndex!==t.startIndex||e.endIndex!==t.endIndex)}var G,N,U=system(function(){var e=stream(),t=stream(),a=statefulStream(0),u=stream(),s=statefulStream(0),c=stream(),d=stream(),f=statefulStream(0),m=statefulStream(0),h=stream(),p=stream(),g=statefulStream(!1);return connect(pipe(e,map(function(e){return e[0]})),t),connect(pipe(e,map(function(e){return e[1]})),d),connect(t,s),{scrollContainerState:e,scrollTop:t,viewportHeight:c,headerHeight:f,footerHeight:m,scrollHeight:d,smoothScrollTargetReached:u,scrollTo:h,scrollBy:p,statefulScrollTop:s,deviation:a,scrollingInProgress:g}},[],{singleton:!0});!function(e){e[e.DEBUG=0]="DEBUG",e[e.INFO=1]="INFO",e[e.WARN=2]="WARN",e[e.ERROR=3]="ERROR";}(N||(N={}));var _=((G={})[N.DEBUG]="debug",G[N.INFO]="log",G[N.WARN]="warn",G[N.ERROR]="error",G),j=system(function(){var e=statefulStream(N.ERROR);return {log:statefulStream(function(t,n,o){var r;void 0===o&&(o=N.INFO),o>=(null!=(r=("undefined"==typeof globalThis?window:globalThis).VIRTUOSO_LOG_LEVEL)?r:getValue(e))&&console[_[o]]("%creact-virtuoso: %c%s %o","color: #0253b3; font-weight: bold","color: initial",t,n);}),logLevel:e}},[],{singleton:!0}),K=system(function(e){var t=e[0].log,n=statefulStream(!1),r=streamFromEmitter(pipe(n,filter(function(e){return e}),distinctUntilChanged()));return subscribe(n,function(e){e&&getValue(t)("props updated",{},N.DEBUG);}),{propsReady:n,didMount:r}},tup(j),{singleton:!0}),Y="up",Z={atBottom:!1,notAtBottomBecause:"NOT_SHOWING_LAST_ITEM",state:{offsetBottom:0,scrollTop:0,viewportHeight:0,scrollHeight:0}},q=system(function(e){var t=e[0],f=t.scrollContainerState,y=t.scrollTop,b=t.viewportHeight,H=t.headerHeight,R=t.footerHeight,z=t.scrollBy,k=statefulStream(!1),B=statefulStream(!0),E=stream(),L=stream(),O=statefulStream(4),M=streamFromEmitter(pipe(merge(pipe(duc(y),skip(1),mapTo(!0)),pipe(duc(y),skip(1),mapTo(!1),debounceTime(100))),distinctUntilChanged())),P=statefulStreamFromEmitter(pipe(merge(pipe(z,mapTo(!0)),pipe(z,mapTo(!1),debounceTime(200))),distinctUntilChanged()),!1);connect(pipe(duc(y),map(function(e){return 0===e}),distinctUntilChanged()),B),connect(B,L);var A=streamFromEmitter(pipe(combineLatest(f,duc(b),duc(H),duc(R),duc(O)),scan(function(e,t){var n,o,r=t[0],i=r[0],l=r[1],a=t[1],u={viewportHeight:a,scrollTop:i,scrollHeight:l};return i+a-l>-t[4]?(i>e.state.scrollTop?(n="SCROLLED_DOWN",o=e.state.scrollTop-i):(n="SIZE_DECREASED",o=e.state.scrollTop-i||e.scrollTopDelta),{atBottom:!0,state:u,atBottomBecause:n,scrollTopDelta:o}):{atBottom:!1,notAtBottomBecause:u.scrollHeight>e.state.scrollHeight?"SIZE_INCREASED":a<e.state.viewportHeight?"VIEWPORT_HEIGHT_DECREASING":i<e.state.scrollTop?"SCROLLING_UPWARDS":"NOT_FULLY_SCROLLED_TO_LAST_ITEM_BOTTOM",state:u}},Z),distinctUntilChanged(function(e,t){return e&&e.atBottom===t.atBottom}))),V=statefulStreamFromEmitter(pipe(f,scan(function(e,t){var n=t[0],o=t[1];return e.scrollHeight!==o?e.scrollTop!==n?{scrollHeight:o,scrollTop:n,jump:e.scrollTop-n,changed:!0}:{scrollHeight:o,scrollTop:n,jump:0,changed:!0}:{scrollTop:n,scrollHeight:o,jump:0,changed:!1}},{scrollHeight:0,jump:0,scrollTop:0,changed:!1}),filter(function(e){return e.changed}),map(function(e){return e.jump})),0);connect(pipe(A,map(function(e){return e.atBottom})),k),subscribe(k,function(e){setTimeout(function(){return publish(E,e)});});var W=statefulStream("down");subscribe(k,function(e){setTimeout(function(){publish(E,e);});}),connect(pipe(f,map(function(e){return e[0]}),distinctUntilChanged(),scan(function(e,t){return getValue(P)?{direction:e.direction,prevScrollTop:t}:{direction:t<e.prevScrollTop?Y:"down",prevScrollTop:t}},{direction:"down",prevScrollTop:0}),map(function(e){return e.direction})),W),connect(pipe(f,throttleTime(50),mapTo("none")),W),connect(k,E);var D=statefulStream(0);return connect(pipe(M,filter(function(e){return !e}),mapTo(0)),D),connect(pipe(y,throttleTime(100),withLatestFrom(M),filter(function(e){return !!e[1]}),scan(function(e,t){return [e[1],t[0]]},[0,0]),map(function(e){return e[1]-e[0]})),D),{isScrolling:M,isAtTop:B,isAtBottom:k,atBottomState:A,atTopStateChange:L,atBottomStateChange:E,scrollDirection:W,atBottomThreshold:O,scrollVelocity:D,lastJumpDueToItemResize:V}},tup(U)),J=system(function(e){var t=e[0].scrollVelocity,a=statefulStream(!1),u=stream(),f=statefulStream(!1);return connect(pipe(t,withLatestFrom(f,a,u),filter(function(e){return !!e[1]}),map(function(e){var t=e[0],n=e[1],o=e[2],r=e[3],i=n.enter;if(o){if((0, n.exit)(t,r))return !1}else if(i(t,r))return !0;return o}),distinctUntilChanged()),a),subscribe(pipe(combineLatest(a,t,u),withLatestFrom(f)),function(e){var t=e[0],n=e[1];return t[0]&&n&&n.change&&n.change(t[1],t[2])}),{isSeeking:a,scrollSeekConfiguration:f,scrollVelocity:t,scrollSeekRangeChanged:u}},tup(q),{singleton:!0}),$={lvl:0};function Q(e,t,n,o,r){return void 0===o&&(o=$),void 0===r&&(r=$),{k:e,v:t,lvl:n,l:o,r:r}}function X(e){return e===$}function ee(){return $}function te(e,t){if(X(e))return $;var n=e.k,o=e.l,r=e.r;if(t===n){if(X(o))return r;if(X(r))return o;var i=le(o);return ce(ue(e,{k:i[0],v:i[1],l:ae(o)}))}return ce(ue(e,t<n?{l:te(o,t)}:{r:te(r,t)}))}function ne(e,t,n){if(void 0===n&&(n="k"),X(e))return [-Infinity,void 0];if(e[n]===t)return [e.k,e.v];if(e[n]<t){var o=ne(e.r,t,n);return -Infinity===o[0]?[e.k,e.v]:o}return ne(e.l,t,n)}function oe(e,t,n){return X(e)?Q(t,n,1):t===e.k?ue(e,{k:t,v:n}):function(e){return me(he(e))}(ue(e,t<e.k?{l:oe(e.l,t,n)}:{r:oe(e.r,t,n)}))}function re(e,t,n){if(X(e))return [];var o=e.k,r=e.v,i=e.r,l=[];return o>t&&(l=l.concat(re(e.l,t,n))),o>=t&&o<=n&&l.push({k:o,v:r}),o<=n&&(l=l.concat(re(i,t,n))),l}function ie(e){return X(e)?[]:[].concat(ie(e.l),[{k:e.k,v:e.v}],ie(e.r))}function le(e){return X(e.r)?[e.k,e.v]:le(e.r)}function ae(e){return X(e.r)?e.l:ce(ue(e,{r:ae(e.r)}))}function ue(e,t){return Q(void 0!==t.k?t.k:e.k,void 0!==t.v?t.v:e.v,void 0!==t.lvl?t.lvl:e.lvl,void 0!==t.l?t.l:e.l,void 0!==t.r?t.r:e.r)}function se(e){return X(e)||e.lvl>e.r.lvl}function ce(e){var t=e.l,n=e.r,o=e.lvl;if(n.lvl>=o-1&&t.lvl>=o-1)return e;if(o>n.lvl+1){if(se(t))return he(ue(e,{lvl:o-1}));if(X(t)||X(t.r))throw new Error("Unexpected empty nodes");return ue(t.r,{l:ue(t,{r:t.r.l}),r:ue(e,{l:t.r.r,lvl:o-1}),lvl:o})}if(se(e))return me(ue(e,{lvl:o-1}));if(X(n)||X(n.l))throw new Error("Unexpected empty nodes");var r=n.l,i=se(r)?n.lvl-1:n.lvl;return ue(r,{l:ue(e,{r:r.l,lvl:o-1}),r:me(ue(n,{l:r.r,lvl:i})),lvl:r.lvl+1})}function de(e,t,n){return X(e)?[]:fe(re(e,ne(e,t)[0],n),function(e){return {index:e.k,value:e.v}})}function fe(e,t){var n=e.length;if(0===n)return [];for(var o=t(e[0]),r=o.index,i=o.value,l=[],a=1;a<n;a++){var u=t(e[a]),s=u.index,c=u.value;l.push({start:r,end:s-1,value:i}),r=s,i=c;}return l.push({start:r,end:Infinity,value:i}),l}function me(e){var t=e.r,n=e.lvl;return X(t)||X(t.r)||t.lvl!==n||t.r.lvl!==n?e:ue(t,{l:ue(e,{r:t.l}),lvl:n+1})}function he(e){var t=e.l;return X(t)||t.lvl!==e.lvl?e:ue(t,{r:ue(e,{l:t.r})})}function pe(e,t,n,o){void 0===o&&(o=0);for(var r=e.length-1;o<=r;){var i=Math.floor((o+r)/2),l=n(e[i],t);if(0===l)return i;if(-1===l){if(r-o<2)return i-1;r=i-1;}else {if(r===o)return i;o=i+1;}}throw new Error("Failed binary finding record in array - "+e.join(",")+", searched for "+t)}function ge(e,t,n){return e[pe(e,t,n)]}function ve(e,t){return Math.round(e.getBoundingClientRect()[t])}function Ie(e){var t=e.size,n=e.startIndex,o=e.endIndex;return function(e){return e.start===n&&(e.end===o||Infinity===e.end)&&e.value===t}}function Ce(e,t){var n=e.index;return t===n?0:t<n?-1:1}function Se(e,t){var n=e.offset;return t===n?0:t<n?-1:1}function Te(e){return {index:e.index,value:e}}function xe(e,t){var n=t[0],o=t[1];n.length>0&&(0, t[2])("received item sizes",n,N.DEBUG);var r=e.sizeTree,i=e.offsetTree,l=r,a=0;if(o.length>0&&X(r)&&2===n.length){var u=n[0].size,s=n[1].size;l=o.reduce(function(e,t){return oe(oe(e,t,u),t+1,s)},l);}else {var c=function(e,t){for(var n,o=X(e)?0:Infinity,r=W(t);!(n=r()).done;){var i=n.value,l=i.size,a=i.startIndex,u=i.endIndex;if(o=Math.min(o,a),X(e))e=oe(e,0,l);else {var s=de(e,a-1,u+1);if(!s.some(Ie(i))){for(var c,d=!1,f=!1,m=W(s);!(c=m()).done;){var h=c.value,p=h.start,g=h.end,v=h.value;d?(u>=p||l===v)&&(e=te(e,p)):(f=v!==l,d=!0),g>u&&u>=p&&v!==l&&(e=oe(e,u+1,v));}f&&(e=oe(e,a,l));}}}return [e,o]}(l,n);l=c[0],a=c[1];}if(l===r)return e;var d=0,f=0,m=0,h=0;if(0!==a){h=pe(i,a-1,Ce),m=i[h].offset;var p=ne(l,a-1);d=p[0],f=p[1],i.length&&i[h].size===ne(l,a)[1]&&(h-=1),i=i.slice(0,h+1);}else i=[];for(var g,v=W(de(l,a,Infinity));!(g=v()).done;){var I=g.value,C=I.start,S=I.value,T=(C-d)*f+m;i.push({offset:T,size:S,index:C}),d=C,m=T,f=S;}return {sizeTree:l,offsetTree:i,groupOffsetTree:o.reduce(function(e,t){return oe(e,t,we(t,i))},ee()),lastIndex:d,lastOffset:m,lastSize:f,groupIndices:o}}function we(e,t){if(0===t.length)return 0;var n=ge(t,e,Ce);return n.size*(e-n.index)+n.offset}function ye(e,t){if(!be(t))return e;for(var n=0;t.groupIndices[n]<=e+n;)n++;return e+n}function be(e){return !X(e.groupOffsetTree)}var He={offsetHeight:"height",offsetWidth:"width"},Re=system(function(e){var t=e[0].log,f=stream(),m=stream(),h=stream(),p=statefulStream(0),g=statefulStream([]),v=statefulStream(void 0),C=statefulStream(void 0),T=statefulStream(function(e,t){return ve(e,He[t])}),x=statefulStream(void 0),y={offsetTree:[],sizeTree:ee(),groupOffsetTree:ee(),lastIndex:0,lastOffset:0,lastSize:0,groupIndices:[]},b=statefulStreamFromEmitter(pipe(f,withLatestFrom(g,t),scan(xe,y),distinctUntilChanged()),y);connect(pipe(g,filter(function(e){return e.length>0}),withLatestFrom(b),map(function(e){var t=e[0],n=e[1],o=t.reduce(function(e,t,o){return oe(e,t,we(t,n.offsetTree)||o)},ee());return P({},n,{groupIndices:t,groupOffsetTree:o})})),b),connect(pipe(m,withLatestFrom(b),filter(function(e){return e[0]<e[1].lastIndex}),map(function(e){var t=e[1];return [{startIndex:e[0],endIndex:t.lastIndex,size:t.lastSize}]})),f),connect(v,C);var H=statefulStreamFromEmitter(pipe(v,map(function(e){return void 0===e})),!0);connect(pipe(C,filter(function(e){return void 0!==e&&X(getValue(b).sizeTree)}),map(function(e){return [{startIndex:0,endIndex:0,size:e}]})),f);var R=streamFromEmitter(pipe(f,withLatestFrom(b),scan(function(e,t){var n=t[1];return {changed:n!==e.sizes,sizes:n}},{changed:!1,sizes:y}),map(function(e){return e.changed})));connect(pipe(p,scan(function(e,t){return {diff:e.prev-t,prev:t}},{diff:0,prev:0}),map(function(e){return e.diff}),filter(function(e){return e>0})),h),subscribe(pipe(p,withLatestFrom(t)),function(e){e[0]<0&&(0, e[1])("`firstItemIndex` prop should not be set to less than zero. If you don't know the total count, just use a very high value",{firstItemIndex:p},N.ERROR);});var z=streamFromEmitter(h);return connect(pipe(h,withLatestFrom(b),map(function(e){var t=e[0],n=e[1];if(n.groupIndices.length>0)throw new Error("Virtuoso: prepending items does not work with groups");return ie(n.sizeTree).reduce(function(e,n){var o=n.k,r=n.v;return {ranges:[].concat(e.ranges,[{startIndex:e.prevIndex,endIndex:o+t-1,size:e.prevSize}]),prevIndex:o+t,prevSize:r}},{ranges:[],prevIndex:0,prevSize:n.lastSize}).ranges})),f),{data:x,totalCount:m,sizeRanges:f,groupIndices:g,defaultItemSize:C,fixedItemSize:v,unshiftWith:h,beforeUnshiftWith:z,firstItemIndex:p,sizes:b,listRefresh:R,trackItemSizes:H,itemSize:T}},tup(j),{singleton:!0}),ze="undefined"!=typeof document&&"scrollBehavior"in document.documentElement.style;function ke(e){var t="number"==typeof e?{index:e}:e;return t.align||(t.align="start"),t.behavior&&ze||(t.behavior="auto"),t.offset||(t.offset=0),t}var Be=system(function(e){var t=e[0],a=t.sizes,u=t.totalCount,s=t.listRefresh,c=e[1],f=c.scrollingInProgress,m=c.viewportHeight,h=c.scrollTo,p=c.smoothScrollTargetReached,g=c.headerHeight,v=c.footerHeight,I=e[2].log,C=stream(),S=statefulStream(0),x=null,b=null,H=null;function R(){x&&(x(),x=null),H&&(H(),H=null),b&&(clearTimeout(b),b=null),publish(f,!1);}return connect(pipe(C,withLatestFrom(a,m,u,S,g,v,I),map(function(e){var t=e[0],n=e[1],o=e[2],r=e[3],l=e[4],a=e[5],u=e[6],c=e[7],m=ke(t),h=m.align,g=m.behavior,v=m.offset,I=r-1,S=m.index;"LAST"===S&&(S=I),S=ye(S,n);var w=we(S=Math.max(0,S,Math.min(I,S)),n.offsetTree)+a;"end"===h?(w=w-o+ne(n.sizeTree,S)[1],S===I&&(w+=u)):"center"===h?w=w-o/2+ne(n.sizeTree,S)[1]/2:w-=l,v&&(w+=v);var z=function(e){R(),e?(c("retrying to scroll to",{location:t},N.DEBUG),publish(C,t)):c("list did not change, scroll successful",{},N.DEBUG);};if(R(),"smooth"===g){var k=!1;H=subscribe(s,function(e){k=k||e;}),x=handleNext(p,function(){z(k);});}else x=handleNext(pipe(s,function(e){var t=setTimeout(function(){e(!1);},50);return function(n){n&&(e(!0),clearTimeout(t));}}),z);return b=setTimeout(function(){R();},1200),publish(f,!0),c("scrolling from index to",{index:S,top:w,behavior:g},N.DEBUG),{top:w,behavior:g}})),h),{scrollToIndex:C,topListHeight:S}},tup(Re,U,j),{singleton:!0});function Ee(e,t,n){return "number"==typeof e?n===Y&&"top"===t||"down"===n&&"bottom"===t?e:0:n===Y?"top"===t?e.main:e.reverse:"bottom"===t?e.main:e.reverse}function Le(e,t){return "number"==typeof e?e:e[t]||0}var Oe=system(function(e){var t=e[0],r=t.scrollTop,a=t.viewportHeight,u=t.deviation,d=t.headerHeight,f=stream(),m=statefulStream(0),p=statefulStream(0),g=statefulStream(0),v=statefulStream(0);return {listBoundary:f,overscan:v,topListHeight:m,fixedHeaderHeight:p,increaseViewportBy:g,visibleRange:statefulStreamFromEmitter(pipe(combineLatest(duc(r),duc(a),duc(d),duc(f,D),duc(v),duc(m),duc(p),duc(u),duc(g)),map(function(e){var t=e[0],n=e[1],o=e[2],r=e[3],i=r[0],l=r[1],a=e[4],u=e[6],s=e[7],c=e[8],d=t-s,f=e[5]+u,m=Math.max(o-d,0),h="none",p=Le(c,"top"),g=Le(c,"bottom");return i-=s,l+=o,(i+=o)>t+f-p&&(h=Y),(l-=s)<t-m+n+g&&(h="down"),"none"!==h?[Math.max(d-o-Ee(a,"top",h)-p,0),d-m-u+n+Ee(a,"bottom",h)+g]:null}),filter(function(e){return null!=e}),distinctUntilChanged(D)),[0,0])}},tup(U),{singleton:!0}),Me=system(function(e){var t=e[0],a=t.scrollTo,u=t.scrollContainerState,s=stream(),c=stream(),d=stream(),f=statefulStream(!1);return connect(pipe(combineLatest(s,c),map(function(e){var t=e[0],n=t[1];return [Math.max(0,t[0]-e[1].offsetTop),n]})),u),connect(pipe(a,withLatestFrom(c),map(function(e){var t=e[0];return P({},t,{top:t.top+e[1].offsetTop})})),d),{useWindowScroll:f,windowScrollContainerState:s,windowViewportRect:c,windowScrollTo:d}},tup(U)),Pe={items:[],offsetBottom:0,offsetTop:0,top:0,bottom:0,itemHeight:0,itemWidth:0},Ae={items:[{index:0}],offsetBottom:0,offsetTop:0,top:0,bottom:0,itemHeight:0,itemWidth:0},Ve=Math.ceil,We=Math.floor,De=Math.min,Fe=Math.max;function Ge(e){return Ve(e)-e<.03?Ve(e):We(e)}function Ne(e,t){return Array.from({length:t-e+1}).map(function(t,n){return {index:n+e}})}var Ue=system(function(e){var t=e[0],a=t.overscan,d=t.visibleRange,f=t.listBoundary,m=e[1],p=m.scrollTop,v=m.viewportHeight,S=m.scrollBy,T=m.scrollTo,x=m.smoothScrollTargetReached,y=m.scrollContainerState,b=e[2],H=e[3],R=e[4],z=R.propsReady,k=R.didMount,B=e[5],E=B.windowViewportRect,L=B.windowScrollTo,O=B.useWindowScroll,M=B.windowScrollContainerState,A=statefulStream(0),V=statefulStream(0),W=statefulStream(Pe),G=statefulStream({height:0,width:0}),N=statefulStream({height:0,width:0}),U=stream(),_=stream(),j=statefulStream(0);connect(pipe(k,withLatestFrom(V),filter(function(e){return 0!==e[1]}),map(function(e){return {items:Ne(0,e[1]-1),top:0,bottom:0,offsetBottom:0,offsetTop:0,itemHeight:0,itemWidth:0}})),W),connect(pipe(combineLatest(duc(A),d,duc(N,function(e,t){return e&&e.width===t.width&&e.height===t.height})),withLatestFrom(G),map(function(e){var t=e[0],n=t[0],o=t[1],r=o[0],i=o[1],l=t[2],a=e[1],u=l.height,s=l.width,c=a.width;if(0===n||0===c)return Pe;if(0===s)return Ae;var d=Ge(c/s),f=d*We(r/u),m=d*Ve(i/u)-1;m=De(n-1,m);var h=Ne(f=De(m,Fe(0,f)),m),p=_e(a,l,h),g=p.top,v=p.bottom;return {items:h,offsetTop:g,offsetBottom:Ve(n/d)*u-v,top:g,bottom:v,itemHeight:u,itemWidth:s}})),W),connect(pipe(G,map(function(e){return e.height})),v),connect(pipe(combineLatest(G,N,W),map(function(e){var t=_e(e[0],e[1],e[2].items);return [t.top,t.bottom]}),distinctUntilChanged(D)),f);var K=streamFromEmitter(pipe(duc(W),filter(function(e){return e.items.length>0}),withLatestFrom(A),filter(function(e){var t=e[0].items;return t[t.length-1].index===e[1]-1}),map(function(e){return e[1]-1}),distinctUntilChanged())),Y=streamFromEmitter(pipe(duc(W),filter(function(e){var t=e.items;return t.length>0&&0===t[0].index}),mapTo(0),distinctUntilChanged())),Z=streamFromEmitter(pipe(duc(W),filter(function(e){return e.items.length>0}),map(function(e){var t=e.items;return {startIndex:t[0].index,endIndex:t[t.length-1].index}}),distinctUntilChanged(F)));connect(Z,H.scrollSeekRangeChanged),connect(pipe(U,withLatestFrom(G,N,A),map(function(e){var t=e[1],n=e[2],o=e[3],r=ke(e[0]),i=r.align,l=r.behavior,a=r.offset,u=r.index;"LAST"===u&&(u=o-1);var s=je(t,n,u=Math.max(0,u,Math.min(o-1,u)));return "end"===i?s=Math.round(s-t.height+n.height):"center"===i&&(s=Math.round(s-t.height/2+n.height/2)),a&&(s+=a),{top:s,behavior:l}})),T);var q=statefulStreamFromEmitter(pipe(W,map(function(e){return e.offsetBottom+e.bottom})),0);return connect(pipe(E,map(function(e){return {width:e.visibleWidth,height:e.visibleHeight}})),G),P({totalCount:A,viewportDimensions:G,itemDimensions:N,scrollTop:p,scrollHeight:_,overscan:a,scrollBy:S,scrollTo:T,scrollToIndex:U,smoothScrollTargetReached:x,windowViewportRect:E,windowScrollTo:L,useWindowScroll:O,windowScrollContainerState:M,deviation:j,scrollContainerState:y,initialItemCount:V},H,{gridState:W,totalListHeight:q},b,{startReached:Y,endReached:K,rangeChanged:Z,propsReady:z})},tup(Oe,U,q,J,K,Me));function _e(e,t,n){var o=t.height;return void 0===o||0===n.length?{top:0,bottom:0}:{top:je(e,t,n[0].index),bottom:je(e,t,n[n.length-1].index)+o}}function je(e,t,n){var o=Ge(e.width/t.width);return We(n/o)*t.height}function Ke(e,t){void 0===t&&(t=!0);var n=react.useRef(null),o=function(e){};if("undefined"!=typeof ResizeObserver){var r=new ResizeObserver(function(t){var n=t[0].target;null!==n.offsetParent&&e(n);});o=function(e){e&&t?(r.observe(e),n.current=e):(n.current&&r.unobserve(n.current),n.current=null);};}return {ref:n,callbackRef:o}}function Ye(e,t){return void 0===t&&(t=!0),Ke(e,t).callbackRef}function Ze(e){var t=react.useRef(null),n=react.useCallback(function(n){if(null!==n){var o=n.getBoundingClientRect(),r=window.innerHeight-Math.max(0,o.top),i=o.top+window.pageYOffset;t.current={offsetTop:i,visibleHeight:r,visibleWidth:o.width},e(t.current);}},[e]),o=Ke(n),r=o.callbackRef,i=o.ref,l=react.useCallback(function(){n(i.current);},[n,i]);return react.useEffect(function(){return window.addEventListener("scroll",l),window.addEventListener("resize",l),function(){window.removeEventListener("scroll",l),window.removeEventListener("resize",l);}},[l]),r}var qe="undefined"!=typeof document?react.useLayoutEffect:react.useEffect;function Je(e,t,n,o,r){return Ye(function(n){for(var i=function(e,t,n,o){var r=e.length;if(0===r)return null;for(var i=[],l=0;l<r;l++){var a=e.item(l);if(a&&void 0!==a.dataset.index){var u=parseInt(a.dataset.index),s=parseFloat(a.dataset.knownSize),c=t(a,"offsetHeight");if(0===c&&o("Zero-sized element, this should not happen",{child:a},N.ERROR),c!==s){var d=i[i.length-1];0===i.length||d.size!==c||d.endIndex!==u-1?i.push({startIndex:u,endIndex:u,size:c}):i[i.length-1].endIndex++;}}}return i}(n.children,t,0,r),l=n.parentElement;!l.dataset.virtuosoScroller;)l=l.parentElement;var a="window"===l.firstElementChild.dataset.viewportType?window.pageYOffset||document.documentElement.scrollTop:l.scrollTop;o([Math.max(a,0),l.scrollHeight]),null!==i&&e(i);},n)}function $e(e,t,n,o){void 0===o&&(o=noop);var r=react.useRef(null),i=react.useRef(null),l=react.useRef(null),a=react.useCallback(function(n){var o=n.target,r=o===window||o===document?window.pageYOffset||document.documentElement.scrollTop:o.scrollTop,a=o===window?document.documentElement.scrollHeight:o.scrollHeight;e([Math.max(r,0),a]),null!==i.current&&(r===i.current||r<=0||r===o.scrollHeight-ve(o,"height"))&&(i.current=null,t(!0),l.current&&(clearTimeout(l.current),l.current=null));},[e,t]);return react.useEffect(function(){var e=r.current;return o(r.current),a({target:e}),e.addEventListener("scroll",a,{passive:!0}),function(){o(null),e.removeEventListener("scroll",a);}},[r,a,n,o]),{scrollerRef:r,scrollByCallback:function(e){null===i.current&&r.current.scrollBy(e);},scrollToCallback:function(n){var o=r.current;if(o&&(!("offsetHeight"in o)||0!==o.offsetHeight)){var a,u,s,c="smooth"===n.behavior;if(o===window?(u=Math.max(ve(document.documentElement,"height"),document.documentElement.scrollHeight),a=window.innerHeight,s=document.documentElement.scrollTop):(u=o.scrollHeight,a=ve(o,"height"),s=o.scrollTop),n.top=Math.ceil(Math.max(Math.min(u-a,n.top),0)),Math.abs(a-u)<1.01||n.top===s)return e([s,u]),void(c&&t(!0));c?(i.current=n.top,l.current&&clearTimeout(l.current),l.current=setTimeout(function(){l.current=null,i.current=null,t(!0);},1e3)):i.current=null,o.scrollTo(n);}}}}var Qe=system(function(e){var t=e[0],n=t.sizes,l=t.listRefresh,a=t.defaultItemSize,u=e[1].scrollTop,c=e[2].scrollToIndex,f=e[3].didMount,m=statefulStream(!0),h=statefulStream(0);return connect(pipe(f,withLatestFrom(h),filter(function(e){return 0!==e[1]}),mapTo(!1)),m),subscribe(pipe(combineLatest(l,f),withLatestFrom(m,n,a),filter(function(e){var t=e[1],n=e[3];return e[0][1]&&(!X(e[2].sizeTree)||void 0!==n)&&!t}),withLatestFrom(h)),function(e){var t=e[1];setTimeout(function(){handleNext(u,function(){publish(m,!0);}),publish(c,t);});}),{scrolledToInitialItem:m,initialTopMostItemIndex:h}},tup(Re,U,Be,K),{singleton:!0});function Xe(e){return !!e&&("smooth"===e?"smooth":"auto")}var et=system(function(e){var t=e[0],n=t.totalCount,r=t.listRefresh,u=e[1],c=u.isAtBottom,f=u.atBottomState,m=e[2].scrollToIndex,g=e[3].scrolledToInitialItem,v=e[4],I=v.propsReady,x=v.didMount,b=e[5].log,H=e[6].scrollingInProgress,R=statefulStream(!1),z=null;function k(e){publish(m,{index:"LAST",align:"end",behavior:e});}return subscribe(pipe(combineLatest(pipe(duc(n),skip(1)),x),withLatestFrom(duc(R),c,g,H),map(function(e){var t=e[0],n=t[0],o=t[1]&&e[3],r="auto";return o&&(r=function(e,t){return "function"==typeof e?Xe(e(t)):t&&Xe(e)}(e[1],e[2]||e[4]),o=o&&!!r),{totalCount:n,shouldFollow:o,followOutputBehavior:r}}),filter(function(e){return e.shouldFollow})),function(e){var t=e.totalCount,n=e.followOutputBehavior;z&&(z(),z=null),z=handleNext(r,function(){getValue(b)("following output to ",{totalCount:t},N.DEBUG),k(n),z=null;});}),subscribe(pipe(combineLatest(duc(R),n,I),filter(function(e){return e[0]&&e[2]}),scan(function(e,t){var n=t[1];return {refreshed:e.value===n,value:n}},{refreshed:!1,value:0}),filter(function(e){return e.refreshed}),withLatestFrom(R,n)),function(e){var t=e[1],n=handleNext(f,function(e){!t||e.atBottom||"SIZE_INCREASED"!==e.notAtBottomBecause||z||(getValue(b)("scrolling to bottom due to increased size",{},N.DEBUG),k("auto"));});setTimeout(n,100);}),subscribe(combineLatest(duc(R),f),function(e){var t=e[1];e[0]&&!t.atBottom&&"VIEWPORT_HEIGHT_DECREASING"===t.notAtBottomBecause&&k("auto");}),{followOutput:R}},tup(Re,q,Be,Qe,K,j,U));function tt(e){return e.reduce(function(e,t){return e.groupIndices.push(e.totalCount),e.totalCount+=t+1,e},{totalCount:0,groupIndices:[]})}var nt=system(function(e){var t=e[0],o=t.totalCount,a=t.groupIndices,d=t.sizes,f=e[1],m=f.scrollTop,h=f.headerHeight,p=stream(),g=stream(),v=streamFromEmitter(pipe(p,map(tt)));return connect(pipe(v,map(prop("totalCount"))),o),connect(pipe(v,map(prop("groupIndices"))),a),connect(pipe(combineLatest(m,d,h),filter(function(e){return be(e[1])}),map(function(e){return ne(e[1].groupOffsetTree,Math.max(e[0]-e[2],0),"v")[0]}),distinctUntilChanged(),map(function(e){return [e]})),g),{groupCounts:p,topItemsIndexes:g}},tup(Re,U)),ot={items:[],topItems:[],offsetTop:0,offsetBottom:0,top:0,bottom:0,topListHeight:0,totalCount:0};function rt(e,t,n){if(0===e.length)return [];if(!be(t))return e.map(function(e){return P({},e,{index:e.index+n,originalIndex:e.index})});for(var o,r=[],i=de(t.groupOffsetTree,e[0].index,e[e.length-1].index),l=void 0,a=0,u=W(e);!(o=u()).done;){var s=o.value;(!l||l.end<s.index)&&(l=i.shift(),a=t.groupIndices.indexOf(l.start)),r.push(P({},s.index===l.start?{type:"group",index:a}:{index:s.index-(a+1)+n,groupIndex:a},{size:s.size,offset:s.offset,originalIndex:s.index,data:s.data}));}return r}function it(e,t,n,o,r){var i=0,l=0;if(e.length>0){i=e[0].offset;var a=e[e.length-1];l=a.offset+a.size;}var u=i,s=o.lastOffset+(n-o.lastIndex)*o.lastSize-l;return {items:rt(e,o,r),topItems:rt(t,o,r),topListHeight:t.reduce(function(e,t){return t.size+e},0),offsetTop:i,offsetBottom:s,top:u,bottom:l,totalCount:n}}var lt,at,ut,st=system(function(e){var t=e[0],a=t.sizes,d=t.totalCount,f=t.data,m=t.firstItemIndex,p=e[1],g=e[2],v=g.visibleRange,S=g.listBoundary,T=g.topListHeight,y=e[3],b=y.scrolledToInitialItem,z=y.initialTopMostItemIndex,k=e[4].topListHeight,B=e[5],E=e[6].didMount,L=statefulStream([]),O=stream();connect(p.topItemsIndexes,L);var M=statefulStreamFromEmitter(pipe(combineLatest(E,duc(v),duc(d),duc(a),duc(z),b,duc(L),duc(m),f),filter(function(e){return e[0]}),map(function(e){var t=e[1],n=t[0],o=t[1],r=e[2],i=e[4],l=e[5],a=e[6],u=e[7],s=e[8],c=e[3],d=c.sizeTree,f=c.offsetTree;if(0===r||0===n&&0===o)return ot;if(X(d))return it(function(e,t,n){if(be(t)){var o=ye(e,t);return [{index:ne(t.groupOffsetTree,o)[0],size:0,offset:0},{index:o,size:0,offset:0,data:n&&n[0]}]}return [{index:e,size:0,offset:0,data:n&&n[0]}]}(i,c,s),[],r,c,u);var m=[];if(a.length>0)for(var h,p=a[0],g=a[a.length-1],v=0,I=W(de(d,p,g));!(h=I()).done;)for(var C=h.value,S=C.value,T=Math.max(C.start,p),x=Math.min(C.end,g),w=T;w<=x;w++)m.push({index:w,size:S,offset:v,data:s&&s[w]}),v+=S;if(!l)return it([],m,r,c,u);var y=a.length>0?a[a.length-1]+1:0,b=function(e,t,n,o){return void 0===o&&(o=0),o>0&&(t=Math.max(t,ge(e,o,Ce).offset)),fe((i=n,a=pe(r=e,t,l=Se),u=pe(r,i,l,a),r.slice(a,u+1)),Te);var r,i,l,a,u;}(f,n,o,y);if(0===b.length)return null;var H=r-1;return it(tap([],function(e){for(var t,r=W(b);!(t=r()).done;){var i=t.value,l=i.value,a=l.offset,u=i.start,c=l.size;l.offset<n&&(a+=((u+=Math.floor((n-l.offset)/c))-i.start)*c),u<y&&(a+=(y-u)*c,u=y);for(var d=Math.min(i.end,H),f=u;f<=d&&!(a>=o);f++)e.push({index:f,size:c,offset:a,data:s&&s[f]}),a+=c;}}),m,r,c,u)}),filter(function(e){return null!==e}),distinctUntilChanged()),ot);return connect(pipe(f,filter(function(e){return void 0!==e}),map(function(e){return e.length})),d),connect(pipe(M,map(prop("topListHeight"))),k),connect(k,T),connect(pipe(M,map(function(e){return [e.top,e.bottom]})),S),connect(pipe(M,map(function(e){return e.items})),O),P({listState:M,topItemsIndexes:L,endReached:streamFromEmitter(pipe(M,filter(function(e){return e.items.length>0}),withLatestFrom(d,f),filter(function(e){var t=e[0].items;return t[t.length-1].originalIndex===e[1]-1}),map(function(e){return [e[1]-1,e[2]]}),distinctUntilChanged(D),map(function(e){return e[0]}))),startReached:streamFromEmitter(pipe(M,throttleTime(200),filter(function(e){var t=e.items;return t.length>0&&t[0].originalIndex===e.topItems.length}),map(function(e){return e.items[0].index}),distinctUntilChanged())),rangeChanged:streamFromEmitter(pipe(M,filter(function(e){return e.items.length>0}),map(function(e){var t=e.items;return {startIndex:t[0].index,endIndex:t[t.length-1].index}}),distinctUntilChanged(F))),itemsRendered:O},B)},tup(Re,nt,Oe,Qe,Be,q,K),{singleton:!0}),ct=system(function(e){var t=e[0],n=t.sizes,a=t.firstItemIndex,u=t.data,c=e[1].listState,d=e[2].didMount,f=statefulStream(0);return connect(pipe(d,withLatestFrom(f),filter(function(e){return 0!==e[1]}),withLatestFrom(n,a,u),map(function(e){var t=e[0][1],n=e[1],o=e[2],r=e[3],i=void 0===r?[]:r,l=0;if(n.groupIndices.length>0)for(var a,u=W(n.groupIndices);!((a=u()).done||a.value-l>=t);)l++;var s=t+l;return it(Array.from({length:s}).map(function(e,t){return {index:t,size:0,offset:0,data:i[t]}}),[],s,n,o)})),c),{initialItemCount:f}},tup(Re,st,K),{singleton:!0}),dt=system(function(e){var t=e[0].topItemsIndexes,n=statefulStream(0);return connect(pipe(n,filter(function(e){return e>0}),map(function(e){return Array.from({length:e}).map(function(e,t){return t})})),t),{topItemCount:n}},tup(st)),ft=system(function(e){var t=e[0],o=t.footerHeight,a=t.headerHeight,u=e[1].listState,s=stream(),c=statefulStreamFromEmitter(pipe(combineLatest(o,a,u),map(function(e){var t=e[2];return e[0]+e[1]+t.offsetBottom+t.bottom})),0);return connect(duc(c),s),{totalListHeight:c,totalListHeightChanged:s}},tup(U,st),{singleton:!0}),mt=system(function(e){var t=e[0],n=t.scrollBy,o=t.scrollTop,a=t.deviation,c=t.scrollingInProgress,f=e[1],m=f.isScrolling,h=f.isAtBottom,p=f.atBottomState,g=f.scrollDirection,v=e[3],y=v.beforeUnshiftWith,b=v.sizes,H=e[4].log,R=streamFromEmitter(pipe(e[2].listState,withLatestFrom(f.lastJumpDueToItemResize),scan(function(e,t){var n=e[1],o=t[0],r=o.items,i=o.totalCount,l=t[1],a=0;if(e[2]===i){if(n.length>0&&r.length>0){var u=1===r.length;if(0!==r[0].originalIndex||0!==n[0].originalIndex)for(var s=function(e){var t=r[e],o=n.find(function(e){return e.originalIndex===t.originalIndex});return o?t.offset!==o.offset||u?(a=t.offset-o.offset+t.size-o.size,"break"):void 0:"continue"},c=r.length-1;c>=0;c--){var d=s(c);if("continue"!==d&&"break"===d)break}}0!==a&&(a+=l);}return [a,r,i]},[0,[],0]),filter(function(e){return 0!==e[0]}),withLatestFrom(o,g,c,H,h,p),filter(function(e){return !e[3]&&0!==e[1]&&e[2]===Y}),map(function(e){var t=e[0][0];return (0, e[4])("Upward scrolling compensation",{amount:t},N.DEBUG),t})));return connect(pipe(R,withLatestFrom(a),map(function(e){return e[1]-e[0]})),a),subscribe(pipe(combineLatest(statefulStreamFromEmitter(m,!1),a),filter(function(e){return !e[0]&&0!==e[1]}),map(function(e){return e[1]}),throttleTime(1)),function(e){e>0?(publish(n,{top:-e,behavior:"auto"}),publish(a,0)):(publish(a,0),publish(n,{top:-e,behavior:"auto"}));}),connect(pipe(y,withLatestFrom(b),map(function(e){return e[0]*e[1].lastSize})),R),{deviation:a}},tup(U,q,st,Re,j)),ht=system(function(e){var t=e[0].totalListHeight,n=e[1].didMount,r=e[2].scrollTo,a=statefulStream(0);return subscribe(pipe(n,withLatestFrom(a),filter(function(e){return 0!==e[1]}),map(function(e){return {top:e[1]}})),function(e){handleNext(pipe(t,filter(function(e){return 0!==e})),function(){setTimeout(function(){publish(r,e);});});}),{initialScrollTop:a}},tup(ft,K,U),{singleton:!0}),pt=system(function(e){var t=e[0].viewportHeight,n=e[1].totalListHeight,r=statefulStream(!1);return {alignToBottom:r,paddingTopAddition:statefulStreamFromEmitter(pipe(combineLatest(r,t,n),filter(function(e){return e[0]}),map(function(e){return Math.max(0,e[1]-e[2])}),distinctUntilChanged()),0)}},tup(U,ft),{singleton:!0}),gt=system(function(e){var t=e[0],o=t.sizes,a=t.totalCount,u=e[1],c=u.scrollTop,d=u.viewportHeight,f=u.headerHeight,m=u.scrollingInProgress,h=e[2].scrollToIndex,g=stream();return connect(pipe(g,withLatestFrom(o,d,a,f,c),map(function(e){var t=e[0],n=t.index,o=t.behavior,r=void 0===o?"auto":o,l=t.done,a=e[1],u=e[2],c=e[4],d=e[5],f=e[3]-1,h=null;n=ye(n,a);var g=we(n=Math.max(0,n,Math.min(f,n)),a.offsetTree)+c;return g<d?h={index:n,behavior:r,align:"start"}:g+ne(a.sizeTree,n)[1]>d+u&&(h={index:n,behavior:r,align:"end"}),h?l&&handleNext(pipe(m,skip(1),filter(function(e){return !1===e})),l):l&&l(),h}),filter(function(e){return null!==e})),h),{scrollIntoView:g}},tup(Re,U,Be,st,j),{singleton:!0}),vt=system(function(e){return P({},e[0],e[1],e[2],e[3],e[4],e[5],e[6],e[7],e[8])},tup(Oe,ct,K,J,ft,ht,pt,Me,gt)),It=system(function(e){var t=e[0],n=t.totalCount,o=t.sizeRanges,a=t.fixedItemSize,u=t.defaultItemSize,s=t.trackItemSizes,c=t.itemSize,d=t.data,f=t.firstItemIndex,m=t.groupIndices,h=e[1],p=h.initialTopMostItemIndex,g=h.scrolledToInitialItem,v=e[2],I=e[3],C=e[4],S=C.listState,T=C.topItemsIndexes,x=A(C,["listState","topItemsIndexes"]),w=e[5].scrollToIndex,y=e[7].topItemCount,b=e[8].groupCounts,R=e[9],z=e[10];return connect(x.rangeChanged,R.scrollSeekRangeChanged),connect(pipe(R.windowViewportRect,map(prop("visibleHeight"))),v.viewportHeight),P({totalCount:n,data:d,firstItemIndex:f,sizeRanges:o,initialTopMostItemIndex:p,scrolledToInitialItem:g,topItemsIndexes:T,topItemCount:y,groupCounts:b,fixedItemHeight:a,defaultItemHeight:u},I,{listState:S,scrollToIndex:w,trackItemSizes:s,itemSize:c,groupIndices:m},x,R,v,z)},tup(Re,Qe,U,et,st,Be,mt,dt,nt,vt,j)),Ct=(lt=function(){if("undefined"==typeof document)return "sticky";var e=document.createElement("div");return e.style.position="-webkit-sticky","-webkit-sticky"===e.style.position?"-webkit-sticky":"sticky"},ut=!1,function(){return ut||(ut=!0,at=lt()),at});function St(e){return e}var Tt=system(function(){var e=statefulStream(function(e){return "Item "+e}),t=statefulStream(function(e){return "Group "+e}),n=statefulStream({}),r=statefulStream(St),a=statefulStream("div"),u=statefulStream(noop),s=function(e,t){return void 0===t&&(t=null),statefulStreamFromEmitter(pipe(n,map(function(t){return t[e]}),distinctUntilChanged()),t)};return {itemContent:e,groupContent:t,components:n,computeItemKey:r,headerFooterTag:a,scrollerRef:u,FooterComponent:s("Footer"),HeaderComponent:s("Header"),TopItemListComponent:s("TopItemList"),ListComponent:s("List","div"),ItemComponent:s("Item","div"),GroupComponent:s("Group","div"),ScrollerComponent:s("Scroller","div"),EmptyPlaceholder:s("EmptyPlaceholder"),ScrollSeekPlaceholder:s("ScrollSeekPlaceholder")}});function xt(e,t){var o=stream();return subscribe(o,function(){return console.warn("react-virtuoso: You are using a deprecated property. "+t,"color: red;","color: inherit;","color: blue;")}),connect(o,e),o}var wt=system(function(e){var t=e[0],o=e[1],u={item:xt(o.itemContent,"Rename the %citem%c prop to %citemContent."),group:xt(o.groupContent,"Rename the %cgroup%c prop to %cgroupContent."),topItems:xt(t.topItemCount,"Rename the %ctopItems%c prop to %ctopItemCount."),itemHeight:xt(t.fixedItemHeight,"Rename the %citemHeight%c prop to %cfixedItemHeight."),scrollingStateChange:xt(t.isScrolling,"Rename the %cscrollingStateChange%c prop to %cisScrolling."),adjustForPrependedItems:stream(),maxHeightCacheSize:stream(),footer:stream(),header:stream(),HeaderContainer:stream(),FooterContainer:stream(),ItemContainer:stream(),ScrollContainer:stream(),GroupContainer:stream(),ListContainer:stream(),emptyComponent:stream(),scrollSeek:stream()};function s(e,t,n){connect(pipe(e,withLatestFrom(o.components),map(function(e){var o,r=e[0],i=e[1];return console.warn("react-virtuoso: "+n+" property is deprecated. Pass components."+t+" instead."),P({},i,((o={})[t]=r,o))})),o.components);}return subscribe(u.adjustForPrependedItems,function(){console.warn("react-virtuoso: adjustForPrependedItems is no longer supported. Use the firstItemIndex property instead - https://virtuoso.dev/prepend-items.","color: red;","color: inherit;","color: blue;");}),subscribe(u.maxHeightCacheSize,function(){console.warn("react-virtuoso: maxHeightCacheSize is no longer necessary. Setting it has no effect - remove it from your code.");}),subscribe(u.HeaderContainer,function(){console.warn("react-virtuoso: HeaderContainer is deprecated. Use headerFooterTag if you want to change the wrapper of the header component and pass components.Header to change its contents.");}),subscribe(u.FooterContainer,function(){console.warn("react-virtuoso: FooterContainer is deprecated. Use headerFooterTag if you want to change the wrapper of the footer component and pass components.Footer to change its contents.");}),subscribe(u.scrollSeek,function(e){var n=e.placeholder,r=A(e,["placeholder"]);console.warn("react-virtuoso: scrollSeek property is deprecated. Pass scrollSeekConfiguration and specify the placeholder in components.ScrollSeekPlaceholder instead."),publish(o.components,P({},getValue(o.components),{ScrollSeekPlaceholder:n})),publish(t.scrollSeekConfiguration,r);}),s(u.footer,"Footer","footer"),s(u.header,"Header","header"),s(u.ItemContainer,"Item","ItemContainer"),s(u.ListContainer,"List","ListContainer"),s(u.ScrollContainer,"Scroller","ScrollContainer"),s(u.emptyComponent,"EmptyPlaceholder","emptyComponent"),s(u.GroupContainer,"Group","GroupContainer"),P({},t,o,u)},tup(It,Tt)),yt=function(e){return react.createElement("div",{style:{height:e.height}})},bt={position:Ct(),zIndex:1,overflowAnchor:"none"},Ht=react.memo(function(e){var t=e.showTopList,n=void 0!==t&&t,o=Ft("listState"),r=Ft("deviation"),i=Dt("sizeRanges"),l=Ft("useWindowScroll"),a=Dt("windowScrollContainerState"),u=Dt("scrollContainerState"),s=l?a:u,c=Ft("itemContent"),d=Ft("groupContent"),f=Ft("trackItemSizes"),m=Ft("itemSize"),h=Ft("log"),p=Je(i,m,f,n?noop:s,h),g=Ft("EmptyPlaceholder"),v=Ft("ScrollSeekPlaceholder")||yt,I=Ft("ListComponent"),C=Ft("ItemComponent"),S=Ft("GroupComponent"),T=Ft("computeItemKey"),x=Ft("isSeeking"),w=Ft("groupIndices").length>0,y=Ft("paddingTopAddition"),H=Ft("scrolledToInitialItem"),R=Ft("firstItemIndex"),z=n?{}:{boxSizing:"border-box",paddingTop:o.offsetTop+y,paddingBottom:o.offsetBottom,marginTop:r};return !n&&0===o.items.length&&g&&H?react.createElement(g):react.createElement(I,{ref:p,style:z,"data-test-id":n?"virtuoso-top-item-list":"virtuoso-item-list"},(n?o.topItems:o.items).map(function(e){var t=e.originalIndex,n=T(t+R,e.data);return x?react.createElement(v,P({key:n,index:e.index,height:e.size,type:e.type||"item"},"group"===e.type?{}:{groupIndex:e.groupIndex})):"group"===e.type?react.createElement(S,{key:n,"data-index":t,"data-known-size":e.size,"data-item-index":e.index,style:bt},d(e.index)):react.createElement(C,{key:n,"data-index":t,"data-known-size":e.size,"data-item-index":e.index,"data-item-group-index":e.groupIndex,style:{overflowAnchor:"none"}},w?c(e.index,e.groupIndex,e.data):c(e.index,e.data))}))}),Rt={height:"100%",outline:"none",overflowY:"auto",position:"relative",WebkitOverflowScrolling:"touch"},zt={width:"100%",height:"100%",position:"absolute",top:0},kt={width:"100%",position:Ct(),top:0},Bt=react.memo(function(){var e=Ft("HeaderComponent"),t=Dt("headerHeight"),n=Ft("headerFooterTag"),o=Ye(function(e){return t(ve(e,"height"))});return e?react.createElement(n,{ref:o},react.createElement(e)):null}),Et=react.memo(function(){var e=Ft("FooterComponent"),t=Dt("footerHeight"),n=Ft("headerFooterTag"),o=Ye(function(e){return t(ve(e,"height"))});return e?react.createElement(n,{ref:o},react.createElement(e)):null});function Lt(e){var t=e.usePublisher,n=e.useEmitter,o=e.useEmitterValue;return react.memo(function(e){var r=e.style,i=e.children,l=A(e,["style","children"]),a=t("scrollContainerState"),u=o("ScrollerComponent"),s=$e(a,t("smoothScrollTargetReached"),u,o("scrollerRef")),c=s.scrollerRef,d=s.scrollByCallback;return n("scrollTo",s.scrollToCallback),n("scrollBy",d),react.createElement(u,P({ref:c,style:P({},Rt,r),"data-test-id":"virtuoso-scroller","data-virtuoso-scroller":"true",tabIndex:0},l),i)})}function Ot(e){var t=e.usePublisher,n=e.useEmitter,o=e.useEmitterValue;return react.memo(function(e){var r=e.style,i=e.children,l=A(e,["style","children"]),a=t("windowScrollContainerState"),u=o("ScrollerComponent"),s=t("smoothScrollTargetReached"),c=o("totalListHeight"),d=o("deviation"),f=$e(a,s,u,noop),m=f.scrollerRef,h=f.scrollByCallback,p=f.scrollToCallback;return qe(function(){return m.current=window,function(){m.current=null;}},[m]),n("windowScrollTo",p),n("scrollBy",h),react.createElement(u,P({style:P({position:"relative"},r,0!==c?{height:c+d}:{}),"data-virtuoso-scroller":"true"},l),i)})}var Mt=function(e){var t=e.children,n=Dt("viewportHeight"),o=Ye(compose(n,function(e){return ve(e,"height")}));return react.createElement("div",{style:zt,ref:o,"data-viewport-type":"element"},t)},Pt=function(e){var t=e.children,n=Ze(Dt("windowViewportRect"));return react.createElement("div",{ref:n,style:zt,"data-viewport-type":"window"},t)},At=function(e){var t=e.children,n=Ft("TopItemListComponent"),o=Ft("headerHeight"),r=P({},kt,{marginTop:o+"px"});return react.createElement(n||"div",{style:r},t)},Vt=systemToComponent(wt,{required:{},optional:{followOutput:"followOutput",firstItemIndex:"firstItemIndex",itemContent:"itemContent",groupContent:"groupContent",overscan:"overscan",increaseViewportBy:"increaseViewportBy",totalCount:"totalCount",topItemCount:"topItemCount",initialTopMostItemIndex:"initialTopMostItemIndex",components:"components",groupCounts:"groupCounts",atBottomThreshold:"atBottomThreshold",computeItemKey:"computeItemKey",defaultItemHeight:"defaultItemHeight",fixedItemHeight:"fixedItemHeight",itemSize:"itemSize",scrollSeekConfiguration:"scrollSeekConfiguration",headerFooterTag:"headerFooterTag",data:"data",initialItemCount:"initialItemCount",initialScrollTop:"initialScrollTop",alignToBottom:"alignToBottom",useWindowScroll:"useWindowScroll",scrollerRef:"scrollerRef",logLevel:"logLevel",item:"item",group:"group",topItems:"topItems",itemHeight:"itemHeight",scrollingStateChange:"scrollingStateChange",maxHeightCacheSize:"maxHeightCacheSize",footer:"footer",header:"header",ItemContainer:"ItemContainer",ScrollContainer:"ScrollContainer",ListContainer:"ListContainer",GroupContainer:"GroupContainer",emptyComponent:"emptyComponent",HeaderContainer:"HeaderContainer",FooterContainer:"FooterContainer",scrollSeek:"scrollSeek"},methods:{scrollToIndex:"scrollToIndex",scrollIntoView:"scrollIntoView",scrollTo:"scrollTo",scrollBy:"scrollBy",adjustForPrependedItems:"adjustForPrependedItems"},events:{isScrolling:"isScrolling",endReached:"endReached",startReached:"startReached",rangeChanged:"rangeChanged",atBottomStateChange:"atBottomStateChange",atTopStateChange:"atTopStateChange",totalListHeightChanged:"totalListHeightChanged",itemsRendered:"itemsRendered",groupIndices:"groupIndices"}},react.memo(function(e){var t=Ft("useWindowScroll"),n=Ft("topItemsIndexes").length>0,o=t?Pt:Mt;return react.createElement(t?Ut:Nt,P({},e),react.createElement(o,null,react.createElement(Bt,null),react.createElement(Ht,null),react.createElement(Et,null)),n&&react.createElement(At,null,react.createElement(Ht,{showTopList:!0})))})),Wt=Vt.Component,Dt=Vt.usePublisher,Ft=Vt.useEmitterValue,Gt=Vt.useEmitter,Nt=Lt({usePublisher:Dt,useEmitterValue:Ft,useEmitter:Gt}),Ut=Ot({usePublisher:Dt,useEmitterValue:Ft,useEmitter:Gt}),_t=system(function(){var e=statefulStream(function(e){return "Item "+e}),t=statefulStream({}),n=statefulStream("virtuoso-grid-item"),r=statefulStream("virtuoso-grid-list"),a=statefulStream(St),u=statefulStream(noop),s=function(e,n){return void 0===n&&(n=null),statefulStreamFromEmitter(pipe(t,map(function(t){return t[e]}),distinctUntilChanged()),n)};return {itemContent:e,components:t,computeItemKey:a,itemClassName:n,listClassName:r,scrollerRef:u,ListComponent:s("List","div"),ItemComponent:s("Item","div"),ScrollerComponent:s("Scroller","div"),ScrollSeekPlaceholder:s("ScrollSeekPlaceholder","div")}}),jt=system(function(e){var t=e[0],o=e[1],u={item:xt(o.itemContent,"Rename the %citem%c prop to %citemContent."),ItemContainer:stream(),ScrollContainer:stream(),ListContainer:stream(),emptyComponent:stream(),scrollSeek:stream()};function s(e,t,n){connect(pipe(e,withLatestFrom(o.components),map(function(e){var o,r=e[0],i=e[1];return console.warn("react-virtuoso: "+n+" property is deprecated. Pass components."+t+" instead."),P({},i,((o={})[t]=r,o))})),o.components);}return subscribe(u.scrollSeek,function(e){var n=e.placeholder,r=A(e,["placeholder"]);console.warn("react-virtuoso: scrollSeek property is deprecated. Pass scrollSeekConfiguration and specify the placeholder in components.ScrollSeekPlaceholder instead."),publish(o.components,P({},getValue(o.components),{ScrollSeekPlaceholder:n})),publish(t.scrollSeekConfiguration,r);}),s(u.ItemContainer,"Item","ItemContainer"),s(u.ListContainer,"List","ListContainer"),s(u.ScrollContainer,"Scroller","ScrollContainer"),P({},t,o,u)},tup(Ue,_t)),Kt=react.memo(function(){var e=Qt("gridState"),t=Qt("listClassName"),n=Qt("itemClassName"),o=Qt("itemContent"),r=Qt("computeItemKey"),i=Qt("isSeeking"),l=$t("scrollHeight"),a=Qt("ItemComponent"),u=Qt("ListComponent"),s=Qt("ScrollSeekPlaceholder"),c=$t("itemDimensions"),d=Ye(function(e){l(e.parentElement.parentElement.scrollHeight);var t=e.firstChild;t&&c(t.getBoundingClientRect());});return react.createElement(u,{ref:d,className:t,style:{paddingTop:e.offsetTop,paddingBottom:e.offsetBottom}},e.items.map(function(t){var l=r(t.index);return i?react.createElement(s,{key:l,index:t.index,height:e.itemHeight,width:e.itemWidth}):react.createElement(a,{className:n,"data-index":t.index,key:l},o(t.index))}))}),Yt=function(e){var t=e.children,n=$t("viewportDimensions"),o=Ye(function(e){n(e.getBoundingClientRect());});return react.createElement("div",{style:zt,ref:o},t)},Zt=function(e){var t=e.children,n=Ze($t("windowViewportRect"));return react.createElement("div",{ref:n,style:zt},t)},qt=systemToComponent(jt,{optional:{totalCount:"totalCount",overscan:"overscan",itemContent:"itemContent",components:"components",computeItemKey:"computeItemKey",initialItemCount:"initialItemCount",scrollSeekConfiguration:"scrollSeekConfiguration",listClassName:"listClassName",itemClassName:"itemClassName",useWindowScroll:"useWindowScroll",scrollerRef:"scrollerRef",item:"item",ItemContainer:"ItemContainer",ScrollContainer:"ScrollContainer",ListContainer:"ListContainer",scrollSeek:"scrollSeek"},methods:{scrollTo:"scrollTo",scrollBy:"scrollBy",scrollToIndex:"scrollToIndex"},events:{isScrolling:"isScrolling",endReached:"endReached",startReached:"startReached",rangeChanged:"rangeChanged",atBottomStateChange:"atBottomStateChange",atTopStateChange:"atTopStateChange"}},react.memo(function(e){var t=P({},e),n=Qt("useWindowScroll"),o=n?Zt:Yt;return react.createElement(n?tn:en,P({},t),react.createElement(o,null,react.createElement(Kt,null)))})),$t=qt.usePublisher,Qt=qt.useEmitterValue,Xt=qt.useEmitter,en=Lt({usePublisher:$t,useEmitterValue:Qt,useEmitter:Xt}),tn=Ot({usePublisher:$t,useEmitterValue:Qt,useEmitter:Xt}),nn=system(function(){var e=statefulStream(function(e){return react.createElement("td",null,"Item $",e)}),t=statefulStream(null),n=statefulStream({}),r=statefulStream(St),a=statefulStream(noop),u=function(e,t){return void 0===t&&(t=null),statefulStreamFromEmitter(pipe(n,map(function(t){return t[e]}),distinctUntilChanged()),t)};return {itemContent:e,fixedHeaderContent:t,components:n,computeItemKey:r,scrollerRef:a,TableComponent:u("Table","table"),TableHeadComponent:u("TableHead","thead"),TableBodyComponent:u("TableBody","tbody"),TableRowComponent:u("TableRow","tr"),ScrollerComponent:u("Scroller","div"),EmptyPlaceholder:u("EmptyPlaceholder"),ScrollSeekPlaceholder:u("ScrollSeekPlaceholder")}}),on=system(function(e){return P({},e[0],e[1])},tup(It,nn)),rn=function(e){return react.createElement("tr",null,react.createElement("td",{style:{height:e.height}}))},ln=function(e){return react.createElement("tr",null,react.createElement("td",{style:{height:e.height,padding:0,border:0}}))},an=react.memo(function(){var e=mn("listState"),t=mn("deviation"),n=fn("sizeRanges"),o=mn("useWindowScroll"),r=fn("windowScrollContainerState"),i=fn("scrollContainerState"),l=o?r:i,a=mn("itemContent"),u=mn("trackItemSizes"),s=Je(n,mn("itemSize"),u,l,mn("log")),c=mn("EmptyPlaceholder"),d=mn("ScrollSeekPlaceholder")||rn,f=mn("TableBodyComponent"),m=mn("TableRowComponent"),h=mn("computeItemKey"),p=mn("isSeeking"),g=mn("paddingTopAddition"),v=mn("scrolledToInitialItem"),I=mn("firstItemIndex");if(0===e.items.length&&c&&v)return react.createElement(c);var C=e.offsetTop+g+t,S=e.offsetBottom,T=C>0?react.createElement(ln,{height:C,key:"padding-top"}):null,x=S>0?react.createElement(ln,{height:S,key:"padding-bottom"}):null,w=e.items.map(function(e){var t=e.originalIndex,n=h(t+I,e.data);return p?react.createElement(d,{key:n,index:e.index,height:e.size,type:e.type||"item"}):react.createElement(m,{key:n,"data-index":t,"data-known-size":e.size,"data-item-index":e.index,style:{overflowAnchor:"none"}},a(e.index,e.data))});return react.createElement(f,{ref:s,"data-test-id":"virtuoso-item-list"},[T].concat(w,[x]))}),un=function(e){var t=e.children,n=fn("viewportHeight"),o=Ye(compose(n,function(e){return ve(e,"height")}));return react.createElement("div",{style:zt,ref:o,"data-viewport-type":"element"},t)},sn=function(e){var t=e.children,n=Ze(fn("windowViewportRect"));return react.createElement("div",{ref:n,style:zt,"data-viewport-type":"window"},t)},cn=systemToComponent(on,{required:{},optional:{followOutput:"followOutput",firstItemIndex:"firstItemIndex",itemContent:"itemContent",fixedHeaderContent:"fixedHeaderContent",overscan:"overscan",increaseViewportBy:"increaseViewportBy",totalCount:"totalCount",topItemCount:"topItemCount",initialTopMostItemIndex:"initialTopMostItemIndex",components:"components",groupCounts:"groupCounts",atBottomThreshold:"atBottomThreshold",computeItemKey:"computeItemKey",defaultItemHeight:"defaultItemHeight",fixedItemHeight:"fixedItemHeight",itemSize:"itemSize",scrollSeekConfiguration:"scrollSeekConfiguration",data:"data",initialItemCount:"initialItemCount",initialScrollTop:"initialScrollTop",alignToBottom:"alignToBottom",useWindowScroll:"useWindowScroll",scrollerRef:"scrollerRef",logLevel:"logLevel"},methods:{scrollToIndex:"scrollToIndex",scrollIntoView:"scrollIntoView",scrollTo:"scrollTo",scrollBy:"scrollBy"},events:{isScrolling:"isScrolling",endReached:"endReached",startReached:"startReached",rangeChanged:"rangeChanged",atBottomStateChange:"atBottomStateChange",atTopStateChange:"atTopStateChange",totalListHeightChanged:"totalListHeightChanged",itemsRendered:"itemsRendered",groupIndices:"groupIndices"}},react.memo(function(e){var t=mn("useWindowScroll"),n=fn("fixedHeaderHeight"),o=mn("fixedHeaderContent"),r=Ye(compose(n,function(e){return ve(e,"height")})),i=t?gn:pn,l=t?sn:un,a=mn("TableComponent"),u=mn("TableHeadComponent"),s=o?react.createElement(u,{key:"TableHead",style:{zIndex:1,position:"sticky",top:0},ref:r},o()):null;return react.createElement(i,P({},e),react.createElement(l,null,react.createElement(a,{style:{borderSpacing:0}},[s,react.createElement(an,{key:"TableBody"})])))})),fn=cn.usePublisher,mn=cn.useEmitterValue,hn=cn.useEmitter,pn=Lt({usePublisher:fn,useEmitterValue:mn,useEmitter:hn}),gn=Ot({usePublisher:fn,useEmitterValue:mn,useEmitter:hn}),vn=Wt;

export { vn as Virtuoso };
