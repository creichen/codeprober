package codeprober;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import codeprober.ast.AstNode;
import codeprober.metaprogramming.InvokeProblem;
import codeprober.metaprogramming.PositionRepresentation;
import codeprober.metaprogramming.Reflect;
import codeprober.protocol.PositionRecoveryStrategy;

public class AstInfo {

	public final AstNode ast;
	public final PositionRecoveryStrategy recoveryStrategy;
	public final PositionRepresentation positionRepresentation;
	public final Function<String, Class<?>> loadAstClass;
	public final Class<?> basAstClazz;
	
	private Class<?> locatorTALRoot;
	private boolean loadedLocatorTALRoot;

	
	private final Map<Class<?>, Map<String, Boolean>> hasOverrides = new HashMap<>();
	
	public AstInfo(AstNode ast, PositionRecoveryStrategy recoveryStrategy,
			PositionRepresentation positionRepresentation, Function<String, Class<?>> loadAstClass) {
		this.ast = ast;
		this.recoveryStrategy = recoveryStrategy;
		this.positionRepresentation = positionRepresentation;
		this.loadAstClass = loadAstClass;

		Class<?> baseAstType = ast.underlyingAstNode.getClass();
		while (true) {
			Class<?> parentType = baseAstType.getSuperclass();
			if (parentType == null || !parentType.getPackage().getName().equals(baseAstType.getPackage().getName())) {
				break;
			}
			baseAstType = parentType;
		}
		this.basAstClazz = baseAstType;
	}

	public String getQualifiedAstType(String simpleName) {
		if (basAstClazz.getEnclosingClass() != null) {
			return basAstClazz.getEnclosingClass().getName() + "$" + simpleName;
		}
		return basAstClazz.getPackage().getName() + "." + simpleName;
	}
	
	public Class<?> getLocatorTALRoot() {
		if (!loadedLocatorTALRoot) {
			loadedLocatorTALRoot = true;
			try {
				String underlyingType = (String)Reflect.invoke0(ast.underlyingAstNode, "cpr_locatorTALRoot");
				locatorTALRoot = loadAstClass.apply(getQualifiedAstType(underlyingType));
			} catch (InvokeProblem e) {
				// OK, this is an optional attribute after all
			} catch (ClassCastException e) {
				System.out.println("cpr_locatorTALRoot returned non-String value");
				e.printStackTrace();
			}
		}
		return locatorTALRoot;
	}
	
	public boolean hasOverride0(Class<?> cls, String mthName) {
		Map<String, Boolean> inner = hasOverrides.get(cls);
		if (inner == null) {
			inner = new HashMap<>();
			hasOverrides.put(cls, inner);
		}
		
		Boolean ex = inner.get(mthName);
		if (ex != null) { return ex; }
		
		boolean fresh;
		try {
			cls.getMethod(mthName);
			fresh = true;
		} catch (NoSuchMethodException e) {
			fresh = false;
		}
		inner.put(mthName, fresh);
		return fresh;
	}
}