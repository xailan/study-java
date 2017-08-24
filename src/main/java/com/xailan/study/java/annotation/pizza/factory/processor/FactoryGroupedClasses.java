package com.xailan.study.java.annotation.pizza.factory.processor;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.processing.Filer;
import javax.lang.model.util.Elements;

import com.xailan.study.java.annotation.pizza.factory.Factory;

public class FactoryGroupedClasses {
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
		
	}
}
