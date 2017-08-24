package com.xailan.study.java.annotation.pizza.factory.processor;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.MirroredTypeException;

import org.apache.commons.lang3.StringUtils;

import com.xailan.study.java.annotation.pizza.factory.Factory;

public class FactoryAnnotatedClass {
	private TypeElement annotatedClassElement;
	private String qualifiedSuperClassName;
	private String simpleTypeName;
	private String id;

	public FactoryAnnotatedClass(TypeElement annotatedClassElement) {
		this.annotatedClassElement = annotatedClassElement;
		//找到该类型 得Factory注解
		Factory annotation = annotatedClassElement.getAnnotation(Factory.class);
		//获得注解上得ID
		this.id = annotation.id();
		if (StringUtils.isEmpty(this.id)) {
			throw new IllegalArgumentException(
					String.format("id() in @%s for class %s is null or empty! that's not allowed",
							Factory.class.getSimpleName(), annotatedClassElement.getQualifiedName().toString()));
		}
		try {
			// 这种是编译后得@Factory
			// 拿到该factory注解得class类型
			//获得注解上得 Class对象
			Class<?> clazz = annotation.type();
			// 拿到该类型得全名称 com.xailan.study.java.annotation.pizza.CalzonePizza
			qualifiedSuperClassName = clazz.getCanonicalName();
			// 拿到该类型得简单名称 CalzonePizza
			simpleTypeName = clazz.getSimpleName();
		} catch (MirroredTypeException mte) { // 未编译好得。
			DeclaredType classTypeMirror = (DeclaredType) mte.getTypeMirror();
			TypeElement classTypeElement = (TypeElement) classTypeMirror.asElement();
			qualifiedSuperClassName = classTypeElement.getQualifiedName().toString();
			simpleTypeName = classTypeElement.getSimpleName().toString();
		}
	}

	public String getId() {
		return id;
	}

	/**
	 * 获取在{@link Factory#type()}指定的类型合法全名
	 *
	 * @return qualified name
	 */
	public String getQualifiedFactoryGroupName() {
		return qualifiedSuperClassName;
	}

	/**
	 * 获取在{@link Factory#type()}{@link Factory#type()}指定的类型的简单名字
	 *
	 * @return qualified name
	 */
	public String getSimpleFactoryGroupName() {
		return simpleTypeName;
	}

	/**
	 * 获取被@Factory注解的原始元素
	 */
	public TypeElement getTypeElement() {
		return annotatedClassElement;
	}

}
