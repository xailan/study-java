package com.xailan.study.java.annotation.pizza.factory.processor;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

import com.google.auto.service.AutoService;
import com.xailan.study.java.annotation.pizza.factory.Factory;

/**
 * 
 * @author andros 谷歌开发得注解处理器 http://www.importnew.com/15246.html
 */
@AutoService(Process.class)
public class FactoryProcessor extends AbstractProcessor {

	private Types typeUtils; // 镜子
	private Elements elementUtils; // 元素 包、类 、属性、方法
	private Filer filer; // 创建文件
	private Messager messager;
	private Map<String, FactoryGroupedClasses> factoryClasses = new LinkedHashMap<String, FactoryGroupedClasses>();

	@Override
	public synchronized void init(ProcessingEnvironment processingEnv) {
		super.init(processingEnv);
		typeUtils = processingEnv.getTypeUtils();
		elementUtils = processingEnv.getElementUtils();
		filer = processingEnv.getFiler();
		messager = processingEnv.getMessager();
	}

	private void error(Element e, String msg, Object... args) {
		messager.printMessage(Diagnostic.Kind.ERROR, String.format(msg, args), e);
	}

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		// TODO Auto-generated method stub
		// 搜索所有注解为@Factory元素列表
		for (Element annotatedElement : roundEnv.getElementsAnnotatedWith(Factory.class)) {
			// 检查被注解为@Factory的元素是否是一个类 CalzonePizza MargheritaPizza Tiramisu
			if (annotatedElement.getKind() != ElementKind.CLASS) {
				error(annotatedElement, "Only classes can be annotated with @%s", Factory.class.getSimpleName());
				return true; // 退出处理
			}
			// 因为我们已经知道它是ElementKind.CLASS类型，所以可以直接强制转换
			TypeElement typeElement = (TypeElement) annotatedElement;

			try {
				// 获得该对象
				FactoryAnnotatedClass annotatedClass = new FactoryAnnotatedClass(typeElement); // throws
																								// IllegalArgumentException

				if (!isValidClass(annotatedClass)) {
					return true; // 已经打印了错误信息，退出处理过程
				}

				// 所有检查都没有问题，所以可以添加了
				FactoryGroupedClasses factoryClass = factoryClasses.get(annotatedClass.getQualifiedFactoryGroupName());
				if (factoryClass == null) {
					String qualifiedGroupName = annotatedClass.getQualifiedFactoryGroupName();
					factoryClass = new FactoryGroupedClasses(qualifiedGroupName);
					factoryClasses.put(qualifiedGroupName, factoryClass);
				}
				// 如果和其他的@Factory标注的类的id相同冲突，
				// 抛出IdAlreadyUsedException异常
				factoryClass.add(annotatedClass);
				return true;

			} catch (IllegalArgumentException e) {
				// @Factory.id()为空
				error(typeElement, e.getMessage());
				return true;
			} catch (ProcessingException e) {

				// 已经存在
				error(e.getElement(),
						"Conflict: The class %s is annotated with @%s with id ='%s' but %s already uses the same id",
						typeElement.getQualifiedName().toString(), Factory.class.getSimpleName());
				return true;
			}
		}
		return false;
	}

	private boolean isValidClass(FactoryAnnotatedClass item) {
		// 转换为TypeElement, 含有更多特定的方法
		TypeElement classElement = item.getTypeElement();

		// 该类必须是公开类 public
		if (!classElement.getModifiers().contains(Modifier.PUBLIC)) {
			error(classElement, "The class %s is not public.", classElement.getQualifiedName().toString());
			return false;
		}
		// 该类必须是一个非抽象类
		if (classElement.getModifiers().contains(Modifier.ABSTRACT)) {
			error(classElement, "The class %s is abstract. You can't annotate abstract classes with @%",
					classElement.getQualifiedName().toString(), Factory.class.getSimpleName());
			return false;
		}
		// 检查继承关系: 必须是@Factory.type()指定的类型 父类

		// 拿到type上得值得类型。
		TypeElement superClassElement = elementUtils.getTypeElement(item.getQualifiedFactoryGroupName());

		// 判断这个type 是否是接口类型
		if (superClassElement.getKind() == ElementKind.INTERFACE) {
			// 检查接口是否实现了
			if (!classElement.getInterfaces().contains(superClassElement.asType())) {

				error(classElement, "The class %s annotated with @%s must implement the interface %s",
						classElement.getQualifiedName().toString(), Factory.class.getSimpleName(),
						item.getQualifiedFactoryGroupName());
				return false;
			}
			// 如果是类类型
		} else {
			// 检查类
			TypeElement currentClass = classElement;
			while (true) {
				// 得到他得父类
				TypeMirror superClassType = currentClass.getSuperclass();

				if (superClassType.getKind() == TypeKind.NONE) { // 如果超类是Object类型
																	// 跳出 说明没找到
					// 到达了基本类型(java.lang.Object), 所以退出
					error(classElement, "The class %s annotated with @%s must inherit from %s",
							classElement.getQualifiedName().toString(), Factory.class.getSimpleName(),
							item.getQualifiedFactoryGroupName());
					return false;
				}
				// 找到了父类 跳出。
				if (superClassType.toString().equals(item.getQualifiedFactoryGroupName())) {
					// 找到了要求的父类
					break;
				}

				// 在继承树上继续向上搜寻
				currentClass = (TypeElement) typeUtils.asElement(superClassType); // 把当前类型转成它父类。继续往上找
			}
		}
		// 检查是否提供了默认公开构造函数
		for (Element enclosed : classElement.getEnclosedElements()) {
			if (enclosed.getKind() == ElementKind.CONSTRUCTOR) {
				ExecutableElement constructorElement = (ExecutableElement) enclosed;
				// 构造函数中无参数 并且 访问修饰符是public得 就是默认构造函数
				if (constructorElement.getParameters().size() == 0
						&& constructorElement.getModifiers().contains(Modifier.PUBLIC)) {
					// 找到了默认构造函数
					return true;
				}
			}
		}

		// 没有找到默认构造函数
		error(classElement, "The class %s must provide an public empty default constructor",
				classElement.getQualifiedName().toString());
		return false;
	}

	public Set<String> getSupportedAnnotationTypes() {
		Set<String> annotataions = new LinkedHashSet<String>();
		// 本处理器只处理@Factory注解
		annotataions.add(Factory.class.getCanonicalName());
		return annotataions;
	}

}
