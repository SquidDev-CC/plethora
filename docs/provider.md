# Metadata providers
 - Takes an object, returns a `Map<String, Object>`. The `Object` must be
   convertable through CC's conversion system. Must not return null.
 - Bound to a specific target class. Anything implementing that class will be
   passed to it.
 - Can be registered through:
   - `.registerMetaProvider(Class<?> klass, IMetaProvider prov)`
   - `.registerMetaProvider(Class<?> klass, String namespace, IMetaProvider prov)`
   - `@MetaProvider(namespace)` annotation. This will automatically be found and
     added. It would be nice to also register methods this way but that may not be
     feasible.

# Method provider
 - `IMethod` has several members:
   - `String getName()` Get the name of the method. **Note:** How can we support
     aliases.
   - `bool canApply(IContext<T> ctx)` Checks if a method can be applied to a context.
   - `Object[] apply(IContext<T> ctx, IArguments args)` Applies a method to a context.
   - `bool worldThread()` If the method must be executed on the world thread.
 - Registered against a target class, like meta providers.
 - Register through registry or `@LuaMethod(target)`.

## `IContext`
 - `IContext` holds target object, and "context" objects such as parent inventory.
 - Each object is stored as a reference. These references must be valid when the
   function is executed. Example references include:
   - Item stack in inventory: ensures that it hasn't been moved.
   - Entity: ensures it is still alive. This should also be a weak reference.
 - Implicit converters add additional targets. For instance you can provide an
  `Item` from an `ItemStack`. Both method targets should be searched.

**Note:** How to handle capabilities?
