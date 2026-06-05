package com.playmonumenta.libraryofsouls.utils;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.EntitySelectorArgument;
import dev.jorel.commandapi.executors.CommandArguments;
import it.unimi.dsi.fastutil.Pair;
import java.beans.BeanInfo;
import java.beans.Introspector;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.util.TriState;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Display.Billboard;
import org.bukkit.entity.Display.Brightness;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.TextDisplay.TextAlignment;
import org.bukkit.entity.Warden.AngerLevel;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionType;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.TypeReference;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;

public final class ReflectionUtils {
	private ReflectionUtils() {

	}

	private static final String COMMAND = "debuginternals";
	private static final String PERMISSION = "monumenta.commands.debuginternals";
	private static final EntitySelectorArgument.OnePlayer PLAYER_ARGUMENT = new EntitySelectorArgument.OnePlayer("player");
	private static final EntitySelectorArgument.OneEntity ENTITY_ARGUMENT = new EntitySelectorArgument.OneEntity("meower");
	private static final Set<Class<?>> supported = Set.of(
			// Warden
			AngerLevel.class,
			// Display
			Billboard.class, Transformation.class, Matrix4f.class, Brightness.class, TextAlignment.class,
			// Generic Bukkit
			Location.class, Component.class, TriState.class, Particle.class, PotionType.class, PotionEffect.class, Color.class, Vector.class,
			// Primitives
			UUID.class, String.class, int.class, double.class, float.class, boolean.class, void.class);

	public static void register() {
		new CommandAPICommand(COMMAND).withPermission(PERMISSION)
				// .withAliases(ALIAS_ONE, ALIAS_TWO)
				.withArguments(ENTITY_ARGUMENT).executes(ReflectionUtils::callback).register();
	}

	public static void callback(CommandSender sender, CommandArguments args) {
		final var entity = args.getByArgument(ENTITY_ARGUMENT);
		final var type = entity.getType();
		final var typeClass = type.getEntityClass() == null ? Entity.class : type.getEntityClass();
		if (typeClass == null) {
			return;
		}
		// recursiveMeowingNoises(typeClass, sender);
		for (final var a : typeClass.getMethods()) {
			final var readMethod = parseMethod(a);
			sender.sendMessage(readMethod);
		}
	}

	public static void recursiveMeowingNoises(Class<?> clazz, Audience sender) {
		fetchInfoAboutClass(clazz, sender);
		for (final var subClazz : clazz.getInterfaces()) {
			recursiveMeowingNoises(subClazz, sender);
		}
	}

	public static void fetchInfoAboutClass(Class<?> clazz, Audience sender) {
		BeanInfo info = null;
		try {
			info = Introspector.getBeanInfo(clazz, Introspector.IGNORE_ALL_BEANINFO);
		} catch (Exception ignored) {
			// MEOW!
		}
		if (info != null) {
			sender.sendMessage(Component.text("Class: " + clazz.getSimpleName(), NamedTextColor.GOLD));
			for (final var a : info.getPropertyDescriptors()) {
				final var readMethod = a.getReadMethod();
				final var writeMethod = a.getWriteMethod();
				if (readMethod == null && writeMethod == null) {
					continue;
				}
				final var meow = Component.text("- " + a.getDisplayName(), NamedTextColor.DARK_GRAY).appendNewline().append(Component.text("  - Read: " + parseMethod(readMethod), NamedTextColor.LIGHT_PURPLE)).appendNewline().append(Component.text("  - Write: " + parseMethod(writeMethod), NamedTextColor.DARK_PURPLE));
				sender.sendMessage(meow);
			}
		}
	}

	public static String parseMethod(Method method) {
		if (method == null) {
			return "";
		}
		final var sb = new StringBuilder();
		sb.append(method.getReturnType().getSimpleName()).append(' ');
		sb.append(method.getDeclaringClass().getSimpleName()).append('.');
		sb.append(method.getName());
		sb.append((String) Arrays.stream(method.getParameterTypes()).map(Type::getTypeName).collect(Collectors.joining(",", "(", ")")));
		return sb.toString();
	}

	public record Property(String name, Method getter, Method setter, Class<?> type, boolean getterNullable, boolean setterNullable) {}

	private static final List<String> GETTER_PREFIX = Arrays.asList("getCan", "get", "is", "has", "can", "should", "will");
	private static final List<String> SETTER_PREFIX = Arrays.asList("setIs", "setHas", "setCan", "setShould", "setWill", "set");

