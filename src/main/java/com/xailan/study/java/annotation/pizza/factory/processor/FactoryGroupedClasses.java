package com.xailan.study.java.annotation.pizza.factory.processor;

import java.io.IOException;
import java.io.Writer;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.JavaFileObject;

import com.squareup.javawriter.JavaWriter;
import com.xailan.study.java.annotation.pizza.factory.Factory;

public class FactoryGroupedClasses {
	private static final String SUFFIX = "Factory";

	private String qualifiedClassName;
	private Map<String, FactoryAnnotatedClass> itemsMap = new LinkedHashMap<String, FactoryAnnotatedClass>();

	public FactoryGroupedClasses(String qualifiedClassName) {
		this.qualifiedClassName = qualifiedClassName;
	}

	public void add(FactoryAnnotatedClass toInsert) throws ProcessingException {

		FactoryAnnotatedClass existing = itemsMap.get(toInsert.getId());
		if (existing != null) {
			// Alredy existing
			throw new ProcessingException(toInsert.getTypeElement(),
					"Conflict: The class %s is annotated with @%s with id ='%s' but %s already uses the same id",
					toInsert.getTypeElement().getQualifiedName().toString(), Factory.class.getSimpleName(),
					toInsert.getId(), existing.getTypeElement().getQualifiedName().toString());
		}
		itemsMap.put(toInsert.getId(), toInsert);
	}

	public void generateCode(Elements elementUtils, Filer filer) throws IOException {
		TypeElement superClassName = elementUtils.getTypeElement(qualifiedClassName);
		String factoryClassName = superClassName.getSimpleName() + SUFFIX;
		JavaFileObject jfo = filer.createSourceFile(qualifiedClassName + SUFFIX);
		Writer writer = jfo.openWriter();
		JavaWriter jw = new JavaWriter(writer);
		// 写包名
		PackageElement pkg = elementUtils.getPackageOf(superClassName);
		if (!pkg.isUnnamed()) {
			jw.emitPackage(pkg.getQualifiedName().toString());
			jw.emitEmptyLine();
		} else {
			jw.emitPackage("");
		}

		jw.beginType(factoryClassName, "class", EnumSet.of(Modifier.PUBLIC));
		jw.emitEmptyLine();
		jw.beginMethod(qualifiedClassName, "create", EnumSet.of(Modifier.PUBLIC), "String", "id");

		jw.beginControlFlow("if (id == null)");
		jw.emitStatement("throw new IllegalArgumentException(\"id is null!\")");
		jw.endControlFlow();

		for (FactoryAnnotatedClass item : itemsMap.values()) {
			jw.beginControlFlow("if (\"%s\".equals(id))", item.getId());
			jw.emitStatement("return new %s()", item.getTypeElement().getQualifiedName().toString());
			jw.endControlFlow();
			jw.emitEmptyLine();
		}

		jw.emitStatement("throw new IllegalArgumentException(\"Unknown id = \" + id)");
		jw.endMethod();
		jw.endType();
		jw.close();
	}
}