	private static Map<String, Method> doFilter(Collection<Method> methods, List<String> pre, BiPredicate<Class<?>, Class<?>[]> p) {
		return methods.stream().map(method -> {
			if (!p.test(method.getReturnType(), method.getParameterTypes())) {
				return null;
			}
			if (method.getAnnotation(Deprecated.class) != null) {
				return null;
			}
			for (final var s : pre) {
				if (method.getName().startsWith(s)) {
					return Pair.of(method.getName().substring(s.length()), method);
				}
			}
			return null;
		}).filter(Objects::nonNull).collect(Collectors.groupingBy(Pair::first)).entrySet().stream().filter(e -> e.getValue().size() == 1).collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().get(0).second()));
	}

	private static Property mkProp(String name, Method getter, @Nullable Method setter, Class<?> type, boolean getterNullable, boolean setterNullable) {
		return new Property(name, getter, setter, type, getterNullable, setterNullable);
	}

	public static boolean isOverride(Method subclassMethod) {
		// Cannot override if method is private or static
		final var modifiers = subclassMethod.getModifiers();
		if (Modifier.isPrivate(modifiers) || Modifier.isStatic(modifiers)) {
			return false;
		}
		final var methodName = subclassMethod.getName();
		final var paramTypes = subclassMethod.getParameterTypes();
		final var allParents = findParents(subclassMethod.getDeclaringClass());
		for (Class<?> parent : allParents) {
			if (parent.equals(subclassMethod.getDeclaringClass())) {
				continue;
			}
			try {
				Method parentMethod = parent.getDeclaredMethod(methodName, paramTypes);
				int parentModifiers = parentMethod.getModifiers();
				if (!Modifier.isPrivate(parentModifiers) && !Modifier.isStatic(parentModifiers)) {
					if (parentMethod.getReturnType().isAssignableFrom(subclassMethod.getReturnType())) {
						return true;
					}
				}
			} catch (NoSuchMethodException ignored) {
			}
		}
		return false;
	}

	private static boolean isPublic(Method method) {
		return Modifier.isPublic(method.getModifiers() & method.getDeclaringClass().getModifiers());
	}

	public static ClassDesc process(Class<?> clazz) {
		// Process getters
		final var methods = Arrays.stream(clazz.getDeclaredMethods())
			.filter(ReflectionUtils::isPublic)
			.filter(x -> !isOverride(x))
			.filter(x -> x.getAnnotation(Deprecated.class) == null)
			.collect(Collectors.toCollection(HashSet::new));
		final var getters = doFilter(methods, GETTER_PREFIX, (ret, args) -> ret != void.class && args.length == 0);
		final var setters = doFilter(methods, SETTER_PREFIX, (ret, args) -> ret == void.class && args.length == 1);
		setters.keySet().stream().filter(x -> !getters.containsKey(x)).forEach(it -> System.err.printf("disassociated setter in class %s: %s\n", clazz.getCanonicalName(), it));
		final var props = new ArrayList<Property>();
		// Compare getters and setters
		getters.forEach((name, getter) -> {
			Class<?> getterReturnType = getter.getReturnType();
			Method setter = setters.get(name);
			final var getterNullable = ClassFileAccess.getReturnInvisTyAnn(Nullable.class, getter) != null;
			if (setter != null) {
				final var setterNullable = ClassFileAccess.getParamInvisTyAnn(Nullable.class, setter, 0) != null;
				Class<?>[] setterParamTypes = setter.getParameterTypes();
				if (setterNullable && !getterNullable) {
					System.err.println("warning: you need to write a nullable setter for " + setter);
				}
				// only warn if the getter is nullable but the setter is not
				if (!getterReturnType.equals(setterParamTypes[0]) || (!setterNullable && getterNullable)) {
					System.err.printf("mismatched getter/setter in class %s: %s/%s\n", clazz.getName(), getter.getName(), setter.getName());
				} else {
					methods.remove(getter);
					methods.remove(setter);
					props.add(mkProp(name, getter, setter, getterReturnType, getterNullable, setterNullable));
				}
			} else {
				methods.remove(getter);
				props.add(mkProp(name, getter, null, getterReturnType, getterNullable, false));
			}
		});
		final var methods2 = new HashSet<>(methods);
		for (final var a : methods2) {
			if (!methods.contains(a)) {
				continue;
			}
			final var returnType = a.getReturnType();
			for (final var b : methods2) {
				if (!methods.contains(b)) {
					continue;
				}
				final var setterParamTypes = b.getParameterTypes();
				if (a.getName() == b.getName() && setterParamTypes.length == 1 && returnType == setterParamTypes[0]) {
					methods.remove(a);
					methods.remove(b);
					final var getterNullable = ClassFileAccess.getReturnInvisTyAnn(Nullable.class, a) != null;
					final var setterNullable = ClassFileAccess.getParamInvisTyAnn(Nullable.class, b, 0) != null;
					props.add(mkProp(a.getName(), a, b, returnType, getterNullable, setterNullable));
					break;
				}
			}
		}
		return new ClassDesc(clazz, props, new ArrayList<>(methods), Arrays.asList(clazz.getInterfaces()));
	}

	public static Set<Class<?>> findParents(Class<?> clazz) {
		final var visited = new HashSet<Class<?>>();
		final var queue = new ArrayDeque<Class<?>>();
		queue.add(clazz);
		while (!queue.isEmpty()) {
			final var current = queue.poll();
			if (!visited.add(current)) {
				continue;
			}
			queue.addAll(Arrays.asList(current.getInterfaces()));
		}
		return visited;
	}

	public record ClassDesc(Class<?> clazz, List<Property> properties, List<Method> dissociatedMethods, List<Class<?>> supers) {
		private static final Map<String, Method> GETTERS = new HashMap<>();
		private static final Map<String, Method> SETTERS = new HashMap<>();

	}

	public static final Map<Class<?>, ClassDesc> dataMap = new HashMap<>();

	public static void meow(EntityType type) {
		final var entityClass = type.getEntityClass();
		if (entityClass == null) {
			return;
		}
		final var classes = findParents(entityClass);
		for (final var clazz : classes) {
			// Already added
			if (dataMap.containsKey(clazz)) {
				continue;
			}
			final var data = process(clazz);
			dataMap.put(clazz, data);
		}
	}

	public static void init() {
		for (var type : EntityType.values()) {
			meow(type);
		}
	}

	public static void test(CommandSender sender, CommandArguments args) {
		final var array = new ArrayList<String>();
		final var data = dataMap.get(Creeper.class);
		array.addAll(data.properties.stream().filter(x -> x.getter != null).map(x -> parseMethod(x.getter)).toList());
		array.addAll(data.properties.stream().filter(x -> x.setter != null).map(x -> parseMethod(x.setter)).toList());
		for (var meow : array) {
			sender.sendMessage(meow);
		}
	}

	public class ClassFileAccess {
		private static final Map<Class<?>, ClassNode> cache = new HashMap<>();

		public static ClassNode getNode(Class<?> clazz) {
			return cache.computeIfAbsent(clazz, ignored -> {
				try (final var stream = ClassFileAccess.class.getResourceAsStream("/" + clazz.getName().replace(".", "/") + ".class")) {
					if (stream == null) {
						return null;
					}
					final var cr = new ClassReader(stream);
					final var node = new ClassNode();
					cr.accept(node, ClassReader.SKIP_CODE);
					return node;
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			});
		}
		public static AnnotationNode getReturnInvisTyAnn(Class<?> annotation, Method method) {
			final var node = getNode(method.getDeclaringClass());
			if (node == null) {
				return null;
			}
			for (final var m : node.methods) {
				if (!(m.name.equals(method.getName()) && m.desc.equals(org.objectweb.asm.Type.getMethodDescriptor(method)))) {
					continue;
				}
				final var invisibleTypeAnnotations = m.invisibleTypeAnnotations;
				if (invisibleTypeAnnotations == null) {
					return null;
				}
				return invisibleTypeAnnotations.stream().filter(x -> new TypeReference(x.typeRef).getSort() == TypeReference.METHOD_RETURN).filter(x -> x.desc.equals(org.objectweb.asm.Type.getDescriptor(annotation))).findFirst().orElse(null);
			}
			throw new AssertionError("unreachable");
		}

		public static AnnotationNode getParamInvisTyAnn(Class<?> annotation, Method method, int argIdx) {
			final var node = getNode(method.getDeclaringClass());
			if (node == null) {
				return null;
			}
			for (final var m : node.methods) {
				if (!(m.name.equals(method.getName()) && m.desc.equals(org.objectweb.asm.Type.getMethodDescriptor(method)))) {
					continue;
				}
				final var invisibleTypeAnnotations = m.invisibleTypeAnnotations;
				if (invisibleTypeAnnotations == null) {
					return null;
				}
				return invisibleTypeAnnotations.stream().filter(x -> {
					final var tr = new TypeReference(x.typeRef);
					return tr.getSort() == TypeReference.METHOD_FORMAL_PARAMETER && tr.getFormalParameterIndex() == argIdx;
				}).filter(x -> x.desc.equals(org.objectweb.asm.Type.getDescriptor(annotation))).findFirst().orElse(null);
			}
			throw new AssertionError("unreachable");
		}
	}

}
